/*
 * Copyright 2007-2010 NLR - National Aerospace Laboratory
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
package org.jecars.apps;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.Query;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_EventManager;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_RESTMethodHandled;
import org.jecars.CARS_Utils;
import org.jecars.support.CARS_Mime;
import org.jecars.tools.CARS_ToolNode;
import org.jecars.tools.CARS_ToolSignal;
import org.jecars.tools.CARS_ToolSignalManager;

/**
 * CARS_DefaultInterface
 *
 * @version $Id: CARS_DefaultInterface.java,v 1.31 2009/07/30 12:07:42 weertj Exp $
 */
public class CARS_DefaultInterface implements CARS_Interface, EventListener {
    
  static final public Logger gLog = Logger.getLogger( "org.jecars.apps" );

  private String mToBeCheckedInterface = "";
  
  /** Retrieves the name of the application source
   * @return
   */
  @Override
  public String getName() {
    return "System";
  }

  /** getToBeCheckedInterface
   *
   * @return
   */
  protected String getToBeCheckedInterface() {
    return mToBeCheckedInterface;
  }
  
  public void setToBeCheckedInterface( final String pI ) {
    mToBeCheckedInterface = pI;
    return;
  }

    
  /** Will be be called only once, when JeCARS is started
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source
   */
  @Override
  public void init( final CARS_Main pMain, final Node pInterfaceNode ) throws Exception {
    gLog.log( Level.INFO, "Application source init: " + pInterfaceNode.getPath() );
    reportVersionInfo( pMain, getVersion(), null );
    return;
  }

  /** reportVersionInfo
   *
   * @param pMain
   * @param pMessage
   * @param pBody
   * @throws java.lang.Exception
   */
  protected void reportVersionInfo( final CARS_Main pMain, final String pMessage, final String pBody ) throws Exception {
    final List<String> vef = getVersionEventFolders();
    final Session ses = CARS_Factory.getSystemApplicationSession();
    final CARS_EventManager eman = CARS_Factory.getEventManager();
    synchronized( ses ) {
      for( String v : vef ) {
        eman.addEvent( pMain, null, null, v, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_LOGIN, pMessage, null, pBody );
      }
    }
    return;
  }
 
  /** getVersionEventFolders
   * 
   * @return
   */
  @Override
  public ArrayList<String>getVersionEventFolders() {
    final ArrayList<String>ef = new ArrayList<String>();
    ef.add( "/JeCARS/default/Events/System/jecars:EventsVERSION" );
    return ef;
  }
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + " " + new Date().toString();
  }
    
    /** _createHttpConnection
     * @param pURL
     * @return
     * @throws java.lang.Exception
     */
    protected HttpURLConnection _createHttpConnection( String pURL ) throws Exception {   
      URL u = new URL( pURL );
      HttpURLConnection uc = (HttpURLConnection)u.openConnection();
      uc.setUseCaches( false );
      uc.setRequestProperty( "Connection", "Keep-Alive" );
      uc.setDoOutput( true );
      return uc;
    }

  /** _importStreamAsBackup
   * 
   * @param pNode
   * @param pBody
   * @throws javax.jcr.RepositoryException
   * @throws java.io.IOException
   */
 // **** TODO Elderberry
 /*
  private void _importStreamAsBackup( Node pNode, InputStream pBody ) throws RepositoryException, IOException {
    JB_ImportData imp = new JB_ImportData();
    JB_Options options = new JB_Options();
    options.setImportNamespaces( false );
    options.setImportNodeTypes( false );
    imp.importFromStream( pNode, pBody, options );
    return;
  }
  */
   
  /** Store a binary stream, on default the jecars:datafile node type is supported.
   *  If the pNode is an other type the method will stored the data in a Binary property
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pNode the node in which the data will be stored
   * @param pMimeType the mime type of the data if known, otherwise NULL, or "" when changes are neccesary
   * @return true when a update on the node is performed
   * @throws Exception when an error occurs.
   */
  @SuppressWarnings("empty-statement")
  @Override
  public boolean setBodyStream( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final InputStream pBody, final String pMimeType ) throws Exception {
    boolean changed = false;
    if (pMimeType==null) return changed;
    if (pMimeType.equals( CARS_Mime.BACKUP_MIMETYPE )) {
  // **** TODO Elderberry
//      _importStreamAsBackup( pNode, pBody );
      return true;
    }
    if (pNode.isNodeType( "jecars:datafile" )) {
      if (pBody!=null) {
        pNode.setProperty( "jcr:data", pNode.getSession().getValueFactory().createBinary( pBody ) );
        changed = true;
      }
    } else {
      // **** Find a binary property and stored the data.
      final PropertyDefinition pds[] = pNode.getPrimaryNodeType().getPropertyDefinitions();
      int i=0;
      for( ; i<pds.length; i++ ) {
        if (pds[i].getRequiredType()==PropertyType.BINARY) {
          pNode.setProperty( pds[i].getName(), pNode.getSession().getValueFactory().createBinary( pBody ) );
          i = -1;
          changed = true;
          break;
        }
      }
      if (i!=-1) {
//        if (pBody!=null) {
         // ***** TODO
//        }
        return changed;
      }
    }      
    try { // just trying
      pNode.setProperty( "jcr:lastModified", Calendar.getInstance() );
      changed = true;
    } catch (Exception e) {
    };
    try { // just trying
      // **** If the mimetype is an empty string "", then no mimetype will be set.
      if (!"".equals(pMimeType)) {
        pNode.setProperty( "jcr:mimeType", pMimeType );
      }
      changed = true;
    } catch (Exception e) {
    };
    return changed;
  }

     
  /** Add a node to the repository
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pParentNode the node under which the object must be added
   * @param pName the node name
   * @param pPrimType the node type
   * @param pParams list of parameters
   */
  @Override
  public Node addNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pName, final String pPrimType, final JD_Taglist pParams ) throws Exception {
    if (pParentNode.isNodeType( "jecars:Tool" )) {
      CARS_ToolNode toolNode = CARS_ToolNode.newInstance( pMain, pParentNode, false );
      return toolNode.addNode( pMain, pInterfaceNode, pParentNode, pName, pPrimType, pParams );
    } else {
      return CARS_DefaultMain.addNode( pParentNode, pName, pPrimType );
    }
  }

  /** copyNode
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pCopyNode
   * @param pName
   * @param pPrimType
   * @param pParams
   * @return
   * @throws Exception
   */
  @Override
  public Node copyNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final Node pCopyNode, final String pName, final String pPrimType, final JD_Taglist pParams ) throws Exception {

    pMain.getSession().getWorkspace().copy( pCopyNode.getPath(), pParentNode.getPath() + '/' + pName );
    return pMain.getSession().getNode( pParentNode.getPath() + '/' + pName );

   // **** Changed in v1.3.2
//    final PropertyIterator pi = pCopyNode.getProperties();
//    Property cprop;
//    while( pi.hasNext() ) {
//      cprop = pi.nextProperty();
//      if (cprop.getType()==PropertyType.BINARY) {
//        // **** Binary property
//        pParams.replaceData( cprop.getName(), cprop.getStream() );
//      } else if (pParams.getData( cprop.getName() )==null) {
//        pParams.replaceData( cprop.getName(), cprop.getValue().getString() );
//      }
//    }
//    return addNode( pMain, pInterfaceNode, pParentNode, pName, pPrimType, pParams );
  }



  /** initGetNodes
   *  Before the getNodes() is called the initGetNodes() is called to provide the plugin with means to
   *  process the init information
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pPathParts
   * @param pCurrentIndex
   * @throws java.lang.Exception
   */
  @Override
  public void initGetNodes(     CARS_Main pMain, Node pInterfaceNode, Node pParentNode,
                                ArrayList<String>pPathParts, int pCurrentIndex ) throws Exception {
    return;
  }


    /** getNodes
     *
     * @param pMain
     * @param pInterfaceNode
     * @param pParentNode
     * @param pLeaf
     * @throws org.jecars.CARS_RESTMethodHandled
     * @throws java.lang.Exception
     */
    @Override
    public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws CARS_RESTMethodHandled, Exception {
      return;
    }

    /** initHeadNodes
     * 
     * @param pMain
     * @param pInterfaceNode
     * @param pParentNode
     * @param pPathParts
     * @param pCurrentIndex
     * @throws java.lang.Exception
     */
    @Override
    public void initHeadNodes(     CARS_Main pMain, Node pInterfaceNode, Node pParentNode,
                                ArrayList<String>pPathParts, int pCurrentIndex ) throws Exception {
      return;
    }

    /** headNodes
     * 
     * @param pMain
     * @param pInterfaceNode
     * @param pParentNode
     * @param pLeaf
     * @throws java.lang.Exception
     */
    @Override
    public void headNodes( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pLeaf ) throws Exception {
      getNodes( pMain, pInterfaceNode, pParentNode, pLeaf );
      return;
    }


   /** nodeAdded
    * @param pMain
    * @param pInterfaceNode
    * @param pNewNode
    * @param pBody
    * @throws java.lang.Exception
    */
    @SuppressWarnings("empty-statement")
    @Override
    public void nodeAdded( final CARS_Main pMain, final Node pInterfaceNode, final Node pNewNode, final InputStream pBody )  throws Exception {

      if (pNewNode.getParent().isNodeType( "jecars:Tool" )) {
        final CARS_ToolNode toolNode = CARS_ToolNode.newInstance( pMain, pNewNode.getParent(), false );
        toolNode.nodeAdded( pMain, pInterfaceNode, pNewNode, pBody );
      }

      final CARS_ActionContext ac = pMain.getContext(0);
      if (ac!=null) {
        ac.setErrorCode( HttpURLConnection.HTTP_CREATED );
      }
      try { // just trying
        if (pMain.mayChangeNode( pNewNode )) {
          CARS_Utils.setCurrentModificationDate( pNewNode );
        }
      } catch (RepositoryException re) {
        // **** modification not allowed
      }
      return;
    }

    /** A node has been added (addNode) and saved
     *
     * @param pMain
     * @param pInterfaceNode
     * @param pNewNode
     * @throws java.lang.Exception
     */
    @Override
    public void nodeAddedAndSaved( CARS_Main pMain, Node pInterfaceNode, Node pNewNode )  throws Exception {
      if (pNewNode.getParent().isNodeType( "jecars:Tool" )) {
        final CARS_ToolNode toolNode = CARS_ToolNode.newInstance( pMain, pNewNode.getParent(), false );
        toolNode.nodeAddedAndSaved( pMain, pInterfaceNode, pNewNode );
      }
      return;
    }


    /** Remove a node from the JeCARS repository
     * @param pMain the CARS_Main object
     * @param pInterfaceNode the Node which defines the application source or NULL
     * @param pNode the node which has to be removed
     * @param pParams list of parameters
     */ 
    @Override
    public void removeNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final JD_Taglist pParams ) throws Exception {
      CARS_ActionContext ac = pMain.getContext();
      if ((pParams!=null) && (pParams.getData( CARS_ActionContext.gDefNamePattern )!=null)) {
        final String pattern = (String)pParams.getData( CARS_ActionContext.gDefNamePattern );
        final NodeIterator ni = pNode.getNodes( pattern );
        while( ni.hasNext() ) {
          final Node dn = ni.nextNode();
          if (ac!=null) ac.addDeletedNodePath( dn.getPath() );
          removeJeCARSNode( pMain, pInterfaceNode, dn, pParams );
        }
        pNode.save();
      } else {
        final Node parent = pNode.getParent();
        final String pp = pNode.getPath();
//          System.out.println("REMVOE " + pp );
        removeJeCARSNode( pMain, pInterfaceNode, pNode, pParams );
        if (ac!=null) {
          ac.addDeletedNodePath( pp );
        }
        if (parent!=null) {
          parent.save();
        }
      }        
      return;
    }

    /** removeGroupMembers
     * 
     * @param pPath
     * @throws RepositoryException
     * @throws Exception 
     */
    private void removeGroupMembers( final String pPath ) throws RepositoryException, Exception {
      // **** Check path properties
      final Session appSession = CARS_Factory.getSystemApplicationSession();
      synchronized( appSession ) {
        final String query = "SELECT * FROM jecars:root WHERE jecars:GroupMembers = '" + pPath + "'";
        final Query q = appSession.getWorkspace().getQueryManager().createQuery( query, Query.SQL );
        final NodeIterator ni = q.execute().getNodes();
        while( ni.hasNext() ) {
          final Node n = ni.nextNode();
          CARS_Utils.removeMultiProperty( n, "jecars:GroupMembers", pPath );
          if (!n.hasProperty( "jecars:GroupMembers" )) {
            n.remove();
            n.getParent().setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
          }
        }
        appSession.save();
      }
      return;
    }
    
    /** Remove a node from the JeCARS repository, this is the actual remove procedure
     * @param pMain the CARS_Main object
     * @param pInterfaceNode the Node which defines the application source or NULL
     * @param pNode the node which has to be removed
     * @param pParams list of parameters
     * @throws RepositoryException
     */
    protected void removeJeCARSNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final JD_Taglist pParams ) throws RepositoryException {
      String force = null, tc = null;
      final boolean forced;
      if (pParams==null) {
        forced = false;
      } else {
        force  = (String)pParams.getData( "jecars:force" );
        tc     = (String)pParams.getData( "jecars:trashcan" );
        forced = "true".equals( force );
      }
      final String path = pNode.getPath();
      try {
        if (tc!=null) throw new Exception( "move to trashcan" );
        final Node parent = pNode.getParent();
        synchronized( CARS_Factory.getLastFactory().getAccessManager().getExclusiveControlObject() ) {
          pNode.remove();
          pNode.save();
          try {
            CARS_Utils.setCurrentModificationDate( parent );
            parent.save();
          } catch( RepositoryException re ) {
            // **** Just trying to write the parent modified date
          }
        }
      } catch( Exception cve ) {
        // **** Cannot remove object, check other parameters
        if (forced) {
          if (tc!=null) {
            Node trashcan;
            if ("true".equals( tc )) {
              Session appSession = CARS_Factory.getSystemApplicationSession();              
              synchronized( appSession ) {
                // **** Use default trashcan
                Node fromParent = pNode.getParent();
                trashcan = appSession.getRootNode().getNode( "JeCARS/jecars:Trashcans/jecars:General" );
                final String fromPath = pNode.getPath();
                final String toPath   = trashcan.getPath() + "/(" + trashcan.getProperty( "jecars:ObjectCount" ).getLong() + ")_" + pNode.getName();
                trashcan.setProperty( "jecars:ObjectCount", trashcan.getProperty( "jecars:ObjectCount" ).getLong()+1 );
//                fromParent = appSession.getNodeByUUID( fromParent.getUUID() );
                fromParent = appSession.getRootNode().getNode( fromParent.getPath().substring(1) );
                appSession.getWorkspace().move( fromPath, toPath );
//              fromParent.save();
                final Node to = appSession.getRootNode().getNode( toPath.substring(1) );
                to.addMixin( "jecars:trashed");
                to.setProperty( "jecars:RestorePath", fromPath );            
//            System.out.println( "saving:: " + fromParent.getPath() );
//            CARS_Factory.gSystemCarsSession.save();
                trashcan.save();
              }
            }                
          }
        }
      } finally {
        if (forced) {
          try {
            removeGroupMembers( path );
          } catch(Exception e) {
            pMain.getSession().save();
            throw new RepositoryException( e );
          }
        }
        pMain.getSession().save();
      }
      return;
    }


    /** Set param property implementation
     *  A check is performed for "jecars:StateRequest" property in a "jecars:Tool" node type.
     *   this will result in a lock of the node and setStateRequest() in the CARS_ToolInterface.
     * 
     * @param pMain
     * @param pInterfaceNode
     * @param pNode
     * @param pPropName
     * @param pValue
     * @return
     * @throws java.lang.Exception
     */
    @Override
    public Property setParamProperty( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final String pPropName, final String pValue ) throws Exception {
      
      if (pNode.isNodeType( "jecars:Tool" )) {
        final CARS_ToolNode toolNode = CARS_ToolNode.newInstance( pMain, pNode, false );
        final Property resultProp = toolNode.setParamProperty( pMain, pInterfaceNode, pNode, pPropName, pValue );
        if (resultProp!=null) {
          return resultProp;
        }
        if ("jecars:LastToolSignal".equals( pPropName )) {
          CARS_ToolSignalManager.sendSignal( pNode.getPath(), CARS_ToolSignal.valueOf( pValue ) );
        }
      }
      return pMain.setParamProperty( pNode, pPropName, pValue );
    }
    
  /** Add a node
   * @param pParentNode
   * @param pSourceNode
   * @param pNewNodeTypeName
   * @return
   * @throws java.lang.Exception
   */
  protected Node synchronizeNode_AddNewNode( Node pParentNode, Node pSourceNode, String pNewNodeTypeName ) throws Exception {
    Node n = pParentNode.addNode( pSourceNode.getName(), pNewNodeTypeName );
    Calendar c = Calendar.getInstance();
    n.setProperty( CARS_ActionContext.DEF_MODIFIED, c );
    n.setProperty( CARS_ActionContext.gDefLastAccessed, Calendar.getInstance() );
    return n;
  }
    
  /** synchronizeNode_RemoveNode
   * @param pParentNode
   * @param pSourceNode
   * @throws java.lang.Exception
   */
  protected void synchronizeNode_RemoveNode( Node pParentNode, Node pSourceNode ) throws Exception {
    pSourceNode.remove();
    return;
  }
  
  /** synchronizeNode
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pSourceNode
   * @param pNewNodeTypeName
   * @throws java.lang.Exception
   */
  protected void synchronizeNode( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, Node pSourceNode, String pNewNodeTypeName ) throws Exception {
    NodeIterator nis = pSourceNode.getNodes();
    synchronizeNode( pMain, pInterfaceNode, pParentNode, nis, pNewNodeTypeName );
    return;
  }

  /** synchronizeNode, add new nodes and remove them according to the given pNodeIt
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pNodeIt the source node
   * @param pNewNodeTypeName when creating a new node use this node type name
   * @throws java.lang.Exception
   */
  protected void synchronizeNode( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, NodeIterator pNodeIt, String pNewNodeTypeName ) throws Exception {
    Calendar synchTime = Calendar.getInstance();
    Thread.sleep(10);
//    NodeIterator nis = pSourceNode.getNodes();
    NodeIterator nis = pNodeIt;
    while( nis.hasNext() ) {
      Node nn = nis.nextNode();
      if (pParentNode.hasNode( nn.getName() )==false) {
        Node newNode = synchronizeNode_AddNewNode( pParentNode, nn, pNewNodeTypeName );
//        confAppEvent( pMain, newNode, "Applications/pNewNodeTypeName, pNewNodeTypeName, pNewNodeTypeName, pNewNodeTypeName)
      } else {
        // **** Node already exists
        Node ne = pParentNode.getNode( nn.getName() );
        ne.setProperty( CARS_ActionContext.gDefLastAccessed, Calendar.getInstance() );
      }
    }

    // **** Check for Nodes which are removed
    NodeIterator ni = pParentNode.getNodes();
    Node n;
    boolean remove;
    while( ni.hasNext() ) {
      n = ni.nextNode();
      remove = false;
      if (n.hasProperty( CARS_ActionContext.gDefLastAccessed )) {
        if (n.getProperty( CARS_ActionContext.gDefLastAccessed ).getDate().before( synchTime )) {
          remove = true;
        }
      } else {
        remove = true;
      }
      if (remove==true) {
        synchronizeNode_RemoveNode( pParentNode, n );
//        if (n.isNodeType( "jecars:datafile" )) {
//          directoryConfigurationEvent( pMain.getLoginUser(), n, "update", "EXTERNAL REMOVED FILE: " + n.getPath() );
//        } else {
//          directoryConfigurationEvent( pMain.getLoginUser(), n, "update", "EXTERNAL REMOVED DIRECTORY: " + n.getPath() );
//        }
      }
    }
    return;
  }

  
  /** Create an event object
   * 
   * @param pMain
   * @param pWhat
   * @param pFamily e.g. "Applications/.../..."
   * @param pJeCARSFamily
   * @param pShortMessage
   * @param pBodyMessage
   * @return event object
   * @throws java.lang.Exception
   */
  protected Node confAppEvent( CARS_Main pMain, Node pWhat, String pFamily, String pJeCARSFamily,
                               String pShortMessage, String pBodyMessage ) throws Exception {
    Node event = null;
    try {
      if (pMain!=null) {
        pMain.setId( pWhat );
        event = CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pWhat,
                pFamily, "APP", pJeCARSFamily, pShortMessage, CARS_Definitions.DEFAULTNS + "Event", pBodyMessage );
        pMain.setId( event );
      } else {
        event = CARS_Factory.getEventManager().addEvent( null, null, pWhat,
                pFamily, "APP", pJeCARSFamily, pShortMessage, CARS_Definitions.DEFAULTNS + "Event", pBodyMessage );
      }
//      event.setProperty( "jecars:Body", pBodyMessage );
//      event.save();
    } catch (Exception e) {
      gLog.log( Level.WARNING, null, e );
    }
    return event;
  }

  
  /** Is called when this object (superobject) registered in the ObservationManager
   * @param pEvents the event iterator
   */
  @Override
  public void onEvent( EventIterator pEvents ) {
    return;
  }

  
}
