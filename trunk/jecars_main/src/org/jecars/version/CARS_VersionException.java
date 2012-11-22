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
package org.jecars.version;

/**
 * CARS_VersionException
 * 
 * @version $Id: CARS_VersionException.java,v 1.1 2007/10/19 14:31:50 weertj Exp $
 */
public class CARS_VersionException extends Exception {

  /**
   * Creates a new instance of <code>CARS_VersionException</code> without detail message.
   */
  public CARS_VersionException() {
  }

  /**
   * Constructs an instance of <code>CARS_VersionException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public CARS_VersionException(String msg) {
    super(msg);
  }
  
  /**
   * Constructs an instance of <code>CARS_VersionException</code> with the specified exception.
   * @param e the exception.
   */
  public CARS_VersionException( Exception e ) {
    super(e);
  }

}
