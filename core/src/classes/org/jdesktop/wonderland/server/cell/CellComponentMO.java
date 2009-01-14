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
package org.jdesktop.wonderland.server.cell;

import java.io.Serializable;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.AppContext;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellComponentServerState;

/**
 *
 * @author paulby
 */
public abstract class CellComponentMO implements ManagedObject, Serializable {
    protected ManagedReference<CellMO> cellRef;
    protected CellID cellID;
    
    public CellComponentMO(CellMO cell) {        
        this.cellRef = AppContext.getDataManager().createReference(cell);
        cellID = cell.getCellID();
    }
    
    protected void setLive(boolean live) {
        // Do nothing by default 
    }
    
    /**
     * Set up the cell component from the given properties
     * 
     * @param setup the properties to setup with
     */
    public void setupCellComponent(CellComponentServerState setup) {
        // Do nothing by default
    }

    /**
     * Returns the setup information currently configured in the component. If
     * the setup argument is non-null, fill in that object and return it. If the
     * setup argument is null, create a new setup object.
     *
     * @param setup The setup object, if null, creates one.
     * @return The current setup information
     */
    public CellComponentServerState getServerState(CellComponentServerState setup) {
        // Do nothing by default
        return setup;
    }

    /**
     * If this component has a client side component then return the fully
     * qualified name of the client class. If there is no client portion to this
     * component, return null.
     * @return
     */
    protected abstract String getClientClass();

    /**
     * Return the class used to reference this component. Usually this will return
     * the class of the component, but in some cases, such as the ChannelComponentMO
     * subclasses of ChannelComponentMO will return their parents class
     * @return
     */
    protected Class getLookupClass() {
        return getClass();
    }
}
