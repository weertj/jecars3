/*
 * Copyright 2007-2008 NLR - National Aerospace Laboratory
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.support.CARS_Mime;

/**
 * CARS_DirectoryApp
 *
 * @version $Id: CARS_DirectoryApp.java,v 1.7 2009/01/16 13:56:27 weertj Exp $
 */
public class CARS_DirectoryApp extends CARS_DefaultInterface implements CARS_Interface {
  
  static public boolean gFORCEREADONLY = true;
  static public boolean gWRITEEVENTS   = true;
  
  private transient boolean mReadOnly = true;
    
  /** Creates a new instance of CARS_DirectoryApp */
  public CARS_DirectoryApp() {
  }

  /** isReadOnly
   * 
   * @return 
   */
  private boolean isReadOnly() {
    if (!gFORCEREADONLY) {
      return mReadOnly;
    }
    return gFORCEREADONLY;
  }
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": JeCARS version=" + CARS_Definitions.VERSION_ID + " $Id: CARS_DirectoryApp.java,v 1.7 2009/01/16 13:56:27 weertj Exp $";
  }

  
  private File getStartDirectory( Node pInterfaceNode ) throws Exception {    
    File f = new File( pInterfaceNode.getProperty( "jecars:StorageDirectory" ).getString(), pInterfaceNode.getName() );
    return f;
  }
  
  /** synchronizeDirectory
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pRelative
   * @throws Exception 
   */
  public void synchronizeDirectory( CARS_Main pMain, Node pInterfaceNode, Node pParentNode, String pRelative ) throws Exception {
//   System.out.println( "a=-=--= " + pInterfaceNode.getProperty( "jecars:StorageDirectory" ).getString() + " ::: " + pRelative );    
    final String pathStart = pInterfaceNode.getProperty( "jecars:StorageDirectory" ).getString();
    final File f;
    if (pathStart.startsWith( "(ABS)" )) {
      final int ix = pRelative.indexOf( "/", 1 );
      String rel = "";
      if (ix!=-1) {
        rel = pRelative.substring(ix);
      }
      f = new File( pathStart.substring( "(ABS)".length() ), rel );
    } else {
      f = new File( pathStart, pRelative );
    }
    if (f.exists()) {
//      Calendar synchTime = Calendar.getInstance();
//      Thread.sleep(10);
      final File files[] = f.listFiles();
      final Calendar synchTime = Calendar.getInstance();
      if (files!=null) {
        for( int i=0; i<files.length; i++ ) {
          if (files[i].isDirectory()) {
            // **** Is Directory
            if (!pParentNode.hasNode( files[i].getName() )) {
              final Node n = pParentNode.addNode( files[i].getName(), "jecars:datafolder" );
              final Calendar mod = Calendar.getInstance();
              mod.setTimeInMillis( files[i].lastModified() );
              final Calendar c = Calendar.getInstance();
              if (mod.before(c)) c.setTime( mod.getTime() );
//              n.setProperty( "jecars:Created",  c );
              n.addMixin( "jecars:mixin_unstructured" );
              n.setProperty( "jecars:Modified", mod );
              n.setProperty( "jecars:DirectoryURL", files[i].toURI().toURL().toExternalForm() );
              directoryConfigurationEvent( pMain.getLoginUser(), n, "update", "EXTERNAL ADDED DIRECTORY: " + n.getPath() );
            }
          } else {
            // **** Is File
            if (!pParentNode.hasNode( files[i].getName() )) {
              final Node n = pParentNode.addNode( files[i].getName(), "jecars:datafile" );
              final Calendar mod = Calendar.getInstance();
              mod.setTimeInMillis( files[i].lastModified() );
              final Calendar c = Calendar.getInstance();
              if (mod.before(c)) c.setTime( mod.getTime() );
//              n.setProperty( "jecars:Created",  c );
              n.setProperty( "jecars:Modified", mod );
              n.setProperty( "jcr:lastModified", mod );
              n.setProperty( "jcr:data", "" );
              n.setProperty( "jcr:mimeType", CARS_Mime.getMIMEType( files[i].getName(), null ) );
              n.setProperty( "jecars:URL", files[i].toURI().toURL().toExternalForm() );
              n.setProperty( "jecars:ContentLength", files[i].length() );
              directoryConfigurationEvent( pMain.getLoginUser(), n, "update", "EXTERNAL ADDED FILE: " + n.getPath() );
            } else {
              // **** Check modification
              final Node n = pParentNode.getNode( files[i].getName() );
              final long tm1 = files[i].lastModified();
              final long tm2 = n.getProperty( "jecars:Modified" ).getDate().getTimeInMillis();
              if (tm1!=tm2) {
                final Calendar mod = Calendar.getInstance();
                mod.setTimeInMillis( files[i].lastModified() );
                n.setProperty( "jecars:Modified", mod );
                n.setProperty( "jcr:lastModified", mod );
                n.setProperty( "jecars:ContentLength", files[i].length() );
              }
              
            }
          }
          Node n = pParentNode.getNode( files[i].getName() );
          n.setProperty( "jecars:LastAccessed", Calendar.getInstance() );
//          directoryConfigurationEvent( pMain.getLoginUser(), n, "browse", pRelative );
        }
        // **** Check for files/directories which are removed
        NodeIterator ni = pParentNode.getNodes();
        Node n;
        boolean remove;
        while( ni.hasNext() ) {
          n = ni.nextNode();
          remove = false;
          if (n.isNodeType( "jecars:dataresource" )) {
            if (n.hasProperty( "jecars:LastAccessed" )) {
              if (n.getProperty( "jecars:LastAccessed" ).getDate().before( synchTime )) {
                remove = true;
              }
            } else {
              remove = true;
            }
          }
          if (remove==true) {
            if (n.isNodeType( "jecars:datafile" )) {
              directoryConfigurationEvent( pMain.getLoginUser(), n, "update", "EXTERNAL REMOVED FILE: " + n.getPath() );
            } else {
              directoryConfigurationEvent( pMain.getLoginUser(), n, "update", "EXTERNAL REMOVED DIRECTORY: " + n.getPath() );
            }
            n.remove();
          }
        }
      }
    } else {
      // **** no file/directory
      throw new PathNotFoundException( f.getAbsolutePath() + " NOT FOUND" );
    }
    return;
  }
  
  /**
   */
  protected void directoryConfigurationEvent( Node pWho, Node pWhat, String pAction, String pMessage ) throws Exception {
    if (gWRITEEVENTS) {
        if (pAction.equals( "browse" )) {
          Node event = CARS_Factory.getEventManager().addEvent( pWho, pWhat,
                        "Applications/Directory", "DIR", "READ", "BROWSE: " + pMessage );
        }
        if (pAction.equals( "update" )) {
          Node event = CARS_Factory.getEventManager().addEvent( pWho, pWhat,
                        "Applications/Directory", "DIR", "UPDATE", "UPDATE: " + pMessage );
        }
        if (pAction.equals( "retrieve" )) {
          Node event = CARS_Factory.getEventManager().addEvent( pWho, pWhat,
                        "Applications/Directory", "DIR", "READ", "RETRIEVE: " + pMessage );
    //      CARS_Factory.getEventManager().addEventReference( event, "Applications/HeliumLoadMonitoring/all" );
        }
        if (pAction.equals( "create" )) {
          Node event = CARS_Factory.getEventManager().addEvent( pWho, pWhat,
                        "Applications/Directory", "DIR", "CREATE", "CREATE: " + pMessage );
        }
        if (pAction.equals( "delete" )) {
          Node event = CARS_Factory.getEventManager().addEvent( pWho, pWhat,
                        "Applications/Directory", "DIR", "DELETE", "DELETE: " + pMessage );
        }
    }
    return;
  }


  // **** Interface
       
  /** removeNode
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pNode
   * @param pParams
   * @throws Exception 
   */
  @Override
  public void removeNode( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final JD_Taglist pParams ) throws Exception {
    final Node parent = pNode.getParent();
    
    if (pNode.hasProperty( "jecars:URL" )) {
      final String furl = pNode.getProperty( "jecars:URL" ).getString();
      final File f = new File( new URL(furl).toURI() );
      final boolean deleted;
      if (isReadOnly()) {
        deleted = false;
      } else {
//        deleted = f.delete();
      }
//      directoryConfigurationEvent( pMain.getLoginUser(), pNode, "delete", " Local URL: " + furl );
//      if (!deleted) {
//        CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pNode,
//                    "Applications/Directory", "DIR", "WARNING", "Cannot delete " + f.getCanonicalPath() );
//      }
    }    
    directoryConfigurationEvent( pMain.getLoginUser(), pNode, "delete", pNode.getName() );
    pNode.remove();
    parent.save();
    return;
  }
  
  /** Store a binary stream, on default the jecars:datafile node type is supported.
   *  If the pNode is an other type the method will stored the data in a Binary property
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pNode the node in which the data will be stored
   * @param pMimeType the mime type of the data if known, otherwise NULL
   * @return true when a update on the node is performed
   * @throws Exception when an error occurs.
   */
  @Override
  public boolean setBodyStream( CARS_Main pMain, Node pInterfaceNode, Node pNode, InputStream pBody, String pMimeType ) throws Exception {
    if (pBody==null) {
      return false;
    } else {
      nodeAdded( pMain, pInterfaceNode, pNode, pBody );
    }
    return true;
  }
    
  /** nodeAdded
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pNewNode
   * @param pBody
   * @throws Exception 
   */
  @Override
  public void nodeAdded( final CARS_Main pMain, final Node pInterfaceNode, final Node pNewNode, final InputStream pBody )  throws Exception {
    if (pNewNode.isNodeType( "jecars:datafile" )) {
      File f = getStartDirectory( pInterfaceNode );
      String xtra = pNewNode.getPath().substring( pInterfaceNode.getPath().length() );
      f = new File( f, xtra );
      if (f.exists()) {
        if (!isReadOnly()) {
//          if (!f.delete()) {
//            throw new IOException( "Cannot delete file: " + f.getCanonicalPath() );
//          }
        }
      }
      if (!f.createNewFile()) {
        throw new IOException( "Cannot create new file: " + f.getCanonicalPath() );
      }
      if (pBody!=null) {
        final FileOutputStream fos = new FileOutputStream( f );
        try {
          directoryConfigurationEvent( pMain.getLoginUser(), pNewNode, "create", pNewNode.getPath() + " (Started)" );
          CARS_Utils.sendInputStreamToOutputStream( 50000, pBody, fos );
          pNewNode.setProperty( "jecars:URL", f.toURI().toURL().toExternalForm() );
          directoryConfigurationEvent( pMain.getLoginUser(), pNewNode, "create", pNewNode.getPath() + " (Ended)" );
        } finally {
          fos.flush();
         fos.close();
        }
      }
    }
    super.nodeAdded( pMain, pInterfaceNode, pNewNode, pBody );
    return;
  }
  
  /** getNodes
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws Exception 
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws Exception {
//    System.out.println( "Must put the nodes under: " + pParentNode.getPath() );
//    System.out.println( "The leaf is (fullpath): " + pLeaf );
    
//    if (pParentNode.isNodeType( "jecars:CARS_Interface" )) {
//      // **** Hey!.... it the root....
//    } else {
//    }
    final Session appSession = CARS_Factory.getSystemApplicationSession();
    // **** sys* nodes have all rights.
//    Node sysParentNode = appSession.getRootNode().getNode( pParentNode.getPath().substring(1) );
    synchronized( appSession ) {
      Node sysParentNode = appSession.getNode( pParentNode.getPath() );
      synchronizeDirectory( pMain, pInterfaceNode, sysParentNode, pLeaf );
      sysParentNode.save();
    }
    return;
  }

  
}
