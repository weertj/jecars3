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

package org.jecars.client;

import java.util.ArrayList;
import java.util.List;

/**
 * JC_Rights
 *
 * @version $Id: JC_Rights.java,v 1.2 2008/11/13 22:26:08 weertj Exp $
 */
public class JC_Rights {

  final static public String R_READ         = "read";
  final static public String R_ADDNODE      = "add_node";
  final static public String R_GETPROPERTY  = "get_property";
  final static public String R_SETPROPERTY  = "set_property";
  final static public String R_REMOVE       = "remove";
  final static public String R_DELEGATE     = "delegate";
  final static public String R_ACL_READ     = "acl_read";
  final static public String R_ACL_EDIT     = "acl_edit";

  private final List<String>mRights = new ArrayList<String>();
  
  /** addRight
   * 
   * @param pRight
   */
  public void addRight( final String pRight ) {
    // **** The pRight line can be multiline
    if (pRight.indexOf('\n')==-1) {
      if (!hasRight( pRight )) {
        mRights.add( pRight );
      }
    } else {
      final String rights[] = pRight.split( "\n" );
      for( final String r : rights ) {
        if (!hasRight( r )) {
          mRights.add( r );
        }
      }
    }
    return;
  }

  /** hasRight
   * 
   * @param pRight
   * @return
   */
  public boolean hasRight( final String pRight ) {
    return mRights.contains( pRight );
  }

  /** toString
   *
   * @return
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder( "Rights: " );
    for (String right : mRights) {
      sb.append( right ).append( ',' );
    }
    return sb.toString();
  }

}
