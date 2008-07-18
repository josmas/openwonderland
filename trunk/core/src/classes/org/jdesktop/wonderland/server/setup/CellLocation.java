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
package org.jdesktop.wonderland.server.setup;

/**
 *
 * @author jkaplan
 */
public interface CellLocation {
    public enum BoundsType { BOX, SPHERE };
    
    public BoundsType getBoundsType();
    public void setBoundsType(BoundsType boundsType);
   
    public double getBoundsRadius();
    public void setBoundsRadius(double boundsRadius);
    
    public double[] getOrigin();
    public void setOrigin(double[] origin);
    
    public double[] getRotation();
    public void setRotation(double[] rotation);
    
    public double getScale();
    public void setScale(double scale);
}
