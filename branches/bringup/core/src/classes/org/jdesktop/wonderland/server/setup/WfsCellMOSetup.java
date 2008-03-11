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

import org.jdesktop.wonderland.common.cell.*;
import java.net.URL;

/**
 * Superclass for all setup classes of wfs created cells
 *
 * @author paulby
 */
public abstract class WfsCellMOSetup implements CellMOSetup {

    private URL wfsURL=null;
    
    /**
     * Returns the URL from which this
     * @return
     */
    public URL getWfsURL() {
        return wfsURL;
    }
    
    public void setWfsURL(URL wfsURL) {
        this.wfsURL = wfsURL;
    }
}
