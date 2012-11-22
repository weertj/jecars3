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
package org.jecars.support;

import java.util.*;

import javax.jcr.*;

/**
 * CARS_DefaultPropertyIterator
 *
 * @version $Id: CARS_DefaultPropertyIterator.java,v 1.1 2007/09/26 14:19:06 weertj Exp $
 */
public class CARS_DefaultPropertyIterator extends CARS_DefaultRangeIterator implements PropertyIterator {
    
    public Property nextProperty() {
      try {
        return (Property)next();
      } catch( ArrayIndexOutOfBoundsException ae ) {
        throw new NoSuchElementException();
      }
    }    
    
}
