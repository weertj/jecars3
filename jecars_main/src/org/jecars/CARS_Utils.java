/*
 * Copyright 2007-2011 NLR - National Aerospace Laboratory
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

import com.google.gdata.util.common.base.CharEscapers;
import com.google.gdata.util.common.base.Escaper;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

/**
 * CARS_Utils
 *
 * @version $Id: CARS_Utils.java,v 1.17 2009/07/22 08:59:23 weertj Exp $
 */
public class CARS_Utils {

  static public final Escaper URLENCODER = CharEscapers.uriEscaper(false);
  static public final String[] EMPTY_STRING_ARRAY = {};


  private CARS_Utils() {
  }

  /** getAbsolutePath
   * 
   * @param pF
   * @return 
   */
  static public String getAbsolutePath( final File pF ) {
    String ab = pF.getAbsolutePath();
    ab = ab.replace( '\\', '/' );
    return ab;
  }
  
  /** getNodeByString
   * @param pSession
   * @param pValue
   * @return
   * @throws java.lang.Exception
   */
  static public Node getNodeByString( Session pSession, String pValue ) throws Exception {
    Node n = null;
    try {
      n = pSession.getNodeByUUID( pValue );
    } catch (Exception e) {
      if (pValue.startsWith("/")) {
        n = pSession.getRootNode().getNode( pValue.substring(1) );
      }
    }
    return n;
  }
  
  public static void sendInputToOutputNIOBuffer( final File pSource, final File pTarget ) throws IOException {
    FileChannel in = null;
    FileChannel out = null;
    try {
      in  = new FileInputStream(pSource).getChannel();
      out = new FileOutputStream(pTarget).getChannel();
      ByteBuffer buffer = ByteBuffer.allocateDirect(50000);
      while (in.read(buffer) != -1) {
        buffer.flip();
        while(buffer.hasRemaining()){
          out.write(buffer);
        }
        buffer.clear();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      in.close();
      out.close();
    }
  }
  
  /** sendInputStreamToOutputStream
   * @param pBufferSize
   * @param pInput
   * @param pOutput
   * @throws java.lang.Exception
   */
  static public void sendInputStreamToOutputStream( final int pBufferSize, final InputStream pInput, final OutputStream pOutput ) throws IOException {
    final BufferedInputStream  bis = new BufferedInputStream(  pInput );
    final BufferedOutputStream bos = new BufferedOutputStream( pOutput );
    try {
      final byte[] buff = new byte[pBufferSize];
//      long sended = 0;
      int bytesRead;
      while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
        bos.write(buff, 0, bytesRead);
//        bos.flush();
//        sended += bytesRead;
//              System.out.println( "--- " + sended );
      }
      bos.flush();      
    } finally {
      pInput.close();
      if (bis!=null) {
        bis.close();
      }
      if (bos!=null) {
        bos.flush();      
        bos.close();
      }
    }
    return;
  }

//  /** sendInputStreamToOutputStream
//   * @param pBufferSize
//   * @param pInput
//   * @param pOutput
//   * @throws java.lang.Exception
//   */
//  static public void sendInputStreamToOutputStream( final int pBufferSize, final InputStream pInput, final OutputStream pOutput ) throws IOException {
//    final BufferedInputStream  bis = new BufferedInputStream(  pInput );
//    final BufferedOutputStream bos = new BufferedOutputStream( pOutput );
//    try {
//      final byte[] buff = new byte[pBufferSize];
////      long sended = 0;
//      int bytesRead;
//      while(-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
//        bos.write(buff, 0, bytesRead);
//        bos.flush();
////        sended += bytesRead;
////              System.out.println( "--- " + sended );
//      }
//    } finally {
//      pInput.close();
//      if (bis!=null) bis.close();
//      if (bos!=null) bos.close();
//    }
//    return;
//  }  
  
  
  /** Get the propertydefinition of a node
   */
  static public PropertyDefinition getPropertyDefinition( Node pNode, String pPropName ) throws RepositoryException {
    PropertyDefinition pd = null;
    PropertyDefinition[] pds = pNode.getPrimaryNodeType().getPropertyDefinitions();
// System.out.println( "a--ds- " + pNode.getPrimaryNodeType().getName() );
//    for( int i=0; i<pds.length; i++ ) { System.out.println( (pds[i].getName() )); }
    for( int i=0; i<pds.length; i++ ) {
      if (pds[i].getName().equals( pPropName )) {
        pd = pds[i];
        break;
      }
    }
    if (pd==null) {
      // **** Try the mixin's
      NodeType[] nts = pNode.getMixinNodeTypes();
      for (NodeType nt : nts) {
        pds = nt.getPropertyDefinitions();
        for( int i=0; i<pds.length; i++ ) {
          if (pds[i].getName().equals( pPropName )) {
            pd = pds[i];
            break;
          }
        }
      }

    }
    return pd;
  }

  /** Add a value to a multiple property
   *
   * @param pNode
   * @param pPropName
   * @param pValue
   * @param pIncludeDuplicates included to solve a bug that duplicated values aren't written.
   *                           use 'false' to get the original behaviour.
   * @throws java.lang.Exception
   */
  static public void addMultiProperty( Node pNode, String pPropName, String pValue, boolean pIncludeDuplicates ) throws Exception {
    if (pNode.hasProperty( pPropName )) {
      Property prop = pNode.getProperty( pPropName );
      ArrayList<Value> al = new ArrayList<Value>(Arrays.asList(prop.getValues()));
      Value newVal;
      if (prop.getType()==PropertyType.REFERENCE) {
        // **** Is reference type
        Node refNode = getNodeByString( pNode.getSession(), pValue );
        newVal = prop.getSession().getValueFactory().createValue( refNode );
      } else {
        newVal = prop.getSession().getValueFactory().createValue( pValue, prop.getType() );
      }
      if (!pIncludeDuplicates) {
        if (!al.contains(newVal)) {
          al.add( newVal );
          prop.setValue( al.toArray(new Value[0]) );
          pNode.setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
        }
      } else {
        al.add( newVal );
        prop.setValue( al.toArray(new Value[0]) );
        pNode.setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
      }
    } else {
      // **** New property
      PropertyDefinition pd = getPropertyDefinition( pNode, pPropName );
      if (pd==null) {
        throw new Exception( "No definition for propertytype: " + pPropName );
      }
      Value[] sv = new Value[1];
      if (pd.getRequiredType()==PropertyType.REFERENCE) {
        // **** Is reference type
        Node refNode = getNodeByString( pNode.getSession(), pValue );
        sv[0] = pNode.getSession().getValueFactory().createValue( refNode );
      } else {
        sv[0] = pNode.getSession().getValueFactory().createValue( pValue, pd.getRequiredType() );
      }
      pNode.setProperty( pPropName, sv );
      pNode.setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
    }
    return;
  }

  /** Remove a value from a multiple property
   * 
   * @param pNode
   * @param pPropName
   * @param pValue
   * @throws java.lang.Exception
   */
  static public void removeMultiProperty( final Node pNode, final String pPropName, final String pValue ) throws Exception {
    if (pNode.hasProperty( pPropName )) {
      final Property prop = pNode.getProperty( pPropName );
      final List<Value> al = new ArrayList<Value>(Arrays.asList(prop.getValues()));
      final Value newVal = prop.getSession().getValueFactory().createValue( pValue, prop.getType() );
      if (al.contains(newVal)) {
        al.remove( newVal );
        prop.setValue( al.toArray(new Value[0]) );
        pNode.setProperty( CARS_ActionContext.DEF_MODIFIED, Calendar.getInstance() );
      }
    }
    return;
  }

  /** Add permission object
   * @param pParentNode the node under which the permission node is added
   * @param pGroupname the group name (without the path), may be null
   * @param pUsername the user name (without the path), may be null
   * @param pRights the rights stored in a string e.g. "read,add_node"
   * @return The created permission node
   * @throws Exception when an exception occurs
   */
  static public Node addPermission( final Node pParentNode, final String pGroupname, final String pUsername, final String pRights ) throws RepositoryException {
    Node n = null;
    Node prin = null;
    if (pGroupname!=null) {
      prin = pParentNode.getSession().getRootNode().getNode( CARS_Definitions.gGroupsPath + "/" + pGroupname );
    }
    if (pUsername!=null) {
      prin = pParentNode.getSession().getRootNode().getNode( CARS_Definitions.gUsersPath  + "/" + pUsername  );
    }
    if (prin==null) {
      return null;
    }
    if (pParentNode.hasNode( "P_" + prin.getName())) {
      n = pParentNode.getNode( "P_" + prin.getName() );
    } else {
      n = pParentNode.addNode( "P_" + prin.getName(), CARS_Definitions.DEFAULTNS + "Permission" );
    }
//    Value[] vals = {pParentNode.getSession().getValueFactory().createValue( prin )};
    final Value[] vals = {pParentNode.getSession().getValueFactory().createValue( prin.getPath() )};
    n.setProperty( CARS_Definitions.DEFAULTNS + "Principal", vals );
    if (pRights.indexOf( "delegate" )!=-1) n.setProperty( CARS_Definitions.DEFAULTNS + "Delegate", "true" );
    int l = 0;
    if (pRights.indexOf( "read"     )!=-1) l++;
    if (pRights.indexOf( "add_node" )!=-1) l++;
    if (pRights.indexOf( "set_property" )!=-1) l++;
    if (pRights.indexOf( "get_property" )!=-1) l++;
    if (pRights.indexOf( "remove"   )!=-1) l++;
    if (pRights.indexOf( "acl_read" )!=-1) l++;
    if (pRights.indexOf( "acl_edit" )!=-1) l++;
    String[] rr = new String[l];
    l = 0;
    if (pRights.indexOf( "read"     )!=-1) rr[l++] = "read";
    if (pRights.indexOf( "add_node" )!=-1) rr[l++] = "add_node";
    if (pRights.indexOf( "set_property" )!=-1) rr[l++] = "set_property";
    if (pRights.indexOf( "get_property" )!=-1) rr[l++] = "get_property";
    if (pRights.indexOf( "remove"   )!=-1) rr[l++] = "remove";
    if (pRights.indexOf( "acl_read" )!=-1) rr[l++] = "acl_read";
    if (pRights.indexOf( "acl_edit" )!=-1) rr[l++] = "acl_edit";
    n.setProperty( CARS_Definitions.DEFAULTNS + "Actions", rr );
    return n;
  }

  /** Append a user or group to a permission object
   * @param pParentNode the node under which the permission node is added
   * @param pName the name of the permission object
   * @param pGroupname the group name (without the path), may be null
   * @param pUsername the user name (without the path), may be null
   * @return The changed permission node
   * @throws Exception when an exception occurs
   */
  static public Node appendPermission( Node pParentNode, String pName, String pGroupname, String pUsername ) throws Exception {
    Node permission = null;
    if (pParentNode.hasNode( pName )==true) {
      permission = pParentNode.getNode( pName );
//      Node prin = null;
//      if (pGroupname!=null) prin = pParentNode.getSession().getRootNode().getNode( CARS_AccessManager.gGroupsPath + "/" + pGroupname );
//      if (pUsername!=null)  prin = pParentNode.getSession().getRootNode().getNode( CARS_AccessManager.gUsersPath  + "/" + pUsername  );
      String prin = null;
      if (pGroupname!=null) prin = CARS_Definitions.gGroupsPath + "/" + pGroupname;
      if (pUsername!=null)  prin = CARS_Definitions.gUsersPath  + "/" + pUsername;
//      addMultiProperty( permission, CARS_Main.DEFAULTNS + "Principal", pParentNode.getSession().getRootNode().getNode( prin ).getUUID() );
      addMultiProperty( permission, CARS_Definitions.DEFAULTNS + "Principal", "/" + prin, false );
    }
    return permission;
  }

 /** readAsString2
   * 
   * @param pInput
   * @return
   * @throws IOException 
   */
  static public String readAsString2( final InputStream pInput ) throws IOException {
    final char[] buffer = new char[10000];
    final StringBuilder out = new StringBuilder();
    final Reader in = new InputStreamReader( pInput, "iso-8859-1" );
    try {
      int read;
      do {
        read = in.read(buffer, 0, buffer.length);
        if (read>0) {
          out.append(buffer, 0, read);
        }
      } while (read>=0);
    } finally {
      in.close();
    }
    return out.toString();
  }

  
  /** Read an inputstream as string
   * @param pInput The inputstream
   * @return The resulting string
   * @throws IOException when an error occurs
   */
  static public String readAsString( final InputStream pInput ) throws IOException {
    final InputStreamReader isr = new InputStreamReader(pInput);
    final BufferedReader br = new BufferedReader(isr);
    try {
      final StringBuilder buf = new StringBuilder();
      String line;
      while((line = br.readLine()) != null) {
        buf.append(line).append('\n');
      }
      return buf.toString();
    } finally {
      br.close();
      isr.close();
    }
  }

  /** readAsByteArray
   * 
   * @param pInput
   * @param pSize
   * @return
   * @throws IOException 
   */
  static public byte[] readAsByteArray( final InputStream pInput ) throws IOException {
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nread;
    final byte[] data = new byte[10000];
    while ((nread = pInput.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nread);
    }
    buffer.flush();
    return buffer.toByteArray();
  }

  
  /** encode
   * @param pS
   * @return
   * @throws java.io.UnsupportedEncodingException
   */
  static public String encode( String pS ) throws UnsupportedEncodingException {
//    return URLEncoder.encode( pS, "UTF-8" );
    return URLENCODER.escape( pS );
  }

  /** xmlContentDeEscape
   * 
   * @param s
   * @return
   */
  static public String xmlContentUnEscape(String s) {
    s = s.replace( "&amp;", "&" );
    s = s.replace( "&lt;", "<" );
    return s;
  }

  
  /** decode
   * 
   * @param pS
   * @return
   * @throws java.io.UnsupportedEncodingException
   */
  static public String decode( String pS ) throws UnsupportedEncodingException {
    return URLDecoder.decode( pS, "UTF-8" );
  }
  
  /** Return the encoded path of the node
   */
  static public String getEncodedPath( Node pNode ) throws RepositoryException {
    Node rn = pNode;
    String p = "";
    while( rn!=null ) {
      try {
        String encName = encode(rn.getName());
//        if (rn.getIndex()>1) encName += "[" + rn.getIndex() + "]";
        if (rn.getIndex()>1) encName += "%5B" + rn.getIndex() + "%5D";
        if (p.equals("")) {
          p = encName + p;
        } else {
          p = encName + "/" + p;
        }
      } catch (Exception e) {
        throw new RepositoryException(e);
      }
      if (rn.getDepth()==0) break;
      rn = rn.getParent();
    }
//    p = "/" + p;
    return p;
  }


  /** convertNodeName
   *
   * @param pNode
   * @return
   * @throws javax.jcr.RepositoryException
   * @throws java.io.UnsupportedEncodingException
   */
  static public String convertNodeName( final Node pNode ) throws RepositoryException, UnsupportedEncodingException {
    final int ix = pNode.getIndex();
    if (ix==1) {
      return new String(pNode.getName().getBytes( "UTF-8" ));
    }
    return new String(pNode.getName().getBytes( "UTF-8" )) + '[' + ix + ']';
  }

  /** setCurrentModificationDate
   * 
   * @param pNode
   * @throws RepositoryException
   */
  static public void setCurrentModificationDate( final Node pNode ) throws RepositoryException {
    final Calendar c = Calendar.getInstance();
    try {
      pNode.setProperty( CARS_ActionContext.DEF_MODIFIED, c );
    } catch( final ItemNotFoundException ie ) {
      // **** No error, just trying to set it.
    }
    if (pNode.hasProperty( "jcr:lastModified" )) {
      pNode.setProperty( "jcr:lastModified", c );
    }
    return;
  }

  /** setExpireDate
   * 
   * @param pNode
   * @param pMinutes
   * @throws RepositoryException
   */
  static public void setExpireDate( final Node pNode, final int pMinutes ) throws RepositoryException {
    final Calendar c = Calendar.getInstance();
    c.add( Calendar.MINUTE, pMinutes );
    pNode.setProperty( "jecars:ExpireDate", c );
    return;
  }

  /** deleteDirectory
   *
   * @param pPath
   * @return
   */
  static public boolean deleteDirectory( final File pPath ) {
    if (pPath.exists()) {
      final File[] files = pPath.listFiles();
      for( int i=0; i<files.length; i++ ) {
        if(files[i].isDirectory()) {
          deleteDirectory(files[i]);
        } else {
          files[i].delete();
        }
      }
    }
    return pPath.delete();
  }

  /** getLinkedNode
   *  TODO check for endless links
   * 
   * @param pN
   * @param pPN
   * @return
   * @throws RepositoryException 
   */
  static public Node getLinkedNode( final Node pN ) throws RepositoryException {
    if (pN.hasProperty( "jecars:Link" )) {
      return getLinkedNode( pN.getProperty( "jecars:Link" ).getNode() );
    }
    return pN;
  }

  /** getLinkedProperty
   * 
   * @param pMain
   * @param pN
   * @param pPropertyName
   * @return
   * @throws Exception 
   */
  static public Property getLinkedProperty( final CARS_Main pMain, final Node pN, final String pPropertyName ) throws Exception {
    if (pN.hasProperty( pPropertyName )) {
      return pN.getProperty( pPropertyName );
    } else if (pN.hasProperty( "jecars:Link" )) {
      final Node n = pMain.getNode( pN.getProperty( CARS_ActionContext.gDefLink ).getString(), null, false );
      if (n==null) {
        return null;
      } else {
        return getLinkedProperty( pMain, pMain.getNode( pN.getProperty( CARS_ActionContext.gDefLink ).getString(), null, false ), pPropertyName );
      }
    } else {
      return null;
    }
  }

  
  /** getLinkedNode
   * 
   * @param pMain
   * @param pN
   * @return
   * @throws Exception 
   */
  static public Node getLinkedNode( final CARS_Main pMain, final Node pN ) throws Exception {
    if (pN.hasProperty( CARS_ActionContext.gDefLink )) {
      final Node n = pMain.getNode( pN.getProperty( CARS_ActionContext.gDefLink ).getString(), null, false );
      return getLinkedNode( pMain, n );
    }
    return pN;
  }

  
  /** copyInputResourceToDirectory
   * 
   * @param pLinkedNode
   * @param pDirectory
   * @throws IOException
   * @throws IllegalStateException
   * @throws RepositoryException 
   */
  static public File copyInputResourceToDirectory( final Node pLinkedNode, final File pDirectory ) throws IOException, IllegalStateException, RepositoryException {
      File    inputResFile = null;
      File      sourceFile = null;
      Binary           bin = null;
      InputStream       is = null;
      FileOutputStream fos = null;
      try {
        String name = pLinkedNode.getName();
        if (!name.contains( ":") && (!pLinkedNode.isNodeType( "jecars:parameterdata" ))) {
          if (pLinkedNode.hasProperty("jecars:URL")) {
            final String path = pLinkedNode.getProperty("jecars:URL").getValue().getString();
            final URL u = new URL(path);
            if (path.startsWith("file:/")) {
              sourceFile = new File(URLDecoder.decode(u.getFile(), "UTF-8"));
            } else {
              is = u.openStream();
            }
          } else  if (pLinkedNode.hasProperty("jecars:PathToFile")) {
            sourceFile = new File(pLinkedNode.getProperty("jecars:PathToFile").getValue().getString());
          } else if (pLinkedNode.hasProperty( "jcr:data" )) {
            bin = pLinkedNode.getProperty("jcr:data").getBinary();
            is = bin.getStream();
          }
          inputResFile = new File( pDirectory, name );
          if (sourceFile == null) {
            if (is==null) {
              // **** No contents
            } else {
              fos = new FileOutputStream(inputResFile);
              sendInputStreamToOutputStream(50000, is, fos);
            }
          } else {
            sendInputToOutputNIOBuffer(sourceFile, inputResFile);
          }
        }
      } finally {
        if (bin != null) {
          bin.dispose();
        }
        if (fos != null) {
          fos.close();
        }
        if (is != null) {
          is.close();
        }
      }
      return inputResFile;
    }

  
}
