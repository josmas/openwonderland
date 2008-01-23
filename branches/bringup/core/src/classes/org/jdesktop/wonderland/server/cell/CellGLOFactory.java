/**
 * Project Looking Glass
 * 
 * $RCSfile: CellGLOFactory.java,v $
 * 
 * Copyright (c) 2004-2007, Sun Microsystems, Inc., All Rights Reserved
 * 
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 * 
 * $Revision: 1.2 $
 * $Date: 2007/10/14 19:10:50 $
 * $State: Exp $ 
 */
package org.jdesktop.wonderland.server.cell;

import java.util.Iterator;
import java.util.ServiceLoader;

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
public class CellGLOFactory {
   
    /**
     * Instantiate a cell GLO of the given type with the given arguments.
     * This will try to load from each provider in turn, returning either
     * the first response or null if no cell can be created with the given
     * type name.
     * 
     * @param typeName the name of the cell type to instantiate.
     * @param args the arguments to the constructor of the given cell type
     * @throws LoadCellGLOException if there is an error loading the
     * given cell type with the given arguments
     */
    public static CellMO loadCellGLO(String typeName, Object... args) 
        throws LoadCellGLOException
    {       
        CellMO res = null;
        
        // get the service providers fot the CellGLOProvider class
        ServiceLoader<CellGLOProvider> s = 
                ServiceLoader.load(CellGLOProvider.class);
    
        // check each provider
        for (Iterator<CellGLOProvider> i = s.iterator(); i.hasNext();) {
            res = i.next().loadCellGLO(typeName, args);
            if (res != null) {
                break;
            }
        }
        
        // no luck -- try instantiating as a class
        try {
            Class clazz = Class.forName(typeName);
            res = new DefaultCellGLOProvider().loadCellGLO(typeName, args);
        } catch (ClassNotFoundException cnfe) {
            // ignore -- it wasn't a class name after all
        }
        
        // see what we found
        return res;
    }
    
    // default provider
    static class DefaultCellGLOProvider extends CellGLOProvider {
        @SuppressWarnings("unchecked")
        public CellMO loadCellGLO(String typeName, Object... args) 
                throws LoadCellGLOException 
        {
            // assume type name is a fully-qualified class name
            try {
                Class<CellMO> clazz = 
                        (Class<CellMO>) Class.forName(typeName);
                return createCell(clazz, args);
            } catch (Exception ex) {
                throw new LoadCellGLOException("Error loading type " + typeName, 
                                               ex);
            }
        }
    }
}
