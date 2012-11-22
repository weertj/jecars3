/*
 * Copyright (c) 1996-2011 Maverick Software Development, 11/11 Software.
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


/*
 * JD_Thread.java
 *
 * Created on 7 augustus 2000, 17:03
 */
 
package nl.msd.jdots;

/** 
 * Thread helper class, for JDots threading support
 * @author  weertj
 * @version 0.1
 */
public class JD_Thread extends Thread {

  private String        mMethod = null;
  private JD_Objectable mObject = null;
  private JD_Taglist    mTags   = null;
  private int           mFlags  = 0;

  /** Creates new JD_Thread
   */
  public JD_Thread( JD_Objectable pObject, String pMethod, JD_Taglist pTags, int pFlags ) {
    mObject = pObject;
    mMethod = pMethod;
    try {
      mTags = (JD_Taglist)pTags.clone();
    } catch ( Exception e ) {
    }
    mFlags = pFlags;
    return;
  }
  
  public void run() {
    try {
      mObject.JD_doMethod( mMethod, mTags, mFlags );
      mObject.JD_reportError( null );
    } catch ( JD_Exception e ) {
      mObject.JD_reportError( e );
    } finally {
      if (mTags!=null) {
        mTags.clear();
        mTags = null;
      }
      mObject = null;
      mMethod = null;
    }
    return;
  }
  
}
