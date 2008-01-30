/**
 * Project Looking Glass
 *
 * $RCSfile: CellLocation.java,v $
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
 * $Revision: 1.2 $
 * $Date: 2007/10/17 17:11:13 $
 * $State: Exp $
 */

package org.jdesktop.wonderland.server.setup;

import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author jkaplan
 */
public interface CellLocation {
    public String getBoundsType();
    public void setBoundsType(String boundsType);
   
    public float getBoundsRadius();
    public void setBoundsRadius(float boundsRadius);
    
//    public float[] getOrigin();
//    public void setOrigin(float[] origin);
//    
//    public float[] getRotation();
//    public void setRotation(float[] rotation);
//    
//    public float getScale();
//    public void setScale(float scale);
    public CellTransform getCellTransform();
    public void setCellTransform(CellTransform transform);
}
