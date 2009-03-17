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
package org.jdesktop.wonderland.modules.securitygroups.web.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupDTO;
import org.jdesktop.wonderland.modules.securitygroups.weblib.db.GroupDAO;


/**
 *
 * @author jkaplan
 */
public class GroupResource {
    private SecurityContext security;
    private GroupDAO groups;
    private UriInfo uriInfo;
    private String groupId;

    public GroupResource(GroupDAO groups, UriInfo uriInfo, String groupId, 
                         SecurityContext security)
    {
        this.groups = groups;
        this.uriInfo = uriInfo;
        this.groupId = groupId;
        this.security = security;
    }

    @GET
    @Produces({"text/plain", "application/xml", "application/json"})
    public Response get() {
        GroupDTO out = GroupResourceUtil.toDTO(groups.getGroup(groupId), true);
        if (out == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // set whether or not the requesting user can edit the group
        out.setEditable(canModify());

        // return the encoded group
        return Response.ok(out).build();
    }
    
    @PUT
    @Consumes({"application/xml", "application/json"})
    public Response put(GroupDTO group) {
        if (group.getId() == null || !group.getId().equals(groupId)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // only group owners are allowed to update exiting groups
        if (!canModify()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        groups.updateGroup(GroupResourceUtil.toEntity(group));
        return Response.ok().build();
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    public Response post(GroupDTO group) {
        return put(group);
    }

    /**
     * This is used by the prototype javascript library to handle delete
     * requests from a browser by encoding the method in an argument to
     * a post.
     * @param method the method to execute
     * @return the result of executing the method
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response postForm(@FormParam("_method") String method) {
        if (method == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (method.equalsIgnoreCase("delete")) {
            return delete();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @DELETE
    public Response delete() {
        // only group owners are allowed to update exiting groups
        if (!canModify()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        groups.removeGroup(groupId);
        return Response.ok().build();
    }

    private boolean canModify() {
        return GroupResourceUtil.canModify(groupId, groups, security);
    }
}
