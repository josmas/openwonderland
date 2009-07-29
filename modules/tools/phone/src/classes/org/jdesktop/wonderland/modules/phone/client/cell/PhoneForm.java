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
package org.jdesktop.wonderland.modules.phone.client.cell;

import org.jdesktop.wonderland.common.cell.CellID;

import org.jdesktop.wonderland.modules.phone.common.CallListing;

import org.jdesktop.wonderland.client.cell.ChannelComponent;

import org.jdesktop.wonderland.client.comms.WonderlandSession;

import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;

import java.awt.Color;
import java.awt.Point;

import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author  nsimpson
 */
public class PhoneForm extends JDialog implements KeypadListener {

    private static final Logger logger = Logger.getLogger(PhoneForm.class.getName());
    // we need to know who our parent cell is in order properly pass messages to the server
    private boolean locked = false;
    private String phoneNumber;

    private PhoneMessageHandler phoneMessageHandler;

    private CellID phoneCellID;

    private PresenceManager pm;

    public PhoneForm(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public PhoneForm(WonderlandSession session, CellID phoneCellID, 
	    ChannelComponent channelComp, PhoneMessageHandler phoneMessageHandler, 
	    boolean locked, String phoneNumber, boolean passwordProtected) {

        initComponents();

	pm = PresenceManagerFactory.getPresenceManager(session);

        getRootPane().setDefaultButton(callButton);

	this.phoneCellID = phoneCellID;
	this.phoneMessageHandler = phoneMessageHandler;
        this.phoneNumber = phoneNumber;

        if (passwordProtected == false) {
            unlockButton.setEnabled(false);
        } else {
            phonePasswordDialog = 
		new PhonePasswordDialog(this, phoneCellID, channelComp);
        }

        setLocked(locked);

        setTitle("Phone " + phoneNumber);
    }

    public void reset() {
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		contactNameTextField.setText(null);
		contactNumberTextField.setText(null);
		keypadButton.setEnabled(false);
	        callButton.setText("Call");
		callButton.setEnabled(false);
		setLocked(locked);
		statusMessageLabel.setText(null);
            }
        });
    }

    @Override
    public void setVisible(boolean b) {
        if (b == true) {
            reset();
        }
        super.setVisible(b);
    }

    private void setLocked() {
        setLocked(locked);
    }

    private void setLocked(final boolean locked) {
        this.locked = locked;

	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	if (locked) {
            	    lockedLabel.setForeground(Color.RED);
            	    lockedLabel.setText("Locked");
            	    setSimulationMode(true);
            	    privateCallCheckBox.setSelected(false);
            	    privateCallCheckBox.setEnabled(false);
            	    callButton.setEnabled(false);
            	    unlockButton.setText("Unlock");
        	} else {
	            lockedLabel.setForeground(new Color(0, 153, 0));
           	    lockedLabel.setText("Unlocked");
	            setSimulationMode(false);
            	    privateCallCheckBox.setSelected(true);
            	    privateCallCheckBox.setEnabled(true);
            	    setDialoutButtonState();
            	    unlockButton.setText("Lock");
        	}
	    }
	});
    }

    public void changeLocked(final boolean locked, final boolean wasSuccessful) {
	
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	if (wasSuccessful == false) {
            	    phonePasswordDialog.invalidPassword();
            	    return;
        	}

        	phonePasswordDialog.setVisible(false);

		setLocked(locked);
	    }
	});
    }

    private void setSimulationMode(boolean simulating) {
        simulationModeCheckBox.setSelected(simulating);
        simulationModeCheckBox.setEnabled(!locked);
        simulationLabel.setEnabled(simulating);
        privateCallCheckBox.setEnabled(!simulating);
        privateCallCheckBox.setSelected(!simulating);
    }

    private void setDialoutButtonState() {
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	if (contactNameTextField.isEditable() == false) {
	    	    if (callButton.getText().equals("End Call") == false) {
                	callButton.setEnabled(false);
	    	    }
            	    return;
        	}

		if (contactNameTextField.getText().length() > 0 &&
                    contactNumberTextField.getText().length() > 0) {
            	    callButton.setEnabled(true);
		} else {
		    if (callButton.getText().equals("End Call") == false) {
			callButton.setEnabled(false);
	    	    }
        	}
	    }
	});
    }

    public void setStatusMessage(final String s) {
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
		statusMessageLabel.setText(s);
	    }
	});
    }

    public void setCallEstablished(final boolean enabled) {
	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	statusMessageLabel.setText("Call in progress");

        	joinButton.setEnabled(enabled);

        	if (!simulationModeCheckBox.isSelected()) {
		    // allow calls to be ended whether private or public if not
		    // simulating calls
		    // REMIND: should allow simulated calls to be ended too
		    keypadButton.setEnabled(true);
        	}
	    }
	});
    }

    public void setCallEnded(String reasonCallEnded) {
        if (reasonCallEnded == null) {
            reasonCallEnded = "Hung up";
        } else if (reasonCallEnded.equals("Not Found")) {
            /*
             * The gateway returns "Not Found" meaning Invalid phone number
             */
            reasonCallEnded = "Invalid phone number";
        } else if (reasonCallEnded.indexOf("gateway error") >= 0) {
            /*
             * The gateway returned an error.
             */
            reasonCallEnded = "Telephone connection error";
        } else if (reasonCallEnded.indexOf("No voip Gateway!") >= 0) {
            /*
             * There is no gateway specified.
             */
            reasonCallEnded = "No telephone connection";
        } else if (reasonCallEnded.indexOf("User requested call termination") >= 0) {
            reasonCallEnded = "Hung up";
        }

	final String s = reasonCallEnded;

	java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
        	statusMessageLabel.setText("Ended:  " + s);

		//jTextFieldContactName.setEditable(true);
		//jTextFieldContactNumber.setEditable(true);

        	setDialoutButtonState();

        	if (simulationModeCheckBox.isSelected()) {
            	    privateCallCheckBox.setEnabled(false);
        	}

	 	callButton.setText("Call");
		joinButton.setEnabled(false);
		keypadButton.setEnabled(false);

		unlockButton.setEnabled(true);

		if (keypad != null) {
		    keypad.setVisible(false);
        	}
	    }
	});
    }

//    public void updateCallListings(HashMap<String,CallListing> callListings, CallListing selectListing) {    
//    //    CallListing previouslySelected = (CallListing)jListCalls.getSelectedValue();
//        
//        //Update the call list
//        callList.clear();       
//        int i = 0;
//        int selectIndex = -1;
//        for(CallListing listing : callListings.values()) {
//            callList.addElement((Object)listing);
//            
//            if (selectListing != null && selectListing.getListingID().compareTo(listing.getListingID()) == 0)
//                selectIndex = i;
//            
//            i++;
//        }
//        
//        if (selectIndex >= 0) {
//            jListCalls.setSelectedIndex(selectIndex);      
//        } else if (previouslySelected != null) {
//            jListCalls.setSelectedIndex(
//		callList.indexOf(previouslySelected));
//	}
//    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        placeCallLabel = new javax.swing.JLabel();
        contactNameLabel = new javax.swing.JLabel();
        contactNameTextField = new javax.swing.JTextField();
        contactNumberLabel = new javax.swing.JLabel();
        contactNumberTextField = new javax.swing.JTextField();
        callButton = new javax.swing.JButton();
        privateCallCheckBox = new javax.swing.JCheckBox();
        simulationModeCheckBox = new javax.swing.JCheckBox();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        statusMessageLabel = new javax.swing.JLabel();
        lockedLabel = new javax.swing.JLabel();
        simulationLabel = new javax.swing.JLabel();
        joinButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        unlockButton = new javax.swing.JButton();
        keypadButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Place Call");
        setName("phoneForm"); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                PhoneForm.this.windowClosed(evt);
            }
        });

        placeCallLabel.setFont(new java.awt.Font("Dialog", 1, 13));
        placeCallLabel.setText("Place Call");

        contactNameLabel.setFont(new java.awt.Font("Dialog", 0, 13));
        contactNameLabel.setText("Contact Name:");

        contactNameTextField.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        contactNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contactNameTextFieldKeyReleased(evt);
            }
        });

        contactNumberLabel.setFont(new java.awt.Font("Dialog", 0, 13));
        contactNumberLabel.setText("Phone Number:");

        contactNumberTextField.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        contactNumberTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contactNumberTextFieldActionPerformed(evt);
            }
        });
        contactNumberTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contactNumberTextFieldKeyReleased(evt);
            }
        });

        callButton.setFont(new java.awt.Font("Dialog", 0, 13));
        callButton.setText("Call");
        callButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callButtonActionPerformed(evt);
            }
        });

        privateCallCheckBox.setFont(new java.awt.Font("Dialog", 0, 13));
        privateCallCheckBox.setSelected(true);
        privateCallCheckBox.setText("Private Call");

        simulationModeCheckBox.setFont(new java.awt.Font("Dialog", 0, 13));
        simulationModeCheckBox.setText("Simulate telephone features");
        simulationModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulationModeCheckBoxActionPerformed(evt);
            }
        });

        statusLabel.setText("Status:");

        statusMessageLabel.setText("Ready");

        lockedLabel.setForeground(new java.awt.Color(255, 0, 0));
        lockedLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lockedLabel.setText("Locked");

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 55, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusMessageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lockedLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(30, 30, 30))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
            .add(lockedLabel)
            .add(statusMessageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(statusLabel)
        );

        statusPanelLayout.linkSize(new java.awt.Component[] {lockedLabel, statusLabel, statusMessageLabel}, org.jdesktop.layout.GroupLayout.VERTICAL);

        simulationLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        simulationLabel.setText("The phone will simulate calling");

        joinButton.setFont(new java.awt.Font("Dialog", 0, 13));
        joinButton.setText("Join to World");
        joinButton.setEnabled(false);
        joinButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                joinButtonActionPerformed(evt);
            }
        });

        closeButton.setFont(new java.awt.Font("Dialog", 0, 13));
        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        unlockButton.setText("Unlock");
        unlockButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unlockButtonActionPerformed(evt);
            }
        });

        keypadButton.setText("Keypad");
        keypadButton.setEnabled(false);
        keypadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keypadButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(contactNameLabel)
                            .add(contactNumberLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(contactNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, contactNumberTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(keypadButton)
                        .add(37, 37, 37))
                    .add(layout.createSequentialGroup()
                        .add(116, 116, 116)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(joinButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(unlockButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(closeButton))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(callButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(privateCallCheckBox)
                                    .add(simulationModeCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 207, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(23, 23, 23)
                                        .add(simulationLabel))))))
                    .add(layout.createSequentialGroup()
                        .add(placeCallLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 341, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .add(statusPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        layout.linkSize(new java.awt.Component[] {callButton, closeButton, keypadButton, unlockButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(placeCallLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(contactNameLabel)
                    .add(contactNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(75, 75, 75)
                        .add(simulationModeCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(simulationLabel))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(contactNumberLabel)
                            .add(contactNumberTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(keypadButton))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(callButton)
                            .add(privateCallCheckBox))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(unlockButton)
                    .add(joinButton)
                    .add(closeButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(statusPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {callButton, closeButton, joinButton, keypadButton, unlockButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void callButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_callButtonActionPerformed
    if (callButton.getText().equals("End Call")) {
        phoneMessageHandler.endCall();
        callButton.setText("Call");
        return;
    }

    String name = contactNameTextField.getText();

    //Disallow empty contact names
    if (contactNameTextField.getText().equals("")) {
        JOptionPane.showMessageDialog(this, "You must enter a contact name.");
        return;
    }

    PresenceInfo[] info = pm.getAllUsers();

    for (int i = 0; i < info.length; i++) {
        if (info[i].usernameAlias.equals(name) ||
		info[i].userID.getUsername().equals(name)) {

            statusMessageLabel.setText("Name is already being used!");
	    return;
        }
    }

    statusMessageLabel.setText("");

    // Ask the server to place a call for us.
    // We'll update our display lists from the phone cell once the success message echos back.
    String privateClientName = "";

    if (privateCallCheckBox.isSelected()) {
        privateClientName = "Private"; // This is going to be updated on the server side        
        callButton.setText("End Call");
    }

    CallListing listing = new CallListing(name, contactNumberTextField.getText(),
            privateClientName, simulationModeCheckBox.isSelected());

    if (simulationModeCheckBox.isSelected() == false) {
        callButton.setEnabled(true);
        unlockButton.setEnabled(false);
    }

    phoneMessageHandler.placeCall(listing);

    if (simulationModeCheckBox.isSelected()) {
        statusMessageLabel.setText("Call in progress");
    }
    // mostRecentCallListing = listing;
}//GEN-LAST:event_callButtonActionPerformed

private void simulationModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulationModeCheckBoxActionPerformed
    setSimulationMode(simulationModeCheckBox.isSelected());
}//GEN-LAST:event_simulationModeCheckBoxActionPerformed

private void joinButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_joinButtonActionPerformed
    phoneMessageHandler.joinCall();

    keypadButton.setEnabled(false);
    setDialoutButtonState();
    callButton.setText("Call");
    setDialoutButtonState();
    unlockButton.setEnabled(true);
}//GEN-LAST:event_joinButtonActionPerformed

private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
    setVisible(false);
}//GEN-LAST:event_closeButtonActionPerformed

private void unlockButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unlockButtonActionPerformed
    phonePasswordDialog.setLocation(new Point(getX() + getWidth(), getY()));
    phonePasswordDialog.setLocked(locked);
    phonePasswordDialog.setVisible(true);
}//GEN-LAST:event_unlockButtonActionPerformed

private void keypadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keypadButtonActionPerformed
    if (keypad == null) {
        keypad = new JDialogKeypad(this, false);
        keypad.setListener(this);
    }

    keypad.setLocation(new Point(getX() + getWidth(), getY()));

    keypad.setVisible(true);
}//GEN-LAST:event_keypadButtonActionPerformed

    public void keypadPressed(char key) {
        phoneMessageHandler.dtmf(key);
    }

private void contactNumberTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contactNumberTextFieldActionPerformed
    if (callButton.isEnabled()) {
        callButton.doClick();
    }
}//GEN-LAST:event_contactNumberTextFieldActionPerformed

private void windowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_windowClosed
// TODO add your handling code here:
}//GEN-LAST:event_windowClosed

private void contactNameTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contactNameTextFieldKeyReleased
    setDialoutButtonState();
}//GEN-LAST:event_contactNameTextFieldKeyReleased

private void contactNumberTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contactNumberTextFieldKeyReleased
    setDialoutButtonState();
}//GEN-LAST:event_contactNumberTextFieldKeyReleased

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                PhoneForm dialog = new PhoneForm(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton callButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel contactNameLabel;
    private javax.swing.JTextField contactNameTextField;
    private javax.swing.JLabel contactNumberLabel;
    private javax.swing.JTextField contactNumberTextField;
    private javax.swing.JButton joinButton;
    private javax.swing.JButton keypadButton;
    private javax.swing.JLabel lockedLabel;
    private javax.swing.JLabel placeCallLabel;
    private javax.swing.JCheckBox privateCallCheckBox;
    private javax.swing.JLabel simulationLabel;
    private javax.swing.JCheckBox simulationModeCheckBox;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JButton unlockButton;
    // End of variables declaration//GEN-END:variables

    private JDialogKeypad keypad;
    private PhonePasswordDialog phonePasswordDialog;
}
