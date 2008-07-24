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
package org.jdesktop.wonderland.server.auth;

import com.sun.sgs.auth.Identity;
import com.sun.sgs.auth.IdentityAuthenticator;
import com.sun.sgs.auth.IdentityCredentials;
import com.sun.sgs.impl.auth.NamePasswordCredentials;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginException;

/**
 * An authenticator that uses LDAP to get a person's information.  This 
 * authenticator relies on the following properties being set in
 * the Darkstar configuration file:
 * <ul>
 * <li><code>org.jdesktop.lg3d.wonderland.darkstar.server.auth.ldap.directory</code>
 *     The LDAP URL to connect to, for example ldaps://directory.java.net
 * </li>
 * <li><code>org.jdesktop.lg3d.wonderland.darkstar.server.auth.ldap.base-dn</code>
 *     The base DN to perform lookups under.  For example dc=org,dc=jdesktop
 * </li>
 * </ul>
 * 
 * Additionally, the following optional parameters may be specified:
 * <ul>
 * <li><code>org.jdesktop.lg3d.wonderland.darkstar.server.auth.ldap.search-filter</code>
 *     The LDAP filter to search for user names, for example "employeenumber=%s", 
 *     where %s will be subsitituted with the user name used for authentication. 
 *     The default is "uid=%s"
 * </li>
 * <li><code>org.jdesktop.lg3d.wonderland.darkstar.server.auth.ldap.context-factory</code>
 *     The directory context factory.  Default is "com.sun.jndi.ldap.LdapCtxFactory"
 * </li>
 * <li><code>org.jdesktop.lg3d.wonderland.darkstar.server.auth.ldap.username-attr</code>
 *     The attribute in the directory that represents the username.  Default is "uid"
 * </li>
 * <li><code>org.jdesktop.lg3d.wonderland.darkstar.server.auth.ldap.fullname-attr</code>
 *     The attribute in the directory that represents the full name.  Default is "cn"
 * </li>
 * <li><code>org.jdesktop.lg3d.wonderland.darkstar.server.auth.ldap.email-attr</code>
 *     The attribute in the directory that represents the email address.  Default is "mail"
 * </li>
 * </ul>
 * @author jkaplan
 */
public class LDAPAuth implements IdentityAuthenticator {
    // logger
    private static final Logger logger =
            Logger.getLogger(LDAPAuth.class.getName());
    
    private Hashtable ldapEnv;
   
    // properties to use
    private static final String PROP_BASE = 
            LDAPAuth.class.getPackage().getName() + ".ldap.";
    private static final String PROP_LDAP_URL = PROP_BASE + "directory";
    private static final String PROP_BASE_DN = PROP_BASE + "base-dn";
    private static final String PROP_SEARCH_FILTER = PROP_BASE + "search-filter";
    private static final String PROP_CONTEXT_FACTORY = PROP_BASE + "context-factory";
    private static final String PROP_USERNAME_ATTR = PROP_BASE + "username-attr";
    private static final String PROP_FULLNAME_ATTR = PROP_BASE + "fullname-attr";
    private static final String PROP_EMAIL_ATTR = PROP_BASE + "email-attr";
    
    // default values
    private static final String SEARCH_FILTER_DEFAULT = "uid=%s";
    private static final String CONTEXT_FACTORY_DEFAULT = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String USERNAME_ATTR_DEFAULT = "uid";
    private static final String FULLNAME_ATTR_DEFAULT = "cn";
    private static final String EMAIL_ATTR_DEFAULT = "mail";
    
    // properties not in environment
    String searchFilter;
    String baseDN;
    String usernameAttr;
    String fullnameAttr;
    String emailAttr;
    
    // directory context
    DirContext dnContext;
    
    public LDAPAuth(Properties prop) {
        // required properties
        String ldapURL = prop.getProperty(PROP_LDAP_URL);
        if (ldapURL == null) {
            throw new RuntimeException(PROP_LDAP_URL + " required");
        }
        baseDN = prop.getProperty(PROP_BASE_DN);
        if (baseDN == null) {
            throw new RuntimeException(PROP_BASE_DN + " required.");
        }
        
        // optional properties
        searchFilter = prop.getProperty(PROP_SEARCH_FILTER, 
                                        SEARCH_FILTER_DEFAULT);
        usernameAttr = prop.getProperty(PROP_USERNAME_ATTR,
                                        USERNAME_ATTR_DEFAULT);
        fullnameAttr = prop.getProperty(PROP_FULLNAME_ATTR,
                                        FULLNAME_ATTR_DEFAULT);
        emailAttr = prop.getProperty(PROP_EMAIL_ATTR,
                                     EMAIL_ATTR_DEFAULT);
        
        String contextFactory = prop.getProperty(PROP_CONTEXT_FACTORY, 
                                        CONTEXT_FACTORY_DEFAULT);
        
        ldapEnv = new Hashtable();
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        ldapEnv.put(Context.PROVIDER_URL, ldapURL);
        
        logger.info("Loading LDAP authenticator");
    }

    public String[] getSupportedCredentialTypes() {
        return new String[] { "NameAndPasswordCredentials" };
    }
    
    public Identity authenticateIdentity(IdentityCredentials credentials) 
        throws LoginException 
    {
        if (!(credentials instanceof NamePasswordCredentials)) {
            throw new CredentialException("Wrong credential class: " +
                    credentials.getClass().getName());
        }
        
        NamePasswordCredentials npc = (NamePasswordCredentials) credentials;
        String dn;
        DirContext userContext;
       
        // map name to an identity
        try {
            userContext = findUser(npc.getName());
            dn = userContext.getNameInNamespace();
        } catch (NamingException ne) {
            logger.log(Level.WARNING, "Directory lookup error.", ne);
            throw new LoginException("Directory lookup error.");
        }
        
        // make sure we found the user
        if (dn == null) {
            throw new AccountNotFoundException("User " + npc.getName() + 
                                               " not found");
        }
        
        // make sure the password is not zero-length
        if (npc.getPassword().length == 0) {
            throw new CredentialException("Invalid username or password.");
        }
        
        // now try to bind to that dn
        Hashtable secureEnv = new Hashtable(ldapEnv);
        secureEnv.put(Context.SECURITY_PRINCIPAL, dn);
        secureEnv.put(Context.SECURITY_CREDENTIALS, npc.getPassword());
                
        // try connecting.  If we succeed, the password was correct.
        try {
            DirContext secureContext = new InitialDirContext(secureEnv);
            secureContext.close();
        } catch (NamingException ne) {
            throw new CredentialException("Bad password for " + dn);
        }
                
        // our connection succeeded, so now return a valid identity
        try {
            WonderlandIdentity id = getIdentity(userContext);
            return id;
        } catch (NamingException ne) {
            logger.log(Level.WARNING, "Identity resolution error.");
            throw new LoginException("Error getting identity information");
        }
    }
    
    /**
     * Map a username to a user's directory entry.
     * @param username the name to lookup
     * @return the DirectoryContext corresponding to the given user name
     * @throws NamingException if there is an error finding the given
     * username
     */
    protected DirContext findUser(String username)  
        throws NamingException
    {
        DirContext ret = null;
        
        // see if we need to instantiate the context
        if (dnContext == null) {
            dnContext = new InitialDirContext(ldapEnv);
        }
        
        // setup the search controls
        SearchControls sc = new SearchControls();
        sc.setReturningAttributes(new String[] { "cn" });
        sc.setReturningObjFlag(true);
        sc.setCountLimit(1);
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        
        // do the search
        String filter = searchFilter.replaceAll("%s", username);        
        NamingEnumeration ne = dnContext.search(baseDN, filter, sc);
        if (ne.hasMore()) {
            SearchResult res = (SearchResult) ne.next();
            ret = (DirContext) res.getObject();
        }
        
        // return the name
        return ret;
    }

    /**
     * Get a WonderlandIdentity from the user's LDAP entry.
     * @param userContext the user's directory information
     * @return a WonderlandIdentity with information about the
     * given user
     * @throws NamingException if there is an error reading the
     * directory information
     */
    protected WonderlandIdentity getIdentity(DirContext userContext)
        throws NamingException
    {
        String username = getAttribute(usernameAttr, userContext);
        String fullname = getAttribute(fullnameAttr, userContext);
        String email = getAttribute(emailAttr, userContext);
        
        return new WonderlandIdentity(username, fullname, email);
    }
    
    /**
     * Get an attribute's first value
     * @param attrName the attribute to get
     * @param context the context to get the attribute from
     * @return the attribute's first value, or null if the attribute
     * doesn't exist
     * @throws NamingException if there is an error
     */
    protected String getAttribute(String attrName, DirContext context)
        throws NamingException
    {
        Attributes attrs = context.getAttributes("", new String[] { attrName });
        Attribute attr = attrs.get(attrName);
        if (attr != null) {
            return (String) attr.get();
        } else {
            return null;
        }
    }
}
