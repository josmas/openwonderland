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
package org.jdesktop.wonderland.common.cell.config;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The CellConfig class is the base class of all state information
 * communicated between the client and Darkstar server nodes.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class CellConfig implements Serializable {
    
    private ArrayList<String> clientComponentClasses = new ArrayList();

    /**
     * Returns the class names of all the client CellComponents which should
     * be added to the cell at config time
     * 
     * @return
     */
    public String[] getClientComponentClasses() {
        if (clientComponentClasses==null)
            return new String[0];
        return clientComponentClasses.toArray(new String[clientComponentClasses.size()]);
    }
    
    /**
     * Set the CellComponent class names that will be installed in the client
     * cell
     * @param cellComponenClasses the array of class names for client CellComponents
     */
    public void addClientComponentClasses(String[] cellComponenClasses) {
        if (cellComponenClasses!=null) {        
            for(String s : cellComponenClasses)
                clientComponentClasses.add(s);
        }
    }

    /**
     * Add a client component class to the set of components
     * @param clientClass
     */
    public void addClientComponentClasses(String clientClass) {
        clientComponentClasses.add(clientClass);
    }

}
