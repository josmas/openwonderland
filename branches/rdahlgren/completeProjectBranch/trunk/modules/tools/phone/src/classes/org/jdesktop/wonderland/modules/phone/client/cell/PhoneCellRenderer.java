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
package org.jdesktop.wonderland.modules.phone.client.cell;

import com.jme.scene.Node;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.wonderland.client.jme.input.MouseButtonEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D;
import org.jdesktop.wonderland.client.jme.input.MouseEvent3D.ButtonId;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;

import org.jdesktop.wonderland.modules.jmecolladaloader.client.jme.cellrenderer.JmeColladaRenderer;

import java.lang.reflect.Method;

import java.net.MalformedURLException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jkaplan
 */
public class PhoneCellRenderer extends JmeColladaRenderer {

    public PhoneCellRenderer(Cell cell) {
        super(cell);
    }

    @Override
    protected Node createSceneGraph(Entity entity) {
        new MyMouseListener().addToEntity(entity);

        try {
            Node ret = loadColladaAsset(cell.getCellID().toString(), getAssetURL("wla://phone/conference_phone.dae"));
            ret.setName("PhoneRoot");
            return ret;
        } catch (MalformedURLException ex) {
            Logger.getLogger(PhoneCellRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    class MyMouseListener extends EventClassListener {

        public Class[] eventClassesToConsume() {
            return new Class[]{MouseEvent3D.class};
        }

        public void commitEvent(Event event) {
            if (event instanceof MouseButtonEvent3D) {
                MouseButtonEvent3D buttonEvent = (MouseButtonEvent3D) event;
                if (buttonEvent.isPressed() && buttonEvent.getButton() == ButtonId.BUTTON1) {
                    ((PhoneCell) cell).phoneSelected();
                }
            }
        }
    }

}
