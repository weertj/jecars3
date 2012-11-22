/*
 * Copyright 2008 NLR - National Aerospace Laboratory
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

/** JC_Defs
 *
 * @version $Id: JC_Defs.java,v 1.10 2008/10/21 10:13:05 weertj Exp $
 */
public class JC_Defs {

  static final public String EXPIRE_DATE = "jecars:ExpireDate";
  
  static final public String OUTPUTTYPE_ATOM = "atom";
  static final public String OUTPUTTYPE_HTML = "html";
  static final public String OUTPUTTYPE_PROPERTIES = "properties";
  static final public String OUTPUTTYPE_BACKUP = "backup";
  static final public String OUTPUTASATOM = "alt=" + OUTPUTTYPE_ATOM;
  static final public String OUTPUTASHTML = "alt=" + OUTPUTTYPE_HTML;
  static final public String OUTPUTASPROPERTIES = "alt=" + OUTPUTTYPE_PROPERTIES;
  static final public String OUTPUTASBACKUP = "alt=" + OUTPUTTYPE_BACKUP;
  
  static final public String ATOM_TITLE     = "title";
  static final public String ATOM_CATEGORY  = "category";

  static final public String UNSTRUCT_NODETYPE = "jecars:unstruct_nodetype";
  
  static final public String PARAM_GETALLPROPS = "getAllProperties";
  
  static final public String FILTER_EVENTTYPES = "FET";
  
  static final public String BACKUP_MIMETYPE = "text/jecars-backup";
  
  /**** JeCARS Tools
  
      [jecars:Tool] > jecars:dataresource, mix:referenceable
    - jecars:AutoStart      (Boolean)
    - jecars:ToolTemplate   (Path)
    - jecars:ToolClass      (String)
    - jecars:StateRequest   (String) < '(start|suspend|resume|stop)'
    - jecars:State          (String)='open.notrunning' mandatory autocreated
    - jecars:PercCompleted  (Double)='0'
    + *                     (jecars:parameterresource) multiple
    + *                     (jecars:inputresource)     multiple
    + *                     (jecars:outputresource)    multiple
    + jecars:Parameter      (jecars:dataresource) multiple
    + jecars:Input          (jecars:dataresource) multiple
    + jecars:Output         (jecars:dataresource) multiple
   */
  
  static final public String STATEREQUEST           = "jecars:StateRequest";
  final static public String STATEREQUEST_START     = "start";
  final static public String STATEREQUEST_ABORT     = "abort";
  final static public String STATEREQUEST_PAUSE     = "pause";
  final static public String STATEREQUEST_STOP      = "stop";
  final static public String STATE_NONE                                = "none";
  final static public String STATE_UNKNOWN                             = "unknown";
  final static public String STATE_OPEN                                = "open.";
  final static public String STATE_OPEN_NOTRUNNING                     = "open.notrunning";
  final static public String STATE_OPEN_NOTRUNNING_SUSPENDED           = "open.notrunning.suspended";
  final static public String STATE_OPEN_RUNNING_INIT                   = "open.running.init";
  final static public String STATE_OPEN_RUNNING_INPUT                  = "open.running.input";
  final static public String STATE_OPEN_RUNNING_PARAMETERS             = "open.running.parameters";
  final static public String STATE_OPEN_RUNNING_OUTPUT                 = "open.running.output";
  final static public String STATE_OPEN_RUNNING                        = "open.running";
  final static public String STATE_PAUSED                              = ".paused";
  final static public String STATE_OPEN_ABORTING                       = "open.aborting";
  final static public String STATE_CLOSED                              = "closed.";
  final static public String STATE_CLOSED_COMPLETED                    = "closed.completed";
  final static public String STATE_CLOSED_ABNORMALCOMPLETED            = "closed.abnormalCompleted";
  final static public String STATE_CLOSED_ABNORMALCOMPLETED_TERMINATED = "closed.abnormalCompleted.terminated";
  final static public String STATE_CLOSED_ABNORMALCOMPLETED_ABORTED    = "closed.abnormalCompleted.aborted";

  
  static final public String TRUE  = "true";
  static final public String FALSE = "false";
  
  /** Value indicating that a numeric field is not set.
   */
  public static final int UNDEFINED = -999999;

  
}
