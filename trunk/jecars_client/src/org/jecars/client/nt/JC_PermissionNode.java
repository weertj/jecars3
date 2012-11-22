/*
 * Copyright 2008-2009 NLR - National Aerospace Laboratory
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
import java.util.List;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Defs;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Rights;

/**
 * JC_Permission
 *
 * @version $Id: JC_PermissionNode.java,v 1.8 2008/09/08 13:10:43 weertj Exp $
 */

public class JC_PermissionNode extends JC_DefaultNode {

  /** Replaced by JC_Rights.xxx */
  @Deprecated
  final static public String R_READ         = "read";
  /** Replaced by JC_Rights.xxx */
  @Deprecated
  final static public String R_ADDNODE      = "add_node";
  /** Replaced by JC_Rights.xxx */
  @Deprecated
  final static public String R_GETPROPERTY  = "get_property";
  /** Replaced by JC_Rights.xxx */
  @Deprecated
  final static public String R_SETPROPERTY  = "set_property";
  /** Replaced by JC_Rights.xxx */
  @Deprecated
  final static public String R_REMOVE       = "remove";
  /** Replaced by JC_Rights.xxx */
  @Deprecated
  final static public String R_DELEGATE     = "delegate";

  final static public Collection<String> RS_ALLRIGHTS = new ArrayList<String>();
  final static public Collection<String> RS_ALLNODERIGHTS = new ArrayList<String>();

  final static public Collection<String> RS_FOLDERACCESS    = new ArrayList<String>();
  final static public Collection<String> RS_READACCESS      = new ArrayList<String>();
  final static public Collection<String> RS_READWRITEACCESS = new ArrayList<String>();
  final static public Collection<String> RS_ALLREADACCESS   = new ArrayList<String>();

  final static public Collection<String> RS_READCREATEACCESS    = new ArrayList<String>();
  final static public Collection<String> RS_ALLREADCREATEACCESS = new ArrayList<String>();
  
  static {
    RS_ALLRIGHTS.add( JC_Rights.R_READ );
    RS_ALLRIGHTS.add( JC_Rights.R_ADDNODE );
    RS_ALLRIGHTS.add( JC_Rights.R_GETPROPERTY );
    RS_ALLRIGHTS.add( JC_Rights.R_SETPROPERTY );
    RS_ALLRIGHTS.add( JC_Rights.R_REMOVE );
    RS_ALLRIGHTS.add( JC_Rights.R_DELEGATE );

    RS_ALLNODERIGHTS.add( JC_Rights.R_READ );
    RS_ALLNODERIGHTS.add( JC_Rights.R_ADDNODE );
    RS_ALLNODERIGHTS.add( JC_Rights.R_GETPROPERTY );
    RS_ALLNODERIGHTS.add( JC_Rights.R_SETPROPERTY );
    RS_ALLNODERIGHTS.add( JC_Rights.R_REMOVE );
    
    RS_FOLDERACCESS.add( JC_Rights.R_READ );

    RS_READACCESS.add( JC_Rights.R_READ );
    RS_READACCESS.add( JC_Rights.R_GETPROPERTY );

    RS_READWRITEACCESS.add( JC_Rights.R_READ );
    RS_READWRITEACCESS.add( JC_Rights.R_GETPROPERTY );
    RS_READWRITEACCESS.add( JC_Rights.R_SETPROPERTY );

    RS_ALLREADACCESS.add( JC_Rights.R_READ );
    RS_ALLREADACCESS.add( JC_Rights.R_GETPROPERTY );
    RS_ALLREADACCESS.add( JC_Rights.R_DELEGATE );

    RS_READCREATEACCESS.add( JC_Rights.R_READ );
    RS_READCREATEACCESS.add( JC_Rights.R_GETPROPERTY );
    RS_READCREATEACCESS.add( JC_Rights.R_SETPROPERTY );
    RS_READCREATEACCESS.add( JC_Rights.R_ADDNODE );

    RS_ALLREADCREATEACCESS.add( JC_Rights.R_READ );
    RS_ALLREADCREATEACCESS.add( JC_Rights.R_GETPROPERTY );
    RS_ALLREADCREATEACCESS.add( JC_Rights.R_SETPROPERTY );
    RS_ALLREADCREATEACCESS.add( JC_Rights.R_ADDNODE );
    RS_ALLREADCREATEACCESS.add( JC_Rights.R_DELEGATE );
  }
  

  /** addRights
   * 
   * @param pPrincipal
   * @param pRights
   * @throws org.jecars.client.JC_Exception
   */
  public void addRights( final JC_Nodeable pPrincipal, final Collection<String>pRights ) throws JC_Exception {
    addRights( this, pPrincipal, pRights );
    return;
  }

  /** setRights - this method only sets the rights when there are existing ones (replace)
   *
   * @param pRights
   * @throws JC_Exception
   */
  public void setRights( final Collection<String>pRights ) throws JC_Exception {
    if (pRights!=null) {
      if (pRights.contains( JC_Rights.R_DELEGATE )) {
        setProperty( "jecars:Delegate", JC_Defs.TRUE );
      } else {
        setProperty( "jecars:Delegate", JC_Defs.FALSE );
      }
      final List<String> rights = new ArrayList<String>( pRights );
      rights.remove( JC_Rights.R_DELEGATE );
      replaceMultiValueProperty( "jecars:Actions", rights );
    }
    return;
  }

  /** addRights
   * 
   * @param pNode
   * @param pPrincipal
   * @param pRights
   * @throws org.jecars.client.JC_Exception
   */
  static public void addRights( final JC_Nodeable pNode, final JC_Nodeable pPrincipal, final Collection<String>pRights ) throws JC_Exception {
    if (pPrincipal!=null) {
      pNode.setProperty( "jecars:Principal", "+" + pPrincipal.getPath() );
    }
    if (pRights!=null) {
      String rs = "+";
      for (String r : pRights) {
        if (r.equals( JC_Rights.R_DELEGATE )) {
          pNode.setProperty( "jecars:Delegate", JC_Defs.TRUE );
        } else {
          if (!rs.equals( "+" )) rs += ",";
          rs += r;
        }
      }
      pNode.setProperty( "jecars:Actions", rs );
    }
    return;
  }

  /** addMixinRights
   *
   * @param pNode
   * @param pPrincipal
   * @param pRights
   * @throws JC_Exception
   */
  static public void addMixinRights( final JC_Nodeable pNode, final JC_Nodeable pPrincipal, final Collection<String>pRights ) throws JC_Exception {
    pNode.addMixin( "jecars:permissionable" );
    if (pPrincipal!=null) {
      pNode.setProperty( "jecars:Principal", "+" + pPrincipal.getPath() );
    }
    if (pRights!=null) {
      String rs = "+";
      for (String r : pRights) {
        if (r.equals( JC_Rights.R_DELEGATE )) {
          pNode.setProperty( "jecars:Delegate", JC_Defs.TRUE );
        } else {
          if (!rs.equals( "+")) {
            rs += ",";
          }
          rs += r;
        }
      }
      pNode.setProperty( "jecars:Actions", rs );
    }
    return;
  }



}
