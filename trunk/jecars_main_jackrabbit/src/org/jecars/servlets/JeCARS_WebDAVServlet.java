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
 * Copyright 2007-2009 NLR - National Aerospace Laboratory
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
package org.jecars.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.jackrabbit.server.BasicCredentialsProvider;
import org.apache.jackrabbit.server.CredentialsProvider;
import org.apache.jackrabbit.server.SessionProvider;
import org.apache.jackrabbit.server.SessionProviderImpl;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavSessionProvider;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.SimpleLockManager;
import org.apache.jackrabbit.webdav.server.AbstractWebdavServlet;
import org.apache.jackrabbit.webdav.simple.*;

import javax.jcr.Repository;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.login.CredentialExpiredException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.jackrabbit.webdav.DavCompliance;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavMethods;
import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.bind.BindInfo;
import org.apache.jackrabbit.webdav.bind.BindableResource;
import org.apache.jackrabbit.webdav.bind.RebindInfo;
import org.apache.jackrabbit.webdav.bind.UnbindInfo;
import org.apache.jackrabbit.webdav.header.OverwriteHeader;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.InputContextImpl;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.io.OutputContextImpl;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.observation.EventDiscovery;
import org.apache.jackrabbit.webdav.observation.ObservationResource;
import org.apache.jackrabbit.webdav.observation.Subscription;
import org.apache.jackrabbit.webdav.observation.SubscriptionInfo;
import org.apache.jackrabbit.webdav.ordering.OrderPatch;
import org.apache.jackrabbit.webdav.ordering.OrderingResource;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropContainer;
import org.apache.jackrabbit.webdav.search.SearchConstants;
import org.apache.jackrabbit.webdav.search.SearchInfo;
import org.apache.jackrabbit.webdav.search.SearchResource;
import org.apache.jackrabbit.webdav.security.AclProperty;
import org.apache.jackrabbit.webdav.security.AclResource;
import org.apache.jackrabbit.webdav.transaction.TransactionInfo;
import org.apache.jackrabbit.webdav.transaction.TransactionResource;
import org.apache.jackrabbit.webdav.version.ActivityResource;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.DeltaVResource;
import org.apache.jackrabbit.webdav.version.LabelInfo;
import org.apache.jackrabbit.webdav.version.MergeInfo;
import org.apache.jackrabbit.webdav.version.OptionsInfo;
import org.apache.jackrabbit.webdav.version.OptionsResponse;
import org.apache.jackrabbit.webdav.version.UpdateInfo;
import org.apache.jackrabbit.webdav.version.VersionControlledResource;
import org.apache.jackrabbit.webdav.version.VersionResource;
import org.apache.jackrabbit.webdav.version.VersionableResource;
import org.apache.jackrabbit.webdav.version.report.Report;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.tika.detect.Detector;
import org.apache.tika.mime.MimeTypesFactory;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.webdav.CARS_DavRequestImpl;
import org.jecars.webdav.CARS_DavResource;
import org.jecars.webdav.CARS_DavResourceConfig;
import org.jecars.webdav.CARS_DavResourceFactory;
import org.jecars.webdav.CARS_DavSessionProvider;
import org.w3c.dom.Document;

/**
 * WebdavServlet provides webdav support (level 1 and 2 complient) for
 * repository resources.
 * <p>
 * Implementations of this abstract class must implement the
 * {@link #getRepository()} method to access the repository.
 */
public class JeCARS_WebDAVServlet extends AbstractWebdavServlet {

    protected static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger( JeCARS_WebDAVServlet.class.getPackage().getName() );

    /**
     * init param name of the repository prefix
     */
    public static final String INIT_PARAM_RESOURCE_PATH_PREFIX = "resource-path-prefix";

    /**
     * Name of the optional init parameter that defines the value of the
     * 'WWW-Authenticate' header.<p/>
     * If the parameter is omitted the default value
     * {@link #DEFAULT_AUTHENTICATE_HEADER "Basic Realm=Jackrabbit Webdav Server"}
     * is used.
     *
     * @see #getAuthenticateHeaderValue()
     */
    public static final String INIT_PARAM_AUTHENTICATE_HEADER = "authenticate-header";

    /** the 'missing-auth-mapping' init parameter */
    public final static String INIT_PARAM_MISSING_AUTH_MAPPING = "missing-auth-mapping";

    public final static String INIT_PARAM_MIME_INFO = "mime-info";

    /**
     * Name of the init parameter that specify a separate configuration used
     * for filtering the resources displayed.
     */
    public static final String INIT_PARAM_RESOURCE_CONFIG = "resource-config";

    /**
     * Servlet context attribute used to store the path prefix instead of
     * having a static field with this servlet. The latter causes problems
     * when running multiple
     */
    public static final String CTX_ATTR_RESOURCE_PATH_PREFIX = "jackrabbit.webdav.simple.resourcepath";

    /**
     * the pResource path prefix
     */
    private String resourcePathPrefix;

    /**
     * Header value as specified in the {@link #INIT_PARAM_AUTHENTICATE_HEADER} parameter.
     */
    private String authenticate_header;

    /**
     * Map used to remember any webdav lock created without being reflected
     * in the underlying repository.
     * This is needed because some clients rely on a successful locking
     * mechanism in order to perform properly (e.g. mac OSX built-in dav client)
     */
    private LockManager lockManager;

    /**
     * the pResource factory
     */
    private DavResourceFactory resourceFactory;

    /**
     * the locator factory
     */
    private DavLocatorFactory locatorFactory;

    /**
     * the webdav session provider
     */
    private DavSessionProvider davSessionProvider;

    /**
     * the repository session provider
     */
    private SessionProvider sessionProvider;

    /**
     * The config
     */
    private CARS_DavResourceConfig config;

    /** init
     *
     * @param pSC
     */
    protected void init( final ServletContext pSC, final JeCARS_RESTServlet pServlet ) {
      resourcePathPrefix = pServlet.getInitParameter( INIT_PARAM_RESOURCE_PATH_PREFIX );
      if (resourcePathPrefix==null) {
        resourcePathPrefix = pServlet.getResourcePathPrefix();
        if (resourcePathPrefix==null) {
          LOG.warning( "Missing path prefix > setting to empty string. (resource-path-prefix)" );
          resourcePathPrefix = "";
        }
      } else if (resourcePathPrefix.endsWith("/")) {
        LOG.warning( "Path prefix ends with '/' > removing trailing slash." );
        resourcePathPrefix = resourcePathPrefix.substring(0, resourcePathPrefix.length() - 1);
      }
      return;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isPreconditionValid(WebdavRequest request,
                                          DavResource resource) {
        return !resource.exists() || request.matchesIfHeader(resource);
    }

    /**
     * Returns the configured path prefix
     *
     * @return resourcePathPrefix
     * @see #INIT_PARAM_RESOURCE_PATH_PREFIX
     */
    public String getPathPrefix() {
        return resourcePathPrefix;
    }

    /**
     * Returns the configured path prefix
     *
     * @return resourcePathPrefix
     * @see #INIT_PARAM_RESOURCE_PATH_PREFIX
     */
    public static String getPathPrefix(ServletContext ctx) {
        return (String) ctx.getAttribute(CTX_ATTR_RESOURCE_PATH_PREFIX);
    }

    /**
     * Returns the <code>DavLocatorFactory</code>. If no locator factory has
     * been set or created a new instance of {@link org.apache.jackrabbit.webdav.simple.LocatorFactoryImpl} is
     * returned.
     *
     * @return the locator factory
     * @see AbstractWebdavServlet#getLocatorFactory()
     */
    public DavLocatorFactory getLocatorFactory() {
        if (locatorFactory == null) {
            locatorFactory = new LocatorFactoryImplEx(resourcePathPrefix);
        }
        return locatorFactory;
    }

    /**
     * Sets the <code>DavLocatorFactory</code>.
     *
     * @param locatorFactory
     * @see AbstractWebdavServlet#setLocatorFactory(DavLocatorFactory)
     */
    public void setLocatorFactory(DavLocatorFactory locatorFactory) {
        this.locatorFactory = locatorFactory;
    }

    /**
     * Returns the <code>LockManager</code>. If no lock manager has
     * been set or created a new instance of {@link SimpleLockManager} is
     * returned.
     *
     * @return the lock manager
     */
    public LockManager getLockManager() {
        if (lockManager == null) {
            lockManager = new SimpleLockManager();
        }
        return lockManager;
    }

    /**
     * Sets the <code>LockManager</code>.
     *
     * @param lockManager
     */
    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    /**
     * Returns the <code>DavResourceFactory</code>. If no pRequest factory has
     * been set or created a new instance of {@link ResourceFactoryImpl} is
     * returned.
     *
     * @return the pResource factory
     * @see AbstractWebdavServlet#getResourceFactory()
     */
    @Override
    public DavResourceFactory getResourceFactory() {
        if (resourceFactory == null) {
          try {
            resourceFactory = new CARS_DavResourceFactory(getLockManager(), getResourceConfig());
          } catch( ServletException se ) {
            se.printStackTrace();
          }
        }
        return resourceFactory;
    }

    /**
     * Sets the <code>DavResourceFactory</code>.
     *
     * @param resourceFactory
     * @see AbstractWebdavServlet#setResourceFactory(org.apache.jackrabbit.webdav.DavResourceFactory)
     */
    public void setResourceFactory(DavResourceFactory resourceFactory) {
        this.resourceFactory = resourceFactory;
    }

    private Detector getDetector() throws ServletException {
      return MimeTypesFactory.create();
      /**
      URL url;
      String mimeInfo = getInitParameter( INIT_PARAM_MIME_INFO );
      if (mimeInfo!=null) {
        try {
          url = getServletContext().getResource( mimeInfo );
        } catch( MalformedURLException e ) {
          throw new ServletException( "Invalid " + INIT_PARAM_MIME_INFO + " configuration setting: " + mimeInfo, e );
        }
      } else {
        url = MimeTypesFactory.class.getResource( "tike-mimetypes.xml" );
      }
      try {
        return MimeTypesFactory.create( url );
      } catch( IOException ie ) {
        throw new ServletException(ie);
      } catch( MimeTypeException me ) {
        throw new ServletException( me );
      }
       */
    }


    /**
     * Returns the <code>SessionProvider</code>. If no session provider has been
     * set or created a new instance of {@link SessionProviderImpl} that extracts
     * credentials from the pRequest's <code>Authorization</code> header is
     * returned.
     *
     * @return the session provider
     */
    public synchronized SessionProvider getSessionProvider() {
        if (sessionProvider == null) {
            sessionProvider = new SessionProviderImpl(getCredentialsProvider());
        }
        return sessionProvider;
    }

    /**
     * Factory method for creating the credentials provider to be used for
     * accessing the credentials associated with a pRequest. The default
     * implementation returns a {@link BasicCredentialsProvider} instance,
     * but subclasses can override this method to add support for other
     * types of credentials.
     *
     * @return the credentilas provider
     * @since 1.3
     */
    protected CredentialsProvider getCredentialsProvider() {
    	return new BasicCredentialsProvider("");
    }

    /**
     * Sets the <code>SessionProvider</code>.
     *
     * @param sessionProvider
     */
    public synchronized void setSessionProvider(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    /**
     * Returns the <code>DavSessionProvider</code>. If no session provider has
     * been set or created a new instance of {@link DavSessionProviderImpl}
     * is returned.
     *
     * @return the session provider
     * @see AbstractWebdavServlet#getDavSessionProvider()
     */
    @Override
    public synchronized DavSessionProvider getDavSessionProvider() {
      if (davSessionProvider == null) {
        davSessionProvider = new CARS_DavSessionProvider();
      }
      return davSessionProvider;
    }

    /**
     * Sets the <code>DavSessionProvider</code>.
     *
     * @param sessionProvider
     * @see AbstractWebdavServlet#setDavSessionProvider(org.apache.jackrabbit.webdav.DavSessionProvider)
     */
    @Override
    public synchronized void setDavSessionProvider(DavSessionProvider sessionProvider) {
        this.davSessionProvider = sessionProvider;
    }

    /**
     * Returns the header value retrieved from the {@link #INIT_PARAM_AUTHENTICATE_HEADER}
     * init parameter. If the parameter is missing, the value defaults to
     * {@link #DEFAULT_AUTHENTICATE_HEADER}.
     *
     * @return the header value retrieved from the corresponding init parameter
     * or {@link #DEFAULT_AUTHENTICATE_HEADER}.
     * @see AbstractWebdavServlet#getAuthenticateHeaderValue()
     */
    public String getAuthenticateHeaderValue() {
        return authenticate_header;
    }

    /**
     * Returns the pResource configuration to be applied
     *
     * @return the pResource configuration.
     */
    public ResourceConfig getResourceConfig() throws ServletException {
        // fallback if no config present
        if (config == null) {
// v2.0
          config = new CARS_DavResourceConfig( getDetector() );
//            config = new CARS_DavResourceConfig();
        }
        return config;
    }

    /**
     * Set the pResource configuration
     *
     * @param config
     */
//    public void setResourceConfig(ResourceConfig config) {
//        this.config = config;
//    }




    /**
     * Service the given pRequest.
     *
     * @param pRequest
     * @param pResponse
     * @param pAC
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void service( final HttpServletRequest request, final HttpServletResponse response,
                            final CARS_ActionContext pAC,     final CARS_Factory pFactory ) throws ServletException, CredentialExpiredException, Exception {

//        WebdavRequest webdavRequest = new WebdavRequestImpl(pRequest, getLocatorFactory());
//     System.out.println(" adsdsad " + pRequest.getContextPath() );
//     System.out.println(" adsdsad1 " + pRequest.getPathInfo() );
//     System.out.println(" adsdsad1 " + pRequest.getRequestURI() );
//     System.out.println(" adsdsad1 " + pRequest.getServletPath() );
        final WebdavRequest webdavRequest = new CARS_DavRequestImpl( request, getLocatorFactory(), request.getContextPath() + request.getServletPath() );
// String s = CARS_Utils.readAsString( request.getInputStream() );
//        System.out.println("sdds " + s );
        // **** DeltaV requires 'Cache-Control' header for all methods except 'VERSION-CONTROL' and 'REPORT'.
        final int methodCode = DavMethods.getMethodCode(request.getMethod());
        final boolean noCache = DavMethods.isDeltaVMethod(webdavRequest) && !(DavMethods.DAV_VERSION_CONTROL == methodCode || DavMethods.DAV_REPORT == methodCode);
        final WebdavResponse webdavResponse = new WebdavResponseImpl(response, noCache);
        CARS_Main main = null;
        try {
            pAC.setAction( CARS_ActionContext.gDefActionGET );
            main = pFactory.createMain( pAC );
            pAC.setMain( main );

            // **** make sure there is a authenticated user
            if (!((CARS_DavSessionProvider)getDavSessionProvider()).attachSession( webdavRequest, pAC, pFactory )) {
              return;
            }

//            CARS_DavSession cds = (CARS_DavSession)webdavRequest.getDavSession();
//            cds.setActionContext( pAC );

            // **** check matching if=header for lock-token relevant operations
            final CARS_DavResourceFactory drf = (CARS_DavResourceFactory)getResourceFactory();
            final DavResource resource = drf.createResource(webdavRequest.getRequestLocator(), webdavRequest, webdavResponse, pAC, pFactory );
            if (!isPreconditionValid(webdavRequest, resource)) {
                webdavResponse.sendError(DavServletResponse.SC_PRECONDITION_FAILED);
                return;
            }
            if (!execute(webdavRequest, webdavResponse, methodCode, resource)) {
                super.service(request, response);
            }

        } catch (DavException e) {
            if (e.getErrorCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                webdavResponse.setHeader("WWW-Authenticate", getAuthenticateHeaderValue());
//                webdavResponse.sendError(e.getErrorCode(), e.getStatusPhrase());
                final Throwable cause = e.getCause();
                if (cause==null) {
                  webdavResponse.sendError(e.getErrorCode(), e.getStatusPhrase() );
                } else {
                  webdavResponse.sendError(e.getErrorCode(),
                          e.getStatusPhrase() + '\n' +
                          cause.getMessage() );
                }
            } else {
              final Throwable cause = e.getCause();
              if (cause==null) {
                webdavResponse.sendError(e);
              } else {
                webdavResponse.sendError(e.getErrorCode(), cause.getMessage() );
//                cause.printStackTrace();
              }
            }
        } finally {
          if (main!=null) {
            main.destroy();
          }
          getDavSessionProvider().releaseSession(webdavRequest);
        }
    }

    /**
     * Returns the <code>Repository</code>. If no repository has been set or
     * created the repository initialized by <code>RepositoryAccessServlet</code>
     * is returned.
     *
     * @return repository
     * @see RepositoryAccessServlet#getRepository(ServletContext)
     */
    public Repository getRepository() {
      return null;
    }




    /**
     * Executes the respective method in the given webdav context
     *
     * @param pRequest
     * @param pResponse
     * @param method
     * @param pResource
     * @throws ServletException
     * @throws IOException
     * @throws DavException
     */
    @Override
    protected boolean execute( final WebdavRequest request, final WebdavResponse response,
                               final int method, final DavResource resource )
            throws ServletException, IOException, DavException {

        switch (method) {
            case DavMethods.DAV_GET:
                doGet(request, response, resource);
                break;
            case DavMethods.DAV_HEAD:
                doHead(request, response, resource);
                break;
            case DavMethods.DAV_PROPFIND:
                doPropFind(request, response, resource);
                break;
            case DavMethods.DAV_PROPPATCH:
                doPropPatch(request, response, resource);
                break;
            case DavMethods.DAV_POST:
                doPost(request, response, resource);
                break;
            case DavMethods.DAV_PUT:
                doPut(request, response, resource);
                break;
            case DavMethods.DAV_DELETE:
                doDelete(request, response, resource);
                break;
            case DavMethods.DAV_COPY:
                doCopy(request, response, resource);
                break;
            case DavMethods.DAV_MOVE:
                doMove(request, response, resource);
                break;
            case DavMethods.DAV_MKCOL:
                doMkCol(request, response, resource);
                break;
            case DavMethods.DAV_OPTIONS:
                doOptions(request, response, resource);
                break;
            case DavMethods.DAV_LOCK:   // **** TODO
                doLock(request, response, resource);
                break;
            case DavMethods.DAV_UNLOCK:
                doUnlock(request, response, resource);
                break;
            case DavMethods.DAV_ORDERPATCH:
                doOrderPatch(request, response, resource);
                break;
            case DavMethods.DAV_SUBSCRIBE:
                doSubscribe(request, response, resource);
                break;
            case DavMethods.DAV_UNSUBSCRIBE:
                doUnsubscribe(request, response, resource);
                break;
            case DavMethods.DAV_POLL:
                doPoll(request, response, resource);
                break;
            case DavMethods.DAV_SEARCH:
                doSearch(request, response, resource);
                break;
            case DavMethods.DAV_VERSION_CONTROL:
                doVersionControl(request, response, resource);
                break;
            case DavMethods.DAV_LABEL:
                doLabel(request, response, resource);
                break;
            case DavMethods.DAV_REPORT:
                doReport(request, response, resource);
                break;
            case DavMethods.DAV_CHECKIN:
                doCheckin(request, response, resource);
                break;
            case DavMethods.DAV_CHECKOUT:
                doCheckout(request, response, resource);
                break;
            case DavMethods.DAV_UNCHECKOUT:
                doUncheckout(request, response, resource);
                break;
            case DavMethods.DAV_MERGE:
                doMerge(request, response, resource);
                break;
            case DavMethods.DAV_UPDATE:
                doUpdate(request, response, resource);
                break;
            case DavMethods.DAV_MKWORKSPACE:
                doMkWorkspace(request, response, resource);
                break;
            case DavMethods.DAV_MKACTIVITY:
                doMkActivity(request, response, resource);
                break;
            case DavMethods.DAV_BASELINE_CONTROL:
                doBaselineControl(request, response, resource);
                break;
            case DavMethods.DAV_ACL:
                doAcl(request, response, resource);
                break;
            case DavMethods.DAV_REBIND:
                doRebind(request, response, resource);
                break;
            case DavMethods.DAV_UNBIND:
                doUnbind(request, response, resource);
                break;
            case DavMethods.DAV_BIND:
                doBind(request, response, resource);
                break;
            default:
                // any other method
                return false;
        }
        return true;
    }





    /**
     * The OPTION method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     */
    protected void doOptions(WebdavRequest request, WebdavResponse response,
                             DavResource resource) throws IOException, DavException {
        response.addHeader(DavConstants.HEADER_DAV, resource.getComplianceClass());
        response.addHeader("Allow", resource.getSupportedMethods());
        response.addHeader("MS-Author-Via", DavConstants.HEADER_DAV);
        if (resource instanceof SearchResource) {
            String[] langs = ((SearchResource) resource).getQueryGrammerSet().getQueryLanguages();
            for (int i = 0; i < langs.length; i++) {
                response.addHeader(SearchConstants.HEADER_DASL, "<" + langs[i] + ">");
            }
        }
        // with DeltaV the OPTIONS pRequest may contain a Xml body.
        OptionsResponse oR = null;
        OptionsInfo oInfo = request.getOptionsInfo();
        if (oInfo != null && resource instanceof DeltaVResource) {
            oR = ((DeltaVResource) resource).getOptionResponse(oInfo);
        }
        if (oR == null) {
            response.setStatus(DavServletResponse.SC_OK);
        } else {
            response.sendXmlResponse(oR, DavServletResponse.SC_OK);
        }
    }

    /**
     * The HEAD method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     */
    protected void doHead(WebdavRequest request, WebdavResponse response,
                          DavResource resource) throws IOException {
        spoolResource( request, response, resource, false );
        return;
    }

    /**
     * The GET method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     */
    protected void doGet(WebdavRequest request, WebdavResponse response,
                         DavResource resource) throws IOException {
        spoolResource( request, response, resource, true );
        return;
    }

    /** spoolResource
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @param pSendContent
     * @throws IOException
     */
    private void spoolResource( final WebdavRequest pRequest, final WebdavResponse pResponse,
                                final DavResource pResource,  final boolean pSendContent)
            throws IOException {

        if (!pResource.exists()) {
            pResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        final long modSince = pRequest.getDateHeader("If-Modified-Since");
        if (modSince > UNDEFINED_TIME) {
            final long modTime = pResource.getModificationTime();
            // test if pResource has been modified. note that formatted modification
            // time lost the milli-second precision
            if (modTime != UNDEFINED_TIME && (modTime / 1000 * 1000) <= modSince) {
                // pResource has not been modified since the time indicated in the
                // 'If-Modified-Since' header.
                pResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }

        // spool pResource properties and ev. pResource content.
        final OutputStream out = (pSendContent) ? pResponse.getOutputStream() : null;
        if (out!=null) {
          pResource.spool(getOutputContext(pResponse, out));
          pResponse.flushBuffer();
        } else {
          pResponse.setStatus(HttpServletResponse.SC_OK );
        }
        return;
    }

    /**
     * The PROPFIND method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     */
    @Override
    protected void doPropFind( final WebdavRequest pRequest, final WebdavResponse pResponse,
                               final DavResource   pResource ) throws IOException, DavException {

        if (!pResource.exists()) {
            pResponse.sendError(DavServletResponse.SC_NOT_FOUND);
            return;
        }
        final int depth = pRequest.getDepth(DEPTH_INFINITY);
        final DavPropertyNameSet requestProperties = pRequest.getPropFindProperties();
        final int propfindType = pRequest.getPropFindType();

        MultiStatus mstatus = new MultiStatus();

/*

            PropContainer status200 = new DavPropertySet();
            // clone set of property, since several resources could use this again
          DavPropertyNameSet propNameSet = new DavPropertyNameSet(requestProperties);
//          DavPropertyNameIterator it = requestProperties.iterator();
//          while( it.hasNext() ) {
//            DavPropertyName dpn = it.nextPropertyName();
//            propNameSet.add( DavPropertyName.create( dpn.getName() ) );
//          }

            // Add requested properties or all non-protected properties, or
            // non-protected properties plus requested properties (allprop/include)
            DavPropertyIterator iter = pResource.getProperties().iterator();
            while (iter.hasNext()) {
                DavProperty property = iter.nextProperty();
                boolean allDeadPlusRfc4918LiveProperties =
                    propfindType == PROPFIND_ALL_PROP || propfindType == PROPFIND_ALL_PROP_INCLUDE;
             DavPropertyName remName = property.getName();
//             remName = DavPropertyName.create( remName.getName() );
                boolean wasRequested = propNameSet.remove(remName);

                if ((allDeadPlusRfc4918LiveProperties && !property.isInvisibleInAllprop()) || wasRequested) {
                    status200.addContent(property);
                }
            }

            if (!propNameSet.isEmpty() && propfindType != PROPFIND_ALL_PROP) {
                PropContainer status404 = new DavPropertySet();
                DavPropertyNameIterator iter1 = propNameSet.iterator();
                while (iter1.hasNext()) {
                    DavPropertyName propName = iter1.nextPropertyName();
                    status404.addContent(propName);
                }
            }
*/

        mstatus.addResourceProperties(pResource, requestProperties, propfindType, depth);
        pResponse.sendMultiStatus(mstatus);
    }

    /**
     * The PROPPATCH method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     */
    @Override
    protected void doPropPatch( final WebdavRequest pRequest, final WebdavResponse pResponse,
                                final DavResource   pResource ) throws IOException, DavException {

        final List changeList = pRequest.getPropPatchChangeList();
        if (changeList.isEmpty()) {
          pResponse.sendError(DavServletResponse.SC_BAD_REQUEST);
          return;
        }

        final MultiStatus          ms = new MultiStatus();
        final MultiStatusResponse msr = pResource.alterProperties(changeList);
        ms.addResponse(msr);
        pResponse.sendMultiStatus(ms);
        return;
    }

    /**
     * The POST method. Delegate to PUT
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doPost(WebdavRequest request, WebdavResponse response,
                          DavResource resource) throws IOException, DavException {
        doPut(request, response, resource);
    }

    /**
     * The PUT method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doPut(WebdavRequest pRequest, WebdavResponse pResponse,
                         DavResource pResource) throws IOException, DavException {

        final CARS_DavResource parentResource = (CARS_DavResource)pResource.getCollection();
        if (parentResource == null || !parentResource.exists()) {
            // parent does not exist
            pResponse.sendError(DavServletResponse.SC_CONFLICT);
            return;
        }

        int status;
        // test if pResource already exists
        if (pResource.exists()) {
            status = DavServletResponse.SC_NO_CONTENT;
            pResource.getCollection().removeMember( pResource );
        } else {
            status = DavServletResponse.SC_CREATED;
        }

//        String overwrite = pRequest.getHeader( "Overwrite" );
//        if (overwrite==null) {
//          overwrite = OverwriteHeader.OVERWRITE_TRUE;
//        }

        parentResource.addMember(pResource, getInputContext(pRequest, pRequest.getInputStream()));
        pResponse.setStatus(status);
        return;
    }

    /**
     * The MKCOL method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    @Override
    protected void doMkCol( final WebdavRequest request, final WebdavResponse response,
                            final DavResource resource ) throws IOException, DavException {

        final DavResource parentResource = resource.getCollection();
        if (parentResource == null || !parentResource.exists() || !parentResource.isCollection()) {
          // **** parent does not exist or is not a collection
          response.sendError(DavServletResponse.SC_CONFLICT);
          return;
        }
        // **** shortcut: mkcol is only allowed on deleted/non-existing resources
        if (resource.exists()) {
          response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
          return;
        }

        if (request.getContentLength() > 0 || request.getHeader("Transfer-Encoding") != null) {
          response.setStatus( 415 );
          return;
//          parentResource.addMember(resource, getInputContext(request, request.getInputStream()));
        } else {
          parentResource.addMember(resource, getInputContext(request, null));
        }
        response.setStatus( DavServletResponse.SC_CREATED );
        return;
    }

    /**
     * The DELETE method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doDelete(WebdavRequest request, WebdavResponse response,
                            DavResource resource) throws IOException, DavException {
        DavResource parent = resource.getCollection();
        if (parent != null) {
            parent.removeMember(resource);
            response.setStatus(DavServletResponse.SC_NO_CONTENT);
        } else {
            response.sendError(DavServletResponse.SC_FORBIDDEN, "Cannot remove the root resource.");
        }
    }

    /**
     * The COPY method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    @Override
    protected void doCopy( final WebdavRequest pRequest, final WebdavResponse pResponse,
                           final DavResource   pResource ) throws IOException, DavException {

        // **** only depth 0 and infinity is allowed
        final int depth = pRequest.getDepth(DEPTH_INFINITY);
        if (!(depth == DEPTH_0 || depth == DEPTH_INFINITY)) {
          pResponse.sendError(DavServletResponse.SC_BAD_REQUEST);
          return;
        }

        final DavResource destResource = getResourceFactory().createResource( pRequest.getDestinationLocator(), pRequest, pResponse );
        int status = validateDestination( destResource, pRequest, true );
        if (status > DavServletResponse.SC_NO_CONTENT) {
          pResponse.sendError(status);
          return;
        }

        String overwrite = pRequest.getHeader( "Overwrite" );
        if (overwrite==null) {
          overwrite = OverwriteHeader.OVERWRITE_TRUE;
        }

        ((CARS_DavResource)pResource).copy( destResource, depth == DEPTH_0, overwrite );
        if (status==DavServletResponse.SC_OK) {
          status = DavServletResponse.SC_CREATED;
        }
        pResponse.setStatus(status);
        return;
    }

    /**
     * The MOVE method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doMove( final WebdavRequest pRequest, final WebdavResponse pResponse,
                           final DavResource pResource) throws IOException, DavException {

        String overwrite = pRequest.getHeader( "Overwrite" );
        if (overwrite==null) {
          overwrite = OverwriteHeader.OVERWRITE_TRUE;
        }

        final DavResource destResource = getResourceFactory().createResource(pRequest.getDestinationLocator(), pRequest, pResponse);
        final int status = validateDestination( destResource, pRequest, true );
        if (status > DavServletResponse.SC_NO_CONTENT) {
            pResponse.sendError(status);
            return;
        }
//        DavResource parent = pResource.getCollection();
        ((CARS_DavResource)pResource).move( destResource, overwrite );
//        parent.removeMember(pResource); // **** JeCARS performs a copy instead of a move
        pResponse.setStatus(status);
    }

    /**
     * The BIND method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource the collection pResource to which a new member will be added
     * @throws IOException
     * @throws DavException
     */
    protected void doBind(WebdavRequest request, WebdavResponse response,
                          DavResource resource) throws IOException, DavException {

        if (!resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
        }
        BindInfo bindInfo = request.getBindInfo();
        DavResource oldBinding = getResourceFactory().createResource(request.getHrefLocator(bindInfo.getHref()), request, response);
        if (!(oldBinding instanceof BindableResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        DavResource newBinding = getResourceFactory().createResource(request.getMemberLocator(bindInfo.getSegment()), request, response);
        int status = validateDestination(newBinding, request, false);
        if (status > DavServletResponse.SC_NO_CONTENT) {
            response.sendError(status);
            return;
        }
        ((BindableResource) oldBinding).bind(resource, newBinding);
        response.setStatus(status);
    }

    /**
     * The REBIND method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource the collection pResource to which a new member will be added
     * @throws IOException
     * @throws DavException
     */
    protected void doRebind(WebdavRequest request, WebdavResponse response,
                            DavResource resource) throws IOException, DavException {

        if (!resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
        }
        RebindInfo rebindInfo = request.getRebindInfo();
        DavResource oldBinding = getResourceFactory().createResource(request.getHrefLocator(rebindInfo.getHref()), request, response);
        if (!(oldBinding instanceof BindableResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        DavResource newBinding = getResourceFactory().createResource(request.getMemberLocator(rebindInfo.getSegment()), request, response);
        int status = validateDestination(newBinding, request, false);
        if (status > DavServletResponse.SC_NO_CONTENT) {
            response.sendError(status);
            return;
        }
        ((BindableResource) oldBinding).rebind(resource, newBinding);
        response.setStatus(status);
    }

    /**
     * The UNBIND method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource the collection pResource from which a member will be removed
     * @throws IOException
     * @throws DavException
     */
    protected void doUnbind(WebdavRequest request, WebdavResponse response,
                            DavResource resource) throws IOException, DavException {

        UnbindInfo unbindInfo = request.getUnbindInfo();
        DavResource srcResource = getResourceFactory().createResource(request.getMemberLocator(unbindInfo.getSegment()), request, response);
        resource.removeMember(srcResource);
    }

    /**
     * Validate the given destination pResource and return the proper status
     * code: Any return value greater/equal than {@link DavServletResponse#SC_NO_CONTENT}
     * indicates an error.
     *
     * @param destResource destination pResource to be validated.
     * @param pRequest
     * @return status code indicating whether the destination is valid.
     */
    /*
    private int validateDestination( final DavResource destResource, final WebdavRequest request, final boolean checkHeader)
            throws DavException {

        if (checkHeader) {
          final String destHeader = request.getHeader(HEADER_DESTINATION);
          if (destHeader == null || "".equals(destHeader)) {
            return DavServletResponse.SC_BAD_REQUEST;
          }
        }
        if (destResource.getLocator().equals(request.getRequestLocator())) {
          return DavServletResponse.SC_FORBIDDEN;
        }

        int status;
        if (destResource.exists()) {
            if (request.isOverwrite()) {
                // matching if-header required for existing resources
                if (!request.matchesIfHeader(destResource)) {
                    return DavServletResponse.SC_PRECONDITION_FAILED;
                } else {
                    // overwrite existing pResource
                    destResource.getCollection().removeMember(destResource);
                    status = DavServletResponse.SC_NO_CONTENT;
                }
            } else {
                // cannot copy/move to an existing item, if overwrite is not forced
//                return DavServletResponse.SC_PRECONDITION_FAILED;
                return DavServletResponse.SC_OK;
            }
        } else {
            // destination does not exist >> copy/move can be performed
            status = DavServletResponse.SC_CREATED;
        }
        return status;
    }
     * 
     */
    
    /**
     * The LOCK method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doLock( final WebdavRequest pRequest, final WebdavResponse pResponse,
                           final DavResource pResource) throws IOException, DavException {

        final LockInfo lockInfo = pRequest.getLockInfo();
        if (lockInfo.isRefreshLock()) {
            // refresh any matching existing locks
            final ActiveLock[] activeLocks = pResource.getLocks();
            final List lList = new ArrayList();
            for (int i = 0; i < activeLocks.length; i++) {
                // adjust lockinfo with type/scope retrieved from the lock.
                lockInfo.setType(activeLocks[i].getType());
                lockInfo.setScope(activeLocks[i].getScope());

                final DavProperty etagProp = pResource.getProperty(DavPropertyName.GETETAG);
                final String etag = etagProp != null ? String.valueOf(etagProp.getValue()) : "";
                if (pRequest.matchesIfHeader(pResource.getHref(), activeLocks[i].getToken(), etag)) {
                    lList.add(pResource.refreshLock(lockInfo, activeLocks[i].getToken()));
                }
            }
            if (lList.isEmpty()) {
                throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
            }
            final ActiveLock[] refreshedLocks = (ActiveLock[]) lList.toArray(new ActiveLock[lList.size()]);
            pResponse.sendRefreshLockResponse(refreshedLocks);
        } else {
            // create a new lock
            final ActiveLock[] locks = {pResource.lock(lockInfo)};
            pResponse.sendRefreshLockResponse(locks);
        }
    }

    /**
     * The UNLOCK method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     */
    protected void doUnlock( final WebdavRequest request, final WebdavResponse response,
                             final DavResource resource) throws DavException {
        // get lock token from header
        final String lockToken = request.getLockToken();
        final TransactionInfo tInfo = request.getTransactionInfo();
        if (tInfo != null) {
            ((TransactionResource) resource).unlock(lockToken, tInfo);
        } else {
            resource.unlock(lockToken);
        }
        response.setStatus(DavServletResponse.SC_NO_CONTENT);
    }

    /**
     * The ORDERPATCH method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doOrderPatch(WebdavRequest request,
                                WebdavResponse response,
                                DavResource resource)
            throws IOException, DavException {

        if (!(resource instanceof OrderingResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        OrderPatch op = request.getOrderPatch();
        if (op == null) {
            response.sendError(DavServletResponse.SC_BAD_REQUEST);
            return;
        }
        // perform reordering of internal members
        ((OrderingResource) resource).orderMembers(op);
        response.setStatus(DavServletResponse.SC_OK);
    }

    /**
     * The SUBSCRIBE method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doSubscribe(WebdavRequest request,
                               WebdavResponse response,
                               DavResource resource)
            throws IOException, DavException {

        if (!(resource instanceof ObservationResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        SubscriptionInfo info = request.getSubscriptionInfo();
        if (info == null) {
            response.sendError(DavServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        Subscription subs = ((ObservationResource) resource).subscribe(info, request.getSubscriptionId());
        response.sendSubscriptionResponse(subs);
    }

    /**
     * The UNSUBSCRIBE method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doUnsubscribe(WebdavRequest request,
                                 WebdavResponse response,
                                 DavResource resource)
            throws IOException, DavException {

        if (!(resource instanceof ObservationResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        ((ObservationResource) resource).unsubscribe(request.getSubscriptionId());
        response.setStatus(DavServletResponse.SC_NO_CONTENT);
    }

    /**
     * The POLL method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws IOException
     * @throws DavException
     */
    protected void doPoll(WebdavRequest request,
                          WebdavResponse response,
                          DavResource resource)
            throws IOException, DavException {

        if (!(resource instanceof ObservationResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        EventDiscovery ed = ((ObservationResource) resource).poll(
                request.getSubscriptionId(), request.getPollTimeout());
        response.sendPollResponse(ed);
    }

    /**
     * The VERSION-CONTROL method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doVersionControl(WebdavRequest request, WebdavResponse response,
                                    DavResource resource)
            throws DavException, IOException {
        if (!(resource instanceof VersionableResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        ((VersionableResource) resource).addVersionControl();
    }

    /**
     * The LABEL method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doLabel(WebdavRequest request, WebdavResponse response,
                           DavResource resource)
            throws DavException, IOException {

        LabelInfo labelInfo = request.getLabelInfo();
        if (resource instanceof VersionResource) {
            ((VersionResource) resource).label(labelInfo);
        } else if (resource instanceof VersionControlledResource) {
            ((VersionControlledResource) resource).label(labelInfo);
        } else {
            // any other pResource type that does not support a LABEL pRequest
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    /**
     * The REPORT method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doReport(WebdavRequest request, WebdavResponse response,
                            DavResource resource)
            throws DavException, IOException {
        ReportInfo info = request.getReportInfo();
        Report report;
        if (resource instanceof DeltaVResource) {
            report = ((DeltaVResource) resource).getReport(info);
        } else if (resource instanceof AclResource) {
            report = ((AclResource) resource).getReport(info);
        } else {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        int statusCode = (report.isMultiStatusReport()) ? DavServletResponse.SC_MULTI_STATUS : DavServletResponse.SC_OK;
        response.sendXmlResponse(report, statusCode);
    }

    /**
     * The CHECKIN method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doCheckin(WebdavRequest request, WebdavResponse response,
                             DavResource resource)
            throws DavException, IOException {

        if (!(resource instanceof VersionControlledResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        String versionHref = ((VersionControlledResource) resource).checkin();
        response.setHeader(DeltaVConstants.HEADER_LOCATION, versionHref);
        response.setStatus(DavServletResponse.SC_CREATED);
    }

    /**
     * The CHECKOUT method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doCheckout(WebdavRequest request, WebdavResponse response,
                              DavResource resource)
            throws DavException, IOException {
        if (!(resource instanceof VersionControlledResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        ((VersionControlledResource) resource).checkout();
    }

    /**
     * The UNCHECKOUT method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doUncheckout(WebdavRequest request, WebdavResponse response,
                                DavResource resource)
            throws DavException, IOException {
        if (!(resource instanceof VersionControlledResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        ((VersionControlledResource) resource).uncheckout();
    }

    /**
     * The MERGE method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doMerge(WebdavRequest request, WebdavResponse response,
                           DavResource resource) throws DavException, IOException {

        if (!(resource instanceof VersionControlledResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        MergeInfo info = request.getMergeInfo();
        MultiStatus ms = ((VersionControlledResource) resource).merge(info);
        response.sendMultiStatus(ms);
    }

    /**
     * The UPDATE method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doUpdate(WebdavRequest request, WebdavResponse response,
                            DavResource resource) throws DavException, IOException {

        if (!(resource instanceof VersionControlledResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        UpdateInfo info = request.getUpdateInfo();
        MultiStatus ms = ((VersionControlledResource) resource).update(info);
        response.sendMultiStatus(ms);
    }

    /**
     * The MKWORKSPACE method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doMkWorkspace(WebdavRequest request, WebdavResponse response,
                                 DavResource resource) throws DavException, IOException {
        if (resource.exists()) {
            response.sendError(DavServletResponse.SC_FORBIDDEN);
            return;
        }

        DavResource parentResource = resource.getCollection();
        if (parentResource == null || !parentResource.exists() || !parentResource.isCollection()) {
            // parent does not exist or is not a collection
            response.sendError(DavServletResponse.SC_CONFLICT);
            return;
        }
        if (!(parentResource instanceof DeltaVResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        ((DeltaVResource) parentResource).addWorkspace(resource);
        response.setStatus(DavServletResponse.SC_CREATED);
    }

    /**
     * The MKACTIVITY method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doMkActivity(WebdavRequest request, WebdavResponse response,
                                DavResource resource) throws DavException, IOException {
        if (resource.exists()) {
            response.sendError(DavServletResponse.SC_FORBIDDEN);
            return;
        }

        DavResource parentResource = resource.getCollection();
        if (parentResource == null || !parentResource.exists() || !parentResource.isCollection()) {
            // parent does not exist or is not a collection
            response.sendError(DavServletResponse.SC_CONFLICT);
            return;
        }
        // TODO: improve. see http://issues.apache.org/jira/browse/JCR-394
        if (parentResource.getComplianceClass().indexOf(DavCompliance.ACTIVITY) < 0) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        if (!(resource instanceof ActivityResource)) {
            response.sendError(DavServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // try to add the new activity pResource
        parentResource.addMember(resource, getInputContext(request, request.getInputStream()));

        // Note: mandatory cache control header has already been set upon pResponse creation.
        response.setStatus(DavServletResponse.SC_CREATED);
    }

    /**
     * The BASELINECONTROL method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doBaselineControl(WebdavRequest request, WebdavResponse response,
                                     DavResource resource)
        throws DavException, IOException {

        if (!resource.exists()) {
            response.sendError(DavServletResponse.SC_NOT_FOUND);
            return;
        }
        // TODO: improve. see http://issues.apache.org/jira/browse/JCR-394
        if (!(resource instanceof VersionControlledResource) || !resource.isCollection()) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        // TODO : missing method on VersionControlledResource
        throw new DavException(DavServletResponse.SC_NOT_IMPLEMENTED);
        /*
        ((VersionControlledResource) pResource).addBaselineControl(pRequest.getRequestDocument());
        // Note: mandatory cache control header has already been set upon pResponse creation.
        pResponse.setStatus(DavServletResponse.SC_OK);
        */
    }

    /**
     * The SEARCH method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doSearch(WebdavRequest request, WebdavResponse response,
                            DavResource resource) throws DavException, IOException {

        if (!(resource instanceof SearchResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        Document doc = request.getRequestDocument();
        if (doc != null) {
            SearchInfo sR = SearchInfo.createFromXml(doc.getDocumentElement());
            response.sendMultiStatus(((SearchResource) resource).search(sR));
        } else {
            // pRequest without pRequest body is valid if requested pResource
            // is a 'query' pResource.
            response.sendMultiStatus(((SearchResource) resource).search(null));
        }
    }

    /**
     * The ACL method
     *
     * @param pRequest
     * @param pResponse
     * @param pResource
     * @throws DavException
     * @throws IOException
     */
    protected void doAcl(WebdavRequest request, WebdavResponse response,
                         DavResource resource) throws DavException, IOException {
        if (!(resource instanceof AclResource)) {
            response.sendError(DavServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        Document doc = request.getRequestDocument();
        if (doc == null) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "ACL request requires a DAV:acl body.");
        }
        AclProperty acl = AclProperty.createFromXml(doc.getDocumentElement());
        ((AclResource)resource).alterAcl(acl);
    }

    /**
     * Return a new <code>InputContext</code> used for adding pResource members
     *
     * @param pRequest
     * @param in
     * @return
     * @see #spoolResource(WebdavRequest, WebdavResponse, DavResource, boolean)
     */
    protected InputContext getInputContext(DavServletRequest request, InputStream in) {
        return new InputContextImpl(request, in);
    }

    /**
     * Return a new <code>OutputContext</code> used for spooling pResource properties and
     * the pResource content
     *
     * @param pResponse
     * @param out
     * @return
     * @see #doPut(WebdavRequest, WebdavResponse, DavResource)
     * @see #doPost(WebdavRequest, WebdavResponse, DavResource)
     * @see #doMkCol(WebdavRequest, WebdavResponse, DavResource)
     */
    protected OutputContext getOutputContext(DavServletResponse response, OutputStream out) {
        return new OutputContextImpl(response, out);
    }




}
