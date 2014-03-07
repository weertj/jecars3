/*
 * Copyright 2007-2012 NLR - National Aerospace Laboratory
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

import com.google.gdata.util.common.base.StringUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.*;
import javax.jcr.*;
import javax.jcr.nodetype.PropertyDefinition;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import nl.msd.jdots.JD_Taglist;
import org.jecars.jaas.CARS_PasswordService;
import org.jecars.apps.CARS_DefaultInterface;
import org.jecars.apps.CARS_Interface;
import org.jecars.version.CARS_VersionManager;
import org.w3c.dom.NamedNodeMap;


/**
 * CARS_DefaultMain
 *
 * @version $Id: CARS_DefaultMain.java,v 1.52 2009/07/30 12:07:42 weertj Exp $
 */
public class CARS_DefaultMain implements CARS_Main {

  static final public Logger LOG = Logger.getLogger( "org.jecars" );

  static final private String CARS_INTERFACE     = "CARS_Interface";
  static final public  String INTERFACECLASS     = "InterfaceClass";
  static final public  String DEF_INTERFACECLASS = CARS_Definitions.DEFAULTNS + INTERFACECLASS;

  static final public String SPECIAL_PREFIX                 = "!_#_!";
  static final public String PREFIX_VALUE_REMOVE            = SPECIAL_PREFIX + "VREMOVE";
  static final public String UNSTRUCT_PREFIX_DOUBLE         = SPECIAL_PREFIX + "UD";
  static final public String UNSTRUCT_PREFIX_LONG           = SPECIAL_PREFIX + "UL";
  static final public String UNSTRUCT_PREFIX_BOOLEAN        = SPECIAL_PREFIX + "UB";
  static final public String UNSTRUCT_PREFIX_MSTRINGS       = SPECIAL_PREFIX + "UMS";
  static final public String UNSTRUCT_PREFIX_MDOUBLE        = SPECIAL_PREFIX + "UMD";
  static final public String UNSTRUCT_PREFIX_MLONG          = SPECIAL_PREFIX + "UML";
  static final private Value[] VALUE0 = new Value[0];

  private final transient CARS_Factory  mFactory;
  private final transient Session       mSession;
  private final transient Node          mUserNode;
  private Node                          mCurrentView         = null;
  private Property                      mCurrentPropertyView = null;
  private final List<CARS_ActionContext>mContexts = new ArrayList<CARS_ActionContext>();
  
  static final private Object NODEMUTATION_LOCK = new Object();

  private final static DocumentBuilderFactory gFactory = DocumentBuilderFactory.newInstance();
//  protected static DocumentBuilder        gBuilder = null;
  
  static {
    gFactory.setNamespaceAware( true );
  }

  
  /** Creates a new instance of CARS_DefaultMain
   *
   * @param pSession
   * @param pFactory
   * @throws RepositoryException
   */
  protected CARS_DefaultMain( final Session pSession, final CARS_Factory pFactory ) throws RepositoryException  {
    mSession = pSession;
    mFactory = pFactory;
    if (mSession==null) {
      mUserNode = null;
    } else {
      final Node users = getUsers();
      final String userId = mSession.getUserID();
      if (users.hasNode( userId )) {
        mUserNode = getUsers().getNode( userId );
      } else {
        mUserNode = null;
      }
    }
    return;
  }
  
  /** getDocumentBuilder
   * 
   * @return
   */
  protected DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
//    return gBuilder;
    return gFactory.newDocumentBuilder();
  }
  
  /** Add node and also take care of all special cases
   * 
   * @param pParent
   * @param pName
   * @param pNodeType
   * @return
   * @throws java.lang.Exception
   */
  static public Node addNode( final Node pParent, final String pName, final String pNodeType ) throws RepositoryException {
    Node node;
    final Calendar cal = Calendar.getInstance();
    if (pNodeType==null) {
      node = pParent.addNode( pName );
    } else {
      node = pParent.addNode( pName, pNodeType );
    }
    if (node.isNodeType( "nt:resource" )) {
      final ValueFactory vf = node.getSession().getValueFactory();
      node.setProperty( "jcr:data", vf.createValue( new ByteArrayInputStream( "".getBytes() ) ));
      node.setProperty( "jcr:lastModified", cal );
      node.setProperty( "jcr:mimeType", "text/plain" );
    }

    // **** Update the Modified property in the parent
    CARS_Utils.setCurrentModificationDate( node );
//    pParent.setProperty( CARS_ActionContext.DEF_MODIFIED, cal );

//    Calendar cal = Calendar.getInstance();
//    node.setProperty( "jecars:Created", cal );
    return node;
  }
  

  /** Add permission object
   * @param pParentNode the node under which the permission node is added
   * @param pGroupname the group name (without the path), may be null
   * @param pUsername the user name (without the path), may be null
   * @param pRights the rights stored in a string e.g. "read,add_node"
   * @return The created permission node
   * @throws Exception when an exception occurs
   */
  @Deprecated
  @Override
  public Node addPermission( Node pParentNode, String pGroupname, String pUsername, String pRights ) throws Exception {
    Node n = null;
    Session appSession = CARS_Factory.getSystemApplicationSession();
    synchronized( appSession ) {
      Node prin = null;
      if (pGroupname!=null) prin = appSession.getRootNode().getNode( CARS_Definitions.MAINFOLDER + "/default/Groups" + "/" + pGroupname );
      if (pUsername!=null)  prin = appSession.getRootNode().getNode( CARS_Definitions.MAINFOLDER + "/default/Users"  + "/" + pUsername  );
      if (pParentNode.hasNode( "P_" + prin.getName() )==false) {
        n = pParentNode.addNode( "P_" + prin.getName(), CARS_Definitions.DEFAULTNS + "Permission" );
      } else {
        n = pParentNode.getNode( "P_" + prin.getName() );
      }
//      Value[] vals = {appSession.getValueFactory().createValue( prin )};
      Value[] vals = {appSession.getValueFactory().createValue( prin.getPath() )};
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
    }
    return n;
  }

  /** setCurrentViewNode
   *
   * @param pNode
   */
  @Override
  public void setCurrentViewNode( final Node pNode ) {
    mCurrentView = pNode;
    return;
  }
  
     
  @Override
  public Node getCurrentViewNode() {
    return mCurrentView;
  }

  @Override
  public void setCurrentViewProperty( final Property pProp ) {
    mCurrentPropertyView = pProp;
    return;
  }
  
     
  @Override
  public Property getCurrentViewProperty() {
    return mCurrentPropertyView;
  }
  
  /** addContext
   *
   * @param pContext
   */
  @Override
  public void addContext( final CARS_ActionContext pContext ) {
    synchronized( mContexts ) {
      mContexts.clear();
      mContexts.add( pContext );
      pContext.putMain( this );
    }
    return;
  }
  
  /** Get the default action context
   * 
   * @return
   */
  @Override
  public CARS_ActionContext getContext() {
    return getContext(0);
  }
   
  /** getContext
   * @param pNo
   * @return
   */
  @Override
  public CARS_ActionContext getContext( final int pNo ) {
    synchronized( mContexts ) {
      if (mContexts.size()>pNo) {
        return mContexts.get(pNo);
      }
    }
    return null;
//    if (mContexts!=null) {
//      if (mContexts.size()>pNo) {
//        return mContexts.get(pNo);
//      }
//    }
//    return null;
  }

  /** removeContext
   *
   * @param pContext
   */
  @Override
  public void removeContext( final CARS_ActionContext pContext ) {
    synchronized( mContexts ) {
      mContexts.remove( pContext );
      if (mContexts.isEmpty()) {
        destroy();
      }
    }
    return;
  }

     
  @Override
  public Session getSession() {
    return mSession;
  }

  /*
  protected void setSession( Session pSession ) throws Exception {
    mSession = pSession;
    Node users = null;
//  try {
    users = getUsers();
    if (users.hasNode( mSession.getUserID() )) {
      mUserNode = getUsers().getNode( mSession.getUserID() );
    }
//    } catch (Exception e) {
//      LOG.log( Level.WARNING, null, e );
//    }
    return;
  }
   */


  /** getLoginUser
   *
   * @return
   */
  @Override
  public Node getLoginUser() {
    return mUserNode;
  }

  /*
  protected void setFactory( CARS_Factory pFactory ) {
    mFactory = pFactory;
    return;
  }
   */

  /** Get the store CARS_Factory
   */     
  @Override
  public CARS_Factory getFactory() {
    return mFactory;
  }
  
  /** Add a node reference to a multivalued property
   * @param pNode Property of this node
   * @param pProperty The name of the property
   * @param pReference The node reference
   * @return the multivalued property
   * @throws Exception when an error occurs
   */     
  @Override
  public Property addReference( Node pNode, String pProperty, Node pReference ) throws Exception {
    Property p;
    if (pNode.hasProperty( pProperty )==false) {
      ArrayList<Value> l = new ArrayList<Value>();
      Value v = pNode.getSession().getValueFactory().createValue( pReference );
      l.add( v );
      p = pNode.setProperty( pProperty,l.toArray(new Value[0]) );
    } else {
      Value[] refs = pNode.getProperty( pProperty ).getValues();
      ArrayList<Value> l = new ArrayList<Value>();
      l.addAll( Arrays.asList( refs ) );
      Value v = pNode.getSession().getValueFactory().createValue( pReference );
      l.add( v );
      p = pNode.setProperty( pProperty,l.toArray(new Value[0]) );
    }
    return p;
  }
  

  /** addUser
   *
   * @param pID
   * @param pPassword
   * @return
   * @throws Exception
   */
  @Override
  public Node addUser( final String pID, final char[] pPassword ) throws Exception {
    return addUser( pID, pPassword, CARS_Definitions.DEFAULTNS + "User" );
  }
  
  /** addUser
   * 
   * @param pID
   * @param pPassword
   * @param pUserNodeType
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public Node addUser( String pID, char[] pPassword, String pUserNodeType ) throws Exception {
    Node n = getUsers();
    Node nn = n.addNode( pID, pUserNodeType );
    nn.setProperty( CARS_Definitions.DEFAULTNS + "Source", getUserSources().getNode( "internal" ).getPath() );
    nn.setProperty( CARS_Definitions.DEFAULTNS + "Fullname", pID );
    CARS_DefaultMain.setCryptedProperty( nn, "jecars:Password_crypt", new String(pPassword) );
//s    nn.setProperty( DEFAULTNS + "Password_crypt", CARS_PasswordService.getInstance().encrypt(new String(pPassword)) );
    Node gn = getGroups();
    Node world = gn.getNode( "World" );
//    addReference( world, DEFAULTNS + "GroupMembers", nn );
    CARS_Utils.addMultiProperty( world, CARS_Definitions.DEFAULTNS + "GroupMembers", nn.getPath(), false );
    n.save();
    world.save();
    return nn;
  }
  
     
  @Override
  public Node addGroup( String pID, String pFullname ) throws Exception {
    return addGroup( pID, pFullname, CARS_Definitions.DEFAULTNS + "Group" );
  }
  
     
  @Override
  public Node addGroup( String pID, String pFullname, String pGroupNodeType ) throws Exception {
    Node n = getGroups();
    Node nn = n.addNode( pID, pGroupNodeType );
    nn.setProperty( CARS_Definitions.DEFAULTNS + "Fullname", pFullname );
    n.save();
    return nn;
  }

  
  @Override
  public void destroy() {
    if (mSession!=null) {
//      JackrabbitRepository jr = (JackrabbitRepository)mSession.getRepository();
//      if (CARS_Factory.isInSessionPool( mSession )==false) {
//      if (mSession.isLive()) {
        mSession.logout();
//      }
//      }
//      mSession = null;
      mCurrentView = null;
//      mUserNode = null;
//      mFactory = null;
//      jr.shutdown();
    }
    return;
  }

  
  /** Get groups
   */
  @Override
  public Node getGroups() throws Exception {
    return mSession.getRootNode().getNode( CARS_Definitions.MAINFOLDER + "/default/Groups" );
  }

  /** Get the root
   */
  @Override
  public Node getRoot() throws RepositoryException {
    return mSession.getRootNode();
  }

  /** getNodeWithInterface
   *
   * @param pAL
   * @param pStartNode
   * @return
   * @throws Exception
   */
  private int getNodeWithInterface( final List<String>pAL, int pStartNode ) throws Exception {
//    if (pAL.size()==0) return pStartNode;
    final int bi = pStartNode;
    Node n = getRoot().getNode( pAL.get( pStartNode ) );
    while( pStartNode>=0 ) {
      if (n.hasProperty( DEF_INTERFACECLASS )) {
        break;
      }
      if (pStartNode==0) {
        pStartNode = bi;
        break;
      }
      n = getRoot().getNode( pAL.get( --pStartNode ) );
    }
    return pStartNode;
  }
  
  /** Check if the property is a known mixin type, if so then add the mixin
   * @return true if a mixin is added
   */
  protected boolean checkAndAddMixin( final Node pNode, final String pPropName ) throws RepositoryException {
    if ("jecars:Keywords".equals( pPropName )) {
      pNode.addMixin( "jecars:keywordable" );
      return true;
    }
    return false;
  }
  
  /** Set a property of a node, multiple values are supported
   * @param pNode the node of which a property will be set
   * @param pPropName property name
   * @param pValue the value,
   *                 for multi value;
   *                    +.... (to add a value)
   *                    -.... (to remove a value)
   *                    *.... (to replace the values)
   *                    ~ to remove all values
   * @throws Exception when an exception occurs.
   */
  @Override
  public Property setParamProperty( final Node pNode, final String pPropName, String pValue ) throws Exception {
    
    if (pPropName.equals( CARS_Definitions.DEFAULTNS + "title" )) {
      if (pValue.indexOf( '/' )==-1) {
        // **** The node must be renamed
        CARS_Factory.getEventService().offer( new CARS_Event( this, null, "URL", "MOVE", getContext(), pNode.getPath() + " to " + pNode.getParent().getPath() + "/" + pValue ) );
//        CARS_Factory.getEventManager().addEvent( this, mUserNode, pNode, null, "URL", "MOVE",
//                pNode.getPath() + " to " + pNode.getParent().getPath() + "/" + pValue );
        pNode.getSession().getWorkspace().move( pNode.getPath(), pNode.getParent().getPath() + "/" + pValue );
      } else {
        // **** The node must be moved
        CARS_Factory.getEventService().offer( new CARS_Event( this, null, "URL", "MOVE", getContext(), pNode.getPath() + " to " + pValue ) );
//        CARS_Factory.getEventManager().addEvent( this, mUserNode, pNode, null, "URL", "MOVE",
//                pNode.getPath() + " to " + pValue );
        pNode.getSession().getWorkspace().move( pNode.getPath(), pValue );
      }
      return null;
    }

    Property prop;
    if (pNode.hasProperty( pPropName )) {

      prop = pNode.getProperty( pPropName );

      // **** Check the specials
      if (PREFIX_VALUE_REMOVE.equals( pValue )) {
        if (prop!=null) {
          prop.remove();
        }
        return null;
      }

      // **********************************************************************
      // **** Property exists... modification
      if (prop.getDefinition().isMultiple()) {
        // **** Multiple values
        String paramValue = pValue;
        if ((paramValue.charAt(0)=='+') || (paramValue.charAt(0)=='*')) {
          final String[] values = paramValue.substring(1).split( "," );
          for (final String value : values) {
            prop = pNode.getProperty( pPropName );
            final ArrayList<Value> al;
            if (paramValue.charAt(0)=='*') {
              al = new ArrayList<Value>();
              paramValue = "+" + paramValue.substring(1);
            } else {
              al = new ArrayList<Value>(Arrays.asList(prop.getValues()));
            }
            switch( prop.getType() ) {
              // **** TODO: References are only added when no duplicate is discovered
              case PropertyType.REFERENCE:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( CARS_Utils.getNodeByString( pNode.getSession(), value ) );
                if (!al.contains(newVal)) {
                  al.add( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                }
                break;
              }
              // **** TODO: Paths are only added when no duplicate is discovered
              case PropertyType.PATH: {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.PATH );
                if (!al.contains(newVal)) {
                  al.add( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                }
                break;
              }
              // **** TODO: Strings are only added when no duplicate is discovered
              case PropertyType.STRING:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.STRING );
                if (!al.contains(newVal)) {
                  al.add( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                }
                break;
              }
              case PropertyType.DOUBLE:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.DOUBLE );
                al.add( newVal );
                prop.setValue( al.toArray( VALUE0 ) );
                break;
              }
              case PropertyType.LONG:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.LONG );
                al.add( newVal );
                prop.setValue( al.toArray( VALUE0 ) );
                break;
              }
              case PropertyType.BOOLEAN:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.BOOLEAN );
                al.add( newVal );
                prop.setValue( al.toArray( VALUE0 ) );
                break;
              }
              default:
                  throw new Exception( "Property type: " + prop.getName() + " not supported" );
            }
          }
        } else if (pValue.charAt(0)=='-') { //pValue.startsWith( "-" )) {
          // **** The value must be removed
          final String[] values = pValue.substring(1).split( "," );
          for (final String value : values) {
            prop = pNode.getProperty( pPropName );
//          pValue = pValue.substring(1);
            ArrayList<Value> al = new ArrayList<Value>(Arrays.asList(prop.getValues()));
            switch( prop.getType() ) {
              case PropertyType.REFERENCE:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( CARS_Utils.getNodeByString( pNode.getSession(), value ) );
                if (al.contains(newVal)) {
                  al.remove( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                } else {
                  // **** Value not found.... no error
                }
                break;
              }
              case PropertyType.PATH: {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.PATH );
                if (al.contains(newVal)) {
                  al.remove( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                } else {
                  // **** Value not found.... no error                  
                }
                break;                
              }
              case PropertyType.STRING:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.STRING );
                if (al.contains(newVal)) {
                  al.remove( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                } else {
                  // **** Value not found.... no error                  
                }
                break;
              }
              case PropertyType.DOUBLE:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.DOUBLE );
                if (al.contains(newVal)) {
                  al.remove( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                } else {
                  // **** Value not found.... no error
                }
                break;
              }
              case PropertyType.LONG:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.LONG );
                if (al.contains(newVal)) {
                  al.remove( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                } else {
                  // **** Value not found.... no error
                }
                break;
              }
              case PropertyType.BOOLEAN:  {
                final Value newVal = pNode.getSession().getValueFactory().createValue( value, PropertyType.BOOLEAN );
                if (al.contains(newVal)) {
                  al.remove( newVal );
                  prop.setValue( al.toArray( VALUE0 ) );
                } else {
                  // **** Value not found.... no error
                }
                break;
              }
              default:
                  throw new Exception( "Property type: " + prop.getName() + " not supported" );
            }
          }
        } else if ("~".equals( pValue )) {
          // **** Remove all property values
          prop.setValue( (Value[])null );
        } else {
          // **** Error modifier not recon.
          throw new Exception( "Multiple property value should start with '+','*',-' or '~' (" + pPropName + ")" );
        }
      } else {
        // ******************************
        // **** Property exist, modify it
        final PropertyDefinition pd = CARS_Utils.getPropertyDefinition( pNode, pPropName );
//        prop.setValue( pValue );

        if (pd==null) {
          // **** Check if the node inherits from nt:unstructured
          if ((pNode.isNodeType( "nt:unstructured" )) ||
              (pNode.isNodeType( "jecars:mixin_unstructured" ))) {
            if (pValue.startsWith( UNSTRUCT_PREFIX_DOUBLE )) {
              prop = pNode.setProperty( pPropName, Double.parseDouble( pValue.substring( UNSTRUCT_PREFIX_DOUBLE.length() ) ) );
            } else if (pValue.startsWith( UNSTRUCT_PREFIX_BOOLEAN )) {
              prop = pNode.setProperty( pPropName, Boolean.parseBoolean( pValue.substring( UNSTRUCT_PREFIX_BOOLEAN.length() ) ) );
            } else {
              prop = pNode.setProperty( pPropName, pValue );
            }
            return prop;
          } else {
            throw new Exception( "No definition for propertytype: " + pPropName );
          }
        }
        
        // **** No multiple
        switch( pd.getRequiredType() ) {
          case PropertyType.BOOLEAN:   { prop = pNode.setProperty( pPropName, Boolean.parseBoolean(pValue) ); break; }
          case PropertyType.STRING:    { 
             if (pPropName.endsWith( "_crypt" )) {
//               prop = pNode.setProperty( pPropName, CARS_PasswordService.getInstance().encrypt( pValue ) );
               prop = setCryptedProperty( pNode, pPropName, pValue );
             } else {
               prop = pNode.setProperty( pPropName, pValue );
             }
             break; 
           }
          case PropertyType.REFERENCE: { prop = pNode.setProperty( pPropName, CARS_Utils.getNodeByString( pNode.getSession(), pValue ) ); break; }
          case PropertyType.LONG:      { prop = pNode.setProperty( pPropName, Long.parseLong(pValue) ); break; }
          case PropertyType.DOUBLE:    { prop = pNode.setProperty( pPropName, Double.parseDouble(pValue) ); break; }
//          case PropertyType.PATH:      { prop = pNode.setProperty( pPropName, CARS_Utils.getNodeByString( pNode.getSession(), pValue ).getPath() ); break; }
          case PropertyType.PATH:      { prop = pNode.setProperty( pPropName, pValue ); break; }
          case PropertyType.BINARY:    { prop = pNode.setProperty( pPropName, pValue ); break; }
          case PropertyType.DATE:      { prop = pNode.setProperty( pPropName, CARS_ActionContext.getCalendarFromString( pValue) ); break; }
          default: throw new Exception( "Property type: " + pd.getName() + " not supported" );
        }
      
      }
    } else {
      // **************************************************************************
      // **** New property

      // **** Check the specials
      if (PREFIX_VALUE_REMOVE.equals( pValue )) {
        return null;
      }

      PropertyDefinition pd = CARS_Utils.getPropertyDefinition( pNode, pPropName );
      boolean isMultiple = false;
      int reqType = 0;
      if (pd==null) {
        // **** Check if the property is part of a mixin nodetype
        if (checkAndAddMixin( pNode, pPropName )) {
          pd = CARS_Utils.getPropertyDefinition( pNode, pPropName );          
        }
        if (pd==null) {
          // **** Check if the node inherits from nt:unstructured
          if ((pNode.isNodeType( "nt:unstructured" )) ||
              (pNode.isNodeType( "jecars:mixin_unstructured" ))) {
            if (pValue.startsWith( UNSTRUCT_PREFIX_DOUBLE )) {
              prop = pNode.setProperty( pPropName, Double.parseDouble( pValue.substring( UNSTRUCT_PREFIX_DOUBLE.length() ) ) );
            } else if (pValue.startsWith( UNSTRUCT_PREFIX_LONG )) {
              prop = pNode.setProperty( pPropName, Long.parseLong( pValue.substring( UNSTRUCT_PREFIX_LONG.length() ) ) );
            } else if (pValue.startsWith( UNSTRUCT_PREFIX_BOOLEAN )) {
              prop = pNode.setProperty( pPropName, Boolean.parseBoolean( pValue.substring( UNSTRUCT_PREFIX_BOOLEAN.length() ) ) );
            } else if (pValue.startsWith( UNSTRUCT_PREFIX_MSTRINGS )) {
              prop = pNode.setProperty( pPropName, pValue.substring( UNSTRUCT_PREFIX_MSTRINGS.length() ).split( "\n" ) );
            } else if (pValue.startsWith( UNSTRUCT_PREFIX_MLONG )) {
              isMultiple = true;
              pValue = pValue.substring( UNSTRUCT_PREFIX_MLONG.length() );
              reqType = PropertyType.LONG;
              prop = null;
            } else if (pValue.startsWith( UNSTRUCT_PREFIX_MDOUBLE )) {
              isMultiple = true;
              pValue = pValue.substring( UNSTRUCT_PREFIX_MDOUBLE.length() );
              reqType = PropertyType.DOUBLE;
              prop = null;
            } else {
              prop = pNode.setProperty( pPropName, pValue );
            }
            if (prop!=null) {
              return prop;
            }
          } else {
            throw new Exception( "No definition for propertytype: " + pPropName );
          }
        }
      }

      if (!isMultiple) {
        isMultiple = pd.isMultiple();
      }

      if (isMultiple) {
        // **** Is multiple
        if (pValue.charAt(0)=='~') return null;
//        if (value.startsWith( "+" )) value = pValue.substring(1);        
        String[] values;
        if (pValue.charAt(0)=='+') values = pValue.substring(1).split( "," );
        else values = pValue.split( "," );

        // **** No attr modifiers
        if (reqType==0) {
          reqType = pd.getRequiredType();
        }
        switch( reqType ) {
          case PropertyType.PATH:
          case PropertyType.STRING: {
            prop = pNode.setProperty( pPropName, values );
            break;
          }
          case PropertyType.LONG: {
            Value[] sv = new Value[values.length];
            int i = 0;
            for( String vs : values ) {
              sv[i++] = pNode.getSession().getValueFactory().createValue( Long.parseLong( vs ) );
            }
            prop = pNode.setProperty( pPropName, sv );
            break;
          }
          case PropertyType.DOUBLE: {
            Value[] sv = new Value[values.length];
            int i = 0;
            for( String vs : values ) {
              sv[i++] = pNode.getSession().getValueFactory().createValue( Double.parseDouble( vs ) );
            }
            prop = pNode.setProperty( pPropName, sv );
            break;
          }
          case PropertyType.BOOLEAN: {
            Value[] sv = new Value[values.length];
            int i = 0;
            for( String vs : values ) {
              sv[i++] = pNode.getSession().getValueFactory().createValue( Boolean.parseBoolean( vs ) );
            }
            prop = pNode.setProperty( pPropName, sv );
            break;
          }
          case PropertyType.REFERENCE: {
            Value[] sv = new Value[values.length];
            int i = 0;
            for( String vs : values ) {
              sv[i++] = pNode.getSession().getValueFactory().createValue( CARS_Utils.getNodeByString( pNode.getSession(), vs ) );
            }
            prop = pNode.setProperty( pPropName, sv );
            break;
          }
          default: throw new Exception( "Property type: " + pd.getName() + " not supported (multi)" );
        }

        /*
        for (String value : values) {
          // **** No attr modifiers
          switch( pd.getRequiredType() ) {
            case PropertyType.PATH:
            case PropertyType.STRING: {
              prop = pNode.setProperty( pPropName, values );  // TODO (values ipv value)
               return prop;  // **** Ready
            }
            case PropertyType.REFERENCE: { 
              Value[] sv = new Value[1];
              sv[0] = pNode.getSession().getValueFactory().createValue( CARS_Utils.getNodeByString( pNode.getSession(), value ) );
               prop = pNode.setProperty( pPropName, sv ); break; }
            default: throw new Exception( "Property type: " + pd.getName() + " not supported (multi)" );
          }
        }
         */
      } else {
        // **** No multiple
        switch( pd.getRequiredType() ) {
          case PropertyType.BOOLEAN:   { prop = pNode.setProperty( pPropName, Boolean.parseBoolean(pValue) ); break; }
          case PropertyType.STRING:    { 
             if (pPropName.endsWith( "_crypt" )) {               
               //prop = pNode.setProperty( pPropName, CARS_PasswordService.getInstance().encrypt( pValue ) );
               prop = setCryptedProperty( pNode, pPropName, pValue );
             } else {
               prop = pNode.setProperty( pPropName, pValue );
             }
             break; 
           }
          case PropertyType.REFERENCE: { prop = pNode.setProperty( pPropName, CARS_Utils.getNodeByString( pNode.getSession(), pValue ) ); break; }
          case PropertyType.LONG:      { prop = pNode.setProperty( pPropName, Long.parseLong(pValue) ); break; }
          case PropertyType.DOUBLE:    { prop = pNode.setProperty( pPropName, Double.parseDouble(pValue) ); break; }
//          case PropertyType.PATH:      { prop = pNode.setProperty( pPropName, CARS_Utils.getNodeByString( pNode.getSession(), pValue ).getPath() ); break; }
          case PropertyType.PATH:      {
            if ((pValue==null) || "".equals( pValue )) {              
              prop = pNode.setProperty( pPropName, (String)null );
            } else {
              prop = pNode.setProperty( pPropName, pValue );
            }
            break;
          }
          case PropertyType.BINARY:    { prop = pNode.setProperty( pPropName, pValue ); break; }
          case PropertyType.DATE:      { prop = pNode.setProperty( pPropName, CARS_ActionContext.getCalendarFromString( pValue) ); break; }
          default: throw new Exception( "Property type: " + pd.getName() + " not supported" );
        }
      }
    }
    
    return prop;
  }

  /** setCryptedProperty
   *
   * @param pNode
   * @param pPropName
   * @param pValue
   * @return
   * @throws RepositoryException
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   */
  static public Property setCryptedProperty( final Node pNode, final String pPropName, final String pValue ) throws RepositoryException, NoSuchAlgorithmException, UnsupportedEncodingException {
    final Property prop = pNode.setProperty( pPropName, CARS_PasswordService.getInstance().encrypt( pValue ) );
    if (pNode.isNodeType( "jecars:User" )) {
      if (!pNode.isNodeType( "jecars:digestauth" )) {
        pNode.addMixin( "jecars:digestauth" );
      }
      final MessageDigest md = MessageDigest.getInstance( "MD5" );
      final byte[] md5 = md.digest( ( pNode.getName() + ":" + CARS_Definitions.getRealm() + ":" + pValue ).getBytes() );
      final String ha1 = StringUtil.bytesToHexString( md5 );
      pNode.setProperty( "jecars:HA1", ha1 );
    }
    return prop;
  }

  /** Set the jecars:Id property on the given node
   * @param pNode The node
   * @throws java.lang.Exception when an exception occurs
   */
  @Override
  public void setId( Node pNode ) throws Exception {
    if (pNode.hasProperty( "jecars:Id" )==false) {
      Session ses = CARS_Factory.getSystemCarsSession();
      synchronized(ses) {
        Node main = ses.getRootNode().getNode( "JeCARS" );
        long id = main.getProperty( "jecars:CurrentId" ).getLong();
        pNode.setProperty( "jecars:Id", id++ );
        main.setProperty( "jecars:CurrentId", id );
        ses.save();
      }
    }
    return;
  }
  
  /** Retrieve the jecars:Id property from the node
   * @param pNode The node
   * @return id
   * @throws java.lang.Exception
   */
  @Override
  public long getId( Node pNode ) throws Exception {
    if ((pNode!=null) && (pNode.hasProperty( "jecars:Id" )==true)) {
      return pNode.getProperty( "jecars:Id" ).getLong();
    }
    return -1;
  }

  /** getIndexParamFromTL
   * @param pParamsTL
   * @param pTag
   * @param pValue
   * @return
   * @throws java.lang.Exception
   */
  static public int getIndexParamFromTL( JD_Taglist pParamsTL, String pTag, String pValue ) throws Exception {
    int ix = -1;
    int i = 0;
    String data;
    while( (data=(String)pParamsTL.getData( "$" + i + "." + pTag ))!=null ) {
      if (data.equals( pValue )) {
        ix = i;
        break;
      }
      i++;
    }
    
    i = 0;
    // **** if not found, check with the jecars: prefix    
    while( (data=(String)pParamsTL.getData( "jecars:$" + i + "." + pTag ))!=null ) {
      if (data.equals( pValue )) {
        ix = i;
        break;
      }
      i++;
    }
    return ix;
  }

  /** getParamFromTL
   * @param pParamsTL
   * @param pTag
   * @param pIndex
   * @return
   * @throws java.lang.Exception
   */
  static public String getParamFromTL( JD_Taglist pParamsTL, String pTag, int pIndex ) throws Exception {
    String v = (String)pParamsTL.getData( "$" + pIndex + "." + pTag );
    if (v==null) v = (String)pParamsTL.getData( "jecars:$" + pIndex + "." + pTag );
    return v;
  }
  
  /** Convert the body contents to paramtl (if possible)
   * @param pParamsTL taglist in which the resulting paramaters are stored
   * @param pBody the inputstream
   * @param pBodyContentType the mime type
   * @return true if the pBody contents was a parseable content, false if not
   * @throws Exception when an exception occurs
   */

  /* TODO
---- ==== ------=_Part_0_27355241.1192709085140
Content-Type: application/atom+xml

<entry xmlns='http://www.w3.org/2005/Atom'>
   <category scheme='http://schemas.google.com/g/2005#kind' term='http://schemas.google.com/docs/2007#document' label='document'/>
   <title type='text'>test.txt</title>
   <content type='text/plain'/>
</entry>
------=_Part_0_27355241.1192709085140
Content-Type: text/plain



ablalbalabl
sadksaodk
koasdkaso

------=_Part_0_27355241.1192709085140--

  */
  
  protected boolean streamToParamTL( JD_Taglist pParamsTL, InputStream pBody, String pBodyContentType ) throws Exception {
    if ((pBody!=null) && (pBodyContentType!=null)) {
// String xml = CARS_Utils.readAsString( pBody );
// System.out.println( " ---- ==type = " + pBodyContentType );
// System.out.println( " ---- ==== " + xml );
//      if (pBodyContentType.startsWith( "application/atom+xml" )) {
      if ("application/atom+xml".equals( pBodyContentType )) {
        org.w3c.dom.Document doc = getDocumentBuilder().parse( pBody );
        org.w3c.dom.NodeList nl  = doc.getChildNodes();
        org.w3c.dom.Node      n  = null;
        int i = 0;      
        while( i<nl.getLength() ) {
          n = nl.item(i++);
          if ((n.getNodeType()==n.ELEMENT_NODE) && (n.getLocalName().equals( "entry" ))) {
            // **** Parse the entry body
            org.w3c.dom.NodeList nl2  = n.getChildNodes();
            int ii = 0;
            while( ii<nl2.getLength() ) {
              n = nl2.item(ii++);
              if (n.getNodeType()==n.ELEMENT_NODE) {
                String prefix = n.getNodeName();
                if (prefix.indexOf( ':' )!=-1) prefix = prefix.substring( 0, prefix.indexOf( ':' ));
                if (CARS_ActionContext.gIncludeNS.contains( prefix )) {
//                  pParamsTL.replaceData( n.getNodeName(), n.getTextContent() ); 
//                  pParamsTL.replaceData( n.getNodeName(), StringUtil.unescapeHTML(n.getTextContent()) ); 
                  pParamsTL.replaceData( n.getNodeName(), CARS_Utils.xmlContentUnEscape(n.getTextContent()) ); 
                } else {
                  String nname = "$0." + n.getNodeName();
                  if (pParamsTL.getData( nname )!=null) {
                    int tc = 1;
                    while( pParamsTL.getData( "$" + tc + "." + n.getNodeName() )!=null) tc++;
                    nname = "$" + tc + "." + n.getNodeName();
                  }
//                  pParamsTL.replaceData( nname, n.getTextContent() );
                  pParamsTL.replaceData( nname, StringUtil.unescapeHTML(n.getTextContent()) );
                  if (n.getAttributes()!=null) {
                    NamedNodeMap nnm = n.getAttributes();
                    for( int nnmi = 0; nnmi<nnm.getLength(); nnmi++ ) {
                      org.w3c.dom.Node nnnm = nnm.item( nnmi );
//                      pParamsTL.replaceData( nname + "." + nnnm.getNodeName(), nnnm.getNodeValue() );
                      pParamsTL.replaceData( nname + "." + nnnm.getNodeName(), StringUtil.unescapeHTML(nnnm.getNodeValue()) );
                    }
                  }
                }
              }
            }
          }
        }
        return true;
      } else if ("application/x-www-form-urlencoded".equals( pBodyContentType )) {
        final String form = CARS_Utils.readAsString( pBody );
        if (!"".equals(form)) {
          final String[] param = form.split( "&" );
          for( int i=0; i<param.length; i++ ) {
            final String[] parts = param[i].split( "=", 2 );
            final String propName = CARS_ActionContext.convertPropertyName( CARS_ActionContext.untransportString(parts[0]));
            if (parts.length==2) {
              pParamsTL.replaceData( propName,
                          CARS_ActionContext.convertValueName( propName, CARS_ActionContext.untransportString(parts[1])) );
            } else {
              pParamsTL.replaceData( propName,
                          CARS_ActionContext.convertValueName( propName, CARS_ActionContext.untransportString("")) );
            }
          }
        }
      }
    }
    return false;
  }
  
  /** Check if the parameter is a versioning parameter
   * @param the parameter including the prefix
   * @return true if yes
   */
  private boolean isVersionParameter( final String pKey ) {
    if (pKey.startsWith( CARS_ActionContext.gDefVCS )) return true;
    return false;
  }

  /** Check if the parameter is a special parameter
   * @param the parameter including the prefix
   * @return true if yes
   */
  private boolean isPOSTParameter( final String pKey ) {
//    if ((pKey.startsWith( "jcr:" )) || (pKey.indexOf( "$" )!=-1)) return true;
    if (pKey.indexOf( '$' )!=-1) return true;
    if ("jcr:primaryType".equals( pKey )) return true;
    if ("jcr:mixinTypes".equals(  pKey )) return true;
    if ("jcr:created".equals(     pKey )) return true;
    if ("jcr:createdBy".equals(   pKey )) return true;
    if ("jcr:uuid".equals(        pKey )) return true;
    if ("jecars:alt".equals(      pKey )) return true;
//    if (pKey.equals( "jecars:X-HTTP-Method-Override" )) return true;
    if ("jecars:GOOGLELOGIN_AUTH".equals(  pKey )) return true;
    if ("jecars:EventCollectionID".equals( pKey )) return true;
    if ("jecars:FET".equals( pKey )) return true;
    if (pKey.startsWith( "jecars:X-" )) return true;
    return false;
  }

  /** addNodeNameProcessing
   *
   * @param pName
   * @return
   */
  private String addNodeNameProcessing( String pName ) {
    if (pName.indexOf( '[' )!=-1) {
      pName = pName.substring( 0, pName.indexOf( '[' ));
    }
    return pName;
  }

  /** Add a node to the JeCARS repository
   * @param pFullPath
   * @param pParamsTL
   * @param pBody
   * @param pBodyContentType
   * @return
   * @throws java.lang.Exception
   */
  @Override
  public Node addNode( final String pFullPath, final JD_Taglist pParamsTL, InputStream pBody, String pBodyContentType ) throws Exception {
    
    // **** Convert body contents
    if ((pBody!=null) && (streamToParamTL( pParamsTL, pBody, pBodyContentType ))) {
      pBody = null;
      pBodyContentType = null;
    }
    // **** Test if the node is added
    final JD_Taglist addNodeTags = new JD_Taglist();
    Node cnode;
    {      
      
      // ********************************************
      // **** The node is not known, add the new node
      // **** ----------------- CREATE ---------------
      final String path = pFullPath.substring( 0, pFullPath.lastIndexOf( '/' ));
      Node newNode = null;
      cnode = getNode( path, addNodeTags, false );
      CARS_Interface cars = null;
      Node           interfaceClass = null;
      if (addNodeTags.getData( CARS_INTERFACE )!=null) {
        cars = (CARS_Interface)addNodeTags.getData( CARS_INTERFACE );
        interfaceClass = (Node)addNodeTags.getData( INTERFACECLASS );
      }
      
      // ********************************
      // **** Calculate the primary type
      final CARS_DefaultInterface di = new CARS_DefaultInterface();
      String primType = (String)pParamsTL.getData( "jcr:primaryType" );
      if (primType==null) primType = (String)pParamsTL.getData( "$0.category.term" );
      if (primType==null) primType = (String)pParamsTL.getData( "jecars:$0.category.term" );
      String name = pFullPath.substring( pFullPath.lastIndexOf( '/' )+1);
      name = addNodeNameProcessing( name );
      
      synchronized(NODEMUTATION_LOCK) {

      // ********************************
      // **** Check for copy from (link)
         if ((pParamsTL.getData( "jecars:$0.link" )!=null) || (pParamsTL.getData( "$0.link" )!=null)) {
           List linkrel = pParamsTL.getDataList( "jecars:$0.link.rel" );
           if (linkrel==null) linkrel = pParamsTL.getDataList( "$0.link.rel" );
           List linkhref = pParamsTL.getDataList( "jecars:$0.link.href" );
           if (linkhref==null) linkhref = pParamsTL.getDataList( "$0.link.href" );
           for (int i = 0; i < linkrel.size(); i++) {
             final String rel = (String)linkrel.get( i );
             if ("via".equals( rel )) {
               final String href = (String)linkhref.get( i );
               if (href.startsWith( getContext().getBaseContextURL() )) {
                 final String copyPath = href.substring( getContext().getBaseContextURL().length() );
                 final Node copyNode = getNode( copyPath, null, false );
                 if (cnode.isNodeType( "jecars:Dav_deftypes" ) && cnode.hasProperty( "jecars:Dav_DefaultFileType" )) {
                   primType = cnode.getProperty( "jecars:Dav_DefaultFileType" ).getString();
                 } else {
                   primType = copyNode.getPrimaryNodeType().getName();
                 }
                 if (cars==null) {
                   newNode = di.copyNode( this, interfaceClass, cnode, copyNode, name, primType, pParamsTL );
                 } else {
                   newNode = cars.copyNode( this, interfaceClass, cnode, copyNode, name, primType, pParamsTL );
                 }
/*
                 final PropertyIterator pi = copyNode.getProperties();
                 Property cprop;
                 while( pi.hasNext() ) {
                   cprop = pi.nextProperty();
                   if (pParamsTL.getData( cprop.getName() )==null) {
                     pParamsTL.replaceData( cprop.getName(), cprop.getValue().getString() );
                   }
                 }
 */
               } else {
                 // **** TODO currently only local repository objects are supported
                 throw new Exception( "Copy of object " + href + " not supported" );
               }

             }
           }
         }
          
          // **** Add node, if not already created
          if (newNode==null) {
            if (cars==null) {
              newNode = di.addNode( this, interfaceClass, cnode, name, primType, pParamsTL );
            } else {
              newNode = cars.addNode( this, interfaceClass, cnode, name, primType, pParamsTL );
            }
          }

          // **** Check for mixin types
          Iterator it = pParamsTL.getIterator();
          String key;
          while( it.hasNext() ) {
            key = (String)it.next();
            if ("jcr:mixinTypes".equals( key )) {
              final String mixinType = (String)pParamsTL.getData( key );
              if (mixinType.startsWith( "-" )) {
                // **** The mixin type must removed
                newNode.removeMixin( mixinType.substring(1) );
              } else if (mixinType.startsWith( "+" )) {
                // **** The mixin type must removed
                newNode.addMixin( mixinType.substring(1) );
              } else {
                newNode.addMixin( mixinType );
              }
            }
          }

          it = pParamsTL.getIterator();
          while( it.hasNext() ) {
            key = (String)it.next();
            if (!isPOSTParameter( key )) {
              final Object data = pParamsTL.getData( key );
              if (data instanceof String) {
                if (cars==null) {
                  di.setParamProperty( this, null, newNode, key, (String)pParamsTL.getData( key ) );
                } else {
                  cars.setParamProperty( this, interfaceClass, newNode, key, (String)pParamsTL.getData( key ) );
                }
              } else if (data instanceof InputStream) {
                if (pBody==null) {
                  pBody = (InputStream)data;
                  pBodyContentType = "";
                }
              }
            }
          }
          if (cars==null) {
            di.nodeAdded( this, null, newNode, pBody );
            // **** The binary data
            di.setBodyStream( this, null, newNode, pBody, pBodyContentType );
          } else {
            cars.nodeAdded( this, interfaceClass, newNode, pBody );
            // **** The binary data
            cars.setBodyStream( this, interfaceClass, newNode, pBody, pBodyContentType );
          }
          cnode.save();
          if (cnode.getDepth()>0) {
            cnode.getParent().save();
          }
          cnode = newNode;
          if (cars==null) {
            di.nodeAddedAndSaved( this, null, newNode );
          } else {
            cars.nodeAddedAndSaved( this, interfaceClass, newNode );
          }
      }
    }
    return cnode;
  }

  /** Update node to the JeCARS repository
   * 
   * @param pFullPath
   * @param pParamsTL
   * @param pBody
   * @param pBodyContentType
   * @return the updated node
   * @throws java.lang.Exception
   */
  @Override
  public Node updateNode( final String pFullPath, final JD_Taglist pParamsTL, InputStream pBody, String pBodyContentType ) throws Exception {

    Exception localException = null;

    // **** Convert body contents
    if (pBody!=null) {
      if (streamToParamTL( pParamsTL, pBody, pBodyContentType )) {
        pBody = null;
        pBodyContentType = null;
      }
    }
    // **** If no alt=? parameter is set, set the atom_entry (APP standard)
    if (pParamsTL.getData( CARS_ActionContext.gDefAlt )==null)
        pParamsTL.putData( CARS_ActionContext.gDefAlt, "atom_entry" );
    
    // **** Test if the node is added
    final JD_Taglist updateNodeTags = new JD_Taglist();
    Node     cnode = null;
    Property cprop;
    try {
      final Item item = getNode( pFullPath, updateNodeTags, false );
      if (item.isNode()) {
        cnode = (Node)item;
      } else {
        cprop = (Property)item;
        cnode = cprop.getParent();
        pParamsTL.replaceData( cprop.getName(), null );
      }
    } catch (CARS_RESTMethodHandled re) {
      return null;
    } catch (CARS_CustomException ce) {
      throw ce;
    } catch (Exception e) {
      localException = e;
    }
    
    if (cnode!=null) {
      // **************************************************************************
      // **** There is already a node available, perform a update on the properties
      // **** --------------- MODIFY ------------------               
      boolean modified = false;
      CARS_Interface cars = null;
      Node           interfaceClass = null;
      if (updateNodeTags.getData( CARS_INTERFACE )!=null) {
        cars = (CARS_Interface)updateNodeTags.getData( CARS_INTERFACE );
        interfaceClass = (Node)updateNodeTags.getData( "InterfaceClass" );
      }

      final CARS_DefaultInterface di = new CARS_DefaultInterface();
      // **** Taglist for version parameters
      JD_Taglist versionTL = null;
      
      // **** Check for mixin types     
      Iterator it = pParamsTL.getIterator();
      String key, data;
      while( it.hasNext() ) {
        key = (String)it.next();
        if ("jcr:mixinTypes".equals( key )) {
          final String mixinType = (String)pParamsTL.getData( key );
          if (mixinType.startsWith( "-" )) {
            // **** The mixin type must removed
            cnode.removeMixin( mixinType.substring(1) );
          } else if (mixinType.startsWith( "+" )) {
            // **** The mixin type must removed
            cnode.addMixin( mixinType.substring(1) );
          } else {
            cnode.addMixin( mixinType );
          }
        }
      }
      
      it = pParamsTL.getIterator();
//      Iterator it = pParamsTL.getIterator();
//      String key, data;
      while( it.hasNext() ) {
        key = (String)it.next();
        if (isVersionParameter( key )) {
          if (versionTL==null) {
            versionTL = new JD_Taglist();
          }
          versionTL.putData( key, pParamsTL.getData( key ) );
        } else if (!isPOSTParameter( key )) {
          data = (String)pParamsTL.getData( key );
          if (cars==null) {
            di.setParamProperty( this, null, cnode, key, data );
          } else {
            cars.setParamProperty( this, interfaceClass, cnode, key, data );
          }
          modified = true;
        }
      }

      if (pBodyContentType==null) {
        pBodyContentType = (String)pParamsTL.getData( "jcr:mimeType" );
      }

      if (cars!=null) {
        if (cars.setBodyStream( this, interfaceClass, cnode, pBody, pBodyContentType )) {
          modified = true;
        }
      } else {
        // **** The binary data
        if (di.setBodyStream( this, null, cnode, pBody, pBodyContentType )) {
          modified = true;
        }
        /// ****** TODO Property HINT....
      }
      
      // **** Are there modifications?
      if (modified) {
        if (mayChangeNode( cnode )) {
          CARS_Utils.setCurrentModificationDate( cnode );
        }
      }
      
      // **********************
      // **** Version commands?
      if (versionTL!=null) {
        final String vcs    = (String)versionTL.getData( CARS_ActionContext.gDefVCS );
        final String vcscmd = (String)versionTL.getData( CARS_ActionContext.gDefVCSCmd );
        final CARS_VersionManager vm = CARS_ActionContext.getVersionManager( vcs );
        if ("checkin".equals(vcscmd)) {
          vm.checkin( this, cnode, (String)versionTL.getData( CARS_ActionContext.gDefVCSLabel ) );
        } else if ("checkout".equals( vcscmd )) {
          cnode = vm.checkout( this, cnode );
        } else if ("restore".equals( vcscmd )) {
          if (versionTL.getData( CARS_ActionContext.gDefVCSLabel )==null) {
            throw new Exception( "vcs-restore operation requires a vcs-label" );
          } else {
            cnode = vm.restore( this, cnode, (String)versionTL.getData( CARS_ActionContext.gDefVCSLabel ) );
          }
        } else if ("removeByLabel".equals( vcscmd )) {
          if (versionTL.getData( CARS_ActionContext.gDefVCSLabel )==null) {
            throw new Exception( "vcs-removeByLabel operation requires a vcs-label" );
          } else {
            vm.removeVersionByLabel( (String)versionTL.getData( CARS_ActionContext.gDefVCSLabel ), cnode);
          }
        }
      }

      // **** Save the changes
      cnode.save();

      // **** Check for permission effects
      if (cnode.isNodeType( "jecars:Principal" )) {
        // **** Clear the access cache
        CARS_Factory.getLastFactory().getAccessManager().clearPathCache();
        // **** Update also the modification date in the node, because it have effects for the viewable children objects
        final Node cnodeParent = cnode.getParent();
        try {
          CARS_Utils.setCurrentModificationDate( cnodeParent );
          cnodeParent.save();
        } catch( ItemNotFoundException ie ) {
          // **** Not available or no rights to set the property
        }
      }

    } else {
      // **** localException could be set by the getNode() call
      if (localException==null) {
        throw new PathNotFoundException( pFullPath );
      } else {
        throw new PathNotFoundException( pFullPath, localException );
      }
    }
    return cnode;
  }

  /** mayChangeNode
   * 
   * @param pNode
   * @return
   * @throws javax.jcr.RepositoryException
   */
  @Override
  public boolean mayChangeNode( final Node pNode ) throws RepositoryException {
    return !pNode.isNodeType( "jecars:Tool" );
  }
  
  /** getNodeDirect
   * Get Node without(!) using the interface nodes
   * 
   * @param pFullPath
   * @return
   * @throws RepositoryException 
   */
  @Override
  public Node getNodeDirect( final String pFullPath ) throws Exception {
    Node n;
    setCurrentViewNode( null );
    setCurrentViewProperty( null );
    n = mSession.getNode( pFullPath );
    setCurrentViewNode(n);
    return n;
  }

  
  /** Get the full node using all possible resolving options
   * @param pFullPath the path to be resolved
   * @param pTags taglist for storing parameters;
   *         "InterfaceClass" = ..
   *         "CARS_Interface" = ..
   * @param pAsHead
   * @return The node found
   * @throws java.lang.Exception
   */    
  @Override
  public Node getNode( final String pFullPath, final JD_Taglist pTags, final boolean pAsHead ) throws Exception {
    Node n;
    setCurrentViewNode( null );
    setCurrentViewProperty( null );
    Node rn = getRoot();
    final StringBuilder     appPath = new StringBuilder("");
    final StringTokenizer      stok = new StringTokenizer( pFullPath, "/" );
    final ArrayList<String>  fparts = new ArrayList<String>(12);
    final ArrayList<String>   parts = new ArrayList<String>(12);
    final StringBuilder       ppart = new StringBuilder(128);
    String part;
    while( stok.hasMoreTokens() ) {
      part = stok.nextToken();
//      part = URLDecoder.decode( part, "UTF-8" );
      part = CARS_ActionContext.untransportString( part );
      parts.add( part );
      ppart.append( part );
      fparts.add( ppart.toString() );
      ppart.append( '/' );
    }
    // **** Downsizing path
    int i;
    if (parts.isEmpty()) {
      // **** root access
      i = 0;
      return rn;
    } else {
      for( i=parts.size()-1; i>=0; i-- ) {
        try {
          getRoot().getNode( fparts.get(i) );
          break;
        } catch (Exception e) {
        }
      }
    }
    if (i!=-1) {
      // **** Upsizing the path
      CARS_Interface cars = null;
      i = getNodeWithInterface( fparts, i );
      rn = getRoot().getNode( fparts.get(i) );
      if ((i<(parts.size()-1)) || (rn.hasProperty( DEF_INTERFACECLASS ))) {
        Node interfaceNode = null;
        while( true ) {
          if (rn.hasProperty( DEF_INTERFACECLASS )) {
            interfaceNode = rn;
            if (pTags!=null) pTags.replaceData( "InterfaceClass", interfaceNode );
            // **** Is an application source
            final String clss = interfaceNode.getProperty( DEF_INTERFACECLASS ).getString();
            try {
              cars = (CARS_Interface)Class.forName( clss ).newInstance();
              if (pTags!=null) pTags.replaceData( CARS_INTERFACE, cars );
              appPath.append( '/' ).append( parts.get(i) );
              if (pAsHead) {
                cars.initHeadNodes( this, interfaceNode, rn, parts, i );
                cars.headNodes( this, interfaceNode, rn, appPath.toString() );
              } else {
                cars.initGetNodes( this, interfaceNode, rn, parts, i );
                cars.getNodes(     this, interfaceNode, rn, appPath.toString() );
              }
            } catch( ClassNotFoundException cnfe ) {
              if (pTags!=null) pTags.removeData( CARS_INTERFACE );
              LOG.log( Level.SEVERE, cnfe.getMessage(), cnfe );
            }
          } else if (cars!=null) {
            appPath.append( '/' ).append( parts.get(i) );
            if (pAsHead) {
              cars.headNodes( this, interfaceNode, rn, appPath.toString() );
            } else {
              cars.getNodes( this, interfaceNode, rn, appPath.toString() );
            }
          }
          i++;
          if (i>=parts.size()) break;
          if (rn.hasNode( parts.get(i))) {
            rn = rn.getNode( parts.get(i) );
          } else {
            // **** Check if property exists
            setCurrentViewProperty( rn.getProperty( parts.get(i) ) );
          }
        }
      }      
    } else {
      throw new PathNotFoundException( pFullPath );
    }
    n = rn;
    setCurrentViewNode(n);
    return n;
  }

  
  
  /** Remove Node from the repository
   * @param pFullPath the full path of the to be removed node
   * @param pTags
   * @throws Exception when an exception occurs
   */   
  @Override
  @SuppressWarnings("empty-statement")
  public void removeNode( final String pFullPath, final JD_Taglist pTags ) throws Exception {
    
    // **** Test if the node is available
    final JD_Taglist removeNodeTags = new JD_Taglist();
    Node cnode = null;
    try {
      cnode = getNode( pFullPath, removeNodeTags, false );
      // **** Remove first all children
      NodeIterator ni = cnode.getNodes();
      while( ni.hasNext() ) {
        removeNode( ni.nextNode().getPath(), pTags );
      }
    } catch (Exception e) {
    }
    
    if (cnode!=null) {

      CARS_Interface cars = null;
      Node           interfaceClass = null;
      if (removeNodeTags.getData( CARS_INTERFACE )!=null) {
        cars = (CARS_Interface)removeNodeTags.getData( CARS_INTERFACE );
        interfaceClass = (Node)removeNodeTags.getData( INTERFACECLASS );
      }

      synchronized( NODEMUTATION_LOCK ) {
        if (cars==null) {
          final CARS_DefaultInterface di = new CARS_DefaultInterface();
          di.removeNode( this, null, cnode, pTags );
        } else {
          cars.removeNode( this, interfaceClass, cnode, pTags );
        }
      }
      
    } else {
      throw new PathNotFoundException( pFullPath );
    }
    return;
  }
  
  
  /** Get users
   *
   */
  @Override
  final public Node getUsers() throws PathNotFoundException, RepositoryException {
//    return CARS_Factory.getSystemCarsSession().getRootNode().getNode( "JeCARS/default/Users" );
    return mSession.getNode( "/JeCARS/default/Users" );
  }
  
  
  /** Get user sources
   */
  public Node getUserSources() throws PathNotFoundException, RepositoryException {
    return mSession.getNode( "/JeCARS/UserSources" );
  }
  
}
