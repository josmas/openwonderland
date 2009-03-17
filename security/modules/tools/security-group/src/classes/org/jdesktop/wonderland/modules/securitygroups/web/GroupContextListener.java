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
package org.jdesktop.wonderland.modules.securitygroups.web;

import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.jdesktop.wonderland.front.admin.AdminRegistration;
import org.jdesktop.wonderland.modules.securitygroups.weblib.db.GroupDAO;
import org.jdesktop.wonderland.modules.securitygroups.weblib.db.GroupEntity;
import org.jdesktop.wonderland.modules.securitygroups.weblib.db.MemberEntity;
        
/**
 * Manage the installation and removal of the PingDataListener
 * @author jkaplan
 */
public class GroupContextListener implements ServletContextListener 
{
    private static final Logger logger =
            Logger.getLogger(GroupContextListener.class.getName());

    @PersistenceUnit
    private EntityManagerFactory emf;

    private AdminRegistration ar;
    
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        createInitialGroups();

        ar = new AdminRegistration("Group Editor",
                                   "/security-groups/security-groups/editor");
        AdminRegistration.register(ar, context);
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        if (ar != null) {
            AdminRegistration.unregister(ar, context);
        }
    }

    private void createInitialGroups() {
        GroupDAO groups = new GroupDAO(emf);
        GroupEntity adminGroup = groups.getGroup("admin");
        if (adminGroup == null) {
            adminGroup = new GroupEntity("admin");

            MemberEntity adminMember = new MemberEntity("admin", "admin");
            adminMember.setOwner(true);
            adminMember.setGroup(adminGroup);
            adminGroup.getMembers().add(adminMember);

            MemberEntity darkstarMember = new MemberEntity("admin", "darkstar");
            darkstarMember.setGroup(adminGroup);
            adminGroup.getMembers().add(darkstarMember);

            MemberEntity webserverMember = new MemberEntity("admin", "webserver");
            webserverMember.setGroup(adminGroup);
            adminGroup.getMembers().add(webserverMember);

            groups.updateGroup(adminGroup);

            logger.warning("Created initial group " + adminGroup.getId() +
                           " with " + adminGroup.getMembers().size() +
                           " members.");
        }
    }
}
