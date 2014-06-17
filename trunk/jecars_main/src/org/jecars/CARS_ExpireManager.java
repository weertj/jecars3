/*
 * Copyright 2007-2014 NLR - National Aerospace Laboratory
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jecars;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.apache.jackrabbit.util.ISO8601;
import org.jecars.jaas.CARS_Credentials;
import org.jecars.tools.CARS_DefaultToolInterface;

/** CARS_ExpireManager
 *
 * @version $Id: CARS_ExpireManager.java,v 1.32 2009/06/05 14:42:38 weertj Exp $
 */
public class CARS_ExpireManager extends CARS_DefaultToolInterface {

  static private final int     MIN_REMOVEDOBJECTS_FOR_LOG = 4;
  static private final long    MAX_ACCESSMANAGER_CACHE    = 1000000L;
  static private       long    gCHECKEVERY                = 7*60000L; // **** 7 minutes
  static private       int     gDATASTORE_GC_TIMES        = 10;     // **** Datastore garbage collect every (10*7) minutes
  static private       int     gGC_BETWEEN_FROM_HOUR      = 0;
  static private       int     gGC_BETWEEN_TO_HOUR        = 6;
  static private       int     gEXPIRE_BETWEEN_FROM_HOUR  = 0;
  static private       int     gEXPIRE_BETWEEN_TO_HOUR    = 24;
  static private       boolean gDISABLED                  = false;

  static private final Object    LOCK = new Object();

  /** setEnabled
   * 
   * @param pEnable
   */
  static public void setEnabled( final boolean pEnable ) {
    gDISABLED = !pEnable;
    return;
  }
  
  /** setDatastoreGCTimes
   *
   * @param pTimes -1 will disable the garbage collector
   */
  static public void setDatastoreGCTimes( final int pTimes ) {
    gDATASTORE_GC_TIMES = pTimes;
    return;
  }

  /** setExpireCheckTime
   *
   * @param pCheckEvery
   */
  static public void setExpireCheckTime( final long pCheckEvery ) {
    gCHECKEVERY = pCheckEvery;
    return;
  }

  private transient long        mLastExpireCheck    = System.currentTimeMillis();
  private transient int         mDataStoreGCCurrent = 0;      // **** Datastore garbage collect every (10*30) seconds
  private transient Session     mSession            = null;

  /** Creates a new instance of CARS_ExpireManager
   */
    public CARS_ExpireManager() {
      super();
      return;
    }
  
  /** Superclass must implement this method to actually start the tool
   */
    @Override
  protected void toolRun() throws Exception {
    super.toolRun();
    if (!gDISABLED) {
      try {
        final Node n = getTool();
        mSession = n.getSession().getRepository().login( new CARS_Credentials( CARS_Definitions.gSuperuserName, "".toCharArray(), null ));
        purge();
      } catch (ConstraintViolationException cve) {
        cve.printStackTrace();
        LOG.log( Level.SEVERE, cve.getMessage(), cve );
      } finally {
        mSession.logout();
        mSession = null;
      }
    }
    return;
  }

  /** shutdown the expire manager
   */
  public void shutdown(  ) {
    try {
      setStateRequest( STATEREQUEST_ABORT );
    } catch (Exception e) {
      LOG.log( Level.WARNING, null, e );
    }
    if (mSession!=null) {        
      mSession.logout();
      mSession = null;
    }
    return;
  }
  
  /** getExpireNodes
   * @param pQM
   * @param pTime
   * @return
   * @throws javax.jcr.query.InvalidQueryException
   * @throws javax.jcr.RepositoryException
   */
  public NodeIterator getExpireNodes( final QueryManager pQM, final Calendar pTime) throws InvalidQueryException, RepositoryException {
    final String qu = "SELECT * FROM jecars:root WHERE jecars:ExpireDate<= TIMESTAMP '" +
            ISO8601.format(pTime) + "'";
    final Query q = pQM.createQuery( qu, Query.SQL );
    final QueryResult qr = q.execute();
    return qr.getNodes();    
  }

  /** Purge, search for objects which can be expired
   *
   * @throws RepositoryException
   */
  private void purge(  ) throws RepositoryException {
    if (mSession==null) {
      return;
    }
    final Calendar cal = Calendar.getInstance( Locale.getDefault() );
    if ((cal.get( Calendar.HOUR_OF_DAY )<gEXPIRE_BETWEEN_FROM_HOUR) ||
        (cal.get( Calendar.HOUR_OF_DAY )>gEXPIRE_BETWEEN_TO_HOUR)) {
      LOG.info( "ExpireManager: Expire not running " + cal.get( Calendar.HOUR_OF_DAY ) + " not between " + gEXPIRE_BETWEEN_FROM_HOUR + " : " + gEXPIRE_BETWEEN_TO_HOUR );
      return;
    }
    if ((System.currentTimeMillis()-gCHECKEVERY)>mLastExpireCheck) {
      try {
        synchronized( LOCK ) {
          if ((++mDataStoreGCCurrent)>=gDATASTORE_GC_TIMES) {
            if ((cal.get( Calendar.HOUR_OF_DAY )>=gGC_BETWEEN_FROM_HOUR) &&
                (cal.get( Calendar.HOUR_OF_DAY )<=gGC_BETWEEN_TO_HOUR)) {
              try {
                int du = CARS_Factory.getLastFactory().getSessionInterface().runGarbageCollector( mSession );
                if (du>0) {
                  LOG.info( "ExpireManager: Ready removing " + du + " datastore objects" );
                }
                // **** Check accessmanager cache size
                final long cs = CARS_Factory.getLastFactory().getAccessManager().getCacheSize();
                if (cs>MAX_ACCESSMANAGER_CACHE) {
                  CARS_Factory.getLastFactory().getAccessManager().clearPathCache();
                  LOG.info( "ExpireManager: Accessmanager cache CLEAR" );
                }
                mDataStoreGCCurrent = 0;
              } catch( NullPointerException npe) {
                LOG.log( Level.WARNING, "Garbage Collector", npe );
              } finally {
  //              if (gc!=null) {
  //                gc.close();
  //              }
              }
            } else {
              // **** GC sweep not between
              mDataStoreGCCurrent = 0;
              LOG.info( "ExpireManager: GC not running " + cal.get( Calendar.HOUR_OF_DAY ) + " not between " + gGC_BETWEEN_FROM_HOUR + " : " + gGC_BETWEEN_TO_HOUR );
            }
          }
          
          CARS_Main main = null;
          mLastExpireCheck = System.currentTimeMillis();
          final Calendar c = Calendar.getInstance();
          final QueryManager qm = mSession.getWorkspace().getQueryManager();
          final NodeIterator ni = getExpireNodes( qm, c );
          if ((ni!=null) && (ni.hasNext())) {
            int totremoved = 0;
            final List<String> removeNodeList = new ArrayList<String>();
            while( ni.hasNext() ) {
              removeNodeList.add( ni.nextNode().getPath() );
            }
            for( String enp : removeNodeList ) {
//            while( ni.hasNext() ) {
//              final Node en = ni.nextNode();
              if (mSession.itemExists( enp )) {
                final Node en = mSession.getNode( enp );
                try {
                  if (en.getReferences().getSize()==0) {
                    if (main==null) {
                      main = CARS_Factory.getLastFactory().createMain( mSession );
                    }
                    final int removed = removeNodes( main, en, 0 );
                    if (removed>MIN_REMOVEDOBJECTS_FOR_LOG) {
                      LOG.log( Level.INFO, "ExpireManager: " + removed + " removed" );
                    }
                    totremoved += removed;
                  } else {
                    // **** There are still references
                  }
                  // **** TODO Elderberry
//                } catch( NoSuchItemStateException nsise ) {
                  // **** The node is already removed
                } catch( InvalidItemStateException iise ) {
                  // **** The node is already removed
                  //            } catch( NoSuchItemStateException nsise ) {
                  // ****
                } catch( ItemNotFoundException infe ) {
                  // ****
                } catch( Exception e ) {
                  // **** Catch general exception
                  LOG.log( Level.WARNING, "ExpireManager", e );
                }
              }
            }
            if (totremoved>MIN_REMOVEDOBJECTS_FOR_LOG) {
              LOG.log( Level.INFO, "ExpireManager: Ready removing " + totremoved + " objects." );
            }
          }
        }
      } finally {
        try {
          final Node tool = getTool();
          if (tool!=null) {
            tool.save();
            tool.setProperty( CARS_ActionContext.gDefLastAccessed, Calendar.getInstance() );
            if (tool.hasProperty( CARS_ActionContext.gDefExpireDate )) {
              tool.setProperty( CARS_ActionContext.gDefExpireDate, (Calendar)null );              
            }
            tool.getSession().save();
          }
        } catch( InvalidItemStateException iise ) {
          LOG.log( Level.WARNING, iise.getMessage(), iise );
        }
      }
    }
    return;
  }

  /** removeNodes
   *
   * @param pMain
   * @param pNode
   * @param pRemoved
   * @return
   * @throws Exception
   */
  private int removeNodes(final CARS_Main pMain, final Node pNode, final int pRemoved) throws Exception {
    int removed = pRemoved;
    if (pNode.hasNodes()) {
      Node n;
      final NodeIterator ni = pNode.getNodes();
      while (ni.hasNext()) {
        n = ni.nextNode();
        try {
          removed += removeNodes( pMain, n, pRemoved );
        } catch( ConstraintViolationException cve ) {
//          cve.printStackTrace();
        }
      }
    }
    try {
      pMain.removeNode( pNode.getPath(), null );
      removed++;
    } catch( ConstraintViolationException cve ) {
//      cve.printStackTrace();
    } catch( PathNotFoundException pe ) {
      try {
        // **** Try a normal remove
        final Node parent = pNode.getParent();
        pNode.remove();
        parent.setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
        parent.save();
        removed++;
      } catch( RepositoryException re) {
        re.printStackTrace();
      }
    }
    return removed;
  }

  /** isScheduledTool
   *
   * @return
   */
  @Override
  public boolean isScheduledTool(  ) {
    return true;
  }

  /** getDelayInSecs
   *
   * @return
   */
  @Override
  public long getDelayInSecs() {
    return gCHECKEVERY/1000;
  }

  /** createToolSession
   *
   * @return
   * @throws javax.jcr.RepositoryException
   */
  @Override
  public Session createToolSession(  ) throws RepositoryException {
    return getTool().getSession().getRepository().login( new CARS_Credentials( CARS_Definitions.gSuperuserName, "".toCharArray(), null ));
  }

  @Override
  public int getRunningExpireMinutes() {
    return -1;
  }

  @Override
  public int getClosedExpireMinutes() {
    return -1;
  }

}
