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
package org.jdesktop.wonderland.modules.jmecolladaloader.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import com.jme.bounding.BoundingBox;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.sun.sgs.app.ClientSession;
import org.jdesktop.wonderland.common.ExperimentalAPI;
import org.jdesktop.wonderland.common.cell.CellTransform;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.config.CellConfig;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.config.JmeColladaCellConfig;
import org.jdesktop.wonderland.common.cell.setup.BasicCellSetup;
import org.jdesktop.wonderland.modules.jmecolladaloader.common.cell.setup.JMEColladaCellSetup;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.setup.BasicCellSetupHelper;
import org.jdesktop.wonderland.server.setup.BeanSetupMO;


/**
 * A cell for collada models
 * @author paulby
 */
@ExperimentalAPI
public class JmeColladaCellMO extends CellMO implements BeanSetupMO { 
    
    /* The unique model URI */
    private String modelURI = null;
    private Vector3f geometryTranslation;
    private Quaternion geometryRotation;
    	
    /** Default constructor, used when cell is created via WFS */
    public JmeColladaCellMO() {
    }
    public JmeColladaCellMO(Vector3f center, float size, String modelURI, Vector3f geometryTranslation, Quaternion geometryRotation) {
        super(new BoundingBox(new Vector3f(), size, size, size), new CellTransform(null, center));
        this.modelURI = modelURI;
        this.geometryRotation = geometryRotation;
        this.geometryTranslation = geometryTranslation;
    }

    public JmeColladaCellMO(Vector3f center, float size, String modelURI) {
        this(center, size,  modelURI, null, null);
    }
    
    public JmeColladaCellMO(BoundingVolume bounds, CellTransform transform) {
        super(bounds, transform);
    }
    
    @Override protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "org.jdesktop.wonderland.modules.jmecolladaloader.client.cell.JmeColladaCell";
    }

    @Override
    public CellConfig getCellConfig(WonderlandClientID clientID, ClientCapabilities capabilities) {
        CellConfig ret = new JmeColladaCellConfig(this.modelURI, geometryTranslation, geometryRotation);
        populateCellConfig(ret);
        return ret;
    }

    @Override
    public void setupCell(BasicCellSetup setup) {
        super.setupCell(setup);
        this.modelURI = ((JMEColladaCellSetup)setup).getModel();
    }

    @Override
    public void reconfigureCell(BasicCellSetup setup) {
        super.reconfigureCell(setup);
        setupCell(setup);
    }
}
