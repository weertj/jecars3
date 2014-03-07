/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jecars.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import org.jecars.CARS_Utils;
import org.jecars.client.JC_Clientable;
import org.jecars.client.JC_Exception;
import org.jecars.client.JC_Factory;
import org.jecars.client.JC_Nodeable;
import org.jecars.client.JC_Streamable;
import org.jecars.client.nt.JC_ParameterDataNode;
import org.jecars.client.nt.JC_ToolNode;
import org.jecars.client.nt.JC_UserNode;
import static org.jecars.tools.CARS_DefaultToolInterface.LOG;
import static org.jecars.tools.CARS_ExternalTool.WORKINGDIRECTORY;
import org.jecars.tools.workflow.WF_WorkflowRunner;
import org.jecars.tools.workflow.xml.WF_XmlException;
import org.jecars.tools.workflow.xml.WF_XmlReader;

/**
 *
 * @author weert
 */
public class CARS_MultiExternalTool extends CARS_ExternalTool {

  @Override
  protected void toolInit() throws Exception {
    super.toolInit();
    
    
  }
  
  
  @Override
  protected void toolRun() throws Exception {
    
    extRun();
    //multiToolRun();
    return;
  }
  
  protected void extRun() throws JC_Exception, RepositoryException, IOException, WF_XmlException {
    final JC_Clientable client = JC_Factory.createClient( "http://------:8080/fanomos" );
    client.setCredentials( "--------", "----".toCharArray() );
    final JC_Nodeable toolsNode = client.getSingleNode("/JeCARS/default/jecars:Tools");
    final JC_UserNode user = (JC_UserNode)client.getUserNode().morphToNodeType();
    // **** Run the tool
    final JC_ToolNode templateTool = (JC_ToolNode)toolsNode.getNode( "netstatToolTemplate" ).morphToNodeType();
    if (toolsNode.hasNode( "netstatTool" )) {
      // **** Remove tool
      toolsNode.getNode( "netstatTool" ).removeNode();
      toolsNode.save();
    }
    final JC_ToolNode runTool = JC_ToolNode.createTool( toolsNode, templateTool, "netstatTool", user );
    runTool.setAutoStartParameters( null, "jecars:Input" );
    runTool.save();
    runTool.addParameterData( "JeCARS-RunOnSystem" ).addParameter( "------" );
    runTool.addInput( "jecars:Input", "jecars:inputresource", "text/plain", "Echo Hello World\n" );
    final JC_ParameterDataNode pdn = runTool.addParameterData( "commandLine" );
    pdn.addParameter( "-s" );
    try {
      while( 1==1 ) {
        final String state = runTool.getState();
        System.out.println( "state = " + state );
        Thread.sleep( 1000 );
        if (state.startsWith( JC_ToolNode.STATE_CLOSED )) {
          break;
        }
      }
    } catch( InterruptedException e ) {
      e.printStackTrace();
    } finally {
      transferToCoreSystem( runTool );
    }
    
    return;
  }
  
  private void transferToCoreSystem( JC_ToolNode pTool ) throws JC_Exception, RepositoryException, IOException, WF_XmlException {

    JC_Streamable wfxml = pTool.getClient().getNodeAsStream( pTool.getPath() + "?alt=wfxml" );
    //String contents = CARS_Utils.readAsString( wfxml.getStream() );
    //System.out.println(contents);

    WF_XmlReader reader = new WF_XmlReader();
    reader.addJcrNode( wfxml.getStream(), getTool().getParent(), true );

    
//    for( JC_Nodeable n : pTool.getNodes() ) {
//      if ("jecars:outputresource".equals(n.getNodeType())) {
//        replaceNode( n );
//      }
//    }
    return;
  }
  
  private void replaceNode( JC_Nodeable pNode ) throws RepositoryException, JC_Exception {
    try {
      Node tn = getTool();
      if (tn.hasNode( pNode.getName() )) {
        tn.remove();
      }
      tn.save();      
      Node newN = tn.addNode( pNode.getName(), pNode.getNodeType() );
//      for( JC_Propertyable prop : pNode.getProperties() ) {
//        newN.setProperty( prop.getName(), BigDecimal.ONE)
//      }
    } finally {
      getTool().save();      
    }
  }
  
  /** toolRun
   *
   * @throws Exception
   */
  protected void multiToolRun() throws Exception {
//    System.out.println("TOOL RUN 1 " + System.currentTimeMillis());
    // **** file snapshot
    final File workDir = getWorkingDirectory();
    if (workDir!=null) {
      final File[] files = workDir.listFiles();
      for( final File file : files ) {
        getPreRunFiles().add( file );
      }
    }


    boolean recalculate = false;
    
    // *************************************************************************
    // **** Check for result files   
    if (getResultFiles().isEmpty()) {
      recalculate = true;
    } else {      
      // **** Result files will be checked, if of the result file one or more files
      // **** aren't available the tool must recalculate again
      for( final String result : getResultFiles() ) {
        if (recalculate) {
          break;
        }
        final File resultFile = new File( result );
        if (!resultFile.exists()) {
          // **** Perhaps the filename is a regular expression
          final Pattern fpat = Pattern.compile( resultFile.getName() );
          boolean match = false;
          for( final File checkFiles : resultFile.getParentFile().listFiles() ) {
            if (fpat.matcher( checkFiles.getName() ).find()) {
              recalculate = false;
              match = true;
              break;
            }
          }
          if (!match) {
            recalculate = true;
          }
        }
      }
    }

    // **** Check if we need to start the tool
    if (!recalculate) {
      reportStatusMessage( "No need to starting tool " + getTool().getPath() + " result is still available" );
      super.toolRun();
      return;
    }

    // **** Run this tool, check we must change the JeCARS-Control parameter
    int ix = getParameterStringIndex( "JeCARS-Control", "state=.*" );
    if (ix!=-1) {
      setParameterString( "JeCARS-Control", ix, "state=run" );
    }
    
//    System.out.println("TOOL RUN 2 " + System.currentTimeMillis());

    final Node config = getConfigNode();
    if (config.hasProperty( "jecars:ExecPath" )) {
      String execPath = config.getProperty( "jecars:ExecPath" ).getString();
      // **** Check if the tools is available
      final File execFile = new File( execPath );
      if (!execFile.exists()) {
        // **** Check if the exec file is given
        final Node toolTemplate = getToolTemplate( getTool() );
        final NodeIterator ni = toolTemplate.getNodes();
        while( ni.hasNext() ) {
          final Node execn = ni.nextNode();
          if ((execn.hasProperty( "jcr:mimeType" )) && ("application/x-exe".equals( execn.getProperty( "jcr:mimeType" ).getString() ))) {
            execFile.getParentFile().mkdirs();
            final Binary bin = execn.getProperty( "jcr:data" ).getBinary();
            final FileOutputStream fos = new FileOutputStream( execFile );
            final InputStream is = bin.getStream();
            CARS_Utils.sendInputStreamToOutputStream( 10000, is, fos );
            fos.close();
            is.close();
          }
        }
      }
      reportMessage( Level.CONFIG, "ExecPath=" + execPath, false );
//    System.out.println("TOOL RUN 3 " + System.currentTimeMillis());
// ****** @Deprecated START
      final String cmdParam = getParameterString( "commandLine", 0 );
      final List<String> commands = new ArrayList<>(8);
      commands.add( execPath );
      if (cmdParam!=null) {
        String[] cmdParams = cmdParam.split( " " );
        for( final String cp : cmdParams ) {
          commands.add( cp );
        }
      }
      for(final File input : getFileInputs() ) {
        commands.add( input.getAbsolutePath() );
      }
// ****** @Deprecated  END
//    System.out.println("TOOL RUN 4 " + System.currentTimeMillis());
      
      // **********************************
      // **** Command option parsing
      final SortedMap<Long,Node> commandOptions = new ConcurrentSkipListMap<>();
      final NodeIterator ni = getTool().getNodes();
      while( ni.hasNext() ) {
        final Node node = ni.nextNode();
        if (node.isNodeType( "jecars:mix_commandlineitem" )) {
          commandOptions.put( node.getProperty( "jecars:Priority" ).getLong(), node );
        }
      }
      final Set<Map.Entry<Long, Node>> cmdopts = commandOptions.entrySet();
      for( final Map.Entry<Long, Node> cmdopt : cmdopts ) {
        Node nn = cmdopt.getValue();
        if (nn.isNodeType( "jecars:parameterdata" )) {
          commands.add( getParameterString( nn.getName(), 0 ) );
        } else if (nn.hasProperty( "jecars:string" )) {          
          commands.add( getParameterString( nn.getName(), 0 ) );
        } else {
          commands.add( nn.getName() );
        }
      }
//    System.out.println("TOOL RUN 4 " + System.currentTimeMillis());
              
      final ProcessBuilder pb = new ProcessBuilder( commands );
      if (config.hasProperty( WORKINGDIRECTORY )) {
        pb.directory( getWorkingDirectory() );
      }
 
      reportStatusMessage( "Starting tool " + getTool().getPath() + " as " + commands );
//    System.out.println("TOOL RUN 5 " + System.currentTimeMillis());
//  System.out.println("START TOOL  time=" + System.currentTimeMillis()  );
      
      // **** Remove error.txt & stdout.txt before running
      if (getTool().hasNode( "error.txt" )) {
        getTool().getNode( "error.txt" ).remove();
      }
      if (getTool().hasNode( "stdout.txt" )) {
        getTool().getNode( "stdout.txt" ).remove();
      }
   // **** TEMP
//   Node dfn = getTool().addNode( "stdout.txt", "jecars:datafile" );
//   dfn.setProperty( "jcr:data", "This file is intentionally left blank" );
//   dfn.setProperty( "jcr:mimeType", "text/plain");
//   dfn = getTool().addNode( "error.txt", "jecars:datafile" );
//   dfn.setProperty( "jcr:data", "This file is intentionally left blank" );
//   dfn.setProperty( "jcr:mimeType", "text/plain");
//   dfn.getSession();
   // **** TEMP
      
      reportProgress( 0 );      
      int err;
      CARS_ExternalTool.IOStreamThreadFile error = null;
      CARS_ExternalTool.IOStreamThreadFile input = null;
      try {
        final Process process = pb.start();
//    System.out.println("TOOL RUN 6 " + System.currentTimeMillis());
        
//        InputStream is = process.getInputStream();
//      InputStreamReader isr = new InputStreamReader(is);
//      BufferedReader br = new BufferedReader(isr);
//      String line;
//      while ((line = br.readLine()) != null) {
////        System.out.println(line);
//      }
        error = new CARS_ExternalTool.IOStreamThreadFile( "__error.txt",  process.getErrorStream(), new File( getWorkingDirectory(), "__error.txt" ) );
        input = new CARS_ExternalTool.IOStreamThreadFile( "__stdout.txt", process.getInputStream(), new File( getWorkingDirectory(), "__stdout.txt" ) );
        error.start();
        input.start();
        addFileToOutput( new File( getWorkingDirectory(), "__error.txt" ) );
        addFileToOutput( new File( getWorkingDirectory(), "__stdout.txt" ) );
//    System.out.println("TOOL RUN 6.1 " + System.currentTimeMillis());
        err = process.waitFor();
//     Thread.sleep( 5000 );
//  System.out.println("START TOOL END  time=" + System.currentTimeMillis()  );
        error.join( 4000 );
        input.join( 4000 );
        process.destroy();
//    System.out.println("TOOL RUN 7 " + System.currentTimeMillis());
        synchronized( WF_WorkflowRunner.WRITERACCESS ) {
          reportProgress( 1 );
          try {
            getTool().save();
          } catch( RepositoryException re ) {
            LOG.warning( re.getMessage() );
          }
        }
      } catch( Throwable e ) {
        reportException( e, Level.SEVERE );
        super.toolRun();
        throw e;
      } finally {
        if (error!=null) {
          error.finish();
        }
        if (input!=null) {
          input.finish();
        }
//        error.getStreamSession().logout();
//        input.getStreamSession().logout();        
      }
//    System.out.println("TOOL RUN 8 " + System.currentTimeMillis());
      synchronized( WF_WorkflowRunner.WRITERACCESS ) {
        reportStatusMessage( "External tool " + getTool().getPath() + " is ending result = " + err );
        if (err!=0) {
          String logmessage =  "External tool " + getTool().getPath() + "(" + execPath + ") has produced an error " + err;
          LOG.warning( logmessage );
          getTool().save();
          throw new CARS_ToolException( logmessage );
        }
        getTool().save();
      }
//    System.out.println("TOOL RUN 9 " + System.currentTimeMillis());
    } else {
      throw new InvalidParameterException( "No execpath" );
    }
    super.toolRun();
//    System.out.println("TOOL RUN END " + System.currentTimeMillis());
//  System.out.println("START TOOL EXIT  time=" + System.currentTimeMillis()  );
    return;
  }

  
}
