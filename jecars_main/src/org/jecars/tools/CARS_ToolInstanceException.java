/*
 * Copyright 2007-2008 NLR - National Aerospace Laboratory
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
package org.jecars.tools;

/** CARS_ToolInstanceException
 *
 * @version $Id: CARS_ToolInstanceException.java,v 1.2 2007/12/21 15:35:54 weertj Exp $
 */
public class CARS_ToolInstanceException extends java.lang.Exception {
  
  /**
   * Creates a new instance of <code>LPF_ToolInstanceException</code> without detail message.
   */
  public CARS_ToolInstanceException() {
  }
  
  public CARS_ToolInstanceException( Exception pE ) {
    super( pE );
  }
  
  /**
   * Constructs an instance of <code>LPF_ToolInstanceException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public CARS_ToolInstanceException(String msg) {
    super(msg);
  }
}
