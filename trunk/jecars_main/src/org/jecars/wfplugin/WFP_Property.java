/*
 * Copyright 2013 NLR - National Aerospace Laboratory
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
package org.jecars.wfplugin;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 *
 * @author weert
 */
public class WFP_Property implements IWFP_Property {

  final private Property mProperty;
    
  public WFP_Property( final Property pP ) {
    mProperty = pP;
    return;
  }

  @Override
  public String getStringValue() throws WFP_Exception {
    try {
      return mProperty.getValue().getString();
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  @Override
  public InputStream getStreamValue() throws WFP_Exception {
    try {
      return mProperty.getBinary().getStream();
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  @Override
  public List<Object> getValues() throws WFP_Exception {
    try {
      final Value[] vals = mProperty.getValues();
      final List<Object> list = new ArrayList<Object>();
      for( final Value val : vals ) {
        switch( val.getType() ) {
          case PropertyType.BINARY: {
            list.add( val.getBinary().getStream() );
            break;
          }
          case PropertyType.BOOLEAN: {
            list.add( val.getBoolean() );
            break;
          }
          case PropertyType.DATE: {
            list.add( val.getDate() );
            break;
          }
          case PropertyType.DECIMAL: {
            list.add( val.getDecimal() );
            break;
          }
          case PropertyType.DOUBLE: {
            list.add( val.getDouble() );
            break;
          }
          case PropertyType.LONG: {
            list.add( val.getLong() );
            break;
          }
          default: {
            list.add( val.getString() );            
          }
        }
      }
      return list;
    } catch( RepositoryException re ) {
      throw new WFP_Exception( re );
    }
  }

  @Override
  public String getName() throws WFP_Exception {
    try {
      return mProperty.getName();
    } catch( RepositoryException re ) {      
      throw new WFP_Exception(re);
    }    
  }

  
}
