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
 * JD_ObjectIterator.java
 *
 * Created on 15 februari 2005, 20:06
 */

package nl.msd.jdots;

/** Interface to define a JDots object iterator
 * @author weertj
 */
public interface JD_ObjectIterator extends JD_RangeIterator {

  public JD_ObjectIterator cloneIterator() throws CloneNotSupportedException;
  public JD_Objectable nextObject();
}
