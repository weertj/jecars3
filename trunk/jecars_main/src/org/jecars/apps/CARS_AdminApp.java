/*
 * Copyright 2007-2010 NLR - National Aerospace Laboratory
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
package org.jecars.apps;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Event;
import org.jecars.CARS_EventManager;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.backup.JB_ExportData;
import org.jecars.backup.JB_Options;
import org.jecars.output.CARS_InputStream;
import org.jecars.tools.*;
import org.jecars.tools.workflow.IWF_WorkflowRunner;

/**
 * CARS_AdminApp
 *
 * @version $Id: CARS_AdminApp.java,v 1.32 2009/06/19 11:55:41 weertj Exp $
 */
public class CARS_AdminApp extends CARS_DefaultInterface {

  static final protected Logger LOG = Logger.getLogger( "org.jecars.apps" );

  static final private CARS_AdminApp ADMINAPP = new CARS_AdminApp();

  /** Creates a new instance of CARS_AdminApp
   */
  public CARS_AdminApp() {
    super();
  }

  /** autoStartTools
   * @throws java.lang.Exception
   */
  public static void autoStartTools() throws Exception {
//  if (1==1) return;
    final Session ses = CARS_Factory.getSystemToolsSession();
    synchronized( ses ) {
      try {
//        CARS_Main main = CARS_Factory.createMain( ses, null );
        // **** Autostart all autostart tools
        final Query q = ses.getWorkspace().getQueryManager().createQuery( "SELECT * FROM jecars:Tool WHERE jecars:AutoStart='true'", Query.SQL );
        final QueryResult qr = q.execute();
        final NodeIterator ni = qr.getNodes();
        Node tool;
        while( ni.hasNext() ) {
          tool = ni.nextNode();
          if (!tool.isNodeType( "mix:lockable")) {
            tool.addMixin( "mix:lockable" );
            tool.save();
          }
          if (!tool.isLocked()) {
            gLog.log( Level.INFO, "Auto start: " + tool.getPath() );
//            tool.lock( true, true );
//            final Session supersession = ses.getRepository().login( new CARS_Credentials( CARS_AccessManager.gSuperuserName, "".toCharArray(), null ));
//            supersession.
            final CARS_Main main = CARS_Factory.createMain( ses, CARS_Factory.getLastFactory() );            
            final CARS_ActionContext ac = CARS_ActionContext.createActionContext( (CARS_ActionContext)null ); //"Superuser", "pw".toCharArray() );
            main.addContext(ac);
            final CARS_ToolInterface ti = CARS_ToolsFactory.getTool( main, tool, null, true );
            ti.setStateRequest( CARS_ToolInterface.STATEREQUEST_START );
          }
        }
        ses.save();
      } catch( PathNotFoundException pfn ) {
        gLog.log( Level.FINE, null, pfn );
      }
    }

    // **** Observation manager
//    if (gAdminApp==null) {
//      gAdminApp = new CARS_AdminApp();
//    }
    final ObservationManager om = CARS_Factory.getObservationSession().getWorkspace().getObservationManager();
    om.addEventListener( ADMINAPP, Event.PROPERTY_CHANGED|Event.NODE_REMOVED, "/JeCARS/default/Users", true, null, null, false );

    return;
  }
  
  /** Will be be called only once, when JeCARS is started
   * @param pMain the CARS_Main object
   * @param pInterfaceNode the Node which defines the application source
   */
  @Override
  public void init( final CARS_Main pMain, final Node pInterfaceNode ) throws Exception {
    super.init( pMain, pInterfaceNode );
    return;
  }

  
    /** getVersionEventFolders
   * 
   * @return
   */
  @Override
  public ArrayList<String>getVersionEventFolders() {
    return super.getVersionEventFolders();
    // *** add an extra eventfolder target eg.
    //  ef.add( "/JeCARS/default/Events/System/jecars:EventsVERSION" );
    //  the folder must be of the type jecars:SystemEventsFolder
//    return ef;
  }
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": " + CARS_Definitions.PRODUCTNAME + " version=" + CARS_Definitions.VERSION_ID + " CARS_AdminApp";
  }
  

  /** getNodes
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws Exception
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws Exception {
//    System.out.println( "Must put the nodes under: " + pParentNode.getPath() );
//    System.out.println( "The leaf is (fullpath): " + pLeaf );
    
    if (pParentNode.isNodeType( "jecars:CARS_Interface" )) {
      // **** Hey!.... it the root....
      if (!pParentNode.hasNode( "Init_JeCARS_(!WARNING!)")) {
        pParentNode.addNode( "Init_JeCARS_(!WARNING!)", CARS_ActionContext.NTJ_ROOT );
      }
      if (!pParentNode.hasNode( "ObservationServer" )) {
        pParentNode.addNode( "ObservationServer", "jecars:Obs_Server" );
      }
      if (!pParentNode.hasNode( "BackupFacility")) {
        pParentNode.addNode( "BackupFacility", "jecars:backup" );
      }
      if (!pParentNode.hasNode( "Tools")) {
        final Node tools = pParentNode.addNode( "Tools", "jecars:Tools" );
        CARS_DefaultToolInterface.initToolFolder( tools );
        pParentNode.save();
//        setParamProperty( pMain, pInterfaceNode, tool, "jecars:StateRequest", CARS_ToolInterface.STATEREQUEST_START );
      }
      if (!pParentNode.hasNode( "Tools/new/ExpireManager" )) {
        final Node tool = CARS_ToolsFactory.createDynamicTool( pParentNode.getNode( "Tools/new" ), null, null, "ExpireManager", false );
        tool.setProperty( "jecars:AutoStart", true );
        tool.setProperty( "jecars:ToolClass", "org.jecars.CARS_ExpireManager" );
        tool.setProperty( CARS_ActionContext.gDefTitle, "Tool to expire jecars objects" );
        tool.setProperty( CARS_ActionContext.gDefBody, "This tool scans JeCARS at regular intervals to remove expired objects" );
        tool.setProperty( CARS_ActionContext.gDefExpireDate, (Calendar)null );
        pParentNode.save();
      }
      if (!pParentNode.hasNode( "Tools/new/MailManager" )) {
        final Node tool = CARS_ToolsFactory.createDynamicTool( pParentNode.getNode( "Tools/new" ), null, null, "MailManager", false, "jecars:MailManager" );
        tool.setProperty( "jecars:AutoStart", true );
        tool.setProperty( "jecars:ToolClass", "org.jecars.CARS_MailManager" );
        tool.setProperty( CARS_ActionContext.gDefExpireDate, (Calendar)null );
        pParentNode.save();
      }
      if (!pParentNode.hasNode( "PerformanceTest")) {
        pParentNode.addNode( "PerformanceTest", CARS_ActionContext.NTJ_ROOT );
      }
      if (!pParentNode.hasNode( "WorkflowRunners")) {
        pParentNode.addNode( "WorkflowRunners", "jecars:datafolder" );
      }      
      if (!pParentNode.hasNode( "Config")) {
        pParentNode.addNode( "Config", CARS_ActionContext.NTJ_ROOT );
      }
      if (!pParentNode.hasNode( "GC")) {
        pParentNode.addNode( "GC", CARS_ActionContext.NTJ_ROOT );
      }
      if (!pParentNode.hasNode( "CleanAndGC")) {
        pParentNode.addNode( "CleanAndGC", CARS_ActionContext.NTJ_ROOT );
      }
      if (!pParentNode.hasNode( "OpenStreams")) {
        pParentNode.addNode( "OpenStreams", "jecars:datafolder" );
      }
      if (!pParentNode.hasNode( "LongPolls")) {
        pParentNode.addNode( "LongPolls", "jecars:datafolder" );
      }
      if (!pParentNode.hasNode( "AccessManager")) {
        pParentNode.addNode( "AccessManager", "jecars:datafolder" );
      }
      if (!pParentNode.hasNode( "AccessManager/status")) {
        pParentNode.addNode( "AccessManager/status", CARS_ActionContext.NTJ_ROOT );
      }
      if (!pParentNode.hasNode( "AccessManager/clear")) {
        pParentNode.addNode( "AccessManager/clear", CARS_ActionContext.NTJ_ROOT );
      }
    } else {
      if (pLeaf.equals( "/AdminApp/Init_JeCARS_(!WARNING!)" )) {
        gLog.log( Level.INFO, "INIT SYSTEM: " + pLeaf );
        CARS_Factory.getEventService().offer( new CARS_Event( pMain, null, "SYS", "CREATE", pMain.getContext(), "Init JeCARS System" ) );
//        CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pParentNode,
//                    null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, "Init JeCARS System" );
        try {
          initJeCARSSystem( pMain );
        } catch(Exception e) {
          e.printStackTrace();
          throw e;
        }
      } else if (pLeaf.equals( "/AdminApp/BackupFacility/jecars:StartBackup" )) {
        // **** Backup facility
        backupFacility( pLeaf, pParentNode.getParent() );
      } else if (pLeaf.equals( "/AdminApp/PerformanceTest" )) {
        jecars_PerformanceTest( pMain, pParentNode );
      } else if (pLeaf.equals( "/AdminApp/WorkflowRunners" )) {
        jecars_WorkflowRunners( pMain );
      } else if (pLeaf.equals( "/AdminApp/Config" )) {
        jecars_Config( pMain );
//      } else if (pLeaf.equals( "/AdminApp/ObservationServer" )) {
//        jecars_ObservationServer( pMain, pParentNode );
      } else if (pLeaf.equals( "/AdminApp/GC" )) {
        jecars_GC( pMain );
      } else if (pLeaf.equals( "/AdminApp/CleanAndGC" )) {
        jecars_CleanAndGC( pMain );
      } else if (pLeaf.equals( "/AdminApp/AccessManager/status" )) {
        CARS_Utils.setCurrentModificationDate( pParentNode );
        adminApp_AccessManager_status( pMain );
        pParentNode.save();
      } else if (pLeaf.equals( "/AdminApp/AccessManager/clear" )) {
        adminApp_AccessManager_clear( pMain, pInterfaceNode, pParentNode );
      } else if ("/AdminApp/OpenStreams".equals( pLeaf )) {
        CARS_Utils.setCurrentModificationDate( pParentNode );
        adminApp_OpenStreams( pMain );
      } else if ("/AdminApp/LongPolls".equals( pLeaf )) {
        CARS_Utils.setCurrentModificationDate( pParentNode );
       // **** TODO Elderberry
//        adminApp_LongPolls( pMain );
      }
    }

    pParentNode.save();
    return;
  }

  /** jecars_GC
   * 
   * @param pMain
   * @throws javax.jcr.RepositoryException
   * @throws org.apache.jackrabbit.core.state.ItemStateException
   * @throws java.io.IOException
   */
  private void jecars_GC( CARS_Main pMain ) {
    System.gc();
    final Session session = CARS_Factory.getSystemApplicationSession();
    synchronized( session ) {
      try {
        int du = CARS_Factory.getLastFactory().getSessionInterface().runGarbageCollector( session );
      } catch( RepositoryException re ) {
        re.printStackTrace();
      }
    }
    return;
  }

  /** jecars_CleanAndGC
   * 
   * @param pMain
   * @throws javax.jcr.RepositoryException
   * @throws org.apache.jackrabbit.core.state.ItemStateException
   * @throws java.io.IOException
   */
  private void jecars_CleanAndGC( CARS_Main pMain ) throws RepositoryException, IOException {
    LOG.info( "AdminApp: Start GarbageCollector" );
    int du = CARS_Factory.getLastFactory().getSessionInterface().runGarbageCollector( CARS_Factory.getSystemApplicationSession() );
    // TODO  Elderberry
//    final GarbageCollector gc  = ((SessionImpl)CARS_Factory.getLastFactory().getSystemApplicationSession()).createDataStoreGarbageCollector();
//    gc.mark();
//    final int du = gc.sweep();
//    gc.close();
    LOG.info( "AdminApp: Ready removing " + du + " datastore objects" );
    final CARS_ActionContext ac = pMain.getContext();
    ac.setErrorCode( HttpURLConnection.HTTP_OK );
    String report;
    report  = "GARBAGE COLLECT\n==================================\n\n";
    report += "DELETED " + du + " OBJECTS";
    final ByteArrayInputStream bais = new ByteArrayInputStream( report.getBytes() );
    ac.setContentsResultStream( bais, "text/plain" );
    
//    final RepositoryImpl rep = (RepositoryImpl)CARS_Factory.getSystemApplicationSession().getRepository();
//    final CacheManager cache = rep.getCacheManager();
//    cache.setMaxMemory( 1 );
//
//    final SessionImpl ses = (SessionImpl)CARS_Factory.getSystemApplicationSession();
//    final WorkspaceImpl workspace = (WorkspaceImpl)ses.getWorkspace();
//    workspace.getItemStateManager().dispose();
//
//    // **** Close all sessions
//    CARS_Factory.closeAllSessions();

    System.gc();
    return;
  }
  
  /** adminApp_AccessManager_clear
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @throws java.lang.Exception
   */
  protected void adminApp_AccessManager_clear( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode ) throws Exception {
    CARS_Factory.getLastFactory().getAccessManager().gClearCache();
    confAppEvent( pMain, pInterfaceNode, "System/jecars:EventsTRASHED", "INFO", "Purge accessmanager", null );
    return;
  }

  /** jecars_PerformanceTest
   * 
   * @param pMain
   * @param pParentNode
   * @throws RepositoryException 
   */
  protected void jecars_PerformanceTest( final CARS_Main pMain, final Node pParentNode ) throws RepositoryException {
    pParentNode.addMixin( "jecars:mixin_unstructured" );
    pParentNode.save();
    StringBuilder report = new StringBuilder();    
    long time = System.currentTimeMillis();
    report.append( "# Performance test start at " ).append( time ).append( "\n" );
    
    // *************************************************************************
    // **** Test 1
    report.append( "# Test 1 Create 500 nodes under " ).append( pParentNode.getPath() ).append( "\n" );
    for( int i=0; i<500; i++ ) {
      pParentNode.addNode( "Node_" + i, "jecars:unstructured" );
      pParentNode.save();
    }
    report.append( "test1.time=" ).append( System.currentTimeMillis()-time ).append( "\n" );

    // *************************************************************************
    // **** Test 2
    time = System.currentTimeMillis();
    report.append( "# Test 2 Query/SetProperty 500 nodes under " ).append( pParentNode.getPath() ).append( "\n" );
    for( int i=0; i<500; i++ ) {
      if (pParentNode.hasNode( "Node_" + i )) {
        Node n = pParentNode.getNode( "Node_" + i );
        n.setProperty( "prop1", "value1" );
        pParentNode.save();
      }
      pParentNode.save();
    }    
    report.append( "test2.time=" ).append( System.currentTimeMillis()-time ).append( "\n" );

    // *************************************************************************
    // **** Test 3
    time = System.currentTimeMillis();
    report.append( "# Test 3 Remove 500 nodes under " ).append( pParentNode.getPath() ).append( "\n" );
    for( int i=0; i<500; i++ ) {
      if (pParentNode.hasNode( "Node_" + i )) {
        pParentNode.getNode( "Node_" + i ).remove();
      }
      pParentNode.save();
    }    
    report.append( "test3.time=" ).append( System.currentTimeMillis()-time ).append( "\n" );

    
    time = System.currentTimeMillis();

    final CARS_ActionContext ac = pMain.getContext();
    ac.setErrorCode( HttpURLConnection.HTTP_OK );
    final ByteArrayInputStream bais = new ByteArrayInputStream( report.toString().getBytes() );
    ac.setContentsResultStream( bais, "text/plain" );
    return;
  }

  /** jecars_WorkflowRunners
   * 
   * @param pMain 
   */
  protected void jecars_WorkflowRunners( final CARS_Main pMain ) {

    final CARS_ActionContext ac = pMain.getContext();
    ac.setErrorCode( HttpURLConnection.HTTP_OK );
    StringBuilder report = new StringBuilder( 128 );
    
    final List<IWF_WorkflowRunner> wfpaths = CARS_DefaultWorkflow.getRunners( "/JeCARS" );
    int ix = 0;
    for( IWF_WorkflowRunner wfp : wfpaths ) {
      report.append( "WorkflowRunner.Current." ).append( ix ).append( "=" ).append( wfp.getPath() ).append( '\n' );
      report.append( "WorkflowRunner.Current." ).append( ix ).append( ".Instructions=" ).append( wfp.getInstructions() ).append( '\n' );
      ix++;
    }
    
    final ByteArrayInputStream bais = new ByteArrayInputStream( report.toString().getBytes() );
    ac.setContentsResultStream( bais, "text/plain" );

    return;
  }

  
  /** jecars_Config
   *
   * @param pMain
   */
  protected void jecars_Config( final CARS_Main pMain ) {

    final CARS_ActionContext ac = pMain.getContext();
    ac.setErrorCode( HttpURLConnection.HTTP_OK );
    StringBuilder report = new StringBuilder();
    report.append( "Java Configuration\n================================\n\n" );
    Properties prop = System.getProperties();    
    report.append( "Java version\t=\t" + prop.getProperty( "java.version" ) + "\n\n" );
    report.append( "JeCARS Configuration\n================================\n\n" );
    report.append( "JeCARS version\t=\t" + CARS_Definitions.VERSION + '\n' );
    report.append( "Starttime\t=\t" + CARS_Factory.getJeCARSStartTime().getTime().toString() );
    report.append( "\n\nJackrabbit Configuration\n================================\n\n" );
    
    File logs = new File( CARS_Factory.gRepLogHome );
    for( File f : logs.listFiles() ) {
      report.append( "Log file = " + f.getAbsolutePath() + "\n" );
    }
    
//    Session ses = CARS_Factory.getSystemApplicationSession();
//0    ((SessionImpl)ses).getA

    final ByteArrayInputStream bais = new ByteArrayInputStream( report.toString().getBytes() );
    ac.setContentsResultStream( bais, "text/plain" );

    return;
  }

  /** jecars_ObservationServer
   *
   * @param pMain
   */
//  protected void jecars_ObservationServer( final CARS_Main pMain, final Node pOBS ) throws RepositoryException, UnknownHostException, SocketException {
//
//    final CARS_ActionContext ac = pMain.getContext();
//    ac.setErrorCode( HttpURLConnection.HTTP_OK );
//    String report;
//    report  = "JeCARS Observation Server\n================================\n\n";
//    report  = "  Definitions: " + pOBS.getPath() + "\n";
//    report += CARS_ObservationServer.startObservation( pOBS ) + '\n';
//    final ByteArrayInputStream bais = new ByteArrayInputStream( report.getBytes() );
//    ac.setContentsResultStream( bais, "text/plain" );
//
//    return;
//  }

  /** adminApp_LongPolls
   * 
   * @param pMain
   * @throws RepositoryException 
   */
  // **** TODO Elderberry
 /*
  protected void adminApp_LongPolls( final CARS_Main pMain ) throws RepositoryException {
    final CARS_ActionContext ac = pMain.getContext();
    ac.setErrorCode( HttpURLConnection.HTTP_OK );
    final StringBuilder report = new StringBuilder();
    report.append( "LONGPOLLS\n====================================\n\n" );
    final Map<String, CARS_DefaultLongPolling.PollData> gos = CARS_DefaultLongPolling.getPollers();
    for( final Map.Entry<String, CARS_DefaultLongPolling.PollData> gis : gos.entrySet() ) {
      report.append( "  NodePath\t=\t" ).append( gis.getKey() ).append( "\n" );
    }
    final ByteArrayInputStream bais = new ByteArrayInputStream( report.toString().getBytes() );
    ac.setContentsResultStream( bais, "text/plain" );
    return;
  }
  */

  
  /** adminApp_OpenStreams
   *
   * @param pMain
   */
  protected void adminApp_OpenStreams( final CARS_Main pMain ) throws RepositoryException {
    final CARS_ActionContext ac = pMain.getContext();
    ac.setErrorCode( HttpURLConnection.HTTP_OK );
    final StringBuilder report = new StringBuilder();
    report.append( "OPENSTREAMS\n====================================\n\n" );
    final List<CARS_InputStream>gos = CARS_InputStream.getOpenStreams();
    for( final CARS_InputStream gis : gos ) {
      report.append( "CARS_InputStream - " ).append( gis.getClass() ).append( "\n" );
      report.append( "  Node\t=\t" ).append( gis.getNode().getPath() ).append( "\n" );
      report.append( "  File\t=\t" ).append( gis.getFile().getAbsoluteFile() ).append( "\n" );
      report.append( "  Readen\t=\t" ).append( gis.getBytesReaden() ).append( "\n" );
    }
    final ByteArrayInputStream bais = new ByteArrayInputStream( report.toString().getBytes() );
    ac.setContentsResultStream( bais, "text/plain" );
    return;
  }

  /** adminApp_AccessManager_status
   * 
   * @param pMain
   */
  protected void adminApp_AccessManager_status( final CARS_Main pMain ) {
          
    final CARS_ActionContext ac = pMain.getContext();
    ac.setErrorCode( HttpURLConnection.HTTP_OK );
    String report;
    report  = "ACCESSMANAGER\n==================================\n\n";
    report += "Cache size = " + CARS_Factory.getLastFactory().getAccessManager().getCacheSize() + " bytes\n";

    report += "\nREAD DELEGATE PATH CACHE\n==================================\n\n";
    Set<String> cache = CARS_Factory.getLastFactory().getAccessManager().getAllPermissionsDelegatePathCache();
    for (String c : cache) {
      report += c + '\n';
    }
    
    report += "\nREAD PATH CACHE\n==================================\n\n";
    cache = CARS_Factory.getLastFactory().getAccessManager().getReadPathCache();
    for (String c : cache) {
      report += c + '\n';
    }

    report += "\nWRITE PATH CACHE\n==================================\n\n";
    cache = CARS_Factory.getLastFactory().getAccessManager().getWritePathCache();
    for (String c : cache) {
      report += c + '\n';
    }

    report += "\nREMOVE PATH CACHE\n==================================\n\n";
    cache = CARS_Factory.getLastFactory().getAccessManager().getRemovePathCache();
    for (String c : cache) {
      report += c + '\n';
    }

    report += "\nSETPROPERTY PATH CACHE\n==================================\n\n";
    cache = CARS_Factory.getLastFactory().getAccessManager().getSetPropPathCache();
    for (String c : cache) {
      report += c + '\n';
    }
    
    report += "\nDENY READ PATH CACHE\n==================================\n\n";
    cache = CARS_Factory.getLastFactory().getAccessManager().getDenyReadPathCache();
    for (String c : cache) {
      report += c + '\n';
    }
    
    final ByteArrayInputStream bais = new ByteArrayInputStream( report.getBytes() );
    ac.setContentsResultStream( bais, "text/plain" );
      
    return;
  }
  
  /** Implements the JeCARS backup facility
   *
   * @param pLeaf
   * @param pParentNode
   * @throws Exception
   */
  protected void backupFacility( String pLeaf, Node pParentNode ) throws Exception {
      if (pParentNode.hasProperty( "jecars:BackupDirectory" )) {
        String exportPath = "/";
//        if (pParentNode.hasProperty( "jecars:ExportPath" )==true) {
//          exportPath = pParentNode.getProperty( "jecars:ExportPath" ).getString();
//        }
        long backStartTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat();
        gLog.log( Level.INFO, "Backup started at " + sdf.format( new Date(backStartTime) ));
        try {
          File backupDir = new File(pParentNode.getProperty( "jecars:BackupDirectory" ).getString());
          JB_ExportData export = new JB_ExportData();
          JB_Options options = new JB_Options();
          options.setExportDirectory( backupDir );
          options.addExcludePath( "/jcr:system.*" );
          pParentNode.setProperty( "jecars:LastBackup", Calendar.getInstance() );        
          export.exportToDirectory( pParentNode.getSession().getRootNode(), options );
          pParentNode.save();
        } finally {
          long backupEndTime = System.currentTimeMillis();
          gLog.log( Level.INFO, "Backup ended at " + sdf.format( new Date(backupEndTime) ));
          gLog.log( Level.INFO, "Backup lasted for " + ((float)(backupEndTime-backStartTime)/1000.0f) + " seconds" );          
        }
      }
    return;
  }
  
  
  /** Init the JeCARS system
   */
  protected void initJeCARSSystem( CARS_Main pMain ) throws Exception {
    Session ses = CARS_Factory.getSystemApplicationSession();
    synchronized( ses ) {
      
//      CARS_Factory.getLastFactory().initApplicationSources( pMain );
        
      // **** Create the standard groups
      Node users  = ses.getRootNode().getNode( "JeCARS/default/Users" );
      Node groups = ses.getRootNode().getNode( "JeCARS/default/Groups" );
      // ***** /Groups
      if (groups.hasNode( "DefaultReadGroup" )==false) {
        Node group = groups.addNode( "DefaultReadGroup", "jecars:Group" );
        group.setProperty( "jecars:Title", "Group with has read rights on default objects" );
        group.setProperty( "jecars:Fullname", "DefaultReadGroup" );
        group.setProperty( "jecars:Body", "Members of this group are allowed to read the ../default tree" );
        Node def = groups.getParent();
        Node p = def.addNode( "P_DefaultReadGroup", "jecars:Permission" );
        String[] perms = {"read"};
        p.setProperty( "jecars:Actions", perms );
        Value[] refs = {ses.getValueFactory().createValue(
                          "/JeCARS/default/Groups/DefaultReadGroup" ) };
//                          ses.getRootNode().getNode( "JeCARS/default/Groups/DefaultReadGroup" )) };
        p.setProperty( "jecars:Principal", refs );
        def = def.getParent();
        p = users.addNode( "P_DefaultReadGroup", "jecars:Permission" );
        p.setProperty( "jecars:Actions", perms );
        p.setProperty( "jecars:Principal", refs );
        p = def.addNode( "P_DefaultReadGroup", "jecars:Permission" );
        p.setProperty( "jecars:Actions",  perms );
        p.setProperty( "jecars:Principal", refs );
        Node apps = ses.getRootNode().getNode( "JeCARS/ApplicationSources" );
        p = apps.addNode( "P_DefaultReadGroup", "jecars:Permission" );
        p.setProperty( "jecars:Actions",  perms );
        p.setProperty( "jecars:Principal", refs );
        apps = ses.getRootNode();
        p = apps.addNode( "P_DefaultReadGroup", "jecars:Permission" );
        p.setProperty( "jecars:Actions",  perms );
        p.setProperty( "jecars:Principal", refs );
        groups.save();
        
        // **** Add DefaultReadGroup to the /account/... path
        Node permanon = ses.getRootNode().getNode( "accounts/jecars:P_anonymous" );
        CARS_Utils.addMultiProperty( permanon, "jecars:Principal", group.getPath(), false );
        permanon.save();
      }
      if (groups.hasNode( "UserManagers" )==false) {
        Node group = groups.addNode( "UserManagers", "jecars:Group" );
        group.setProperty( "jecars:Fullname", "UserManagers" );
        group.setProperty( "jecars:Body", "Members of this group are allowed to create and change other users" );
      }
      if (users.hasNode( "UserManager" )==false) {
        Node user = users.addNode( "UserManager", "jecars:User" );
        user.setProperty( "jecars:Fullname", "UserManager" );
        CARS_DefaultMain.setCryptedProperty( user, "jecars:Password_crypt", "jecars" );
//        user.setProperty( "jecars:Password_crypt", CARS_PasswordService.getInstance().encrypt("jecars") );
        Node group = groups.getNode( "UserManagers" );
//        CARS_Utils.addMultiProperty( group, "jecars:GroupMembers", user.getUUID() );
        CARS_Utils.addMultiProperty( group, "jecars:GroupMembers", user.getPath(), false );
//        group.setProperty( "jecars:GroupMembers", refs );
      }
      if (users.hasNode( "P_UserManagers" )==false) {
        Value[] refs = {ses.getValueFactory().createValue(
//                          ses.getRootNode().getNode( "JeCARS/default/Groups/UserManagers" )) };
                          "/JeCARS/default/Groups/UserManagers" ) };
        String[] perms = {"read","add_node","get_property","set_property","remove"};
        Node p = users.addNode( "P_UserManagers", "jecars:Permission" );
        p.setProperty( "jecars:Actions",  perms );
        p.setProperty( "jecars:Delegate", true );            
        p.setProperty( "jecars:Principal", refs );
        Node perm = users.getNode( "P_UserManagers" );
        // **** Groups
        p = groups.addNode( "P_UserManagers", "jecars:Permission" );
        p.setProperty( "jecars:Actions",  perms );
        p.setProperty( "jecars:Delegate", true );
        p.setProperty( "jecars:Principal", refs );

        String[] perms2 = {"read","get_property"};
        // **** JeCARS/UserSources
        Node us = ses.getRootNode().getNode( "JeCARS/UserSources" );
        p = us.addNode( "P_UserManagers", "jecars:Permission" );
        p.setProperty( "jecars:Delegate", true );
        p.setProperty( "jecars:Actions",  perms2 );
        p.setProperty( "jecars:Principal", refs );
        // **** JeCARS/GroupSources
        us = ses.getRootNode().getNode( "JeCARS/GroupSources" );
        p = us.addNode( "P_UserManagers", "jecars:Permission" );
        p.setProperty( "jecars:Delegate", true );
        p.setProperty( "jecars:Actions",  perms2 );
        p.setProperty( "jecars:Principal", refs );
      }
          
      // **** Group/Group relations
      Node drg = groups.getNode( "DefaultReadGroup" );
      Node umg = groups.getNode( "UserManagers" );
//      CARS_Utils.addMultiProperty( drg, "jecars:GroupMembers", umg.getUUID() );
      CARS_Utils.addMultiProperty( drg, "jecars:GroupMembers", umg.getPath(), false );
      
      // **** Outputgenerators
//      if (ses.getRootNode().hasNode( "JeCARS/default/OutputGenerators" )==false) {
//        Node outgens = ses.getRootNode().getNode( "JeCARS/default/OutputGenerators" );
//        outgens.addNode( "OutputGenerator_HTML", "org.jecars.output.CARS_OutputGenerator_HTML" );
//      }
      
      ses.save();
    }
    return;
  }
  
  /** Is called when this object (superobject) registered in the ObservationManager
   * @param pEvents the event iterator
   */
  @Override
  public void onEvent( final EventIterator pEvents ) {
    Event lastEvent = null;
    try {
      final Session session = CARS_Factory.getObservationSession();
      while( pEvents.hasNext() ) {
        lastEvent = pEvents.nextEvent();
//        System.out.println( "-a-a-a- " + lastEvent.toString() );
        final String path = lastEvent.getPath();
        if (path.endsWith( "/jecars:Password_crypt" )) {
          if (lastEvent.getType()==Event.PROPERTY_CHANGED) {
            // **** Password change
            synchronized(session) {
              session.refresh( false );
              final Node rn = session.getRootNode().getNode( path.substring( 1, path.lastIndexOf('/')) );
              rn.setProperty( "jecars:PasswordChangedAt", Calendar.getInstance() );
              rn.setProperty( "jecars:PasswordMustChange", false );
              rn.save();
            }
          }
        }

        // **** Check Dest properties
//        final String ident = lastEvent.getIdentifier();
//        if (ident!=null) {
//          if (lastEvent.getType()==Event.PROPERTY_CHANGED) {
//            final Node n = session.getNodeByIdentifier( ident );
//            if (n.isNodeType( "jecars:principalexport" )) {
//              // **** We must export the results
//              System.out.println("EXPORT === " + n.getPath() );
//              exportUserNode( n );
//            }
//          }
//        }

      }
      super.onEvent( pEvents );
    } catch( Exception e ) {
      if (lastEvent==null) {
        gLog.log( Level.SEVERE, "onEvent", e );
      } else {
        gLog.log( Level.SEVERE, "onEvent:" + lastEvent.toString(), e );        
      }
    }
    return;
  }

//  private void exportUserNode( final Node pUserNode ) throws RepositoryException, JC_Exception {
//    if (pUserNode.hasProperty( "jecars:Dest" )) {
//      final Value[] dests       = pUserNode.getProperty( "jecars:Dest" ).getValues();
//      for (int i = 0; i < dests.length; i++) {
//        final Value destValue = dests[i];
//        final Node userSource = pUserNode.getSession().getNode( destValue.getString() );
//        final Value loginName = userSource.getProperty( "jecars:LoginName" ).getValue();
//        final Value loginPwd  = userSource.getProperty( "jecars:LoginPassword" ).getValue();
//        final String repClass = userSource.getProperty( "jecars:RepositoryClass" ).getString();
//        final String[] repClasses = repClass.split( "," );
//        final JC_Clientable client = JC_Factory.createClient( repClasses[1] );
//      System.out.println(" Connect toR " + repClasses[1] );
//        client.setCredentials( loginName.getString(), loginPwd.getString().toCharArray() );
//      System.out.println(" client = " + client );
//        final JC_UsersNode users = client.getUsersNode();
//      System.out.println(" users = " + users.getPath() );
//        if (!users.hasUser( pUserNode.getName() ) ) {
//            System.out.println(" HAS No USER " + pUserNode.getName() );
//          users.addUser( pUserNode.getName(), null, pUserNode.getProperty( "jecars:Password" ).getString().toCharArray(), null );
//          users.save();
//        }
        // **** Update the user
        
//      }
//    }
//    return;
//  }
    
}
