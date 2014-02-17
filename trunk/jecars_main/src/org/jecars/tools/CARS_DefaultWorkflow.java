/*
 * Copyright 2012-2014 NLR - National Aerospace Laboratory
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
package org.jecars.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.jcr.*;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import static org.jecars.tools.CARS_ToolInterface.STATEREQUEST_RERUN;
import org.jecars.tools.workflow.IWF_WorkflowRunner;
import org.jecars.tools.workflow.WF_WorkflowRunner;
import org.jecars.wfplugin.IWFP_Context;
import org.jecars.wfplugin.IWFP_InterfaceResult;
import org.jecars.wfplugin.WFP_Context;
import org.jecars.wfplugin.WFP_InterfaceResult;
import org.jecars.wfplugin.WFP_Tool;
import org.jecars.wfplugin.tools.WFPT_Archive;

/** CARS_DefaultWorkflow
 *
 * @author weert
 */
public class CARS_DefaultWorkflow extends CARS_DefaultToolInterface {
 
  static final private Object LOCK = new Object();

  private transient long mToolStartTime = 0;
  private transient long mToolAverageRunningTime = 0;

  private final ExecutorService                         mExecutor = Executors.newCachedThreadPool();
  private final CompletionService<IWFP_InterfaceResult> mExecutorService = new ExecutorCompletionService<>( mExecutor );

  
  /** toolInit
   * 
   * @throws Exception
   */
  @Override
  protected void toolInit() throws Exception {
    CARS_ToolSignalManager.addToolSignalListener( this );
    super.toolInit();
    getTool().addMixin( "jecars:mix_datafolder" );
    final Session syssession = CARS_Factory.getSystemToolsSession();
    synchronized( syssession ) {        
      final Node tt = syssession.getNode( getToolTemplate( getTool() ).getPath() );
      if (tt!=null) {
        if (!tt.isNodeType( "jecars:mix_toolstatistics" )) {
          tt.addMixin( "jecars:mix_toolstatistics" );
          tt.save();
        }
        if (!tt.hasNode( "toolstatistics" )) {
          tt.addNode( "toolstatistics", "jecars:ToolStatistics" );
        }
        Node tstat = tt.getNode( "toolstatistics" );
        tstat.setProperty( "LastStarted", Calendar.getInstance() );
        syssession.save();
        mToolAverageRunningTime = tstat.getProperty( "AverageRunTimeInSecs" ).getLong();
      }
      syssession.save();
    }
    mToolStartTime = System.currentTimeMillis();        
    
    final Node runners = getTool().getNode( "runners" );
    if (!runners.hasNode( "Main")) {
      Node mainRunner = runners.addNode( "Main", "jecars:WorkflowRunner" );
      mainRunner.addMixin( "jecars:interfaceclass" );
      mainRunner.setProperty( "jecars:InterfaceClass", "org.jecars.tools.CARS_WorkflowRunnerInterfaceApp" );
      getTool().save();
    }
    return;
  }

  /** toolInput
   *    Copy the inputs to the first main context
   * @throws Exception
   */
  @Override
  protected void toolInput() throws Exception {
    super.toolInput();
    Node config = getConfigNode();
    
    Node context = getTool().getNode( "runners/Main/context" );
    
    final Session session = context.getSession();
    for( final Node in : getMixInputs() ) {
      final String copyPath = context.getPath() + "/" + in.getName();
      try {
        session.getNode( copyPath );
      } catch( PathNotFoundException pe ) {
        session.getWorkspace().copy( in.getPath(), copyPath );
      }
    }
    
    {
      final NodeIterator ni = getTool().getNodes( "jecars:Input*" );
      while( ni.hasNext() ) {
        Node n = ni.nextNode();
        if (!context.hasNode( n.getName() )) {
          n.getSession().getWorkspace().copy( n.getPath(), context.getPath() + "/" + n.getName() );
        }
      }
    }

    // **** Copy the parameters
    {
      final NodeIterator ni = getTool().getNodes();
      while( ni.hasNext() ) {
        Node n = ni.nextNode();
        if (n.isNodeType( "jecars:parameterresource" )) {
          if (!context.hasNode( n.getName() )) {
            n.getSession().getWorkspace().copy( n.getPath(), context.getPath() + "/" + n.getName() );
          }
        }
      }
    }
    
    return;
  }

  
  /** toolRun
   * 
   * @throws Exception 
   */
  @Override
  @SuppressWarnings("SleepWhileInLoop")
  protected void toolRun() throws Exception {
    super.toolRun();
//    System.out.println("DEFAULT WORKFLOW 1 " + System.currentTimeMillis());
    
    final List<IWF_WorkflowRunner>
               currentRunners = new ArrayList<>(16);
    final CARS_Main      main = getMain().getFactory().createMain( CARS_ActionContext.createActionContext(getMain().getContext()) );
    final Node     runnerNode = main.getSession().getNode( getTool().getNode( "runners/Main" ).getPath() );
    final boolean       RERUN = (STATEREQUEST_RERUN.equals(getStateRequest()));
    final WF_WorkflowRunner mainwr = new WF_WorkflowRunner( main, runnerNode, RERUN );
    int nodesIsErrorCancelCountdown = 2;
    try {
        mainwr.restart( RERUN, false );
        final List<Node> nodesInError = new ArrayList<>(4);
        final List<IWFP_InterfaceResult> results = new ArrayList<>(4);
        do {
          final NodeIterator rin = getTool().getNode( "runners" ).getNodes();
          while( rin.hasNext() ) {
            // **** Write progress
            double runtime = (System.currentTimeMillis()-mToolStartTime)/1000;
            double progress = runtime/(double)mToolAverageRunningTime;
            if (progress>0.95) {
              progress = 0.95;
            }
            synchronized( WF_WorkflowRunner.WRITERACCESS ) {
              getTool().setProperty( "jecars:PercCompleted", 100.0*progress );
              getTool().save();
            }

            final Node runner = rin.nextNode();
    //    System.out.println("check 00 " + runner.getPath() + " - " + runner.getProperty( "jecars:State" ).getString() );
            if (runner.getProperty( "jecars:State" ).getString().startsWith( CARS_ToolInterface.STATE_CLOSED ) ) {
              // **** Runner is finished
              if (runner.getProperty( "jecars:State" ).getString().startsWith( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED )) {
                // **** Runner is finished with an error
                if (!nodesInError.contains( runner )) {
                  nodesInError.add( runner );
                }
              }
              for( final IWF_WorkflowRunner wrun : currentRunners ) {
                if (wrun.getPath().equals( runner.getPath() )) {
                  currentRunners.remove( wrun );
//                  System.out.println("CANCEL 1111 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ");
//                  wrun.cancel();
                  break;
                }
              }
            } else {
              boolean isNew = true;
              for( final IWF_WorkflowRunner wrun : currentRunners ) {
                if (wrun.getPath().equals( runner.getPath() )) {
                  isNew = false;
                  break;
                }
              }
              if (isNew) {
                // **** Create a new workflow
                final CARS_Main newmain = main.getFactory().createMain( CARS_ActionContext.createActionContext(getMain().getContext()) );
                final Node newrunnerNode = newmain.getSession().getNode( runner.getPath() );              
                final WF_WorkflowRunner wrun = new WF_WorkflowRunner( newmain, newrunnerNode, RERUN );
                currentRunners.add( wrun );
                runWorkflowRunner wr = new runWorkflowRunner( wrun );
                final Future<IWFP_InterfaceResult> tir = mExecutorService.submit( wr );                
                wrun.setFuture( tir );
              }
            }
          }
          if (!nodesInError.isEmpty()) {
            nodesIsErrorCancelCountdown--;
//          System.out.println("NODES In ERROR CANCEL " + nodesIsErrorCancelCountdown );            
            if (nodesIsErrorCancelCountdown<0) {
              // **** Runner(s) are in error state, and the thread isn't returned
              for( final IWF_WorkflowRunner runner : currentRunners ) {
//            System.out.println("WANT TO CANCEL " + runner.getPath() );
                // **** Force end of the thread
                runner.cancel();
              }
              nodesIsErrorCancelCountdown = 2;
            } else {
              // **** Wait for the worker thread in error to settle
              Thread.sleep( 2000 );
            }
            break;
          }
          
          // **** Wait (poll) for results of the Executor service
          if (!currentRunners.isEmpty()) {
            final Future<IWFP_InterfaceResult> fwrunResult = mExecutorService.poll( 4, TimeUnit.SECONDS );
            if (fwrunResult!=null) {
              final IWFP_InterfaceResult ir = fwrunResult.get();
              if (ir!=null) {
                results.add( ir );
              }
            }
            }
//          Thread.sleep( 100 );      
//          Thread.sleep( 2000 );      
        } while( !currentRunners.isEmpty() );

        // **** Check for the runner errors
        if (!nodesInError.isEmpty()) {
//          throw new CARS_ToolException( "Runner in Error " + nodesInError );
          throw new CARS_ToolException( nodesInError, results );
        }
    } finally {
//      main.getSession().save();
      main.destroy();
    }

    return;
  }

  
  /** toolRun
   * 
   * @throws Exception 
   */
/*
  @Override
  @SuppressWarnings("SleepWhileInLoop")
  protected void toolRun() throws Exception {
    super.toolRun();
    System.out.println("DEFAULT WORKFLOW 1 " + System.currentTimeMillis());
    
    final List<WF_WorkflowRunner> currentRunners = new ArrayList<WF_WorkflowRunner>();
    final CARS_Main main = getMain().getFactory().createMain( CARS_ActionContext.createActionContext(getMain().getContext()) );
    final Node runnerNode = main.getSession().getNode( getTool().getNode( "runners/Main" ).getPath() );
    final boolean RERUN = (STATEREQUEST_RERUN.equals(getStateRequest()));
    final WF_WorkflowRunner mainwr = new WF_WorkflowRunner( main, runnerNode, RERUN );
    try {
        mainwr.restart( RERUN );
        final List<Node> nodesInError = new ArrayList<Node>();
        do {
          final NodeIterator rin = getTool().getNode( "runners" ).getNodes();
          while( rin.hasNext() ) {
            // **** Write progress
            double runtime = (System.currentTimeMillis()-mToolStartTime)/1000;
            double progress = runtime/(double)mToolAverageRunningTime;
            if (progress>0.95) {
              progress = 0.95;
            }
            synchronized( WF_WorkflowRunner.WRITERACCESS ) {
              getTool().setProperty( "jecars:PercCompleted", 100.0*progress );
              getTool().save();
            }

            final Node runner = rin.nextNode();
    //    System.out.println("check 00 " + runner.getPath() + " - " + runner.getProperty( "jecars:State" ).getString() );
            if (runner.getProperty( "jecars:State" ).getString().startsWith( CARS_ToolInterface.STATE_CLOSED ) ) {
              // **** Runner is finished
              if (runner.getProperty( "jecars:State" ).getString().startsWith( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED )) {
                // **** Runner is finished with an error
                nodesInError.add( runner );
              }
              for( final WF_WorkflowRunner wrun : currentRunners ) {
                if (wrun.getPath().equals( runner.getPath() )) {
                  currentRunners.remove( wrun );
                  break;
                }
              }
            } else {
              boolean isNew = true;
              for( final WF_WorkflowRunner wrun : currentRunners ) {
                if (wrun.getPath().equals( runner.getPath() )) {
                  isNew = false;
                  break;
                }
              }
              if (isNew) {
                final CARS_Main newmain = main.getFactory().createMain( CARS_ActionContext.createActionContext(getMain().getContext()) );
                final Node newrunnerNode = newmain.getSession().getNode( runner.getPath() );              
                final WF_WorkflowRunner wrun = new WF_WorkflowRunner( newmain, newrunnerNode, RERUN );
                currentRunners.add( wrun );
                runWorkflowRunner wr = new runWorkflowRunner( wrun );
                final Thread t = new Thread( wr );
                t.setPriority( Thread.currentThread().getPriority() );
                t.setName( wrun.getPath() );
                t.start();
                wrun.setThread( t );
              }
            }
          }
          if (!nodesInError.isEmpty()) {
            // **** Runner(s) are in error state
            for( final WF_WorkflowRunner runner : currentRunners ) {
              runner.getThread().interrupt();
            }
            break;
          }
          Thread.sleep( 100 );      
//          Thread.sleep( 2000 );      
        } while( !currentRunners.isEmpty() );

        // **** Check for the runner errors
        if (!nodesInError.isEmpty()) {
          throw new CARS_ToolException( "Runner in Error " + nodesInError );
        }
    } finally {
//      main.getSession().save();
      main.destroy();
    }

    return;
  }
 */

  /** runWorkflowRunner
   * 
   */
  private class runWorkflowRunner implements Callable<IWFP_InterfaceResult> {
    
    private final transient WF_WorkflowRunner mRunner;

    /** runWorkflowRunner
     * 
     * @param pRunner 
     */
    public runWorkflowRunner( final WF_WorkflowRunner pRunner ) {
      mRunner = pRunner;
      return;
    }

    /** call
     * 
     * @return
     * @throws Exception 
     */
    @Override
    public IWFP_InterfaceResult call() throws Exception {
      String currentSource = "";
      IWFP_InterfaceResult res = null;
      try {
//    System.out.println("DEFAULT WORKFLOW THREAD 1 " + System.currentTimeMillis());
        
//        mRunner.restart();
        boolean run = true;
        // **** A restart can be the reason that another run must be initiated
        while( run ) {
          run = false;
          while( (res=mRunner.singleStep()).hasState( WFP_InterfaceResult.STATE.OK ) ) {
            if (mRunner.getCurrentLink().isNULL()) {
              currentSource = mRunner.getCurrentTask().toString();
//              System.out.println("Child TASK wrun: " + currentSource );
            } else {
              currentSource = mRunner.getCurrentLink().toString();            
//              System.out.println("Child LINK wrun: " + currentSource );
            }
            if (STATE_PAUSED.equals( getState() )) {
              mRunner.setState( STATE_OPEN_RUNNING + STATE_PAUSED );
              while(STATE_PAUSED.equals( getState() )) {
                Thread.sleep( 2000 );
              }
              mRunner.setState( STATE_OPEN_RUNNING );            
            } else if (STATE_OPEN_ABORTING.equals( getState() ) ) {
              // **** Workflow must be aborted
              mRunner.setState( STATE_CLOSED_ABNORMALCOMPLETED_ABORTED );
              break;
            }
          }
          if (res.hasState( WFP_InterfaceResult.STATE.RERUN )) {
            mRunner.restart( false, true );
            run = true;
          }
        }
        if (res.hasState( WFP_InterfaceResult.STATE.ERROR )) {
          synchronized( WF_WorkflowRunner.WRITERACCESS ) {
            mRunner.getWorkflow().getNode().setProperty( "jecars:State", CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED );
            mRunner.getWorkflow().save();
          }
        }
        if (mRunner.getState().startsWith( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED )) {
          synchronized( WF_WorkflowRunner.WRITERACCESS ) {
            mRunner.getWorkflow().getNode().setProperty( "jecars:State", mRunner.getState() );
            mRunner.getWorkflow().save();
          }
        }
//    System.out.println("DEFAULT WORKFLOW THREAD 2 " + System.currentTimeMillis());
//      System.out.println("WORKFLOW END  time=" + System.currentTimeMillis()  );
//        System.out.println("  STATE = " + mRunner.getWorkflow().getNode().getProperty( "jecars:State" ).getString() );

        // **** Check for the archive option
        // **** Dit moet anders, als het nu draait dan is de Session al afgesloten wanneer de method returned
//        try {
//          Node config = mRunner.getWorkflow().getConfig();
//          if (config!=null && config.hasProperty( "jecars:ArchiveDirectory" )) {
//            Thread.sleep( 5000 );
//            String archive = config.getProperty( "jecars:ArchiveDirectory" ).getString();
//            WFPT_Archive archiveTool = new WFPT_Archive();
//            final WFP_Tool       tool = new WFP_Tool( null, mRunner.getWorkflow() );
//            final IWFP_Context context = new WFP_Context( mRunner.getContext(), getMain() );
//            if (config.hasProperty( "jecars:JeCARSBackupDirectory" )) {
//              String backup = config.getProperty( "jecars:JeCARSBackupDirectory" ).getString();
//              context.addTransientObject( WFPT_Archive.JECARSBACKUPDIRECTORY, backup );
//            }
//            context.addTransientObject( WFPT_Archive.NODEFORARCHIVE, mRunner.getWorkflow().getNode() );
//            context.addTransientObject( WFPT_Archive.ARCHIVEDIRECTORY, archive );
//            archiveTool.start( tool, context );
//          }
//        } catch( RepositoryException re ) {
//          reportException( re, Level.SEVERE );
//        } finally {
////          mRunner.destroy();
//        }
      } catch( Exception e ) {
        CARS_ToolInstanceEvent tie = reportExceptionEvent( e, Level.SEVERE );
        try {
          Node eventNode = tie.getEventNode( mRunner.getNode().getSession() );
          if (eventNode!=null) {
            eventNode.addMixin( "jecars:mixin_unstructured" );
            eventNode.setProperty( "SourcePath", currentSource );
            eventNode.save();
          }
          final String state = CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED;
          synchronized( WF_WorkflowRunner.WRITERACCESS ) {
            mRunner.setState( state );
            mRunner.save();
            mRunner.getWorkflow().getNode().setProperty( "jecars:State", state );
            mRunner.getWorkflow().save();
          }
        } catch( RepositoryException re ) {
          reportException( re, Level.WARNING );
        }
      } finally {
        mRunner.destroy();        
      }
//    System.out.println("DEFAULT WORKFLOW THREAD 3 RESULT " + mRunner.getPath() + " -- " + res.hasState(WFP_InterfaceResult.STATE.ERROR) );
      return res;
    }

    
    
    /** run
     * 
     */
 /*
    @Override
    public void run() {
      String currentSource = "";
      try {
    System.out.println("DEFAULT WORKFLOW THREAD 1 " + System.currentTimeMillis());
        
//        mRunner.restart();
        WFP_InterfaceResult res;
        while( (res=mRunner.singleStep()).hasState( WFP_InterfaceResult.STATE.OK ) ) {
          if (mRunner.getCurrentLink().isNULL()) {
            currentSource = mRunner.getCurrentTask().toString();
          System.out.println("Child TASK wrun: " + currentSource );
          } else {
            currentSource = mRunner.getCurrentLink().toString();            
          System.out.println("Child LINK wrun: " + currentSource );
          }
          if (STATE_PAUSED.equals( getState() )) {
            mRunner.setState( STATE_OPEN_RUNNING + STATE_PAUSED );
            while(STATE_PAUSED.equals( getState() )) {
              Thread.sleep( 2000 );
            }
            mRunner.setState( STATE_OPEN_RUNNING );            
          } else if (STATE_OPEN_ABORTING.equals( getState() ) ) {
            // **** Workflow must be aborted
            mRunner.setState( STATE_CLOSED_ABNORMALCOMPLETED_ABORTED );
            break;
          }
        }
        if (res.hasState( WFP_InterfaceResult.STATE.ERROR )) {
          synchronized( WF_WorkflowRunner.WRITERACCESS ) {
            mRunner.getWorkflow().getNode().setProperty( "jecars:State", CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED );
            mRunner.getWorkflow().save();
          }
        }
        if (mRunner.getState().startsWith( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED )) {
          synchronized( WF_WorkflowRunner.WRITERACCESS ) {
            mRunner.getWorkflow().getNode().setProperty( "jecars:State", mRunner.getState() );
            mRunner.getWorkflow().save();
          }
        }
//    System.out.println("DEFAULT WORKFLOW THREAD 2 " + System.currentTimeMillis());
//      System.out.println("WORKFLOW END  time=" + System.currentTimeMillis()  );
//        System.out.println("  STATE = " + mRunner.getWorkflow().getNode().getProperty( "jecars:State" ).getString() );

        // **** Check for the archive option
        try {
          Node config = mRunner.getWorkflow().getConfig();
          if (config!=null && config.hasProperty( "jecars:ArchiveDirectory" )) {
            Thread.sleep( 5000 );
            String archive = config.getProperty( "jecars:ArchiveDirectory" ).getString();
            WFPT_Archive archiveTool = new WFPT_Archive();
            final WFP_Tool       tool = new WFP_Tool( null, mRunner.getWorkflow() );
            final IWFP_Context context = new WFP_Context( mRunner.getContext(), getMain() );
            if (config.hasProperty( "jecars:JeCARSBackupDirectory" )) {
              String backup = config.getProperty( "jecars:JeCARSBackupDirectory" ).getString();
              context.addTransientObject( WFPT_Archive.JECARSBACKUPDIRECTORY, backup );
            }
            context.addTransientObject( WFPT_Archive.NODEFORARCHIVE, mRunner.getWorkflow().getNode() );
            context.addTransientObject( WFPT_Archive.ARCHIVEDIRECTORY, archive );
            archiveTool.start( tool, context );
          }          
        } catch( RepositoryException re ) {
          reportException( re, Level.SEVERE );
        } finally {
          mRunner.destroy();
        }
      } catch( Exception e ) {
        CARS_ToolInstanceEvent tie = reportExceptionEvent( e, Level.SEVERE );
        try {
          tie.getEventNode().addMixin( "jecars:mixin_unstructured" );
          tie.getEventNode().setProperty( "SourcePath", currentSource );
          tie.getEventNode().save();
          final String state = CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED;
          synchronized( WF_WorkflowRunner.WRITERACCESS ) {
            mRunner.setState( state );
            mRunner.save();
            mRunner.getWorkflow().getNode().setProperty( "jecars:State", state );
            mRunner.getWorkflow().save();
          }
        } catch( RepositoryException re ) {
          reportException( re, Level.WARNING );
        }
      }      
    System.out.println("DEFAULT WORKFLOW THREAD 3 " + System.currentTimeMillis());
      return;
    } */

  }
       
  /** toolOutput
   *
   * @throws Exception
   */
  @Override
  protected void toolOutput() throws Exception {
    synchronized( LOCK ) {
      super.toolOutput();
    }
    return;
  }

  
  
  /** toolFinally
   *
   */
  @Override
  protected void toolFinally() {
    try {
      synchronized( WF_WorkflowRunner.WRITERACCESS ) {
        final Session syssession = CARS_Factory.getSystemToolsSession();
        synchronized( syssession ) {        
          final Node tt = syssession.getNode( getToolTemplate( getTool() ).getPath() );
          if (tt!=null) {
            Node tstat = tt.getNode( "toolstatistics" );
            tstat.setProperty( "TotalNumberOfRuns", tstat.getProperty( "TotalNumberOfRuns" ).getLong()+1 );
            long lastcase = (System.currentTimeMillis()-mToolStartTime)/1000;
            tstat.setProperty( "LastCaseExecution", lastcase );
            tstat.setProperty( "TotalRunTimeInSecs", tstat.getProperty( "TotalRunTimeInSecs" ).getLong()+lastcase );
            if (tstat.getProperty( "WorstCaseExecution" ).getLong()<lastcase) {
              tstat.setProperty( "WorstCaseExecution", lastcase );           
            }
            if (tstat.getProperty( "BestCaseExecution" ).getLong()>lastcase) {
              tstat.setProperty( "BestCaseExecution", lastcase );           
            }
            tstat.setProperty( "AverageRunTimeInSecs", tstat.getProperty( "TotalRunTimeInSecs" ).getLong()/tstat.getProperty( "TotalNumberOfRuns" ).getLong() );
            tstat.save();        
          }
        }
        getTool().setProperty( "jecars:PercCompleted", 100.0 );
        getTool().save();
      }
    } catch( RepositoryException re ) {
      reportException( re, Level.SEVERE );
    }

    CARS_ToolSignalManager.removeToolSignalListener( this );
    super.toolFinally();
    return;
  }

  
  /** refreshOutputFiles
   *
   * @throws FileNotFoundException
   * @throws RepositoryException
   */
  private void refreshOutputFiles( final Node pTool ) throws FileNotFoundException, RepositoryException {
    return;
  }
  
  /** signal
   *
   * @param pToolPath
   * @param pSignal
   */
  @Override
  public void signal( final String pToolPath, final CARS_ToolSignal pSignal ) {
    switch( pSignal ) {

      /** REFRESH_OUTPUTS 
       *
       */
      case REFRESH_OUTPUTS: {
        try {
          if (STATE_OPEN_RUNNING.equals( getCurrentState() )) {
//         System.out.println("REQUEST OUTPUT: " + getState() );
            Session toolSession = createToolSession();
            Node tool = toolSession.getNode( getTool().getPath() );
            try {
              refreshOutputFiles( tool );
            } finally {
              toolSession.save();
              toolSession.logout();
            }
          }
        } catch( Exception e ) {
          LOG.log( Level.WARNING, e.getMessage(), e );
        }
        break;
      }
    }
    super.signal(pToolPath, pSignal);
  }

  
    
}
