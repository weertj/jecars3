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
package org.jecars.apps;

import org.jecars.CARS_Definitions;
import org.jecars.CARS_Main;

/**
 * CARS_SharedApp
 *
 */
public class CARS_SharedApp extends CARS_DefaultInterface implements CARS_Interface {
    
  /** Creates a new instance of CARS_SharedApp
   */
  public CARS_SharedApp() {
  }

  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": " + CARS_Definitions.PRODUCTNAME + " version=" + CARS_Definitions.VERSION_ID + " CARS_SharedApp";
  }

  
}
