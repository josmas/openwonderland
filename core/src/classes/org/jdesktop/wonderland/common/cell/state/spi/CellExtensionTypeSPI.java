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

package org.jdesktop.wonderland.common.cell.state.spi;

import org.jdesktop.wonderland.common.cell.state.CellServerState;

/**
 * The CellExtensionTypeSPI service provider interface is for classes that
 * register themselves with file extension types. Implementations of this
 * interface return an array of string file extension names they support (e.g.
 * "jpg", "dae", etc) and create a cell setup object given the extension and
 * the asset URI.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public interface CellExtensionTypeSPI {
    /**
     * Returns an array of extension types supported by the class
     */
    public String[] getSupportedExtensions();
    
    /**
     * Returns an instance of a setup class given the file extension and an
     * opaque URI of the media
     */
    public CellServerState getCellSetup(String extension, String uri);
}