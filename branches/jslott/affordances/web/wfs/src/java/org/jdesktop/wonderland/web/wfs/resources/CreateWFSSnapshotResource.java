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

package org.jdesktop.wonderland.web.wfs.resources;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.jdesktop.wonderland.common.wfs.WorldRoot;
import org.jdesktop.wonderland.tools.wfs.WFS;
import org.jdesktop.wonderland.web.wfs.WFSManager;

/**
 * Handles Jersey RESTful requests to create a wfs "snapshot" (a backup of a
 * wfs). Creates the snapshot in the pre-determined directory according to the
 * current date and time. Returns an XML representation of the WorldRoot class
 * given the unique path of the wfs for later reference.
 * <p>
 * URI: http://<machine>:<port>/wonderland-web-wfs/wfs/create/snapshot
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/create/snapshot")
public class CreateWFSSnapshotResource {

    /**
     * Creates a new snapshot, using the current date on the server. Adds a
     * new WFS object and creates the entry on disk. Returns a WorldRoot object
     * that represents the new snapshot
     * 
     * @return A Snapshot object
     */
    @GET
    @Produces({"text/plain", "application/xml", "application/json"})
    public Response createWFSSnapshot() {
        // Do some basic stuff, get the WFS manager class, etc
        Logger logger = Logger.getLogger(CreateWFSSnapshotResource.class.getName());
        WFSManager manager = WFSManager.getWFSManager();
        
        // Find the currnent date, convert to a suitable formatted string
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String dateString = df.format(new Date());
        
        // Create the WFS check return value is not null (error if so)
        WFS wfs = manager.createWFSSnapshot(dateString);
        if (wfs == null) {
            logger.warning("[WFS] Unable to create snapshot " + dateString);
            ResponseBuilder rb = Response.status(Response.Status.BAD_REQUEST);
            return rb.build();
        }
        
        // Form the root path of the wfs: "snapshots/<date>/world-wfs"
        String rootPath = WFSManager.SNAPSHOT_DIRS + File.separator +
                dateString + File.separator + WFSManager.SNAPSHOT_WFS;
        WorldRoot worldRoot = new WorldRoot(rootPath);
        
        // Formulate the response and return the world root object
        ResponseBuilder rb = Response.ok(worldRoot);
        return rb.build();
    }
}
