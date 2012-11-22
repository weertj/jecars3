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
package org.jecars.backup;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;

/**
 * startExport
 * 
 * @version $Id: startExport.java,v 1.1 2007/10/02 13:03:48 weertj Exp $
 */
public class startExport {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException, LoginException, RepositoryException {
        try {
          JB_Session session = new JB_Session();
          session.setRepDirectory( "C:/Dev/Data/cars/jackrabbit" );
          JB_ExportData export = new JB_ExportData();
//          fos = new FileOutputStream("test.jb");
          JB_Options options = new JB_Options();
          options.setExportDirectory( new File( "exportTest" ));
          options.addExcludePath( "/jcr:system.*" );
          export.exportToDirectory( session.getSession().getRootNode(), options );
        } catch (Exception ex) {
          Logger.getLogger(startExport.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
      }
        
  
}
