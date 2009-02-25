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
package org.jdesktop.wonderland.modules.securitygroups.common;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import javax.xml.bind.JAXBException;

/**
 *
 * @author jkaplan
 */
public class GroupUtils {
    private static final String GROUPS_PATH =
            "security-groups/security-groups/resources/groups";
    
    public static GroupDTO getGroup(String groupName,
                                    CredentialManager cm)
        throws IOException, JAXBException
    {
        URL u = new URL(cm.getBaseURL() + GROUPS_PATH + "/" + groupName);
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        cm.secureConnection(uc);

        if (uc.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            // group doesn't exist
            return null;
        }

        return GroupDTO.decode(new InputStreamReader(uc.getInputStream()));
    }

    public static Set<GroupDTO> getGroups(String filter, boolean members,
                                          CredentialManager cm)
        throws IOException, JAXBException
    {
        String urlStr = cm.getBaseURL() + GROUPS_PATH + "?members=" + members;
        if (filter != null) {
            urlStr += "&pattern=" + URLEncoder.encode(filter, "UTF-8");
        }

        return getGroups(urlStr, cm);
    }

    public static Set<GroupDTO> getGroupsForUser(String userId, boolean members,
                                                 CredentialManager cm)
        throws IOException, JAXBException
    {
        String urlStr = cm.getBaseURL() + GROUPS_PATH + "?members=" + members;
        urlStr += "&user=" + userId;

        return getGroups(urlStr, cm);
    }

    private static Set<GroupDTO> getGroups(String url, CredentialManager cm)
        throws IOException, JAXBException
    {
        URL u = new URL(url);
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        cm.secureConnection(uc);

        GroupsDTO groups = GroupsDTO.decode(new InputStreamReader(uc.getInputStream()));
        return groups.getGroups();
    }

    public static void updateGroup(GroupDTO group, CredentialManager cm)
        throws IOException, JAXBException
    {
        // create the URL for the group
        URL u = new URL(cm.getBaseURL() + GROUPS_PATH + "/" + group.getId());
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        uc.setRequestMethod("POST");
        uc.setDoOutput(true);
        uc.setDoInput(true);
        cm.secureConnection(uc);

        // write the XML to the output stream
        group.encode(new OutputStreamWriter(uc.getOutputStream()));
        uc.getOutputStream().close();

        if (uc.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error updating group " + group.getId() +
                                  ": " + uc.getResponseMessage());
        }
    }

    public static void removeGroup(String groupName, CredentialManager cm)
            throws IOException
    {
        URL u = new URL(cm.getBaseURL() + GROUPS_PATH + "/" + groupName);
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        uc.setRequestMethod("DELETE");
        cm.secureConnection(uc);

        if (uc.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Error updating group " + groupName +
                                  ": " + uc.getResponseMessage());
        }
    }
}
