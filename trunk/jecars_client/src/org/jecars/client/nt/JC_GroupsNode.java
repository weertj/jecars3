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

import java.util.Collection;
import java.util.LinkedList;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Filter;
import org.jecars.client.JC_Nodeable;

/**
 * JC_GroupsNode
 *
 * @version $Id: JC_GroupsNode.java,v 1.5 2009/02/18 15:27:23 weertj Exp $
 */
public class JC_GroupsNode extends JC_DefaultNode {

  /** hasGroup
   *
   * @param pGroupname
   * @return
   * @throws JC_Exception
   */
  public boolean hasGroup(final String pGroupname) throws JC_Exception {
    return hasNode(pGroupname);
  }

    /** addGroup
     * 
     * @param pGroupname
     * @param pFullname
     * @return
     * @throws org.jecars.client.JC_Exception
     */
  public JC_GroupNode addGroup( final String pGroupname ) throws JC_Exception {
    final JC_Nodeable n = addNode( pGroupname, "jecars:Group" );
    return (JC_GroupNode)n.morphToNodeType();
  }

    /** addGroup
     * 
     * @param pGroupname
     * @param pTitle
     * @return
     * @throws org.jecars.client.JC_Exception
     */
  public JC_GroupNode addGroup( final String pGroupname, final String pTitle ) throws JC_Exception {
    final JC_GroupNode group = addGroup( pGroupname );
    group.setProperty( "jecars:Title", pTitle );
    return group;
  }
  
    /** getGroup
     * 
     * @param pUsername
     * @return
     * @throws org.jecars.client.JC_Exception
     */
  public JC_GroupNode getGroup( final String pGroupname ) throws JC_Exception {
    final JC_DefaultNode n = (JC_DefaultNode)getNode( pGroupname );
    return (JC_GroupNode)n.morphToNodeType();
  }

  /** getGroups
   *
   * @return
   * @throws JC_Exception
   */
  public Collection<JC_GroupNode> getGroups() throws JC_Exception {
    final JC_Filter filter = JC_Filter.createFilter();
    filter.addCategory("jecars:Group");
    final Collection<JC_Nodeable> nodes = getNodes(null, filter, null);
    final Collection<JC_GroupNode> result = new LinkedList();
    if (nodes != null) {
      final JC_Nodeable[] children = nodes.toArray(new JC_Nodeable[0]);
      for (JC_Nodeable n : children) {
        final JC_DefaultNode dn = (JC_DefaultNode) n;
        result.add((JC_GroupNode) dn.morphToNodeType());
      }
    }
    return result;
  }

  /** removeUser
   * 
   * @param pNode
   * @throws org.jecars.client.JC_Exception
   */
  public void removeGroup( final JC_GroupNode pNode ) throws JC_Exception {
    pNode.removeNodeForced();
    pNode.save();
    return;
  }

  /** removeGroup
   *
   * @param pGroupname
   * @throws org.jecars.client.JC_Exception
   */
  public void removeGroup( final String pGroupname ) throws JC_Exception {
    final JC_DefaultNode n = (JC_DefaultNode)getNode(pGroupname);
    final JC_GroupNode gn = (JC_GroupNode)n.morphToNodeType();
    removeGroup(gn);
    return;
  }

}
