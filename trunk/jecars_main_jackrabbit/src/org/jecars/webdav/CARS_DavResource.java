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

import org.apache.jackrabbit.JcrConstants;
//import org.apache.jackrabbit.uuid.UUID;
import org.apache.jackrabbit.server.io.AbstractExportContext;
import org.apache.jackrabbit.server.io.DefaultIOListener;
import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.ExportContextImpl;
import org.apache.jackrabbit.server.io.IOListener;
import org.apache.jackrabbit.server.io.IOManager;
import org.apache.jackrabbit.server.io.IOUtil;
import org.apache.jackrabbit.server.io.ImportContext;
import org.apache.jackrabbit.server.io.ImportContextImpl;
import org.apache.jackrabbit.server.io.PropertyExportContext;
import org.apache.jackrabbit.server.io.PropertyImportContext;
import org.apache.jackrabbit.util.Text;
import org.apache.jackrabbit.webdav.DavCompliance;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceFactory;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.bind.BindConstants;
import org.apache.jackrabbit.webdav.bind.BindableResource;
import org.apache.jackrabbit.webdav.bind.ParentSet;
import org.apache.jackrabbit.webdav.bind.ParentElement;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.jcr.JcrDavException;
import org.apache.jackrabbit.webdav.jcr.JcrDavSession;
import org.apache.jackrabbit.webdav.jcr.lock.JcrActiveLock;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.lock.LockDiscovery;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.LockManager;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.SupportedLock;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.property.HrefProperty;
import org.apache.jackrabbit.webdav.property.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.lock.Lock;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.ItemExistsException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.security.auth.login.CredentialExpiredException;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.header.OverwriteHeader;
import org.apache.jackrabbit.webdav.simple.ItemFilter;
import org.apache.jackrabbit.webdav.simple.ResourceConfig;
import org.apache.jackrabbit.webdav.xml.Namespace;
import org.apache.pdfbox.util.PDFImageWriter;
import org.apache.tika.detect.Detector;
import org.jecars.CARS_ActionContext;
import org.jecars.CARS_DefaultMain;
import org.jecars.CARS_Definitions;
import org.jecars.CARS_Factory;
import org.jecars.CARS_Main;
import org.jecars.CARS_Utils;
import org.jecars.apps.CARS_DefaultInterface;
import org.jecars.apps.CARS_Interface;
import org.jecars.support.CARS_Mime;

/** CARS_DavResource
 *
 *  @version $Id: CARS_DavResource.java,v 1.9 2009/07/30 12:08:35 weertj Exp $
 */
public class CARS_DavResource implements DavResource, BindableResource, JcrConstants {

    /**
     * the default logger
     */
    private static final Logger log = LoggerFactory.getLogger(CARS_DavResource.class);

    public static final String METHODS = DavResource.METHODS + ", " + BindConstants.METHODS;

    public static final String COMPLIANCE_CLASSES = DavCompliance.concatComplianceClasses(
        new String[] {
            DavCompliance._1_,
            DavCompliance._2_,
            DavCompliance._3_,
            DavCompliance.BIND
        }
    );


    private DavResourceFactory factory;
    private LockManager lockManager;
    private JcrDavSession session;
    private Node node;
    private DavResourceLocator locator;

    protected DavPropertySet properties = new DavPropertySet();
    protected boolean propsInitialized = false;
    private boolean isCollection = true;
    private String rfc4122Uri;
    
    private ResourceConfig config;
    private long modificationTime = IOUtil.UNDEFINED_TIME;

    /**
     * Create a new {@link DavResource}.
     *
     * @param locator
     * @param factory
     * @param session
     * @deprecated
     */
    public CARS_DavResource(DavResourceLocator locator, DavResourceFactory factory,
                           DavSession session, ResourceConfig config) throws DavException {
        JcrDavSession.checkImplementation(session);
        this.session = (JcrDavSession)session;
        this.factory = factory;
        this.locator = locator;
        this.config = config;

        if (locator != null && locator.getRepositoryPath() != null) {
            try {
                Item item = getJcrSession().getItem(locator.getRepositoryPath());
                if (item != null && item.isNode()) {
                    node = (Node) item;
                    // define what is a collection in webdav
                    isCollection = config.isCollectionResource(node);
                    initRfc4122Uri();
                }
            } catch (PathNotFoundException e) {
                // ignore: exists field evaluates to false
            } catch (RepositoryException e) {
                // some other error
                throw new JcrDavException(e);
            }
        } else {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Create a new {@link DavResource}.
     *
     * @param locator
     * @param factory
     * @param session
     * @param config
     * @param isCollection
     * @throws DavException
     */
    public CARS_DavResource(DavResourceLocator locator, DavResourceFactory factory,
                           DavSession session, ResourceConfig config,
                           boolean isCollection) throws DavException {
        this(locator, factory, session, config, null);
        this.isCollection = isCollection;
    }

    /**
     * Create a new {@link DavResource}.
     *
     * @param locator
     * @param factory
     * @param session
     * @param config
     * @param node
     * @throws DavException
     */
    public CARS_DavResource(DavResourceLocator locator, DavResourceFactory factory,
                           DavSession session, ResourceConfig config, Node node) throws DavException {
        if (locator == null || session == null || config == null) {
            throw new IllegalArgumentException();
        }
        this.session = (JcrDavSession)session;
        this.factory = factory;
        this.locator = locator;
        this.config = config;

        if (locator.getResourcePath() != null) {
            if (node != null) {
                this.node = node;
                // define what is a collection in webdav
                isCollection = config.isCollectionResource(node);
                initRfc4122Uri();
            }
        } else {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * If the Node associated with this DavResource has a UUID that allows for the creation of a rfc4122 compliant
     * URI, we use it as the value of the protected DAV property DAV:resource-id, which is defined by the BIND
     * specification.
     */
    private void initRfc4122Uri() {
        try {
            if (node.isNodeType(MIX_REFERENCEABLE)) {
                String uuid = node.getUUID();
                try {
//                  UUID.fromString(uuid);
                  rfc4122Uri = "urn:uuid:" + uuid.toString();
                } catch (IllegalArgumentException e) {
                    //no, this is not a UUID
                }
            }
        } catch (RepositoryException e) {
            log.warn("Error while detecting UUID", e);
        }
    }


    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getComplianceClass()
     */
    public String getComplianceClass() {
        return COMPLIANCE_CLASSES;
    }

    /**
     * @return DavResource#METHODS
     * @see org.apache.jackrabbit.webdav.DavResource#getSupportedMethods()
     */
    public String getSupportedMethods() {
        return METHODS;
    }

    /**
     * @see DavResource#exists() )
     */
    public boolean exists() {
        return node != null;
    }

    /**
     * @see DavResource#isCollection()
     */
    public boolean isCollection() {
        return isCollection;
    }

    /**
     * Package protected method that allows to define whether this resource
     * represents a collection or not.
     *
     * @param isCollection
     * @deprecated Use the constructor taking a boolean flag instead.
     */
    void setIsCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getLocator()
     */
    public DavResourceLocator getLocator() {
        return locator;
    }

    /**
     * @see DavResource#getResourcePath()
     */
    public String getResourcePath() {
        return locator.getResourcePath();
    }

    /**
     * @see DavResource#getHref()
     */
    public String getHref() {
        return locator.getHref(isCollection());
    }

    /**
     * Returns the the last segment of the resource path.<p>
     * Note that this must not correspond to the name of the underlying
     * repository item for two reasons:<ul>
     * <li>SameNameSiblings have an index appended to their item name.</li>
     * <li>the resource path may differ from the item path.</li>
     * </ul>
     * Using the item name as DAV:displayname caused problems with XP built-in
     * client in case of resources representing SameNameSibling nodes.
     *
     * @see DavResource#getDisplayName()
     */
    public String getDisplayName() {
        String resPath = getResourcePath();
        return (resPath != null) ? Text.getName(resPath) : resPath;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getModificationTime()
     */
    public long getModificationTime() {
        initProperties();
        return modificationTime;
    }

    /**
     * If this resource exists and the specified context is not <code>null</code>
     * this implementation build a new {@link ExportContext} based on the specified
     * context and forwards the export to its <code>IOManager</code>. If the
     * {@link IOManager#exportContent(ExportContext, DavResource)} fails,
     * an <code>IOException</code> is thrown.
     *
     * @see DavResource#spool(OutputContext)
     * @see ResourceConfig#getIOManager()
     * @throws IOException if the export fails.
     */
    @Override
    public void spool( final OutputContext pOutputContext ) throws IOException {
      if (exists() && pOutputContext != null) {
        try {
          Node n = getNode();
          if (n.isNodeType( "nt:resource" )) {
            n = CARS_Utils.getLinkedNode(n);
            final OutputStream os = pOutputContext.getOutputStream();
            if (os!=null) {
              Binary bin = n.getProperty( "jcr:data" ).getBinary();
              CARS_Utils.sendInputStreamToOutputStream( 10000, bin.getStream(),
                        pOutputContext.getOutputStream() );
              bin.dispose();
            }
          }
        } catch( Exception re ) {
          throw new IOException( re );
        }
      }
      return;
    }

    /**
     * @see DavResource#getProperty(org.apache.jackrabbit.webdav.property.DavPropertyName)
     */
    public DavProperty getProperty(DavPropertyName name) {
        initProperties();
        return properties.get(name);
    }

    /**
     * @see DavResource#getProperties()
     */
    public DavPropertySet getProperties() {
        initProperties();
//        Collection col = properties.getContent();
//        for (Object c : col) {
//          if (c instanceof DefaultDavProperty) {
//            DefaultDavProperty ddp = (DefaultDavProperty)c;
//              System.out.println(" okok " + ddp.getName().getName() + " = " + ddp.getValue() );
//          }
//        }
//        DavPropertyName[] dpn = properties.getPropertyNames();
//        for (DavPropertyName dp : dpn) {
//            System.out.println("ijijd " + dp.getName() );
//        }
        return properties;
    }

    /**
     * @see DavResource#getPropertyNames()
     */
    @Override
    public DavPropertyName[] getPropertyNames() {
      return getProperties().getPropertyNames();
    }

    /**
     * Fill the set of properties
     */
    protected void initProperties() {
        if (!exists() || propsInitialized) {
            return;
        }

        final Namespace jecarsns = Namespace.getNamespace( "jecars", "http://jecars:org" );

        try {
            config.getPropertyManager().exportProperties(getPropertyExportContext(), isCollection());
        } catch (RepositoryException e) {
            log.warn("Error while accessing resource properties", e);
        }

        // set (or reset) fundamental properties
        if (getDisplayName() != null) {
            properties.add(new DefaultDavProperty(DavPropertyName.DISPLAYNAME, getDisplayName() ));
        }
        if (isCollection()) {
            properties.add(new ResourceType(ResourceType.COLLECTION));
            // Windows XP support
            properties.add(new DefaultDavProperty(DavPropertyName.ISCOLLECTION, "1"));
        } else {
            properties.add(new ResourceType(ResourceType.DEFAULT_RESOURCE));
            // Windows XP support
            properties.add(new DefaultDavProperty(DavPropertyName.ISCOLLECTION, "0"));
        }

        // **** last modified
        String lastModified = null;
        try {
          if (node.hasProperty( "jecars:Modified")) {
            lastModified = DavConstants.modificationDateFormat.format( node.getProperty( "jecars:Modified" ).getDate().getTime() );
          } else if (node.hasProperty( "jecars:Published")) {
            lastModified = DavConstants.modificationDateFormat.format( node.getProperty( "jecars:Published" ).getDate().getTime() );
          } else if (node.hasProperty( "jcr:lastModified")) {
            lastModified = DavConstants.modificationDateFormat.format( node.getProperty( "jcr:lastModified" ).getDate().getTime() );
          } else if (node.hasProperty( "jcr:created" )) {
            lastModified = DavConstants.modificationDateFormat.format( node.getProperty( "jcr:created" ).getDate().getTime() );
          }
        } catch(RepositoryException re ) {
          log.warn("Error while accessing resource properties", re );
        }
        if (lastModified==null) lastModified = DavConstants.modificationDateFormat.format( new Date() );
        properties.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED, lastModified));

        // **** last created
//        String created = null;
//        try {
//          if (node.hasProperty( "jcr:created" )==true) {
//            created = DavConstants.modificationDateFormat.format( node.getProperty( "jcr:created" ).getDate().getTime() );
//          }
//        } catch(RepositoryException re ) {
//          log.warn("Error while accessing resource properties", re );
//        }
//        if (created!=null) {
//          properties.add(new DefaultDavProperty(DavPropertyName.CREATIONDATE, created));
//        }

        // **** mimeType
        try {
          if (node.hasProperty( "jcr:mimeType" )) {
            final String ct = IOUtil.buildContentType( node.getProperty( "jcr:mimeType" ).getString(), "utf-8" );
            properties.add(new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE, ct ));
          }
        } catch(RepositoryException re ) {
          log.warn("Error while accessing resource properties", re );
        }

        // **** contentLength
        try {
          if (node.hasProperty( "jecars:ContentLength" )) {
            properties.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLENGTH, node.getProperty( "jecars:ContentLength" ).getString() ));
          }
        } catch(RepositoryException re ) {
          log.warn("Error while accessing resource properties", re );
        }

        // **** Add other properties
        try {
          final PropertyIterator pi = node.getProperties();
          while( pi.hasNext() ) {
            final Property prop = pi.nextProperty();
            if (prop.getName().startsWith( "jecars:" )) {
              if (prop.getDefinition().getRequiredType()!=PropertyType.BINARY) {
                final DefaultDavProperty ddp;
                if (node.hasProperty( prop.getName() + "_NS" )) {
                  final Namespace ns = Namespace.getNamespace( node.getProperty( prop.getName() + "_NS" ).getString() );
                  ddp = new DefaultDavProperty(
                        DavPropertyName.create( prop.getName().substring( "jecars:".length() ), ns ),
                        prop.getString() );
                } else {
                  ddp = new DefaultDavProperty(
                        DavPropertyName.create( prop.getName().substring( "jecars:".length() ), jecarsns ),
                        prop.getString() );
                }
                properties.add( ddp );
              }
            }
          }
        } catch(RepositoryException re ) {
          log.warn("Error while accessing jecars properties", re );
        }

        if (rfc4122Uri != null) {
          properties.add(new HrefProperty(BindConstants.RESOURCEID, rfc4122Uri, true));
        }

        final Set parentElements = getParentElements();
        if (!parentElements.isEmpty()) {
          properties.add(new ParentSet(parentElements));
        }

        /* set current lock information. If no lock is set to this resource,
        an empty lockdiscovery will be returned in the response. */
        properties.add(new LockDiscovery(getLock(Type.WRITE, Scope.EXCLUSIVE)));

        /* lock support information: all locks are lockable. */
        final SupportedLock supportedLock = new SupportedLock();
        supportedLock.addEntry(Type.WRITE, Scope.EXCLUSIVE);
        properties.add(supportedLock);

        // **** Additional WebDAV Collection Properties
        // **** http://greenbytes.de/tech/webdav/draft-hopmann-collection-props-00.txt
        try {
          if (node.getDepth()==0) {
            properties.add(new DefaultDavProperty(DavPropertyName.create( "isroot" ), "1" ));
          }
          if (node.isNodeType( "jecars:permissionable" )) {
            properties.add(new DefaultDavProperty(DavPropertyName.create( "ishidden" ), "1" ));
          }
          properties.add(new DefaultDavProperty(DavPropertyName.create( "id" ), node.getIdentifier() ));
          if ((node.isNodeType( "jecars:datafolder" )) || (node.isNodeType( "jecars:configresource" ))) {
//          if (node.isNodeType( "jecars:datafolder" )) {
            properties.add(new DefaultDavProperty(DavPropertyName.create( "isfolder" ), "1" ));
          } else if (node.isNodeType( "jecars:datafile" )) {
            properties.add(new DefaultDavProperty(DavPropertyName.create( "isfolder" ), "0" ));
          }
        } catch(RepositoryException re ) {
          log.warn("Error while accessing resource properties", re );
        }

        propsInitialized = true;
        return;
    }

    /**
     * @param property
     * @throws DavException
     * @see DavResource#setProperty(org.apache.jackrabbit.webdav.property.DavProperty)
     */
    public void setProperty(DavProperty property) throws DavException {
        alterProperty(property);
    }

    /**
     * @param propertyName
     * @throws DavException
     * @see DavResource#removeProperty(org.apache.jackrabbit.webdav.property.DavPropertyName)
     */
    public void removeProperty(DavPropertyName propertyName) throws DavException {
        alterProperty(propertyName);
    }

    private void alterProperty(Object prop) throws DavException {
        if (isLocked(this)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
        if (!exists()) {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
        try {
            List l = new ArrayList(1);
            l.add(prop);
            alterProperties(l);
            Map failure = config.getPropertyManager().alterProperties(getPropertyImportContext(l), isCollection());
            if (failure.isEmpty()) {
                node.save();
            } else {
                node.refresh(false);
                // TODO: retrieve specific error from failure-map
                throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (RepositoryException e) {
            // revert any changes made so far
            JcrDavException je = new JcrDavException(e);
            try {
                node.refresh(false);
            } catch (RepositoryException re) {
                // should not happen...
            }
            throw je;
        }
    }

    /**
     * @see DavResource#alterProperties(DavPropertySet, DavPropertyNameSet)
     */
    public MultiStatusResponse alterProperties(DavPropertySet setProperties,
                                               DavPropertyNameSet removePropertyNames)
            throws DavException {
        List changeList = new ArrayList();
        if (removePropertyNames != null) {
            DavPropertyNameIterator it = removePropertyNames.iterator();
            while (it.hasNext()) {
                changeList.add(it.next());
            }
        }
        if (setProperties != null) {
            DavPropertyIterator it = setProperties.iterator();
            while (it.hasNext()) {
                changeList.add(it.next());
            }
        }
        return alterProperties(changeList);
    }

    /** alterProperties
     * 
     * @param pChangeList
     * @return
     * @throws DavException
     */
    public MultiStatusResponse alterProperties( final List pChangeList ) throws DavException {
        if (isLocked(this)) {
          throw new DavException(DavServletResponse.SC_LOCKED);
        }
        if (!exists()) {
          throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
        final MultiStatusResponse msr = new MultiStatusResponse(getHref(), null);
        try {
            final CARS_DavPropertyManager dpm = new CARS_DavPropertyManager( this );
            final Map failures = dpm.alterProperties(getPropertyImportContext(pChangeList), isCollection());
//            Map failures = config.getPropertyManager().alterProperties(getPropertyImportContext(pChangeList), isCollection());
            if (failures.isEmpty()) {
                // save all changes together (reverted in case this fails)
                node.save();
            } else {
                // set/remove of at least a single prop failed: undo modifications.
                node.refresh(false);
            }
            /* loop over list of properties/names that were successfully altered
               and them to the multistatus response respecting the resulte of the
               complete action. in case of failure set the status to 'failed-dependency'
               in order to indicate, that altering those names/properties would
               have succeeded, if no other error occured.*/
            final Iterator it = pChangeList.iterator();
            while (it.hasNext()) {
                Object o = it.next();
                int statusCode;
                if (failures.containsKey(o)) {
                    Object error = failures.get(o);
                    statusCode = (error instanceof RepositoryException)
                        ? new JcrDavException((RepositoryException) error).getErrorCode()
                        : DavServletResponse.SC_INTERNAL_SERVER_ERROR;
                } else {
                    statusCode = (failures.isEmpty()) ? DavServletResponse.SC_OK : DavServletResponse.SC_FAILED_DEPENDENCY;
                }
                if (o instanceof DavProperty) {
                    msr.add(((DavProperty) o).getName(), statusCode);
                } else {
                    msr.add((DavPropertyName) o, statusCode);
                }
            }
            return msr;
        } catch (RepositoryException e) {
            // revert any changes made so far an throw exception
            try {
                node.refresh(false);
            } catch (RepositoryException re) {
                // should not happen
            }
            throw new JcrDavException(e);
        }
    }

    /**
     * @see DavResource#getCollection()
     */
    public DavResource getCollection() {
        DavResource parent = null;
        if (getResourcePath() != null && !getResourcePath().equals("/")) {
            String parentPath = Text.getRelativeParent(getResourcePath(), 1);
            if ("".equals(parentPath)) {
                parentPath = "/";
            }
            DavResourceLocator parentloc = locator.getFactory().createResourceLocator(locator.getPrefix(), locator.getWorkspacePath(), parentPath);
            try {
                parent = factory.createResource(parentloc, session);
            } catch (DavException e) {
              e.printStackTrace();
            }
        }
        return parent;
    }

    /**
     * @see DavResource#getMembers()
     */
    public DavResourceIterator getMembers() {
        ArrayList list = new ArrayList();
        if (exists() && isCollection()) {
            try {
                NodeIterator it = node.getNodes();
                while (it.hasNext()) {
                    Node n = it.nextNode();
                    if (!isFilteredItem(n)) {
                        DavResourceLocator resourceLocator = locator.getFactory().createResourceLocator(locator.getPrefix(), locator.getWorkspacePath(), n.getPath(), false);
                        DavResource childRes = factory.createResource(resourceLocator, session);
                        list.add(childRes);
                    } else {
                        log.debug("Filtered resource '" + n.getName() + "'.");
                    }
                }
            } catch (RepositoryException e) {
              e.printStackTrace();
            } catch (DavException e) {
              e.printStackTrace();
            }
        }
        return new DavResourceIteratorImpl(list);
    }

    /**
     * Adds a new pMember to this resource.
     *
     * @see DavResource#addMember(DavResource, org.apache.jackrabbit.webdav.io.InputContext)
     */
    @Override
    public void addMember( final DavResource pMember, final InputContext pInputContext ) throws DavException {
        if (!exists()) {
            throw new DavException(DavServletResponse.SC_CONFLICT);
        }
        if (isLocked(this) || isLocked(pMember)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
        try {
            // don't allow creation of nodes if this resource represents a protected
            // item or if the new resource would be filtered out
            if (isFilteredResource(pMember) || node.getDefinition().isProtected()) {
                log.debug("Forbidden to add member: " + pMember.getDisplayName());
                throw new DavException(DavServletResponse.SC_FORBIDDEN);
            }

            final String memberName = Text.getName( pMember.getLocator().getRepositoryPath() );
//       String s = CARS_Utils.readAsString( pInputContext.getInputStream() );
//          System.out.println("disjf " + s );
//        if ("".equals(s)) return;
            final ImportContext ctx = getImportContext( pInputContext, memberName );
//            try {
//              if (!config.getIOManager().importContent(ctx, pMember)) {
//                // any changes should have been reverted in the importer
//                throw new DavException(DavServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
//              }
//            } catch( IOException ie ) {
                final CARS_DavSession davSession = (CARS_DavSession)pMember.getSession();
                if (pMember.isCollection()) {
                  // ********************
                  // **** Add a directory
                  final Node interfaceNode;
                  final CARS_Interface di;
                  if (node.hasProperty( CARS_DefaultMain.DEF_INTERFACECLASS )) {
                    interfaceNode = node;
                    final String clss = node.getProperty( CARS_DefaultMain.DEF_INTERFACECLASS ).getString();
                    di = (CARS_Interface)Class.forName( clss ).newInstance();
                  } else {
                    interfaceNode = null;
                    di = new CARS_DefaultInterface();
                  }
                  final CARS_Main main = davSession.getActionContext().getMain();
//                    final CARS_DefaultInterface di = new CARS_DefaultInterface();
                  final Node cnode;
                  if (node.isNodeType( "jecars:Dav_deftypes" ) && node.hasProperty( "jecars:Dav_DefaultFolderType" )) {
//                    node.addNode( memberName, node.getProperty( "jecars:Dav_DefaultFolderType" ).getString() );
                    cnode = di.addNode( main, interfaceNode, node, memberName, node.getProperty( "jecars:Dav_DefaultFolderType" ).getString(), null );
                  } else {
//                    node.addNode( memberName, "jecars:datafolder" );
                    cnode = di.addNode( main, interfaceNode, node, memberName, "jecars:datafolder", null );
                  }
                  di.nodeAdded( main, interfaceNode, cnode, null );
                  main.getSession().save();
                  di.nodeAddedAndSaved( main, interfaceNode, cnode );

                } else {
                    // ***************
                    // **** Add a file
                  if (node.isNodeType( "jecars:Dav_deftypes" ) && node.hasProperty( "jecars:Dav_DefaultFileType" )) {
                    final Node interfaceNode;
                    final CARS_Interface di;
                    if (node.hasProperty( CARS_DefaultMain.DEF_INTERFACECLASS )) {
                      interfaceNode = node;
                      final String clss = node.getProperty( CARS_DefaultMain.DEF_INTERFACECLASS ).getString();
                      di = (CARS_Interface)Class.forName( clss ).newInstance();
                    } else {
                      interfaceNode = null;
                      di = new CARS_DefaultInterface();
                    }
//                    final CARS_DefaultInterface di = new CARS_DefaultInterface();
                    final CARS_Main main = davSession.getActionContext().getMain();
                    final Node cnode = di.addNode( main, null, node, memberName, node.getProperty( "jecars:Dav_DefaultFileType" ).getString(), null );
                    di.setBodyStream( main, interfaceNode, cnode, ctx.getInputStream(), "" );//ctx.getMimeType() ); // **** TODO
                    try {
                      cnode.setProperty( "jecars:ContentLength", ctx.getContentLength() );
                    } catch( RepositoryException re ) {                        
                    }
                    di.nodeAdded( main, interfaceNode, cnode, null );
                    main.getSession().save();
                    di.nodeAddedAndSaved( main, interfaceNode, cnode );
                  } else {
                    final Node interfaceNode;
                    final CARS_Interface di;
                    if (node.hasProperty( CARS_DefaultMain.DEF_INTERFACECLASS )) {
                      interfaceNode = node;
                      final String clss = node.getProperty( CARS_DefaultMain.DEF_INTERFACECLASS ).getString();
                      di = (CARS_Interface)Class.forName( clss ).newInstance();
                    } else {
                      interfaceNode = null;
                      di = new CARS_DefaultInterface();
                    }
                    final CARS_Main main = davSession.getActionContext().getMain();
                    final Node cnode = di.addNode( main, null, node, memberName, "jecars:datafile", null );
//                    Node cnode = node.addNode( memberName, "jecars:datafile" );
                    String mimeType = ctx.getMimeType();
//                      System.out.println("dkoosdk " + mimeType );
                    if (("application/octet-stream".equals( mimeType )) || ("text/plain".equals( mimeType ))) {
                      mimeType = CARS_Mime.getMIMEType( memberName, null );
                    }
//                    cnode.setProperty( "jcr:mimeType", mimeType );
//                    final Calendar c = Calendar.getInstance();
//                    c.setTimeInMillis( ctx.getModificationTime() );
//                    cnode.setProperty( "jcr:lastModified", c );
                    di.setBodyStream( main, interfaceNode, cnode, ctx.getInputStream(), mimeType );
//                    final Binary bin = node.getSession().getValueFactory().createBinary( ctx.getInputStream() );
//                    cnode.setProperty( "jcr:data", bin );
//                    cnode.setProperty( "jcr:data", ctx.getInputStream() );
                    cnode.setProperty( "jecars:ContentLength", ctx.getContentLength() );
                    di.nodeAdded( main, interfaceNode, cnode, null );
                    main.getSession().save();
                    di.nodeAddedAndSaved( main, interfaceNode, cnode );
                  }
                }
//            }
            // persist changes after successful import
            node.save();
        } catch (RepositoryException e) {
            log.error("Error while importing resource: " + e.toString());
            throw new JcrDavException(e);
        } catch (IOException e) {
            log.error("Error while importing resource: " + e.toString());
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("Error while importing resource: " + e.toString());
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * @see DavResource#removeMember(DavResource)
     */
    @Override
    public void removeMember( final DavResource pMember) throws DavException {
        if (!exists() || !pMember.exists()) {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
        if (isLocked(this) || isLocked(pMember)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }

        // don't allow removal of nodes, that would be filtered out
        if (isFilteredResource(pMember)) {
            log.debug("Avoid removal of filtered resource: " + pMember.getDisplayName());
            throw new DavException(DavServletResponse.SC_FORBIDDEN);
        }


        try {
//            String itemPath = pMember.getLocator().getRepositoryPath();
//            Item memItem = getJcrSession().getItem(itemPath);
            final CARS_DavResource cdr = (CARS_DavResource)pMember;
            final Node memItem = cdr.getNode();
            //TODO once jcr2 is out: simply call removeShare()
//            if (memItem instanceof org.apache.jackrabbit.api.jsr283.Node) {
//                org.apache.jackrabbit.api.jsr283.Node n = (org.apache.jackrabbit.api.jsr283.Node) memItem;
//                n.removeShare();
//            } else {
                final CARS_DavSession davSession = (CARS_DavSession)pMember.getSession();
//                final CARS_DefaultInterface di = new CARS_DefaultInterface();
                final CARS_Main main = davSession.getActionContext().getMain();
                main.removeNode( memItem.getPath(), null );
//                di.removeNode( main, null, memItem, null);
//                memItem.remove();
//            }
            getJcrSession().save();

            // make sure, non-jcr locks are removed, once the removal is completed
            try {
                if (!isJsrLockable()) {
                    ActiveLock lock = getLock(Type.WRITE, Scope.EXCLUSIVE);
                    if (lock != null) {
                        lockManager.releaseLock(lock.getToken(), pMember);
                    }
                }
            } catch (DavException e) {
                // since check for 'locked' exception has been performed before
                // ignore any error here
            }
        } catch (RepositoryException re) {
            throw new JcrDavException(re);
        } catch (Exception e) {
            throw new DavException( DavServletResponse.SC_INTERNAL_SERVER_ERROR, e );
        }
    }

    /** move
     * 
     * @param destination
     * @throws DavException
     */
    @Override
    public void move( final DavResource pDestination ) throws DavException {
      move( pDestination );
      return;
    }

    /**
     * @see DavResource#move(DavResource)
     */
    public void move( final DavResource pDestination, final String pOverwrite ) throws DavException {
        if (!exists()) {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
        if (isLocked(this)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
        if (isFilteredResource(pDestination)) {
            throw new DavException(DavServletResponse.SC_FORBIDDEN);
        }
        // make sure, that src and pDestination belong to the same workspace
        checkSameWorkspace(pDestination.getLocator());
//            String destItemPath = pDestination.getLocator().getRepositoryPath();
            final CARS_DavSession ses = (CARS_DavSession)session;
            final CARS_ActionContext ac;
            try {
              ac = CARS_ActionContext.createActionContext( ses.getActionContext() );
            } catch( CloneNotSupportedException ce ) {
              throw new JcrDavException( ce, DavServletResponse.SC_INTERNAL_SERVER_ERROR );
            }
//            final CARS_DavSession   ses = (CARS_DavSession)session;
//            final CARS_ActionContext ac = CARS_ActionContext.createActionContext( ses.getActionContext() );
            final String scrPath  = CARS_DavResourceFactory.getRepoPath( getLocator() );
            final String destPath = CARS_DavResourceFactory.getRepoPath( pDestination.getLocator() ); // ac.getPathInfo();
//            String path = getLocator().getResourcePath();
//            if (path.startsWith( "/webdav" )) {
//              path = path.substring( "/webdav".length() );
//            }

            if (OverwriteHeader.OVERWRITE_FALSE.equals( pOverwrite )) {
              // **** Check if resource already exists
              final Session admSession = CARS_Factory.getSystemApplicationSession();
              synchronized( admSession ) {
                try {
                  admSession.getNode( destPath );
                  // **** Error not allowed
                  throw new JcrDavException( new ItemExistsException( destPath ), DavServletResponse.SC_PRECONDITION_FAILED );
                } catch( RepositoryException re ) {
                }
              }
            }

            try {
              ac.setPathInfo( scrPath );
              ac.setQueryString( CARS_Definitions.DEFAULTNS + "title=" + pDestination.getLocator().getResourcePath() );
              ses.getFactory().performPutAction( ac, ac.getMain() );
            } catch (CredentialExpiredException cee) {
              throw new JcrDavException( cee, DavServletResponse.SC_METHOD_NOT_ALLOWED );
            } catch (AccessDeniedException ade) {
              throw new JcrDavException( ade, DavServletResponse.SC_METHOD_NOT_ALLOWED );
            }

//            ac.setPathInfo( pDestination.getLocator().getResourcePath() );
//            final Map map = ac.getParameterMap();
//            final String[] link = {""};
//            final String[] rel = {"via"};
//            final String[] href = {ac.getBaseContextURL() + locator.getRepositoryPath() };
//            map.put( "$0.link", link );
//            map.put( "$0.link.rel", rel );
//            map.put( "$0.link.href", href );
//            ac.setParameterMap( map );
//            ses.getFactory().performPostAction( ac );
    }

    /** copy
     *
     * @param pDestination
     * @param pShallow
     * @throws DavException
     */
    @Override
    public void copy( final DavResource pDestination, final boolean pShallow ) throws DavException {
      copy( pDestination, pShallow, "T" );
      return;
    }

    /** copy
     *
     * @param pDestination
     * @param pShallow
     * @param pOverwrite
     * @throws DavException
     */
    public void copy( final DavResource pDestination, final boolean pShallow, final String pOverwrite ) throws DavException {
        final CARS_DavResource destination = (CARS_DavResource)pDestination;
        if (!exists()) {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
        if (isLocked(destination)) {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
        if (isFilteredResource(destination)) {
            throw new DavException(DavServletResponse.SC_FORBIDDEN);
        }
//        if (pShallow && isCollection()) {
            // TODO: currently no support for shallow copy; however this is
            // only relevant if the source resource is a collection, because
            // otherwise it doesn't make a difference
//            throw new DavException(DavServletResponse.SC_FORBIDDEN, "Unable to perform shallow copy.");
//        }
        // make sure, that src and pDestination belong to the same workspace
        checkSameWorkspace(destination.getLocator());


        final CARS_DavSession ses = (CARS_DavSession)session;
        final CARS_ActionContext ac;
        try {
          ac = CARS_ActionContext.createActionContext( ses.getActionContext() );
        } catch( CloneNotSupportedException ce ) {
          throw new JcrDavException( ce, DavServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
        final String srcPath  = locator.getRepositoryPath();
        final String destPath = CARS_DavResourceFactory.getRepoPath( destination.getLocator() ); // ac.getPathInfo();
        ac.setPathInfo( destPath );

        if (OverwriteHeader.OVERWRITE_FALSE.equals( pOverwrite )) {
          // **** Check if resource already exists
          final Session admSession = CARS_Factory.getSystemApplicationSession();
          synchronized( admSession ) {
            try {
//              final String path = pDestination.getNode().getPath();//ses.getActionContext().getPathInfo();
              admSession.getNode( destPath );
              // **** Error not allowed
              throw new JcrDavException( new ItemExistsException( destPath ), DavServletResponse.SC_PRECONDITION_FAILED );
            } catch( RepositoryException re ) {
            }
          }
        }

        try {
//            String destItemPath = pDestination.getLocator().getRepositoryPath();
//            getJcrSession().getWorkspace().copy(locator.getRepositoryPath(), destItemPath);


//            ac.setPathInfo( pDestination.getNode().getPath() );
            final Map map = ac.getParameterMap();
            final String[] link = {""};
            final String[] rel = {"via"};
            final String[] href = {ac.getBaseContextURL() + srcPath};// destItemPath.substring(0, destItemPath.lastIndexOf('/'))};
            map.put( "$0.link", link );
            map.put( "$0.link.rel", rel );
            map.put( "$0.link.href", href );
            ac.setParameterMap( map );
            ses.getFactory().performPostAction( ac );
            if (ac.getErrorCode()==HttpURLConnection.HTTP_NOT_FOUND) {
              throw new JcrDavException( ac.getError(), DavServletResponse.SC_CONFLICT );
            }
        } catch (CredentialExpiredException cee) {
          throw new JcrDavException( cee, DavServletResponse.SC_METHOD_NOT_ALLOWED );
        } catch (AccessDeniedException ade) {
          throw new JcrDavException( ade, DavServletResponse.SC_METHOD_NOT_ALLOWED );
//        } catch( RepositoryException re ) {
//          throw new JcrDavException( re, DavServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
    }

    /**
     * @param type
     * @param scope
     * @return true if type is {@link Type#WRITE} and scope is {@link Scope#EXCLUSIVE}
     * @see DavResource#isLockable(org.apache.jackrabbit.webdav.lock.Type, org.apache.jackrabbit.webdav.lock.Scope)
     */
    public boolean isLockable(Type type, Scope scope) {
        return Type.WRITE.equals(type) && Scope.EXCLUSIVE.equals(scope);
    }

    /**
     * @see DavResource#hasLock(org.apache.jackrabbit.webdav.lock.Type, org.apache.jackrabbit.webdav.lock.Scope)
     */
    public boolean hasLock(Type type, Scope scope) {
        return getLock(type, scope) != null;
    }

    /**
     * @see DavResource#getLock(Type, Scope)
     */
    public ActiveLock getLock( final Type type, final Scope scope) {
        ActiveLock lock = null;
        if (exists() && Type.WRITE.equals(type) && Scope.EXCLUSIVE.equals(scope)) {
            // try to retrieve the repository lock information first
            try {
                if (node.isLocked()) {
                    Lock jcrLock = node.getLock();
                    if (jcrLock != null && jcrLock.isLive()) {
                        lock = new JcrActiveLock(jcrLock);
                    }
                }
            } catch (RepositoryException e) {
                // LockException (no lock applies) >> should never occur
                // RepositoryException, AccessDeniedException or another error >> ignore
            }

            // could not retrieve a jcr-lock. test if a simple webdav lock is present.
            if (lock == null) {
                lock = lockManager.getLock(type, scope, this);
            }
        }
        return lock;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getLocks()
     */
    public ActiveLock[] getLocks() {
        ActiveLock writeLock = getLock(Type.WRITE, Scope.EXCLUSIVE);
        return (writeLock != null) ? new ActiveLock[]{writeLock} : new ActiveLock[0];
    }

    /**
     * @see DavResource#lock(LockInfo)
     */
    public ActiveLock lock(LockInfo lockInfo) throws DavException {
        ActiveLock lock = null;
        if (isLockable(lockInfo.getType(), lockInfo.getScope())) {
            // TODO: deal with existing locks, that may have been created, before the node was jcr-lockable...
            if (isJsrLockable()) {
                try {
                    // try to execute the lock operation
                    Lock jcrLock = node.lock(lockInfo.isDeep(), false);
                    if (jcrLock != null) {
                        lock = new JcrActiveLock(jcrLock);
                    }
                } catch (RepositoryException e) {
                    throw new JcrDavException(e);
                }
            } else {
                // create a new webdav lock
                lock = lockManager.createLock(lockInfo, this);
            }
        } else {
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "Unsupported lock type or scope.");
        }
        return lock;
    }

    /**
     * @see DavResource#refreshLock(LockInfo, String)
     */
    public ActiveLock refreshLock(LockInfo lockInfo, String lockToken) throws DavException {
        if (!exists()) {
            throw new DavException(DavServletResponse.SC_NOT_FOUND);
        }
        ActiveLock lock = getLock(lockInfo.getType(), lockInfo.getScope());
        if (lock == null) {
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED, "No lock with the given type/scope present on resource " + getResourcePath());
        }

        if (lock instanceof JcrActiveLock) {
            try {
                // refresh JCR lock and return the original lock object.
                node.getLock().refresh();
            } catch (RepositoryException e) {
                throw new JcrDavException(e);
            }
        } else {
            lock = lockManager.refreshLock(lockInfo, lockToken, this);
        }
        /* since lock has infinite lock (simple) or undefined timeout (jcr)
           return the lock as retrieved from getLock. */
        return lock;
    }

    /**
     * @see DavResource#unlock(String)
     */
    @Override
    public void unlock( final String lockToken ) throws DavException {
        final ActiveLock lock = getLock(Type.WRITE, Scope.EXCLUSIVE);
 if (lock!=null) {
     try {
     System.out.println(" ---0--0 " + node.getPath());
    String ll = lock.getToken();
     System.out.println("  asjioasjio " + ll + " ::: " + lockToken);
     } catch(RepositoryException re ) {
       re.printStackTrace();
     }
 }
        if (lock == null) {
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
        } else if (lock.isLockedByToken(lockToken)) {
            if (lock instanceof JcrActiveLock) {
                try {
                    node.unlock();
                } catch (RepositoryException e) {
                    throw new JcrDavException(e);
                }
            } else {
                lockManager.releaseLock(lockToken, this);
            }
        } else {
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
    }

    /**
     * @see DavResource#addLockManager(org.apache.jackrabbit.webdav.lock.LockManager)
     */
    public void addLockManager(LockManager lockMgr) {
        this.lockManager = lockMgr;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getFactory()
     */
    public DavResourceFactory getFactory() {
        return factory;
    }

    /**
     * @see org.apache.jackrabbit.webdav.DavResource#getSession()
     */
    public DavSession getSession() {
        return session;
    }


    /**
     * @see BindableResource#rebind(DavResource, DavResource)
     */
    public void bind(DavResource collection, DavResource newBinding) throws DavException {
        if (!exists()) {
            //DAV:bind-source-exists
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
        }
        if (isLocked(collection)) {
            //DAV:locked-update-allowed?
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
        if (isFilteredResource(newBinding)) {
            throw new DavException(DavServletResponse.SC_FORBIDDEN);
        }
        checkSameWorkspace(collection.getLocator());
        try {
            if (!node.isNodeType(MIX_SHAREABLE)) {
                if (!node.canAddMixin(MIX_SHAREABLE)) {
                    //DAV:binding-allowed
                    throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
                }
                node.addMixin(MIX_SHAREABLE);
                node.save();
            }
            Workspace workspace = session.getRepositorySession().getWorkspace();
            workspace.clone(workspace.getName(), node.getPath(), newBinding.getLocator().getRepositoryPath(), false);

        } catch (RepositoryException e) {
            throw new JcrDavException(e);
        }

    }

    /**
     * @see BindableResource#rebind(DavResource, DavResource)
     */
    public void rebind(DavResource collection, DavResource newBinding) throws DavException {
        if (!exists()) {
            //DAV:rebind-source-exists
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
        }
        if (isLocked(this)) {
            //DAV:protected-source-url-deletion.allowed
            throw new DavException(DavServletResponse.SC_PRECONDITION_FAILED);
        }
        if (isLocked(collection)) {
            //DAV:locked-update-allowed?
            throw new DavException(DavServletResponse.SC_LOCKED);
        }
        if (isFilteredResource(newBinding)) {
            throw new DavException(DavServletResponse.SC_FORBIDDEN);
        }
        checkSameWorkspace(collection.getLocator());
        try {
            if (!node.isNodeType(MIX_REFERENCEABLE)) {
                throw new DavException(node.canAddMixin(MIX_REFERENCEABLE)?
                                       DavServletResponse.SC_CONFLICT : DavServletResponse.SC_METHOD_NOT_ALLOWED);
            }
            getJcrSession().getWorkspace().move(locator.getRepositoryPath(), newBinding.getLocator().getRepositoryPath());
        } catch (RepositoryException e) {
            throw new JcrDavException(e);
        }
    }

    /**
     * @see org.apache.jackrabbit.webdav.bind.BindableResource#getParentElements()
     */
    public Set getParentElements() {
        try {
            if (node.getDepth() > 0) {
                //TODO remove this check once jcr2 is out
//                if (!(node instanceof org.apache.jackrabbit.api.jsr283.Node)) {
                    DavResourceLocator loc = locator.getFactory().createResourceLocator(
                            locator.getPrefix(), locator.getWorkspacePath(), node.getParent().getPath(), false);
                    return Collections.singleton(new ParentElement(loc.getHref(true), node.getName()));
//                }
//                Set ps = new HashSet();
//                NodeIterator sharedSetIterator = ((org.apache.jackrabbit.api.jsr283.Node) node).getSharedSet();
//                while (sharedSetIterator.hasNext()) {
//                    Node sharednode = sharedSetIterator.nextNode();
//                    DavResourceLocator loc = locator.getFactory().createResourceLocator(
//                            locator.getPrefix(), locator.getWorkspacePath(), sharednode.getParent().getPath(), false);
//                    ps.add(new ParentElement(loc.getHref(true), sharednode.getName()));
//                }
//                return ps;
            }
        } catch (RepositoryException e) {
            log.warn("unable to calculate parent set", e);
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Returns the node that is wrapped by this resource.
     *
     * @return
     */
    protected Node getNode() {
        return node;
    }

    /**
     * Returns a new <code>ImportContext</code>
     *
     * @param pInputCtx
     * @param systemId
     * @return a new <code>ImportContext</code>
     * @throws IOException
     */
    protected ImportContext getImportContext( final InputContext pInputCtx, final String pSystemId ) throws IOException {
  // **** TODO, resolve for Jackrabbit v2.0
//      throw new UnsupportedOperationException( "resolve for Jackrabbit v2.0" );
//        return new ImportContextImpl(node, systemId, pInputCtx, config.getMimeResolver());
      final Detector d = config.getDetector();
      return new ImportContextImpl( node, pSystemId, pInputCtx, (pInputCtx!=null) ? pInputCtx.getInputStream() : null, new DefaultIOListener(log), d );
    }

    /**
     * Returns a new <code>ExportContext</code>
     *
     * @param outputCtx
     * @return a new <code>ExportContext</code>
     * @throws IOException
     */
    protected ExportContext getExportContext(OutputContext outputCtx) throws IOException {
  // **** TODO, resolve for Jackrabbit v2.0
//      throw new UnsupportedOperationException( "resolve for Jackrabbit v2.0" );
      return new ExportContextImpl( node, outputCtx );
    }

    /**
     * Returns a new <code>PropertyImportContext</code>.
     *
     * @param pChangeList
     * @return
     */
    protected PropertyImportContext getPropertyImportContext(List changeList) {
        return new ProperyImportCtx(changeList);
    }

    /**
     * Returns a new <code>PropertyExportContext</code>.
     *
     * @return a new <code>PropertyExportContext</code>
     */
    protected PropertyExportContext getPropertyExportContext() {
        return new PropertyExportCtx();
    }

    /**
     * Returns true, if the underlying node is nodetype jcr:lockable,
     * without checking its current lock status. If the node is not jcr-lockable
     * an attempt is made to add the mix:lockable mixin type.
     *
     * @return true if this resource is lockable.
     */
    private boolean isJsrLockable() {
        boolean lockable = false;
        if (exists()) {
            try {
                lockable = node.isNodeType(MIX_LOCKABLE);
                // not jcr-lockable: try to make the node jcr-lockable
                if (!lockable && node.canAddMixin(MIX_LOCKABLE)) {
                    node.addMixin(MIX_LOCKABLE);
                    node.save();
                    lockable = true;
                }
            } catch (RepositoryException e) {
                // -> node is definitely not jcr-lockable.
            }
        }
        return lockable;
    }

    /**
     * Return true if this resource cannot be modified due to a write lock
     * that is not owned by the given session.
     *
     * @return true if this resource cannot be modified due to a write lock
     */
    private boolean isLocked(DavResource res) {
        ActiveLock lock = res.getLock(Type.WRITE, Scope.EXCLUSIVE);
        if (lock == null) {
            return false;
        } else {
            String[] sLockTokens = session.getLockTokens();
            for (int i = 0; i < sLockTokens.length; i++) {
                if (sLockTokens[i].equals(lock.getToken())) {
                    return false;
                }
            }
            return true;
        }
    }

    private Session getJcrSession() {
        return session.getRepositorySession();
    }

    private boolean isFilteredResource(DavResource resource) {
        // TODO: filtered nodetypes should be checked as well in order to prevent problems.
        ItemFilter filter = config.getItemFilter();
        return filter != null && filter.isFilteredItem(resource.getDisplayName(), getJcrSession());
    }

    private boolean isFilteredItem(Item item) {
        ItemFilter filter = config.getItemFilter();
        return filter != null && filter.isFilteredItem(item);
    }

    private void checkSameWorkspace(DavResourceLocator otherLoc) throws DavException {
//        String wspname = getJcrSession().getWorkspace().getName();
//        if (!wspname.equals(otherLoc.getWorkspaceName())) {
//            throw new DavException(DavServletResponse.SC_FORBIDDEN, "Workspace mismatch: expected '" + wspname + "'; found: '" + otherLoc.getWorkspaceName() + "'");
//        }
    }

    //--------------------------------------------------------< inner class >---
    /**
     * ExportContext that writes the properties of this <code>DavResource</code>
     * and provides not stream.
     */
    private class PropertyExportCtx extends AbstractExportContext implements PropertyExportContext {

        private PropertyExportCtx() {
            super(node, false, null);
            // set defaults:
            setCreationTime(IOUtil.UNDEFINED_TIME);
            setModificationTime(IOUtil.UNDEFINED_TIME);
        }

        public OutputStream getOutputStream() {
            return null;
        }

        public void setContentLanguage(String contentLanguage) {
            if (contentLanguage != null) {
                properties.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLANGUAGE, contentLanguage));
            }
        }

        public void setContentLength(long contentLength) {
            if (contentLength > IOUtil.UNDEFINED_LENGTH) {
                properties.add(new DefaultDavProperty(DavPropertyName.GETCONTENTLENGTH, contentLength + ""));
            }
        }

        public void setContentType(String mimeType, String encoding) {
            String contentType = IOUtil.buildContentType(mimeType, encoding);
            if (contentType != null) {
                properties.add(new DefaultDavProperty(DavPropertyName.GETCONTENTTYPE, contentType));
            }
        }

        public void setCreationTime(long creationTime) {
            String created = IOUtil.getCreated(creationTime);
            properties.add(new DefaultDavProperty(DavPropertyName.CREATIONDATE, created));
        }

        public void setModificationTime(long modTime) {
            if (modTime <= IOUtil.UNDEFINED_TIME) {
                modificationTime = new Date().getTime();
            } else {
                modificationTime = modTime;
            }
            String lastModified = IOUtil.getLastModified(modificationTime);
            properties.add(new DefaultDavProperty(DavPropertyName.GETLASTMODIFIED, lastModified));
        }

        public void setETag(String etag) {
            if (etag != null) {
                properties.add(new DefaultDavProperty(DavPropertyName.GETETAG, etag));
            }
        }

        public void setProperty(Object propertyName, Object propertyValue) {
            if (propertyValue == null) {
                log.warn("Ignore 'setProperty' for " + propertyName + "with null value.");
                return;
            }

            if (propertyValue instanceof DavProperty) {
                properties.add((DavProperty)propertyValue);
            } else {
                DavPropertyName pName;
                if (propertyName instanceof DavPropertyName) {
                    pName = (DavPropertyName)propertyName;
                } else {
                    // create property name with default DAV: namespace
                    pName = DavPropertyName.create(propertyName.toString());
                }
                properties.add(new DefaultDavProperty(pName, propertyValue));
            }
        }
    }

    private class ProperyImportCtx implements PropertyImportContext {

        private final IOListener ioListener = new DefaultIOListener(log);
        private final List changeList;
        private boolean completed;

        private ProperyImportCtx(List changeList) {
            this.changeList = changeList;
        }

        /**
         * @see PropertyImportContext#getImportRoot()
         */
        public Item getImportRoot() {
            return node;
        }

        /**
         * @see PropertyImportContext#getChangeList()
         */
        public List getChangeList() {
            return Collections.unmodifiableList(changeList);
        }

        public IOListener getIOListener() {
            return ioListener;
        }

        public boolean hasStream() {
            return false;
        }

        /**
         * @see PropertyImportContext#informCompleted(boolean)
         */
        public void informCompleted(boolean success) {
            checkCompleted();
            completed = true;
        }

        /**
         * @see PropertyImportContext#isCompleted()
         */
        public boolean isCompleted() {
            return completed;
        }

        /**
         * @throws IllegalStateException if the context is already completed.
         * @see #isCompleted()
         * @see #informCompleted(boolean)
         */
        private void checkCompleted() {
            if (completed) {
                throw new IllegalStateException("PropertyImportContext has already been consumed.");
            }
        }
    }
}
