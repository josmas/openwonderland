/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.comms.session;

import org.jdesktop.wonderland.client.comms.LoginFailureException;
import org.jdesktop.wonderland.client.comms.LoginParameters;
import org.jdesktop.wonderland.client.comms.OKErrorResponseListener;
import org.jdesktop.wonderland.client.comms.ResponseListener;
import org.jdesktop.wonderland.client.comms.WonderlandSession.Status;
import org.jdesktop.wonderland.common.comms.messages.SessionInitializationMessage;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ProtocolSelectionMessage;

/**
 * An attempt to log in
 */
class LoginAttempt {
    // parameters to log in with
    private LoginParameters params;
    // whether the login is complete
    private boolean loginComplete;
    // whether the login succeeded
    private boolean loginSuccess;
    // the exception if the login failed
    private LoginFailureException loginException;
    private final WonderlandSessionImpl session;

    /**
     * Create a new login attempt
     * @param params the login parameters
     */
    /**
     * Create a new login attempt
     * @param params the login parameters
     */
    public LoginAttempt(LoginParameters params, final WonderlandSessionImpl session) {
        this.session = session;
        this.params = params;
        loginComplete = false;
        loginSuccess = true;
    }

    /**
     * Get the login parameters
     * @return the login parameters
     */
    public LoginParameters getLoginParameters() {
        return params;
    }

    /**
     * Set a successful result for the login phase.  This will initiate the
     * protocol selection phase.
     */
    public synchronized void setLoginSuccess() {
        ProtocolSelectionMessage psm = new ProtocolSelectionMessage(session.getProtocolName(), session.getProtocolVersion());
        ResponseListener rl = new OKErrorResponseListener() {
            @Override
            public void onSuccess(MessageID messageID) {
                // move to the next phase
                setProtocolSuccess();
            }

            @Override
            public void onFailure(MessageID messageID, String message, Throwable cause) {
                setFailure(message, cause);
            }
        };
        // send the message using the default client
        session.getInternalClient().send(psm, rl);
    }

    /**
     * Set success in the protocol selection phase.
     */
    public synchronized void setProtocolSuccess() {
        SessionInitializationMessage initMessage = null;
        try {
            initMessage = session.getInternalClient().waitForInitialization();
        } catch (InterruptedException ie) {
            // ignore -- treat as a null init message
            // ignore -- treat as a null init message
            // ignore -- treat as a null init message
        }
        if (initMessage == null) {
            // no initialization message means there has been a login
            // problem of some sort
            setFailure("No initialization message.");
        } else {
            // we got an initialization message.  Read the session id
            // and then notify everyone that login has succeeded
            session.setID(initMessage.getSessionID());
            session.setUserID(initMessage.getUserID());
            setSessionInitialized();
        }
    }

    /**
     * Called when we receive a session initialization message
     */
    public synchronized void setSessionInitialized() {
        loginComplete = true;
        loginSuccess = true;
        session.finishLogin(Status.CONNECTED);
        notifyAll();
    }

    /**
     * Set a failed result
     * @param reason the reason for failure
     */
    public synchronized void setFailure(String reason) {
        setFailure(reason, null);
    }

    /**
     * Set a failed result
     * @param reason the reason for failure
     * @param cause the underlying cause of the failure
     */
    public synchronized void setFailure(String reason, Throwable cause) {
        loginComplete = true;
        loginSuccess = false;
        loginException = new LoginFailureException(reason, cause);
        session.finishLogin(Status.DISCONNECTED);
        notifyAll();
    }

    /**
     * Get the result of logging in.  This method blocks until the
     * login and protocol selection succeeds or fails.
     * @return true if everything works, or false if not
     */
    public synchronized LoginResult waitForLogin() throws InterruptedException {
        while (!loginComplete) {
            wait();
        }
        return new LoginResult(loginSuccess, loginException, session);
    }
    
}
