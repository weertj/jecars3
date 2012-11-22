/*
 * Copyright 2012 NLR - National Aerospace Laboratory
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
package org.jecars.client.observation;

import java.util.Calendar;
import org.jecars.client.JC_Path;

/**
 *
 * @author weert
 */
public class JC_DefaultEvent implements JC_Event {

  private final Calendar mDate;
  private final String   mId;
  private final JC_Path  mPath;
  private final TYPE     mType;

  /** JC_DefaultEvent
   *
   * @param pDate
   * @param pId
   * @param pPath
   * @param pType
   */
  public JC_DefaultEvent( final Calendar pDate, final String pId, final JC_Path pPath, final TYPE pType) {
    mDate = pDate;
    mId   = pId;
    mPath = pPath;
    mType = pType;
    return;
  }



  @Override
  public Calendar getDate() {
    return mDate;
  }

  @Override
  public String getIdentifier() {
    return mId;
  }

  @Override
  public JC_Path getPath() {
    return mPath;
  }

  @Override
  public TYPE getType() {
    return mType;
  }

  @Override
  public String toString() {
    return "Event: ID=" + mId + " at " + mPath + " on " + mDate.getTime() + " of type " + mType;
  }


}
