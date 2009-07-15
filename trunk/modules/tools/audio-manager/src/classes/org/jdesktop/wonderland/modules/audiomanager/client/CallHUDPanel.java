/*
 * CallHUDPanel.java
 *
 * Created on July 10, 2009, 11:00 AM
 */

package org.jdesktop.wonderland.modules.audiomanager.client;

import java.util.ArrayList;

import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import org.jdesktop.wonderland.modules.audiomanager.common.messages.VoiceChatInfoRequestMessage;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer.NameTagNode;

import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent;
import org.jdesktop.wonderland.client.hud.HUDComponentEvent.ComponentEventType;
import org.jdesktop.wonderland.client.hud.HUDComponentListener;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/**
 *
 * @author  jp
 */
public class CallHUDPanel extends javax.swing.JPanel implements PresenceManagerListener,
	MemberChangeListener, DisconnectListener {

    private static final Logger logger = Logger.getLogger(CallHUDPanel.class.getName());

    private AudioManagerClient client;
    private WonderlandSession session;
    private PresenceManager pm;
    private PresenceInfo myPresenceInfo;

    private DefaultListModel userListModel;

    private InCallHUDPanel inCallHUDPanel;

    private PresenceInfo caller;
    private String group;

    private PropertyChangeSupport listeners;

    private HUDComponent callHUDComponent;

    /** Creates new form CallHUDPanel */
    public CallHUDPanel() {
        initComponents();
    }

    public CallHUDPanel(AudioManagerClient client, WonderlandSession session,
	    PresenceInfo myPresenceInfo) {

	this(client, session, myPresenceInfo, null);
    }

    public CallHUDPanel(AudioManagerClient client, WonderlandSession session,
	    PresenceInfo myPresenceInfo, InCallHUDPanel inCallHUDPanel) {

	this.client = client;
	this.session = session;
        this.myPresenceInfo = myPresenceInfo;
	this.inCallHUDPanel = inCallHUDPanel;

        initComponents();

        userListModel = new DefaultListModel();
        userList.setModel(userListModel);

  	userList.setEnabled(false);

	if (inCallHUDPanel != null) {
	    caller = inCallHUDPanel.getCaller();
	    group = inCallHUDPanel.getGroup();

	    inCallHUDComponent = inCallHUDPanel.getHUDComponent();

	    callJLabel.setText("Call " + group);
	    client.addMemberChangeListener(group, this);
	    secretRadioButton.setEnabled(false);
	    privateRadioButton.setEnabled(false);
	    session.send(client, new VoiceChatInfoRequestMessage(group));
	} else {
	    caller = myPresenceInfo;
	}

        pm = PresenceManagerFactory.getPresenceManager(session);
	pm.addPresenceManagerListener(this);

	userList.setEnabled(false);

	//getRootPane.setDefaultButton(inviteButton);
	inviteButton.setEnabled(false);
	setVisible(true);
    }

    public void setHUDComponent(HUDComponent callHUDComponent) {
	this.callHUDComponent = callHUDComponent;
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

    private void addToUserList(PresenceInfo presenceInfo) {
	if (presenceInfo.equals(myPresenceInfo)) {
	    return;
	}

	userListModel.addElement(presenceInfo.usernameAlias);
    }

    public void presenceInfoChanged(PresenceInfo presenceInfo, ChangeType type) {
	if (type.equals(ChangeType.USER_REMOVED)) {
	    synchronized (userListModel) {
		userListModel.removeElement(presenceInfo.usernameAlias);
	    }
	} else if (type.equals(ChangeType.USER_ADDED)) {
	    synchronized (userListModel) {
		addToUserList(presenceInfo);
	    }
	} 
    }

    public void memberChange(PresenceInfo presenceInfo, boolean added) {
	logger.finer("memberChange " + presenceInfo + " added " + added);

	if (added) {
	    synchronized (userListModel) {
		userListModel.removeElement(presenceInfo.usernameAlias);
	    }
	} else {
	    synchronized (userListModel) {
		addToUserList(presenceInfo);
	    }
	}
    }

    public void setMemberList(PresenceInfo[] members) {
	userListModel.clear();

	PresenceInfo[] presenceInfoList = pm.getAllUsers();

	synchronized (userListModel) {
	    for (int i = 0; i < presenceInfoList.length; i++) {
	        if (contains(members, presenceInfoList[i]) == false) {
		    addToUserList(presenceInfoList[i]);
		}
	    }
	}
    }

    private boolean contains(PresenceInfo[] members, PresenceInfo info) {
	for (int i = 0; i < members.length; i++) {
	    if (members[i].equals(info)) {
		return true;
	    }
	}
	return false;
    }

    public void disconnected() {
        callHUDComponent.setVisible(false);
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
        callJLabel = new javax.swing.JLabel();
        phoneNumberRadioButton = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        numberTextField = new javax.swing.JTextField();
        inWorldRadioButton = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        userList = new javax.swing.JList();
        jLabel4 = new javax.swing.JLabel();
        secretRadioButton = new javax.swing.JRadioButton();
        privateRadioButton = new javax.swing.JRadioButton();
        cancelButton = new javax.swing.JButton();
        inviteButton = new javax.swing.JButton();

        callJLabel.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        callJLabel.setText("Place Call");

        buttonGroup1.add(phoneNumberRadioButton);
        phoneNumberRadioButton.setSelected(true);
        phoneNumberRadioButton.setText("External user");
        phoneNumberRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneNumberRadioButtonActionPerformed(evt);
            }
        });

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("Name:");

        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });
        nameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameTextFieldKeyReleased(evt);
            }
        });

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Phone Number:");

        numberTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberTextFieldActionPerformed(evt);
            }
        });
        numberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                numberTextFieldKeyReleased(evt);
            }
        });

        buttonGroup1.add(inWorldRadioButton);
        inWorldRadioButton.setText("In-world user");
        inWorldRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inWorldRadioButtonActionPerformed(evt);
            }
        });

        userList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                userListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(userList);

        jLabel4.setText("Privacy:");

        buttonGroup2.add(secretRadioButton);
        secretRadioButton.setText("Secret");

        buttonGroup2.add(privateRadioButton);
        privateRadioButton.setSelected(true);
        privateRadioButton.setText("Private");

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

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(inWorldRadioButton)
                            .add(layout.createSequentialGroup()
                                .add(jLabel4)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(secretRadioButton)
                                    .add(privateRadioButton)))
                            .add(layout.createSequentialGroup()
                                .add(21, 21, 21)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                                    .add(layout.createSequentialGroup()
                                        .add(jLabel3)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(numberTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 41, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(nameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))))))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(10, 10, 10)
                                .add(phoneNumberRadioButton))
                            .add(callJLabel)))
                    .add(layout.createSequentialGroup()
                        .add(52, 52, 52)
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(inviteButton)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {jLabel2, jLabel3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.linkSize(new java.awt.Component[] {cancelButton, inviteButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(callJLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(phoneNumberRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(numberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(inWorldRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 126, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(secretRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(privateRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(inviteButton)
                    .add(cancelButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void userListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_userListValueChanged
    inviteButton.setEnabled(userList.getSelectedValues().length > 0);
}//GEN-LAST:event_userListValueChanged

private void nameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameTextFieldKeyReleased
    inviteButton.setEnabled(nameTextField.getText().length() > 0 && numberTextField.getText().length() > 0);
}//GEN-LAST:event_nameTextFieldKeyReleased

private void numberTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_numberTextFieldKeyReleased
    inviteButton.setEnabled(nameTextField.getText().length() > 0 && numberTextField.getText().length() > 0);
}//GEN-LAST:event_numberTextFieldKeyReleased

private void phoneNumberRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneNumberRadioButtonActionPerformed
    nameTextField.setEnabled(true);
    numberTextField.setEnabled(true);
    userList.setEnabled(false);

    inviteButton.setEnabled(nameTextField.getText().length() > 0 && numberTextField.getText().length() > 0);
}//GEN-LAST:event_phoneNumberRadioButtonActionPerformed

private void inWorldRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inWorldRadioButtonActionPerformed
    nameTextField.setEnabled(false);
    numberTextField.setEnabled(false);
    userList.setEnabled(true);
    inviteButton.setEnabled(userList.getSelectedValues().length > 0);
}//GEN-LAST:event_inWorldRadioButtonActionPerformed

private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    if (listeners == null) {
	callHUDComponent.setVisible(false);
	return;
    }

    listeners.firePropertyChange("cancel", new String(""), null);
}//GEN-LAST:event_cancelButtonActionPerformed

private HUDComponent inCallHUDComponent;

private void inviteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inviteButtonActionPerformed
    Object[] selectedValues = userList.getSelectedValues();

    ArrayList<PresenceInfo> usersToInvite = new ArrayList();

    if (inWorldRadioButton.isSelected() && selectedValues.length > 0) {
	for (int i = 0; i < selectedValues.length; i++) {
            String username = NameTagNode.getUsername((String) selectedValues[i]);

            PresenceInfo[] info = pm.getAliasPresenceInfo(username);

            if (info == null) {
                logger.warning("no PresenceInfo for " + username);
                continue;
            }

	    if (info[0].equals(myPresenceInfo)) {
                /*
                 * I'm the caller and will be added automatically
                 */
                continue;
            }

            usersToInvite.add(info[0]);
        }
    }

    secretRadioButton.setEnabled(false);
    privateRadioButton.setEnabled(false);

    if (inCallHUDPanel == null) {
        inCallHUDPanel = new InCallHUDPanel(client, session, myPresenceInfo, caller);

	inCallHUDPanel.setCallHUDPanel(this);

	HUD mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
	inCallHUDComponent = mainHUD.createComponent(inCallHUDPanel);

	inCallHUDPanel.setHUDComponent(inCallHUDComponent);

	//inCallHUDComponent.setPreferredLocation(Layout.NORTH);
	mainHUD.addComponent(inCallHUDComponent);
	inCallHUDComponent.addComponentListener(new HUDComponentListener() {
	    public void HUDComponentChanged(HUDComponentEvent e) {
	        if (e.getEventType().equals(ComponentEventType.DISAPPEARED)) {
	        }
	    }
	});

        inCallHUDComponent.setVisible(true);

        //System.out.println("Call x,y " + callHUDComponent.getX() + ", " + callHUDComponent.getY()
        //    + " width " + callHUDComponent.getWidth() + " height " + callHUDComponent.getHeight()
        //    + " Incall x,y " + (callHUDComponent.getX() - callHUDComponent.getWidth())
        //    + ", " + (callHUDComponent.getY() + callHUDComponent.getHeight() - inCallHUDComponent.getHeight()));

        inCallHUDComponent.setLocation(callHUDComponent.getX() - callHUDComponent.getWidth(), 
	    callHUDComponent.getY() + callHUDComponent.getHeight() - inCallHUDComponent.getHeight());
    }

    if (inWorldRadioButton.isSelected()) {
	if (secretRadioButton.isEnabled()) {
	    inCallHUDPanel.inviteUsers(usersToInvite, secretRadioButton.isSelected());
	} else {
	    inCallHUDPanel.inviteUsers(usersToInvite);
	}
    } else {
	inCallHUDPanel.callUser(nameTextField.getText(), numberTextField.getText());
    }

    inCallHUDComponent.setVisible(true);
}//GEN-LAST:event_inviteButtonActionPerformed

private void numberTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_numberTextFieldActionPerformed

private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
    // TODO add your handling code here:
}//GEN-LAST:event_nameTextFieldActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JLabel callJLabel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton inWorldRadioButton;
    private javax.swing.JButton inviteButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTextField numberTextField;
    private javax.swing.JRadioButton phoneNumberRadioButton;
    private javax.swing.JRadioButton privateRadioButton;
    private javax.swing.JRadioButton secretRadioButton;
    private javax.swing.JList userList;
    // End of variables declaration//GEN-END:variables

}
