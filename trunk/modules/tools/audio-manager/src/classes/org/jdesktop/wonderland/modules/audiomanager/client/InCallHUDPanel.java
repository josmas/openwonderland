/*
 * InCallHUDPanel.java
 *
 * Created on July 10, 2009, 11:06 AM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatDialOutMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatHoldMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatJoinMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatLeaveMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatMessage.ChatType;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Logger;

import java.awt.Point;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;

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

    private static int groupNumber;

    private static HashMap<String, InCallHUDPanel> inCallHUDPanelMap = new HashMap();

    private HUDComponent inCallHUDComponent;

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

	members.add(myPresenceInfo);
	addToUserList(myPresenceInfo);

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

	inCallJLabel.setText("InCall " + group);

	inCallHUDPanelMap.put(group, this);

	client.addMemberChangeListener(group, this);

        setVisible(true);
    }

    public void setCallHUDPanel(CallHUDPanel callHUDPanel) {
	this.callHUDPanel = callHUDPanel;
    }

    public void setHUDComponent(HUDComponent inCallHUDComponent) {
	this.inCallHUDComponent = inCallHUDComponent;
    }

    public void callUser(String name, String number) {
	SoftphoneControl sc = SoftphoneControlImpl.getInstance();

	String callID = sc.getCallID();

	PresenceInfo info = new PresenceInfo(null, null, new WonderlandIdentity(name, name, null), callID);

	addToUserList(info);
        session.send(client, new VoiceChatDialOutMessage(group, callID, ChatType.PRIVATE, info, number));
    }

    public void inviteUsers(ArrayList<PresenceInfo> usersToInvite) {
	inviteUsers(usersToInvite, secretRadioButton.isSelected());
    }

    public void inviteUsers(ArrayList<PresenceInfo> usersToInvite, boolean isSecretChat) {
	for (PresenceInfo info : usersToInvite) {
	    addToUserList(info);
	    members.add(info);
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

    private void addToUserList(PresenceInfo info) {
	removeFromUserList(info);

	String name = NameTagNode.getDisplayName(info.usernameAlias,
	    info.isSpeaking, info.isMuted);

	System.out.println("ADDING " + name);

	synchronized (userListModel) {
	    userListModel.addElement(name);
	}
    }

    private void removeFromUserList(PresenceInfo info) {
	synchronized (userListModel) {
	    String name = NameTagNode.getDisplayName(info.usernameAlias, false, false);
            userListModel.removeElement(name);

	    name = NameTagNode.getDisplayName(info.usernameAlias, false, true);
            userListModel.removeElement(name);

	    name = NameTagNode.getDisplayName(info.usernameAlias, true, false);
            userListModel.removeElement(name);

	    name = NameTagNode.getDisplayName(info.usernameAlias, true, true);
            userListModel.removeElement(name);
	}
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
	removeFromUserList(presenceInfo);

	if (members.contains(presenceInfo) == false) {
	    return;
	}
 
	if (type.equals(ChangeType.USER_REMOVED) == false) {
	    addToUserList(presenceInfo);
	}
    }

    private ArrayList<PresenceInfo> members = new ArrayList();

    public void setMemberList(PresenceInfo[] memberList) {
    }

    public void memberChange(PresenceInfo member, boolean added) {
	System.out.println("memberChange " + member + " added " + added);

	if (added == true) {
	    return;
	}

	synchronized (members) {
	    members.remove(member);
	}
	removeFromUserList(member);
    }

    public void disconnected() {
	inCallHUDPanelMap.remove(group);
        inCallHUDComponent.setVisible(false);
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
        jLabel2 = new javax.swing.JLabel();
        secretRadioButton = new javax.swing.JRadioButton();
        privateRadioButton = new javax.swing.JRadioButton();
        addButton = new javax.swing.JButton();
        hangupButton = new javax.swing.JButton();
        speakerButton = new javax.swing.JButton();
        holdButton = new javax.swing.JButton();
        chatTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();

        inCallJLabel.setText("In Call");

        userList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userList);

        jLabel2.setText("Change Privacy:");

        buttonGroup1.add(secretRadioButton);
        secretRadioButton.setText("Secret");

        buttonGroup1.add(privateRadioButton);
        privateRadioButton.setText("Private");

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        hangupButton.setText("Hang up");
        hangupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hangupButtonActionPerformed(evt);
            }
        });

        speakerButton.setText("Speaker");
        speakerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speakerButtonActionPerformed(evt);
            }
        });

        holdButton.setText("Hold");
        holdButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                holdButtonActionPerformed(evt);
            }
        });

        chatTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chatTextFieldActionPerformed(evt);
            }
        });

        jLabel3.setText("Text:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(123, 123, 123)
                        .add(inCallJLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(60, 60, 60)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(layout.createSequentialGroup()
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(secretRadioButton)
                                .add(18, 18, 18)
                                .add(privateRadioButton))
                            .add(layout.createSequentialGroup()
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(addButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(speakerButton))
                                        .add(63, 63, 63)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                            .add(holdButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .add(hangupButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)))
                                    .add(chatTextField))))))
                .add(53, 53, 53))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(inCallJLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(secretRadioButton)
                    .add(privateRadioButton))
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hangupButton)
                    .add(addButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(speakerButton)
                    .add(holdButton))
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(chatTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
    setEnableHangupButton();
}//GEN-LAST:event_userListValueChanged

private CallHUDPanel callHUDPanel;
private HUDComponent callHUDComponent;

private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
    if (callHUDPanel != null) {
        callHUDComponent.setVisible(true);
        return;
    }

    callHUDPanel = new CallHUDPanel(client, session, myPresenceInfo, this);

    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
    callHUDComponent = mainHUD.createComponent(callHUDPanel);

    callHUDPanel.setHUDComponent(callHUDComponent);

    //System.out.println("InCall x,y " + inCallHUDComponent.getX() + ", " + inCallHUDComponent.getY()
    //    + " width " + inCallHUDComponent.getWidth() + " height " + inCallHUDComponent.getHeight()
    //    + " Call x,y " + (inCallHUDComponent.getX() + inCallHUDComponent.getWidth())
    //    + ", " + (inCallHUDComponent.getY() + inCallHUDComponent.getHeight() - callHUDComponent.getHeight()));

    mainHUD.addComponent(callHUDComponent);
    callHUDComponent.addComponentListener(new HUDComponentListener() {
        public void HUDComponentChanged(HUDComponentEvent e) {
            if (e.getEventType().equals(ComponentEventType.DISAPPEARED)) {
            }
        }
    });

    callHUDComponent.setVisible(true);
    callHUDComponent.setLocation(inCallHUDComponent.getX() + inCallHUDComponent.getWidth(), 
	inCallHUDComponent.getY() + inCallHUDComponent.getHeight() - callHUDComponent.getHeight());

    PropertyChangeListener plistener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent pe) {
            if (pe.getPropertyName().equals("ok") || pe.getPropertyName().equals("cancel")) {
                callHUDComponent.setVisible(false);
            }
        }
    };
    callHUDPanel.addPropertyChangeListener(plistener);
    callHUDComponent.setVisible(true);
}//GEN-LAST:event_addButtonActionPerformed

private void hangupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hangupButtonActionPerformed
    hangup();
}//GEN-LAST:event_hangupButtonActionPerformed

private void speakerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speakerButtonActionPerformed
    changePrivacy(ChatType.PUBLIC);
}//GEN-LAST:event_speakerButtonActionPerformed

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

private void chatTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chatTextFieldActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_chatTextFieldActionPerformed

private void hold(boolean onHold) {
    if (holdHUDPanel == null) {
        if (onHold == false) {
            return;
        }

        holdHUDPanel = new HoldHUDPanel(client, session, group, this);

        HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
        holdHUDComponent = mainHUD.createComponent(holdHUDPanel);
        holdHUDComponent.setPreferredLocation(Layout.SOUTHWEST);

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

            PresenceInfo[] info = pm.getAliasPresenceInfo(usernameAlias);

            if (info == null || info.length == 0) {
                System.out.println("No presence info for " + (String) selectedValues[i]);
                continue;
            }

            membersInfo.add(info[0]);
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
	    System.out.println("Removing map entry for " + group);
	    inCallHUDPanelMap.remove(group);
	}
    }

    public void endHeldCall() {
        session.send(client, new VoiceChatLeaveMessage(group, myPresenceInfo));
        inCallHUDPanelMap.remove(group);
        inCallHUDComponent.setVisible(false);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField chatTextField;
    private javax.swing.JButton hangupButton;
    private javax.swing.JButton holdButton;
    private javax.swing.JLabel inCallJLabel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JButton speakerButton;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables

}
