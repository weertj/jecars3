/*
 * Copyright 2007-2013 NLR - National Aerospace Laboratory
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.jcr.*;
import javax.jcr.version.OnParentVersionAction;
import javax.jcr.version.Version;
import org.jecars.CARS_Utils;

/**
 * JB_ExportData
 * 
 * @version $Id: JB_ExportData.java,v 1.8 2009/02/03 10:49:55 weertj Exp $
 */
public class JB_ExportData {

  private ArrayList<Version> mExportedVersions = new ArrayList<Version>();
    
  static final String EOL = "\n";
  
  /** exportToDirectory
   * 
   * @param pStartNode
   * @param pOptions
   * @throws FileNotFoundException
   * @throws RepositoryException
   * @throws IOException
   * @throws Exception 
   */
  public void exportToDirectory( Node pStartNode, JB_Options pOptions ) throws FileNotFoundException, RepositoryException, IOException, Exception {    
    
    mExportedVersions.clear();

    if (!pOptions.getExportDirectory().exists()) {
      if (!pOptions.getExportDirectory().mkdirs()) {
        throw new Exception( "Cannot create directory: " + pOptions.getExportDirectory().getCanonicalPath() );
      }
    }
    
    if (pOptions.getExportNamespaces()) {
      // **** Export the namespaces
      File namespaceExportFile = new File( pOptions.getExportDirectory(), pOptions.getExportNamespacesFilename() );
      FileOutputStream fos = new FileOutputStream( namespaceExportFile );
      DataOutputStream dos = new DataOutputStream( fos );
      try {
        System.out.println( "Export namespaces using JB file: " + CARS_Utils.getAbsolutePath(namespaceExportFile) );    
        String[] nms = pStartNode.getSession().getNamespacePrefixes();
        for( int i=0; i<nms.length; i++ ) {
          dos.writeBytes( nms[i] );
          dos.writeBytes( "\t" );
          dos.writeBytes( pStartNode.getSession().getNamespaceURI( nms[i] ) );
          dos.writeBytes( EOL );
        }
      } finally {
        dos.close();
        fos.close();
      }
    }
    
    if (pOptions.getExportNodeTypes()) {
      // **** Export the nodetypes
      File f = new File( pOptions.getExportDirectory(), pOptions.getExportNodeTypesFilename() );
      FileOutputStream fos = new FileOutputStream( f );
      try {
        JB_ExportNodeTypes nt = new JB_ExportNodeTypes();
        String cnd = nt.createCND( pStartNode.getSession(), false );
        fos.write( cnd.getBytes() );
      } finally {
        fos.flush();
        fos.close();
      }
    }

    
    File mainExportFile = new File( pOptions.getExportDirectory(), pOptions.getExportJeCARSFilename() );
    System.out.println( "Using JB file: " + CARS_Utils.getAbsolutePath(mainExportFile) );    
    FileOutputStream fos = new FileOutputStream( mainExportFile );
    try {
      exportToStream( pStartNode, fos, pOptions );
    } finally {
      fos.flush();
      fos.close();
    }
    return;
  }
  
  /** exportToStream
   * 
   * @param pStartNode
   * @param pStream
   * @param pOptions
   * @throws RepositoryException
   * @throws IOException 
   */
  public void exportToStream( final Node pStartNode, final OutputStream pStream, final JB_Options pOptions ) throws RepositoryException, IOException {
    
    final DataOutputStream dos = new DataOutputStream( pStream );    
    
    if (pStartNode.getPath().equals("/")) {
      // **** Write the children
      final NodeIterator ni = pStartNode.getNodes();
      if (ni.getSize()>0) {
        while( ni.hasNext() ) {
          final Node n = ni.nextNode();        
          exportNode( 1, n, dos, pOptions );
        }
      }
    } else {
      exportNode( 1, pStartNode, dos, pOptions );
    }      
    return; 
  }

  /** exportToStreamAsJeCARS
   * 
   * @param pStartNode
   * @param pStream
   * @param pOptions
   * @throws RepositoryException
   * @throws IOException 
   */
  public void exportToStreamAsJeCARS( final Node pStartNode, final OutputStream pStream, final JB_Options pOptions ) throws RepositoryException, IOException {    
    final DataOutputStream dos = new DataOutputStream( pStream );    
    exportNode( 1, pStartNode, dos, pOptions );
    return; 
  }

  
  /** encodeString
   * 
   * @param pValue
   * @return
   * @throws UnsupportedEncodingException 
   */
  protected String encodeString( String pValue ) throws UnsupportedEncodingException {    
    return URLEncoder.encode( pValue, "UTF-8" );
  }
  
  /** encodeValue
   * 
   * @param pValue
   * @return
   * @throws ValueFormatException
   * @throws IllegalStateException
   * @throws RepositoryException
   * @throws UnsupportedEncodingException 
   */
  protected String encodeValue( Value pValue ) throws ValueFormatException, IllegalStateException, RepositoryException, UnsupportedEncodingException {
    String s = null;
    switch( pValue.getType() ) {
      default:
      {
        s = encodeString( pValue.getString() );
        break;
      }
    }
    return s;
  }
   
  /** isSysProperty
   * 
   * @param pPropName
   * @return 
   */
  protected boolean isSysProperty( String pPropName ) {
    if (pPropName.equals( "jcr:mixinTypes"  )) return true;
    if (pPropName.equals( "jcr:primaryType" )) return true;    
    return false;
  }
  
  /** exportSysProperties
   * 
   * @param pNode
   * @param pStream
   * @throws RepositoryException
   * @throws IOException 
   */
  protected void exportSysProperties( Node pNode, DataOutputStream pStream ) throws RepositoryException, IOException {
    if (pNode.hasProperty( "jcr:mixinTypes" )) {
      Value[] v = pNode.getProperty( "jcr:mixinTypes" ).getValues();
      for( int i=0; i<v.length; i++ ) {
        pStream.writeBytes( "-jcr:mixinTypes\tName\t" );
        pStream.writeBytes( encodeValue( v[i] ) + EOL );
      }
    }
    return;
  }

  /** Export version
   * 
   * @param pBaseNode
   * @param pLevel
   * @param pVersion
   * @param pStream
   * @param pOptions
   * @throws RepositoryException
   * @throws IOException 
   */
  protected void exportVersionNode( Node pBaseNode, int pLevel, Version pVersion, DataOutputStream pStream, JB_Options pOptions ) throws RepositoryException, IOException {

//    System.out.println( "VERSION export : " + version.getName() + " -- " + version.getPath() );
    if (pLevel>0) {
      pStream.writeBytes( "#VERSION#\t" + encodeString(pVersion.getName()) + EOL );
      String vl[] = pBaseNode.getVersionHistory().getVersionLabels( pVersion );
      for (String vlabel : vl) {
        pStream.writeBytes( "#VERSIONLABEL#\t" + encodeString(vlabel) + EOL );
      }
      Node n = pVersion.getNode( "jcr:frozenNode" );
      // **** Write node
      if (n.getIndex()>1) {
        // **** Indexed node
        pStream.writeBytes( "+" + encodeString(pBaseNode.getName()) + "[" + pBaseNode.getIndex() + "]\t" + pBaseNode.getPrimaryNodeType().getName() + EOL );
      } else {
        pStream.writeBytes( "+" + encodeString(pBaseNode.getName()) + "\t" + pBaseNode.getPrimaryNodeType().getName() + EOL );
      }
      exportProperties( pLevel, n, pStream, pOptions, pBaseNode );
    }
    Version[] vers = pVersion.getSuccessors();
    for( Version version : vers ) {
      exportVersionNode( pBaseNode, pLevel+1, version, pStream, pOptions );
    }
    return;
  }

  /** Export node
   * 
   * @param pLevel
   * @param pNode
   * @param pStream
   * @param pOptions
   * @throws RepositoryException
   * @throws IOException 
   */
  protected void exportNode( int pLevel, Node pNode, DataOutputStream pStream, JB_Options pOptions ) throws RepositoryException, IOException {

    if (pOptions.excludePath( pNode.getPath() )==true) {
//      System.out.println( "Excluding: " + pNode.getPath() );
      return;
    }
        
   // System.out.println( "EXPORT Node: " + pNode.getPath() );   
    
    // **** Is this node versionable?
//    if (pNode.isNodeType( "mix:versionable" )==true) {
//      Version v = pNode.getVersionHistory().getRootVersion();
//      exportVersionNode( pNode, 0, v, pStream, pOptions );
//    } else {       
    {
      // **** Write node
      String primNodeName = pNode.getPrimaryNodeType().getName();
      if (pOptions.excludeNodeType( primNodeName )==true) {
//        System.out.println( "Excluding (NT): " + pNode.getPath() );
        return;
      }
      if (pNode.hasProperty( "jcr:frozenPrimaryType" )==true) {
        primNodeName = pNode.getProperty( "jcr:frozenPrimaryType" ).getString();
      }
      if (pNode.getIndex()>1) {          
        // **** Indexed node        
        pStream.writeBytes( "+" + encodeString(pNode.getName()) + "[" + pNode.getIndex() + "]\t" + primNodeName + EOL );
      } else {
        pStream.writeBytes( "+" + encodeString(pNode.getName()) + "\t" + primNodeName + EOL );
      }
      exportProperties( pLevel, pNode, pStream, pOptions, null );    
      if (pNode.isNodeType( "mix:versionable" )==true) {
        try {
          Version v = pNode.getVersionHistory().getRootVersion();
          if (mExportedVersions.contains(v)==false) {
            mExportedVersions.add(v);
            exportVersionNode( pNode, 0, v, pStream, pOptions );
            if (pNode.getIndex()>1) {          
              // **** Indexed node        
              pStream.writeBytes( "+" + encodeString(pNode.getName()) + "[" + pNode.getIndex() + "]\t" + primNodeName + EOL );
            } else {
              pStream.writeBytes( "+" + encodeString(pNode.getName()) + "\t" + primNodeName + EOL );
            }
            exportProperties( pLevel, pNode, pStream, pOptions, null );
          }
        } catch (Exception eee) {            
        }
      }
    }
    return;
  }

  /** Export properties
   * 
   * @param pLevel
   * @param pNode
   * @param pStream
   * @param pOptions
   * @param pBaseNode
   * @throws RepositoryException
   * @throws IOException 
   */
  protected void exportProperties( int pLevel, Node pNode, DataOutputStream pStream, JB_Options pOptions, Node pBaseNode ) throws RepositoryException, IOException {
    // **** Export sys properties first
    exportSysProperties( pNode, pStream );
    
    // **** Write all properties    
    PropertyIterator pi = pNode.getProperties();
    while( pi.hasNext() ) {
      Property p = pi.nextProperty();
      writeProperty( p, pStream, pOptions );      
    }
    if (pBaseNode!=null) {
      pi = pBaseNode.getProperties();
      while( pi.hasNext() ) {
        // **** Write the OPV IGNORE
        Property p = pi.nextProperty();
        if (p.getDefinition().getOnParentVersion()==OnParentVersionAction.IGNORE) {
          writeProperty( p, pStream, pOptions );
        }
      }      
    }
    
    // **** Write the children
    if (!pOptions.getOnlyOneLevel()) {
      if (pOptions.getOnlyOneLevelChildren()) {
        pOptions.setOnlyOneLevel( true );
      }
      NodeIterator ni = pNode.getNodes();
      if (ni.getSize()>0) {
        pStream.writeBytes( "[\n" );
        while( ni.hasNext() ) {
          Node n = ni.nextNode();
          exportNode( pLevel+1, n, pStream, pOptions );
        }
        pStream.writeBytes( "]\n" );
      }
      if (pOptions.getOnlyOneLevelChildren()) {
        pOptions.setOnlyOneLevel( false );
      }
    }

      
    return;
  }

  /** Write property
   * 
   * @param p
   * @param pStream
   * @param pOptions
   * @throws RepositoryException
   * @throws IOException 
   */
  protected void writeProperty( Property p, DataOutputStream pStream, JB_Options pOptions ) throws RepositoryException, IOException {       
    if (!isSysProperty( p.getName() )) {
      if (!p.getDefinition().isMultiple()) {
        // **** Single value property
        final Value v = p.getValue();
//        System.out.println( "-=a " + p.getPath());
        if (v.getType()==PropertyType.BINARY) {
          // **** Binary data
          if (pOptions.getExportBinary()) {
            pStream.writeBytes( "-" + p.getName() + "\t" + PropertyType.nameFromValue(v.getType()) );
            System.out.println( "Export BINARY property " + p.getPath() );
            pStream.writeBytes( "\t" + exportBinaryProperty( p, pOptions ) + EOL );
          }
        } else if (v.getType()==PropertyType.REFERENCE) {
          if (pOptions.getExportReferences()==true) {
            pStream.writeBytes( "-" + p.getName() + "\t" + PropertyType.nameFromValue(v.getType()) );
            pStream.writeBytes( "\t" + encodeString( p.getNode().getPath() ) + EOL );
          }
        } else {
          pStream.writeBytes( "-" + p.getName() + "\t" + PropertyType.nameFromValue(v.getType()) );
          if ("jecars:PathToFile".equals( p.getName() )) {
            final File changeToFile = pOptions.changeFilePathRoot();
            String s = p.getValue().getString();
            if (changeToFile!=null) {
              int ix = s.lastIndexOf( '/' );
              if (ix!=-1) {
                s = s.substring( s.lastIndexOf( '/' ) );
                s = CARS_Utils.getAbsolutePath(changeToFile) + s;
              }
            }
            pStream.writeBytes( "\t" + encodeString(s) + EOL );
          } else {
            pStream.writeBytes( "\t" + encodeValue( v ) + EOL );
          }
        }
      } else {
        Value[] v = p.getValues();
        for( int i=0; i<v.length; i++ ) {
          if (v[i].getType()==PropertyType.BINARY) {
            // **** Binary data
            if (pOptions.getExportBinary()==true) {
              pStream.writeBytes( "-" + p.getName() + "\t" + PropertyType.nameFromValue(v[i].getType()) );
              System.out.println( "Export BINARY property " + p.getPath() );
              pStream.writeBytes( "\t" + exportBinaryProperty( p, pOptions ) + EOL );
            }
          } else if (v[i].getType()==PropertyType.REFERENCE) {
            if (pOptions.getExportReferences()==true) {
              pStream.writeBytes( "-" + p.getName() + "\t" + PropertyType.nameFromValue(v[i].getType()) );
              pStream.writeBytes( "\t" + encodeString( p.getSession().getNodeByUUID(v[i].getString()).getPath() ) + EOL );
            }
          } else {
            pStream.writeBytes( "-" + p.getName() + "\t" + PropertyType.nameFromValue(v[i].getType()) );
            pStream.writeBytes( "\t" + encodeValue( v[i] ) + EOL );
          }
        }
      }
    }
    return;
  }
  
  /** exportBinaryProperty
   * 
   * @param pProperty
   * @param pOptions
   * @return
   * @throws RepositoryException
   * @throws UnsupportedEncodingException
   * @throws FileNotFoundException
   * @throws IOException 
   */
  protected String exportBinaryProperty( Property pProperty, JB_Options pOptions ) throws RepositoryException, UnsupportedEncodingException, FileNotFoundException, IOException {
    File exportDir = pOptions.getExportDirectory();
    
    File binFile = new File( exportDir, pOptions.getNextBinaryExportFilename() );
    FileOutputStream binFos = new FileOutputStream( binFile );
    try {
      InputStream is = pProperty.getBinary().getStream();
      byte[] buffer = new byte[50000];
      int read;
      while( (read=is.read(buffer))!=-1 ) {
        binFos.write( buffer, 0, read );
      }      
    } finally {
      binFos.close();
    }
    
    return binFile.getName();
  }

  
}
