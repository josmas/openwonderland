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

package org.jdesktop.wonderland.modules;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import javax.xml.bind.JAXBException;

/**
 * The ModuleFactory class creates instances of modules, either if the already
 * exist on disk as directories or JAR archive files, or whether they are
 * being created in memory.
 * <p>
 * All methods on this class are static.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public final class ModuleFactory {

    /**
     * Opens an existing module given its URL. This factory method supports URLs
     * with the following protocols: 'file:' and 'jar:'. If a URL with a 'file:'
     * protocol is given, this method expects to find the root directory of the
     * module on the disk file system. If a URL with a 'jar:' protocol is given,
     * this method interprets the contents as a JAR file that contains a module.
     * The jar file may be additionally located on a disk file system or over
     * the network (see the Javadoc for the ArchiveModule class for more details
     * about the format of the URL in this case).
     *
     * @param url The URL of the WFS to open
     * @throw FileNotFoundException If the WFS does not exist
     * @throw IOException Upon some general I/O error reading the WFS
     * @throw JAXBException Upon error reading XML
     * @throw InvalidWFSException If the WFS is not properly formatted 
     */
    public static final Module open(URL url) throws FileNotFoundException, IOException, JAXBException {        
        String protocol = url.getProtocol();
        
        /* If the URL points to a disk directory */
        if (protocol.equals(Module.FILE_PROTOCOL) == true) {
            //return new FileWFS(url.getPath(), false);
            return null;
        }
        else if (protocol.equals(Module.JAR_PROTOCOL) == true) {
            //return new ArchiveWFS(url);
            return null;
        }
        else {
            throw new IOException("Invalid Protocol for Module Given: " + url.toString());
        }
    }
}
