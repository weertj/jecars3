/*
 * Copyright 2009-2011 NLR - National Aerospace Laboratory
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_DefaultNode;
import org.jecars.client.JC_DefaultStream;
import org.jecars.client.JC_Defs;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Filter;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Params;
import org.jecars.client.JC_Propertyable;
import org.jecars.client.JC_Query;
import org.jecars.client.JC_RESTComm;
import org.jecars.client.JC_Streamable;

/** JC_ToolNode (Tracker ID: 2612650)
 *
 * @version $Id: JC_ToolNode.java,v 1.8 2009/07/30 12:03:26 weertj Exp $
 */
public class JC_ToolNode extends JC_DefaultNode {

  final static public String STATEREQUEST_START     = "start";
//  final static public String STATEREQUEST_ABORT = "abort";
  final static public String STATEREQUEST_RESUME    = "resume";
  final static public String STATEREQUEST_PAUSE     = "pause";
  final static public String STATEREQUEST_STOP      = "stop";
  final static public String STATEREQUEST_RERUN     = "rerun";

  final static public String STATE_NONE                                = "none";
  final static public String STATE_UNKNOWN                             = "unknown";
  final static public String STATE_OPEN                                = "open.";
  final static public String STATE_RUNNING                             = ".running";
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
    
  
  /** start
   * 
   * @throws org.jecars.client.JC_Exception
   */
  public void start() throws JC_Exception {
    setProperty( "jecars:StateRequest", STATEREQUEST_START );
    save();
    return;
  }

  public void rerun() throws JC_Exception {
    setProperty( "jecars:StateRequest", STATEREQUEST_RERUN );
    save();
    return;
  }

  /** pause
   * 
   * @throws org.jecars.client.JC_Exception
   */
  public void pause() throws JC_Exception {
    setProperty( "jecars:StateRequest", STATEREQUEST_PAUSE );
    save();
    return;
  }

  public void resume() throws JC_Exception {
    setProperty( "jecars:StateRequest", STATEREQUEST_RESUME );
    save();
    return;
  }

  /** stop
   * 
   * @throws org.jecars.client.JC_Exception
   */
  public void stop() throws JC_Exception {
    setProperty( "jecars:StateRequest", STATEREQUEST_STOP );
    save();
    return;
  }

  /** abort
   *
   * @throws org.jecars.client.JC_Exception
   */
  public void abort() throws JC_Exception {
    stop();
    stop();
    return;
  }

  /** sendSignal
   *
   * @param pSignal
   * @throws JC_Exception
   */
  public void sendSignal( final JC_ToolSignal pSignal ) throws JC_Exception {
    final JC_Propertyable prop = setProperty( "jecars:LastToolSignal", pSignal.name() );
    prop.setChanged( true );
    save();
    return;
  }

  /** getPercCompleted
   * 
   * @return
   * @throws JC_Exception 
   */
  public double getPercCompleted() throws JC_Exception {
    return getProperty( "jecars:PercCompleted" ).getValueAsDouble();
  }
  
  /** getState
   * 
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public String getState() throws JC_Exception {
    refresh();
    return getProperty( "jecars:State" ).getValueString();
  }

  public JC_WorkflowNode getToolTemplate() throws JC_Exception {
    if (hasProperty( "jecars:ToolTemplate" )) {
      final String tt = getProperty( "jecars:ToolTemplate" ).getValueString();
      return (JC_WorkflowNode)getClient().getNode( tt ).morphToNodeType();
    } else {
      return null;
    }
  }

  
  /** isRunning
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public boolean isRunning() throws JC_Exception {
    final String state = getState();
    if (state.indexOf( STATE_RUNNING )==-1) {
      return false;
    }
    return true;
  }

  /** getToolEvents
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   * @throws java.io.UnsupportedEncodingException
   */
  public ArrayList<JC_ToolEventNode> getToolEvents() throws JC_Exception, UnsupportedEncodingException, CloneNotSupportedException {
    final ArrayList<JC_ToolEventNode> events = new ArrayList<JC_ToolEventNode>();
    final JC_Clientable client = getClient();
    final JC_Params params = client.createParams( JC_RESTComm.GET ).cloneParams();
//    params = (JC_Params)params.clone();
    params.setDeep( true );
    params.setOutputFormat( JC_Defs.OUTPUTTYPE_PROPERTIES );
    params.setIncludeBinary( true );
    params.setAllProperties( true );
    final JC_Filter filter = JC_Filter.createFilter();
    filter.addCategory( "jecars:ToolEvent" );
    final JC_Query query = JC_Query.createQuery();
    query.setOrderByString( "jcr:created" );
    final Collection<JC_Nodeable> nodes = getNodes( params, filter, query );
    for (JC_Nodeable node : nodes) {
      events.add( (JC_ToolEventNode)node.morphToNodeType() );
    }
    return events;
  }


  /** addInput
   *
   * @param pName
   * @param pType
   * @param pMimeType
   * @param pData
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public JC_Nodeable addInput( final String pName, final String pType, final String pMimeType, final String pData ) throws JC_Exception {
    final JC_Nodeable inputN = addNode( pName, pType );
    inputN.setProperty( "jcr:mimeType", "text/plain" ); // TODO ???
    inputN.setProperty( "jecars:Title", pName );
    inputN.setProperty( "jcr:data", pData );
    inputN.save();
    return inputN;
  }

  /** addInputLink
   * 
   * @param pType
   * @param pInput
   * @return
   * @throws JC_Exception 
   */
  public JC_Nodeable addInputLink( final String pType, final JC_Nodeable pInput ) throws JC_Exception {
    final JC_Nodeable inputN = addNode( pInput.getName(), pType );    
    inputN.setProperty( "jcr:mimeType", pInput.getProperty( "jcr:mimeType" ).getValueString() );
    inputN.setProperty( "jecars:Title", pInput.getName() );
    inputN.setProperty( "jcr:data", "" );
    inputN.save();
    inputN.addMixin( "jecars:mix_link" );
    inputN.save();
    inputN.setProperty( "jecars:Link", pInput.getPath() );
    return inputN;    
  }


  /** addInput
   *
   * @param pName
   * @param pType
   * @param pMimeType
   * @param pData
   * @return
   * @throws JC_Exception
   */
  public JC_Nodeable addInput( final String pName, final String pType, final String pMimeType, final InputStream pData ) throws JC_Exception {
    final JC_Nodeable inputN = addNode( pName, pType );
    inputN.setProperty( "jcr:mimeType", pMimeType );
    inputN.setProperty( "jecars:Title", pName );
    final JC_Streamable stream = JC_DefaultStream.createStream( pData, pMimeType );
    inputN.setProperty( "jcr:data", stream );
    inputN.save();
    return inputN;
  }


  /** addParameter
   *
   * @param pName
   * @param pType
   * @param pMimeType
   * @param pData
   * @return
   * @throws JC_Exception
   */
  public JC_Nodeable addParameter( final String pName, final String pType, final String pMimeType, final String pData ) throws JC_Exception {
    final JC_Nodeable paramN = addNode( pName, pType );
    paramN.setProperty( "jcr:mimeType", pMimeType );
    paramN.setProperty( "jecars:Title", pName );
    paramN.setProperty( "jcr:data", pData );
    paramN.save();
    return paramN;
  }

  /** addConfig
   * 
   * @return
   * @throws JC_Exception 
   */
  public JC_Nodeable addConfig() throws JC_Exception {
    final JC_Nodeable config = addNode( "jecars:Config", "jecars:ToolConfig" );
    config.setProperty( "jecars:RunningExpireMinutes", JC_Params.UNSTRUCT_PREFIX_LONG + "-1" );
    config.setProperty( "jecars:ClosedExpireMinutes",  JC_Params.UNSTRUCT_PREFIX_LONG + "60" );
    config.addNode( "WorkDirectory", "jecars:datafolder" );
    return config;
  }
  
  /** getConfig
   * 
   * @return
   * @throws JC_Exception 
   */
  public JC_Nodeable getConfig() throws JC_Exception {
    return getNode( "jecars:Config" );
  }
  
  /** setRunningExpireMinutes
   * 
   * @param pNo
   * @throws JC_Exception 
   */
  public void setRunningExpireMinutes( int pNo ) throws JC_Exception {
    final JC_Nodeable config = getNode( "jecars:Config" );
    config.setProperty( "jecars:RunningExpireMinutes", pNo );
    return;
  }

  /** setClosedExpireMinutes
   * 
   * @param pNo
   * @throws JC_Exception 
   */
  public void setClosedExpireMinutes( int pNo ) throws JC_Exception {
    final JC_Nodeable config = getNode( "jecars:Config" );
//    config.setProperty( "jecars:ClosedExpireMinutes", JC_Params.UNSTRUCT_PREFIX_LONG + pNo );
    config.setProperty( "jecars:ClosedExpireMinutes", pNo );
    return;
  }

  /** addDocs
   * 
   * @return
   * @throws JC_Exception 
   */
  public JC_datafolderNode addDocs() throws JC_Exception {
    final JC_Nodeable docs = addNode( "jecars:Docs", "jecars:datafolder" );
    return (JC_datafolderNode)docs.morphToNodeType();
  }

  
  /** addConfigExternalTool
   * 
   * @param pExecPath
   * @return
   * @throws JC_Exception
   */
  public JC_Nodeable addConfigExternalTool( final String pWorkingDirectory, final String pExecPath, final boolean pGenerateUniqueWorkingDirectory ) throws JC_Exception {
    final JC_Nodeable config = addNode( "jecars:Config", "jecars:ToolConfig" );
    config.setProperty( "jecars:RunningExpireMinutes", JC_Params.UNSTRUCT_PREFIX_LONG + "-1" );
    config.setProperty( "jecars:ClosedExpireMinutes",  JC_Params.UNSTRUCT_PREFIX_LONG + "60" );
    config.setProperty( "jecars:ExecPath", pExecPath );
    if (pWorkingDirectory!=null) {
      config.setProperty( "jecars:WorkingDirectory", pWorkingDirectory );
      config.setProperty( "jecars:GenerateUniqueWorkingDirectory", pGenerateUniqueWorkingDirectory );
    }
    config.save();
    return config;
  }

  /** addConfigUploadExternalTool
   * 
   * @param pWorkingDirectory
   * @param pExecPath
   * @param pGenerateUniqueWorkingDirectory
   * @return
   * @throws JC_Exception
   * @throws FileNotFoundException 
   */
  public JC_Nodeable addConfigUploadExternalTool( final String pWorkingDirectory, final String pExecPath, final boolean pGenerateUniqueWorkingDirectory ) throws JC_Exception, FileNotFoundException {
    final File exeFile = new File( pExecPath );
    if (exeFile.exists()) {
      final JC_Nodeable config = addConfig();
      config.setProperty( "jecars:ExecPath", pExecPath );
      if (pWorkingDirectory!=null) {
        config.setProperty( "jecars:WorkingDirectory", pWorkingDirectory );
        config.setProperty( "jecars:GenerateUniqueWorkingDirectory", pGenerateUniqueWorkingDirectory );
      }
      config.save();

      final FileInputStream  fis = new FileInputStream( exeFile );
      final JC_Nodeable  exeNode = addNode( exeFile.getName(), "jecars:parameterresource" );
      final JC_Streamable stream = JC_DefaultStream.createStream( fis, "application/x-exe");
      stream.setContentLength( exeFile.length() );
      exeNode.setProperty(stream);
      exeNode.save();

      return config;
    } else {
      throw new JC_Exception( "Tool " + pExecPath + " not found" );
    }
  }

  /** setAutoStartParameters
   * 
   * @param pDefaultInstancePath
   * @param pAutoRunWhenInput
   * @throws JC_Exception
   */
  public void setAutoStartParameters( final String pDefaultInstancePath, final String pAutoRunWhenInput ) throws JC_Exception {
    if (pDefaultInstancePath!=null) {
      setProperty( "jecars:DefaultInstancePath", pDefaultInstancePath );
    }
    if (pAutoRunWhenInput!=null) {
      setProperty( "jecars:AutoRunWhenInput", pAutoRunWhenInput );
    }
    return;
  }

  /** asCommandLineOption
   * 
   * @param pNode
   * @param pPriority
   * @return
   * @throws JC_Exception 
   */
  public JC_Nodeable asCommandLineOption( final JC_Nodeable pNode, final long pPriority ) throws JC_Exception {
    pNode.addMixin( "jecars:mix_commandlineitem" );
    pNode.save();
    pNode.addMixin( "jecars:mix_inputresource" );
    pNode.save();
    pNode.setProperty( "jecars:Priority", pPriority );
    pNode.save();
    return pNode;
  }
  
  /** addParameterData
   *
   * @param pName
   * @return
   * @throws java.lang.Exception
   */
  public JC_ParameterDataNode addParameterData( final String pName ) throws JC_Exception {
    final JC_Nodeable pn = addNode( pName, "jecars:parameterdata" );
    pn.save();
    return (JC_ParameterDataNode)pn.morphToNodeType();
  }

  /** getInputs
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public Collection<JC_Nodeable> getInputs() throws JC_Exception {
    final JC_Filter f = new JC_Filter();
    f.setNamePattern( "jecars:Input" );
    return getNodes( null, f, null );
  }

  /** getOutputs
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public Collection<JC_Nodeable> getOutputs() throws JC_Exception {
    final JC_Filter f = new JC_Filter();
    f.setNamePattern( "jecars:Output" );
    return getNodes( null, f, null );
  }

  /** getOutputResources
   * 
   * @return
   * @throws JC_Exception
   */
  public Collection<JC_Nodeable> getOutputResources() throws JC_Exception {
    final JC_Filter f = new JC_Filter();
    f.addCategory( "jecars:outputresource" );
    return getNodes( null, f, null );
  }

  /** getOutputResourcesMix
   * 
   * @return
   * @throws JC_Exception 
   */
  public Collection<JC_Nodeable> getOutputResourcesMix() throws JC_Exception {
    final JC_Filter f = new JC_Filter();
    f.addCategory( "jecars:mix_outputresource" );
    return getNodes( null, f, null );
  }

  /** isStopped
   *
   * @return
   * @throws JC_Exception
   */
  public boolean isStopped() throws JC_Exception {
    return getState().startsWith( JC_ToolNode.STATE_CLOSED );
  }


  public List<JC_Nodeable> waitForEvents() throws JC_Exception {
    final List<JC_Nodeable> events = new ArrayList<JC_Nodeable>();
    refresh();
    JC_Params p = getClient().createParams( JC_RESTComm.GET );
    p.setLongPoll( true );
    events.addAll( getNodeList() );
    return events;
  }
  
  /** createTool
   *
   * @param pParentNode
   * @param pTemplateTool
   * @param pToolName
   * @param pToolUser
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_ToolNode createTool( final JC_Nodeable pParentNode, final JC_ToolNode pTemplateTool, final String pToolName, final JC_UserNode pToolUser ) throws JC_Exception {
    return createTool( pParentNode, pTemplateTool.getPath(), pToolName, pToolUser );
  }

  /** createTool
   * 
   * @param pParentNode
   * @param pTemplateTool
   * @param pToolName
   * @param pToolUser
   * @param pExpireMinutes
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_ToolNode createTool( final JC_Nodeable pParentNode, final JC_ToolNode pTemplateTool, final String pToolName, final JC_UserNode pToolUser, final int pExpireMinutes ) throws JC_Exception {
    return createTool( pParentNode, pTemplateTool.getPath(), pToolName, pToolUser, pExpireMinutes );
  }

  /** createTool
   * 
   * @param pParentNode
   * @param pTemplateTool
   * @param pToolName
   * @param pToolUser
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_ToolNode createTool( final JC_Nodeable pParentNode, final String pTemplateTool, final String pToolName, final JC_UserNode pToolUser ) throws JC_Exception {
    return createTool( pParentNode, pTemplateTool, pToolName, pToolUser, 30 );
  }

  /** createTool
   * 
   * @param pParentNode
   * @param pTemplateTool
   * @param pToolName
   * @param pToolUser
   * @param pExpireMinutes
   * @return
   * @throws JC_Exception
   */
  static public JC_ToolNode createTool( final JC_Nodeable pParentNode, final String pTemplateTool, final String pToolName, final JC_UserNode pToolUser, final int pExpireMinutes ) throws JC_Exception {
    return createTool( pParentNode, pTemplateTool, pToolName, pToolUser, pExpireMinutes, "jecars:Tool" );
  }

  /** createToolInterface
   * 
   * @param pParentNode
   * @param pTemplateTool
   * @param pToolName
   * @param pToolUser
   * @param pExpireMinutes
   * @param pToolType
   * @return
   * @throws JC_Exception 
   */
  static public JC_ToolNode createToolInterface( final JC_Nodeable pParentNode, final String pTemplateTool,
                    final String pToolName, final JC_UserNode pToolUser, final int pExpireMinutes,
                    final String pToolType ) throws JC_Exception {
    final JC_ToolNode tool = createTool( pParentNode, pTemplateTool, pToolName, pToolUser, pExpireMinutes, pToolType );
    tool.addMixin( "jecars:interfaceclass" );
    tool.save();
    tool.setProperty( "jecars:InterfaceClass", "org.jecars.tools.CARS_ToolInterfaceApp" );
    tool.save();
    return tool;
  }
  
  /** createTool
   *
   * @param pParentNode
   * @param pTemplateTool
   * @param pToolName
   * @param pToolUser if null then no permissions are created
   * @param pExpireMinutes
   * @param pToolType
   * @return
   * @throws JC_Exception
   */
  static public JC_ToolNode createTool( final JC_Nodeable pParentNode, final String pTemplateTool,
                    final String pToolName, final JC_UserNode pToolUser, final int pExpireMinutes,
                    final String pToolType ) throws JC_Exception {
    final JC_ToolNode tool = (JC_ToolNode)pParentNode.addNode( pToolName, pToolType ).morphToNodeType();
    tool.setProperty( "jecars:ToolTemplate", pTemplateTool );
    if (pToolUser!=null) {
      tool.addMixin( "jecars:permissionable" );
      JC_PermissionNode.addRights( tool, pToolUser, JC_PermissionNode.RS_ALLRIGHTS );
    }
    if (pExpireMinutes>0) {
      final Calendar c = Calendar.getInstance();
      c.add( Calendar.MINUTE, pExpireMinutes );
      tool.setExpireDate( c );
    }
    tool.save();
    if (pToolUser!=null) {
      final JC_PermissionNode perm = (JC_PermissionNode)tool.addNode( "P_" + pToolUser.getUsername(), "jecars:Permission" ).morphToNodeType();
      perm.addRights( pToolUser, JC_PermissionNode.RS_ALLRIGHTS );
      perm.save();
    }
    return tool;
  }


  /** createTemplateTool
   *
   * @param pParentNode
   * @param pName
   * @param pServerClass
   * @param pStoreEvents
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_ToolNode createTemplateTool( JC_Nodeable pParentNode, final String pName, final String pServerClass, final boolean pStoreEvents ) throws JC_Exception {
    JC_Nodeable tool = pParentNode.addNode( pName, "jecars:Tool" );
    tool.setProperty( "jecars:ToolClass", pServerClass );
    tool.setProperty( "jecars:StoreEvents", pStoreEvents );
    tool.save();
    return (JC_ToolNode)tool.morphToNodeType();
  }

  /** createTemplateTool
   * 
   * @param pParentNode
   * @param pName
   * @param pServerClass
   * @param pStoreEvents
   * @param pReplaceEvents
   * @param pIsScheduled
   * @param pDelayInSecs
   * @param pIsSingle
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_ToolNode createTemplateTool( final JC_Nodeable pParentNode, final String pName, final String pServerClass, final boolean pStoreEvents,
                                        final boolean pReplaceEvents, final boolean pIsScheduled, final long pDelayInSecs, final boolean pIsSingle ) throws JC_Exception {
    final JC_Nodeable tool = pParentNode.addNode( pName, "jecars:Tool" );
    tool.setProperty( "jecars:ToolClass", pServerClass );
    tool.setProperty( "jecars:StoreEvents", pStoreEvents );
    tool.setProperty( "jecars:ReplaceEvents", pReplaceEvents );
    tool.setProperty( "jecars:IsScheduled", pIsScheduled );
    tool.setProperty( "jecars:DelayInSecs", pDelayInSecs );
    tool.setProperty( "jecars:IsSingle", pIsSingle );
    tool.save();
    return (JC_ToolNode)tool.morphToNodeType();
  }


}
