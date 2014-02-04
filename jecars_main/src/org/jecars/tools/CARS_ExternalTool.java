/*
 * Copyright 2010-2014 NLR - National Aerospace Laboratory
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Utils;
import org.jecars.tools.workflow.WF_WorkflowRunner;

/**
 *
 * @author weert
 */
public class CARS_ExternalTool extends CARS_DefaultToolInterface {

  static final private Object LOCK = new Object();

  static final private AtomicInteger IOSTREAMTHREAD_ID = new AtomicInteger( 0 );
  
  static final public int SAVEOUTPUTSPER = 30;

  static final public String WORKINGDIRECTORY               = "jecars:WorkingDirectory";
  static final public String FIXEDWORKINGDIRECTORY          = "jecars:FixedWorkingDirectory";
  static final public String GENERATEUNIQUEWORKINGDIRECTORY = "jecars:GenerateUniqueWorkingDirectory";

  private final transient List<File> mPreRunFiles = new ArrayList<>();

  private final transient List<File> mInputs = new ArrayList<>();

//  private final transient List<File> mResultFiles = new ArrayList<File>(16);
  
  private transient File mWorkingDirectory = null;

  private transient long mToolStartTime = 0;
  private transient long mToolAverageRunningTime = 0;

    /** IOStreamThreadFile
   *
   */
  private class IOStreamThreadFile extends Thread {
    final private String      mName;
    final private InputStream mInput;
    final private File        mOutput;

    /** IOStreamThreadFile
     * 
     * @param pName
     * @param pIs 
     */
    private IOStreamThreadFile( final String pName, final InputStream pIs, final File pOutput ) {
      super();
      setName( pName + '_' + IOSTREAMTHREAD_ID.incrementAndGet() );
      mName  = pName;
      mInput = pIs;
      mOutput = pOutput;
      return;
    }

    /** finish
     * 
     */
    public void finish() {
      
      return;
    }
    
    /** run
     * 
     */
    @Override
    public void run() {    
      try {
//        System.out.println("START PIPE " + mName + " " + System.currentTimeMillis());
        try(InputStreamReader isr = new InputStreamReader(mInput)) {
          try(BufferedReader br = new BufferedReader(isr)) {
            String line;
            try (FileOutputStream fos = new FileOutputStream(mOutput)) {
              while ( (line = br.readLine()) != null) {
                fos.write( line.getBytes() );
                fos.write( '\n' );
              }
            }
          }
        }
      } catch (IOException ioe) {
        reportException( ioe, Level.WARNING );
      }
//   System.out.println("END PIPE " + mName + " " + System.currentTimeMillis());
      return;
    }

  }

  /** IOStreamThread
   *
   */
 /*
  private class IOStreamThread extends Thread {
    final private String      mName;
    final private InputStream mInput;
          private Session     mStreamSession = null;

    public Session getStreamSession() {
      return mStreamSession;
    }

  public IOStreamThread( final String pName, final InputStream pIs ) {
      super();
      setName( pName + '_' + IOSTREAMTHREAD_ID.incrementAndGet() );
      mName  = pName;
      mInput = pIs;
      return;
    }

    @Override
    public void run() {    
      try {
//        System.out.println("START PIPE " + mName + " " + System.currentTimeMillis());
        final InputStreamReader isr = new InputStreamReader(mInput);
        final BufferedReader br = new BufferedReader(isr);
        String line;
        final StringBuilder sbuf = new StringBuilder();
        final Session streamSession = createToolSession();
        try {
          final Node tool;
          Node output;
          synchronized( WF_WorkflowRunner.WRITERACCESS ) {
            tool = streamSession.getNode( getTool().getPath() );
            output = replaceOutput( tool, mName, "" );
            output.setProperty( "jecars:Partial", true );
            output.addMixin( "jecars:mixin_unstructured" );
            streamSession.save();
          }
          while ( (line = br.readLine()) != null) {
            sbuf.append( line ).append( '\n' );
//   System.out.println("PIPE " + mName + " " + line + " : " + System.currentTimeMillis());
            synchronized( WF_WorkflowRunner.WRITERACCESS ) {
              output = replaceOutput( tool, mName, sbuf.toString() );
              output.setProperty( "jecars:LastLine", line );
              output.setProperty( "jecars:Partial", true );
              double runtime = (System.currentTimeMillis()-mToolStartTime)/1000;
              double progress = runtime/(double)mToolAverageRunningTime;
              if (progress>0.95) {
                progress = 0.95;
              }
              tool.setProperty( "jecars:PercCompleted", 100.0*progress );
              streamSession.save();
            }
          }
//        final Node output = replaceOutput( getTool(), mName, sbuf.toString() );
          synchronized( WF_WorkflowRunner.WRITERACCESS ) {
//   System.out.println("PIPE LAST " + mName + " " + line + " : " + System.currentTimeMillis());
            output = replaceOutput( tool, mName, sbuf.toString() );
            output.setProperty( "jecars:Partial", false );
            streamSession.save();
          }
        } finally {
          streamSession.save();
          streamSession.logout();
        }
      } catch (Exception ioe) {
        reportException( ioe, Level.WARNING );
      }
//   System.out.println("END PIPE " + mName + " " + System.currentTimeMillis());
      return;
    }

    
//    @Override
//    public void run() {        
//      try {
//        final InputStreamReader isr = new InputStreamReader(mInput);
//        final BufferedReader br = new BufferedReader(isr);
//        String line;
//        final StringBuilder sbuf = new StringBuilder();
//        while ( (line = br.readLine()) != null) {
//          sbuf.append( line ).append( '\n' );
//        }
////        final Node output = replaceOutput( getTool(), mName, sbuf.toString() );
//        final Session streamSession = createToolSession();
//        try {
//          final Node tool = streamSession.getNode( getTool().getPath() );
//          final Node output = replaceOutput( tool, mName, sbuf.toString() );
//          streamSession.save();
//        } finally {
//          streamSession.logout();
//        }
//      } catch (Exception ioe) {
//        ioe.printStackTrace();
//      }
//      return;
//    }

  }
*/


  /** getWorkingDirectory
   *
   * @return
   */
  protected File getWorkingDirectory() {
    return mWorkingDirectory;
  }

  /** toolInit
   * 
   * @throws Exception
   */
  @Override
  protected void toolInit() throws Exception {
    CARS_ToolSignalManager.addToolSignalListener( this );
    super.toolInit();
//  System.out.println("TOOL INIT 1 " + System.currentTimeMillis());
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
    return;
  }

  /** toolFinally
   *
   */
  @Override
  protected void toolFinally() {
    try {
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
    } catch( RepositoryException re ) {
      reportException( re, Level.SEVERE );
    }
    CARS_ToolSignalManager.removeToolSignalListener( this );
    super.toolFinally();
    return;
  }


  /** processInputDataStream
   * 
   * @param pInput
   * @param pIndex
   * @throws IOException
   */
  protected void processInputDataStream( final InputStream pInput, final int pIndex ) throws IOException {
    processInputDataStream( pInput, "input" + pIndex + ".txt" );
//    final File inputF = new File( mWorkingDirectory, "input" + pIndex + ".txt" );
//    final FileOutputStream fos = new FileOutputStream( inputF );
//    try {
//      CARS_Utils.sendInputStreamToOutputStream( 50000, pInput, fos );
//      mInputs.add( inputF );
//    } finally {
//      fos.close();
//    }
    return;
  }

  /** processInputDataStream
   *
   * @param pInput
   * @param pFilename
   * @throws IOException
   */
  protected void processInputDataStream( final InputStream pInput, final String pFilename ) throws IOException {
    final File inputF = new File( mWorkingDirectory, pFilename );
    try (FileOutputStream fos = new FileOutputStream( inputF )) {
      CARS_Utils.sendInputStreamToOutputStream( 50000, pInput, fos );
      mInputs.add( inputF );
    }
    return;
  }

  /** processDataStream
   * 
   * @param pInput
   * @param pFilename
   * @throws IOException 
   */
  protected void processDataStream( final InputStream pInput, final String pFilename ) throws IOException {
    final File inputF = new File( mWorkingDirectory, pFilename );
    try (FileOutputStream fos = new FileOutputStream( inputF )) {
      CARS_Utils.sendInputStreamToOutputStream( 50000, pInput, fos );
    }
    return;
  }

  /** copyNodeToDirectory
   * 
   * @param pNode
   * @param pDir
   * @throws IOException
   * @throws RepositoryException 
   */
  protected void copyNodeToDirectory( final Node pNode, final File pDir ) throws IOException, RepositoryException {
    final NodeIterator ni = pNode.getNodes();
    while( ni.hasNext() ) {
      final Node n = ni.nextNode();
      if (n.isNodeType( "nt:resource" )) {
        pDir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream( new File( pDir, n.getName() ) )) {
          final Binary bin = n.getProperty( "jcr:data" ).getBinary();
          CARS_Utils.sendInputStreamToOutputStream( 10000, bin.getStream(), fos );
          bin.dispose();
        }
      } else {
        copyNodeToDirectory( n, new File( pDir, n.getName() ) );
      }
    }
    return;
  }

  /** copyDirectoryToNode
   * 
   * @param pDir
   * @param pNode
   * @throws IOException
   * @throws RepositoryException 
   */
  protected void copyDirectoryToNode( final File pDir, final Node pNode ) throws IOException, RepositoryException {
    final File[] fi = pDir.listFiles();
    for( final File f : fi ) {
      if (!pNode.hasNode( f.getName() )) {
        if (f.isDirectory()) {
          final Node ndf = pNode.addNode( f.getName(), "jecars:datafolder" );
          copyDirectoryToNode( f, ndf );
        } else {
          final Node ndf = pNode.addNode( f.getName(), "jecars:outputresource" );
          ndf.setProperty( "jecars:IsLink", true );
          ndf.setProperty( "jecars:ContentLength", f.length() );
          ndf.setProperty( "jecars:Partial", false );
          ndf.setProperty( "jecars:Available", true );
          ndf.setProperty( "jcr:data", "" );
          ndf.addMixin( "jecars:mix_filelink" );
          ndf.setProperty( "jecars:PathToFile", CARS_Utils.getAbsolutePath( f ) );
        }
      }
    }
    return;
  }

  
  /** toolInput
   *
   * @throws Exception
   */
  @Override
  protected void toolInput() throws Exception {
    Node config = getConfigNode();
//  System.out.println("TOOL INPUT 1 " + System.currentTimeMillis());
    
    // **** Copy the config node to the current tool
    Node rootToolConfig = null;
    if (!hasConfigNode()) {
      copyConfigNodeToTool( config );
      // **** Check for overuling config
      Node rootTool = getRootTool();
      if (rootTool!=null) {
        if (rootTool.hasNode( "jecars:Config" )) {
          config = getConfigNode();
          rootToolConfig = rootTool.getNode( "jecars:Config" );
          if (rootToolConfig.hasProperty( WORKINGDIRECTORY )) {
            config.setProperty( WORKINGDIRECTORY, rootToolConfig.getProperty( WORKINGDIRECTORY ).getString() );
          }
          if (rootToolConfig.hasProperty( FIXEDWORKINGDIRECTORY )) {
            config.setProperty( FIXEDWORKINGDIRECTORY, rootToolConfig.getProperty( FIXEDWORKINGDIRECTORY ).getString() );
          }                    
        }
      }
    }
    config = getConfigNode();
    
    if (config.hasProperty( WORKINGDIRECTORY ) || (config.hasProperty( FIXEDWORKINGDIRECTORY ))) {
      // **** Check for jecars:WorkingDirectory

      // ******************************
      // **** Get the working directory
      if (config.hasProperty( FIXEDWORKINGDIRECTORY )) {
        // **** Get the fixed working directory (priority)
        mWorkingDirectory = new File( config.getProperty( FIXEDWORKINGDIRECTORY ).getString() );
        if (!mWorkingDirectory.exists()) {
          if (!mWorkingDirectory.mkdirs()) {
            throw new IOException( "Cannot create directory (Fixed): " + mWorkingDirectory.getAbsolutePath() );
          }
        }
      } else {
        // **** Get the working directory
        mWorkingDirectory = new File( config.getProperty( WORKINGDIRECTORY ).getString() );
        final boolean unique = "true".equals( config.getProperty( GENERATEUNIQUEWORKINGDIRECTORY ).getValue().getString());
        if (unique) {
          final long id = System.nanoTime();
          getTool().setProperty( "jecars:Id", id );
          mWorkingDirectory = new File( mWorkingDirectory, "wd_" + id );
          if (!mWorkingDirectory.mkdirs()) {
            throw new IOException( "Cannot create directory: " + mWorkingDirectory.getAbsolutePath() );
          }
        }
      }

      
/*
      // *************************************************************************
      // **** Check for result file entries
      {
        final NodeIterator ni = config.getNodes();
        int currentrsn = 0;
        while( ni.hasNext() ) {
          final Node rsn = ni.nextNode();
          if (("ResultFile_" + currentrsn).equals( rsn.getName())) {
            if (rsn.hasProperty( "jecars:File" )) {
              File f = new File( rsn.getProperty( "jecars:File" ).getString() );
              if (!f.isAbsolute()) {
                f = new File( mWorkingDirectory, f.getPath() );
              }
              mResultFiles.add( f );
            }
          }
        }
      }
      if (rootToolConfig!=null) {
        final NodeIterator ni = rootToolConfig.getNodes();
        int currentrsn = 0;
        while( ni.hasNext() ) {
          final Node rsn = ni.nextNode();
          if (("ResultFile_" + currentrsn).equals( rsn.getName())) {
            if (rsn.hasProperty( "jecars:File" )) {
              File f = new File( rsn.getProperty( "jecars:File" ).getString() );
              if (!f.isAbsolute()) {
                f = new File( mWorkingDirectory, f.getPath() );
              }
              mResultFiles.add( f );
            }
          }
        }
      }
*/
      
      
      
      // **** Check to copied supporting files in the config node
      if ((config.hasNode( "WorkDirectory" )) && (config.getNode( "WorkDirectory" ).hasNodes())) {
        copyNodeToDirectory( config.getNode( "WorkDirectory" ), mWorkingDirectory );
      }  

      
      // *****************************************************
      // **** Copy the Inputs* object to the working directory
      final Collection<InputStream> inputs = (Collection<InputStream>)getInputsAsObject( InputStream.class, null );
      int i = 1;
      for( final InputStream inputStream : inputs ) {
        processInputDataStream( inputStream, i );
        i++;
      }

      // *****************************************************************
      // **** Copy the input resource of the tool to the working directory
      final Map<String, File> copiedInputs = new HashMap<String, File>();
      {
        final List<Node> inputRes = getInputResources( getTool() );
        for ( final Node input : inputRes ) {
          if (!"jecars:Input".equals(input.getName())) {
            final Node linkedNode = CARS_Utils.getLinkedNode( input );
            if (linkedNode.isNodeType("jecars:datafolder")) {
              final NodeIterator ni = linkedNode.getNodes();              
              while (ni.hasNext()) {
                final Node nextNode = ni.nextNode();
                CARS_Utils.copyInputResourceToDirectory( nextNode, mWorkingDirectory, true );
//                copyInputResourceToWorkingDir(nextNode);
                final File inputResFile = new File( mWorkingDirectory, nextNode.getName() );
                copiedInputs.put( nextNode.getName(), inputResFile );          
              }
            } else {
              CARS_Utils.copyInputResourceToDirectory( linkedNode, mWorkingDirectory, true );
//             copyInputResourceToWorkingDir(linkedNode);
              final File inputResFile = new File( mWorkingDirectory, linkedNode.getName() );
              copiedInputs.put( linkedNode.getName(), inputResFile );
            }
          }
        }
      }

      // **************************************************************************
      // **** Copy the input resource of the template tool to the working directory
      final Node templateTool = getToolTemplate( getTool() );
      if (templateTool!=null) {
            final List<Node> inputRes = getInputResources( templateTool );
            for ( final Node input : inputRes ) {
              if (!copiedInputs.containsKey( input.getName() )) {
                InputStream       is = null;
                FileOutputStream fos = null;
                try {
                  final Binary bin = input.getProperty( "jcr:data" ).getBinary();
                  is = bin.getStream();
                  final File inputResFile = new File( mWorkingDirectory, input.getName() );
                  fos = new FileOutputStream( inputResFile );
                  CARS_Utils.sendInputStreamToOutputStream( 50000, is, fos );
                } finally {
                  if (fos!=null) {
                    fos.close();
                  }
                  if (is!=null) {
                    is.close();
                  }
                }
              }
            }
      }

    }
    super.toolInput();
    return;
  }

  /**
   * Copy linkedNode contents to mWorkingDirectory
   * @param linkedNode
   * @throws IOException
   * @throws IllegalStateException
   * @throws RepositoryException 
   */
  //@Deprecated
  /*
    private void copyInputResourceToWorkingDir(final Node linkedNode) throws IOException, IllegalStateException, RepositoryException {
        File      sourceFile = null;
        Binary           bin = null;
        InputStream       is = null;
        FileOutputStream fos = null;
        try {
            if (linkedNode.hasProperty("jecars:URL")) {
                final String path = linkedNode.getProperty("jecars:URL").getValue().getString();
                final URL u = new URL(path);
                if (path.startsWith("file:/")) {
                    sourceFile = new File(URLDecoder.decode(u.getFile(), "UTF-8"));
                } else {
                    is = u.openStream();
                }
            } else {
                if (linkedNode.hasProperty("jecars:PathToFile")) {
                    sourceFile = new File(linkedNode.getProperty("jecars:PathToFile").getValue().getString());
//                  is = new FileInputStream(  );
                } else {
                    bin = linkedNode.getProperty("jcr:data").getBinary();
                    is = bin.getStream();
                }
            }
            final File inputResFile = new File(mWorkingDirectory, linkedNode.getName());
            if (sourceFile == null) {
                fos = new FileOutputStream(inputResFile);
                CARS_Utils.sendInputStreamToOutputStream(50000, is, fos);
            } else {
                CARS_Utils.sendInputToOutputNIOBuffer(sourceFile, inputResFile);
            }
//              mInputs.add( inputResFile );
        } finally {
            if (bin != null) {
                bin.dispose();
            }
            if (fos != null) {
                fos.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }
*/


  /** toolRun
   *
   * @throws Exception
   */
  @Override
  @SuppressWarnings("LoggerStringConcat")
  protected void toolRun() throws Exception {
//    System.out.println("TOOL RUN 1 " + System.currentTimeMillis());
    // **** file snapshot
    final File workDir = getWorkingDirectory();
    final File[] files = workDir.listFiles();
    for( final File file : files ) {
      mPreRunFiles.add( file );
    }

/*
    boolean recalculate = false;
    
    // **** Check for result files   
    if (mResultFiles.isEmpty()) {
      recalculate = true;
    } else {      
      // **** Result files will be checked, if of the result file one or more files
      // **** aren't available the tool must recalculate again
      for( final File resultFile : mResultFiles ) {
        if (!resultFile.exists()) {
          recalculate = true;
        }
      }
    }

    if (!recalculate) {
      reportStatusMessage( "No need to starting tool " + getTool().getPath() + " result is still available" );
      super.toolRun();
      return;
    }
*/
    
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
      for(final File input : mInputs ) {
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
        pb.directory( mWorkingDirectory );
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
      IOStreamThreadFile error = null;
      IOStreamThreadFile input = null;
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
        error = new IOStreamThreadFile( "__error.txt",  process.getErrorStream(), new File( mWorkingDirectory, "__error.txt" ) );
        input = new IOStreamThreadFile( "__stdout.txt", process.getInputStream(), new File( mWorkingDirectory, "__stdout.txt" ) );
        error.start();
        input.start();
        addFileToOutput( new File( mWorkingDirectory, "__error.txt" ) );
        addFileToOutput( new File( mWorkingDirectory, "__stdout.txt" ) );
//    System.out.println("TOOL RUN 6.1 " + System.currentTimeMillis());
        err = process.waitFor();
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

  /** toolOutput
   *
   * @throws Exception
   */
  @Override
  protected void toolOutput() throws Exception {
    synchronized( LOCK ) {
      super.toolOutput();
      scanOutputFiles( getTool() );
      copyDirectoryToNode( mWorkingDirectory, getTool() );
    }
    return;
  }

  /** addFileToOutput
   * 
   * @param pFile
   * @return
   * @throws RepositoryException 
   */
  private Node addFileToOutput( File pFile ) throws RepositoryException {
    final Node output = addOutputTransient( getTool(), null, pFile.getName() );
    if (output!=null) {
      final long len = pFile.length();
      output.setProperty( "jecars:Title", pFile.getName() );
//      output.setProperty( "jecars:IsLink", outputLink );
      output.setProperty( "jecars:ContentLength", len );
      output.setProperty( "jecars:Partial", false );
      output.setProperty( "jecars:Available", true );
      output.addMixin( "jecars:mix_filelink" );
      output.setProperty( "jecars:PathToFile", CARS_Utils.getAbsolutePath( pFile ) );
      output.getSession().save();
    }
    return output;
  }

  
  /** scanOutputFiles
   *
   * @throws FileNotFoundException
   * @throws Exception
   */
  private void scanOutputFiles( final Node pTool ) throws FileNotFoundException, Exception {
//    synchronized( getTool() ) {
      final File workDir = getWorkingDirectory();
      if ((workDir!=null) && (workDir.exists())) {
        Session saveSession = null;
        try {
  //        reportStatusMessage( "Scan output files [Copy output=" + pCopyOutput + "] [Partial=" + pPartial + "]" );
          final File[]   files = workDir.listFiles();
          final boolean  outputLink;
          final Property outputAsLink = getResolvedToolProperty( pTool, "jecars:OutputAsLink" );
          if (outputAsLink==null) {
            outputLink = false;
          } else {
            outputLink = outputAsLink.getBoolean();
          }
          int saveCounter = SAVEOUTPUTSPER;
          long lastModWorkDir = workDir.lastModified();
          for( final File file : files ) {
            // **** If the file was already available, check if it is updated
            if (mPreRunFiles.contains(file)) {
              if (file.lastModified()>lastModWorkDir) {
                // **** Is updated remove it, and remove from the PreRun list
                if (getTool().hasNode( file.getName() )) {
                  getTool().getNode( file.getName() ).remove();
                }
                mPreRunFiles.remove( file );
              }
            }
            if (!mPreRunFiles.contains(file)) {
              if (outputLink) {
                  
                final Node output = addOutputTransient( pTool, null, file.getName() );
                if (output!=null) {
                  final long len = file.length();
//                  reportStatusMessage( "Set output file length " + len );
                  output.setProperty( "jecars:Title", file.getName() );
                  output.setProperty( "jecars:IsLink", outputLink );
                  output.setProperty( "jecars:ContentLength", len );
                  output.setProperty( "jecars:Partial", false );
                  output.setProperty( "jecars:Available", true );
                  output.addMixin( "jecars:mix_filelink" );
                  output.setProperty( "jecars:PathToFile", CARS_Utils.getAbsolutePath( file ) );
                  saveSession = output.getSession();
                  if (saveCounter--<0) {
                    saveSession.save();
                    saveCounter = SAVEOUTPUTSPER;
                  }
                }

              } else {

                // **** New output file... copy it
  //              reportStatusMessage( "Copy output file " + file.getName() );
                try( final FileInputStream fis = new FileInputStream( file )) {
//                try {
                  final Node output = addOutput( fis, file.getName() );
                  if (output!=null) {
                    output.setProperty( "jecars:IsLink", outputLink );
                    output.setProperty( "jecars:ContentLength", file.length() );
                    output.setProperty( "jecars:Available", true );
                    saveSession = output.getSession();
                  }
//                } finally {
//                  fis.close();
                }
              }
            }
          }
        } finally {
          if (saveSession!=null) {
            saveSession.save();
          }
        }
    }
    return;
  }

  /** refreshOutputFiles
   *
   * @throws FileNotFoundException
   * @throws RepositoryException
   */
  private void refreshOutputFiles( final Node pTool ) throws FileNotFoundException, RepositoryException {
//    synchronized( getTool() ) {
    synchronized( LOCK ) {
      final File workDir = getWorkingDirectory();
      if ((workDir!=null) && (workDir.exists())) {
        Session saveSession = null;
        try {
  //        reportStatusMessage( "Scan output files [Copy output=" + pCopyOutput + "] [Partial=" + pPartial + "]" );
          final File[] files = workDir.listFiles();
          final boolean outputLink;
          final Property outputAsLink = getResolvedToolProperty( pTool, "jecars:OutputAsLink" );
          if (outputAsLink==null) {
            outputLink = false;
          } else {
            outputLink = outputAsLink.getBoolean();
          }
          int saveCounter = SAVEOUTPUTSPER;
          for( final File file : files ) {
            if (!STATE_OPEN_RUNNING.equals( getCurrentState() )) {
              break;
            }
            if (!mPreRunFiles.contains(file)) {
              final Node output = addOutputTransient( pTool, null, file.getName() );
              if (output!=null) {
                final long len = file.length();
                output.setProperty( "jecars:IsLink", outputLink );
                output.setProperty( "jecars:ContentLength", len );
                output.setProperty( "jecars:Partial", true );
                output.addMixin( "jecars:mix_filelink" );
                output.setProperty( "jecars:PathToFile", CARS_Utils.getAbsolutePath( file ) );
                saveSession = output.getSession();
                if (saveCounter--<0) {
                  saveSession.save();
                  saveCounter = SAVEOUTPUTSPER;
                }
              }
            }
          }
        } finally {
          if (saveSession!=null) {
            saveSession.save();
          }
        }
      }
    }
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
