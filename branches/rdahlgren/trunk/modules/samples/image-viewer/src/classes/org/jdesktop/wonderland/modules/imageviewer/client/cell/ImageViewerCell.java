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
package org.jdesktop.wonderland.modules.imageviewer.client.cell;

import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.modules.imageviewer.client.cell.jme.cellrenderer.ImageViewerCellRenderer;
import org.jdesktop.wonderland.modules.imageviewer.common.cell.ImageViewerCellClientState;

/**
 * Client-side cell class to display an image.
 *
 * @author Jordan Slott <jslott@dev.java.net>
 */
@ExperimentalAPI
public class ImageViewerCell extends Cell {

    /** Scale factor for the image width */
    public static final float WIDTH_SCALE_FACTOR = 0.01f;

    /** Scale factory for the image height */
    public static final float HEIGHT_SCALE_FACTOR = 0.01f;

    /* The image uri to use */
    private String imageURI = null;

    /**
     * Create an instance of ImageViewerCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public ImageViewerCell (CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
    }

    /**
     * Initialize the whiteboard with parameters from the server.
     *
     * @param configData the config data to initialize the cell with
     */
    @Override
    public void setClientState(CellClientState clientState) {
        super.setClientState(clientState);
        imageURI = ((ImageViewerCellClientState)clientState).getImageURI();
    }

    /**
     * Returns the image uri
     */
    public String getImageURI() {
        return imageURI;
    }

    @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            return new ImageViewerCellRenderer(this);
        }
        else {
            return super.createCellRenderer(rendererType);
        }
    }
}
