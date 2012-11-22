/*
 * Copyright 2009 NLR - National Aerospace Laboratory
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
package org.jecars.webdav;

import javax.jcr.Session;
import org.apache.jackrabbit.webdav.simple.DavSessionImpl;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Factory;

/** CARS_DavSession
 *
 *  @version $Id: CARS_DavSession.java,v 1.1 2009/03/10 15:09:20 weertj Exp $
 */
public class CARS_DavSession extends DavSessionImpl {

  private CARS_ActionContext mAC      = null;
  private CARS_Factory       mFactory = null;

  public CARS_DavSession( final Session ses ) {
    super(ses);
    return;
  }

  /** setActionContext
   * 
   * @param pAC
   */
  public void setActionContext( final CARS_ActionContext pAC ) {
    mAC = pAC;
    return;
  }

  /** getActionContext
   * 
   * @return
   */
  public CARS_ActionContext getActionContext() {
    return mAC;
  }

  /** setFactory
   * 
   * @param pFactory
   */
  public void setFactory( final CARS_Factory pFactory ) {
    mFactory = pFactory;
    return;
  }

  /** getFactory
   *
   * @return
   */
  public CARS_Factory getFactory() {
    return mFactory;
  }


/*
  private SessionImpl mSession = null;

  public void setSession( SessionImpl pSession ) {
    mSession = pSession;
    return;
  }

  public SessionImpl getSession() {
    return mSession;
  }
*/
}
