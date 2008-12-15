/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.webdav.common;

import java.io.File;
import org.jdesktop.wonderland.modules.contentrepo.common.ContentNode;

/**
 * Webdav implementation of ContentResource
 * @author jkaplan
 */
public class FileContentNode implements ContentNode {
    private File file;
    private FileContentCollection parent;

    FileContentNode(File file, FileContentCollection parent) {
        this.file = file;
        this.parent = parent;
    }

    public String getName() {
        return getFile().getName();
    }

    public String getPath() {
        if (getParent() == null) {
            return "/" + getName();
        } else {
            return getParent().getPath() + "/" + getName();
        }
    }

    public boolean canWrite() {
        return true;
    }

    public FileContentCollection getParent() {
        return parent;
    }

    protected File getFile() {
        return file;
    }
}
