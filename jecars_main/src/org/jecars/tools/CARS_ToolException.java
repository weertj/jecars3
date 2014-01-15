/*
 * Copyright 2008-2014 NLR - National Aerospace Laboratory
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

import java.util.List;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.jecars.wfplugin.IWFP_InterfaceResult;

/**
 *
 * @version $Id: CARS_ToolException.java,v 1.2 2009/05/06 14:10:14 weertj Exp $
 */
public class CARS_ToolException extends Exception {

  private final String mMessage;
  
  /** CARS_ToolException
   * 
   * @param pMessage 
   */
  public CARS_ToolException( final String pMessage ) {
    mMessage = pMessage;
    return;
  }
  
  /** CARS_ToolException
   * 
   * @param pRunners
   * @param pResults 
   */
  public CARS_ToolException( final List<Node>pRunners, final List<IWFP_InterfaceResult>pResults )  {
    String message = "Error in runner(s):\n";
    Node runner = null;
    Throwable error = null;
    try {
      for( final Node wr : pRunners ) {
        message += wr.getPath() + "\n";
        runner = wr;
      }
      for( final IWFP_InterfaceResult ir : pResults ) {
        if (ir.getError()!=null) {
          error = ir.getError();
          message += error.getMessage() + "\n";
        }
      }      
      setStackTrace( error.getStackTrace() );
    } catch( RepositoryException e ) {
      e.printStackTrace();
      System.out.println(e);
    }
    mMessage = message;
    return;
  }

  /** getMessage
   * 
   * @return 
   */
  @Override
  public String getMessage() {
    if (mMessage!=null) {
      return mMessage;
    }
    return super.getMessage();
  }
  
  
  
    
}
