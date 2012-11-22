/*
 * Copyright 2008-2012 NLR - National Aerospace Laboratory
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


/**
 * JC_Itemable
 *
 * @version $Id: JC_Itemable.java,v 1.1 2008/05/06 08:19:50 weertj Exp $
 */
public interface JC_Itemable {
  
  boolean isChanged();
  boolean isRemoved();
  boolean isNew();
  void setChanged(boolean isChanged);
  void setRemoved(boolean isRemoved);
  void setNew(boolean isNew);
    
  /** getName
   * @return
   */
  String getName();

  /** setName
   * @param pName
   */
//  void setName( final String pName );

  /** save
   */
  void save() throws JC_Exception;

  /** save
   */
  void save( final JC_Params pParams ) throws JC_Exception;
  
  /** refresh
   * 
   */
  void refresh() throws JC_Exception;
  
}
