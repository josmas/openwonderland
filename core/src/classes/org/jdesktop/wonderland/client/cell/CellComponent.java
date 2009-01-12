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
package org.jdesktop.wonderland.client.cell;

import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellStatus;

/**
 * CellComponents provide dynamic extensions to the Cell infrastructure. 
 * 
 * @author paulby
 */
@ExperimentalAPI
public class CellComponent {
    protected Cell cell;
    protected CellStatus status;
    
    public CellComponent(Cell cell) {
        this.cell = cell;
    }
    
    /**
     * Set the status of the component.
     * @param status
     */
    public void setStatus(CellStatus status) {
        this.status = status;
    }
    
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
