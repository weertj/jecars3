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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * JC_Factory
 *
 * @version $Id: JC_Factory.java,v 1.8 2009/05/06 14:11:13 weertj Exp $
 */
public class JC_Factory {
  
  static public final String        JECARSLOCAL    = "http://jecarslocal";
  static public final boolean       USE_RETRIEVERS = true;
  static public       IJC_Retriever ATOM_RETRIEVER = null;

  static {
    try {
      ATOM_RETRIEVER = (IJC_Retriever)Class.forName( "org.jecars.client.atom.JC_AtomRetriever" ).newInstance();
    } catch(IllegalAccessException ice) {
      ice.printStackTrace();
    } catch(InstantiationException iie ) {
      iie.printStackTrace();
    } catch(ClassNotFoundException ce) {
      // **** It's ok
    }
  }
  
  private static List<String> getFound(String contents, String regex) {
      if (isEmpty(regex) || isEmpty(contents)) {
          return null;
      }
      List<String> results = new ArrayList<String>();
      Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CASE);
      Matcher matcher = pattern.matcher(contents);
      
      while (matcher.find()) {
          if (matcher.groupCount() > 0) {
              results.add(matcher.group(1));
          } else {
              results.add(matcher.group());
          }
      }
      return results;
  } 
    
  static private String getFirstFound(String contents, String regex) {
    List<String> founds = getFound(contents, regex);
    if (isEmpty(founds)) {
        return null;
    }
    return founds.get(0);
  }
  
   static private boolean isEmpty(List<String> list) {
    if (list == null || list.size() == 0) {
        return true;
    }
    if (list.size() == 1 && isEmpty(list.get(0))) {
        return true;
    }
    return false;
}
  static private boolean isEmpty( final String str ) {
    if (str != null && str.trim().length() > 0) {
      return false;
    }
    return true;
  }
  
  static private String addDefaultPortIfMissing( String pUrlString, final String pDefaultPort ) {
    URL url = null;
    try {
      url = new URL(pUrlString);
    } catch (MalformedURLException e) {
        return pUrlString;
    }
    if (url.getPort() != -1) {
        return pUrlString;
    }
    String regex = "http://([^/]+)";        
    String found = getFirstFound(pUrlString, regex);
    String replacer = "http://" + found + ":" + pDefaultPort;
    
    if (isEmpty(found)) {
      regex = "https://([^/]+)";        
      found = getFirstFound(pUrlString, regex);
      replacer = "https://" + found + ":" + pDefaultPort;
      if (!isEmpty(found)) {
        pUrlString = pUrlString.replaceFirst(regex, replacer);        
      }
    } else {
        pUrlString = pUrlString.replaceFirst(regex, replacer);
    }
    return pUrlString;
}
    
  /** createClient
   *
   * @param pServerPath
   * @return JC_Clientable
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_Clientable createClient( String pServerPath ) throws JC_Exception {
    final JC_DefaultClient c;
    if (JECARSLOCAL.equals( pServerPath )) {
      try {
//        c = new JC_LocalClient();
        c = (JC_DefaultClient)Class.forName( "org.jecars.client.local.JC_LocalClient" ).newInstance();
      } catch( Exception e ) {
        throw new JC_Exception( e );
      }
    } else {
      if (pServerPath.startsWith( "https:" )) {
        pServerPath = addDefaultPortIfMissing( pServerPath, "443" );        
      } else {
        pServerPath = addDefaultPortIfMissing( pServerPath, "80" );
      }
      c = new JC_DefaultClient();
    }
    c.registerNodeClass( "[root]", "org.jecars.client.JC_RootNode" );
    c.registerNodeClass( "*", "org.jecars.client.JC_DefaultNode" );
    c.registerNodeClass( "jecars:main", "org.jecars.client.nt.JC_mainNode" );
    c.registerNodeClass( "jecars:Prefs", "org.jecars.client.nt.JC_PrefsNode" );
    c.registerNodeClass( "jecars:User", "org.jecars.client.nt.JC_UserNode" );
    c.registerNodeClass( "jecars:Users", "org.jecars.client.nt.JC_UsersNode" );
    c.registerNodeClass( "jecars:GroupLevel", "org.jecars.client.nt.JC_GroupNode" );
    c.registerNodeClass( "jecars:Group", "org.jecars.client.nt.JC_GroupNode" );
    c.registerNodeClass( "jecars:Groups", "org.jecars.client.nt.JC_GroupsNode" );
    c.registerNodeClass( "jecars:Permission", "org.jecars.client.nt.JC_PermissionNode" );
    c.registerNodeClass( "jecars:Workflow", "org.jecars.client.nt.JC_WorkflowNode" );
    c.registerNodeClass( "jecars:WorkflowRunner", "org.jecars.client.nt.JC_WorkflowRunnerNode" );
    c.registerNodeClass( "jecars:RunnerContext", "org.jecars.client.nt.JC_RunnerContextNode" );
    c.registerNodeClass( "jecars:workflowtask", "org.jecars.client.nt.JC_WorkflowTaskNode" );
    c.registerNodeClass( "jecars:workflowtaskport", "org.jecars.client.nt.JC_WorkflowTaskPortNode" );
    c.registerNodeClass( "jecars:workflowtaskportref", "org.jecars.client.nt.JC_WorkflowTaskPortRef" );
    c.registerNodeClass( "jecars:workflowlink", "org.jecars.client.nt.JC_WorkflowLinkNode" );
    c.registerNodeClass( "jecars:workflowlinkendpoint", "org.jecars.client.nt.JC_WorkflowLinkEndPointNode" );
    c.registerNodeClass( "jecars:Tool", "org.jecars.client.nt.JC_ToolNode" );
    c.registerNodeClass( "jecars:ToolEvent", "org.jecars.client.nt.JC_ToolEventNode" );
    c.registerNodeClass( "jecars:ToolEventException", "org.jecars.client.nt.JC_ToolEventExceptionNode" );
    c.registerNodeClass( "jecars:parameterdata", "org.jecars.client.nt.JC_ParameterDataNode" );
    c.registerNodeClass( "jecars:Event", "org.jecars.client.nt.JC_EventNode" );
    c.registerNodeClass( "jecars:MailManager", "org.jecars.client.nt.JC_MailManagerNode" );
    c.registerNodeClass( "jecars:datafolder", "org.jecars.client.nt.JC_datafolderNode" );
    c.registerNodeClass( "jecars:datafile", "org.jecars.client.nt.JC_datafileNode" );
    c.registerNodeClass( "jecars:outputresource", "org.jecars.client.nt.JC_outputresourceNode" );
    c.setServerPath( pServerPath );

    c.setDefaultParams( JC_RESTComm.GET,    c.createParams( JC_RESTComm.GET ) );
    c.setDefaultParams( JC_RESTComm.HEAD,   c.createParams( JC_RESTComm.HEAD ) );
    c.setDefaultParams( JC_RESTComm.POST,   c.createParams( JC_RESTComm.POST ) );
    c.setDefaultParams( JC_RESTComm.PUT,    c.createParams( JC_RESTComm.PUT ) );
    c.setDefaultParams( JC_RESTComm.DELETE, c.createParams( JC_RESTComm.DELETE ) );

    return c;
  }
  
  /** createClient
   * 
   * @param pURL
   * @return JC_Clientable
   * @throws org.jecars.client.JC_Exception
   */
  static public JC_Clientable createClient( final URL pURL ) throws JC_Exception {
    return createClient( pURL.toExternalForm() );
  }

  /** createLocalClient
   * 
   * @return
   * @throws JC_Exception
   */
  static public JC_Clientable createLocalClient() throws JC_Exception {
    JC_Clientable client = createClient( JECARSLOCAL );
    return client;
  }

}
