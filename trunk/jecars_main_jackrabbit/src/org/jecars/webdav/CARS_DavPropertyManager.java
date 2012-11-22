/*
 * Copyright 2010 NLR - National Aerospace Laboratory
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

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.security.auth.login.CredentialExpiredException;
import org.apache.jackrabbit.server.io.PropertyExportContext;
import org.apache.jackrabbit.server.io.PropertyHandler;
import org.apache.jackrabbit.server.io.PropertyImportContext;
import org.apache.jackrabbit.server.io.PropertyManager;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Definitions;

/**
 *
 * @author weert
 */
public class CARS_DavPropertyManager implements PropertyManager {

  private final CARS_DavResource mResource;
    
  /** CARS_DavPropertyManager
   * 
   * @param pResource
   */
  public CARS_DavPropertyManager( final CARS_DavResource pResource ) {
    mResource = pResource;
    return;
  }


  /** exportProperties
   *
   * @param pec
   * @param bln
   * @return
   * @throws RepositoryException
   */
  @Override
  public boolean exportProperties(PropertyExportContext pec, boolean bln) throws RepositoryException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /** alterProperties
   *
   * @param pPIC
   * @param pBool
   * @return
   * @throws RepositoryException
   * @throws AccessDeniedException
   */
  @Override
  public Map alterProperties( final PropertyImportContext pPIC, boolean pBool ) throws RepositoryException, AccessDeniedException {
    final Map failures = new HashMap();
    final List l = pPIC.getChangeList();
//      System.out.println("set for = " + mResource.getNode().getPath() );
    final CARS_DavSession ses = (CARS_DavSession)mResource.getSession();
    final CARS_ActionContext ac = ses.getActionContext();
    final StringBuilder query = new StringBuilder();
    for( final Object o : l ) {
      if (o instanceof DefaultDavProperty) {
        final DefaultDavProperty ddp = (DefaultDavProperty)o;
        final DavPropertyName dpn = ddp.getName();
//        if (dpn.getNamespace().isSame( "" )) {
//          throw new RepositoryException( "null Namespace not allowed" );
//        }
        final String namespace = dpn.getNamespace().getURI();
        final String name      = dpn.getName();
        final Object value     = ddp.getValue();
//        System.out.println("iijdji name " + name + " = " + value );
        query.append( CARS_Definitions.DEFAULTNS ).append( name ).append( '=' ).append( value ).append( '&' );
        query.append( CARS_Definitions.DEFAULTNS ).append( name + "_NS" ).append( '=' ).append( namespace ).append( '&' );
      } else if (o instanceof DavPropertyName) {
        final DavPropertyName dpn = (DavPropertyName)o;
        final String name  = dpn.getName();
        query.append( CARS_Definitions.DEFAULTNS ).append( name ).append( '=' ).append( CARS_DefaultMain.PREFIX_VALUE_REMOVE ).append( '&' );
        query.append( CARS_Definitions.DEFAULTNS ).append( name + "_NS" ).append( '=' ).append( CARS_DefaultMain.PREFIX_VALUE_REMOVE ).append( '&' );
//        System.out.println("dddiijdji name " + dpn.getName()  );
//      } else {
//        System.out.println("dffddfdfame " + o  );
      }
    }
    ac.setQueryString( query.toString() );
    try {
      final Node node = mResource.getNode();
      if (!node.isNodeType( "jecars:mixin_unstructured" )) {
        node.addMixin( "jecars:mixin_unstructured" );
        node.save();
      }

      ac.setError( null );
      ac.setErrorCode( HttpURLConnection.HTTP_OK );
      ses.getFactory().performPutAction( ac, ac.getMain() );
      if (ac.getErrorCode()!=HttpURLConnection.HTTP_OK) {
        throw new RepositoryException( ac.getError() );
      }
    } catch( CredentialExpiredException ce ) {
      throw new AccessDeniedException( ce );
    }
    return failures;
  }

  @Override
  public void addPropertyHandler(PropertyHandler ph) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PropertyHandler[] getPropertyHandlers() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
