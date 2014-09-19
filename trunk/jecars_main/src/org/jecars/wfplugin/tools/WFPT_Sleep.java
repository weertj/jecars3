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
package org.jecars.wfplugin.tools;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import org.apache.jackrabbit.util.ISO8601;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_ContextParameter;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_Tool;
import org.jecars.wfplugin.WFP_InterfaceResult;

/**
 *
 * @author weert
 */
public class WFPT_Sleep implements IWFP_Interface {

  static public final String SLEEPTIMEINSECS    = "SleepTimeInSecs";
  static public final String WAKEUPDATE         = "WakeUpDate";
  static public final String REPEATTIMESECS     = "RepeatTimeSecs";
  static public final String REPEATTIMEDAYS     = "RepeatTimeDays";
    
  private Timer mTimer = new Timer();
  final private Object mWakeup = new Object();
  
  private class WakeUpTimer extends TimerTask {

    @Override
    public void run() {
      synchronized( mWakeup ) {
        mWakeup.notifyAll();
      }
      return;
    }
  }
  
  /** start
   * 
   * @param pTool
   * @param pContext
   * @return 
   */
  @Override
  public WFP_InterfaceResult start( final IWFP_Tool pTool, final IWFP_Context pContext ) {
//      System.out.println("----- SLEEP TOOL ------ " + Thread.currentThread() );
    try {
      { // **** The context parameters
      IWFP_ContextParameter sleepsecs  = pTool.getContextParameter( pContext, ".*" + SLEEPTIMEINSECS, null, true );
      IWFP_ContextParameter wakeupdate = pTool.getContextParameter( pContext, ".*" + WAKEUPDATE, null, true );
      IWFP_ContextParameter repeatsecs = pTool.getContextParameter( pContext, ".*" + REPEATTIMESECS, null, true );
      IWFP_ContextParameter repeatdays = pTool.getContextParameter( pContext, ".*" + REPEATTIMEDAYS, null, true );

      if (sleepsecs!=null) {
        pTool.reportMessage( Level.INFO, "Sleep for " + sleepsecs.getStringValue() + " seconds" );
        Thread.sleep( (int)(Double.parseDouble(sleepsecs.getStringValue()) * 1000) );
      } else if (wakeupdate!=null) {        
        Calendar cal = ISO8601.parse( wakeupdate.getStringValue() );
        mTimer = new Timer( pTool.getTaskPath() );
        synchronized( mWakeup ) {
          mTimer.schedule( new WakeUpTimer(), cal.getTime(), 2000 );
          mWakeup.wait();
        }
        if (repeatsecs!=null) {
          int secs = Integer.parseInt( repeatsecs.getStringValue() );
          cal.add( Calendar.SECOND, secs );
          wakeupdate.setValue( ISO8601.format(cal) );
        }
        if (repeatdays!=null) {
          int days = Integer.parseInt( repeatdays.getStringValue() );
          cal.add( Calendar.DAY_OF_YEAR, days );
          wakeupdate.setValue( ISO8601.format(cal) );
        }
      }
      
      
      }
      
      { // **** The tool parameters
      String sleepsecs  = pTool.getParameter( SLEEPTIMEINSECS, null );
      String wakeupdate = pTool.getParameter( WAKEUPDATE, null );
      String repeatsecs = pTool.getParameter( REPEATTIMESECS, null );
      String repeatdays = pTool.getParameter( REPEATTIMEDAYS, null );

      if (sleepsecs!=null) {
        pTool.reportMessage( Level.INFO, "Sleep for " + sleepsecs + " seconds" );
        Thread.sleep( (int)(Double.parseDouble(sleepsecs) * 1000) );
      } else if (wakeupdate!=null) {        
        Calendar cal = ISO8601.parse( wakeupdate );
        mTimer = new Timer( pTool.getTaskPath() );
        synchronized( mWakeup ) {
          mTimer.schedule( new WakeUpTimer(), cal.getTime(), 2000 );
          mWakeup.wait();
        }
        if (repeatsecs!=null) {
          int secs = Integer.parseInt( repeatsecs );
          cal.add( Calendar.SECOND, secs );
          pTool.getParameter( WAKEUPDATE ).setValue( ISO8601.format(cal) );
        }
        if (repeatdays!=null) {
          int days = Integer.parseInt( repeatdays );
          cal.add( Calendar.DAY_OF_YEAR, days );
          pTool.getParameter( WAKEUPDATE ).setValue( ISO8601.format(cal) );
        }
      }
      }
    } catch( Exception e ) {
      pTool.reportException( Level.WARNING, e);
    }
      
    /*
      final IWFP_Node taskNode = pTool.getTaskAsNode();
      if (taskNode.hasNode( SLEEPTIMEINSECS )) {
        IWFP_Node sleeptime = taskNode.getNode( SLEEPTIMEINSECS );
        List<Object> values = sleeptime.getProperty( "jecars:string" ).getValues();
        int time = Integer.parseInt( (String)(values.get(0)) );
        Thread.sleep( time*1000 );
      } else if (taskNode.hasNode( WAKEUPDATE )) {
        IWFP_Node wakeup = taskNode.getNode( WAKEUPDATE );
        List<Object> values = wakeup.getProperty( "jecars:string" ).getValues();
        String wakeupdate = (String)(values.get(0));
        Calendar cal = ISO8601.parse( wakeupdate );
        mTimer = new Timer( pTool.getTaskPath() );
        mTimer.schedule( new WakeUpTimer(), cal.getTime() );
        synchronized( mWakeup ) {
          mWakeup.wait();
        }
        if (taskNode.hasNode( REPEATTIMESECS )) {
          IWFP_Node repeatinsecs = taskNode.getNode( REPEATTIMESECS );
          values = repeatinsecs.getProperty( "jecars:string" ).getValues();
          int repeatsecs = Integer.parseInt( (String)(values.get(0)) );
          cal.add( Calendar.SECOND, repeatsecs );
          wakeup.setProperty( "jecars:string", ISO8601.format(cal) );
          wakeup.save();
        }
      } else {
        Thread.sleep( 1000 );
      }
    } catch( Exception e ) {
      pTool.reportException( Level.WARNING, e);
    }
    */
    return WFP_InterfaceResult.OK();
  }
    
}
