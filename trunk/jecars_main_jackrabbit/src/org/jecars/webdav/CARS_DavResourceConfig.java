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
package org.jecars.webdav;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import org.apache.jackrabbit.server.io.PropertyManager;
import org.apache.jackrabbit.webdav.simple.ResourceConfig;

/** CARS_DavResourceConfig
 *
 *  @version $Id: CARS_DavResourceConfig.java,v 1.1 2009/03/10 15:09:20 weertj Exp $
 */
public class CARS_DavResourceConfig extends ResourceConfig {

  protected static final Logger gLog = java.util.logging.Logger.getLogger( CARS_DavResourceConfig.class.getPackage().getName() );

  // **** v2.0
  public CARS_DavResourceConfig( final org.apache.tika.detect.Detector pDetector ) {
    super( pDetector );
  }


  /** isCollectionResource
   *
   * @param pItem
   * @return
   */
  @Override
  public boolean isCollectionResource( Item pItem ) {
    if (pItem.isNode()) {
      Node n = (Node)pItem;
      try {
        if (n.isNodeType( "jecars:configresource")) {
          return true;
        }
        if (n.isNodeType( "nt:resource" )) {
          return false;
        }
      } catch( RepositoryException e ) {
        gLog.log( Level.WARNING, e.getMessage(), e );
      }
      return true;
    }
    return false;
  }

  @Override
  public PropertyManager getPropertyManager() {
        return super.getPropertyManager();
  }



}
