/*
 * Copyright 2007-2011 NLR - National Aerospace Laboratory
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

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_Main;

/**
 *  CARS_ToolInterface
 * 
    [jecars:Tool] > jecars:dataresource
    - jecars:ToolClass      (String)
    - jecars:StateRequest   (String) < '(start|suspend|stop)'
    - jecars:State          (String)='open.notrunning' mandatory autocreated
    - jecars:PercCompleted  (Double)='0'
    + jecars:Parameter      (jecars:dataresource) multiple
    + jecars:Input          (jecars:dataresource) multiple
    + jecars:Output         (jecars:dataresource) multiple

 * 
 * @version $Id: CARS_ToolInterface.java,v 1.17 2009/05/26 09:21:09 weertj Exp $
 */
public interface CARS_ToolInterface {

  final static String LOCALE = "locale:";
    
  final static String STATEREQUEST_START    = "start";
  final static String STATEREQUEST_ABORT    = "abort";
  final static String STATEREQUEST_PAUSE    = "pause";
  final static String STATEREQUEST_RESUME   = "resume";
  final static String STATEREQUEST_STOP     = "stop";
  final static String STATEREQUEST_RERUN    = "rerun";

  final static String STATE_NONE                                = "none";
  final static String STATE_UNKNOWN                             = "unknown";
  final static String STATE_OPEN                                = "open.";
  final static String STATE_OPEN_NOTRUNNING                     = "open.notrunning";
  final static String STATE_OPEN_NOTRUNNING_SUSPENDED           = "open.notrunning.suspended";
  final static String STATE_OPEN_RUNNING_INIT                   = "open.running.init";
  final static String STATE_OPEN_RUNNING_INPUT                  = "open.running.input";
  final static String STATE_OPEN_RUNNING_PARAMETERS             = "open.running.parameters";
  final static String STATE_OPEN_RUNNING_OUTPUT                 = "open.running.output";
  final static String STATE_OPEN_RUNNING                        = "open.running";
  final static String STATE_OPEN_RUNNING_RERUN                  = "open.running.rerun";
  final static String STATE_PAUSED                              = ".paused";
  final static String STATE_OPEN_ABORTING                       = "open.aborting";
  final static String STATE_CLOSED                              = "closed.";
  final static String STATE_CLOSED_COMPLETED                    = "closed.completed";
  final static String STATE_CLOSED_COMPLETED_SCHEDULED          = "closed.completed.scheduled";
  final static String STATE_CLOSED_ABNORMALCOMPLETED            = "closed.abnormalCompleted";
  final static String STATE_CLOSED_ABNORMALCOMPLETED_TERMINATED = "closed.abnormalCompleted.terminated";
  final static String STATE_CLOSED_ABNORMALCOMPLETED_ABORTED    = "closed.abnormalCompleted.aborted";
  
  
  /** Returns an unique identifier on this system.
   * @return the unique identifier. 
   */
  public String getUUID();

  void setThreadPriority( final int pP );
  int  getThreadPriority();
  
  /** Set the tool node, when the tool has ended the CARS_Main context must be destroyed
   * @param pMain the CARS_Main context
   * @param pTool the tool
   * @throws Exception when an error occurs
   */  
  public void   setTool( CARS_Main pMain, Node pTool ) throws Exception;

  /** Get the tool node
   * @return the tool
   * @throws Exception when an error occurs
   */
  Node   getTool() throws Exception;
  
  /** getParentTool
   * 
   * @param pToolNode retrieved by getTool()
   * @return
   * @throws RepositoryException 
   */
  Node getParentTool( Node pToolNode ) throws RepositoryException;
  
  Node   getRootTool() throws RepositoryException;
  
  /** getToolTemplatePath
   * 
   * @return
   */
  public String getToolTemplatePath() throws RepositoryException;

  /** Set the username and password for the tool
   * @param pUsername
   * @param pPassword
   */
  public void setCredentials( String pUsername, char[] pPassword );
  
  /** Set authentication string
   * 
   * @param pAuth
   */
  public void setCredentialAuth( String pAuth );
  
  /** Get the name of the tool
   * @return the name
   * @throws java.lang.Exception
   */
  public String  getName() throws Exception;
  
  /** Get the title of the tool (human readable)
   * @return the title
   * @throws java.lang.Exception
   */
  public String getTitle() throws Exception;
  
  /** Set the name of the tool
   * @param pName
   * @throws java.lang.Exception
   */
  public void setName( String pName ) throws Exception;
  
  /** Get the tool thread object
   * @return
   */
//  public CARS_DefaultToolInterface.ToolThread getToolThread();
  
  /** Set the current staterequest of the tool
   * @param pStateRequest STATEREQUEST_*
   * @return jecars:StateRequest property
   * @throws Exception when an error occurs
   */
  public Property setStateRequest( String pStateRequest ) throws Exception;

  /** Get the current staterequest of the tool
   * @return STATEREQUEST_*
   * @throws Exception when an error occurs
   */
  public String getStateRequest() throws Exception;

  /** Set the state at (after) which the tool execution must pause
   * @param pState Possible pause states which can be
   *               STATE_PAUSED        = Disable pause
   *               STATE_OPEN_RUNNING_INPUT = The tool is converting context data.
   */
  public void setPauseAtState( String pState );

  /** Set the state at which the tool must be interupted, the tool implementation itself must detect this
   * @param pState
   */
  public void setInterruptAtState( String pState );

  
  /** Get the current state of the tool
   * @return STATE_*
   * @throws Exception when an error occurs
   */
  public String getState() throws Exception;
  
  /** Get percentage completed for the tools
   * @return [0.0-1.0]
   * @throws Exception when an error occurs
   */
  public double getPercCompleted() throws Exception;

  /** Add the node in which the configuration data for this tool is stored
   * @param pNode JCR Node
   * @throws Exception when an error occurs
   */
  public void addConfigNode( Node pNode ) throws Exception;

  /** addToolArgument
   * 
   * @param pKey
   * @param pValue
   * @throws java.lang.Exception
   */
  public void addToolArgument( String pKey, String pValue ) throws Exception;
  
  /** getToolArgument
   * 
   * @param pKey
   * @return
   */
  public String getToolArgument( String pKey );
  
  /** Get the config node
   * @return the JCR node in which the configuration data for the tool is stored
   */
  public Node getConfigNode() throws Exception;

  
  /** Get an iterator with parameters nodes (jecars:Parameter.*)
   * @return the parameters
   * @throws Exception when an error occurs
   */
//  public NodeIterator getParameters() throws Exception;
  public List<Node> getParameters() throws Exception;

  /** getParameterString
   * 
   * @param pName
   * @param pIndex
   * @return
   * @throws java.lang.Exception
   */
  public String getParameterString( final String pName, final int pIndex ) throws Exception;
  
  /** setParameterString
   * 
   * @param pName
   * @param pIndex
   * @param pValue 
   */
  void setParameterString( final String pName, final int pIndex, final String pValue ) throws RepositoryException;

  /** getParameterStringIndex
   * 
   * @param pName
   * @param pValueRegex
   * @return 
   */
  int getParameterStringIndex( final String pName, final String pValueRegex );

  /** addInput
   * @param pInput
   * @throws java.lang.Exception
   */
  public void addInput( InputStream pInput ) throws Exception;

  /** addInput
   * 
   * @param pInput
   * @param pMimeType
   * @throws java.lang.Exception
   */
  public void addInput( InputStream pInput, String pMimeType ) throws Exception;
  
  /** Get an iterator with inputs nodes (jecars:Input.*)
   * @return the parameters
   * @throws Exception when an error occurs
   */
  @Deprecated
  public List<Node> getInputs()     throws Exception;

  /** Get an iterator with inputs nodes (jecars:mix_inputresource)
   * @return the inputs
   * @throws Exception when an error occurs
   */
  public List<Node> getMixInputs() throws RepositoryException;

  /** Get the input as objects in the collection as pObjectClass type
   * @param pObjectClass the class type of the resulting inputs
   * @param pParams optional taglist contains parameter for generating the output
   * @return Collection of object of class type pObjectClass or empty or null.
   * @throws Exception when an error occurs
   */
  public Collection<?> getInputsAsObject( Class pObjectClass, JD_Taglist pParamsTL ) throws Exception;

  /** addOutput
   * 
   * @param pOutput
   * @throws java.lang.Exception
   */
  Node addOutput( InputStream pOutput ) throws Exception;

  /** addOutput
   *
   * @param pOutput
   * @param pOutputName
   * @throws java.lang.Exception
   */
  Node addOutput( InputStream pOutput, String pOutputName ) throws Exception;

  /** Get an iterator with outputs nodes (jecars:Output.*)
   * @return the parameters
   * @throws Exception when an error occurs
   */
  public List<Node> getOutputs()    throws Exception;

  /** Get the output as objects in the collection as pObjectClass type
   * @param pObjectClass the class type of the resulting outputs
   * @param pParams optional taglist contains parameter for generating the output
   * @return Collection of object of class type pObjectClass or empty or null.
   * @throws Exception when an error occurs
   */
  public Collection<?> getOutputsAsObject( Class pObjectClass, JD_Taglist pParamsTL ) throws Exception;

  /** Get nodename as objects in the collection as pObjectClass type
   * @param pNodename e.g. jecars:Input or jecars:Output
   * @param pObjectClass the class type of the resulting outputs
   * @param pParams optional taglist contains parameter for generating the output
   * @return Collection of object of class type pObjectClass or empty or null.
   * @throws Exception when an error occurs
   */
  public Collection<?> getAsObject( String pNodename, Class pObjectClass, JD_Taglist pParamsTL ) throws Exception;

  
  /** Add a instance listener
   * @param pListener A new listener
   */
  public void addInstanceListener( CARS_ToolInstanceListener pListener );
  
  /** Remove a instance listener, if this listener isn't added the method returns quiet.
   * @param pListener A listener to be removed
   */
  public void removeInstanceListener( CARS_ToolInstanceListener pListener );
  
  
  CARS_ToolInstanceEvent reportException( final Throwable pThrow, final Level pLevel );
  CARS_ToolInstanceEvent reportExceptionEvent( final Throwable pThrow, final Level pLevel );

  /** Report progress
   * 
   * @param pProgress [0.0-1.0]
   * @throws java.lang.Exception
   */
  CARS_ToolInstanceEvent reportProgress( double pProgress ) throws Exception;

  /** Report output text to a standard output or log box.
   * @param pOutput Standard output text string
   */
  public void reportOutput( String pOutput );

  /** Report a message to the user.
   * @param pLevel the "level" of the message;
   *        Level.FINEST
   *        Level.FINER
   *        Level.FINE
   *        Level.CONFIG
   *        Level.INFO
   *        Level.WARNING
   *        Level.SERVERE
   *  @param pHtmlMessage the message itself, this message may contain html markups.
   *  @param pBlocking when true the program will continue after the user closes down the requester.
   *                   when false the program continue
   *                   (NOTE) currently only the true option is supported
   */
  CARS_ToolInstanceEvent reportMessage( java.util.logging.Level pLevel, String pHtmlMessage, boolean pBlocking );

  /** Report a message to the user.
   * @param pLevel the "level" of the message;
   *        Level.FINEST
   *        Level.FINER
   *        Level.FINE
   *        Level.CONFIG
   *        Level.INFO
   *        Level.WARNING
   *        Level.SERVERE
   *  @param pHtmlMessage the message itself, this message may contain html markups.
   *  @param pBlocking when true the program will continue after the user closes down the requester.
   *                   when false the program continue
   *                   (NOTE) currently only the true option is supported
   *   @return the event
   */
  CARS_ToolInstanceEvent reportMessageEvent( java.util.logging.Level pLevel, String pHtmlMessage, boolean pBlocking );

  /** Report a status to the user.
   *  @param pHtmlMessage the message itself, this message may contain html markups.
   */
  CARS_ToolInstanceEvent reportStatusMessage( String pHtmlMessage );

  /** Add an input node object
   * @param pNode
   * @throws java.lang.Exception
   */
  public void addInput( Node pNode ) throws Exception;
  
  /** getTranslatedString
   * Translate a string, is the string starts with "locale:" (without the "") (use CARS_ToolInterface.LOCALE)
   * the remaining part of the string will be used as key to be resolved against the
   * language resourcebundles
   * 
   * @param pString the to be translated string
   * @return resulting string
   */
  public String getTranslatedString(  String pString );

  /** sendOutputsAsBatch
   *
   * @return
   */
  public void sendOutputsAsBatch( int pNoLines );
 
  /** storeEvents
   * 
   * @return
   * @throws java.lang.Exception
   */
  public boolean storeEvents() throws Exception;

  /** replaceEvents
   *
   * @return
   * @throws java.lang.Exception
   */
  public boolean replaceEvents() throws Exception;

  /** isScheduledTool
   * 
   * @return true is the tool must be call every x {time}
   */
  public boolean isScheduledTool() throws Exception;

  /** isSingleTool
   * 
   * @return
   */
  public boolean isSingleTool() throws Exception;

  /** getDelayInSecs
   * 
   * @return
   */
  public long getDelayInSecs() throws Exception;

  /** getDefaultInstancePath
   * 
   * @return
   * @throws java.lang.Exception
   */
  public String getDefaultInstancePath() throws Exception;

  /** getAutoRunWhenInput
   *
   * @return
   * @throws java.lang.Exception
   */
  public String getAutoRunWhenInput() throws Exception;

  /** createToolSession
   * 
   * @return
   */
  public Session createToolSession() throws Exception;

  Future getFuture();

  CARS_Main getMain();
  
}
