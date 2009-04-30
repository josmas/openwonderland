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
package org.jdesktop.wonderland.modules.portal.client;

import com.jme.bounding.BoundingSphere;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import java.awt.Image;
import java.util.Properties;
import org.jdesktop.wonderland.client.cell.registry.annotation.CellFactory;
import org.jdesktop.wonderland.client.cell.registry.spi.CellFactorySPI;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Origin;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Rotation;
import org.jdesktop.wonderland.common.cell.state.PositionComponentServerState.Scale;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.state.JmeColladaCellServerState;
import org.jdesktop.wonderland.modules.portal.common.PortalComponentServerState;

/**
 * The cell factory for the portal cell.
 * 
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@CellFactory
public class PortalCellFactory implements CellFactorySPI {

    public String[] getExtensions() {
        return new String[] {};
    }

    public <T extends CellServerState> T getDefaultCellServerState(Properties props) {
        JmeColladaCellServerState state = new JmeColladaCellServerState();

        state.setModel("wla://portal/portal.kmz/models/portal.dae");
        state.setGeometryScale(new Scale(new Vector3f(0.01f, 0.01f, 0.01f)));
        state.setGeometryTranslation(new Origin(new Vector3f(-1.5f, 1.3f, 0f)));
        state.setGeometryRotation(new Rotation(new Quaternion(1f, 0f, 0f, 0.7854f)));
        state.addComponentServerState(new PortalComponentServerState());

        System.out.println("[PortalCellFactory] newer code!");

        //PositionComponentServerState pcs = new PositionComponentServerState();
        //pcs.setBounds(new BoundingSphere(2.0f, new Vector3f(0f, 0f, 0f)));
        // pcs.setOrigin(new Origin(new Vector3f(0f, 2.6f, 0f)));
        //state.addComponentServerState(pcs);

        return (T)state;
    }

    public String getDisplayName() {
        return "Portal Cell";
    }

    public Image getPreviewImage() {
        return null;
    }
}
