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
package org.jecars.client.observation;

import java.util.Calendar;
import org.jecars.client.JC_Path;

/**
 *
 * @author weert
 */
public interface JC_Event {

  static public enum TYPE { NODE_ADDED, NODE_MOVED, NODE_REMOVED, PROPERTY_ADDED, PROPERTY_CHANGED, PROPERTY_REMOVED };

  /** getDate
   * Returns the date when the change was persisted that caused this event.
   * 
   * @return
   */
  Calendar getDate();

  /** getIdentifier
   * Returns the identifier associated with this event or null if this event has no associated identifier.
   * @return the identifier associated with this event or null.
   */
  String getIdentifier();

  /** getPath
   * Returns the absolute path associated with this event or null if this event has no associated identifier.
   * @return
   */
  JC_Path getPath();

  /** getType
   * Returns the type of this event: an enum defined by this interface.
   * @return
   */
  TYPE getType();

}
