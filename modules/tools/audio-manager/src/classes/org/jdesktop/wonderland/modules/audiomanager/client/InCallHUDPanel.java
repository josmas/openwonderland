/*
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
package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatDialOutMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatHoldMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatLeaveMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;

import org.jdesktop.wonderland.client.softphone.SoftphoneControl;
import org.jdesktop.wonderland.client.softphone.SoftphoneControlImpl;

import org.jdesktop.wonderland.common.auth.WonderlandIdentity;

import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent.ComponentEventType;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

import org.jdesktop.wonderland.modules.audiomanager.common.VolumeUtil;

/**
 *
 * @author  jp
 */
public class InCallHUDPanel extends javax.swing.JPanel implements PresenceManagerListener,
        MemberChangeListener, DisconnectListener {

    private static final Logger logger = Logger.getLogger(InCallHUDPanel.class.getName());
    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo myPresenceInfo;
    private PresenceInfo caller;
    private DefaultListModel userListModel;
    private String group;
    private ChatType chatType;
    private static int groupNumber;
    private static HashMap<String, InCallHUDPanel> inCallHUDPanelMap = new HashMap();
    private HUDComponent inCallHUDComponent;

    private AddInWorldHUDPanel addInWorldHUDPanel;
    private HUDComponent addInWorldHUDComponent;

    private AddExternalHUDPanel addExternalHUDPanel;
    private HUDComponent addExternalHUDComponent;

    private boolean personalPhone;

    /** Creates new form InCallHUDPanel */
    public InCallHUDPanel() {
        initComponents();
    }

    public InCallHUDPanel(AudioManagerClient client, WonderlandSession session,
            PresenceInfo myPresenceInfo, PresenceInfo caller) {

        this(client, session, myPresenceInfo, caller, null);
    }

    public InCallHUDPanel(AudioManagerClient client, WonderlandSession session,
            PresenceInfo myPresenceInfo, PresenceInfo caller, String group) {

        this.client = client;
        this.session = session;
        this.myPresenceInfo = myPresenceInfo;
        this.caller = caller;

        initComponents();

        userListModel = new DefaultListModel();
        userList.setModel(userListModel);
        userList.setCellRenderer(new UserListCellRenderer());

	members.add(myPresenceInfo);

        if (caller.equals(myPresenceInfo) == false) {
            members.add(caller);
            addToUserList(caller);
        }

        hangupButton.setEnabled(false);

        pm = PresenceManagerFactory.getPresenceManager(session);

        pm.addPresenceManagerListener(this);

        client.addDisconnectListener(this);

        if (group == null) {
            group = caller.userID.getUsername() + "-" + groupNumber++;
        }

        this.group = group;

        inCallJLabel.setText("Call in progress: " + group);

        inCallHUDPanelMap.put(group, this);

        client.addMemberChangeListener(group, this);

	privacyDescription.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);

        setVisible(true);
    }

    public void setAddInWorldPanel(AddInWorldHUDPanel addInWorldHUDPanel,
	    HUDComponent addInWorldHUDComponent) {

        this.addInWorldHUDPanel = addInWorldHUDPanel;
	this.addInWorldHUDComponent = addInWorldHUDComponent;
    }

    public void setAddExternalHUDPanel(AddExternalHUDPanel addExternalHUDPanel,
	    HUDComponent addExternalHUDComponent) {

        this.addExternalHUDPanel = addExternalHUDPanel;
        this.addExternalHUDComponent = addExternalHUDComponent;
    }

    public void setClosed() {
	holdHUDPanel = null;
	holdHUDComponent = null;

	inCallHUDComponent.setClosed();
    }

    public void setHUDComponent(HUDComponent inCallHUDComponent) {
        this.inCallHUDComponent = inCallHUDComponent;

        inCallHUDComponent.addComponentListener(new HUDComponentListener() {

	    public void HUDComponentChanged(HUDComponentEvent e) {
                if (e.getEventType().equals(ComponentEventType.CLOSED)) {
            	    session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
                }
            }
        });
    }

    public void callUser(String name, String number) {
	personalPhone = true;

        session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo,
                new PresenceInfo[0], ChatType.PRIVATE));

        SoftphoneControl sc = SoftphoneControlImpl.getInstance();

        String callID = sc.getCallID();

        PresenceInfo info = new PresenceInfo(null, null, new WonderlandIdentity(name, name, null), callID);

	pm.addPresenceInfo(info);

        addToUserList(info);
        session.send(client, new VoiceChatDialOutMessage(group, callID, ChatType.PRIVATE, info, number));
    }

    public void inviteUsers(ArrayList<PresenceInfo> usersToInvite) {
        inviteUsers(usersToInvite, secretRadioButton.isSelected());
    }

    public void inviteUsers(ArrayList<PresenceInfo> usersToInvite, boolean isSecretChat) {
        for (PresenceInfo info : usersToInvite) {
            addToUserList(info);
            invitedMembers.add(info);
        }

        if (isSecretChat) {
            secretRadioButton.setSelected(true);
        } else {
            privateRadioButton.setSelected(true);
        }

        session.send(client, new VoiceChatJoinMessage(group, myPresenceInfo,
            usersToInvite.toArray(new PresenceInfo[0]),
            isSecretChat ? ChatType.SECRET : ChatType.PRIVATE));
    }

    public PresenceInfo getCaller() {
        return caller;
    }

    public String getGroup() {
        return group;
    }

    public HUDComponent getHUDComponent() {
        return inCallHUDComponent;
    }

    public static InCallHUDPanel getInCallHUDPanel(String group) {
        return inCallHUDPanelMap.get(group);
    }

    private void addElement(final String name) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                userListModel.removeElement(name);
                userListModel.addElement(name);
            }
        });
    }

    private void removeElement(final String name) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                userListModel.removeElement(name);
            }
        });
    }

    private void addToUserList(PresenceInfo info) {
        removeFromUserList(info);

        String name = NameTagNode.getDisplayName(info.usernameAlias,
                info.isSpeaking, info.isMuted);

        addElement(name);
    }

    private void removeFromUserList(PresenceInfo info) {
        String name = NameTagNode.getDisplayName(info.usernameAlias, false, false);
        removeElement(name);

        name = NameTagNode.getDisplayName(info.usernameAlias, false, true);
        removeElement(name);

        name = NameTagNode.getDisplayName(info.usernameAlias, true, false);
        removeElement(name);
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
        removeFromUserList(presenceInfo);

        if (members.contains(presenceInfo) == false &&
                invitedMembers.contains(presenceInfo) == false) {

            return;
        }

        if (type.equals(ChangeType.USER_REMOVED)) {
	    if (presenceInfo.clientID == null) {
		if (personalPhone && members.size() == 1) {
            	    session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
		    inCallHUDComponent.setVisible(false);
		}
	    }
	} else {
            addToUserList(presenceInfo);
        }
    }

    private ArrayList<PresenceInfo> members = new ArrayList();
    private ArrayList<PresenceInfo> invitedMembers = new ArrayList();

    public void setMemberList(PresenceInfo[] memberList) {
    }

    public void memberChange(PresenceInfo member, boolean added) {
        invitedMembers.remove(member);

        if (added == true) {
	    if (members.contains(member) == false) {
                members.add(member);
	    } 

            addToUserList(member);
            return;
        }

        synchronized (members) {
            members.remove(member);
        }

        removeFromUserList(member);

	if (personalPhone && members.size() == 1) {
	    session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
	    inCallHUDComponent.setVisible(false);
	}
    }

    public void disconnected() {
        inCallHUDPanelMap.remove(group);
        inCallHUDComponent.setClosed();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        inCallJLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        secretRadioButton = new javax.swing.JRadioButton();
        privateRadioButton = new javax.swing.JRadioButton();
        addInWorldButton = new javax.swing.JButton();
        hangupButton = new javax.swing.JButton();
        holdButton = new javax.swing.JButton();
        speakerPhoneRadioButton = new javax.swing.JRadioButton();
        privacyDescription = new javax.swing.JLabel();
        addExternalButton = new javax.swing.JButton();
        groupNameTextField = new javax.swing.JLabel();

        setRequestFocusEnabled(false);

        inCallJLabel.setFont(inCallJLabel.getFont().deriveFont(inCallJLabel.getFont().getStyle() | java.awt.Font.BOLD));
        inCallJLabel.setText("Call in progress: ");

        userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userList);

        buttonGroup1.add(secretRadioButton);
        secretRadioButton.setFont(secretRadioButton.getFont());
        secretRadioButton.setText("Secret");
        secretRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secretRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(privateRadioButton);
        privateRadioButton.setFont(privateRadioButton.getFont());
        privateRadioButton.setSelected(true);
        privateRadioButton.setText("Private");
        privateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateRadioButtonActionPerformed(evt);
            }
        });

        addInWorldButton.setText("Add In-World");
        addInWorldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addInWorldButtonActionPerformed(evt);
            }
        });

        hangupButton.setText("Hang up");
        hangupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hangupButtonActionPerformed(evt);
            }
        });

        holdButton.setText("Hold");
        holdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holdButtonActionPerformed(evt);
            }
        });

        speakerPhoneRadioButton.setText("SpeakerPhone");
        speakerPhoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speakerPhoneRadioButtonActionPerformed(evt);
            }
        });

        addExternalButton.setText("Add External");
        addExternalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addExternalButtonActionPerformed(evt);
            }
        });

        groupNameTextField.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(privacyDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(holdButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(addExternalButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(addInWorldButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(hangupButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(privateRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(secretRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(speakerPhoneRadioButton))
                            .add(inCallJLabel))
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                            .add(groupNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(24, 24, 24))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inCallJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(groupNameTextField))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(privateRadioButton)
                    .add(secretRadioButton)
                    .add(speakerPhoneRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(privacyDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(addExternalButton)
                    .add(addInWorldButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hangupButton)
                    .add(holdButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
    setEnableHangupButton();
}//GEN-LAST:event_userListValueChanged

private void addInWorldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addInWorldButtonActionPerformed
    if (addInWorldHUDPanel != null) {
        addInWorldHUDComponent.setVisible(true);
        return;
    }

    addInWorldHUDPanel = new AddInWorldHUDPanel(client, session, myPresenceInfo, this);

    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
    addInWorldHUDComponent = mainHUD.createComponent(addInWorldHUDPanel);

    addInWorldHUDPanel.setHUDComponent(addInWorldHUDComponent);

    //System.out.println("Call in progress x,y " + inCallHUDComponent.getX() + ", " + inCallHUDComponent.getY()
    //    + " width " + inCallHUDComponent.getWidth() + " height " + inCallHUDComponent.getHeight()
    //    + " Call x,y " + (inCallHUDComponent.getX() + inCallHUDComponent.getWidth())
    //    + ", " + (inCallHUDComponent.getY() + inCallHUDComponent.getHeight() - addInWorldHUDComponent.getHeight()));

    mainHUD.addComponent(addInWorldHUDComponent);
    addInWorldHUDComponent.addComponentListener(new HUDComponentListener() {

        public void HUDComponentChanged(HUDComponentEvent e) {
            if (e.getEventType().equals(ComponentEventType.CLOSED)) {
		addInWorldHUDPanel = null;
		addInWorldHUDComponent = null;
            }
        }
    });

    addInWorldHUDComponent.setVisible(true);
    addInWorldHUDComponent.setLocation(inCallHUDComponent.getX() + inCallHUDComponent.getWidth(),
            inCallHUDComponent.getY() + inCallHUDComponent.getHeight() - addInWorldHUDComponent.getHeight());

    PropertyChangeListener plistener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent pe) {
            if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                addInWorldHUDComponent.setVisible(false);
            }
        }
    };
    addInWorldHUDPanel.addPropertyChangeListener(plistener);
    addInWorldHUDComponent.setVisible(true);
}//GEN-LAST:event_addInWorldButtonActionPerformed

private void hangupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hangupButtonActionPerformed
    hangup();
}//GEN-LAST:event_hangupButtonActionPerformed

    private void changePrivacy(ChatType chatType) {
        ArrayList<PresenceInfo> membersInfo = getSelectedMembers();

        for (PresenceInfo info : membersInfo) {
            session.send(client, new VoiceChatJoinMessage(group, info, new PresenceInfo[0], chatType));
        }
    }
    private HoldHUDPanel holdHUDPanel;
    private HUDComponent holdHUDComponent;
    private boolean onHold = false;

private void holdButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_holdButtonActionPerformed
    onHold = !onHold;

    hold(onHold);
}//GEN-LAST:event_holdButtonActionPerformed

private void addExternalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addExternalButtonActionPerformed
    if (addExternalHUDPanel != null) {
        addExternalHUDComponent.setVisible(true);
        return;
    }

    addExternalHUDPanel = new AddExternalHUDPanel(client, session, myPresenceInfo, this);

    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
    addExternalHUDComponent = mainHUD.createComponent(addExternalHUDPanel);

    addExternalHUDPanel.setHUDComponent(addExternalHUDComponent);

    //System.out.println("Call in progress x,y " + inCallHUDComponent.getX() + ", " + inCallHUDComponent.getY()
    //    + " width " + inCallHUDComponent.getWidth() + " height " + inCallHUDComponent.getHeight()
    //    + " Call x,y " + (inCallHUDComponent.getX() + inCallHUDComponent.getWidth())
    //    + ", " + (inCallHUDComponent.getY() + inCallHUDComponent.getHeight() - addExternalHUDComponent.getHeight()));

    mainHUD.addComponent(addExternalHUDComponent);
    addExternalHUDComponent.addComponentListener(new HUDComponentListener() {

        public void HUDComponentChanged(HUDComponentEvent e) {
            if (e.getEventType().equals(ComponentEventType.CLOSED)) {
		addExternalHUDPanel = null;
		addExternalHUDComponent = null;
            }
        }
    });

    addExternalHUDComponent.setVisible(true);
    addExternalHUDComponent.setLocation(inCallHUDComponent.getX() + inCallHUDComponent.getWidth(),
            inCallHUDComponent.getY() + inCallHUDComponent.getHeight() - addExternalHUDComponent.getHeight());

    PropertyChangeListener plistener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent pe) {
            if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
            }
        }
    };
    addExternalHUDPanel.addPropertyChangeListener(plistener);
    addExternalHUDComponent.setVisible(true);
}//GEN-LAST:event_addExternalButtonActionPerformed

private void speakerPhoneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speakerPhoneRadioButtonActionPerformed
    chatType = ChatType.PUBLIC;
    privacyDescription.setText(VoiceChatMessage.PUBLIC_DESCRIPTION);
}//GEN-LAST:event_speakerPhoneRadioButtonActionPerformed

private void secretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secretRadioButtonActionPerformed
    chatType = ChatType.SECRET;
    privacyDescription.setText(VoiceChatMessage.SECRET_DESCRIPTION);
}//GEN-LAST:event_secretRadioButtonActionPerformed

private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
    chatType = ChatType.PRIVATE;
    privacyDescription.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);
}//GEN-LAST:event_privateRadioButtonActionPerformed

    private void hold(boolean onHold) {
        if (holdHUDPanel == null) {
            if (onHold == false) {
                return;
            }

            holdHUDPanel = new HoldHUDPanel(client, session, group, this, myPresenceInfo);

            HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
            holdHUDComponent = mainHUD.createComponent(holdHUDPanel);
            holdHUDComponent.setPreferredLocation(Layout.SOUTHWEST);

	    holdHUDPanel.setHUDComponent(holdHUDComponent);

            mainHUD.addComponent(holdHUDComponent);
            holdHUDComponent.addComponentListener(new HUDComponentListener() {

                public void HUDComponentChanged(HUDComponentEvent e) {
                    if (e.getEventType().equals(ComponentEventType.DISAPPEARED)) {
                    }
                }
            });

            PropertyChangeListener plistener = new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent pe) {
                    if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                        holdHUDComponent.setVisible(false);
                    }
                }
            };
            holdHUDPanel.addPropertyChangeListener(plistener);
        }

        holdHUDComponent.setVisible(onHold);

        inCallHUDComponent.setVisible(!onHold);
        setHold(onHold, 1);
    }

    public void setHold(boolean onHold, double volume) {
        this.onHold = onHold;

        try {
            session.send(client, new VoiceChatHoldMessage(group, myPresenceInfo, onHold,
                    VolumeUtil.getServerVolume(volume)));

            if (onHold == false) {
                holdOtherCalls();
            }

            inCallHUDComponent.setVisible(!onHold);
            holdHUDComponent.setVisible(onHold);
        } catch (IllegalStateException e) {
            hangup();
        }
    }

    public void holdOtherCalls() {
        InCallHUDPanel[] inCallHUDPanels = inCallHUDPanelMap.values().toArray(new InCallHUDPanel[0]);

        for (int i = 0; i < inCallHUDPanels.length; i++) {
            if (inCallHUDPanels[i] == this) {
                continue;
            }

            inCallHUDPanels[i].hold(true);
        }
    }

    private ArrayList<PresenceInfo> getSelectedMembers() {
        Object[] selectedValues = userList.getSelectedValues();

        ArrayList<PresenceInfo> membersInfo = new ArrayList();

        for (int i = 0; i < selectedValues.length; i++) {
            String usernameAlias = NameTagNode.getUsername((String) selectedValues[i]);

            PresenceInfo info = pm.getAliasPresenceInfo(usernameAlias);

            if (info == null) {
                logger.warning("No presence info for " + (String) selectedValues[i]);
                continue;
            }

            membersInfo.add(info);
        }

        return membersInfo;
    }

    private void setEnableHangupButton() {
        ArrayList<PresenceInfo> membersInfo = getSelectedMembers();

        for (PresenceInfo info : membersInfo) {
            /*
             * You can only select yourself or outworlders
             */
            if (info.clientID != null && myPresenceInfo.equals(info) == false) {
                hangupButton.setEnabled(false);
                return;
            }
        }

        hangupButton.setEnabled(true);
    }

    private void hangup() {
        ArrayList<PresenceInfo> membersInfo = getSelectedMembers();

        boolean hide = false;

        for (PresenceInfo info : membersInfo) {
            session.send(client, new VoiceChatLeaveMessage(group, info));
            if (info.equals(myPresenceInfo)) {
                hide = true;
            }
        }

        if (hide) {
            inCallHUDComponent.setVisible(false);
            inCallHUDPanelMap.remove(group);
        }
    }

    public void endHeldCall() {
        session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
        inCallHUDPanelMap.remove(group);
        inCallHUDComponent.setVisible(false);
    }

    private class UserListCellRenderer implements ListCellRenderer {

        protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
        private Font font = new Font("SansSerif", Font.PLAIN, 13);

        public Component getListCellRendererComponent(JList list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);

            String usernameAlias = NameTagNode.getUsername((String) value);

            PresenceInfo info = pm.getAliasPresenceInfo(usernameAlias);

            if (info == null) {
                logger.warning("No presence info for " + usernameAlias);
                return renderer;
            }

            if (members.contains(info)) {
                renderer.setFont(font);
                renderer.setForeground(Color.BLACK);
            } else {
                renderer.setFont(font);
                renderer.setForeground(Color.BLUE);
            }
            return renderer;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addExternalButton;
    private javax.swing.JButton addInWorldButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel groupNameTextField;
    private javax.swing.JButton hangupButton;
    private javax.swing.JButton holdButton;
    private javax.swing.JLabel inCallJLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel privacyDescription;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JRadioButton speakerPhoneRadioButton;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables
}
