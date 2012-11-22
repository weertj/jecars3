/*
 * Copyright 2008-2010 NLR - National Aerospace Laboratory
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JC_Params
 *
 * @version $Id: JC_Params.java,v 1.11 2009/06/23 20:30:27 weertj Exp $
 */
public class JC_Params implements Serializable {

  private static final long serialVersionUID = 201001211137L;

  private boolean                   mDeep               = false;
  private boolean                   mAllProperties      = false;
  private String                    mOutputFormat       = JC_Defs.OUTPUTTYPE_ATOM;
  private Map<String,String>        mOtherParams        = new LinkedHashMap<String, String>();
  private List<JC_Streamable>       mStreamables        = null;
  private String                    mHTTPOverride       = null;
  private String                    mEventCollectionID  = null;
  private boolean                   mIncludeBinary      = false;
  private String                    mVCSCommand         = null;
  private String                    mVCSLabel           = null;
  private boolean                   mForce              = false;
  private boolean                   mLongPoll           = false;
  private String                    mPropertyName       = null;

  static final public String SPECIAL_PREFIX                 = "!_#_!";
  static final public String PREFIX_VALUE_REMOVE            = SPECIAL_PREFIX + "VREMOVE";
  static final public String UNSTRUCT_PREFIX_BOOLEAN        = SPECIAL_PREFIX + "UB";
  static final public String UNSTRUCT_PREFIX_DOUBLE         = SPECIAL_PREFIX + "UD";
  static final public String UNSTRUCT_PREFIX_LONG           = SPECIAL_PREFIX + "UL";
  static final public String UNSTRUCT_PREFIX_MSTRINGS       = SPECIAL_PREFIX + "UMS";
  static final public String UNSTRUCT_PREFIX_MDOUBLE        = SPECIAL_PREFIX + "UMD";
  static final public String UNSTRUCT_PREFIX_MLONG          = SPECIAL_PREFIX + "UML";

  /** createParams
   *
   * use the JC_Clientable.createParams() call
   *
   * @return
   */
  @Deprecated
  static public JC_Params createParams() {
    return new JC_Params();
  }

  /** clone
   *
   * @return
   * @throws java.lang.CloneNotSupportedException
   */
  @Override
  public Object clone() throws CloneNotSupportedException {
    final JC_Params p = new JC_Params();
    p.mDeep = mDeep;
    p.mAllProperties = mAllProperties;
    p.mOutputFormat  = mOutputFormat;
    if (mOtherParams!=null) {
      p.mOtherParams = (Map<String, String>)(((LinkedHashMap<String, String>)mOtherParams).clone());
    }
    if (mStreamables!=null) {
      p.mStreamables = (List<JC_Streamable>)(((ArrayList<JC_Streamable>)mStreamables).clone());
    }
    p.mHTTPOverride      = mHTTPOverride;
    p.mEventCollectionID = mEventCollectionID;
    return p;
  }

  /** cloneParams
   *
   * @return
   * @throws org.jecars.client.JC_Exception
   */
  public JC_Params cloneParams() throws JC_Exception {
    try {
      return (JC_Params)clone();
    } catch( CloneNotSupportedException ce ) {
      throw new JC_Exception( ce );
    }
  }

  /** setVCSCommand
   * 
   * @param pCmd
   */
  public void setVCSCommand( final String pCmd ) {
    mVCSCommand = pCmd;
    return;
  }

  /** getVCSCommand
   *
   * @return
   */
  public String getVCSCommand() {
    return mVCSCommand;
  }

  public void setVCSLabel( final String pLabel ) {
    mVCSLabel = pLabel;
    return;
  }

  /** getVCSLabel
   *
   * @return
   */
  public String getVCSLabel() {
    return mVCSLabel;
  }

  /** setPropertyName
   *
   * @param pSet
   */
  public void setPropertyName( final String pName ) {
    mPropertyName = pName;
    return;
  }

  /** getIncludeBinary
   *
   * @return
   */
  public String getPropertyName() {
    return mPropertyName;
  }

  /** setIncludeBinary
   *
   * @param pSet
   */
  public void setIncludeBinary( boolean pSet ) {
    mIncludeBinary = pSet;
    return;
  }

  /** getIncludeBinary
   *
   * @return
   */
  public boolean getIncludeBinary() {
    return mIncludeBinary;
  }


  /** setAllProperties
   * 
   * @param pSet
   */
  public void setAllProperties( final boolean pSet ) {
    mAllProperties = pSet;
    return;
  }

  /** getAllProperties
   * 
   * @return
   */
  public boolean getAllProperties() {
    return mAllProperties;
  }

  /** setHTTPOverride
   * 
   * @param pMethod
   */
  public void setHTTPOverride( final String pMethod ) {
    mHTTPOverride = pMethod;
    return;
  }
  
  /** getHTTPOverride
   * 
   * @return
   */
  public String getHTTPOverride() {
    return mHTTPOverride;
  }

  /** getOtherParameters
   * 
   * @return
   */
  public Map<String, String> getOtherParameters() {
    return Collections.unmodifiableMap(mOtherParams);
  }

  /** addOtherParameter
   * 
   * @param pTag
   * @param pValue
   * @return true if the parameter is added, false if not
   */
  public boolean addOtherParameter( final String pTag, final String pValue ) {
    if (mOtherParams.containsKey( pTag )) {
      return false;
    } else {
      mOtherParams.put( pTag, pValue );
      return true;
    }
  }

  /** getOtherParameter
   * 
   * @param pTag
   * @return
   */
  public String getOtherParameter( final String pTag ) {
    return mOtherParams.get( pTag );
  }

  /** addOtherParameter
   * 
   * @param pStream
   * @return
   */
  public boolean addStreamable( final JC_Streamable pStream ) {
    if (mStreamables==null) mStreamables = new ArrayList<JC_Streamable>();
    mStreamables.add( pStream );
    return true;
  }

  /** getStreamables
   * 
   * @return
   */
  public Collection<JC_Streamable> getStreamables() {
    return mStreamables;
  }
  
  /** setOutputFormat
   * 
   * @param pOutputFormat
   */
  public void setOutputFormat( String pOutputFormat ) {
    mOutputFormat = pOutputFormat;
    return;
  }
  
  /** getOutputFormat
   * 
   * @return
   */
  public String getOutputFormat() {
    return mOutputFormat;
  }

  /** setDeep
   *
   * @param pDeep
   */
  public void setDeep( boolean pDeep ) {
    mDeep = pDeep;
    return;
  }

  /** isDeep
   *
   * @return
   */
  public boolean isDeep() {
    return mDeep;
  }

  /** setLongPoll
   * 
   * @param pLP 
   */
  public void setLongPoll( final boolean pLP ) {
    mLongPoll = pLP;
    return;
  }
  
  /** isLongPoll
   * 
   * @return 
   */
  public boolean isLongPoll() {
    return mLongPoll;
  }
  
  /** setForce
   *
   * @param pForce
   */
  public void setForce( final boolean pForce ) {
    mForce = pForce;
    return;
  }

  /** isForced
   *
   * @return
   */
  public boolean isForced() {
    return mForce;
  }

  /** setEventCollectionID
   * 
   * @param pID
   */
  public void setEventCollectionID( String pID ) {
    mEventCollectionID = pID;
  }
  
  /** getEventCollectionID
   * 
   * @return
   */
  public String getEventCollectionID() {
    return mEventCollectionID;
  }

  /** Set the event type which won't! being logged.
   *  eg. if no READ and WRITE type events are neccesary to be logged
   *  set pEventTypes to "READ,WRITE".
   * 
   * @param pEventTypes
   */
  public void setFilterEventTypes( String pEventTypes ) {
    mOtherParams.put( JC_Defs.FILTER_EVENTTYPES, pEventTypes );
    return;
  }
  
}
