/*
 * Copyright 2008-2011 NLR - National Aerospace Laboratory
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

package org.jecars.client;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @version $Id: JC_Path.java,v 1.8 2009/06/21 16:24:53 weertj Exp $
 */
public class JC_Path implements Serializable, Cloneable {

  private static final long serialVersionUID = 200912251214L;

  private static final Pattern PATHPATTERN =
          Pattern.compile("(\\.)|(\\.\\.)|(([^ /:\\[\\]*'\"|](?:[^/:\\[\\]*'\"|]*[^ /:\\[\\]*'\"|])?):)?" +
               "([^ /:\\[\\]*'\"|](?:[^/:\\[\\]*'\"|]*[^ /:\\[\\]*'\"|])?)(\\[([1-9]\\d*)\\])?");
  
  private StringBuilder mPath = null;
//  private String       mPathBuffer = null;
  
  /**
   * JC_Path
   *
   * @version $Id: JC_Path.java,v 1.8 2009/06/21 16:24:53 weertj Exp $
   */
  public JC_Path( final String pPath ) {
    mPath = new StringBuilder( pPath );
    return;
  }
  
  @Override
  protected Object clone() {
    return new JC_Path( getPath() );
  }
  
  public void resolveTo( final String pPath ) {
    String path = mPath.toString();
    if (!path.startsWith( "/" )) {
      final String sourcePath[] = path.split( "/" );
      final String paramPath[] = pPath.split( "/" );
      int srcIX = 0;
      for( int ix=0; ix<sourcePath.length; ix++ ) {
        if ("..".equals(sourcePath[ix])) {
          srcIX++;
        } else {
          mPath.setLength(0);
          for( int i=0; i<(paramPath.length-srcIX); i++ ) {
            mPath.append( paramPath[i] ).append( '/' );
          }
          for( int i=srcIX; i<sourcePath.length; i++ ) {
            mPath.append( sourcePath[i] ).append( '/' );
          }
          mPath.deleteCharAt( mPath.length()-1 );
          break;
        }
      }
    }
    return;
  }
  
  /** ensureEncode
   * 
   */
  public void ensureEncode() {
    String oldpath = getPath();
    String[] parts = oldpath.split( "/" );
    mPath = new StringBuilder();
    for (String part : parts) {
      mPath.append( '/' );
      mPath.append( JC_Utils.urlencode( part ) );
    }
    if (oldpath.endsWith( "/" )) {
      mPath.append( '/' );
    }
    return;
  }

  /** ensureFileEncode
   * 
   */
  public void ensureFileEncode() {
    String oldpath = getPath();
    String[] parts = oldpath.split( "/" );
    mPath = new StringBuilder();
    for (String part : parts) {
      mPath.append( '/' );
      mPath.append( part.replace( ":", "%3A" ) );
    }
    if (oldpath.endsWith( "/" )) {
      mPath.append( '/' );
    }
    return;
  }
  
  /** ensureDecode
   * 
   */
  public void ensureDecode() {
    String oldpath = getPath();
    String[] parts = oldpath.split( "/" );
    mPath = new StringBuilder();
    boolean startWithSl = false;
    if (oldpath.charAt(0)=='/') startWithSl = true;
    for (String part : parts) {
      if (startWithSl) {
        mPath.append( '/' );
      } else {
        startWithSl = true;
      }
      mPath.append( JC_Utils.urldecode( part ) );
    }
    if (oldpath.endsWith( "/" )) {
      mPath.append( '/' );
    }
    return;    
  }
  
  /** checkPath
   * 
   * @return
   */
  public boolean checkPath() {
    String[] parts = toString().split( "/" );
    Matcher match;
    for( int i = 0; i<parts.length; i++ ) {    
      match = PATHPATTERN.matcher( parts[i] );
      if (!match.matches()) {
        return false;
      }
    }
    return true;
  }

  public boolean hasPaths() {
    if (toString().indexOf( '/' )==-1) {
      return false;
    }
    return true;
  }
  
//  public void createNextIndexedItem( Node pParentNode ) throws RepositoryException {
//    String rp = toString();
//    if (rp.endsWith( "]" )) rp = rp.substring( 0, rp.lastIndexOf( "[" ));
//    int i = 2;
//    boolean hasNode = true;
//    while( hasNode ) {
//      mPath.setLength( 0 );
//      mPath.append( rp );
//      mPath.append( '[' );
//      mPath.append( (i++) );
//      mPath.append( ']' );
//      hasNode = pParentNode.hasNode( mPath.toString() );
//    }
//    mPathBuffer = null;
//    return;
//  }
  
  /** addChildPath
   * 
   * @param pPath
   */
  public void addChildPath( JC_Path pPath  ) {
    if (mPath.charAt(mPath.length()-1)!='/') mPath.append( '/' );
    mPath.append( pPath.mPath );
//    mPathBuffer = null;
    return;
  }

  /** addChildPath
   * 
   * @param pPath
   */
  public void addChildPath( final String pPath ) {
    if (mPath.charAt(mPath.length()-1)!='/') {
      mPath.append( '/' );
    }
    final String[] paths = pPath.split( "/" );
    for( int i=0; i<paths.length; ) {
//      mPath.append( JC_Utils.urlencode(paths[i]) );
      mPath.append( paths[i] );
      i++;
      if (i==paths.length) {
        if (pPath.endsWith( "/" )) {
          mPath.append( '/' );
        }
      } else {
        mPath.append( '/' );
      }
    }
//    mPathBuffer = null;
    return;
  }
  
  public JC_Path getParent() {
    JC_Path path = null;
    if (hasPaths()) {
      String p = toString();
      path = new JC_Path( p.substring( 0, p.lastIndexOf( '/' ) ) );
    }
    return path;
  }

  /** getChild
   *
   * @return
   */
  public String getChild() {
    String child;
    if (hasPaths()) {
      child = toString().substring( toString().lastIndexOf('/')+1 );
    } else {
      child = toString();
    }
    return child;
  }

  /** setChild
   *
   * @param pName
   */
  public void setChild( final String pName ) {
    if (hasPaths()) {
      mPath = new StringBuilder( mPath.substring( 0, mPath.lastIndexOf( "/" ) ) );
      mPath.append( '/' ).append( pName );
    } else {
      mPath = new StringBuilder( pName );
    }
  }

  public String getPathRelToRoot() {
    return mPath.toString().substring(1);
  }
    
  public String getPath() {
    return mPath.toString();
//    if (mPathBuffer==null) mPathBuffer = mPath.toString();
//    return mPathBuffer;
  }
  
  @Override
  public String toString() {
    return getPath();
  }
  
  final String getBaseName() {
    int index = mPath.lastIndexOf("/");
    return mPath.substring(index + 1);
  }
}
