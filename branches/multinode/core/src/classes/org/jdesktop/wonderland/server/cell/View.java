/**
 * Project Wonderland
 *
 * $Id$
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
 */
package org.jdesktop.wonderland.server.cell;

import org.jdesktop.wonderland.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.server.UserMO;

/**
 * A view into the virtual world. This could be from an avatar, camera
 * etc.
 * 
 * @author paulby
 */
@ExperimentalAPI
public interface View {

    /**
     * Return the transform of the camera for this view
     * @return
     */
    public CellTransform getTransform();
    
    /**
     * Get the user who owns this view
     * @return
     */
    public UserMO getUser();
}
