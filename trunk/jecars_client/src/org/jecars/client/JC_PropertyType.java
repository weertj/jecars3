/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

package org.jecars.client;

/** JC_PropertyType
 *
 */
public class JC_PropertyType {

  public static final int TYPE_STRING    = 1;
  public static final int TYPE_BINARY    = 2;
  public static final int TYPE_LONG      = 3;
  public static final int TYPE_DOUBLE    = 4;
  public static final int TYPE_DATE      = 5;
  public static final int TYPE_BOOLEAN   = 6;
  public static final int TYPE_NAME      = 7;
  public static final int TYPE_PATH      = 8;
  public static final int TYPE_REFERENCE = 9;
  public static final int TYPE_WEAKREFERENCE = 10;
  public static final int TYPE_URI       = 11;
  public static final int TYPE_DECIMAL   = 12;
  public static final int TYPE_UNDEFINED = 0;
    
  public static final String TYPENAME_STRING = "String";
  public static final String TYPENAME_BINARY = "Binary";
  public static final String TYPENAME_LONG = "Long";
  public static final String TYPENAME_DOUBLE = "Double";
  public static final String TYPENAME_DATE = "Date";
  public static final String TYPENAME_BOOLEAN = "Boolean";
  public static final String TYPENAME_NAME = "Name";
  public static final String TYPENAME_PATH = "Path";
  public static final String TYPENAME_REFERENCE = "Reference";
  public static final String TYPENAME_WEAKREFERENCE = "WeakReference";
  public static final String TYPENAME_URI = "URI";
  public static final String TYPENAME_UNDEFINED = "undefined";

  private int mType = TYPE_UNDEFINED;

  /** setType
   *
   * @param pType
   */
  public void setType( final int pType ) {
    mType = pType;
    return;
  }

  /** getType
   *
   * @return
   */
  public int getType() {
    return mType;
  }

  /** nameFromValue
   *
   * @param pType
   * @return
   */
  public static String nameFromValue( final int pType ) {
    switch (pType) {
        case TYPE_STRING:            return TYPENAME_STRING;
        case TYPE_BINARY:            return TYPENAME_BINARY;
        case TYPE_BOOLEAN:           return TYPENAME_BOOLEAN;
        case TYPE_LONG:              return TYPENAME_LONG;
        case TYPE_DOUBLE:            return TYPENAME_DOUBLE;
        case TYPE_DATE:              return TYPENAME_DATE;
        case TYPE_NAME:              return TYPENAME_NAME;
        case TYPE_PATH:              return TYPENAME_PATH;
        case TYPE_REFERENCE:         return TYPENAME_REFERENCE;
        case TYPE_WEAKREFERENCE:     return TYPENAME_WEAKREFERENCE;
        case TYPE_URI:               return TYPENAME_URI;
    }
    return TYPENAME_UNDEFINED;
  }

  /** valueFromName
   *
   * @param pName
   * @return
   */
  public static int valueFromName( final String pName ) {
    if (pName.equals(TYPENAME_STRING))    {   return TYPE_STRING; }
    if (pName.equals(TYPENAME_BINARY))    {   return TYPE_BINARY; }
    if (pName.equals(TYPENAME_BOOLEAN))   {   return TYPE_BOOLEAN; }
    if (pName.equals(TYPENAME_LONG))      {   return TYPE_LONG; }
    if (pName.equals(TYPENAME_DOUBLE))    {   return TYPE_DOUBLE; }
    if (pName.equals(TYPENAME_DATE))      {   return TYPE_DATE; }
    if (pName.equals(TYPENAME_NAME))      {   return TYPE_NAME; }
    if (pName.equals(TYPENAME_PATH))      {   return TYPE_PATH; }
    if (pName.equals(TYPENAME_REFERENCE)) {   return TYPE_REFERENCE; }
    if (pName.equals(TYPENAME_WEAKREFERENCE)) {   return TYPE_WEAKREFERENCE; }
    if (pName.equals(TYPENAME_URI))       {   return TYPE_URI; }
    return TYPE_UNDEFINED;
  }

  
  
}
