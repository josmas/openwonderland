/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitygroups.weblib;

import org.jdesktop.wonderland.modules.securitygroups.weblib.db.*;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.jdesktop.wonderland.modules.security.weblib.serverauthmodule.GroupResolver;

/**
 *
 * @author jkaplan
 */
public class DBGroupResolverImpl implements GroupResolver {
    private static final String DEFAULT_GROUP_OPT = "default.group";
    private static final String DEFAULT_GROUP_DEFAULT = "users";

    private EntityManagerFactory emf;
    private String defaultGroup;

    public DBGroupResolverImpl() {
        // create the entity manager factory for querying the database for
        // groups
        emf = Persistence.createEntityManagerFactory("WonderlandGroupPU");
    }

    public void initialize(Map opts) {
        // get the default group
        defaultGroup = (String) opts.get(DEFAULT_GROUP_OPT);
        if (defaultGroup == null) {
            defaultGroup = DEFAULT_GROUP_DEFAULT;
        }
    }

    public String[] getGroupsForUser(String userId) {
        // get the groups associated with this user
        GroupDAO dao = new GroupDAO(emf);
        List<GroupEntity> groups = dao.findGroupsForMember(userId);

        String[] out = new String[groups.size() + 1];
        out[0] = defaultGroup;
        for (int i = 0; i < groups.size(); i++) {
            out[i + 1] = groups.get(i).getId();
        }

        return out;
    }
}
