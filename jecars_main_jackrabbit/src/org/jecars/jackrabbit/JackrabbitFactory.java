/*
 * Copyright 2007-2011 NLR - National Aerospace Laboratory
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
package org.jecars.jackrabbit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import org.apache.jackrabbit.commons.cnd.CndImporter;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.cache.CacheManager;
import org.apache.log4j.PropertyConfigurator;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_EventManager;
import org.jecars.CARS_Factory;
import org.jecars.CARS_LogHandler;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.ICARS_Session;
import org.jecars.apps.CARS_AdminApp;
import org.jecars.jaas.CARS_Credentials;
import org.jecars.jaas.CARS_PasswordService;

/**
 * JackrabbitFactory
 *
 * @version $Id: JackrabbitFactory.java,v 1.37 2009/06/21 20:58:48 weertj Exp $
 */
public class JackrabbitFactory extends CARS_Factory {

  @Override
  protected void addNodeTypesDefinitions(Session pSession, String[] pCNDS) throws IOException, RepositoryException {
   int i = 0;
    while( i<pCNDS.length ) {
      gLog.log( Level.INFO, "Process CND file: " + pCNDS[i] + " = " +  pCNDS[i+1] );
      final InputStream is = CARS_Factory.class.getResourceAsStream( pCNDS[i] );
      if (is!=null) {
        final InputStreamReader isr = new InputStreamReader( is );
// v2.0
        try {
          CndImporter.registerNodeTypes( isr, pSession );
        } catch( ParseException pe ) {
          throw new RepositoryException( pe );
        }

//        final org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader cndReader =
//                    new org.apache.jackrabbit.core.nodetype.compact.CompactNodeTypeDefReader( isr, pCNDS[i+1] );
//        final List ntdList = cndReader.getNodeTypeDefs();
//        final NodeTypeManagerImpl ntmgr =(NodeTypeManagerImpl)pSession.getWorkspace().getNodeTypeManager();
//        final NodeTypeRegistry ntreg = ntmgr.getNodeTypeRegistry();
//        for (final Iterator it = ntdList.iterator(); it.hasNext();) {
//          final org.apache.jackrabbit.core.nodetype.NodeTypeDef ntd = (org.apache.jackrabbit.core.nodetype.NodeTypeDef)it.next();
//          try {
//            ntreg.registerNodeType(ntd);
//          } catch (InvalidNodeTypeDefException de) {
//            LOG.log( Level.INFO, de.getMessage() );
//          } catch (RepositoryException re) {
//            LOG.log( Level.INFO, re.getMessage() );
//          }
//        }
        isr.close();
        is.close();
      } else {
        gLog.log( Level.SEVERE, "Cannot find CND file: " + pCNDS[i] + " - " + pCNDS[i+1] );
      }
      i += 2;
    }
    return;
  }

  @Override
  protected void initJCR( CARS_Credentials pCreds) throws Exception {
    initJeCARSProperties();
    // **** init log handler
    initLogging();
    gLog.log( Level.INFO, "Create repository using: " + gConfigFile + " and " + gRepHome );
    gLog.log( Level.INFO, "Config file abspath: " + new File(gConfigFile).getAbsolutePath() );
    gLog.log( Level.INFO, "Repository home abspath: " + new File(gRepHome).getAbsolutePath() );

    try {
      JackrabbitLoginModule.gSuperuserAllowed = true;
      if (gRepository==null) setRepository( new TransientRepository( gConfigFile, gRepHome ) );

      gLog.info( "JeCARS version: " + CARS_Definitions.VERSION );
      gLog.info( "JCR version: " + gRepository.getDescriptor( Repository.SPEC_VERSION_DESC ) + " (" + gRepository.getDescriptor( Repository.SPEC_NAME_DESC ) + ")" );
      gLog.info( "Repository version: " + gRepository.getDescriptor( Repository.REP_VERSION_DESC ) + " (" + gRepository.getDescriptor( Repository.SPEC_NAME_DESC ) + ")" );
      gLog.info( "Repository vendor: " + gRepository.getDescriptor( Repository.REP_VENDOR_DESC ));

      gEventManager = new CARS_EventManager();
      gSystemLoginSession       = gRepository.login( pCreds );
      gSystemAccessSession      = gRepository.login( pCreds );
      gSystemApplicationSession = gRepository.login( pCreds );
      gSystemToolSession        = gRepository.login( pCreds );
      gSystemCarsSession        = gRepository.login( pCreds );
      gObservationSession       = gRepository.login( pCreds );

      CacheManager cache = ((RepositoryImpl)gSystemApplicationSession.getRepository()).getCacheManager();
      cache.setMaxMemory( 16*1024*1024 );
//      cache.setMaxMemory( 1 );
      cache.setMaxMemoryPerCache( 4*1024*1024 );
      cache.setMinMemoryPerCache( 1*1024*1024 );
      
      NamespaceRegistry nsReg = gSystemCarsSession.getWorkspace().getNamespaceRegistry();
      String[] ns = gRepNamespaces.split( "," );
      addNamespaces( nsReg, ns );
      String[] cnds = gRepCNDFiles.split( "," );
      addNodeTypesDefinitions( gSystemCarsSession, cnds );
    } finally {
//      CARS_LoginModule.gSuperuserAllowed = false;
    }
    return;
  }

  @Override
  public void initLogging() throws Exception {
    File f = new File( gRepLogHome );
    if (!f.exists()) {
      if (!f.mkdirs()) {
        throw new Exception( "Cannot create directory: " + f.getCanonicalPath() );
      }
    }
    Handler fh = new FileHandler( f.getAbsolutePath() + "/jecars.log", false );
    fh.setFormatter( new SimpleFormatter() );
    fh.setLevel( gLogLevel );
    gLog.addHandler( fh );
    Properties p = System.getProperties();
    p.put( "derby.stream.error.file", f.getAbsolutePath() + "/derby.log" );
    Properties props = new Properties();
    props.setProperty( "log4j.logger.org.apache.jackrabbit.core", "INFO, A1" );
    props.setProperty( "log4j.appender.A1", "org.apache.log4j.FileAppender" );
    props.setProperty( "log4j.appender.A1.file", f.getAbsolutePath() + "/jackrabbit.log" );
    props.setProperty( "log4j.appender.A1.layout", "org.apache.log4j.PatternLayout" );
    PropertyConfigurator.configure( props );
    return;
  }

  @Override
  public void init(CARS_Credentials pCreds, boolean pReinit) throws Exception {
    
    final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( "org.apache.jackrabbit.core.TransientRepository" );
    logger.setLevel( org.apache.log4j.Level.ERROR );
      
    if (pCreds==null) {
      pCreds = new CARS_Credentials( "Superuser", "pw".toCharArray(), null );
    }
    gSysCreds = pCreds;
    initJCR( gSysCreds );
    final Node rootNode = gSystemCarsSession.getRootNode();
    Node n = gSystemCarsSession.getRootNode();
    if (pReinit) {
      if (n.hasNode( CARS_Definitions.MAINFOLDER )) {
        n.getNode( CARS_Definitions.MAINFOLDER ).remove();
      }
      n.save();
    }
    final Calendar cal = Calendar.getInstance();
    if (!n.hasNode( CARS_Definitions.MAINFOLDER )) {
      n.addNode( CARS_Definitions.MAINFOLDER, "jecars:main" );
    }
    n = n.getNode( CARS_Definitions.MAINFOLDER );
    n.setProperty( "jecars:Started", cal );

    Node internalSource;
    if (!n.hasNode( "Trashcans" )) {
      Node trashcans = n.addNode( "Trashcans", "jecars:Trashcan" );
      Node gt = trashcans.addNode( "General", "jecars:Trashcan" );
      gt.setProperty( "jecars:Body", "General default trashcan" );
    }
    // **********************************
    // **** Add system resources
    {
      if (!n.hasNode( "Systems" )) {
        Node systems = n.addNode( "Systems", "jecars:RES_Systems" );
        systems.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_SystemsApp" );
      }
    }
    
    if (!rootNode.hasNode( "shared" )) {
      final Node ext = rootNode.addNode( "shared", "jecars:CARS_Interface" );
      ext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_SharedApp" );
    }

    if (!rootNode.hasNode( "accounts" )) {
      final Node ext = rootNode.addNode( "accounts", "jecars:CARS_Interface" );
      ext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_AccountsApp" );
//      ext.addNode( "login", "jecars:root" );
    }
    final Node accountsNode = rootNode.getNode( "accounts" );
    
    if (!n.hasNode( "UserSources" )) {
      final Node ext = n.addNode( "UserSources", "jecars:UserSources" );
      ext.addNode( "rest", "jecars:UserSource" );
      internalSource = ext.addNode( "internal", "jecars:UserSource" );
    }
    internalSource = n.getNode( "UserSources/internal" );
    if (!n.hasNode( "GroupSources" )) {
      final Node ext = n.addNode( "GroupSources", "jecars:GroupSources" );
      Node internalGroup = ext.addNode( "internal", "jecars:GroupSource" );
    }
    Node def;
    if (!n.hasNode( "default" )) {
      def = n.addNode( "default", "jecars:workspace" );
    }
    def = n.getNode( "default" );
    if (!def.hasNode( "Users" )) {
      def.addNode( "Users", "jecars:Users" );
    }
    final Node users = def.getNode( "Users" );
    if (!users.hasNode( "Superuser" )) {
      // **** Superuser
      Node su = users.addNode( "Superuser", "jecars:User" );
      su.setProperty( "jecars:Fullname", "Superuser" );
      su.setProperty( "jecars:Source", internalSource.getPath() );
      su.setProperty( "jecars:Password_crypt", CARS_PasswordService.getInstance().encrypt("pw") );
    }
    if (!users.hasNode( "Administrator" )) {
      // **** Administrator
      final Node admin = users.addNode( "Administrator" );
      admin.setProperty( "jecars:Fullname", "Administrator" );
      admin.setProperty( "jecars:Source", internalSource.getPath() );
      CARS_DefaultMain.setCryptedProperty( admin, "jecars:Password_crypt", "admin" );
//      admin.setProperty( "jecars:Password_crypt", CARS_PasswordService.getInstance().encrypt("admin") );
    }
    if (!users.hasNode( "anonymous" )) {
      // **** anonymous
      final Node anon = users.addNode( "anonymous" );
      anon.setProperty( "jecars:Fullname", "anonymous" );
      anon.setProperty( "jecars:Source", internalSource.getPath() );
      CARS_DefaultMain.setCryptedProperty( anon, "jecars:Password_crypt", "anonymous" );
//      anon.setProperty( "jecars:Password_crypt", CARS_PasswordService.getInstance().encrypt("anonymous") );
    }

    if (!accountsNode.hasNode( "jecars:P_anonymous" )) {
      Node p = accountsNode.addNode( "jecars:P_anonymous", "jecars:Permission" );
      final String[] r = {"read","add_node","get_property"};
      p.setProperty( "jecars:Actions", r );
      final Node anon = users.getNode( "anonymous" );
      final Value[] anons = {gSystemCarsSession.getValueFactory().createValue( anon.getPath() )};
      p.setProperty( "jecars:Principal", anons );
      p = rootNode.addNode( "jecars:P_anonymous", "jecars:Permission" );
      final String[] rr = {"read"};
      p.setProperty( "jecars:Actions", rr );
      p.setProperty( "jecars:Principal", anons );
    }

    if (!def.hasNode( "Data" )) {
      Node df = def.addNode( "Data", "jecars:datafolder" );
    }
    if (!def.hasNode( "Events" )) {
      Node df = def.addNode( "Events", "jecars:EventsFolder" );
    }
    final Node events = def.getNode( "Events" );
    if (!events.hasNode( "Applications" )) {
      Node df = events.addNode( "Applications", "jecars:EventsFolder" );
      df = df.addNode( "Directory", "jecars:EventsFolder" );
    }
    if (!events.hasNode( "System" )) {
      Node df = events.addNode( "System", "jecars:SystemEventsFolder" );
    }
    
    final Node su    = users.getNode( "Superuser" );
    final Node admin = users.getNode( "Administrator" );
    
    if (!def.hasNode( "Groups" )) {
      // **** Groups
      def.addNode( "Groups", "jecars:Groups" );
    }

    final Node groups = def.getNode( "Groups" );
    if (!groups.hasNode( "World" )) {
      // **** World
      final Node world = groups.addNode( "World", "jecars:Group" );
      world.setProperty( "jecars:Fullname", "World (all users)" );
      world.setProperty( "jecars:Source", "/JeCARS/GroupSources/internal" );
      final ArrayList<Value> l = new ArrayList<Value>();
      Value v = world.getSession().getValueFactory().createValue( su.getPath() );
      l.add( v );
      v = world.getSession().getValueFactory().createValue( admin.getPath() );
      l.add( v );
      world.setProperty( "jecars:GroupMembers",l.toArray(new Value[0]) );
    }
    if (!groups.hasNode( "Admins" )) {
      // **** Admins
      final Node admins = groups.addNode( "Admins", "jecars:Group" );
      admins.setProperty( "jecars:Fullname", "Administrators" );
      admins.setProperty( "jecars:Source", "/JeCARS/GroupSources/internal" );
      final ArrayList<Value> l = new ArrayList<Value>();
      Value v = admins.getSession().getValueFactory().createValue( su.getPath() );
      l.add( v );
      v = admins.getSession().getValueFactory().createValue( admin.getPath() );
      l.add( v );
      admins.setProperty( "jecars:GroupMembers",l.toArray(new Value[0]) );
    }
    if (!groups.hasNode( "EventsAppUsers" )) {
      // **** EventsAppUsers
      final Node eau = groups.addNode( "EventsAppUsers", "jecars:Group" );
      eau.setProperty( "jecars:Fullname", "Members of this group can use the EventsApp application" );
      eau.setProperty( "jecars:Source", "/JeCARS/GroupSources/internal" );
      CARS_Utils.setCurrentModificationDate( groups );
    }
    final Node eau    = def.getNode( "Groups/EventsAppUsers" );
    final Node world  = def.getNode( "Groups/World" );
    final Node admins = def.getNode( "Groups/Admins" );
    if (!rootNode.hasNode( "jecars:P_Admins" )) {
      final Node p = rootNode.addNode( "jecars:P_Admins", "jecars:Permission" );
      p.setProperty( "jecars:Delegate", true );
      final String[] r = {"read","add_node","set_property","get_property","remove","acl_read","acl_edit"};
      p.setProperty( "jecars:Actions", r );
      final Value[] adminsV = {gSystemCarsSession.getValueFactory().createValue( admins.getPath() )};
      p.setProperty( "jecars:Principal", adminsV );
    }
    
    // **** Application sources
    n = gSystemCarsSession.getRootNode().getNode( "JeCARS" );
    if (!n.hasNode( "ApplicationSources" )) {
      final Node ext = n.addNode( "ApplicationSources", "jecars:ApplicationSources" );
      Node appext = ext.addNode( "AdminApp", "jecars:CARS_Interface" );
      appext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_AdminApp" );
      appext = ext.addNode( "ToolsApp", "jecars:CARS_Interface" );
      appext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_ToolsApp" );
      CARS_Utils.setCurrentModificationDate( ext );
    }
    final Node asn = n.getNode( "ApplicationSources" );
    // **** Always rebuild eventsapp
    if (asn.hasNode( "EventsApp" )) {
      asn.getNode( "EventsApp" ).remove();
      asn.save();
    }
    if (!asn.hasNode( "EventsApp" )) {
      // **** Event application
      final Node appext = asn.addNode( "EventsApp", "jecars:CARS_Interface" );
      appext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_EventsApp" );
      CARS_Utils.setCurrentModificationDate( asn );
    }
    final Node eventsApp = asn.getNode( "EventsApp" );
    if (!eventsApp.hasNode( "jecars:P_EventsAppUsers" )) {
      CARS_Utils.addPermission( eventsApp, "EventsAppUsers", null, "read,get_property,add_node" );
    }

    // **** Always rebuild infoapp
    if (asn.hasNode( "InfoApp" )) {
      asn.getNode( "InfoApp" ).remove();
      asn.save();
    }
    if (!asn.hasNode( "InfoApp" )) {
      // **** Info application
      final Node appext = asn.addNode( "InfoApp", "jecars:CARS_Interface" );
      appext.setProperty( "jecars:InterfaceClass", "org.jecars.apps.CARS_InfoApp" );
      CARS_Utils.setCurrentModificationDate( asn );
      try {
        CARS_Utils.addPermission( appext, "DefaultReadGroup", null, "read,get_property,delegate" );
      } catch( PathNotFoundException pnfe ) {
        gLog.log( Level.WARNING, pnfe.getMessage() + " not found, InfoApp will be added after initialization of JeCARS" );
        appext.remove();
      }
    }

    gSystemCarsSession.save();
    
    // **** Check user/password
    if (def.hasNode( "Users/" + pCreds.getUserID() )) {
      final Node user = def.getNode( "Users/" + pCreds.getUserID() );
      if (!user.getProperty( "jecars:Password_crypt" ).getString().equals(
              CARS_PasswordService.getInstance().encrypt(new String(pCreds.getPassword()) ))) {
        throw new Exception( "Invalid password" );        
      }
    } else {
      throw new Exception( "User unknown: " + pCreds.getUserID() );
    }
    

    // **** Add logger for JeCARS events
    final Handler jecarsH = new CARS_LogHandler();
    final Logger globalLogger = Logger.getLogger( "" );
    globalLogger.addHandler( jecarsH );

    // **** Init the application sources
//    initApplicationSources( gSystemCarsSession );
    initApplicationSources( null );
    CARS_AdminApp.autoStartTools();
    return;
  }

  @Override
  public ICARS_Session getSessionInterface() {
    return new JackrabbitSessionInterface();
  }
  

}
