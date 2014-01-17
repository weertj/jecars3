/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jecars;

import javax.jcr.Node;

/**
 *
 * @author weert
 */
public class CARS_Definitions {

  private CARS_Definitions() {
  }

  static public final String P_GETPROPERTY = "get_property";
  static public final String P_SETPROPERTY = "set_property";
  static public final String P_READ        = "read";
  static public final String P_ADDNODE     = "add_node";
  static public final String P_REMOVE      = "remove";
    
  final static public String VERSION_ID    = "v3.2.2";
  final static public String PRODUCTNAME   = "JeCARS 'Elderberry'";
  final static public String VERSION       = PRODUCTNAME + " " + VERSION_ID;
  final static public String MAINFOLDER    = "JeCARS";
  final static public String DEFAULTNS1    = "jecars";
  final static public String DEFAULTNS     = "jecars:";
  final static public String DEFAULTAUTHOR = "jecars.org";
  final static public String DEFAULTRIGHTS = "(C) 2006-2014";

    
  static public final String ACCOUNTKEYSPATH    = "accounts/ClientLogin";
  static public final String gUsersPath         = MAINFOLDER + "/default/Users";
  static public final String gGroupsPath        = MAINFOLDER + "/default/Groups";
  static public final String gSuperuserName     = "Superuser";
  static public final String gActions           = DEFAULTNS + "Actions";
  static public final String gPrincipal         = DEFAULTNS + "Principal";
  static public final String gPrincipalSQL      = " OR " + gPrincipal + "='";
  static public final String gSelectPermission  = "SELECT * FROM jecars:Permission WHERE (jecars:Principal='";
  static public final String gUSERNAME_GRANTALL = "Administrator";
  static public final String gSelectGroups      = "SELECT * FROM jecars:groupable WHERE jecars:GroupMembers='";
  static public final String gPasswordProperty  = DEFAULTNS + "Password_crypt";

      
  static public final Node[] _NODE = new Node[0];

  static public final int X_NOTCLEARCACHE = 1024;

  static private String gRealm = "";
  static private String gCurrentFullContext = "";
  
  /** getCurrentFullContext
   * 
   * @return 
   */
  static public String getCurrentFullContext() {
    return gCurrentFullContext;
  }
  
  /** setCurrentFullContext
   * 
   * @param pContext 
   */
  static public void setCurrentFullContext( final String pContext ) {
    gCurrentFullContext = pContext;
    return;
  }
  
  /** setRealm
   * 
   * @param pRealm 
   */
  static public void setRealm( final String pRealm ) {
    gRealm = pRealm;
    return;
  }
  
  /** getRealm
   * 
   * @return 
   */
  static public String getRealm() {
    return gRealm;
  }
  
}
