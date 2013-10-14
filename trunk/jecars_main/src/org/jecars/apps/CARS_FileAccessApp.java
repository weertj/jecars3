/*
 * Copyright 2013 NLR - National Aerospace Laboratory
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.logging.Logger;
import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Security;
import org.jecars.CARS_Utils;
import org.jecars.support.CARS_Mime;

/**
 * CARS_FileAccessApp
 *
 */
public class CARS_FileAccessApp extends CARS_DefaultInterface implements CARS_Interface {

  private static final Logger LOG = Logger.getLogger(CARS_FileAccessApp.class.getName());
  
  static final public boolean ENABLED = true;
  
    
  /** Creates a new instance of CARS_FileAccessApp */
  public CARS_FileAccessApp() {
  }

  /** getStartDirectory
   * 
   * @param pInterfaceNode
   * @param pRelative
   * @return
   * @throws Exception 
   */
  private File getStartDirectory( Node pInterfaceNode, String pRelative ) throws Exception {    
    final String pathStart = pInterfaceNode.getProperty( "jecars:StorageDirectory" ).getString();
    if (CARS_Security.isExportPathAllowed( pathStart )) {
      final File f;
      if (pathStart.startsWith( "(ABS)" )) {
        f = new File( pathStart.substring( "(ABS)".length() ) );
      } else {
        f = null;
      }
      File df = new File( f, pRelative );
      return df;
    } else {
      throw new AccessDeniedException( "No access: " + pathStart );
    }    
  }

  /** setParamProperty
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pNode
   * @param pPropName
   * @param pValue
   * @return
   * @throws Exception 
   */
  @Override
  public Property setParamProperty( final CARS_Main pMain, final Node pInterfaceNode, final Node pNode, final String pPropName, final String pValue ) throws Exception {
    if (!ENABLED) {
      throw new AccessDeniedException( "Disabled" );
    }
    if ("jecars:URL".equals(pPropName)) {
      throw new AccessDeniedException( "Cannot set " + pPropName );
    }
    return super.setParamProperty( pMain, pInterfaceNode, pNode, pPropName, pValue );
  }

  
  /** synchronizeDirectory
   * 
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pRelative
   * @throws Exception 
   */
  public void synchronizeDirectory( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pRelative ) throws Exception {
//   System.out.println( "a=-=--= " + pInterfaceNode.getProperty( "jecars:StorageDirectory" ).getString() + " ::: " + pRelative );
    if (!ENABLED) {
      throw new AccessDeniedException( "Disabled" );
    }
    String relative = pRelative;
    if (relative.startsWith( "/" + pInterfaceNode.getName() )) {
      relative = relative.substring( pInterfaceNode.getName().length()+1 );
    }
    if (!pInterfaceNode.hasProperty( "jecars:StorageDirectory" )) {
      return;
    }
    final File f = getStartDirectory( pInterfaceNode, relative );
    if ((f!=null) && f.exists()) {
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
              if (mod.before(c)) {
                c.setTime( mod.getTime() );
              }
//              n.setProperty( "jecars:Created",  c );
              n.setProperty( "jecars:Modified", mod );
            }
          } else {
            // **** Is File
            if (!pParentNode.hasNode( files[i].getName() )) {
              final Node n = pParentNode.addNode( files[i].getName(), "jecars:datafile" );
              final Calendar mod = Calendar.getInstance();
              mod.setTimeInMillis( files[i].lastModified() );
              final Calendar c = Calendar.getInstance();
              if (mod.before(c)) {
                c.setTime( mod.getTime() );
              }
//              n.setProperty( "jecars:Created",  c );
              n.setProperty( "jecars:Modified", mod );
              n.setProperty( "jcr:lastModified", mod );
              n.setProperty( "jcr:data", "" );
              n.setProperty( "jcr:mimeType", CARS_Mime.getMIMEType( files[i].getName(), null ) );
              n.setProperty( "jecars:URL", files[i].toURI().toURL().toExternalForm() );
              n.setProperty( "jecars:ContentLength", files[i].length() );
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
            n.remove();
          }
        }
      }
    } else {
      // **** no file/directory
      CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pParentNode,
                     "Applications/Directory", "DIR", "WARNING", "Cannot find " + f.getCanonicalPath() );
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
    if (!ENABLED) {
      throw new AccessDeniedException( "Disabled" );
    }
    final Node parent = pNode.getParent();    
    if (pNode.hasProperty( "jecars:URL" )) {
      final String furl = pNode.getProperty( "jecars:URL" ).getString();
      final File f = new File( new URL(furl).toURI() );
      final boolean deleted;
      deleted = f.delete();
      if (!deleted) {
        CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pNode,
                     "Applications/Directory", "DIR", "WARNING", "Cannot delete " + f.getCanonicalPath() );
      }
    }    
    pNode.remove();
    parent.save();
    return;
  }
  
  /** Store a binary stream, on default the jecars:datafile node type is supported.
   *  If the pNode is an other type the method will stored the data in a Binary property
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source or NULL
   * @param pNode the node in which the data will be stored
   * @param pBody 
   * @param pMimeType the mime type of the data if known, otherwise NULL
   * @return true when a update on the node is performed
   * @throws Exception when an error occurs.
   */
  @Override
  public boolean setBodyStream( CARS_Main pMain, Node pInterfaceNode, Node pNode, InputStream pBody, String pMimeType ) throws Exception {
    if (!ENABLED) {
      throw new AccessDeniedException( "Disabled" );
    }
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
    if (!ENABLED) {
      throw new AccessDeniedException( "Disabled" );
    }
    if (pNewNode.isNodeType( "jecars:datafile" )) {
      File f = getStartDirectory( pInterfaceNode, "" );
      String xtra = pNewNode.getPath().substring( pInterfaceNode.getPath().length() );
      f = new File( f, xtra );
      if ((pBody!=null) && f.exists()) {
        if (!f.delete()) {
          throw new IOException( "Cannot delete file: " + f.getCanonicalPath() );
        }
      }
      if (!f.exists()) {
        if (!f.createNewFile()) {
          throw new IOException( "Cannot create new file: " + f.getCanonicalPath() );
        }
      }
      if (pBody!=null) {
        final FileOutputStream fos = new FileOutputStream( f );
        try {
          CARS_Utils.sendInputStreamToOutputStream( 50000, pBody, fos );
          pNewNode.setProperty( "jecars:URL", f.toURI().toURL().toExternalForm() );
        } finally {
          fos.flush();
         fos.close();
        }
      }
    }
    // **** jecars:datafolder
    if (pNewNode.isNodeType( "jecars:datafolder" )) {
      File f = getStartDirectory( pInterfaceNode, "" );
      String xtra = pNewNode.getPath().substring( pInterfaceNode.getPath().length() );
      f = new File( f, xtra );
      if ((pBody!=null) && f.exists()) {
        if (!f.delete()) {
          throw new IOException( "Cannot delete directory: " + f.getCanonicalPath() );
        }
      }
      if (!f.exists()) {
        if (!f.mkdirs()) {
          throw new IOException( "Cannot create new directory: " + f.getCanonicalPath() );
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
    if (!ENABLED) {
      throw new AccessDeniedException( "Disabled" );
    }
    final Session appSession = CARS_Factory.getSystemApplicationSession();
    // **** sys* nodes have all rights.
    synchronized( appSession ) {
      Node sysParentNode = appSession.getNode( pParentNode.getPath() );
      synchronizeDirectory( pMain, pInterfaceNode, sysParentNode, pLeaf );
      appSession.save();
    }
    return;
  }

  
}
