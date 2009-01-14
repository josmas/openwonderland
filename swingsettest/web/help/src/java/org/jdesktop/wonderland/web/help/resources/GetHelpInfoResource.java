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
package org.jdesktop.wonderland.web.help.resources;

import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.common.help.HelpInfo;
import org.jdesktop.wonderland.web.help.deployer.HelpDeployer;

/**
 * The GetHelpInfoResource class is a Jersey RESTful resource that allows clients
 * to query for the XXX.
 * 
 * @author Jordan Slott <jslott@dev.java.net>
 */
@Path(value="/info/get")
public class GetHelpInfoResource {
 
    /* From this, get information about the base URL of the web service */
    @Context
    private UriInfo context;

    /* The error logger */
    private static Logger logger = Logger.getLogger(GetHelpInfoResource.class.getName());
    
    /**
     * TBD
     */
    @GET
    @Produces("text/plain")
    public Response getHelpInfo() {
        /* Formulate the HTTP response and send the string */
        HelpInfo info = HelpDeployer.buildHelpInfo();
        this.rewriteURIs(info);
        StringWriter sw = new StringWriter();
        try {
            info.encode(sw);
            ResponseBuilder rb = Response.ok(sw.toString());
            return rb.build();
        } catch (Exception excp) {
        }
        return null;
    }
    
    /**
     * Rewrites all of the URIs in the help content from wlh:// to an http://
     * that a browser can load
     */
    private void rewriteURIs(HelpInfo info) {
        /* Build the http:// prefix to access all of the help pages */
        String prefix = context.getBaseUriBuilder().build().toString();
        
        /* Loop through all of the entries and rewrite the URIs */
        if (info.getHelpEntries() != null) {
            for (HelpInfo.HelpMenuEntry entry : info.getHelpEntries()) {
                this.rewriteURIs(prefix, entry);
            }
        }
    }
    
    /**
     * Rewrites all of the URIs in the help content from wlh:// to an http://
     * that a browser can load
     */
    private void rewriteURIs(String prefix, HelpInfo.HelpMenuEntry entry) {
        /*
         * Only need to rewrite the URI if the entry is an "item", otherwise
         * recursively descend for "folders"
         */
        if (entry instanceof HelpInfo.HelpMenuItem) {
            HelpInfo.HelpMenuItem item = (HelpInfo.HelpMenuItem)entry;
            item.helpURI = this.getHttpURL(prefix, item.helpURI);
        }
        else if (entry instanceof HelpInfo.HelpMenuFolder) {
            HelpInfo.HelpMenuFolder folder = (HelpInfo.HelpMenuFolder)entry;
            HelpInfo.HelpMenuEntry[] folderEntries = folder.entries;
            
            if (folderEntries != null) {
                for (HelpInfo.HelpMenuEntry folderEntry : folderEntries) {
                    this.rewriteURIs(prefix, folderEntry);
                }
            }
        }
    }
    
    /**
     * Takes a string help URI and rewrites it into an HTTP URL from where the
     * help pages can be accesses. If not a valid
     */
    private String getHttpURL(String prefix, String uri) {
        /* See if the URI is a valid wlh:// URI, if not, just return the param */
        try {
            HelpURI helpURI = new HelpURI(uri);
            if (helpURI.getURI().getScheme().equals("wlh") == false) {
                return uri;
            }
            return prefix + helpURI.getModuleName() + "/help/get/" + helpURI.getRelativePath();
        } catch (URISyntaxException excp) {
            /* Log an error and return the original parameter */
            logger.log(Level.WARNING, "[HELP] Invalid Help URI", excp);
            return uri;
        }
    }
}
