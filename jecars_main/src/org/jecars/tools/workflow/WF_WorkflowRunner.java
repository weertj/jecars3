/*
 * Copyright 2011-2014 NLR - National Aerospace Laboratory
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

package org.jecars.tools.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.jcr.*;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_Main;
import org.jecars.CARS_Security;
import org.jecars.CARS_Utils;
import org.jecars.tools.CARS_FileClassLoader;
import org.jecars.tools.CARS_ToolInterface;
import org.jecars.tools.CARS_ToolsFactory;
import org.jecars.wfplugin.IWFP_Interface;
import org.jecars.wfplugin.IWFP_InterfaceResult;
import org.jecars.wfplugin.WFP_Context;
import org.jecars.wfplugin.WFP_InterfaceResult;
import org.jecars.wfplugin.WFP_Tool;

/** WF_WorkflowRunner
 *
 */
@SuppressWarnings("CallToThreadDumpStack")
public class WF_WorkflowRunner extends WF_Default implements IWF_WorkflowRunner {
  
  static final public Object WRITERACCESS = new Object();
  
  static public IWF_WorkflowRunner NULL;
  
  static {
    try {
      NULL = new WF_WorkflowRunner( null, null, false );
    } catch( RepositoryException re ) {
      // **** Will never happen
      re.printStackTrace();
    }
  }

  private final CARS_Main                       mMain;
  private final WFP_Context                     mTransientContext;
  private final EnumSet<EWF_RunnerInstruction>  mInstructions = EnumSet.noneOf(EWF_RunnerInstruction.class);
  private       Future<IWFP_InterfaceResult>    mRunnerFuture;
  private       boolean                         mRerunMode = false;

  
  /** WF_WorkflowRunner
   * 
   * @param pMain
   * @param pNode 
   * @param pRerun 
   * @throws javax.jcr.RepositoryException 
   */
  public WF_WorkflowRunner( final CARS_Main pMain, final Node pNode, final boolean pRerun ) throws RepositoryException {
    super(pNode);    
    mMain = pMain;
    mTransientContext = new WFP_Context(null, pMain );
    mRerunMode = pRerun;
    return;
  }
 
  /** isMainRunner
   * 
   * @return 
   */
  @Override
  public boolean isMainRunner() {
    try {
      return "Main".equals( getNode().getName() );
    } catch( RepositoryException re ) {
      return false;
    }
  }

  /** getInstructions
   * 
   * @return 
   */
  @Override
  public EnumSet<EWF_RunnerInstruction> getInstructions() {
    return mInstructions.clone();
  }

  /** hasInstruction
   * 
   * @param pRI
   * @return 
   */
  @Override
  public boolean hasInstruction(EWF_RunnerInstruction pRI) {
    return mInstructions.contains( pRI );
  }

  /** addInstruction
   * 
   * @param pRI 
   */
  @Override
  public void addInstruction(EWF_RunnerInstruction pRI) {
    mInstructions.add( pRI );
    return;
  }

  /** removeInstruction
   * 
   * @param pRI 
   */
  @Override
  public void removeInstruction(EWF_RunnerInstruction pRI) {
    mInstructions.remove( pRI );
    return;
  }
  
  
  /** setFuture
   * 
   * @param pIR
   * @return 
   */
  @Override
  public IWF_WorkflowRunner setFuture( final Future<IWFP_InterfaceResult> pIR ) {
    mRunnerFuture = pIR;
    return this;
  }

  /** getFuture
   * 
   * @return 
   */
  @Override
  public Future<IWFP_InterfaceResult> getFuture() {
    return mRunnerFuture;
  }

  @Override
  public void cancel() {
    if (mRunnerFuture!=null) {
      mRunnerFuture.cancel( true );
    }
    return;
  }

  
  /** destroy
   * 
   */
  public void destroy() {
    if (mMain!=null) {
      mMain.destroy();
    }
    return;
  }
  
  /** getMain
   * 
   * @return 
   */
  @Override
  public CARS_Main getMain() {
    return mMain;
  }
  
  /** getWorkflow
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public IWF_Workflow getWorkflow() throws RepositoryException {
    return new WF_Workflow( getNode().getParent().getParent() );
  }

  /** getContext
   * 
   * @return
   * @throws RepositoryException 
   */
  @Override
  public IWF_Context getContext() throws RepositoryException {
    return new WF_Context( getNode().getNode( "context" ) );
  }

  /** restart
   * 
   * @param pReRun true will not(!!) delete the runners, use false to start with a clean situation
   * @param pRestoreFromCurrentContext
   * @throws RepositoryException 
   */
  @Override
  public void restart( final boolean pReRun, final boolean pRestoreFromCurrentContext ) throws RepositoryException {
//    System.out.println("restart " + getNode().getPath() );
    synchronized( WRITERACCESS ) {
      getNode().setProperty( "jecars:SingleStep", 0 );
      setState( CARS_ToolInterface.STATE_OPEN_ABORTING );
      save();
      setCurrentTask( "" );
      setCurrentLink( "" );

      if (!pReRun) {
        removeTools();
        save();
        // **** Move context_0 (if available) to context
        if (!pRestoreFromCurrentContext) {
          getContext().restore( 0 );
        }
        removeContexts();
        setProgress( 0 );
      }
      setState( CARS_ToolInterface.STATE_OPEN_NOTRUNNING );
      save();
    }
    return;
  }
  
  /** removeContexts
   * 
   * @throws RepositoryException 
   */
  private void removeContexts() throws RepositoryException {
    // **** Remove tools
    final NodeIterator ni = getNode().getNodes();
    while( ni.hasNext() ) {
      final Node ctx = ni.nextNode();
      if (ctx.getName().startsWith( "context_" )) {
        ctx.remove();
      }
    }
    return;
  }

  /** removeTools
   * 
   * @throws RepositoryException 
   */
  private void removeTools() throws RepositoryException {
    // **** Remove tools
    final NodeIterator ni = getNode().getNodes();
    while( ni.hasNext() ) {
      final Node tool = ni.nextNode();
      if (tool.isNodeType( "jecars:Tool" )) {
        tool.remove();
      }
    }
    return;
  }
  
  /** singleStep
   * 
   * @return
   * @throws Exception 
   */
  @Override
  public WFP_InterfaceResult singleStep() throws Exception {

    String state = getState();
    if ((state.startsWith( CARS_ToolInterface.STATE_CLOSED ))) {
      // **** Do not run
      return WFP_InterfaceResult.STOP();
    } else {
      state = CARS_ToolInterface.STATE_OPEN_RUNNING;
      synchronized( WRITERACCESS ) {
        setState( state );
        save();
      }
      WFP_InterfaceResult res = nextStep();
      if (res.hasState( WFP_InterfaceResult.STATE.OK  )) {
        state = CARS_ToolInterface.STATE_OPEN_RUNNING + CARS_ToolInterface.STATE_PAUSED;
      } else {
        if (res.hasState( WFP_InterfaceResult.STATE.ERROR )) {          
          
          // ***************************************************
          // **** Workflow in error, check for the ErrorHandlers
          boolean rerun = false;
          final List<IWF_Task> tasks = getWorkflow().getTaskByType( EWF_TaskType.ERRORHANDLER );
          for( final IWF_Task errorTask : tasks ) {
            WFP_InterfaceResult ires = runJavaTask( errorTask, mTransientContext );
            if (ires.hasState( WFP_InterfaceResult.STATE.RERUN )) {
              rerun = true;
            }
            if (ires.hasState( WFP_InterfaceResult.STATE.ERROR )) {
              rerun = false;
              res = ires;
              break;
            }
          }
          if (rerun) {
            state = CARS_ToolInterface.STATE_OPEN_RUNNING_RERUN;
            res.addState( WFP_InterfaceResult.STATE.RERUN );
          } else {
            state = CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED;
          }
        } else {
          state = getState();
          if (!state.startsWith( CARS_ToolInterface.STATE_CLOSED )) {
            state = CARS_ToolInterface.STATE_CLOSED_COMPLETED;
          }
        }
      }
      synchronized( WRITERACCESS ) {
        setState( state );
        save();
      }
//   System.out.println("single step 2 " + getNode().getPath() + " time: " + System.currentTimeMillis() );
      return res;
    }
  }

  /** setCurrentTask
   * 
   * @param pCurrentTask
   * @throws RepositoryException 
   */
  @Override
  public void setCurrentTask( final String pCurrentTask ) throws RepositoryException {
    getNode().setProperty( "jecars:currentTask", pCurrentTask );
    return;
  }

  /** setCurrentLink
   * 
   * @param pCurrentLink
   * @throws RepositoryException 
   */
  @Override
  public void setCurrentLink( final String pCurrentLink ) throws RepositoryException {
    getNode().setProperty( "jecars:currentLink", pCurrentLink );
    return;
  }
  
  /** getCurrentTask
   * 
   * @return
   * @throws RepositoryException 
   */
  public IWF_Task getCurrentTask() throws RepositoryException {
    if (getNode().hasProperty( "jecars:currentTask" )) {
      final String ctp = getNode().getProperty( "jecars:currentTask" ).getString();
      if ("".equals( ctp )) {
        return WF_Task.NULL;
      }
      return new WF_Task( getNode().getSession().getNode( ctp ) );
    } else {
      synchronized( WRITERACCESS ) {
        setCurrentTask( "" );
        save();
      }
    }
    return WF_Task.NULL;
  }

  /** getCurrentLink
   * 
   * @return
   * @throws RepositoryException 
   */
  public IWF_Link getCurrentLink() throws RepositoryException {
    if (getNode().hasProperty( "jecars:currentLink" )) {
      final String ctp = getNode().getProperty( "jecars:currentLink" ).getString();
      if ("".equals( ctp )) {
        return WF_Link.NULL;
      }
      return new WF_Link( getNode().getSession().getNode( ctp ) );
    } else {
      synchronized( WRITERACCESS ) {
        setCurrentLink( "" );
        save();
      }
    }
    return WF_Link.NULL;
  }

  /** getStepNumber
   * 
   * @return
   * @throws RepositoryException 
   */
  public long getStepNumber() throws RepositoryException {
    return getNode().getProperty( "jecars:SingleStep" ).getLong();      
  }

  /** nextStep
   * 
   * @throws RepositoryException 
   */
  private WFP_InterfaceResult nextStep() throws Exception {
    
    WFP_InterfaceResult res = WFP_InterfaceResult.OK();
//    boolean stillRunning = true;
      
    final IWF_Workflow workflow = getWorkflow();
    final IWF_Task  currentTask = getCurrentTask();
    final IWF_Link  currentLink = getCurrentLink();

//  System.out.println("+++++ next step time: " + System.currentTimeMillis() + " == " + currentTask.getPath() + " : " + currentLink.getPath() );
    
    if (currentTask.isNULL() && currentLink.isNULL()) {
      
      if (isMainRunner()) {
        // **** First find and start the tasks which have no inputs
        for( final IWF_Task task : workflow.getTaskWithoutInputs() ) {
          if ((task.getType()!=EWF_TaskType.START) && (task.getType()!=EWF_TaskType.ERRORHANDLER)) {
            // **** Fork task
            synchronized( WRITERACCESS ) {
              final IWF_WorkflowRunner newwr = workflow.createRunner( this, task );
              newwr.setCurrentTask( task.getNode().getPath() );
              newwr.setCurrentLink( "" );
              save();
            }
          }
        }
      }      
      
      // **** STEP: Set first current task
      final List<IWF_Task> tasks = workflow.getTaskByType( EWF_TaskType.START );
      if (tasks.isEmpty()) {
        // **** START task not available
        throw new Exception( "START task not found in " + workflow.getNode().getPath() );
      } else {
        synchronized( WRITERACCESS ) {
          // **** TODO only 1 start task is currently allowed
          final IWF_Task starttask = tasks.get(0);
          setCurrentTask( starttask.getNode().getPath() );
          setCurrentLink( "" );
          backupContext();
        }
      }
    } else {
//  System.out.println("WFRUN  time: " + System.currentTimeMillis() );

      if (currentTask.isNULL()) {
/*        
        // *************************
        // **** Execute current link
        synchronized( WRITERACCESS ) {
          getContext().setUsedLink( currentLink );
          save();
          backupContext();
          final IWF_Task toTask = currentLink.getToEndPoint().getEndPoint();        
          setCurrentTask( toTask.getNode().getPath() );
          setCurrentLink( "" );
          save();
        }
        final IWF_Task fromTask = currentLink.getFromEndPoint().getEndPoint();
        final List<IWF_Link> links = workflow.getFromLinkByTask( fromTask );

        // **** Filter the copied items
        final List<IWF_LinkEndPoint> leps = new ArrayList<>();
        leps.add( currentLink.getFromEndPoint() );
        synchronized( WRITERACCESS ) {
          getContext().filter( this, leps );
          final List<IWF_LinkEndPoint> lepsto = new ArrayList<>();
          lepsto.add( currentLink.getToEndPoint() );
          getContext().filter( this, lepsto );
          // **** Execute link functions... property manipulation        
          getContext().linkFunctions( this, leps, lepsto );
          save();
        }
        
        // **** Convert (nodetype) the items
        final List<IWF_LinkEndPoint> oleps = new ArrayList<>();
        for( final IWF_Link link : links ) {
          leps.add( link.getToEndPoint() );
        }
        synchronized( WRITERACCESS ) {
          getContext().convertTo( oleps );
          save();
        }
*/
                
        final List<IWF_LinkEndPoint> leps = new ArrayList<>(1);
        leps.add( currentLink.getFromEndPoint() );
        final boolean hasFilter = getContext().hasFilter( leps );
        if (hasFilter) {
          synchronized( WRITERACCESS ) {
            getContext().setUsedLink( currentLink );
            save();
            backupContext();
            final IWF_Task toTask = currentLink.getToEndPoint().getEndPoint();        
            setCurrentTask( toTask.getNode().getPath() );
            setCurrentLink( "" );
            save();
          }
  //        final IWF_Task fromTask = currentLink.getFromEndPoint().getEndPoint();
  //        final List<IWF_Link> links = workflow.getFromLinkByTask( fromTask );

          // **** Filter the copied items
          synchronized( WRITERACCESS ) {
            getContext().filter( this, leps );
            final List<IWF_LinkEndPoint> lepsto = new ArrayList<>(16);
            lepsto.add( currentLink.getToEndPoint() );
            getContext().filter( this, lepsto );
            // **** Execute link functions... property manipulation        
            getContext().linkFunctions( this, leps, lepsto );
            save();
          }

          // **** Convert (nodetype) the items (TURNED OFF)        
  //        final List<IWF_LinkEndPoint> oleps = new ArrayList<>();
  //        for( final IWF_Link link : links ) {
  //          leps.add( link.getToEndPoint() );
  //        }
  //        synchronized( WRITERACCESS ) {
  //          getContext().convertTo( oleps );
  //          save();
  //        }
        } else {
          // **** No filter available
          synchronized( WRITERACCESS ) {
            getContext().setUsedLink( currentLink );
            save();
//            backupContext();
            final IWF_Task toTask = currentLink.getToEndPoint().getEndPoint();        
            setCurrentTask( toTask.getNode().getPath() );
            setCurrentLink( "" );
            save();
          }          
        }

      } else {
        // **************************
        // **** Execute current task
        synchronized( WRITERACCESS ) {
          getContext().setUsedTask( currentTask );
          save();
        }
        // ******************************
        // **** Check modifiers
        final EnumSet<EWF_TaskModifier> taskMods = currentTask.getModifiers();
//  System.out.println("WFRUN 2 time: " + System.currentTimeMillis() );
        final WFP_InterfaceResult ires = runTask( currentTask );
        if (ires.hasState(WFP_InterfaceResult.STATE.ERROR)) {
          if (taskMods.contains( EWF_TaskModifier.ALLOWERROR )) {
            ires.setState( WFP_InterfaceResult.STATE.OK );
          }
        }
        res.replaceBy( ires );
        final List<IWF_Link> links = workflow.getFromLinkByTask( currentTask );
        // **********************************************
        // **** Check for a decision for the output links
        if (ires.isDecision()) {
          final String outputTaskPort = ires.getStateValue( WFP_InterfaceResult.STATE.OUTPUT_DECISION );
          final List<IWF_Link> tbrem = new ArrayList<>();
          for( final IWF_Link link : links ) {
            boolean toBeRemoved = true;
            for( final IWF_TaskPortRef tpr : link.getFromEndPoint().getTaskPortRefs() ) {
              if (outputTaskPort.equals( tpr.getTaskPort().getPortName() )) {
                toBeRemoved = false;
                break;
              }
            }
            if (toBeRemoved) {
              tbrem.add( link );
            }
          }
          for( final IWF_Link link : tbrem ) {
            links.remove( link );
          }
        }
//  System.out.println("WFRUN 3 time: " + System.currentTimeMillis() );
//        
        if (!res.isContinueWorkflow() || links.isEmpty()) {
          // **** End of this part of the workflow reached
//          stillRunning = false;
          setProgress( 1 );
//          synchronized( WRITERACCESS ) {  // **** Don't set to "", in order to retrace were the runner went into error
//            setCurrentTask( "" );
//            setCurrentLink( "" );
//            save();
//          }
          if (res.isContinueWorkflow()) {
            res.setState( WFP_InterfaceResult.STATE.STOP );
          }
        } else {
          // **** Continue the workflow
          boolean first = true;
          for( final IWF_Link link : links ) {
            if (first) {
              synchronized( WRITERACCESS ) {
                setCurrentTask( "" );
                setCurrentLink( link.getNode().getPath() );
                save();
              }
            } else {
              // **** Fork task
              synchronized( WRITERACCESS ) {
                final IWF_WorkflowRunner newwr = workflow.createRunner( this, currentTask ); //getWorkflow().createRunner( this, currentTask );
                newwr.setCurrentTask( "" );
                newwr.setCurrentLink( link.getNode().getPath() );
                newwr.getContext().copyFrom( getContext() );
                save();
              }
            }
            first = false;
          }
        }
//        // **** Check result
//        if (res.getStates().contains( WFP_InterfaceResult.STATE.ERROR )) {
//          setState( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED );
//        }
      }
    }
    synchronized( WRITERACCESS ) {
      getNode().setProperty( "jecars:SingleStep",  getStepNumber() + 1 );
      save();
    }
    return res;
  }

  /** backupContext
   *  Performs a save at the end
   * @throws RepositoryException 
   */
  private void backupContext() throws RepositoryException {
    final Session session = getNode().getSession();
    final Workspace ws = session.getWorkspace();
    final String newws = getNode().getPath() + "/context_" + getStepNumber();
    if (!mRerunMode) {
      if (session.nodeExists( newws )) {
        session.getNode( newws ).remove();
        save();
      }
      ws.copy( ws.getName(), getContext().getNode().getPath(), newws );
    }
    // **** Check if the context must be expired
    Node bckContext = session.getNode( newws );
    if (bckContext.hasProperty( "jecars:UsedInLink" )) {
      final String linkPath = bckContext.getProperty( "jecars:UsedInLink" ).getString();
      if (!"".equals(linkPath)) {
        final Node link = session.getNode( linkPath );
        if (link.hasProperty( "jecars:ExpireContextAfterMinutes" )) {
          CARS_Utils.setExpireDate( bckContext, (int)link.getProperty( "jecars:ExpireContextAfterMinutes" ).getLong() );
        }
      }
    }
    if (bckContext.hasProperty( "jecars:UsedInTask" )) {
      final String taskPath = bckContext.getProperty( "jecars:UsedInTask" ).getString();
      if (!"".equals(taskPath)) {
        final Node task = session.getNode( taskPath );
        if (task.hasProperty( "jecars:ExpireContextAfterMinutes" )) {
          CARS_Utils.setExpireDate( bckContext, (int)task.getProperty( "jecars:ExpireContextAfterMinutes" ).getLong() );
        }
      }
    }
    save();
    return;
  }
  
  /** searchForIWFP_Interface
   * 
   * @param pNode
   * @return
   * @throws RepositoryException
   * @throws IOException 
   */
  private IWFP_Interface searchForIWFP_Interface( final Node pNode ) throws RepositoryException, IOException {
    IWFP_Interface result = null;
    final NodeIterator ni = pNode.getNodes();
    while( ni.hasNext() ) {
      final Node n = ni.nextNode();
      if (n.getName().endsWith( ".class" )) {
        final InputStream nis = n.getProperty( "jcr:data" ).getBinary().getStream();
        try {
          final CARS_FileClassLoader fcl = new CARS_FileClassLoader( getClass().getClassLoader() );
          try {
            final Class c = fcl.createClass( nis );
            for( Class ic : c.getInterfaces() ) {
              if ("org.jecars.wfplugin.IWFP_Interface".equals( ic.getName() )) {
                // **** Is legal interface
                result = (IWFP_Interface)c.newInstance();
              }
            }
          } catch( Throwable t ) {
//           t.printStackTrace();
            throw new RepositoryException( t );
          }
        } finally {
          nis.close();
        }
      } else {
        result = searchForIWFP_Interface( n );
      }
      if (result!=null) {
        break;
      }
    }
    return result;
  }
  
  /** runJavaTask
   * 
   * @param pTask
   * @throws RepositoryException
   * @throws IOException 
   */
  private WFP_InterfaceResult runJavaTask( final IWF_Task pTask, final WFP_Context pTransientContext ) throws RepositoryException, IOException {
    final IWFP_Interface iface;
    if (pTask.getNode().hasProperty( "jecars:javaclasspath" )) {
      try {
        String javaclasspath = pTask.getNode().getProperty( "jecars:javaclasspath" ).getString();
        if (!CARS_Security.isJavaClassAllowed( javaclasspath )) {
          synchronized( WRITERACCESS ) {
            setState( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED_TERMINATED );
            save();
          }
          throw new AccessDeniedException( "Class instance not allowed: " + javaclasspath );
        }
 //   System.out.println("Start " + javaclasspath + " time=" + System.currentTimeMillis());
        final Class ji = Class.forName( javaclasspath );
        iface = (IWFP_Interface)ji.newInstance();        
      } catch( Throwable t ) {
        throw new RepositoryException( t );
      }
    } else {
      final Node dataNode = pTask.getNode().getNode( "data" );
      iface = searchForIWFP_Interface( dataNode );
    }
    final WFP_Tool       tool = new WFP_Tool( pTask.getNode(), getWorkflow() );
    final WFP_Context context = new WFP_Context( getContext(), getMain() );
    context.copyTransientInputs( pTransientContext );
    final WFP_InterfaceResult res;
    try {
      res = iface.start( tool, context );
    } finally {
      save();
    }
    if (!res.isThreadDeath()) {
      pTransientContext.copyTransientInputs( context );
      if (tool.getWorstExceptionLevel().intValue()>=Level.SEVERE.intValue()) {
        synchronized( WRITERACCESS ) {
          setState( CARS_ToolInterface.STATE_CLOSED_ABNORMALCOMPLETED_ABORTED );
          save();
        }
      }
    }
    return res;
  }

  /** copyDataNodesToContext
   * 
   * @param pOutputFolder
   * @throws RepositoryException 
   */
  private void copyDataNodesToContext( final Node pOutputFolder ) throws RepositoryException {
    // **** Copy the datanode items at context level to the outputfolder
    final List<Node> nl = getContext().getDataNodes();
    for( final Node dataNode : nl ) {
      try {
        if (dataNode.getPath().startsWith(pOutputFolder.getPath())) {
          // **** Node copy not necessary, is already there
        } else {
          synchronized( WRITERACCESS ) {
            dataNode.getSession().move( dataNode.getPath(), pOutputFolder.getPath() + "/" + dataNode.getName() );
            final Node n = pOutputFolder.getNode( dataNode.getName() );
            if (!n.isNodeType( "jecars:mix_outputresource" )) {
              n.addMixin( "jecars:mix_outputresource" );
            }
            save();
          }
        }
      } catch( ItemExistsException ie ) {
      }
    }
    return;
  }
  
  /** handleContextParameters
   * 
   * @param pTask
   * @return
   * @throws RepositoryException 
   */
  private Node handleContextParameters( final IWF_Task pTask ) throws RepositoryException {
    Node wfNode = getContext().getNode();
    
    // ******************************************
    // **** Check for WFP_OUTPUT_FOLDER parameter
    Node param = getContext().getParameterNode( EWF_ContextParameter.WFP_OUTPUT_FOLDER.name() );
    if (param==null) {
      if (pTask.getNode().hasNode( EWF_ContextParameter.WFP_OUTPUT_FOLDER.name() )) {
        param = pTask.getNode().getNode( EWF_ContextParameter.WFP_OUTPUT_FOLDER.name() );
      }
    }
    if (param!=null) {
      final Value[] values = param.getProperty( "jecars:string" ).getValues();
      if (values.length>0) {
        final String folderName = values[0].getString();
        final Node outputFolder;
        if (wfNode.hasNode(folderName)) {
          outputFolder = wfNode.getNode(folderName);
        } else {
          synchronized( WRITERACCESS ) {
            outputFolder = wfNode.addNode( folderName, "jecars:datafolder" );
            outputFolder.addMixin( "jecars:mix_outputresource" );
            save();
          }
        }
        copyDataNodesToContext( outputFolder );        
        wfNode = outputFolder;
      }
    }
    return wfNode;
  }
  
  /** runTask
   * 
   * @param pTask
   * @throws Exception 
   */
  private WFP_InterfaceResult runTask( final IWF_Task pTask ) throws Exception {
    final WFP_InterfaceResult res = WFP_InterfaceResult.OK();
    synchronized( WRITERACCESS ) {
      save();
      backupContext();
    }
    switch( pTask.getType() ) {
      
      // **************
      // **** START
      case START: {
        if (mRerunMode) break;
        handleContextParameters( pTask );
        break;
      }
        
      // **************
      // **** CONSTANTS
      case CONSTANTS: {
        break;
      }
        
      // *****************
      // **** RUN JAVATASK
      case JAVATASK: {
        if (mRerunMode) break;
        handleContextParameters( pTask );
        res.replaceBy( runJavaTask( pTask, mTransientContext ));
        break;
      }

      // ***************
      // **** END 
      case END: {
        if (mRerunMode) break;
        res.setState( WFP_InterfaceResult.STATE.STOP );
//        final List<Node> nl = getContext().getDataNodes();
        final Node outputNode = handleContextParameters( pTask );
//        synchronized( WRITERACCESS ) {
//          Node wfNode = getWorkflow().getNode();
//          Node param = getContext().getParameterNode( EJC_EndTaskParameter.OUTPUT_FOLDER.name() );
//          if (param==null) {
//            if (pTask.getNode().hasNode( EJC_EndTaskParameter.OUTPUT_FOLDER.name() )) {
//              param = pTask.getNode().getNode( EJC_EndTaskParameter.OUTPUT_FOLDER.name() );
//            }
//          }
//          if (param!=null) {
//            final Value[] values = param.getProperty( "jecars:string" ).getValues();
//            if (values.length>0) {
//              final String folderName = values[0].getString();
//              final Node outputFolder;
//              if (wfNode.hasNode(folderName)) {
//                outputFolder = wfNode.getNode(folderName);
//              } else {
//                outputFolder = wfNode.addNode( folderName, "jecars:datafolder" );
//                outputFolder.addMixin( "jecars:mix_outputresource" );
//              }
//              wfNode = outputFolder;                 
//            }
//          }
          copyDataNodesToContext( outputNode );
          
          final NodeIterator ni = outputNode.getNodes();
          final Node wfNode = getWorkflow().getNode();
          synchronized( WRITERACCESS ) {
            while( ni.hasNext() ) {
              Node dataNode = ni.nextNode();
              try {
                final String newNodePath = wfNode.getPath() + "/" + dataNode.getName();
                // **** If the data node already exists in the parent workflow, replace it
                if (wfNode.hasNode( dataNode.getName() )) {
                  wfNode.getNode( dataNode.getName() ).remove();
                }
                wfNode.getSession().move( dataNode.getPath(), newNodePath );
//                wfNode.getSession().move( dataNode.getPath(), wfNode.getPath() + "/" + dataNode.getName() );
                final Node n = wfNode.getNode( dataNode.getName() );
                if (!n.isNodeType( "jecars:mix_outputresource" )) {
                  n.addMixin( "jecars:mix_outputresource" );
                }
              } catch( ItemExistsException ie ) {              
              }            
            }
          }
          save();
          
//          final List<Node> nl = getContext().getDataNodes();
//          for( final Node dataNode : nl ) {
//            try {
//              wfNode.getSession().move( dataNode.getPath(), wfNode.getPath() + "/" + dataNode.getName() );
//              final Node n = wfNode.getNode( dataNode.getName() );
//              if (!n.isNodeType( "jecars:mix_outputresource" )) {
//                n.addMixin( "jecars:mix_outputresource" );
//              }
//            } catch( ItemExistsException ie ) {              
//            }
//          }
//          save();
//        }
        break;
      }

      // *****************
      // **** RUN WORKFLOW
      case WORKFLOW: {
        handleContextParameters( pTask );
        final Node ttn = pTask.getToolTemplateNode();
        final WF_Workflow newWF;
        synchronized( WRITERACCESS ) {
          final WF_Workflow wf = new WF_Workflow( ttn );        
          newWF = wf.copyTo( getNode(), "Workflow_" + getStepNumber() + "_" + ttn.getName() );
          save();
        }          
        final Node n = newWF.getNode();
        n.setProperty( "jecars:ParentTool", getWorkflow().getNode().getPath() );
        
        // **** Copy items from the jecars:workflowtask node
        final NodeIterator nni = pTask.getNode().getNodes();
        final Workspace ws = getNode().getSession().getWorkspace();
        while( nni.hasNext() ) {
          Node ttnn = nni.nextNode();
          if (ttnn.isNodeType( "jecars:parameterresource" )) {
            ttnn = CARS_Utils.getLinkedNode(ttnn);
            ws.copy( ttnn.getPath(), n.getPath() + "/" + ttnn.getName() );
          }
        }

        
        final String toolPath;
        {
          final CARS_ToolInterface ti = CARS_ToolsFactory.getTool( mMain, n, null, true );        
          final Node toolNode = ti.getTool();
          synchronized( WRITERACCESS ) {
            toolNode.addMixin( "jecars:mixin_unstructured" );
            save();
          }
          toolPath = toolNode.getPath();

          // **** Copy data nodes to the root of the new workflow
          {
            final List<Node> nl = getContext().getDataNodes();
            for( final Node dataNode : nl ) {
              if (!toolNode.hasNode( dataNode.getName() )) {
                synchronized( WRITERACCESS ) {
                  toolNode.getSession().move( dataNode.getPath(), toolNode.getPath() + "/" + dataNode.getName() );
                }
              }
            }
          }
          {
            // **** Copy parameter nodes to the root of the new workflow
            final List<Node> nl = getContext().getParameterNodes();
            for( final Node dataNode : nl ) {
              if (!toolNode.hasNode( dataNode.getName() )) {
                synchronized( WRITERACCESS ) {
                  toolNode.getSession().move( dataNode.getPath(), toolNode.getPath() + "/" + dataNode.getName() );
                }
              }
            }
          }
          
          // *******************************************************************
          // **** Master Q
          WF_MasterQData masterQData = new WF_MasterQData();
          if (getWorkflow().getConfig()!=null) {
            boolean masterQ = CARS_Utils.getPropertyValueBoolean( getWorkflow().getConfig(), "Par_MasterQ", false );
            if (masterQ) {
              // **** Get Root Tool
              Node rootTool = ti.getRootTool();
              if (rootTool.hasNode( "Par_MasterQ")) {
                masterQData.fill( rootTool.getNode( "Par_MasterQ" ));
              }            
            }
          }
          if (masterQData.masterQ().isEmpty()) {
            
            // *****************************************************************
            // **** Normal default start and wait
            synchronized( WRITERACCESS ) {
              toolNode.getSession().save();
              ti.setStateRequest( CARS_ToolInterface.STATEREQUEST_START );
            }
            // **** Wait until ready
            final Future futureres = ti.getFuture();
            futureres.get();
            
          } else {
            // **** Close toolinstance session, not needed because masterQ will take over
            ti.getMain().destroy();
            
            // *****************************************************************
            // **** Run with MasterQ control            
            JD_Taglist linkTags = new JD_Taglist();
            linkTags.putData( "$0.link", "true" );            
            linkTags.putData( "$0.link.href", mMain.getContext().getBaseContextURL() + newWF.getPath() );
            linkTags.putData( "$0.link.rel", "via" );
            linkTags.putData( "jcr:primaryType", newWF.getNode().getPrimaryNodeType().getName() );
            Node masterWF = mMain.addNode( masterQData.masterQ() + "/runners/Main/context/Q/AddWorkflows/" + newWF.getNode().getName(),
                            linkTags, null, null );
            // **** To the running queue
            linkTags.clear();
            linkTags.putData( "jcr:primaryType", masterWF.getPrimaryNodeType().getName() );
            masterWF = mMain.addNode( masterQData.masterQ() + "/runners/Main/context/Q/RunningWorkflows/" + masterWF.getName(),
                             linkTags, null, null );
            // **** Wait for result
            boolean wait = true;
            while( wait ) {
              final String state = newWF.getToolInterface().getState();
              wait = state.startsWith( CARS_ToolInterface.STATE_OPEN );
              Thread.sleep( 250 );
            }
          }
        }
        
        // **** Empty context        
        final IWF_Context context = getContext();
        synchronized( WRITERACCESS ) {
          context.clear();
        }

        // **** Check for the result of the workflow.
        final String state = newWF.getToolInterface().getState();
        if (CARS_ToolInterface.STATE_CLOSED_COMPLETED.equals(state)) {
          // **** Tool has finished OK
          res.setState( WFP_InterfaceResult.STATE.OK );
        } else {
          // **** Tool has finished not ok
          res.setState( WFP_InterfaceResult.STATE.ERROR );          
        }

        
        final Node thisTool = getNode().getSession().getNode( toolPath );
        final NodeIterator ni = thisTool.getNodes();
        while( ni.hasNext() ) {
          final Node tnode = ni.nextNode();
          if ((!tnode.isNodeType("jecars:permissionable")) &&
              (!tnode.isNodeType("jecars:configresource")) &&
              (!tnode.isNodeType("jecars:EventsFolder")) &&
              (!tnode.isNodeType("jecars:EventsStoreFolder")) &&
              (!tnode.isNodeType("jecars:mixin_WorkflowRunners")) &&
              (!tnode.isNodeType("jecars:mixin_workflowtasks")) &&
              (!tnode.isNodeType("jecars:mixin_workflowlinks"))) {
            synchronized( WRITERACCESS ) {
              final Node nn;
              if (tnode.isNodeType( "jecars:parameterresource" )) {
//                nn = context.getNode().addNode( tnode.getName(), "jecars:parameterresource" );                
                final Node resparaN = CARS_Utils.getLinkedNode(tnode);
                final String paramn = context.getNode().getPath() + "/" + resparaN.getName();
                if (context.getNode().hasNode( resparaN.getName() )) {
                  context.getNode().getNode( resparaN.getName() ).remove();
                }
                ws.copy( resparaN.getPath(), paramn );
              } else {
                if (context.getNode().hasNode( tnode.getName() )) {
                  context.getNode().getNode( tnode.getName() ).remove();
                }
                nn = context.getNode().addNode( tnode.getName(), "jecars:inputresource" );
                nn.setProperty( "jcr:mimeType", "" );
                nn.setProperty( "jcr:data", "" );
                nn.setProperty( "jcr:lastModified", Calendar.getInstance() );
                nn.addMixin(    "jecars:mix_link" );
                nn.setProperty( "jecars:Link" , tnode.getPath() );
//                nn.setProperty( "jecars:Link" , CARS_Utils.getLinkedNode(tnode).getPath() );
              }
              save();
            }
          }
        
        }
        // **** TODO why?????
//        synchronized( WRITERACCESS ) { 
//          // **** Copy event tree as part of the context
//          if (thisTool.hasNode( "jecars:Events" )) {
//            thisTool.getSession().getWorkspace().copy(
//                  thisTool.getNode( "jecars:Events" ).getPath(),
//                  context.getNode().getPath() + "/jecars:Events" );
//          }        
//          save();
//        }

        break;
      }
        
      // ***************
      // **** RUN TASK
      case TASK: {        
        if (mRerunMode) {
          if (getNode().hasNode( "Tool_" + getStepNumber() )) {
            final Node tool = getNode().getNode( "Tool_" + getStepNumber() );
            if (tool.getProperty( "jecars:State" ).getString().equals( "open.rerun" )) {
              // **** Start the run from this point
              mRerunMode = false;
            }
          }
          if (mRerunMode) break;
        }
        handleContextParameters( pTask );
        // **** Remove old tool node if available
        if (getNode().hasNode( "Tool_" + getStepNumber() )) {
          getNode().getNode( "Tool_" + getStepNumber() ).remove();
        }
        final Node ttn = pTask.getToolTemplateNode();
        final Node tool;
        synchronized( WRITERACCESS ) {
          tool = getNode().addNode( "Tool_" + getStepNumber(), "jecars:Tool" );
          tool.setProperty( "jecars:ParentTool", getWorkflow().getNode().getPath() );
          tool.setProperty( "jecars:ToolTemplate", ttn.getPath() );
          tool.addMixin( "jecars:interfaceclass" );
          tool.setProperty( "jecars:InterfaceClass", "org.jecars.tools.CARS_ToolInterfaceApp" );
          save();
          // **** Copy the parameters
          NodeIterator ni = pTask.getNode().getNodes();
          while( ni.hasNext() ) {
            Node pn = ni.nextNode();
            if (pn.isNodeType( "jecars:parameterresource" )) {
              tool.getSession().getWorkspace().copy( pn.getPath(), tool.getPath() + "/" + pn.getName() );
              final Node prn = tool.getNode( pn.getName() );
              // **** Indicate that this parameter is temporary and should not be copied to future contexts.
              prn.addMixin( "jecars:mix_temporaryresource" );
            }
          }
          save();
        }

//        final CARS_ActionContext ac = CARS_ActionContext.createActionContext( mMain.getContext(0) );
        final Node n = mMain.getSession().getNode( tool.getPath() );
        final String toolPath;
        {
          final CARS_ToolInterface ti = CARS_ToolsFactory.getTool( mMain, n, null, true );        
          final Node toolNode = ti.getTool();
          toolPath = toolNode.getPath();

          final List<Node> nl = getContext().getDataNodes();
          synchronized( WRITERACCESS ) {
            // **** Copy the datanodes
            for( final Node dataNode : nl ) {
              toolNode.getSession().move( dataNode.getPath(), toolNode.getPath() + "/" + dataNode.getName() );
            }
            final List<Node> pnl = getContext().getParameterNodes();
            // **** Copy the parametersnodes
            for( final Node paramNode : pnl ) {
              if (toolNode.hasNode( paramNode.getName() )) {
                toolNode.getNode( paramNode.getName() ).remove();
              }
              toolNode.getSession().move( paramNode.getPath(), toolNode.getPath() + "/" + paramNode.getName() );              
            }            
            toolNode.getSession().save();        
            ti.setStateRequest( CARS_ToolInterface.STATEREQUEST_START );
          }
          // **** Wait until ready
          final Future futureres = ti.getFuture();
          res.replaceBy( (WFP_InterfaceResult)futureres.get() );
          }
//  System.out.println("RUNTASK 5 time: " + System.currentTimeMillis() );
        
        // **** Empty context        
        IWF_Context context = getContext();
        synchronized( WRITERACCESS ) {
          context.clear();
        }
        
        final Node thisTool = getNode().getSession().getNode( toolPath );
        // **** Check for the result of the tool.
        final String state = thisTool.getProperty( "jecars:State" ).getString();
        if (CARS_ToolInterface.STATE_CLOSED_COMPLETED.equals(state)) {
          // **** Tool has finished OK
          res.setState( WFP_InterfaceResult.STATE.OK );
        } else {
          // **** Tool has finished not ok
          res.setState( WFP_InterfaceResult.STATE.ERROR );          
        }
        
        final Workspace ws = getNode().getSession().getWorkspace();
        final String toPath = context.getNode().getPath();
        final NodeIterator ni = thisTool.getNodes();
        synchronized( WRITERACCESS ) {        
          if (thisTool.hasProperty( "jecars:ExpireDate" )) {
            thisTool.setProperty( "jecars:ExpireDate", (Calendar)null );
          }
          while( ni.hasNext() ) {
            final Node tnode = ni.nextNode();
//          ws.copy( tnode.getPath(), toPath + "/" + tnode.getName() );
            // **** Don't copy the jecars:mix_temporaryresource nodes
            if (!tnode.isNodeType( "jecars:mix_temporaryresource" )) {
              // **** Check if the node must be copied to the current context
              if (tnode.isNodeType( "jecars:parameterdata" )) {
                // **** Copy parameter as normal objects v4.1.1
                ws.copy( tnode.getPath(), toPath + "/" + tnode.getName() );              
              } else if ((!"jecars:Events".equals(tnode.getName())) &&
                         (!"jecars:Config".equals(tnode.getName()))) {
                final Node nn = context.getNode().addNode( tnode.getName(), "jecars:root" );
                nn.addMixin( "jecars:mix_link" );
                nn.addMixin( "jecars:mix_inputresource" );
                nn.setProperty( "jecars:Link" , tnode.getPath() );
  //              nn.setProperty( "jecars:Link" , CARS_Utils.getLinkedNode(tnode).getPath() );
              }
            }
          }
          save();
        }
        break;
      }
    }
    return res;
  }
    
  @Override
  public boolean isNULL() {
    return this==NULL;
  }

  /** setProgress
   * Does a save at the end
   * @param pProgress
   * @throws RepositoryException 
   */
  @Override
  public void setProgress( final double pProgress ) throws RepositoryException {
    synchronized( WRITERACCESS ) {
      getNode().setProperty( "jecars:Progress", pProgress );
      if (pProgress>=1) {
        getNode().setProperty( "jecars:Ended", Calendar.getInstance() );
      } else if (pProgress<=0) {
        getNode().setProperty( "jecars:Started", Calendar.getInstance() );      
        getNode().setProperty( "jecars:Ended", (Calendar)null );
      }
      save();
    }
    return;
  }

  /** setState
   * 
   * @param pState
   * @throws RepositoryException 
   */
  @Override
  public void setState( final String pState ) throws RepositoryException {
    synchronized( WRITERACCESS ) {
      getNode().setProperty( "jecars:State", pState );
      save();
    }
    return;
  }
  
  /** getState
   * 
   * @return
   * @throws RepositoryException 
   */
  public String getState() throws RepositoryException {
    return getNode().getProperty( "jecars:State" ).getString();
  }

  @Override
  public String toString() {
    return getPath();
  }
  
  
  
}
