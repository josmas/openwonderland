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
package org.jdesktop.wonderland.web.wfs.resources;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.web.wfs.WFSManager;
import org.jdesktop.wonderland.tools.wfs.WFS;
import org.jdesktop.wonderland.tools.wfs.WFSCell;
import org.jdesktop.wonderland.tools.wfs.WFSCellDirectory;
import org.jdesktop.wonderland.common.wfs.CellList;
import org.jdesktop.wonderland.common.wfs.CellList.Cell;

/**
 * The WFSDirectoryResource class is a Jersey RESTful resource that allows clients
 * to query for the children of a cell by using a URI that describes the WFS
 * root and the path within the WFS to the cell. Within the URL, the standard
 * WFS naming conventions are not employed (e.g. -wld). 
 * <p>
 * The format of the URI is: /wfs/{wfsname}/{path}/directory, where {wfsname} is
 * the name of the WFS root (as returned by the WFSRootsResource), and {path}
 * is the relative path of the file within the WFS (without any -wld or -wlc.xml
 * suffixes).
 * <p>
 * The cell information returned is the JAXB serialization of the cell directory
 * information (the WFSCellChildren class).
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/{wfsname:.*}/directory/{path:.*}")
public class WFSDirectoryResource {
    
    /**
     * Returns the JAXB XML serialization of the cell directory given the
     * name of the root WFS (without the -wfs extension) and the path of the
     * cell within the WFS (without any -wld or -wlc.xml extensions). Returns
     * the XML via an HTTP GET request.
     * 
     * @param wfsName The name of the WFS root (no -wfs extension)
     * @param path The relative path of the file (no -wld, -wlc.xml extensions)
     * @return The XML serialization of the cell setup information via HTTP GET.
     */
    @GET
    @Produces("text/plain")
    public Response getCellResource(@PathParam("wfsname") String wfsName, @PathParam("path") String path) {
        /* Fetch thhe error logger for use in this method */
        Logger logger = WFSManager.getLogger();
        
        /*
         * Fetch the wfs manager and the WFS. If invalid, then return a bad
         * response.
         */
        WFSManager wfsm = WFSManager.getWFSManager();
        WFS wfs = wfsm.getWFS(wfsName);
        if (wfs == null) {
            logger.warning("WFSManager: Unable to find WFS with name " + wfsName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /* Fetch the root directory, check if null, but should never be */
        WFSCellDirectory dir = wfs.getRootDirectory();
        if (dir == null) {
            logger.warning("WFSManager: Unable to find WFS root with name " + wfsName);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /*
         * Split the path up into individual components. We then fetch the
         * object down the chain. We assume the last element is the file. As
         * a special case if path is an empty string, then set paths[] to an
         * empty array.
         */
        String paths[] = new String[0];
        if (path.compareTo("") != 0) {
            paths = path.split("/");
        }
        
        /*
         * Loop through each component and find the subdirectory in turn.
         */
        for (int i = 0; i < paths.length; i++) {
            /*
             * First fetch the cell. If it does not exist, then return a bad
             * response.
             */
            WFSCell cell = dir.getCellByName(paths[i]);
            if (cell == null) {
                logger.info("WFSManager: Unable to find cell with path: " + path);
                ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
                return rb.build();
            }
            
            /*
             * Next, get the directory associated with the cell. It also needs
             * to exist, otherwise, return a bad response. If it does not exist,
             * it means the cell does not have children.
             */
            if ((dir = cell.getCellDirectory()) == null) {
                ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
                return rb.build();
            }
        }
        
        /*
         * If we have reached here, we have 'dir' with the directory in which
         * the cells should be. Create a WFSCellChildren object which is used
         * to serialize the result.
         */
        String names[] = dir.getCellNames();
        if (names == null) {
            logger.info("WFSManager: Child names are null in " + path);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        /*
         * Loop through and create the WFSCellChildren object, we need to
         * include the last modified time so that the client can check whether
         * the cell has been modified or not.
         */
        LinkedList<Cell> list = new LinkedList<Cell>();
        for (String name : names) {
            /* Fetch the cell, it should not be null, but we check anyway */
            WFSCell cell = dir.getCellByName(name);
            if (cell == null) {
                logger.info("WFSManager: no cell exists with name " + name);
                continue;
            }
            
            /* Add it to the list */
            list.add(new Cell(name, cell.getLastModified()));
        }
        
        /* Convert the list of CellChilds to an array */
        Cell[] childs = list.toArray(new Cell[] {});
        CellList children = new CellList(path, childs);
        
        /* Send the serialized cell names to the client */
        try {
            StringWriter sw = new StringWriter();
            children.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (JAXBException excp) {
            logger.info("WFSManager: Unable to write dir with path: " + path + ": " + excp.toString());
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
    }
}
