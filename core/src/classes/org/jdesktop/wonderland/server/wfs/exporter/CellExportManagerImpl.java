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
package org.jdesktop.wonderland.server.wfs.exporter;

import java.util.Set;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.wfs.WorldRoot;

/**
 * Implementation of CellExportManager.  This just forwards everything to
 * the service.
 * @author jkaplan
 */
public class CellExportManagerImpl implements CellExportManager {
    private CellExportService service;

    public CellExportManagerImpl(CellExportService service) {
        this.service = service;
    }

    public void createSnapshot(SnapshotCreationListener listener) {
        service.createSnapshot(listener);
    }

    public void exportCells(WorldRoot worldRoot, Set<CellID> cellIDs,
                            CellExportListener listener)
    {
        service.exportCells(worldRoot, cellIDs, listener);
    }
}
