/*
 * Copyright (c) 2006 Google Inc.
 * Copyright 2008 NLR - National Aerospace Laboratory
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * JC_Query
 *
 * @version $Id: JC_Query.java,v 1.9 2009/06/22 22:37:48 weertj Exp $
 */
public class JC_Query {

  static public JC_Query createQuery() {
    return new JC_Query();
  }

  /** Value for the orderby=.... parameter
   */
  private String mOrderBy;

  /** Value for the where=.... parameter
   */
  private String mWhereString;

  /** Value for the xpath=.... parameter
   */
  private String mXPathString;

  /** Full-text search query string.
   */
  private String mQueryString;

  /** Author name or e-mail address for matched entries.
   */
  private String mAuthor;

  private boolean mVersionHistory = false;

  private boolean mChildNodeDefs = false;

  private long mStartIndex = -1L;
  private long mMaxResults = -1L;

  /** setStartIndex
   *
   * @param pSI
   */
  public void setStartIndex( long pSI ) {
    mStartIndex = pSI;
    return;
  }

  /** getStartIndex
   *
   * @return
   */
  public long getStartIndex() {
    return mStartIndex;
  }

  /** setMaxResults
   *
   * @param pMaxR
   */
  public void setMaxResults( long pMaxR ) {
    mMaxResults = pMaxR;
    return;
  }

  /** getMaxResults
   *
   * @return
   */
  public long getMaxResults() {
    return mMaxResults;
  }

  /** setOrderByString
   * 
   * @param pOrderBy
   * @throws java.io.UnsupportedEncodingException
   */
  public void setOrderByString( final String pOrderBy ) throws UnsupportedEncodingException {
//    mOrderBy = URLEncoder.encode( pOrderBy, JC_RESTComm.CHARENCODE );
    mOrderBy = pOrderBy;
    return;
  }

  /** getOrderByString
   *
   * @return
   */
  public String getOrderByString() {
    return mOrderBy;
  }

  /** setWhereString
   * 
   * @param pWhere
   */
  public void setWhereString( final String pWhere ) {
//    mWhereString = URLEncoder.encode( pWhere, JC_RESTComm.CHARENCODE );
    mWhereString = pWhere;
    return;
  }

  /** getWhereString
   *
   * @return
   */
  public String getWhereString() {
    return mWhereString;
  }

  /** setXPathString
   * 
   * @param pWhere
   */
  public void setXPathString( final String pXPath ) throws UnsupportedEncodingException {
    mXPathString = pXPath;
    return;
  }

  /** getWhereString
   *
   * @return
   */
  public String getXPathString() {
    return mXPathString;
  }

  
  /** setFullTextQuery
   * Sets the full text query string that will be used for the query.
   *
   * @param query the full text search query string.  A value of
   *                    {@code null} disables full text search for this Query.
   */
  public void setFullTextQuery( final String pQuery ) {
    mQueryString = pQuery;
    return;
  }


  /** getFullTextQuery
   * Returns the full text query string that will be used for the query.
   */
  public String getFullTextQuery() {
    return mQueryString;
  }


  /**
   * Sets the author name or email address used for the query.  Only entries
   * with an author whose name or email address match the specified value
   * will be returned.
   *
   * @param author the name or email address for matched entries.  A value of
   *               {@code null} disables author-based matching.
   */
  public void setAuthor( String pAuthor ) {
    mAuthor = pAuthor;
    return;
  }


  /**
   * Returns the author name or email address used for the query.  Only entries
   * with an author whose name or email address match the specified value
   * will be returned.
   *
   * @return the name or email address for matched entries.  A value of
   *          {@code null} means no author-based matching.
   */
  public String getAuthor() {
    return mAuthor;
  }

  /** setChildNodeDefs
   * 
   * @param pDefs
   */
  public void setChildNodeDefs( boolean pDefs ) {
    mChildNodeDefs = pDefs;
    return;
  }
  
  /** getChildNodeDefs
   * 
   * @return
   */
  public boolean getChildNodeDefs() {
    return mChildNodeDefs;
  }

  /** setVersionHistory
   * 
   * @param pVH
   */
  public void setVersionHistory( final boolean pVH ) {
    mVersionHistory = pVH;
    return;
  }

  /** getVersionHistory
   *
   * @return
   */
  public boolean getVersionHistory() {
    return mVersionHistory;
  }
  
}
