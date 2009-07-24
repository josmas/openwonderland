/*
 * AddHUDPanel.java
 *
 * Created on July 24, 2009, 10:20 AM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

import java.util.logging.Logger;

import java.util.ArrayList;

import javax.swing.DefaultListModel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener.ChangeType;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatInfoRequestMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage;
import org.jdesktop.wonderland.modules.audiomanager.common.messages.voicechat.VoiceChatMessage.ChatType;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDEvent;
import org.jdesktop.wonderland.client.hud.HUDEvent.HUDEventType;
import org.jdesktop.wonderland.client.hud.HUDEventListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/**
 *
 * @author  jp
 */
public class AddHUDPanel extends javax.swing.JPanel implements PresenceManagerListener,
        MemberChangeListener, DisconnectListener {

    private static final Logger logger = Logger.getLogger(AddHUDPanel.class.getName());

    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo myPresenceInfo;

    private DefaultListModel userListModel;

    private InCallHUDPanel inCallHUDPanel;
    private HUDComponent inCallHUDComponent;

    private PresenceInfo caller;
    private String group;

    private ChatType chatType;

    private PropertyChangeSupport listeners;

    private HUDComponent addHUDComponent;

    private String name = "Add Users";

    /** Creates new form AddHUDPanel */
    public AddHUDPanel() {
        initComponents();
    }

    public AddHUDPanel(AudioManagerClient client, WonderlandSession session,
	    PresenceInfo myPresenceInfo) {

	this(client, session, myPresenceInfo, null);

	name = "Voice Chat";

	privateRadioButton.setVisible(true);
	secretRadioButton.setVisible(true);
	speakerPhoneRadioButton.setVisible(true);
	privacyDescriptionLabel.setVisible(true);

	phonePrivateRadioButton.setVisible(true);
	phoneSecretRadioButton.setVisible(true);
	phoneSpeakerPhoneRadioButton.setVisible(true);
	phonePrivacyDescriptionLabel.setVisible(true);
    }

    public AddHUDPanel(AudioManagerClient client, WonderlandSession session,
	    PresenceInfo myPresenceInfo, InCallHUDPanel inCallPanel) {

	this.client = client;
	this.session = session;
        this.myPresenceInfo = myPresenceInfo;
	this.inCallHUDPanel = inCallPanel;

        initComponents();

        userListModel = new DefaultListModel();
        userList.setModel(userListModel);

	if (inCallPanel == null) {
            this.inCallHUDPanel = new InCallHUDPanel(client, session, myPresenceInfo, myPresenceInfo);

	    HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
	    inCallHUDComponent = mainHUD.createComponent(this.inCallHUDPanel);

	    this.inCallHUDPanel.setHUDComponent(inCallHUDComponent);

	    mainHUD.addComponent(inCallHUDComponent);

	    inCallHUDComponent.addEventListener(new HUDEventListener() {
	        public void HUDObjectChanged(HUDEvent e) {
	            if (e.getEventType().equals(HUDEventType.CLOSED)) {
			inCallHUDPanel = null;
			inCallHUDComponent = null;
	            }
	        }
	    });

            //inCallHUDComponent.setVisible(true);

            //System.out.println("Call x,y " + addHUDComponent.getX() + ", " + addHUDComponent.getY()
            //    + " width " + addHUDComponent.getWidth() + " height " + addHUDComponent.getHeight()
            //    + " Incall x,y " + (addHUDComponent.getX() - addHUDComponent.getWidth())
            //    + ", " + (addHUDComponent.getY() + addHUDComponent.getHeight() - inCallHUDComponent.getHeight()));
	}

        caller = this.inCallHUDPanel.getCaller();
	group = this.inCallHUDPanel.getGroup();

	inCallHUDComponent = this.inCallHUDPanel.getHUDComponent();

	client.addMemberChangeListener(group, this);

        pm = PresenceManagerFactory.getPresenceManager(session);
	pm.addPresenceManagerListener(this);

	privacyDescriptionLabel.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);
	phonePrivacyDescriptionLabel.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);

	session.send(client, new VoiceChatInfoRequestMessage(group));

	privateRadioButton.setVisible(false);
	secretRadioButton.setVisible(false);
	speakerPhoneRadioButton.setVisible(false);
	privacyDescriptionLabel.setVisible(false);

	phonePrivateRadioButton.setVisible(false);
	phoneSecretRadioButton.setVisible(false);
	phoneSpeakerPhoneRadioButton.setVisible(false);
	phonePrivacyDescriptionLabel.setVisible(false);

	inviteButton.setEnabled(false);
	callButton.setEnabled(false);
	setVisible(true);
    }

    public void setHUDComponent(HUDComponent addHUDComponent) {
	this.addHUDComponent = addHUDComponent;
	addHUDComponent.setName(name);
	inCallHUDPanel.setAddPanel(this, addHUDComponent);
    }

    /**
     * Adds a bound property listener to the dialog
     * @param listener a listener for dialog events
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listeners == null) {
            listeners = new PropertyChangeSupport(this);
        }
        listeners.addPropertyChangeListener(listener);
    }

    /**
     * Removes a bound property listener from the dialog
     * @param listener the listener to remove
     */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listeners != null) {
            listeners.removePropertyChangeListener(listener);
        }
    }

    private void addElement(final String usernameAlias) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		userListModel.removeElement(usernameAlias);
		userListModel.addElement(usernameAlias);
            }
	});
    }

    private void removeElement(final String usernameAlias) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		userListModel.removeElement(usernameAlias);
            }
	});
    }

    private void addToUserList(final PresenceInfo presenceInfo) {
	if (presenceInfo.equals(myPresenceInfo)) {
	    return;
	}

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		removeElement(presenceInfo.usernameAlias);
		addElement(presenceInfo.usernameAlias);
            }
        });
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
	if (type.equals(ChangeType.USER_REMOVED)) {
	    removeElement(presenceInfo.usernameAlias);
	} else if (type.equals(ChangeType.USER_ADDED)) {
	    if (members.contains(presenceInfo)) {
		removeElement(presenceInfo.usernameAlias);
	    } else {
		addToUserList(presenceInfo);
	    }
	} 
    }

    ArrayList<PresenceInfo> members = new ArrayList();

    public void memberChange(PresenceInfo presenceInfo, boolean added) {
	logger.finer("memberChange " + presenceInfo + " added " + added);

	if (added) {
	    if (members.contains(presenceInfo)) {
		return;
	    }

	    members.add(presenceInfo);
	    removeElement(presenceInfo.usernameAlias);
	} else {
	    members.remove(presenceInfo);
	    synchronized (userListModel) {
		addToUserList(presenceInfo);
	    }
	}
    }

    public void setMemberList(PresenceInfo[] memberList) {
	members.clear();

	for (int i = 0; i < memberList.length; i++) {
	    if (members.contains(memberList[i]) == false) {
	        members.add(memberList[i]);
	    }
	}

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		userListModel.clear();
		PresenceInfo[] presenceInfoList = pm.getAllUsers();

	    	for (int i = 0; i < presenceInfoList.length; i++) {
		    PresenceInfo info = presenceInfoList[i];

	            if (members.contains(info)) {
		        removeElement(info.usernameAlias);
		    } else {
		        addElement(info.usernameAlias);
		    }
	        }
	    }
        });
    }

    public void disconnected() {
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	addHUDComponent.setVisible(false);
            }
        });
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
        buttonGroup2 = new javax.swing.ButtonGroup();
        phoneUserTab = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        phoneNumberLabel = new javax.swing.JLabel();
        phoneNumberTextField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        phonePrivateRadioButton = new javax.swing.JRadioButton();
        phoneSecretRadioButton = new javax.swing.JRadioButton();
        phoneSpeakerPhoneRadioButton = new javax.swing.JRadioButton();
        phonePrivacyDescriptionLabel = new javax.swing.JLabel();
        cancelCallButton = new javax.swing.JButton();
        callButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        privateRadioButton = new javax.swing.JRadioButton();
        secretRadioButton = new javax.swing.JRadioButton();
        speakerPhoneRadioButton = new javax.swing.JRadioButton();
        privacyDescriptionLabel = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        inviteButton = new javax.swing.JButton();

        phoneUserTab.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                phoneUserTabStateChanged(evt);
            }
        });

        nameLabel.setText("Name:");

        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyReleased(evt);
            }
        });

        phoneNumberLabel.setText("Phone #:");

        phoneNumberTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberTextFieldActionPerformed(evt);
            }
        });
        phoneNumberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                phoneNumberTextFieldKeyReleased(evt);
            }
        });

        phonePrivateRadioButton.setSelected(true);
        phonePrivateRadioButton.setText("Private");
        phonePrivateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phonePrivateRadioButtonActionPerformed(evt);
            }
        });

        phoneSecretRadioButton.setText("Secret");
        phoneSecretRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneSecretRadioButtonActionPerformed(evt);
            }
        });

        phoneSpeakerPhoneRadioButton.setText("SpeakerPhone");
        phoneSpeakerPhoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneSpeakerPhoneRadioButtonActionPerformed(evt);
            }
        });

        cancelCallButton.setText("Cancel");
        cancelCallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelCallButtonActionPerformed(evt);
            }
        });

        callButton.setText("Call");
        callButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(phoneNumberLabel)
                            .add(nameLabel))
                        .add(6, 6, 6)
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(phoneNumberTextField)
                            .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                            .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 202, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(21, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(phonePrivateRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 6, Short.MAX_VALUE)
                                .add(phoneSecretRadioButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(phoneSpeakerPhoneRadioButton))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                                .add(29, 29, 29)
                                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jPanel2Layout.createSequentialGroup()
                                        .add(6, 6, 6)
                                        .add(cancelCallButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(callButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(phonePrivacyDescriptionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 234, Short.MAX_VALUE))))
                        .add(49, 49, 49))))
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {nameTextField, phoneNumberTextField}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.linkSize(new java.awt.Component[] {callButton, cancelCallButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phoneNumberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(phoneNumberLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(phonePrivateRadioButton)
                    .add(phoneSecretRadioButton)
                    .add(phoneSpeakerPhoneRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(phonePrivacyDescriptionLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelCallButton)
                    .add(callButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        phoneUserTab.addTab("Phone User", jPanel2);

        userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userList);

        buttonGroup1.add(privateRadioButton);
        privateRadioButton.setSelected(true);
        privateRadioButton.setText("Private");
        privateRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(secretRadioButton);
        secretRadioButton.setText("Secret");
        secretRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secretRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(speakerPhoneRadioButton);
        speakerPhoneRadioButton.setText("SpeakerPhone");
        speakerPhoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speakerPhoneRadioButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        inviteButton.setText("Invite");
        inviteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inviteButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(privateRadioButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(secretRadioButton)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(speakerPhoneRadioButton))
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(37, 37, 37)
                            .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(privacyDescriptionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                                    .add(cancelButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                    .add(inviteButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(40, 40, 40))))))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {cancelButton, inviteButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(privateRadioButton)
                    .add(secretRadioButton)
                    .add(speakerPhoneRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(privacyDescriptionLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inviteButton)
                    .add(cancelButton))
                .addContainerGap())
        );

        phoneUserTab.addTab("Add Wonderland User", jPanel1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, phoneUserTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(phoneUserTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
    inviteButton.setEnabled(userList.getSelectedValues().length > 0);
}//GEN-LAST:event_userListValueChanged

private void cancelCallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelCallButtonActionPerformed
    if (listeners == null) {
        addHUDComponent.setVisible(false);
        return;
    }

    listeners.firePropertyChange("cancel", new String(""), null);
}//GEN-LAST:event_cancelCallButtonActionPerformed

private void callButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callButtonActionPerformed
    PresenceInfo[] info = pm.getAllUsers();

    String name = nameTextField.getText();

    for (int i = 0; i < info.length; i++) {
        if (info[i].usernameAlias.equals(name) ||
		info[i].userID.getUsername().equals(name)) {

            statusLabel.setText("Name is already being used!");
            return;
        }
    }

    addHUDComponent.setVisible(false);

    statusLabel.setText("");

    inCallHUDPanel.callUser(nameTextField.getText(), phoneNumberTextField.getText());

    inCallHUDComponent.setVisible(true);

    if (locationSet == false) {
	locationSet = true;
   
        inCallHUDComponent.setLocation(addHUDComponent.getX() - addHUDComponent.getWidth(), 
	    addHUDComponent.getY() + addHUDComponent.getHeight() - inCallHUDComponent.getHeight());
    }
}//GEN-LAST:event_callButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    if (listeners == null) {
        addHUDComponent.setVisible(false);
        return;
    }

    listeners.firePropertyChange("cancel", new String(""), null);
}//GEN-LAST:event_cancelButtonActionPerformed

private boolean locationSet;

private void inviteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inviteButtonActionPerformed
    Object[] selectedValues = userList.getSelectedValues();

    ArrayList<PresenceInfo> usersToInvite = new ArrayList();

    if (selectedValues.length > 0) {
	for (int i = 0; i < selectedValues.length; i++) {
            String username = NameTagNode.getUsername((String) selectedValues[i]);

            PresenceInfo info = pm.getAliasPresenceInfo(username);

            if (info == null) {
                logger.warning("no PresenceInfo for " + username);
                continue;
            }

	    if (info.equals(myPresenceInfo)) {
                /*
                 * I'm the caller and will be added automatically
                 */
                continue;
            }

            usersToInvite.add(info);
        }
    }

    addHUDComponent.setVisible(false);

    inCallHUDPanel.inviteUsers(usersToInvite) ;

    inCallHUDComponent.setVisible(true);

    if (locationSet == false) {
	locationSet = true;
   
        inCallHUDComponent.setLocation(addHUDComponent.getX() - addHUDComponent.getWidth(), 
	    addHUDComponent.getY() + addHUDComponent.getHeight() - inCallHUDComponent.getHeight());
    }
}//GEN-LAST:event_inviteButtonActionPerformed

private void phoneUserTabStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_phoneUserTabStateChanged
// TODO add your handling code here:
}//GEN-LAST:event_phoneUserTabStateChanged

private void phoneNumberTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_phoneNumberTextFieldKeyReleased
    callButton.setEnabled(phoneNumberTextField.getText().length() > 0 &&
	nameTextField.getText().length() > 0);
}//GEN-LAST:event_phoneNumberTextFieldKeyReleased

private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
    callButton.setEnabled(phoneNumberTextField.getText().length() > 0 &&
	nameTextField.getText().length() > 0);
}//GEN-LAST:event_nameTextFieldKeyReleased

private void phonePrivateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phonePrivateRadioButtonActionPerformed
    chatType = ChatType.PRIVATE;
    privacyDescriptionLabel.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);
}//GEN-LAST:event_phonePrivateRadioButtonActionPerformed

private void phoneSecretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneSecretRadioButtonActionPerformed
    chatType = ChatType.SECRET;
    privacyDescriptionLabel.setText(VoiceChatMessage.SECRET_DESCRIPTION);
}//GEN-LAST:event_phoneSecretRadioButtonActionPerformed

private void phoneSpeakerPhoneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneSpeakerPhoneRadioButtonActionPerformed
    chatType = ChatType.PUBLIC;
    privacyDescriptionLabel.setText(VoiceChatMessage.PUBLIC_DESCRIPTION);
}//GEN-LAST:event_phoneSpeakerPhoneRadioButtonActionPerformed

private void privateRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateRadioButtonActionPerformed
    chatType = ChatType.PRIVATE;
    phonePrivacyDescriptionLabel.setText(VoiceChatMessage.PRIVATE_DESCRIPTION);
}//GEN-LAST:event_privateRadioButtonActionPerformed

private void secretRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secretRadioButtonActionPerformed
    chatType = ChatType.SECRET;
    phonePrivacyDescriptionLabel.setText(VoiceChatMessage.SECRET_DESCRIPTION);
}//GEN-LAST:event_secretRadioButtonActionPerformed

private void speakerPhoneRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_speakerPhoneRadioButtonActionPerformed
    chatType = ChatType.PUBLIC;
    phonePrivacyDescriptionLabel.setText(VoiceChatMessage.PUBLIC_DESCRIPTION);
}//GEN-LAST:event_speakerPhoneRadioButtonActionPerformed

private void phoneNumberTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_phoneNumberTextFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton callButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton cancelCallButton;
    private javax.swing.JButton inviteButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel phoneNumberLabel;
    private javax.swing.JTextField phoneNumberTextField;
    private javax.swing.JLabel phonePrivacyDescriptionLabel;
    private javax.swing.JRadioButton phonePrivateRadioButton;
    private javax.swing.JRadioButton phoneSecretRadioButton;
    private javax.swing.JRadioButton phoneSpeakerPhoneRadioButton;
    private javax.swing.JTabbedPane phoneUserTab;
    private javax.swing.JLabel privacyDescriptionLabel;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JRadioButton speakerPhoneRadioButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables

}
