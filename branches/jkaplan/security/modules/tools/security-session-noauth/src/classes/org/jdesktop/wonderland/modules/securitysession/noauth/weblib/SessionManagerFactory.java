/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.securitysession.noauth.weblib;

/**
 *
 * @author jkaplan
 */
public class SessionManagerFactory {
    private static final String SESSION_MANAGER_PROP = "session.manager.class";

    protected SessionManagerFactory() {
    }

    public static SessionManager getSessionManager() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final SessionManager INSTANCE = createInstance();

        private static SessionManager createInstance() {
            String className = System.getProperty(SESSION_MANAGER_PROP);
            if (className == null) {
                return new InternalSessionManagerImpl();
            }

            try {
                Class<SessionManager> clazz =
                        (Class<SessionManager>) Class.forName(className);
                return clazz.newInstance();
            } catch (InstantiationException ie) {
                throw new IllegalStateException("Unable to create class " +
                                                className, ie);
            } catch (IllegalAccessException iae) {
                throw new IllegalStateException("Unable to create class " +
                                                className, iae);
            } catch (ClassNotFoundException cnfe) {
                throw new IllegalStateException("Unable to find class " +
                                                className, cnfe);
            }
        }
    }
}
