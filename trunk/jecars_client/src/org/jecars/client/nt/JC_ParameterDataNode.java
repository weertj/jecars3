/*
 * Copyright 2009 NLR - National Aerospace Laboratory
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
package org.jecars.client.nt;

import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;

/** JC_ParameterResourceNode
 *
 * @version $Id: JC_ParameterDataNode.java,v 1.1 2009/05/06 14:11:45 weertj Exp $
 */
public class JC_ParameterDataNode extends JC_DefaultNode {

  /**
   * 
   * @param pValue
   * @throws org.jecars.client.JC_Exception
   */
  public JC_ParameterDataNode addParameter( final String pValue ) throws JC_Exception {
    setProperty( "jecars:string", pValue );
    save();
    return this;
  }

  public String getStringParameter() throws JC_Exception {
    return getProperty( "jecars:string" ).getValueString();
  }
  
}
