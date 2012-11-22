/*
 * Copyright 2008-2010 NLR - National Aerospace Laboratory
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

package org.jecars.client.nt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_HttpException;
import org.jecars.client.JC_MultiValueProperty;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Path;

/**
 * JC_GroupNode
 *
 * @version $Id: JC_GroupNode.java,v 1.9 2008/09/02 09:45:04 weertj Exp $
 */
public class JC_GroupNode extends JC_DefaultNode {

  static final String GROUPMEMBERS = "jecars:GroupMembers";

  private transient Collection<JC_Nodeable>mCachedMembers = null;

  /** clearCache
   * 
   */
  private final void clearCache() {
    if (mCachedMembers!=null) {
      mCachedMembers.clear();
      mCachedMembers = null;
    }
  }

  /** addUser
   * 
   * @param pUser
   * @throws org.jecars.client.JC_Exception
   */
  public void addUser( JC_Nodeable pUser ) throws JC_Exception {
    setProperty( GROUPMEMBERS, "+" + pUser.getPath() );
    clearCache();
    return;
  }

  /** removeUser
   * 
   * @param pUser
   * @throws org.jecars.client.JC_Exception
   */
  public void removeUser( JC_Nodeable pUser ) throws JC_Exception {
    setProperty( GROUPMEMBERS, "-" + pUser.getPath() );
    clearCache();
    return;
  }

  /** removeGroup
   * 
   * @param pGroup
   * @throws JC_Exception
   */
  public void removeGroup( JC_Nodeable pGroup ) throws JC_Exception {
    setProperty( GROUPMEMBERS, "-" + pGroup.getPath() );
    clearCache();
    return;
  }

  /** removeObject
   *
   * @param pObject
   * @throws JC_Exception
   */
  public void removeObject( JC_Nodeable pObject ) throws JC_Exception {
    setProperty( GROUPMEMBERS, "-" + pObject.getPath() );
    clearCache();
    return;
  }

  /** addGroup
   * 
   * @param pGroup
   * @throws org.jecars.client.JC_Exception
   */
  public void addGroup( JC_Nodeable pGroup ) throws JC_Exception {
    setProperty( GROUPMEMBERS, "+" + pGroup.getPath() );
    clearCache();
    return;
  }
  
  /** hasMembers
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public boolean hasMembers() throws JC_Exception {
    return hasProperty( GROUPMEMBERS );
  }
  
  /** getMembers
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public Collection<JC_Nodeable>getMembers() throws JC_Exception {
    if (mCachedMembers==null) {
      mCachedMembers = new ArrayList<JC_Nodeable>();
      final JC_MultiValueProperty mvp = (JC_MultiValueProperty)getProperty( GROUPMEMBERS );
      final JC_Clientable client = getClient();
      final JC_Nodeable root = client.getRootNode();
      final Collection<String>m = mvp.getValues();
      for (String member : m ) {
        if (root.hasNode(member.substring(1))) {
          mCachedMembers.add( root.getNode( member.substring(1) ) );
        }
      }
    }
    return mCachedMembers;
  }

  /** getErrorMembers
   *
   * @return
   * @throws JC_Exception
   */
  public Collection<JC_Nodeable>getErrorMembers() throws JC_Exception {
    final Collection<JC_Nodeable>members = new ArrayList<JC_Nodeable>();
    if (hasProperty( GROUPMEMBERS )) {
      final JC_MultiValueProperty mvp = (JC_MultiValueProperty)getProperty( GROUPMEMBERS );
      final JC_Clientable client = getClient();
      final JC_Nodeable root = client.getRootNode();
      final Collection<String>m = mvp.getValues();
      for (String member : m ) {
        try {
          if (!root.hasNode(member.substring(1))) {
            members.add( root.getNode( member.substring(1), false ) );
          }
        } catch( JC_Exception je ) {
          members.add( root.getNode( member.substring(1), false ) );
        }
      }
    }
    return members;
  }


  /** getUsers
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public Collection<JC_UserNode>getUsers() throws JC_Exception {
    final Collection<String> types = new ArrayList<String>();
    types.add( "jecars:User" );
    return (Collection<JC_UserNode>)getMembersByType( types, true );
  }

  /** getGroups
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public Collection<JC_GroupNode>getGroups() throws JC_Exception {
    Collection<String> types = new ArrayList<String>();
    types.add( "jecars:Group" );
    return (Collection<JC_GroupNode>)getMembersByType( types, true );
  }
  
  /** getMembersByType
   * 
   * @param pTypes
   * @param pMorphed
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public Collection<? extends JC_Nodeable>getMembersByType( final Collection<String>pTypes, final boolean pMorphed ) throws JC_Exception {
    Collection<JC_Nodeable>members = new ArrayList<JC_Nodeable>();
    if (hasProperty( GROUPMEMBERS )) {
      JC_MultiValueProperty mvp = (JC_MultiValueProperty)getProperty( GROUPMEMBERS );
      JC_Clientable client = getClient();
      Collection<String>m = mvp.getValues();
      JC_Nodeable mn;
      for (String member : m ) {
        try {
          mn = client.getNode( member );
          if (pTypes.contains( mn.getNodeType() )) {
            if (pMorphed==true) {
              members.add( mn.morphToNodeType() );
            } else {
              members.add( mn );
            }
          }
        } catch( JC_HttpException he ) {
          gLog.log( Level.WARNING, he.getMessage(), he );
        }
      }
    }
    return members;
  }

  /** isMember
   * 
   * @param pNodePath
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public boolean isMember( String pNodePath ) throws JC_Exception {
    if (hasMembers()) {
      JC_MultiValueProperty mvp = (JC_MultiValueProperty)getProperty( GROUPMEMBERS );
      return mvp.getValues().contains( pNodePath );
    } else {
      return false;
    }
  }
  
  
  /** isMember
   * 
   * @param pNode
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public boolean isMember( JC_Nodeable pNode ) throws JC_Exception {
    if (hasMembers()) {
      Collection<JC_Nodeable>members = getMembers();
      return members.contains( pNode );
    } else {
      return false;
    }
  }
  
  
  public String getGroupname() throws JC_Exception {
    return getName();
  }

  public String getFullname() throws JC_Exception {
    return getProperty("jecars:Fullname").getValueString();
  }

}
