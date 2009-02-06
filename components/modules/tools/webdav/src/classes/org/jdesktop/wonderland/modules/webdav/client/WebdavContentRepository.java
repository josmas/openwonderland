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
package org.jdesktop.wonderland.modules.webdav.client;

import org.jdesktop.wonderland.modules.webdav.common.WebdavContentCollection;
import org.jdesktop.wonderland.modules.contentrepo.client.ContentRepository;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentRepositoryException;

/**
 *
 * @author jkaplan
 */
public class WebdavContentRepository implements ContentRepository {
    private WebdavContentCollection root;
    private WebdavContentCollection systemRoot;
    private WebdavContentCollection usersRoot;
    private WebdavContentCollection userRoot;
    private String userId;

    public WebdavContentRepository(WebdavContentCollection root,
                                   String userId)
    {
        this.root = root;
        this.userId = userId;
    }

    public WebdavContentCollection getRoot() {
        return root;
    }

    public WebdavContentCollection getSystemRoot()
            throws ContentRepositoryException
    {
        if (systemRoot != null) {
            return systemRoot;
        }

        systemRoot = (WebdavContentCollection) root.getChild("system");
        return systemRoot;
    }

    public WebdavContentCollection getUserRoot() throws ContentRepositoryException {
        return getUserRoot(true);
    }

    public WebdavContentCollection getUserRoot(boolean create)
            throws ContentRepositoryException
    {
        if (userRoot != null) {
            return userRoot;
        }
        
        userRoot = getUserRoot(userId, create);
        return userRoot;
    }

    public WebdavContentCollection getUserRoot(String username)
            throws ContentRepositoryException
    {
        return getUserRoot(username, false);
    }

    protected WebdavContentCollection getUserRoot(String username,
                                                  boolean create)
            throws ContentRepositoryException
    {
        if (usersRoot == null) {
            usersRoot = (WebdavContentCollection) root.getChild("users");
        }
        if (usersRoot == null) {
            return null;
        }

        WebdavContentCollection userDir =
                (WebdavContentCollection) usersRoot.getChild(username);
        if (userDir == null && create) {
            userDir = (WebdavContentCollection)
                    usersRoot.createChild(username, ContentNode.Type.COLLECTION);
        }

        return userDir;
    }
}
