/*
 * Copyright 2012-2013 NLR - National Aerospace Laboratory
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
package org.jecars.sandy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jecars.servlets.JeCARS_RESTServlet;

/**
 *
 * @author weert
 */
public class startSandy {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
      try {
        System.out.println("Sandy server 10042012-1704");
          
        Properties settings = new Properties();
        File setf = new File( "settings.properties" );
        if (setf.isFile()) {
          try {
            FileInputStream options = new FileInputStream( setf );
            settings.load( options );
            options.close();
          } catch(IOException e) {
            e.printStackTrace();
          }
        } else {
          System.out.println( "INFO: " + setf.getAbsolutePath() + " not found");
        }
        
        final Server server = new Server( Integer.parseInt( settings.getProperty( "JECARS_PORT", "8080" ) ));

//        QueuedThreadPool threadPool = new QueuedThreadPool();
//        threadPool.setMaxThreads(1);
//        server.setThreadPool(threadPool);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        JeCARS_RESTServlet rs = new JeCARS_RESTServlet();
        rs.setLongPolling( new SDY_LongPolling() );
        ServletHolder sh = new ServletHolder( rs );
        sh.setInitParameter( "JECARS_CONFIGFILE", settings.getProperty( "JECARS_CONFIGFILE", "data/cars_repository.xml" ));
        sh.setInitParameter( "JECARS_REPHOME",    settings.getProperty( "JECARS_REPHOME", "data/rep" ) );
        sh.setInitParameter( "JECARS_REPLOGHOME", settings.getProperty( "JECARS_REPLOGHOME", "data/logs" ) );
        sh.setInitParameter( "JECARS_NAMESPACES", settings.getProperty( "JECARS_NAMESPACES", "jcr,http://www.jcp.org/jcr,jecars,http://jecars.org" ) );
        sh.setInitParameter( "JECARS_CNDFILES",   settings.getProperty( "JECARS_CNDFILES", "/org/jecars/jcr/nodetype/jecars.cnd,jecars.cnd" ) );
        sh.setInitParameter( "ENABLE_FILE_LOG",   settings.getProperty( "ENABLE_FILE_LOG", "false" ) );
        sh.setInitParameter( "EVENT_LOG_FILE",    settings.getProperty( "EVENT_LOG_FILE", "" ) );
//        sh.setInitParameter( "JECARS_CONFIGFILE",   "data/cars_repository.xml" );
//        sh.setInitParameter( "JECARS_REPHOME",      "data/rep" );
//        sh.setInitParameter( "JECARS_REPLOGHOME",   "data/logs" );
//        sh.setInitParameter( "JECARS_NAMESPACES",   "jcr,http://www.jcp.org/jcr,jecars,http://jecars.org" );
//        sh.setInitParameter( "JECARS_CNDFILES",     "/org/jecars/jcr/nodetype/jecars.cnd,jecars.cnd" );
//        sh.setInitParameter( "JECARS_NAMESPACES",   "jcr,http://www.jcp.org/jcr,jecars,http://jecars.org,helena,http://www.nlr.nl" );
//        sh.setInitParameter( "JECARS_CNDFILES",     "/org/jecars/jcr/nodetype/jecars.cnd,jecars.cnd,/nl/nlr/helena/nodetype/helena.cnd,helena.cnd" );
        context.addServlet( sh, settings.getProperty( "JECARS_SERVLETPATH", "/cars/*" ));
        server.start();
        server.join();
      } catch( Exception e ) {
        e.printStackTrace();
      }
    }
}
