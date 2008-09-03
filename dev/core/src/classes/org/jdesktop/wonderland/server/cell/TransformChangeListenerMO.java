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
package org.jdesktop.wonderland.server.cell;

import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 * Listener for tracking cell transform changes
 * 
 * @author paulby
 */
public interface TransformChangeListenerMO extends ManagedObject, Serializable {

    /**
     * Called when the cells transform has changed.
     * 
     * @param cell
     */
    public void transformChanged(ManagedReference<CellMO> cellRef, CellTransform localTransform, CellTransform localToWorldTransform);
}
