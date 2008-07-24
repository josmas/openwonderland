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

import java.io.Serializable;

/**
 *
 * @author jkaplan
 */
public interface CellMOSetup extends Serializable {
    /**
     * Returns the Cell MO class name corresponding with this properties class,
     * should be overridden by subclasses
     */
    public String getCellMOClassName();

    /**
     * Validate the setup, throw an exception if there is a problem validating
     */
    public void validate() throws InvalidCellMOSetupException;
}
