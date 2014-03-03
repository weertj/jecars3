/*
 * Copyright 2009-2012 NLR - National Aerospace Laboratory
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

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Main;

/**
 * CARS_ToolsFactory
 * 
 * 
[jecars:Tool] > jecars:dataresource, mix:referenceable
- jecars:AutoStart      (Boolean)
- jecars:ToolTemplate   (Path)
- jecars:ToolClass      (String)
- jecars:StateRequest   (String) < '(start|suspend|resume|stop)'
- jecars:State          (String)='open.notrunning' mandatory autocreated
- jecars:PercCompleted  (Double)='0'
+ jecars:Config         (jecars:configresource)
+ *                     (jecars:parameterresource) multiple
+ *                     (jecars:inputresource)     multiple
+ *                     (jecars:outputresource)    multiple
+ jecars:Parameter      (jecars:dataresource) multiple
+ jecars:Input          (jecars:dataresource) multiple
+ jecars:Output         (jecars:dataresource) multiple
 * 
 * 
 * @version $Id: CARS_ToolsFactory.java,v 1.8 2009/05/18 12:17:58 weertj Exp $
 */
public class CARS_ToolsFactory {

  static final private Object ID_GEN = new Object();

  /** getTool
   * @param pMain
   * @param pTplTool
   * @param pParentTool
   * @return
   * @throws java.lang.Exception
   */
  static private CARS_ToolInterface getTool( final CARS_Main pMain, final Node pTplTool, final Node pParentTool, final boolean pCreateNewMain ) throws Exception {
    if (pTplTool.hasProperty( "jecars:ToolClass" )) {
      final Class c = Class.forName( pTplTool.getProperty( "jecars:ToolClass" ).getString() );
      final CARS_ToolInterface ti = (CARS_ToolInterface)c.newInstance();
      if (pCreateNewMain) {
        final CARS_Main main = pMain.getFactory().createMain( pMain, CARS_ActionContext.createActionContext(pMain.getContext()) );
        ti.setTool( main, pParentTool );
      } else {
        ti.setTool( pMain, pParentTool );        
      }
      return ti;
    } else if (pTplTool.hasProperty( "jecars:ToolTemplate" )) {
      final String path = pTplTool.getProperty( "jecars:ToolTemplate" ).getString();
//    System.out.println( "GET TOOL = " + path );
      final Node tplTool = pTplTool.getSession().getRootNode().getNode( path.substring(1) );
      return getTool( pMain, tplTool, pParentTool, pCreateNewMain );
    } else {
      final CARS_DefaultToolInterface dti = new CARS_DefaultToolInterface();
      if (pCreateNewMain) {
        final CARS_Main main = pMain.getFactory().createMain( pMain, CARS_ActionContext.createActionContext(pMain.getContext()) );
        dti.setTool( main, pParentTool );
      } else {
        dti.setTool( pMain, pParentTool );        
      }
      return dti;
    }
  }


  /** getTool
   * @param pMain
   * @param pTplTool
   * @param pFallbackSession, when no tool is found try it in the pFallbackSession
   * @return
   * @throws java.lang.Exception
   */
  static public CARS_ToolInterface getTool( final CARS_Main pMain, final Node pTplTool, final Session pFallbackSession, final boolean pCreateNewMain ) throws Exception {
              
    if (pTplTool.hasProperty( "jecars:ToolClass" )) {
      final Class c = Class.forName( pTplTool.getProperty( "jecars:ToolClass" ).getString() );
      final CARS_ToolInterface ti = (CARS_ToolInterface)c.newInstance();
      if (pMain.getContext()==null) {
        ti.setTool( pMain, pTplTool );          
      } else {
        if (pCreateNewMain) {
          final CARS_Main main = pMain.getFactory().createMain( pMain, CARS_ActionContext.createActionContext(pMain.getContext()) );
          ti.setTool( main, pTplTool );
        } else {
          ti.setTool( pMain, pTplTool );          
        }
      }
      return ti;
    } else if (pTplTool.hasProperty( "jecars:ToolTemplate" )) {
      final String path = pTplTool.getProperty( "jecars:ToolTemplate" ).getString();
//    System.out.println( "GET TOOL = " + path  + " --- " + pTplTool.getPath() );
      try {
        final Node tplTool = pTplTool.getSession().getNode( path );
//        Node tplTool = pTplTool.getSession().getRootNode().getNode( path.substring(1) );
        return getTool( pMain, tplTool, pTplTool, pCreateNewMain );
      } catch (PathNotFoundException pfe) {
        if (pFallbackSession==null) {
          throw pfe;
        } else {
          Node tplTool = pFallbackSession.getRootNode().getNode( path.substring(1) );
          return getTool( pMain, tplTool, pTplTool, pCreateNewMain );
        }
      }
    } else {
      final CARS_DefaultToolInterface dti = new CARS_DefaultToolInterface();
      if (pCreateNewMain) {
        final CARS_Main main = pMain.getFactory().createMain( CARS_ActionContext.createActionContext(pMain.getContext()) );
        dti.setTool( main, pTplTool );
      } else {
        dti.setTool( pMain, pTplTool );
      }
      return dti;
    }
  }

  /** Create jecars:Tool entry
   * @param pParent
   * @param pTemplateTool
   * @param pUserId
   * @param pToolName
   * @return
   * @throws java.lang.Exception
   */
  static public Node createDynamicTool( final Node pParent, final Node pTemplateTool, final String pUserId, final String pToolName ) throws Exception {
    return createDynamicTool( pParent, pTemplateTool, pUserId, pToolName, false );
  }

  static public Node createDynamicTool( final Node pParent, final Node pTemplateTool, final String pUserId, final String pToolName, final boolean pUnique ) throws Exception {
    return createDynamicTool( pParent, pTemplateTool, pUserId, pToolName, pUnique, "jecars:Tool" );
  }

  /** createDynamicTool
   * 
   * @param pParent
   * @param pTemplateTool
   * @param pUserId
   * @param pToolName
   * @param pUnique
   * @return
   * @throws java.lang.Exception
   */
  static public Node createDynamicTool( final Node pParent, final Node pTemplateTool, final String pUserId, final String pToolName, final boolean pUnique, final String pToolNodeType ) throws Exception {
    String id;
    if (pUnique) {
      if (pUserId==null) {
        id = pToolName + "_" + getUniqueID();
      } else {
        id = pUserId + "_" + pToolName + "_" + getUniqueID();
      }
    } else {
      if (pUserId==null) {
        id = pToolName;
      } else {
        id = pUserId + "_" + pToolName;
      }
    }
    final Node jtool = pParent.addNode( id, pToolNodeType );
    jtool.setProperty( CARS_ActionContext.gDefTitle, pToolName );
    if (pTemplateTool!=null) {
      jtool.setProperty( "jecars:ToolTemplate", pTemplateTool.getPath() );
    }
    try {
      jtool.addMixin( "mix:referenceable" );
    } catch( Exception e ) {      
    }
    CARS_DefaultToolInterface.setExpireDateTool( jtool, 10 );    
    return jtool;
  }


  /** getUniqueID
   *
   * @return
   * @throws java.lang.InterruptedException
   */
  static long getUniqueID() throws InterruptedException {
    long id = -1;
    synchronized( ID_GEN ) {
      id = System.nanoTime();
      Thread.sleep(1);
    }
    return id;
  }

}
