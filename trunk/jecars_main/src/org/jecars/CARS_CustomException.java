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
package org.jecars;

/**
 * CARS_CustomException
 *
 * @version $Id: CARS_CustomException.java,v 1.2 2009/04/14 15:16:03 weertj Exp $
 */
public class CARS_CustomException extends Exception {

  public CARS_CustomException( String pName ) {
    super( pName );
  }

  public CARS_CustomException( Throwable pE ) {
    super( pE );
  }
    
}
