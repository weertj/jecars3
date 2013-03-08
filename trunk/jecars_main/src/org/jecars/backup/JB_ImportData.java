/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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
package org.jecars.backup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
//import org.apache.jackrabbit.core.nodetype.InvalidNodeTypeDefException;
//import org.apache.jackrabbit.core.nodetype.NodeTypeDef;
//import org.apache.jackrabbit.core.nodetype.NodeTypeManagerImpl;
//import org.apache.jackrabbit.core.nodetype.NodeTypeRegistry;
//import org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader;
//import org.apache.jackrabbit.core.nodetype.compact.ParseException;
import org.jecars.CARS_Utils;

/**
 * JB_ImportData
 * 
 * @version $Id: JB_ImportData.java,v 1.8 2008/12/03 19:12:33 weertj Exp $
 */
public class JB_ImportData {

  private boolean mPass = false;

  private Throwable mLastThrow = null;

//// v2.0
//  public void importFromDirectory( Node pStartNode, JB_Options pOptions ) throws FileNotFoundException, RepositoryException, IOException, org.apache.jackrabbit.commons.cnd.ParseException {
////  public void importFromDirectory( Node pStartNode, JB_Options pOptions ) throws FileNotFoundException, RepositoryException, IOException, org.apache.jackrabbit.core.nodetype.compact.ParseException {
//
//    if (pOptions.getImportNamespaces()==true) {
//      // **** Import namespaces
//      File mainImportFile = new File( pOptions.getImportDirectory(), pOptions.getExportNamespacesFilename() );
//      System.out.println( "Import namespaces using JB file: " + mainImportFile.getAbsolutePath() );
//      FileInputStream fis = new FileInputStream( mainImportFile );
//      BufferedReader br = new BufferedReader(new InputStreamReader( fis ));
//      try {
//        String line;
//        while( (line=br.readLine())!=null ) {
//          String[] nms = line.split( "\t" );
//          try {
//            System.out.println( "Register namespaces: " + nms[0] + " : " + nms[1] );
//            pStartNode.getSession().getWorkspace().getNamespaceRegistry().registerNamespace( nms[0], nms[1] );
//          } catch( Exception e) {             
//          }
//        }
//      } finally {
//        br.close();
//        fis.close();
//      }
//    }
//
//    if (pOptions.getImportNodeTypes()==true) {
//      // **** Import nodetypes
//      File mainImportFile = new File( pOptions.getImportDirectory(), pOptions.getExportNodeTypesFilename() );
//      System.out.println( "Import nodetypes using CND JB file: " + mainImportFile.getAbsolutePath() );
//      
//      FileInputStream fis = new FileInputStream( mainImportFile );
//      InputStreamReader isr = new InputStreamReader( fis );
//      try {
//// v2.0
//        org.apache.jackrabbit.commons.cnd.CndImporter.registerNodeTypes( isr, null );
////        org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader cndReader = new org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader( isr, mainImportFile.getName() );
////        List ntdList = cndReader.getNodeTypeDefs();
////        NodeTypeManagerImpl ntmgr =(NodeTypeManagerImpl)pStartNode.getSession().getWorkspace().getNodeTypeManager();
////        NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();
////        for (Iterator it = ntdList.iterator(); it.hasNext();) {
////          org.apache.jackrabbit.core.nodetype.NodeTypeDef ntd = (org.apache.jackrabbit.core.nodetype.NodeTypeDef)it.next();
////          try {
////            ntreg.registerNodeType(ntd);
////          } catch (InvalidNodeTypeDefException de) {
////            System.out.println( de.getMessage() );
////          } catch (RepositoryException re) {
////            System.out.println( re.getMessage() );
////          }
////        }
//      } finally {
//        isr.close();
//        fis.close();
//      }
//
//      
//    }
//    
//    boolean rerun = true;
//    int pass = 0;
//    while( rerun==true && (pass<2) ) {
//      File mainImportFile = new File( pOptions.getImportDirectory(), pOptions.getExportJeCARSFilename() );
//      System.out.println( "Using JB file: " + mainImportFile.getAbsolutePath() );
//      FileInputStream fis = new FileInputStream( mainImportFile );
//      try {
//        System.out.println( "PASS " + (++pass) );
//        rerun = importFromStream( pStartNode, fis, pOptions );
//      } finally {
//        fis.close();
//      }
//    }
//    return;
//  }  
  
  public boolean importFromStream( Node pStartNode, InputStream pStream, JB_Options pOptions ) throws RepositoryException, IOException {
    
    BufferedReader br = new BufferedReader(new InputStreamReader( pStream ));
    try {
//      int pass = 0;
//        mPass = true;
//      while( mPass==true ) {
        mLastThrow = null;
        mPass = false;
        importNode( pStartNode, br, pOptions );
        try {
          pStartNode.save();
        } catch( Exception e) {            
        }
//        System.out.println( "PASS " + (++pass) );
//      }
      if (mLastThrow!=null) {
        mLastThrow.printStackTrace();
      }
    } finally {
      br.close();
    }
 
    return mPass; 
  }
  
  protected String decodeString( String pValue ) throws UnsupportedEncodingException {    
    return URLDecoder.decode( pValue, "UTF-8" );
  }

  /** decodeValue
   *
   * @param pNode
   * @param pName
   * @param pType
   * @param pValue
   * @param pOptions
   * @throws javax.jcr.UnsupportedRepositoryOperationException
   * @throws javax.jcr.RepositoryException
   * @throws java.io.FileNotFoundException
   * @throws java.io.IOException
   * @throws java.lang.Exception
   */
  protected void decodeValue( Node pNode, String pName, String pType, String pValue, JB_Options pOptions ) throws UnsupportedRepositoryOperationException, RepositoryException, FileNotFoundException, IOException, Exception {
    int type = PropertyType.valueFromName( pType );
//    ValueFactory vf = pNode.getSession().getValueFactory();

    PropertyDefinition pd = CARS_Utils.getPropertyDefinition( pNode, pName );
//    if ((pd!=null) && (pd.isAutoCreated())) return; // **** TODO....
    switch( type ) {
      case PropertyType.BINARY: {
        File f = new File( pOptions.getImportDirectory(), pValue );
        System.out.println( "Import BINARY file -> " + CARS_Utils.getAbsolutePath(f) );
        if (pNode.hasProperty( pName )==false) {
          FileInputStream fis = new FileInputStream( f );
          try {
            pNode.setProperty( pName, fis );
          } finally {
            fis.close();
          }
        }
        break;   
      }
      case PropertyType.BOOLEAN: {
        try {
          pNode.setProperty( pName, Boolean.parseBoolean( pValue ) );
        } catch (NamespaceException ne) {
        } catch (ConstraintViolationException ce) {            
        }
        break;
      }
      case PropertyType.DATE: {
        try {
          if (pNode.hasProperty( pName )==false) {
            pNode.setProperty( pName, pValue );
          } else {
            if (pNode.getProperty( pName ).getString().equals(pValue)==false) {
              pNode.setProperty( pName, pValue );              
            }
          }
        } catch (NamespaceException ne) {
        } catch (ConstraintViolationException ce) {            
        }
        break;
      }
      case PropertyType.DOUBLE: {
        Property prd = null;
        try {
          prd = pNode.setProperty( pName, Double.parseDouble( pValue ));
        } catch (ValueFormatException vfe) {
        } catch (NamespaceException ne) {
        } catch (ConstraintViolationException ce) {
//          ce.printStackTrace();
        }
        if (prd==null) {
          try {
            if ((pd!=null) && (pd.isMultiple()==true)) {
              CARS_Utils.addMultiProperty( pNode, pName, pValue, true );
            }
          } catch (NamespaceException ne) {
          } catch (ConstraintViolationException ce) {
//         ce.printStackTrace();
          }          
        }
        break;
      }
      case PropertyType.LONG: {
        Property prd = null;
        try {
          prd = pNode.setProperty( pName, Long.parseLong( pValue ));
        } catch (ValueFormatException vfe) {
        } catch (NamespaceException ne) {
        } catch (ConstraintViolationException ce) {            
        }
        if (prd==null) {
          try {
            if ((pd!=null) && (pd.isMultiple()==true)) {
              CARS_Utils.addMultiProperty( pNode, pName, pValue, true );
            }
          } catch (NamespaceException ne) {
          } catch (ConstraintViolationException ce) {
          }          
        }
        break;
      }
      case PropertyType.NAME: {
        try {
          pNode.setProperty( pName, pValue );
        } catch (NamespaceException ne) {
        } catch (ConstraintViolationException ce) {            
        }
        break;
      }
      case PropertyType.PATH: {
        try {
          pNode.setProperty( pName, pValue );
        } catch (NamespaceException ne) {
        } catch (ConstraintViolationException ce) {            
        }
        break;
      }
      
      // *************************
      // **** Set reference values
      case PropertyType.REFERENCE: {
        Node ref = null;
        try {
          ref = pNode.getSession().getRootNode().getNode( pValue.substring(1) );
        } catch( PathNotFoundException pe ) {
        }
        if (ref==null) break;
        try {
          if ((pd!=null) && (pd.isMultiple()==true)) {
            CARS_Utils.addMultiProperty( pNode, pName, ref.getPath(), true );
          } else {
            pNode.setProperty( pName, ref );
          }
        } catch (NamespaceException ne) {
        } catch (ConstraintViolationException ce) {
        }
        break;
      }
      case PropertyType.STRING: {
        try {
          if ((pd!=null) && (pd.isMultiple()==true)) {
            CARS_Utils.addMultiProperty( pNode, pName, pValue, true );
          } else {
            if (pNode.hasProperty( pName )==false) {
              pNode.setProperty( pName, pValue );
            } else {
              if (pNode.getProperty( pName ).getString().equals(pValue)==false) {
                pNode.setProperty( pName, pValue );
              }
            }
          }
        } catch (NamespaceException ne) {
        } catch (ConstraintViolationException ce) {
        }
        break;        
      }
      
    }
    return;
  }


  /** importNode
   *
   * @param pStartNode
   * @param pStream
   * @param pOptions
   * @throws java.io.IOException
   * @throws javax.jcr.ItemNotFoundException
   * @throws javax.jcr.AccessDeniedException
   * @throws javax.jcr.RepositoryException
   */
  public void importNode( Node pStartNode, BufferedReader pStream, JB_Options pOptions ) throws IOException, ItemNotFoundException, AccessDeniedException, RepositoryException {
   
   String line = pStream.readLine();

   boolean noLineRead;
   boolean isVersionable;
   ArrayList<String> vlabels = new ArrayList<String>();
   String versionName = null;
   Node newNode = pStartNode;
   boolean skip = false;
   while( line!=null ) {
       skip          = false;
       noLineRead    = false;
       isVersionable = false;
       vlabels.clear();
     
       if (line.startsWith( "[" )) {
//         System.out.println( "[ -> " + newNode.getPath() );
         pStartNode = newNode;
       }

       if (line.startsWith( "]" )) {
//         System.out.println( "] <- " + pStartNode.getParent().getPath() );
         try {
           pStartNode = pStartNode.getParent();
           newNode = pStartNode;
         } catch ( ItemNotFoundException infe ) {
           return;
         }
       }
       
       // **** Is versionable node?
       if (line.startsWith( "#VERSION#" )) {
         line = line.substring( "#VERSION#".length() );
         String[] vks = line.split( "\t" );
         versionName = vks[1];
         line = pStream.readLine();         // next line
         while( line.startsWith( "#VERSIONLABEL#" )) {
           String[] vls = line.split( "\t" );
           vlabels.add( vls[1] );
           line = pStream.readLine(); // next line           
         }
         isVersionable = true;         
       }
       if (line.startsWith( "+" )) {
         try {
           // **** Add node
           String[] tks = line.split( "\t" );

           String nodeName = decodeString(tks[0].substring( 1 ));
//           System.out.println( "Add NODE: " + pStartNode.getPath() + " + " + nodeName );
           if (nodeName.endsWith( "]" )) {
             if (pStartNode.hasNode( nodeName )==false) {
               newNode = pStartNode.addNode( nodeName.substring( 0, nodeName.indexOf('[') ), tks[1] );
             }
           } else if (pStartNode.hasNode( nodeName )==false) {
             newNode = pStartNode.addNode( nodeName, tks[1] );
           } else {
             newNode = pStartNode.getNode( nodeName );
             if (isVersionable==true) {
               try {
                 newNode.getVersionHistory().getVersion( versionName );
                 // **** Version is already imported.... skip this node
                 skip = true;
               } catch( Exception e) {
                 // **** Version is not available, checkout
                 newNode.checkout();
               }
             } else {
               // **** Version label isn't there, check if it is a mix:versionable
               if (newNode.isNodeType( "mix:versionable" )==true) {
                 newNode.checkout();
               }
             }
           }
           if (skip==false) {
             // **** Set the properties
             line = pStream.readLine();
             while( (line!=null) && (line.startsWith( "-" )) ) {
               tks = line.split( "\t" );
               String prop = tks[0].substring(1);
               if (checkSpecials( prop, newNode, tks )==false) {
//                 System.out.println( "Set PROPERTY: " + newNode.getPath() + " - " + prop + " l " + line );
                 try {
                   if (tks.length<3) {
                     decodeValue( newNode, prop, tks[1], "", pOptions );                     
                   } else {
                     decodeValue( newNode, prop, tks[1], decodeString(tks[2]), pOptions );
                   }
                   mPass = true;
                 } catch (Exception re) {
//                re.printStackTrace();
                   mLastThrow = re;
                 }
               } else {
                 mPass = true;
               }
               line = pStream.readLine();
             }
             pStartNode.save();
             mPass = true;
             noLineRead = true;

             if (isVersionable==true) {
               // **** Is a versionable node
               if (newNode.isNodeType( "mix:versionable" )==false) {
                 newNode.addMixin( "mix:versionable" );
               }
               newNode.save();
//             System.out.println( "CHECKIN = " + newNode.getPath() );
               Version v = newNode.checkin();
               if (vlabels.isEmpty()==false) {
                 try {
                   newNode.getVersionHistory().getVersionByLabel( vlabels.get(0) );
                 } catch (VersionException ve) {
                   newNode.getVersionHistory().addVersionLabel( v.getName(), vlabels.get(0), false );
                 }
               }
             }
           }
           
         } catch (RepositoryException re) {
//          System.out.println( "--- " + re.getMessage() );
//         re.printStackTrace();
           mLastThrow = re;
         }
       }
       if (noLineRead==false) line = pStream.readLine();
   }
   return;
 }
  
 protected boolean checkSpecials( String prop, Node newNode, String[] tks ) {
   try {
     if (prop.equals( "jcr:mixinTypes" )==true) {
       newNode.addMixin( decodeString( tks[2] ) );
       return true;
     }
   } catch (Exception e) {       
   }
   return false;
 }

}
