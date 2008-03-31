/**
 * Project Looking Glass
 *
 * $Id$
 *
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
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

package org.jdesktop.wonderland.server.setup;

import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author jkaplan
 */
public interface CellLocation {
    public enum BoundsType { BOX, SPHERE };
    
    public BoundsType getBoundsType();
    public void setBoundsType(BoundsType boundsType);
   
    public float getBoundsRadius();
    public void setBoundsRadius(float boundsRadius);
    
    public CellTransform getCellTransform();
    public void setCellTransform(CellTransform transform);
}
