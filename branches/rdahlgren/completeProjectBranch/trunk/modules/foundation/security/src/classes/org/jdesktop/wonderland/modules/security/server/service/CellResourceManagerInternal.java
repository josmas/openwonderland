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
package org.jdesktop.wonderland.modules.security.server.service;

import java.util.Set;
import java.util.SortedSet;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.modules.security.common.Permission;
import org.jdesktop.wonderland.modules.security.common.Principal;
import org.jdesktop.wonderland.server.cell.CellResourceManager;

/**
 * Interal operations for the CellResourceManager used by the security
 * component.
 * @author jkaplan
 */
public interface CellResourceManagerInternal extends CellResourceManager {
     /**
     * Update a particular resource in the cache.  If there is no entry
     * for this cell in the cache, it will be added.
     * @param cellID the id of the cell to update
     * @param owners the updated owner set
     * @param permissions the update permission set
     */
    public void updateCellResource(CellID cellID, Set<Principal> owners,
                                   SortedSet<Permission> permissions);

    /**
     * Remove a particular cell from the cache.  It will be reloaded next
     * time a security check is requested.
     * @param cellID the cell id to update
     */
    public void invalidateCellResource(CellID cellID);
}
