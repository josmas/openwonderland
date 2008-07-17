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
 * This interface is used by cell GLOs that can read and write their state
 * to JavaBeans.  This capability can be used to persist a cell's state
 * to disk by systems such as the Wonderland File System.
 * <p>
 * In addition to supporting setup from a JavaBean file, cells that implement
 * this interface must provide a public, no-argument constructor.
 * 
 * @author jkaplan
 */
public interface BeanSetupMO {
    /**
     * Set up the properties of this cell GLO from a JavaBean.  After calling
     * this method, the state of the cell GLO should contain all the information
     * represented in the given cell properties file.
     *
     * @param setup the Java bean to read setup information from
     */
    public void setupCell(CellMOSetup setup);
    
    /**
     * Called when the properties of a cell have changed.
     * 
     * @param setup a Java bean with updated properties
     */
    public void reconfigureCell(CellMOSetup setup);
    
    /**
     * Write the cell's current state to a JavaBean.
     * @return a JavaBean representing the current state
     */
    public CellMOSetup getCellMOSetup();
}
