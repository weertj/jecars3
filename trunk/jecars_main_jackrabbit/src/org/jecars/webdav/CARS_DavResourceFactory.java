/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Copyright 2009 NLR - National Aerospace Laboratory
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
import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.jcr.JcrDavSession;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.webdav.simple.ResourceConfig;
import org.apache.jackrabbit.webdav.simple.VersionControlledResourceImpl;
import org.apache.jackrabbit.webdav.simple.VersionHistoryResourceImpl;
import org.apache.jackrabbit.webdav.simple.VersionResourceImpl;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Factory;

/** CARS_DavResourceFactory
 *
 *  @version $Id: CARS_DavResourceFactory.java,v 1.1 2009/03/10 15:09:20 weertj Exp $
 */
public class CARS_DavResourceFactory implements DavResourceFactory {

    private static Logger log =LoggerFactory.getLogger(CARS_DavResourceFactory.class);

    private final LockManager lockMgr;
    private final ResourceConfig resourceConfig;

    /**
     * Create a new <code>ResourceFactory</code> that uses the given lock
     * manager and the default {@link ResourceConfig resource config}.
     *
     * @param lockMgr
     */
    public CARS_DavResourceFactory(LockManager lockMgr) {
        this.lockMgr = lockMgr;
// v2.0
        this.resourceConfig = new ResourceConfig( null );
//        this.resourceConfig = new ResourceConfig();
    }

    /**
     * Create a new <code>ResourceFactory</code> that uses the given lock
     * manager and resource filter.
     *
     * @param lockMgr
     * @param resourceConfig
     */
    public CARS_DavResourceFactory(LockManager lockMgr, ResourceConfig resourceConfig) {
        this.lockMgr = lockMgr;
// v2.0
        this.resourceConfig = (resourceConfig != null) ? resourceConfig : new ResourceConfig( null );
//        this.resourceConfig = (resourceConfig != null) ? resourceConfig : new ResourceConfig();
    }

    @Override
    public DavResource createResource(DavResourceLocator locator, DavServletRequest request,
                                      DavServletResponse response ) throws DavException {
      return createResource( locator, request, response, null, null );
    }

    /**
     * Create a new <code>DavResource</code> from the given locator and
     * request.
     *
     * @param locator
     * @param request
     * @param response
     * @return DavResource
     * @throws DavException
     * @see DavResourceFactory#createResource(DavResourceLocator,
     *      DavServletRequest, DavServletResponse)
     */
    public DavResource createResource( final DavResourceLocator locator, final DavServletRequest request,
                                       final DavServletResponse response, final CARS_ActionContext pAC, final CARS_Factory pFactory ) throws DavException {
        try {
            final Node node = getNode( request.getDavSession(), locator, pAC, pFactory );
            CARS_DavResource resource;
            if (node == null) {
              log.debug("Creating resource for non-existing repository node.");
              final boolean isCollection = DavMethods.isCreateCollectionRequest(request);
              resource = (CARS_DavResource)createNullResource(locator, request.getDavSession(), isCollection);
            } else {
              resource = (CARS_DavResource)createResource(node, locator, request.getDavSession());
            }
            resource.addLockManager(lockMgr);
            return resource;
        } catch (Exception e) {
//          e.printStackTrace();
            throw new DavException( 500, e);
        }
    }

    /**
     * Create a new <code>DavResource</code> from the given locator and webdav
     * session.
     *
     * @param locator
     * @param session
     * @return
     * @throws DavException
     * @see DavResourceFactory#createResource(DavResourceLocator, DavSession)
     */
    public DavResource createResource(DavResourceLocator locator, DavSession session) throws DavException {
        try {
            CARS_DavSession cds = (CARS_DavSession)session;
            Node node = getNode( cds, locator, cds.getActionContext(), cds.getFactory() );
            DavResource resource = createResource(node, locator, cds );
            resource.addLockManager(lockMgr);
            return resource;
        } catch (Exception e) {
            throw new DavException( 500, e);
        }
    }

    /** getRepoPath
     * 
     * @param pLocator
     * @return
     */
    static public String getRepoPath( final DavResourceLocator pLocator ) {
      String repoPath = pLocator.getRepositoryPath();
      final String wsPath = pLocator.getWorkspacePath();
      if (!"/webdav".equals( wsPath )) {
        repoPath = wsPath + repoPath;
      }
      return repoPath;
    }


    /**
     * Returns the <code>Node</code> corresponding to the given locator or
     * <code>null</code> if it does not exist or if the existing item represents
     * a <code>Property</code>.
     *
     * @param sessionImpl
     * @param locator
     * @return
     * @throws RepositoryException
     */
    private Node getNode( final DavSession sessionImpl, final DavResourceLocator locator, CARS_ActionContext pAC, CARS_Factory pFactory )
                   throws Exception {
      Node node = null;
//      try {
        final CARS_DavSession cds = (CARS_DavSession)sessionImpl;
        if (pAC     ==null) pAC      = cds.getActionContext();
        if (pFactory==null) pFactory = cds.getFactory();
//        final String repoPath = locator.getResourcePath();
        String repoPath = getRepoPath( locator );
//        String repoPath = locator.getRepositoryPath();
//        final String wsPath = locator.getWorkspacePath();
//        if (!"/webdav".equals( wsPath )) {
//          repoPath = wsPath + repoPath;
//        }
//        if (repoPath.startsWith( "/webdav" )) {
//          repoPath = repoPath.substring( "/webdav".length() );
//        }
//        Session session = CARS_Factory.getSystemApplicationSession();
//        node = (Node)session.getItem(repoPath);
        pAC.setPathInfo( repoPath );
        pAC.setError( null );
        pAC.setErrorCode( HttpURLConnection.HTTP_OK );
        pFactory.performGetAction( pAC, pAC.getMain() );
        if (pAC.getErrorCode()==HttpURLConnection.HTTP_OK) {
          node = pAC.getThisNode();
        }
//        pAC.getMain().destroy();
//      } catch (PathNotFoundException e) {
//        e.printStackTrace();
        // item does not exist (yet). return null -> create null-resource
//      }
      return node;
    }

    /**
     * Create a 'null resource'
     *
     * @param locator
     * @param session
     * @param isCollection
     * @return
     * @throws DavException
     */
    private DavResource createNullResource(DavResourceLocator locator,
                                           DavSession session,
                                           boolean isCollection) throws DavException {
        JcrDavSession.checkImplementation(session);
        JcrDavSession sessionImpl = (JcrDavSession)session;

        DavResource resource;
//        if (versioningSupported(sessionImpl.getRepositorySession())) {
//            resource = new VersionControlledResourceImpl(locator, this, sessionImpl, resourceConfig, isCollection);
//        } else {
        resource = new CARS_DavResource(locator, this, sessionImpl, resourceConfig, isCollection);
//        }
        return resource;
    }

    /**
     * Tries to retrieve the repository item defined by the locator's resource
     * path and build the corresponding WebDAV resource. If the repository
     * supports the versioning option different resources are created for
     * version, versionhistory and common nodes.
     *
     * @param node
     * @param locator
     * @param session
     * @return
     * @throws DavException
     */
    private DavResource createResource(Node node, DavResourceLocator locator,
                                       DavSession session) throws DavException {
        DavResource resource;
        resource = new CARS_DavResource(locator, this, session, resourceConfig, node);
        return resource;
    }

    /**
     * @param repoSession
     * @return true if the JCR repository supports versioning.
     */
    private static boolean versioningSupported(Session repoSession) {
        String desc = repoSession.getRepository().getDescriptor(Repository.OPTION_VERSIONING_SUPPORTED);
        return Boolean.valueOf(desc).booleanValue();
    }
}
