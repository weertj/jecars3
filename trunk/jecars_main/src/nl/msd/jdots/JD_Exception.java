/*
 * Copyright (c) 1996-2007 Maverick Software Development, 11/11 Software.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc.,
 * 51 Franklin St,
 * Fifth Floor,
 * Boston, MA  02110-1301 
 * USA
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
