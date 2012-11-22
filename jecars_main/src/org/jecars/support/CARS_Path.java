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
package org.jecars.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * CARS_Path
 *
 * @version $Id: CARS_Path.java,v 1.2 2008/12/02 13:05:14 weertj Exp $
 */
public class CARS_Path {
        
  private static final Pattern gPathPattern =
          Pattern.compile("(\\.)|(\\.\\.)|(([^ /:\\[\\]*'\"|](?:[^/:\\[\\]*'\"|]*[^ /:\\[\\]*'\"|])?):)?" +
               "([^ /:\\[\\]*'\"|](?:[^/:\\[\\]*'\"|]*[^ /:\\[\\]*'\"|])?)(\\[([1-9]\\d*)\\])?");
  
  private StringBuffer mPath = null;
  private String       mPathBuffer = null;
  
  /** Creates a new instance of JCR_PathUtils */
  public CARS_Path( String pPath ) {
    mPath = new StringBuffer( pPath );    
    return;
  }
  
  public boolean checkPath() {
    String[] parts = toString().split( "/" );
    Matcher match;
    for( int i = 0; i<parts.length; i++ ) {    
      match = gPathPattern.matcher( parts[i] );
      if (match.matches()==false) return false;
    }
    return true;
  }

  public boolean hasPaths() {
    if (toString().indexOf( '/' )==-1) {
      return false;
    }
    return true;
  }
  
  public void createNextIndexedItem( Node pParentNode ) throws RepositoryException {
    String rp = toString();
    if (rp.endsWith( "]" )) rp = rp.substring( 0, rp.lastIndexOf( "[" ));
    int i = 2;
    boolean hasNode = true;
    while( hasNode ) {
      mPath.setLength( 0 );
      mPath.append( rp );
      mPath.append( '[' );
      mPath.append( (i++) );
      mPath.append( ']' );
      hasNode = pParentNode.hasNode( mPath.toString() );
    }
    mPathBuffer = null;
    return;
  }

  /** addChildPath
   *
   * @param pPath
   */
  public void addChildPath( CARS_Path pPath  ) {
    if (mPath.charAt(mPath.length()-1)!='/') mPath.append( '/' );
    String path = pPath.mPath.toString();
    if (path.startsWith( "/" )) {
      mPath.append( path.substring(1) );
    } else {
      mPath.append( path );
    }
    mPathBuffer = null;
    return;
  }

  /** addChildPath
   *
   * @param pPath
   */
  public void addChildPath( String pPath ) {
    if (mPath.charAt(mPath.length()-1)!='/') mPath.append( '/' );
    if (pPath.startsWith( "/" )) {
      mPath.append( pPath.substring(1) );
    } else {
      mPath.append( pPath );
    }
    mPathBuffer = null;
    return;
  }
  
  public CARS_Path getParent() {
    CARS_Path path = null;
    if (hasPaths()) {
      String p = toString();
      path = new CARS_Path( p.substring( 0, p.lastIndexOf( '/' ) ) );
    }
    return path;
  }
  
  public String getChild() {
    String child = null;
    if (hasPaths()) {
      child = toString().substring( toString().lastIndexOf('/')+1 );
    } else {
      child = toString();
    }
    return child;
  }
    
  public String getPath() {
    if (mPathBuffer==null) mPathBuffer = mPath.toString();
    return mPathBuffer;
  }
  
  public String toString() {
    return getPath();
  }
  
  
  
}
