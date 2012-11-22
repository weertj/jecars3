/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

/** JC_EventNode
 *
 */
public class JC_EventNode extends JC_DefaultNode {

  static final public String PROP_EVENTCOLLECTIONID = "jecars:EventCollectionID";
  static final public String PROP_SOURCE            = "jecars:Source";
  static final public String PROP_USER              = "jecars:User";
  static final public String PROP_TYPE              = "jecars:Type";
  static final public String PROP_CATEGORY          = "jecars:Category";

  public JC_EventNode() {
    super();
  }

}
