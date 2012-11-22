/*
 * Copyright 2008 NLR - National Aerospace Laboratory
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
import java.util.LinkedList;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Defs;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Filter;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Params;
import org.jecars.client.JC_RESTComm;
import org.jecars.client.JC_Rights;

/**
 * JC_UsersNode
 *
 * @version $Id: JC_UsersNode.java,v 1.10 2009/03/24 15:31:06 weertj Exp $
 */
public class JC_UsersNode extends JC_DefaultNode {

    /** addUser
     * 
     * @param pUsername
     * @param pFullname
     * @param pPassword
     * @return
     * @throws org.jecars.client.JC_Exception
     */
  public JC_UserNode addUser( final String pUsername, final String pFullname, final char[] pPassword,
                              final Collection<String>pRights ) throws JC_Exception {
    final JC_UserNode user = (JC_UserNode)addNode( pUsername, "jecars:User" );
    if (pFullname!=null) {
      user.setProperty( "jecars:Fullname", pFullname );
    }
    user.setProperty( "jecars:Password_crypt", new String(pPassword) );
    user.save();
    if (pRights!=null) {
      final Collection<String> rights = new ArrayList<String>( pRights );
      final JC_DefaultNode permN = (JC_DefaultNode)user.getNode( "jecars:P_UserPermission" );
      final JC_PermissionNode perm = (JC_PermissionNode)permN.morphToNodeType();
      perm.setProperty( "jecars:Principal", "+" + user.getPath() );
      if (rights.contains( JC_Rights.R_DELEGATE )) {
        perm.setProperty( "jecars:Delegate", JC_Defs.TRUE );
      }
      rights.remove( JC_Rights.R_DELEGATE );
      perm.setMultiValueProperty( "jecars:Actions", rights );
      perm.save();
    }
    return user;
  }

    /** getUser
     * 
     * @param pUsername
     * @return
     * @throws org.jecars.client.JC_Exception
     */
  public JC_UserNode getUser( final String pUsername ) throws JC_Exception {
    final JC_DefaultNode n = (JC_DefaultNode)getNode( pUsername );
    return (JC_UserNode)n.morphToNodeType();
  }

  /** hasUser
   *
   * @param pUsername
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public boolean hasUser(final String pUsername) throws JC_Exception {
    return hasNode(pUsername);
  }

  /** getUsers
   *
   * @return
   * @throws JC_Exception
   */
  public Collection<JC_UserNode> getUsers() throws JC_Exception {
    final JC_Filter filter = JC_Filter.createFilter();
    filter.addCategory("jecars:User");
    final JC_Params params = getClient().createParams( JC_RESTComm.GET ).cloneParams();
    params.addOtherParameter( JC_Defs.PARAM_GETALLPROPS, JC_Defs.TRUE );
    params.setOutputFormat( JC_Defs.OUTPUTTYPE_PROPERTIES );
    final Collection<JC_Nodeable> nodes = getNodes( params, filter, null );
    final Collection<JC_UserNode> result = new LinkedList();
    if (nodes != null) {
      final JC_Nodeable[] children = nodes.toArray(new JC_Nodeable[0]);
      for (JC_Nodeable n : children) {
         final JC_DefaultNode dn = (JC_DefaultNode) n;
         result.add((JC_UserNode) dn.morphToNodeType());
      }
    }
    return result;
  }

    /** removeUser, will save the results
     * 
     * @param pNode
     * @throws org.jecars.client.JC_Exception
     */
  public void removeUser( final JC_UserNode pNode ) throws JC_Exception {
    pNode.removeNodeForced();
    pNode.save();
    return;
  }
   
  /** removeUser, will save the results
   * 
   * @param pUsername
   * @throws org.jecars.client.JC_Exception
   */
  public void removeUser( final String pUsername ) throws JC_Exception {
    final JC_DefaultNode n = (JC_DefaultNode) getNode(pUsername);
    final JC_UserNode gn = (JC_UserNode) n.morphToNodeType();
    removeUser(gn);
    return;
  }


}
