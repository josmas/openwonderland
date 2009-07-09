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

package org.jdesktop.wonderland.modules.sample.client;

import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellComponent;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.state.CellComponentClientState;
import org.jdesktop.wonderland.modules.sample.common.SampleCellComponentClientState;

/**
 * Client-side sample cell component
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class SampleCellComponent extends CellComponent {

    private static Logger logger = Logger.getLogger(SampleCellComponent.class.getName());
    private String info = null;

    @UsesCellComponent
    private SampleCellSubComponent component;

    public SampleCellComponent(Cell cell) {
        super(cell);
    }

    @Override
    public void setClientState(CellComponentClientState clientState) {
        super.setClientState(clientState);
        info = ((SampleCellComponentClientState)clientState).getInfo();
    }

    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);
        logger.warning("Setting status on SampleCellComponent to " + status);
    }
}
