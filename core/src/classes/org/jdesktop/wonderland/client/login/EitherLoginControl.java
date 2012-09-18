/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.login;

import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.common.login.AuthenticationInfo;
import org.jdesktop.wonderland.common.login.CredentialManager;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;

/**
 * A wrapper that works with either a NoAuthLoginControl or a
 * WebServiceLoginControl, depending on which method the UI uses.
 */
public class EitherLoginControl extends LoginControl {
    LoginControl wrapped;
    private boolean cancelled = false;
    private final ServerSessionManager sessionManager;

    public EitherLoginControl(AuthenticationInfo info, ServerSessionManager sessionManager) {
        super(info, sessionManager);
        this.sessionManager = sessionManager;
    }

    @Override
    public void requestLogin(LoginUI ui) {
        super.requestLogin(ui);
        ui.requestLogin(this);
    }

    public NoAuthLoginControl getNoAuthLogin() {
        // create a web service object to log in with
        AuthenticationInfo info = super.getAuthInfo().clone();
        info.setType(AuthenticationInfo.Type.NONE);
        NoAuthLoginControl control = new NoAuthLoginControl(info, sessionManager);
        control.setStarted();
        setWrapped(control);
        return control;
    }

    public UserPasswordLoginControl getUserPasswordLogin() {
        // create a web service object to log in with
        AuthenticationInfo info = super.getAuthInfo().clone();
        info.setType(AuthenticationInfo.Type.WEB_SERVICE);
        UserPasswordLoginControl control = new UserPasswordLoginControl(info, sessionManager);
        control.setStarted();
        setWrapped(control);
        return control;
    }

    public String getUsername() {
        return getWrapped().getUsername();
    }

    @Override
    public CredentialManager getCredentialManager() {
        return getWrapped().getCredentialManager();
    }

    @Override
    public LoginParameters getLoginParameters() {
        return getWrapped().getLoginParameters();
    }

    @Override
    public synchronized ScannedClassLoader getClassLoader() {
        return getWrapped().getClassLoader();
    }

    @Override
    public boolean isAuthenticated() {
        if (getWrapped() == null) {
            return false;
        }
        return getWrapped().isAuthenticated();
    }

    @Override
    public boolean isAuthenticating() {
        if (getWrapped() == null) {
            return false;
        }
        return getWrapped().isAuthenticating();
    }

    @Override
    public synchronized void cancel() {
        cancelled = true;
        if (getWrapped() != null) {
            getWrapped().cancel();
        }
    }

    @Override
    protected boolean waitForLogin() throws InterruptedException {
        synchronized (this) {
            // wait for a wrapper to show up
            while (getWrapped() == null) {
                wait();
            }
        }
        // when the wrapper changes, we will cancel the pending login
        // to make sure that the call to waitForLogin() returns.  Therefore
        // we ignore when the wrapper returns fals (cancellation), and
        // only pay attention to our own cancelled value.
        while (!isCancelled()) {
            if (getWrapped().waitForLogin()) {
                return true;
            }
        }
        return false;
    }

    protected synchronized void setWrapped(LoginControl wrapped) {
        if (this.wrapped != null) {
            this.wrapped.cancel();
        }
        this.wrapped = wrapped;
        notify();
    }

    protected synchronized LoginControl getWrapped() {
        return wrapped;
    }

    protected synchronized boolean isCancelled() {
        return cancelled;
    }
    
}
