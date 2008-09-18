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

/**
 * The ModuleArtResource represents a specific kind of resource within a module
 * that include artwork of all types (models, images, textures, etc).
 * <p>
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class ModuleArtResource extends ModuleResource {

    /** Constructor, takes the name of the resource */
    public ModuleArtResource(String pathName) {
        super(pathName);
    }
}
