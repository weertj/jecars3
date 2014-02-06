/*
 * Copyright 2014 NLR - National Aerospace Laboratory
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
package org.jecars;

import java.util.Calendar;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.tools.CARS_ToolInstanceEvent;

/**
 *
 * @author weert
 */
public interface ICARS_Event {
 
  ICARS_Event eventNode( final Node pNode ) throws RepositoryException;
  String      eventNode();
  boolean     waitForEventNode();
  ICARS_Event waitForEventNode( final boolean pW );
  String    application();
  String    category();
  String    folder();
  String    message();
  String    eventType();
  String    type();
  String    user();
  String    source();
  Calendar  creationDate();
  Throwable throwable();
  long      code();
  ICARS_Event body( final String pBody );
  String      body();
  String    referer();
  String    remoteHost();
  String    userAgent();

  ICARS_Event             toolInstanceEvent( final CARS_ToolInstanceEvent pE );
  CARS_ToolInstanceEvent  toolInstanceEvent();
  
}
