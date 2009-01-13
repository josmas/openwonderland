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
package org.jdesktop.wonderland.server.cell;

import org.jdesktop.wonderland.server.cell.*;
import java.util.Iterator;
import sun.misc.Service;

/**
 * A factory that creates cell GLOs by type.  This uses the service provider
 * API to find all installed CellGLOProviders.  The providers are responsible
 * for actually instantiating the cells.
 * <p>
 * CellGLOs are identified by a type name, which may be a fully-qualified
 * class name or may be a shorthand name defined by a specific provider.
 * <p>
 * If no provider can handle the given type name, this method assumes the name
 * is a fully-qualified class name and loads that class.
 * @author jkaplan
 */
public class CellMOFactory {
   
    /**
     * Instantiate a cell GLO of the given type with the given arguments.
     * This will try to load from each provider in turn, returning either
     * the first response or null if no cell can be created with the given
     * type name.
     * 
     * @param typeName the name of the cell type to instantiate.
     * @param args the arguments to the constructor of the given cell type
     * @throws LoadCellMOException if there is an error loading the
     * given cell type with the given arguments
     */
    public static CellMO loadCellMO(String typeName, Object... args) 
        throws LoadCellMOException
    {       
        CellMO res = null;
        
        // check each provider
        for (Iterator<CellMOProvider> i = Service.providers(CellMOProvider.class); 
             i.hasNext();)
        {
            res = i.next().loadCellMO(typeName, args);
            if (res != null) {
                break;
            }
        }
        
        // no luck -- try instantiating as a class
        try {
            Class clazz = Class.forName(typeName);
            res = new DefaultCellMOProvider().loadCellMO(typeName, args);
        } catch (ClassNotFoundException cnfe) {
            // ignore -- it wasn't a class name after all
        }
        
        // see what we found
        return res;
    }
    
    // default provider
    static class DefaultCellMOProvider extends CellMOProvider {
        @SuppressWarnings("unchecked")
        public CellMO loadCellMO(String typeName, Object... args) 
                throws LoadCellMOException 
        {
            // assume type name is a fully-qualified class name
            try {
                Class<CellMO> clazz = 
                        (Class<CellMO>) Class.forName(typeName);
                return createCell(clazz, args);
            } catch (Exception ex) {
                throw new LoadCellMOException("Error loading type " + typeName, 
                                               ex);
            }
        }
    }
}
