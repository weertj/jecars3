/*
 * Copyright 2013-2014 NLR - National Aerospace Laboratory
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

import java.util.Calendar;

/**
 *
 * @author weert
 */
public interface IWFP_ContextParameter extends IWFP_Node {

  Calendar              getCalendarValue() throws WFP_Exception;
  String                getStringValue() throws WFP_Exception;
  IWFP_ContextParameter setValue( String pValue ) throws WFP_Exception;
  IWFP_ContextParameter setValue( Calendar pValue ) throws WFP_Exception;
  
}
