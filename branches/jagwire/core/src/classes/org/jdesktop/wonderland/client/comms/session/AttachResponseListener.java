/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.comms.session;

import org.jdesktop.wonderland.client.comms.ConnectionFailureException;
import org.jdesktop.wonderland.client.comms.WaitResponseListener;
import org.jdesktop.wonderland.common.comms.messages.AttachedClientMessage;
import org.jdesktop.wonderland.common.messages.ErrorMessage;
import org.jdesktop.wonderland.common.messages.ResponseMessage;

/**
 * Listen for responses to the connect() message.
 */
class AttachResponseListener extends WaitResponseListener {
    /** the record to update on success */
    private ClientRecord record;
    /** whether or not we succeeded */
    private boolean success = false;
    /** the exception if we failed */
    private ConnectionFailureException exception;
    private final WonderlandSessionImpl session;

    public AttachResponseListener(ClientRecord record, final WonderlandSessionImpl session) {
        this.session = session;
        this.record = record;
    }

    @Override
    public void responseReceived(ResponseMessage response) {
        if (response instanceof AttachedClientMessage) {
            AttachedClientMessage acm = (AttachedClientMessage) response;
            // set the client id
            session.setClientID(record, acm.getClientID());
            // notify the client that we are now connected
            record.getClient().connected(session);
            // success
            setSuccess(true);
        } else if (response instanceof ErrorMessage) {
            // error -- throw an exception
            ErrorMessage e = (ErrorMessage) response;
            setException(new ConnectionFailureException(e.getErrorMessage(), e.getErrorCause()));
        } else {
            // bad situation
            setException(new ConnectionFailureException("Unexpected response " + "type: " + response));
        }
        super.responseReceived(response);
    }

    public synchronized boolean isSuccess() {
        return success;
    }

    private synchronized void setSuccess(boolean success) {
        this.success = success;
    }

    public synchronized ConnectionFailureException getException() {
        return exception;
    }

    public synchronized void setException(ConnectionFailureException exception) {
        this.exception = exception;
    }
    
}
