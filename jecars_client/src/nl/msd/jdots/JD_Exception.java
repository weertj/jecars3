/*
 * Copyright (c) 1996-2011 Maverick Software Development, 11/11 Software.
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


/*
 * JD_Exception.java
 *
 * Created on 15 februari 2005, 20:15
 */

package nl.msd.jdots;

/**
 * Exception class used for JD_Objects
 * @author weertj(Prog)
 */
public class JD_Exception extends Exception {
    
    /**
     * Creates a new instance of <code>JD_Exception</code> without detail message.
     */
    public JD_Exception() {
      super();
    }
    
    /**
     * Constructs an instance of <code>JD_Exception</code> with the specified throwable.
     * @param msg The throwable which caused the exception.
     */
    public JD_Exception( Throwable pCause ) {
      super( pCause );
      return;
    }
    
    /**
     * Constructs an instance of <code>JD_Exception</code> with the specified detail message.
     * @param msg the detail message.
     */
    public JD_Exception(String msg) {
        super(msg);
    }
}
