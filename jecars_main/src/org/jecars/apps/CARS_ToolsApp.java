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
package org.jecars.apps;

import javax.jcr.Node;
import javax.jcr.Session;
import nl.msd.jdots.JD_Taglist;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_EventManager;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.tools.CARS_DefaultToolInterface;

/**
 * CARS_ToolsApp
 *
 * @version $Id: CARS_ToolsApp.java,v 1.7 2009/07/30 12:05:58 weertj Exp $
 */
public class CARS_ToolsApp extends CARS_DefaultInterface {
    
  /** Creates a new instance of CARS_AdminApp
   */
  public CARS_ToolsApp() {
    super();
  }
 
  
  /** getVersion
   * 
   * @return
   */
  @Override
  public String getVersion() {
    return getClass().getName() + ": JeCARS version=" + CARS_Definitions.VERSION_ID + " $Id: CARS_ToolsApp.java,v 1.7 2009/07/30 12:05:58 weertj Exp $";
  }

  /** getNodes
   *
   * @param pMain
   * @param pInterfaceNode
   * @param pParentNode
   * @param pLeaf
   * @throws java.lang.Exception
   */
  @Override
  public void getNodes( final CARS_Main pMain, final Node pInterfaceNode, final Node pParentNode, final String pLeaf ) throws Exception {
    
    if (pParentNode.isNodeType( "jecars:CARS_Interface" )) {

      final Session appSession = CARS_Factory.getSystemApplicationSession();
      synchronized( appSession ) {
        final Node sysParentNode = appSession.getRootNode().getNode( pParentNode.getPath().substring(1) );

        // **** Hey!.... it the root....
        if (!sysParentNode.hasNode( "Init_Tools_(!WARNING!)")) {
          sysParentNode.addNode( "Init_Tools_(!WARNING!)", "jecars:root" );
        }
        if (!sysParentNode.hasNode( "reportVersion" )) {
          Node rv = sysParentNode.addNode( "reportVersion", "jecars:datafolder" );
          CARS_Utils.addPermission( rv, "DefaultReadGroup", null, "read" );
        }
        sysParentNode.save();
      }
    } else {
      if (pLeaf.equals( "/ToolsApp/Init_Tools_(!WARNING!)" )) {
//        System.out.println( "INIT TOOLS!!!!!" );
        CARS_Factory.getEventManager().addEvent( pMain, pMain.getLoginUser(), pParentNode,
                    null, CARS_EventManager.EVENTCAT_SYS, CARS_EventManager.EVENTTYPE_CREATE, "Init Tools System" );
        try {
          initToolsSystem();
        } catch(Exception e) {
          throw e;
        }
      } else if (pLeaf.endsWith( "/reportVersion" )) {
        // **** Report version information as an event
        final JD_Taglist tags = pMain.getContext().getQueryPartsAsTaglist();
//        tags.print();
        final String message = (String)tags.getData( "jecars:Message" );
        final String body    = (String)tags.getData( "jecars:Body" );
        reportVersionInfo( pMain, message, body );
      }
    }
    pParentNode.save();
    return;
  }

  
  /** Init the JeCARS system
   */
  protected void initToolsSystem() throws Exception {
    Session ses = CARS_Factory.getSystemApplicationSession();
    synchronized( ses ) {
      // **** Create the standard groups
      Node toolsf = ses.getRootNode().getNode( "JeCARS/default/jecars:Tools" );
      if (toolsf.hasNode( "Templates" )==false) {
        CARS_Utils.addPermission( toolsf, "DefaultReadGroup", null, "read" );
        Node tools = toolsf.addNode( "Templates", "jecars:Tools" );        
        CARS_Utils.addPermission( tools, "DefaultReadGroup", null, "read" );
      }      
      Node templates = toolsf.getNode( "Templates" );
      if (templates.hasNode( "URLGetTool" )==false) {
        Node tool = templates.addNode( "URLGetTool", "jecars:Tool" );
        tool.setProperty( "jecars:ToolClass", "org.jecars.tools.CARS_URLGetTool" );
        tool.setProperty( "jecars:Title", "Tool to get URL contents" );
        tool.setProperty( "jecars:Body", "This tool retrieves the contents of an specified URL" );
        CARS_Utils.addPermission( tool, "DefaultReadGroup", null, "read" );
      }
      if (toolsf.hasNode( "RunningTools" )==false) {
        Node tools = toolsf.addNode( "RunningTools", "jecars:Tools" );        
        CARS_Utils.addPermission( tools, "DefaultReadGroup", null, "read" );
        CARS_DefaultToolInterface.initToolFolder( tools );
      }
      Node runningTools = toolsf.getNode( "RunningTools" );
      ses.save();
    }
    return;
  }


  
    
}
