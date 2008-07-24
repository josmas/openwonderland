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

import com.jme.bounding.BoundingVolume;
import org.jdesktop.wonderland.common.cell.setup.CellSetup;
import org.jdesktop.wonderland.common.cell.CellTransform;

/**
 *
 * @author jkaplan
 */
public class BasicCellMOSetup<T extends CellSetup>
    implements CellMOSetup, CellLocation 
{
    /** the setup object to send to the cell */
    private T cellSetup;

    /** the class of cell MO */
    private String cellMOClassName;

    /* the bounds of the cell */
    private BoundingVolume bounds = null;
    
    private short priority;
    private CellTransform transform;
    
    /** Default constructor */
    public BasicCellMOSetup() {
    }
    
    public T getCellSetup() {
        return cellSetup;
    }

    public void setCellSetup(T cellSetup) {
        this.cellSetup = cellSetup;
    }

    public String getCellMOClassName() {
        return cellMOClassName;
    }

    public void setCellMOClassName(String cellMOClassName) {
        this.cellMOClassName = cellMOClassName;
    }
    
    public void setLocalTransform(CellTransform transform) {
        this.transform = transform;
        
    }
    public CellTransform getLocalTransform() {
        return transform;
    }

    public BoundingVolume getLocalBounds() {
        return bounds;
    }

    public void setBounds(BoundingVolume bounds) {
        this.bounds = bounds;
    }
    
    public void validate() throws InvalidCellMOSetupException {
        // do nothing
    }
    
    public short getPriority() {
        return priority;
    }

    public void setPriority(short priority) {
        this.priority = priority;
    }
    
}
