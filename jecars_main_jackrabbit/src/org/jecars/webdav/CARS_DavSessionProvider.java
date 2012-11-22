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

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Factory;

/** CARS_DavSessionProvider
 *
 * @version $Id: CARS_DavSessionProvider.java,v 1.1 2009/03/10 15:09:20 weertj Exp $
 */
public class CARS_DavSessionProvider implements DavSessionProvider {

  @Override
  public boolean attachSession( WebdavRequest pWr ) throws DavException {
    return attachSession( pWr, null, null );
  }

  /** attachSession
   *
   * @param pWr
   * @param pAC
   * @param pFact
   * @return
   * @throws org.apache.jackrabbit.webdav.DavException
   */
  public boolean attachSession( final WebdavRequest pWr, final CARS_ActionContext pAC, final CARS_Factory pFact ) throws DavException {
    final CARS_DavSession cds = new CARS_DavSession( pAC.getMain().getSession() );
    cds.setFactory( CARS_Factory.getLastFactory() );
    cds.setActionContext( pAC );
    pWr.setDavSession( cds );
    return true;
  }

  @Override
  public void releaseSession( WebdavRequest pWr ) {
//    System.out.println("releaseSession: " + pWr );
    return;
  }

}
