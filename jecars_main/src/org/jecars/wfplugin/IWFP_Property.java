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
import java.util.List;

/**
 *
 * @author weert
 */
public interface IWFP_Property {
  
  String        getName()           throws WFP_Exception;
  String        getStringValue()    throws WFP_Exception;
  InputStream   getStreamValue()    throws WFP_Exception;
  List<Object>  getValues()         throws WFP_Exception;

}
