/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

/** JC_InfoApp
 *
 *
 */
public class JC_InfoApp {

  private final JC_Clientable mClient;

  /** JC_InfoApp
   *
   * @param pClient
   */
  public JC_InfoApp( final JC_Clientable pClient ) {
    mClient = pClient;
    return;
  }

  /** whoAmI
   *
   * @return
   * @throws JC_Exception
   */
  public String whoAmI() throws JC_Exception {
    final JC_Nodeable whoAmI = mClient.getRootNode().getNode( "JeCARS/ApplicationSources/InfoApp/WhoAmI" );
    whoAmI.refresh();
    return whoAmI.getProperty( "jecars:Username" ).getValueString();
  }

}
