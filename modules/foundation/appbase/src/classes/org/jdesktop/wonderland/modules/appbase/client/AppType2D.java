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
package org.jdesktop.wonderland.modules.appbase.client;

import org.jdesktop.wonderland.modules.appbase.client.gui.guidefault.Gui2DFactory;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * The client-side description of a type of Wonderland or Conventional 2D application. For example,
 * a share-aware PDF app or X11 window. 
 * <br><br>
 * Note: Unless the getGuiFactory method is overridden by a subclass windows of this app type use the default
 * GUI factory (app.base.gui.default.GuiFactory).
 *
 * @author deronj
 */

@ExperimentalAPI
public abstract class AppType2D extends AppType {

    /**
     * {@inheritDoc}
     */
    public GuiFactory getGuiFactory () {
	return Gui2DFactory.getFactory();
    }
}
