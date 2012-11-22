/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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
package org.jecars.jaas;

import java.security.Principal;

/**
 * CARS_Principal
 *
 * @version $Id: CARS_Principal.java,v 1.1 2007/09/26 14:14:37 weertj Exp $
 */
public class CARS_Principal implements Principal {
  
  private String mName = null;
    
  /** Creates a new instance of CARS_Principal */
  public CARS_Principal( String pName ) {
    mName = pName;
    return;
  }

  @Override
  public String getName() {
    return mName;    
  }
    
}
