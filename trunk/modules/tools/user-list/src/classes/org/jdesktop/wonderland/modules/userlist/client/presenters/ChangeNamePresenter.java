/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above copyright and
 * this condition.
 *
 * The contents of this file are subject to the GNU General Public License,
 * Version 2 (the "License"); you may not use this file except in compliance
 * with the License. A copy of the License is available at
 * http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as subject to
 * the "Classpath" exception as provided by the Open Wonderland Foundation in
 * the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.userlist.client.presenters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.userlist.client.WonderlandUserList;
import org.jdesktop.wonderland.modules.userlist.client.views.ChangeNameView;

/**
 *
 * @author Ryan
 */
public class ChangeNamePresenter {
    
    private ChangeNameView view;
    private WonderlandUserList model;
    private WonderlandUserListPresenter userListPresenter = null;
    private HUDComponent hudComponent;
    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/userlist/client/resources/Bundle");
    
    
    
    public ChangeNamePresenter(WonderlandUserListPresenter userListPresenter, ChangeNameView view, HUDComponent c) {
        this.view = view;
        this.model = WonderlandUserList.INSTANCE;
        this.userListPresenter = userListPresenter;
        this.hudComponent = c;
        addListeners();
    }
    
    public void setVisible(boolean visible) {
        hudComponent.setVisible(visible);
    }
    
    public void handleAliasTextActionPerformed() {
        //when the user enters text and presses enter, it's the same as pressing
        //the OK Button
        handleOKButtonPressed();
    }
    
    public void handleOKButtonPressed() {
        PresenceInfo[] infos = model.getAllUsers();

        String alias = view.getAliasFieldText();

        PresenceInfo localInfo = model.getLocalPresenceInfo();
        
        for(PresenceInfo info: infos ) {
            if(info.getUsernameAlias().equals(alias)
                    || info.getUserID().getUsername().equals(alias)) {
                if(!localInfo.equals(info)) {
                    view.setStatusLabel(BUNDLE.getString("Alias_Used"));
                    return;
                }
            }
        }
        
        view.setStatusLabel("");

        localInfo.setUsernameAlias(view.getAliasFieldText());
        model.requestChangeUsernameAlias(localInfo.getUsernameAlias());
        userListPresenter.changeUsernameAlias(localInfo);
        setVisible(false);
    }
    
    public void handleCancelButtonPressed() {
        setVisible(false);
    }
  
    
    private void addListeners() {
        view.addAliasTextFormActionListener(new ActionListener() {                       
            public void actionPerformed(ActionEvent event) {
                handleAliasTextActionPerformed();
            }
        });
        
        view.addOKButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handleOKButtonPressed();
            }
        });
        
        view.addCancelButtonActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                handleCancelButtonPressed();
            }
        });
        
    }
}
