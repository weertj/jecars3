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

package org.jecars.client.observation;

import java.util.EnumSet;
import org.jecars.client.JC_Path;

/**
 *
 * @author weert
 */
public interface JC_ObservationManager {

  /** addEventListener
   *
   * @param pListener
   * @param pEventTypes
   * @param pAbsPath
   * @param pIsDeep
   * @param pCheckEverySecs
   */
  void addEventListener( final JC_EventListener pListener, final EnumSet<JC_Event.TYPE>pEventTypes, final JC_Path pAbsPath,
                         final boolean pIsDeep, final int pCheckEverySecs, final boolean pRemoveWhenInvalid );


  /** removeEventListener
   * 
   * @param pListener
   */
  void removeEventListener( final JC_EventListener pListener );
}
