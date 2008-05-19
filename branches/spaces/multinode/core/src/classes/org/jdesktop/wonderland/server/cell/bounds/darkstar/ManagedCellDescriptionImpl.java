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
package org.jdesktop.wonderland.server.cell.bounds.darkstar;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.DataManager;
import com.sun.sgs.app.ManagedObject;
import com.sun.sgs.app.ManagedReference;
import java.io.Serializable;
import org.jdesktop.wonderland.server.cell.CellDescription;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.bounds.CellDescriptionImpl;

/**
 * An extension of CellDescriptionImpl that is a ManagedObject
 * @author jkaplan
 */
public class ManagedCellDescriptionImpl extends CellDescriptionImpl
    implements ManagedObject
{
    public ManagedCellDescriptionImpl(CellMO cell) {
        super (cell);
    }
    
    /**
     * Constructor for cloning
     * @param mirror
     */
    ManagedCellDescriptionImpl(CellDescription desc) {
        super (desc);
    }
    
    @Override
    public CellDescriptionImpl clone() {
        return new ManagedCellDescriptionImpl(this);
    }
    
    /**
     * Create a reference to the given cell. This version uses a 
     * ManagedReference
     * @param cell the cell description to create a reference to
     */
    @Override
    protected CellRef createReference(final CellDescriptionImpl desc) {
        if (desc instanceof ManagedCellDescriptionImpl) {
            return new ManagedCellRef((ManagedCellDescriptionImpl) desc);
        } else {
            return super.createReference(desc);
        }
    }
    
    class ManagedCellRef implements CellRef, Serializable {
        private ManagedReference<ManagedCellDescriptionImpl> ref;
        
        public ManagedCellRef(ManagedCellDescriptionImpl desc) {
            DataManager dm = AppContext.getDataManager();
            ref = dm.createReference(desc);
        }
        
        public CellDescriptionImpl get() {
            return ref.get();
        }
    }
}
