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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.WebdavRequest;

import org.apache.jackrabbit.webdav.bind.BindInfo;
import org.apache.jackrabbit.webdav.bind.RebindInfo;
import org.apache.jackrabbit.webdav.bind.UnbindInfo;
import org.apache.jackrabbit.webdav.header.CodedUrlHeader;
import org.apache.jackrabbit.webdav.header.DepthHeader;
import org.apache.jackrabbit.webdav.header.IfHeader;
import org.apache.jackrabbit.webdav.header.LabelHeader;
import org.apache.jackrabbit.webdav.header.OverwriteHeader;
import org.apache.jackrabbit.webdav.header.PollTimeoutHeader;
import org.apache.jackrabbit.webdav.header.TimeoutHeader;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.observation.ObservationConstants;
import org.apache.jackrabbit.webdav.observation.SubscriptionInfo;
import org.apache.jackrabbit.webdav.ordering.OrderPatch;
import org.apache.jackrabbit.webdav.ordering.OrderingConstants;
import org.apache.jackrabbit.webdav.ordering.Position;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.DefaultDavProperty;
import org.apache.jackrabbit.webdav.transaction.TransactionConstants;
import org.apache.jackrabbit.webdav.transaction.TransactionInfo;
import org.apache.jackrabbit.webdav.version.LabelInfo;
import org.apache.jackrabbit.webdav.version.MergeInfo;
import org.apache.jackrabbit.webdav.version.OptionsInfo;
import org.apache.jackrabbit.webdav.version.UpdateInfo;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <code>WebdavRequestImpl</code>...
 */
public class CARS_DavRequestImpl implements WebdavRequest, DavConstants {

    private static Logger log = LoggerFactory.getLogger(CARS_DavRequestImpl.class);

    private final HttpServletRequest httpRequest;
    private final DavLocatorFactory factory;
    private final IfHeader ifHeader;
    private final String hrefPrefix;

    private DavSession session;

    private int propfindType = PROPFIND_ALL_PROP;
    private DavPropertyNameSet propfindProps;
    private DavPropertySet proppatchSet;
    private DavPropertyNameSet proppatchRemove;
    private List proppatchList;

    final private transient String mContextPath;

    private final static DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();

    /**
     * Creates a new <code>DavServletRequest</code> with the given parameters.
     *
     * @param httpRequest
     * @param factory
     */
//    public CARS_DavRequestImpl(HttpServletRequest httpRequest, DavLocatorFactory factory) {
    public CARS_DavRequestImpl( final HttpServletRequest httpRequest, final DavLocatorFactory factory, final String pContextPath ) {
      this.mContextPath = pContextPath;
      this.httpRequest = httpRequest;
      this.factory = factory;
      this.ifHeader = new IfHeader(httpRequest);

      String host = getHeader("Host");
      String scheme = getScheme();
      hrefPrefix = scheme + "://" + host + getContextPath();
      return;
    }

    /**
     * Sets the session field and adds all lock tokens present with either the
     * Lock-Token header or the If header to the given session object.
     *
     * @param session
     * @see DavServletRequest#setDavSession(DavSession)
     */
    public void setDavSession(DavSession session) {
        this.session = session;
        // set lock-tokens from header to the current session
        if (session != null) {
            String lt = getLockToken();
            if (lt != null) {
                session.addLockToken(lt);
            }
            // add all token present in the the If header to the session as well.
            Iterator it = ifHeader.getAllTokens();
            while (it.hasNext()) {
                String ifHeaderToken = (String) it.next();
                session.addLockToken(ifHeaderToken);
            }
        }
    }

    /**
     * @see DavServletRequest#getDavSession()
     */
    public DavSession getDavSession() {
        return session;
    }

    /**
     * Return a <code>DavResourceLocator</code> representing the request handle.
     *
     * @return locator of the requested resource
     * @see DavServletRequest#getRequestLocator()
     */
    public DavResourceLocator getRequestLocator() {
        String path = getRequestURI();
        String ctx = getContextPath();
        if (path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        return factory.createResourceLocator(hrefPrefix, path);
    }

    /**
     * Parse the destination header field and return the path of the destination
     * resource.
     *
     * @return path of the destination resource.
     * @throws DavException
     * @see #HEADER_DESTINATION
     * @see DavServletRequest#getDestinationLocator
     */
    public DavResourceLocator getDestinationLocator() throws DavException {
        return getHrefLocator(httpRequest.getHeader(HEADER_DESTINATION));
    }

    /**
     * Parse a href and return the path of the resource.
     *
     * @return path of the resource identified by the href.
     * @see org.apache.jackrabbit.webdav.bind.BindServletRequest#getHrefLocator
     */
    public DavResourceLocator getHrefLocator(String href) throws DavException {
        String ref = href;
        if (ref != null) {
            //href should be a Simple-ref production as defined in RFC4918, so it is either an absolute URI
            //or an absolute path
            try {
                URI uri = new URI(ref);
                String auth = uri.getAuthority();
                ref = uri.getRawPath();
                if (auth == null) {
                    //verify that href is an absolute path
                    if (ref.startsWith("//") || !ref.startsWith("/")) {
                        log.warn("expected absolute path but found " + ref);
                        throw new DavException(DavServletResponse.SC_BAD_REQUEST);
                    }
                } else if (!auth.equals(httpRequest.getHeader("Host"))) {
                    //this looks like an unsupported cross-server operation, but of course a reverse-proxy
                    //might have rewritten the Host header. Since we can't find out, we have to reject anyway.
                    //Better use absolute paths in DAV:href elements!
                    throw new DavException(DavServletResponse.SC_FORBIDDEN);
                }
            } catch (URISyntaxException e) {
                log.warn("malformed uri: " + href, e);
                throw new DavException(DavServletResponse.SC_BAD_REQUEST);
            }
            // cut off the context path
            final String contextPath = getContextPath() + "/webdav";
            if (ref.startsWith(contextPath)) {
                ref = ref.substring(contextPath.length());
            } else {
                //absolute path has to start with contextpath
                throw new DavException(DavServletResponse.SC_FORBIDDEN);
            }
        }
        return factory.createResourceLocator(hrefPrefix, ref);
    }

    /**
     * Returns the path of the member resource of the request resource which is identified by the segment parameter.
     *
     * @return path of internal member resource.
     */
    public DavResourceLocator getMemberLocator(String segment) {
        String path = (this.getRequestLocator().getHref(true) + segment).substring(hrefPrefix.length());
        return factory.createResourceLocator(hrefPrefix, path);
    }

    /**
     * Return true if the overwrite header does not inhibit overwriting.
     *
     * @return true if the overwrite header requests 'overwriting'
     * @see #HEADER_OVERWRITE
     * @see DavServletRequest#isOverwrite()
     */
    @Override
    public boolean isOverwrite() {
      return new OverwriteHeader(httpRequest).isOverwrite();
    }

    /**
     * @see DavServletRequest#getDepth(int)
     */
    @Override
    public int getDepth( final int pDefaultValue) {
      return DepthHeader.parse(httpRequest, pDefaultValue).getDepth();
    }

    /**
     * @see DavServletRequest#getDepth()
     */
    public int getDepth() {
        return getDepth(DEPTH_INFINITY);
    }

    /**
     * Parse the Timeout header and return a long representing the value.
     * {@link #UNDEFINED_TIMEOUT} is used as default value if no header
     * is available or if the parsing fails.
     *
     * @return milliseconds indicating length of the timeout.
     * @see DavServletRequest#getTimeout()
     * @see TimeoutHeader#parse(javax.servlet.http.HttpServletRequest, long)
     */
    public long getTimeout() {
        return TimeoutHeader.parse(httpRequest, UNDEFINED_TIMEOUT).getTimeout();
    }

    /**
     * Retrive the lock token from the 'Lock-Token' header.
     *
     * @return String representing the lock token sent in the Lock-Token header.
     * @throws IllegalArgumentException If the value has not the correct format.
     * @see #HEADER_LOCK_TOKEN
     * @see DavServletRequest#getLockToken()
     */
    public String getLockToken() {
        return CodedUrlHeader.parse(httpRequest, HEADER_LOCK_TOKEN).getCodedUrl();
    }

    /**
     * @see DavServletRequest#getRequestDocument()
     */
    public Document getRequestDocument() throws DavException {
        Document requestDocument = null;
        /*
        Don't attempt to parse the body if the contentlength header is 0.
        NOTE: a value of -1 indicates that the length is unknown, thus we have
        to parse the body. Note that http1.1 request using chunked transfer
        coding will therefore not be detected here.
        */
        if (httpRequest.getContentLength() == 0) {
            return requestDocument;
        }
        // try to parse the request body
        try {
            InputStream in = httpRequest.getInputStream();
            if (in != null) {
                // use a buffered input stream to find out whether there actually
                // is a request body
                InputStream bin = new BufferedInputStream(in);
                bin.mark(1);
                boolean isEmpty = -1 == bin.read();
                bin.reset();
                if (!isEmpty) {
                    DocumentBuilder docBuilder = FACTORY.newDocumentBuilder();
                    requestDocument = docBuilder.parse(bin);
                }
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to build an XML Document from the request body: " + e.getMessage());
            }
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        } catch (ParserConfigurationException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to build an XML Document from the request body: " + e.getMessage());
            }
            throw new DavException(DavServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (SAXException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to build an XML Document from the request body: " + e.getMessage());
            }
            throw new DavException(DavServletResponse.SC_BAD_REQUEST);
        }
        return requestDocument;
    }

    /**
     * Returns the type of PROPFIND as indicated by the request body.
     *
     * @return type of the PROPFIND request. Default value is {@link #PROPFIND_ALL_PROP allprops}
     * @see DavServletRequest#getPropFindType()
     */
    public int getPropFindType() throws DavException {
        if (propfindProps == null) {
            parsePropFindRequest();
        }
        return propfindType;
    }

    /**
     * Returns the set of properties requested by the PROPFIND body or an
     * empty set if the {@link #getPropFindType type} is either 'allprop' or
     * 'propname'.
     *
     * @return set of properties requested by the PROPFIND body or an empty set.
     * @see DavServletRequest#getPropFindProperties()
     */
    public DavPropertyNameSet getPropFindProperties() throws DavException {
        if (propfindProps == null) {
            parsePropFindRequest();
        }
        return propfindProps;
    }

    /**
     * Parse the propfind request body in order to determine the type of the propfind
     * and the set of requested property.
     * NOTE: An empty 'propfind' request body will be treated as request for all
     * property according to the specification.
     */
    private void parsePropFindRequest() throws DavException {
        propfindProps = new DavPropertyNameSet();
        Document requestDocument = getRequestDocument();
        // propfind httpRequest with empty body >> retrieve all property
        if (requestDocument == null) {
            return;
        }

        // propfind httpRequest with invalid body
        Element root = requestDocument.getDocumentElement();
//        if (!XML_PROPFIND.equals(root.getNodeName().substring(2))) {
        if ((!XML_PROPFIND.equals(root.getNodeName().substring(2))) &&
            (!XML_PROPFIND.equals(root.getNodeName()))) {
            log.info("PropFind-Request has no <propfind> tag.");
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "PropFind-Request has no <propfind> tag.");
        }

        DavPropertyNameSet include = null;

        ElementIterator it = DomUtil.getChildren(root);
        int propfindTypeFound = 0;

        while (it.hasNext()) {
            Element child = it.nextElement();
            String nodeName = child.getLocalName();
            if (NAMESPACE.getURI().equals(child.getNamespaceURI())) {
                if (XML_PROP.equals(nodeName)) {
                    propfindType = PROPFIND_BY_PROPERTY;
                    propfindProps = new DavPropertyNameSet(child);
                    propfindTypeFound += 1;
                }
                else if (XML_PROPNAME.equals(nodeName)) {
                    propfindType = PROPFIND_PROPERTY_NAMES;
                    propfindTypeFound += 1;
                }
                else if (XML_ALLPROP.equals(nodeName)) {
                    propfindType = PROPFIND_ALL_PROP;
                    propfindTypeFound += 1;
                }
                else if (XML_INCLUDE.equals(nodeName)) {
                    include = new DavPropertyNameSet();
                    ElementIterator pit = DomUtil.getChildren(child);
                    while (pit.hasNext()) {
                        include.add(DavPropertyName.createFromXml(pit.nextElement()));
                    }
                }
            }
        }

        if (propfindTypeFound > 1) {
            log.info("Multiple top-level propfind instructions");
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "Multiple top-level propfind instructions");
        }

        if (include != null) {
            if (propfindType == PROPFIND_ALL_PROP) {
                // special case: allprop with include extension
                propfindType = PROPFIND_ALL_PROP_INCLUDE;
                propfindProps = include;
            }
            else {
                throw new DavException(DavServletResponse.SC_BAD_REQUEST, "<include> goes only with <allprop>");

            }
        }
    }

     /**
      * Return a {@link List} of property change operations. Each entry
      * is either of type {@link DavPropertyName}, indicating a &lt;remove&gt;
      * operation, or of type {@link DavProperty}, indicating a &lt;set&gt;
      * operation. Note that ordering is significant here.
      *
      * @return the list of change operations entries in the PROPPATCH request body
      * @see DavServletRequest#getPropPatchChangeList()
      */
     public List getPropPatchChangeList() throws DavException {
         if (proppatchList == null) {
             parsePropPatchRequest();
         }
         return proppatchList;
     }

    /**
     * Parse the PROPPATCH request body.
     */
    private void parsePropPatchRequest() throws DavException {

        proppatchSet = new DavPropertySet();
        proppatchRemove = new DavPropertyNameSet();
        proppatchList = new ArrayList();

        Document requestDocument = getRequestDocument();

        if (requestDocument == null) {
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "Invalid request body.");
        }

        Element root = requestDocument.getDocumentElement();
        if (!DomUtil.matches(root, XML_PROPERTYUPDATE, NAMESPACE)) {
            log.warn("PropPatch-Request has no <DAV:propertyupdate> tag.");
            throw new DavException(DavServletResponse.SC_BAD_REQUEST, "PropPatch-Request has no <propertyupdate> tag.");
        }

        ElementIterator it = DomUtil.getChildren(root);
        while (it.hasNext()) {
            Element el = it.nextElement();
            if (DomUtil.matches(el, XML_SET, NAMESPACE)) {
                Element propEl = DomUtil.getChildElement(el, XML_PROP, NAMESPACE);
                if (propEl != null) {
                    ElementIterator properties = DomUtil.getChildren(propEl);
                    while (properties.hasNext()) {
                        DavProperty davProp = DefaultDavProperty.createFromXml(properties.nextElement());
                        proppatchSet.add(davProp);
                        proppatchList.add(davProp);
                    }
                }
            } else if (DomUtil.matches(el, XML_REMOVE, NAMESPACE)) {
                Element propEl = DomUtil.getChildElement(el, XML_PROP, NAMESPACE);
                if (propEl != null) {
                    ElementIterator properties = DomUtil.getChildren(propEl);
                    while (properties.hasNext()) {
                        DavProperty davProp = DefaultDavProperty.createFromXml(properties.nextElement());
                        proppatchSet.add(davProp);
                        proppatchList.add(davProp.getName());
                    }
                }
            } else {
                log.debug("Unknown element in DAV:propertyupdate: " + el.getNodeName());
                // unknown child elements are ignored
            }
        }
    }

    /**
     * {@link LockInfo} object encapsulating the information passed with a LOCK
     * request if the LOCK request body was valid. If the request body is
     * missing a 'refresh lock' request is assumed. The {@link LockInfo}
     * then only provides timeout and isDeep property and returns true on
     * {@link org.apache.jackrabbit.webdav.lock.LockInfo#isRefreshLock()}
     *
     * @return lock info object or <code>null</code> if an error occured while
     *         parsing the request body.
     * @throws DavException throws a 400 (Bad Request) DavException if a request
     * body is present but does not start with a DAV:lockinfo element. Note however,
     * that a non-existing request body is a valid request used to refresh
     * an existing lock.
     * @see DavServletRequest#getLockInfo()
     */
    public LockInfo getLockInfo() throws DavException {
        LockInfo lockInfo;
        boolean isDeep = (getDepth(DEPTH_INFINITY) == DEPTH_INFINITY);
        Document requestDocument = getRequestDocument();
        // check if XML request body is present. It SHOULD have one for
        // 'create Lock' request and missing for a 'refresh Lock' request
        if (requestDocument != null) {
            Element root = requestDocument.getDocumentElement();
            if (root.getLocalName().equals(XML_LOCKINFO)) {
                lockInfo = new LockInfo(root, getTimeout(), isDeep);
            } else {
                log.debug("Lock request body must start with a DAV:lockinfo element.");
                throw new DavException(DavServletResponse.SC_BAD_REQUEST);
            }
        } else {
            lockInfo = new LockInfo(null, getTimeout(), isDeep);
        }
        return lockInfo;
    }

    /**
     * Test if the if header matches the given resource. The comparison is
     * made with the {@link DavResource#getHref()
     * resource href} and the token returned from an exclusive write lock present on
     * the resource.<br>
     * NOTE: If either the If header or the resource is <code>null</code> or if
     * the resource has not applied an exclusive write lock the preconditions are met.
     * If in contrast the lock applied to the given resource returns a
     * <code>null</code> lock token (e.g. for security reasons) or a lock token
     * that does not match, the method will return false.
     *
     * @param resource Webdav resources being operated on
     * @return true if the test is successful and the preconditions for the
     *         request processing are fulfilled.
     * @see DavServletRequest#matchesIfHeader(DavResource)
     * @see IfHeader#matches(String, String, String)
     * @see DavResource#hasLock(org.apache.jackrabbit.webdav.lock.Type, org.apache.jackrabbit.webdav.lock.Scope)
     * @see org.apache.jackrabbit.webdav.lock.ActiveLock#getToken()
     */
    public boolean matchesIfHeader(DavResource resource) {
        // no ifheader, no resource or no write lock on resource
        // >> preconditions ok so far
        if (!ifHeader.hasValue() || resource == null || !resource.hasLock(Type.WRITE, Scope.EXCLUSIVE)) {
            return true;
        }

        boolean isMatching = false;
        String lockToken = resource.getLock(Type.WRITE, Scope.EXCLUSIVE).getToken();
        if (lockToken != null) {
            isMatching = matchesIfHeader(resource.getHref(), lockToken, getStrongETag(resource));
        } // else: lockToken is null >> the if-header will not match.

        return isMatching;
    }

    /**
     * @see DavServletRequest#matchesIfHeader(String, String, String)
     * @see IfHeader#matches(String, String, String)
     */
    public boolean matchesIfHeader(String href, String token, String eTag) {
        return ifHeader.matches(href, token, isStrongETag(eTag) ?  eTag : "");
    }

    /**
     * Returns the strong etag present on the given resource or empty string
     * if either the resource does not provide any etag or if the etag is weak.
     *
     * @param resource
     * @return strong etag or empty string.
     */
    private String getStrongETag(DavResource resource) {
        DavProperty prop = resource.getProperty(DavPropertyName.GETETAG);
        if (prop != null && prop.getValue() != null) {
            String etag = prop.getValue().toString();
            if (isStrongETag(etag)) {
                return etag;
            }
        }
        // no strong etag available
        return "";
    }

    /**
     * Returns true if the given string represents a strong etag.
     *
     * @param eTag
     * @return true, if its a strong etag
     */
    private static boolean isStrongETag(String eTag) {
        return eTag != null && eTag.length() > 0 && !eTag.startsWith("W\\");
    }

    //-----------------------------< TransactionDavServletRequest Interface >---
    /**
     * @see org.apache.jackrabbit.webdav.transaction.TransactionDavServletRequest#getTransactionId()
     */
    public String getTransactionId() {
        return CodedUrlHeader.parse(httpRequest, TransactionConstants.HEADER_TRANSACTIONID).getCodedUrl();
    }

    /**
     * @see org.apache.jackrabbit.webdav.transaction.TransactionDavServletRequest#getTransactionInfo()
     */
    public TransactionInfo getTransactionInfo() throws DavException {
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            return new TransactionInfo(requestDocument.getDocumentElement());
        }
        return null;
    }

    //-----------------------------< ObservationDavServletRequest Interface >---
    /**
     * @see org.apache.jackrabbit.webdav.observation.ObservationDavServletRequest#getSubscriptionId()
     */
    public String getSubscriptionId() {
        return CodedUrlHeader.parse(httpRequest, ObservationConstants.HEADER_SUBSCRIPTIONID).getCodedUrl();
    }

    /**
     * @see org.apache.jackrabbit.webdav.observation.ObservationDavServletRequest#getPollTimeout()
     */
    public long getPollTimeout() {
        return PollTimeoutHeader.parseHeader(httpRequest, 0).getTimeout();
    }

    /**
     * @see org.apache.jackrabbit.webdav.observation.ObservationDavServletRequest#getSubscriptionInfo()
     */
    public SubscriptionInfo getSubscriptionInfo() throws DavException {
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            Element root = requestDocument.getDocumentElement();
            if (ObservationConstants.XML_SUBSCRIPTIONINFO.equals(root.getLocalName())) {
                int depth = getDepth(DEPTH_0);
                return new SubscriptionInfo(root, getTimeout(), depth == DEPTH_INFINITY);
            }
        }
        return null;
    }

    //--------------------------------< OrderingDavServletRequest Interface >---
    /**
     * @see org.apache.jackrabbit.webdav.ordering.OrderingDavServletRequest#getOrderingType()
     */
    public String getOrderingType() {
        return getHeader(OrderingConstants.HEADER_ORDERING_TYPE);
    }

    /**
     * @see org.apache.jackrabbit.webdav.ordering.OrderingDavServletRequest#getPosition()
     */
    public Position getPosition() {
        String h = getHeader(OrderingConstants.HEADER_POSITION);
        Position pos = null;
        if (h != null) {
            String[] typeNSegment = h.split("\\s");
            if (typeNSegment.length == 2) {
                try {
                    pos = new Position(typeNSegment[0], typeNSegment[1]);
                } catch (IllegalArgumentException e) {
                    log.error("Cannot parse Position header: " + e.getMessage());
                }
            }
        }
        return pos;
    }

    /**
     * @return <code>OrderPatch</code> object representing the orderpatch request
     *         body or <code>null</code> if the
     * @see org.apache.jackrabbit.webdav.ordering.OrderingDavServletRequest#getOrderPatch()
     */
    public OrderPatch getOrderPatch() throws DavException {
        OrderPatch op = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            Element root = requestDocument.getDocumentElement();
            op = OrderPatch.createFromXml(root);
        } else {
            log.error("Error while building xml document from ORDERPATH request body.");
        }
        return op;
    }

    //-------------------------------------< DeltaVServletRequest interface >---
    /**
     * @see org.apache.jackrabbit.webdav.version.DeltaVServletRequest#getLabel()
     */
    public String getLabel() {
        LabelHeader label = LabelHeader.parse(this);
        if (label != null) {
            return label.getLabel();
        }
        return null;
    }

    /**
     * @see org.apache.jackrabbit.webdav.version.DeltaVServletRequest#getLabelInfo()
     */
    public LabelInfo getLabelInfo() throws DavException {
        LabelInfo lInfo = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            Element root = requestDocument.getDocumentElement();
            int depth = getDepth(DEPTH_0);
            lInfo = new LabelInfo(root, depth);
        }
        return lInfo;
    }

    /**
     * @see org.apache.jackrabbit.webdav.version.DeltaVServletRequest#getMergeInfo()
     */
    public MergeInfo getMergeInfo()  throws DavException {
        MergeInfo mInfo = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            mInfo = new MergeInfo(requestDocument.getDocumentElement());
        }
        return mInfo;
    }

    /**
     * @see org.apache.jackrabbit.webdav.version.DeltaVServletRequest#getUpdateInfo()
     */
    public UpdateInfo getUpdateInfo() throws DavException  {
        UpdateInfo uInfo = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            uInfo = new UpdateInfo(requestDocument.getDocumentElement());
        }
        return uInfo;
    }

    /**
     * @see org.apache.jackrabbit.webdav.version.DeltaVServletRequest#getReportInfo()
     */
    public ReportInfo getReportInfo() throws DavException  {
        ReportInfo rInfo = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            rInfo = new ReportInfo(requestDocument.getDocumentElement(), getDepth(DEPTH_0));
        }
        return rInfo;
    }

    /**
     * @see org.apache.jackrabbit.webdav.version.DeltaVServletRequest#getOptionsInfo()
     */
    public OptionsInfo getOptionsInfo() throws DavException {
        OptionsInfo info = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            info = OptionsInfo.createFromXml(requestDocument.getDocumentElement());
        }
        return info;
    }

    /**
     * @see org.apache.jackrabbit.webdav.bind.BindServletRequest#getRebindInfo()
     */
    public RebindInfo getRebindInfo() throws DavException {
        RebindInfo info = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            info = RebindInfo.createFromXml(requestDocument.getDocumentElement());
        }
        return info;
    }

    /**
     * @see org.apache.jackrabbit.webdav.bind.BindServletRequest#getUnbindInfo()
     */
    public UnbindInfo getUnbindInfo() throws DavException {
        UnbindInfo info = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            info = UnbindInfo.createFromXml(requestDocument.getDocumentElement());
        }
        return info;
    }

    /**
     * @see org.apache.jackrabbit.webdav.bind.BindServletRequest#getBindInfo()
     */
    public BindInfo getBindInfo() throws DavException {
        BindInfo info = null;
        Document requestDocument = getRequestDocument();
        if (requestDocument != null) {
            info = BindInfo.createFromXml(requestDocument.getDocumentElement());
        }
        return info;
    }

    //---------------------------------------< HttpServletRequest interface >---
    public String getAuthType() {
        return httpRequest.getAuthType();
    }

    public Cookie[] getCookies() {
        return httpRequest.getCookies();
    }

    public long getDateHeader(String s) {
        return httpRequest.getDateHeader(s);
    }

    public String getHeader(String s) {
        return httpRequest.getHeader(s);
    }

    public Enumeration getHeaders(String s) {
        return httpRequest.getHeaders(s);
    }

    public Enumeration getHeaderNames() {
        return httpRequest.getHeaderNames();
    }

    public int getIntHeader(String s) {
        return httpRequest.getIntHeader(s);
    }

    public String getMethod() {
        return httpRequest.getMethod();
    }

    public String getPathInfo() {
        return httpRequest.getPathInfo();
    }

    public String getPathTranslated() {
        return httpRequest.getPathTranslated();
    }

    public String getContextPath() {
      if (mContextPath==null) {
        return httpRequest.getContextPath();
      }
      return mContextPath;
    }

    public String getQueryString() {
        return httpRequest.getQueryString();
    }

    public String getRemoteUser() {
        return httpRequest.getRemoteUser();
    }

    public boolean isUserInRole(String s) {
        return httpRequest.isUserInRole(s);
    }

    public Principal getUserPrincipal() {
        return httpRequest.getUserPrincipal();
    }

    public String getRequestedSessionId() {
        return httpRequest.getRequestedSessionId();
    }

    public String getRequestURI() {
        return httpRequest.getRequestURI();
    }

    public StringBuffer getRequestURL() {
        return httpRequest.getRequestURL();
    }

    public String getServletPath() {
        return httpRequest.getServletPath();
    }

    public HttpSession getSession(boolean b) {
        return httpRequest.getSession(b);
    }

    public HttpSession getSession() {
        return httpRequest.getSession();
    }

    public boolean isRequestedSessionIdValid() {
        return httpRequest.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return httpRequest.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return httpRequest.isRequestedSessionIdFromURL();
    }

    public boolean isRequestedSessionIdFromUrl() {
        return httpRequest.isRequestedSessionIdFromUrl();
    }

    public Object getAttribute(String s) {
        return httpRequest.getAttribute(s);
    }

    public Enumeration getAttributeNames() {
        return httpRequest.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return httpRequest.getCharacterEncoding();
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        httpRequest.setCharacterEncoding(s);
    }

    public int getContentLength() {
        return httpRequest.getContentLength();
    }

    public String getContentType() {
        return httpRequest.getContentType();
    }

    public ServletInputStream getInputStream() throws IOException {
        return httpRequest.getInputStream();
    }

    public String getParameter(String s) {
        return httpRequest.getParameter(s);
    }

    public Enumeration getParameterNames() {
        return httpRequest.getParameterNames();
    }

    public String[] getParameterValues(String s) {
        return httpRequest.getParameterValues(s);
    }

    public Map getParameterMap() {
        return httpRequest.getParameterMap();
    }

    public String getProtocol() {
        return httpRequest.getProtocol();
    }

    public String getScheme() {
        return httpRequest.getScheme();
    }

    public String getServerName() {
        return httpRequest.getServerName();
    }

    public int getServerPort() {
        return httpRequest.getServerPort();
    }

    public BufferedReader getReader() throws IOException {
        return httpRequest.getReader();
    }

    public String getRemoteAddr() {
        return httpRequest.getRemoteAddr();
    }

    public String getRemoteHost() {
        return httpRequest.getRemoteHost();
    }

    public void setAttribute(String s, Object o) {
        httpRequest.setAttribute(s, o);
    }

    public void removeAttribute(String s) {
        httpRequest.removeAttribute(s);
    }

    public Locale getLocale() {
        return httpRequest.getLocale();
    }

    public Enumeration getLocales() {
        return httpRequest.getLocales();
    }

    public boolean isSecure() {
        return httpRequest.isSecure();
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return httpRequest.getRequestDispatcher(s);
    }

    public String getRealPath(String s) {
        return httpRequest.getRealPath(s);
    }

    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
