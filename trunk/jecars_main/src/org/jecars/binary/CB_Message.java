/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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
package org.jecars.binary;

import java.util.ArrayList;
import java.util.List;
import nl.msd.jdots.JD_Taglist;

/**
 *
 * @author weert
 */
public class CB_Message {
  
  private final transient List<JD_Taglist> mCBMessages = new ArrayList<JD_Taglist>();
  
  public void addMessage( final ECB_Message pMessage, final String pData ) {
    mCBMessages.add( new JD_Taglist( pMessage, pData ) );
    return;
  }

}
