/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitygroups.weblib.db;

import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

/**
 *
 * @author jkaplan
 */
public class GroupDAO {
    private EntityManagerFactory emf;

    public GroupDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public GroupEntity getGroup(String groupName) {
        EntityManager em = emf.createEntityManager();
        return em.find(GroupEntity.class, groupName);
    }

    public boolean hasGroup(String groupName) {
        EntityManager em = emf.createEntityManager();
        return em.contains(new GroupEntity(groupName));
    }

    public List<GroupEntity> getGroups() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("getGroups");
        return (List<GroupEntity>) q.getResultList();
    }

    public List<GroupEntity> findGroups(String groupId) {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("findGroups").setParameter("id", groupId);
        return (List<GroupEntity>) q.getResultList();
    }

    public List<GroupEntity> findGroupsForMember(String memberId) {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("groupsForMember").setParameter("memberId", memberId);

        return (List<GroupEntity>) q.getResultList();
    }

    public GroupEntity updateGroup(final GroupEntity group) {
        try {
            return runInTransaction(new EMCallable<GroupEntity>() {
                public GroupEntity call(EntityManager em) throws Exception {
                    // find the current value (if any) for this entity
                    GroupEntity cur = em.find(GroupEntity.class, group.getId());
                    if (cur == null) {
                        // this is a new entity, just add it and return
                        em.persist(group);
                        return group;
                    }

                    // now manually remove any members that are no longer on
                    // the list from the database
                    for (MemberEntity me : cur.getMembers()) {
                        if (!group.getMembers().contains(me)) {
                            em.remove(me);
                        }
                    }

                    // this is an existing group -- merge in the new data and
                    // return the updated group
                    em.merge(group);
                    return group;
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void removeGroup(final String groupName) {
        try {
            runInTransaction(new EMCallable() {
                public Object call(EntityManager em) throws Exception {
                    GroupEntity ge = em.find(GroupEntity.class, groupName);
                    if (ge != null) {
                        em.remove(ge);
                    }

                    return null;
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    

    protected <T> T runInTransaction(EMCallable<T> call) throws Exception {
        T out = null;
        EntityManager em = emf.createEntityManager();

        UserTransaction utx = getUtx();
        try {
            utx.begin();
            em.joinTransaction();
            out = call.call(em);
            utx.commit();
        } catch (Exception e) {
            utx.rollback();
        } finally {
            em.close();
        }

        return out;
    }

    protected interface EMCallable<T> {
        public T call(EntityManager em) throws Exception;
    }

    private static UserTransaction getUtx() {
        try {
            InitialContext ic = new InitialContext();
            return (UserTransaction) ic.lookup("java:comp/UserTransaction");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
}
