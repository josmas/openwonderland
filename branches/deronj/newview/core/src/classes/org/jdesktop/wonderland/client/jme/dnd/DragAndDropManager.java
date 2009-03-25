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
package org.jdesktop.wonderland.client.jme.dnd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jdesktop.wonderland.client.jme.dnd.spi.DataFlavorHandlerSPI;
import org.jdesktop.wonderland.common.ExperimentalAPI;

/**
 * Manages the drag-and-drop for the world. There is a single drop source for
 * the world, which is typically the main rendering panel. Other parts of the
 * system (e.g. modules) can register (via annotations) to handle various
 * data flavors of drag sources (e.g. if you are dragging from the Desktop
 * versus dragging a cell from the Cell Palette.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class DragAndDropManager {

    /* The drop-target component */
    private Component dropTarget = null;

    /* A map of supported data flavor and the handler */
    private Map<DataFlavor, DataFlavorHandlerSPI> dataFlavorHandlerMap;

    /** Default constructor */
    private DragAndDropManager() {
        // Create the hash map to hold all of the data flavor handlers and add
        // in the default one to handle drag-and-drop of files from the desktop
        dataFlavorHandlerMap = new HashMap();
        registerDataFlavorHandler(new DesktopImportDataFlavorHandler());
        registerDataFlavorHandler(new URLDataFlavorHandler());
    }

    /**
     * Singleton to hold instance of DragAndDropMananger. This holder class is
     * loader on the first execution of DragAndDropManager.getDragAndDropManager().
     */
    private static class DragAndDropManagerHolder {
        private final static DragAndDropManager dndManager = new DragAndDropManager();
    }

    /**
     * Returns a single instance of this class
     * <p>
     * @return Single instance of this class.
     */
    public static final DragAndDropManager getDragAndDropManager() {
        return DragAndDropManagerHolder.dndManager;
    }

    /**
     * Sets the sole drop target for the system-wide drag and drop
     *
     * @param dropTarget The new drop target for all drag-and-drop operations
     */
    public void setDropTarget(Component dropTarget) {
        // First check whether there is an existing drop target and null out
        // its drop target
        if (this.dropTarget != null) {
            this.dropTarget.setDropTarget(null);
        }
        this.dropTarget = dropTarget;
        DropTarget dt = new DropTarget(dropTarget, new JmeDropTarget());
        dropTarget.setDropTarget(dt);
    }

    /**
     * Registers a DataFlavorHandlerSPI. A data flavor handler handles when an
     * item has been dropped into the world for a specific type. Only one
     * handler is permitted per data flavor type.
     *
     * @param handler The data flavor handler
     */
    public void registerDataFlavorHandler(DataFlavorHandlerSPI handler) {
        // For each of the data flavors that are supported by the handler, add
        // then to the map. If the data flavor is already registered, then
        // overwrite.
        DataFlavor flavors[] = handler.getDataFlavors();
        if (flavors != null) {
            for (DataFlavor flavor : flavors) {
                dataFlavorHandlerMap.put(flavor, handler);
            }
        }
    }

    /**
     * Returns a set of supported data flavors.
     *
     * @return A Set of DataFlavor objects
     */
    public Set<DataFlavor> getDataFlavors() {
        return dataFlavorHandlerMap.keySet();
    }

    /**
     * Returns the data flavor handler for the given data flavor, null if one
     * does not exist for the data flavor.
     *
     * @param dataFlavor The DataFlavor object
     * @return A DataFlavorHandlerSPI object
     */
    public DataFlavorHandlerSPI getDataFlavorHandler(DataFlavor dataFlavor) {
        return dataFlavorHandlerMap.get(dataFlavor);
    }
    
    /**
     * Adapter for the drop target event. Dispatches to the various handlers
     * registerd on this class for the matching data flavor
     */
    private class JmeDropTarget extends DropTargetAdapter {

        public void drop(DropTargetDropEvent dtde) {
            // For each of the data flavors that are supported by the drag-and-
            // drop manager, see if the dropped item supports the data flavor
            // and dispatch to the first one found after accepting the drop
            Set<DataFlavor> flavorSet = getDataFlavors();
            for (DataFlavor flavor : flavorSet) {
                if (dtde.isDataFlavorSupported(flavor) == true) {
                    DataFlavorHandlerSPI handler = getDataFlavorHandler(flavor);
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    handler.handleDrop(dtde.getTransferable(), flavor, dtde.getLocation());
                    return;
                }
            }

            // If we have reached here, then there is no handler to support
            // the data flavor and we should reject the drop
            dtde.rejectDrop();
        }
    }
}
