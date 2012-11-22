/*
 * Copyright 2011 NLR - National Aerospace Laboratory
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

/**
 *
 * @author weert
 */
public class JC_WorkflowTaskPortNode extends JC_DefaultNode {

//  public long getSequenceNumber() throws JC_Exception {
//    return getProperty( "jecars:sequencenumber" ).getValueAsLong();
//  }
  
  public String getNodeName() throws JC_Exception {
    return getProperty( "jecars:nodename" ).getValueString();    
  }
    
  public String getHumanReadableName() throws JC_Exception {
    if (hasProperty( "jecars:Title" )) {
      return getProperty( "jecars:Title" ).getValueString();
    }
    return getName();
  }

    
}
