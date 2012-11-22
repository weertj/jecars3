/*
 * Copyright 2010-2012 NLR - National Aerospace Laboratory
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
import java.net.URL;
import java.net.URLDecoder;
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

/**
 *
 * @author weert
 */
public class CARS_ExternalTool extends CARS_DefaultToolInterface {

  static final private Object LOCK = new Object();

  static final private AtomicInteger IOSTREAMTHREAD_ID = new AtomicInteger( 0 );
  
  static final public int SAVEOUTPUTSPER = 30;

  static final public String WORKINGDIRECTORY               = "jecars:WorkingDirectory";
  static final public String GENERATEUNIQUEWORKINGDIRECTORY = "jecars:GenerateUniqueWorkingDirectory";

  private final transient List<File> mPreRunFiles = new ArrayList<File>();

  private final transient List<File> mInputs = new ArrayList<File>();

  private transient File mWorkingDirectory = null;

  private transient long mToolStartTime = 0;
  private transient long mToolAverageRunningTime = 0;

  /** IOStreamThread
   *
   */
  private class IOStreamThread extends Thread {
    final private String      mName;
    final private InputStream mInput;
          private Session     mStreamSession = null;

    public Session getStreamSession() {
      return mStreamSession;
    }

    /** IOStreamThread
     * 
     * @param pName
     * @param pIs 
     */
    public IOStreamThread( final String pName, final InputStream pIs ) {
      super();
      setName( pName + '_' + IOSTREAMTHREAD_ID.incrementAndGet() );
      mName  = pName;
      mInput = pIs;
      return;
    }

    /** run
     * 
     */
    @Override
    public void run() {        
      try {
        final InputStreamReader isr = new InputStreamReader(mInput);
        final BufferedReader br = new BufferedReader(isr);
        String line;
        final StringBuilder sbuf = new StringBuilder();
        final Session streamSession = createToolSession();
        try {
          final Node tool = streamSession.getNode( getTool().getPath() );
          Node output = replaceOutput( tool, mName, "" );
          output.setProperty( "jecars:Partial", true );
          output.addMixin( "jecars:mixin_unstructured" );
          streamSession.save();
          while ( (line = br.readLine()) != null) {
            sbuf.append( line ).append( '\n' );
            output = replaceOutput( tool, mName, sbuf.toString() );
            output.setProperty( "jecars:LastLine", line );
            output.setProperty( "jecars:Partial", true );
            double runtime = (System.currentTimeMillis()-mToolStartTime)/1000;
            double progress = runtime/(double)mToolAverageRunningTime;
            if (progress>0.95) {
              progress = 0.95;
            }
            getTool().setProperty( "jecars:PercCompleted", 100.0*progress );
            getTool().save();
            streamSession.save();
          }
//        final Node output = replaceOutput( getTool(), mName, sbuf.toString() );
          output = replaceOutput( tool, mName, sbuf.toString() );
          output.setProperty( "jecars:Partial", false );
          streamSession.save();
        } finally {
          streamSession.logout();
        }
      } catch (Exception ioe) {
        reportException( ioe, Level.WARNING );
      }
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
    final FileOutputStream fos = new FileOutputStream( inputF );
    try {
      CARS_Utils.sendInputStreamToOutputStream( 50000, pInput, fos );
      mInputs.add( inputF );
    } finally {
      fos.close();
    }
    return;
  }

  protected void processDataStream( final InputStream pInput, final String pFilename ) throws IOException {
    final File inputF = new File( mWorkingDirectory, pFilename );
    final FileOutputStream fos = new FileOutputStream( inputF );
    try {
      CARS_Utils.sendInputStreamToOutputStream( 50000, pInput, fos );
    } finally {
      fos.close();
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
        FileOutputStream fos = new FileOutputStream( new File( pDir, n.getName() ) );
        final Binary bin = n.getProperty( "jcr:data" ).getBinary();
        CARS_Utils.sendInputStreamToOutputStream( 10000, bin.getStream(), fos );
        bin.dispose();
        fos.close();
      } else {
        copyNodeToDirectory( n, new File( pDir, n.getName() ) );
      }
    }
    return;
  }

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
          ndf.setProperty( "jecars:PathToFile", f.getAbsolutePath() );
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

    // **** Copy the config node to the current tool
    if (!hasConfigNode()) {
      copyConfigNodeToTool( config );
    }
    config = getConfigNode();

    if (config.hasProperty( WORKINGDIRECTORY )) {

      // ******************************
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
//        final File inputF = new File( mWorkingDirectory, "input" + i + ".txt" );
//        final FileOutputStream fos = new FileOutputStream( inputF );
//        try {
//          CARS_Utils.sendInputStreamToOutputStream( 50000, inputStream, fos );
//          mInputs.add( inputF );
//        } finally {
//          fos.close();
//        }
        i++;
      }

      // *****************************************************************
      // **** Copy the input resource of the tool to the working directory
      final Map<String, File> copiedInputs = new HashMap<String, File>();
      {
        final List<Node> inputRes = getInputResources( getTool() );
        for ( final Node input : inputRes ) {
          if (!"jecars:Input".equals(input.getName())) {
            InputStream       is = null;
            FileOutputStream fos = null;
            File      sourceFile = null;
            Binary           bin = null;
            try {
              final Node linkedNode = CARS_Utils.getLinkedNode( input );
              if (linkedNode.hasProperty( "jecars:URL" )) {
                final String path = linkedNode.getProperty( "jecars:URL" ).getValue().getString();
                final URL u = new URL( path );
                if (path.startsWith( "file:/" )) {
                  sourceFile = new File( URLDecoder.decode( u.getFile(), "UTF-8" ));
                } else {
                  is = u.openStream();
                }
              } else {
                if (linkedNode.hasProperty( "jecars:PathToFile" )) {
                  sourceFile = new File( linkedNode.getProperty( "jecars:PathToFile" ).getValue().getString() );
//                  is = new FileInputStream(  );
                } else {
                  bin = linkedNode.getProperty( "jcr:data" ).getBinary();
                  is = bin.getStream();
                }
              }
              final File inputResFile = new File( mWorkingDirectory, linkedNode.getName() );
              copiedInputs.put( linkedNode.getName(), inputResFile );
              if (sourceFile==null) {
                fos = new FileOutputStream( inputResFile );
                CARS_Utils.sendInputStreamToOutputStream( 50000, is, fos );
              } else {
                CARS_Utils.sendInputToOutputNIOBuffer( sourceFile, inputResFile );
              }
//              mInputs.add( inputResFile );
            } finally {
              if (bin!=null) {
                bin.dispose();
              }
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


  /** toolRun
   *
   * @throws Exception
   */
  @Override
  protected void toolRun() throws Exception {

    // **** file snapshot
    final File workDir = getWorkingDirectory();
    final File[] files = workDir.listFiles();
    for( final File file : files ) {
      mPreRunFiles.add( file );
    }


    final Node config = getConfigNode();
    if (config.hasProperty( "jecars:ExecPath" )) {
      final String execPath = config.getProperty( "jecars:ExecPath" ).getString();
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
// ****** @Deprecated START
      final String cmdParam = getParameterString( "commandLine", 0 );
      final List<String> commands = new ArrayList<String>();
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
      
      // **********************************
      // **** Command option parsing
      final SortedMap<Long,Node> commandOptions = new ConcurrentSkipListMap<Long, Node>();
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
        } else {
          commands.add( nn.getName() );
        }
      }
              
      final ProcessBuilder pb = new ProcessBuilder( commands );
      if (config.hasProperty( WORKINGDIRECTORY )) {
        pb.directory( mWorkingDirectory );
      }
 
      reportStatusMessage( "Starting tool " + getTool().getPath() + " as " + commands );
      
      reportProgress( 0 );      
      int err;
      try {
        final Process process = pb.start();
        final IOStreamThread error = new IOStreamThread( "error.txt",  process.getErrorStream() );
        final IOStreamThread input = new IOStreamThread( "stdout.txt", process.getInputStream() );
        error.start();
        input.start();
        err = process.waitFor();
        error.join( 4000 );
        input.join( 4000 );
        process.destroy();
        reportProgress( 1 );
        try {
          getTool().save();
        } catch( RepositoryException re ) {
          LOG.warning( re.getMessage() );
        }
      } catch( Throwable e ) {
        reportException( e, Level.SEVERE );
        super.toolRun();
        throw new Exception(e);
      } finally {
//        error.getStreamSession().logout();
//        input.getStreamSession().logout();        
      }
      reportStatusMessage( "External tool is ending result = " + err );
      if (err!=0) {
        LOG.warning( "External tool has produced an error " + err );
      }
      getTool().save();
    } else {
      throw new InvalidParameterException( "No execpath" );
    }
    super.toolRun();
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
          for( final File file : files ) {
            if (!mPreRunFiles.contains(file)) {
              if (outputLink) {
                  
                final Node output = addOutputTransient( pTool, null, file.getName() );
                if (output!=null) {
                  final long len = file.length();
//                  reportStatusMessage( "Set output file length " + len );
                  output.setProperty( "jecars:IsLink", outputLink );
                  output.setProperty( "jecars:ContentLength", len );
                  output.setProperty( "jecars:Partial", false );
                  output.setProperty( "jecars:Available", true );
                  output.addMixin( "jecars:mix_filelink" );
                  output.setProperty( "jecars:PathToFile", file.getAbsolutePath() );
                  saveSession = output.getSession();
                  if (saveCounter--<0) {
                    saveSession.save();
                    saveCounter = SAVEOUTPUTSPER;
                  }
                }

              } else {

                // **** New output file... copy it
  //              reportStatusMessage( "Copy output file " + file.getName() );
                final FileInputStream fis = new FileInputStream( file );
                try {
                  final Node output = addOutput( fis, file.getName() );
                  if (output!=null) {
                    output.setProperty( "jecars:IsLink", outputLink );
                    output.setProperty( "jecars:ContentLength", file.length() );
                    output.setProperty( "jecars:Available", true );
                    saveSession = output.getSession();
                  }
                } finally {
                  fis.close();
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
                output.setProperty( "jecars:PathToFile", file.getAbsolutePath() );
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
