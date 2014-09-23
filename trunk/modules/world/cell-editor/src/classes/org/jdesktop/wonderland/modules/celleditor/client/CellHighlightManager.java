/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

package org.jdesktop.wonderland.modules.celleditor.client;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Geometry;
import com.jme.scene.Spatial;
import java.util.logging.Logger;
import org.jdesktop.mtgame.Entity;
import org.jdesktop.mtgame.RenderComponent;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.input.Event;
import org.jdesktop.wonderland.client.input.EventClassListener;
import org.jdesktop.wonderland.client.jme.ClientContextJME;
import org.jdesktop.wonderland.client.jme.cellrenderer.CellRendererJME;
import org.jdesktop.wonderland.client.jme.utils.traverser.ProcessNodeInterface;
import org.jdesktop.wonderland.client.jme.utils.traverser.TreeScan;
import org.jdesktop.wonderland.client.scenemanager.event.EnterExitEvent;

/**
 * manages the highlighting effect of cell
 * 
 * @author Abhishek Upadhyay
 */
public class CellHighlightManager extends EventClassListener {

    private static Logger LOGGER =
            Logger.getLogger(CellHighlightManager.class.getName());
    public ColorRGBA GLOW_COLOR = new ColorRGBA(ColorRGBA.yellow);
    private static final Vector3f GLOW_SCALE = new Vector3f(1.1f, 1.1f, 1.1f);

    @Override
    public Class[] eventClassesToConsume() {
        return new Class[]{
            EnterExitEvent.class};

    }

    @Override
    public void commitEvent(Event event) {

        EnterExitEvent eeEvent = (EnterExitEvent) event;
        Entity e = eeEvent.getPrimaryEntity();
        Cell cell = eeEvent.getCellForEntity(e);

        if (cell != null) {
            if (eeEvent.isEnter()) {
                highlightCell(cell, true, GLOW_COLOR);
            } else {
                highlightCell(cell, false, GLOW_COLOR);
            }
        }

    }

    /**
     * We assume this is called on the MT-Game render thread
     *
     * @param cell
     * @param highlight
     * @param color
     */
    public void highlightCell(final Cell cell, final boolean highlight, final ColorRGBA color) {
        CellRendererJME r = (CellRendererJME) cell.getCellRenderer(Cell.RendererType.RENDERER_JME);
        Entity entity = r.getEntity();
        RenderComponent rc = entity.getComponent(RenderComponent.class);

        //check if object has Navigate-To capability
        final NewInteractionComponent hc = cell.getComponent(NewInteractionComponent.class);
        if (hc != null && hc.isHighlightEnable()) {
            if (rc == null) {
                return;
            }
            TreeScan.findNode(rc.getSceneRoot(), Geometry.class, new ProcessNodeInterface() {
                public boolean processNode(final Spatial s) {
                    s.setGlowEnabled(highlight);
                    float comps[] = hc.getHighlightColor().getColorComponents(null);
                    ColorRGBA newcolor = new ColorRGBA((float) comps[0],
                            (float) comps[1],
                            (float) comps[2],
                            hc.getHighlightColor().getTransparency());
                    s.setGlowScale(GLOW_SCALE);
                    s.setGlowColor(newcolor);
                    ClientContextJME.getWorldManager().addToUpdateList(s);
                    return true;
                }
            }, false, false);
        }
    }
}
