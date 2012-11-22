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

import java.io.IOException;
import java.io.ObjectInputStream;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Propertyable;
import org.jecars.client.JC_Streamable;

/** JC_ToolEventNode (Tracker ID: 2612650)
 *
 * @version $Id: JC_ToolEventNode.java,v 1.4 2009/03/19 16:16:59 weertj Exp $
 */
public class JC_ToolEventNode extends JC_DefaultNode {

  /** getLevel
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public long getLevel() throws JC_Exception {
    return (Long)getProperty( "jecars:Level" ).getValueAs( Long.class );
  }

  /** getBlocking
   * 
   * @return
   */
  public boolean getBlocking() {
    try {
      return (Boolean)getProperty( "jecars:Blocking" ).getValueAs( Boolean.class );
    } catch( JC_Exception e ) {
      return false;
    }
  }

  /** getType
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public String getType() throws JC_Exception {
    return getProperty( "jecars:Type" ).getValueString();
  }

  /** getDoubleValue
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public Double getDoubleValue() throws JC_Exception {
    try {
      return (Double)getProperty( "jecars:DValue" ).getValueAs( Double.class );
    } catch( JC_Exception je ) {
    }
    return null;
  }

  /** getException
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   * @throws java.io.IOException
   * @throws java.lang.ClassNotFoundException
   */
  public Throwable getException() throws JC_Exception, IOException, ClassNotFoundException {
    if (hasProperty( "jecars:Exception" )) {
      final JC_Propertyable p = getProperty( "jecars:Body" );  // **** Get the body property, which has the textual representation
      final Exception toolException = new Exception( p.getValueString() );
      return toolException;
//      p.decodeStringToStream();
//      ObjectInputStream ois = new ObjectInputStream( p.getStream().getStream() );
//      Throwable t = (Throwable)ois.readObject();
//      return t;
    }
    return null;
  }

  /** getValue
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public String getValue() throws JC_Exception {
    try {
      return getProperty( "jecars:Value" ).getValueString();
    } catch( JC_Exception je ) {
    }
    return null;
  }

  /** toString
   * 
   * @return
   */
  @Override
  public String toString() {
    try {
      String out = getName() + "type=" + getType() + " nodetype=" + getNodeType() + '\n';
      if (hasProperty( "jecars:State" ))  {
        out += "\tState\t=\t" + getProperty( "jecars:State" ).getValueString() + '\n';
      }
      if (hasProperty( "jecars:Body" ))   {
        out += "\tBody\t=\t" + getProperty( "jecars:Body" ).getValueString() + '\n';
      }
      if (hasProperty( "jecars:Value" ))  {
        out += "\tValue\t=\t" + getValue() + '\n';
      }
      if (hasProperty( "jecars:DValue" )) {
        out += "\tDValue\t=\t" + getDoubleValue() + '\n';
      }
      return out;
    } catch( Exception e ) {
     e.printStackTrace();
      return super.toString();
    }
  }

}
