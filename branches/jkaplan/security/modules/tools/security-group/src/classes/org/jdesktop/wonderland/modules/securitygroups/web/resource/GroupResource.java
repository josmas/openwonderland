/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitygroups.web.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
    public Response post(GroupDTO group) {
        return put(group);
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
