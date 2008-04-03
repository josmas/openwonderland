/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.avatar;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.ViewCreateResponseMessage;

/**
 *
 * @author paulby
 */
public interface ClientView {

    /**
     * Return the id of this view
     * @return
     */
    public String getViewID();
    
    /**
     * Notification that the server view initialization has taken place
     * @param msg
     */
    public void serverInitialized(ViewCreateResponseMessage msg);
    
    /**
     * The ViewCell for this view has been configured on this client
     * @param cell
     */
    public void viewCellConfigured(CellID cellID);
}
