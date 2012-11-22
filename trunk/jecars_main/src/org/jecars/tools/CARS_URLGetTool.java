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
package org.jecars.tools;

import java.io.InputStream;
import java.util.Collection;
import nl.msd.jdots.JD_Taglist;

/**
 *
    [jecars:Tool] > jecars:dataresource
    - jecars:ToolClass      (String)
    - jecars:StateRequest   (String) < '(start|suspend|stop)'
    - jecars:State          (String)='open.notrunning' mandatory autocreated
    - jecars:PercCompleted  (Double)='0'
    + jecars:Parameter      (jecars:dataresource) multiple
    + jecars:Input          (jecars:dataresource) multiple
    + jecars:Output         (jecars:dataresource) multiple

 * @version $Id: CARS_URLGetTool.java,v 1.3 2008/02/06 12:36:34 weertj Exp $
 */
public class CARS_URLGetTool extends CARS_DefaultToolInterface {

    
  /** Get the output as objects in the collection as pObjectClass type
   * @param pObjectClass the class type of the resulting outputs
   * @param pParams optional taglist contains parameter for generating the output
   * @return Collection of object of class type pObjectClass or empty or null.
   * @throws Exception when an error occurs
   */
  @Override
  public Collection<?> getOutputsAsObject( Class pObjectClass, JD_Taglist pParamsTL ) throws Exception {
    if (pObjectClass.equals( InputStream.class )) {
      Collection<InputStream> inputs = (Collection<InputStream>)getInputsAsObject( InputStream.class, pParamsTL );
      return inputs;
    }
    return null;
  }
  
  protected void toolRun() throws Exception {
    
    return;
  }
  
}
