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

package org.jecars.client;

/**
 * JC_RootNode
 *
 * @version $Id: JC_RootNode.java,v 1.1 2008/05/06 08:19:49 weertj Exp $
 */
public class JC_RootNode extends JC_DefaultNode {

  private JC_Clientable mClient = null;       
  
  /** setClient
   * @param pClient
   */
  public void setClient( final JC_Clientable pClient ) {
    mClient = pClient;
    return;
  }
  
  /** getClient
   * @return
   */
  @Override
  public JC_Clientable getClient() {
    return mClient;
  }
  
}
