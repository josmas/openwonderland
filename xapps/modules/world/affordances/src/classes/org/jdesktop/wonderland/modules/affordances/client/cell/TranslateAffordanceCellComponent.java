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
package org.jdesktop.wonderland.modules.affordances.client.cell;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.modules.affordances.client.jme.TranslateAffordance;

/**
 * A client-side cell component for translate affordances
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
public class TranslateAffordanceCellComponent extends AffordanceCellComponent {

    public TranslateAffordanceCellComponent(Cell cell) throws AffordanceException {
        super(cell);
        affordance = new TranslateAffordance(cell);
    }

    @Override
    public void remove() {
        super.remove();
        affordance.remove();
        cell.removeComponent(TranslateAffordanceCellComponent.class);
        affordance = null;
    }
}
