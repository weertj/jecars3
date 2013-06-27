/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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

/**
 *
 * @author weert
 */
public interface IWFP_Input extends IWFP_Node {
    
  
//  String    getProperty( final String pName ) throws WFP_Exception;
//  IWFP_Property setProperty( final String pName, final String pValue ) throws WFP_Exception;
//  void      save() throws WFP_Exception;
  void        addMixin( String pMixin ) throws WFP_Exception;
  InputStream openStream()  throws WFP_Exception;
  String      getContentsAsString() throws WFP_Exception;
  void        setContents( final InputStream pIS ) throws WFP_Exception;
  void        closeStream() throws WFP_Exception;
    
}