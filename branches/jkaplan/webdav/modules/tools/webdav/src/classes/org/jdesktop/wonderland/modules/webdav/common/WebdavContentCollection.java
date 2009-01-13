/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.webdav.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.URIException;
import org.apache.webdav.lib.WebdavResource;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode.Type;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;

/**
 * Webdav implementation of ContentResource
 * @author jkaplan
 */
public class WebdavContentCollection extends WebdavContentNode
        implements ContentCollection
{
    public WebdavContentCollection(WebdavResource resource, WebdavContentCollection parent) {
        super (resource, parent);
    }

    public List<ContentNode> getChildren() throws ContentRepositoryException {
        List<ContentNode> out = new ArrayList<ContentNode>();
        try {
            WebdavResource[] children = getResource().listWebdavResources();
            for (WebdavResource child : children) {
                out.add(getContentNode(child));
            }

            return out;
        } catch (IOException ioe) {
            throw new ContentRepositoryException(ioe);
        }
    }

    public WebdavContentNode getChild(String path) throws ContentRepositoryException {
        try {
            HttpURL url = getChildURL(getResource().getHttpURL(), path);
            WebdavResource resource = new WebdavResource(url);
            if (resource.getExistence()) {
                return getContentNode(resource);
            }

            return null;
        } catch (IOException ioe) {
            throw new ContentRepositoryException(ioe);
        }
    }

    public WebdavContentNode createChild(String name, Type type)
            throws ContentRepositoryException
    {
        try {
            HttpURL newURL = getChildURL(getResource().getHttpURL(), name);
            WebdavResource newResource = new WebdavResource(newURL);
            if (newResource.exists()) {
                throw new ContentRepositoryException("Path " + newURL +
                                                     " already exists.");
            }
            switch (type) {
                case COLLECTION:
                    newResource.mkcolMethod();
                    break;
                case RESOURCE:
                    break;
            }

            return getContentNode(newResource);
        } catch (IOException ioe) {
            throw new ContentRepositoryException(ioe);
        }
    }

    public WebdavContentNode removeChild(String name)
            throws ContentRepositoryException
    {
        try {
            HttpURL removeURL = getChildURL(getResource().getHttpURL(), name);
            WebdavResource removeResource = new WebdavResource(removeURL);
            if (removeResource.exists()) {
                removeResource.deleteMethod();
            }
            
            return getContentNode(removeResource);
        } catch (IOException ioe) {
            throw new ContentRepositoryException(ioe);
        }
    }

    protected WebdavContentNode getContentNode(WebdavResource resource) {
        if (resource.isCollection()) {
            return new WebdavContentCollection(resource, this);
        } else {
            return new WebdavContentResource(resource, this);
        }
    }

    protected HttpURL getChildURL(HttpURL parent, String childPath)
        throws URIException
    {
        if (childPath.startsWith("/")) {
            childPath = childPath.substring(1);
        }

        if (!parent.getPath().endsWith("/")) {
            parent.setPath(parent.getPath() + "/");
        }

        return new HttpURL(parent, childPath);
    }
}
