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

import java.io.IOException;
import java.io.InputStream;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Streamable;
import org.jecars.client.JC_Utils;

/**
 *
 * @author weert
 */
public class JC_datafileNode extends JC_DefaultNode {

  public String getContentsAsString() throws JC_Exception, IOException {
    final JC_Streamable stream = getClient().getNodeAsStream( getPath() );
    final InputStream is = stream.getStream();
    try {
      return JC_Utils.readAsString( is );
    } finally {
      is.close();
      stream.destroy();
    }
  }
    
}
