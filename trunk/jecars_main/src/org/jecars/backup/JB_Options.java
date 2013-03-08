/*
 * Copyright 2007 NLR - National Aerospace Laboratory
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
package org.jecars.backup;

import java.io.File;
import nl.msd.jdots.JD_Taglist;

/**
 * JB_Options
 * 
 * @version $Id: JB_Options.java,v 1.4 2007/10/30 22:44:20 weertj Exp $
 */
public class JB_Options {

  private String     mExportNodeTypesFilename  = "exportNodeTypesCND.jb";
  private String     mExportNamespacesFilename = "exportNamespaces.jb";
  private String     mExportJeCARSFilename     = "exportJeCARS.jb";
  
  private JD_Taglist mPaths     = new JD_Taglist();  
  private JD_Taglist mNodeTypes = new JD_Taglist();
  private File       mExportDirectory      = null;
  private String     mExportBinaryPrefix   = "BINARY_";
  private long       mExportBinaryCounter  = 0L;
  private boolean    mExportNamespaces     = true;
  private boolean    mExportNodeTypes      = true;
  private boolean    mExportReferences     = true;
  private boolean    mExportBinary         = true;
  private boolean    mExportVersionHistory = true;

  private File       mImportDirectory = null;
  private boolean    mImportNamespaces = true;
  private boolean    mImportNodeTypes = true;
  
  private File       mChangeFilePathRoot = null;
  
  public JB_Options() {
    setExportVersionHistory( true );
    return;
  }

  /** changeFilePathRoot
   * 
   * @return 
   */
  public File changeFilePathRoot() {
    return mChangeFilePathRoot;    
  }
  
  /** changeFilePathRoot
   * 
   * @param pF
   * @return 
   */
  public File changeFilePathRoot( final File pF ) {
    return mChangeFilePathRoot=pF;
  }
    
  final public void setExportVersionHistory( boolean pExport ) {
    mExportVersionHistory = pExport;
    if (pExport==true) addIncludePath( "/jcr:system/jcr:versionStorage.*" );
    return;
  }
  
  public boolean getExportVersionHistory() {
    return mExportVersionHistory;
  }
  
  public void setExportBinary( boolean pExport ) {
    mExportBinary = pExport;
    return;
  }
  
  public boolean getExportBinary() {
    return mExportBinary;
  }
  
  public void setExportReferences( boolean pExport ) {
    mExportReferences = pExport;
    return;
  }
  
  public boolean getExportReferences() {
    return mExportReferences;
  }
  
  public void setExportNodeTypes( boolean pExport ) {
    mExportNodeTypes = pExport;
    return;
  }
  
  public boolean getExportNodeTypes() {
    return mExportNodeTypes;
  }

  
  public String getExportNodeTypesFilename() {
    return mExportNodeTypesFilename;
  }
  
  public String getExportNamespacesFilename() {
    return mExportNamespacesFilename;
  }
  
  public String getExportJeCARSFilename() {
    return mExportJeCARSFilename;
  }
  
  public void setExportNamespaces( boolean pExport ) {
    mExportNamespaces = pExport;
    return;
  }
  
  public boolean getExportNamespaces() {
    return mExportNamespaces;
  }
  
  public void setImportNamespaces( boolean pImport ) {
    mImportNamespaces = pImport;
    return;
  }
  
  public boolean getImportNamespaces() {
    return mImportNamespaces;
  }

  public void setImportNodeTypes( boolean pImport ) {
    mImportNodeTypes = pImport;
    return;
  }
  
  public boolean getImportNodeTypes() {
    return mImportNodeTypes;
  }
  
  synchronized public String getNextBinaryExportFilename() {
    return mExportBinaryPrefix + (mExportBinaryCounter++);
  }
  
  public void setExportDirectory( File pExportDirectory ) {
    mExportDirectory = pExportDirectory;
    return;
  }
  
  public File getExportDirectory() {
    return mExportDirectory;
  }

  public void setImportDirectory( File pImportDirectory ) {
    mImportDirectory = pImportDirectory;
    return;
  }
  
  public File getImportDirectory() {
    return mImportDirectory;
  }  
  
  public void addExcludeNodeType( String pNodeType ) {
    mNodeTypes.replaceData( pNodeType, "exclude" );
    return;
  }

  public boolean excludeNodeType( String pNodeType ) {
    Object[] exc = mNodeTypes.getMDataRegex( pNodeType , true );
    for (Object e : exc) {
      if (e.equals( "exclude" )) return true;
    }
    return false;
  }

  
  public void addExcludePath( String pPath ) {
    mPaths.replaceData( pPath, "exclude" );
    return;
  }

  public void addIncludePath( String pPath ) {
    mPaths.replaceData( pPath, "include" );
    return;
  }
  
  public boolean excludePath( String pPath ) {
    Object[] exc = mPaths.getMDataRegex( pPath , true );
    for (Object e : exc) {
      if (e.equals( "include" )) return false;
    }
    for (Object e : exc) {
      if (e.equals( "exclude" )) return true;
    }
    return false;
  }
  
}
