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
package org.jdesktop.wonderland.modules.avatarbase.common.cell.messages;

import java.net.URL;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * A message indicating that the avatar model has been updated.
 *
 * @author paulby
 */
public class AvatarConfigMessage extends CellMessage {

    public enum ActionType { REQUEST, APPLY };
    private ActionType actionType;

    // The URL of the configuration file on the server that describes the
    // avatar. If null, use the "default" avatar.
    private String modelConfigURL = null;

    AvatarConfigMessage(ActionType actionType, String modelConfigURL) {
        this.modelConfigURL = modelConfigURL;
        this.actionType = actionType;
    }

    public String getModelConfigURL() {
        return modelConfigURL;
    }

    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Given a request message, return appropriate apply message
     * @param requestMessage
     * @return
     */
    public static AvatarConfigMessage newApplyMessage(AvatarConfigMessage requestMessage) {
        return new AvatarConfigMessage(ActionType.APPLY, requestMessage.getModelConfigURL());
    }

    public static AvatarConfigMessage newRequestMessage(URL newURL) {
        String url = (newURL != null) ? newURL.toExternalForm() : null;
        return new AvatarConfigMessage(ActionType.REQUEST, url);
    }
}
