/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars;

import java.util.Calendar;
import org.jecars.tools.CARS_ToolInstanceEvent;

/**
 *
 * @author weert
 */
public interface ICARS_Event {
 
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
  String    body();

  ICARS_Event             toolInstanceEvent( final CARS_ToolInstanceEvent pE );
  CARS_ToolInstanceEvent  toolInstanceEvent();
  
}
