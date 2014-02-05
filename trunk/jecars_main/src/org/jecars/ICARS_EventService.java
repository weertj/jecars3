/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars;

/**
 *
 * @author weert
 */
public interface ICARS_EventService {

  long    eventsInQueue();
  long    topEventsInQueue();
  long    numberOfEventsWritten();
  boolean offer( ICARS_Event pEvent );
  
}
