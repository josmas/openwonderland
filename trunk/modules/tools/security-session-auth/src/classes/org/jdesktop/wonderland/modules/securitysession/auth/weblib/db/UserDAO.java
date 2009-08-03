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

package org.jdesktop.wonderland.modules.securitysession.auth.weblib.db;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import org.apache.catalina.util.Base64;

/**
 * Access for the data object
 * @author jkaplan
 */
public class UserDAO {
    private EntityManagerFactory emf;

    /**
     * Create a new group data access object
     * @param emf the entity manager factory to access the group data
     */
    public UserDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /**
     * Get a user by id
     * @param userId the id of the group to get
     * @return the user with the given name, or null if no user exists
     * with that name
     */
    public UserEntity getUser(String userId) {
        EntityManager em = emf.createEntityManager();
        return em.find(UserEntity.class, userId);
    }

    /**
     * Get all users
     * @return the list of all users
     */
    public List<UserEntity> getUsers() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("allUsers");
        return (List<UserEntity>) q.getResultList();
    }

    /**
     * Get the count of users
     * @return the number of users defined
     */
    public long getUserCount() {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("userCount");
        return (Long) q.getSingleResult();
    }

    /**
     * Find all users matching the given pattern for user id
     * @param userId the id to match, which may contain wild cards
     * @return the list of users matching the given pattern
     */
    public List<UserEntity> getUsersById(String userId) {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("findUsersById").setParameter("id", userId);
        return (List<UserEntity>) q.getResultList();
    }

    /**
     * Find all users matching the given pattern for full name
     * @param fullname the name to match, which may contain wild cards
     * @return the list of users matching the given pattern
     */
    public List<UserEntity> getUsersByName(String fullname) {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("findUsersByName").setParameter("fullname", fullname);
        return (List<UserEntity>) q.getResultList();
    }

    /**
     * Find all users matching the given pattern for email
     * @param email the email address to match, which may contain wild cards
     * @return the list of users matching the given pattern
     */
    public List<UserEntity> getUsersByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        Query q = em.createNamedQuery("findUsersByEmail").setParameter("email", email);
        return (List<UserEntity>) q.getResultList();
    }

    /**
     * Compare the user's password to the given password.
     * @param userId the id of the user to compare password
     * @param password the password to compare
     * @return true if the given password matches this user, or false if not
     */
    public boolean passwordMatches(String userId, String password) {
        UserEntity ue = getUser(userId);
        if (ue == null) {
            // user not found
            return false;
        }

        return ue.getPasswordHash().equals(getPasswordHash(userId, password));
    }

    /**
     * Create or update a user entity.  This method translates any non-null
     * value in the password field into a corresponding password hash.  If
     * the password field is null, the password hash will not be modified.
     * @param user the user to create or update
     * @return the new user
     */
    public UserEntity updateUser(final UserEntity user) {
        // translate the password (if any) into a password hash
        if (user.getPassword() != null) {
            user.setPasswordHash(getPasswordHash(user.getId(),
                                                 user.getPassword()));
        }

        try {
            return runInTransaction(new EMCallable<UserEntity>() {
                public UserEntity call(EntityManager em) throws Exception {
                    // find the current value (if any) for this entity
                    UserEntity cur = em.find(UserEntity.class, user.getId());
                    if (cur == null) {
                        // this is a new entity, just add it and return
                        em.persist(user);
                        return user;
                    }

                    // the entity exists -- copy the password hash in if the
                    // passed in value is null
                    if (user.getPasswordHash() == null) {
                        user.setPasswordHash(cur.getPasswordHash());
                    }

                    // update the entity in persistence
                    em.merge(user);
                    return user;
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Remove a user entity
     * @param userId the id of the user to remove
     */
    public UserEntity removeUser(final String userId) {
        try {
            return runInTransaction(new EMCallable<UserEntity>() {
                public UserEntity call(EntityManager em) throws Exception {
                    // find the current value (if any) for this entity
                    UserEntity cur = em.find(UserEntity.class, userId);
                    if (cur != null) {
                        em.remove(cur);
                    }

                    return cur;
                }
            });
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Compute the password hash for a given password.  The hash is computed
     * by salting the password with the user id so that the same password
     * for two different users will have a different hash.
     * @param password the password to hash
     * @return the result of hashing the password.
     */
    protected static String getPasswordHash(String userId, String password) {
        MessageDigest md;

        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsae) {
            throw new IllegalStateException("Unable to find SHA", nsae);
        }

        md.update(userId.getBytes());
        byte[] salt = md.digest();

        try {
            md.update(password.getBytes("UTF-8"));
            md.update(salt);
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Unable to find encoding", uee);
        }

        byte[] res = md.digest();
        return new String(Base64.encode(res));
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
