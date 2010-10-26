/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */

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
package org.jdesktop.wonderland.modules.avatarbase.client.jme.cellrenderer;

import imi.character.CharacterEyes;
import imi.character.avatar.AvatarContext.TriggerNames;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDButton;
import org.jdesktop.wonderland.client.hud.HUDManagerFactory;

/**
 * A HUD display for avatar gestures
 *
 * @author nsimpson
 * @author ronny.standtke@fhnw.ch
 */
public class GestureHUD {

    private static final Logger logger = Logger.getLogger(GestureHUD.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/avatarbase/client/resources/Bundle");
    private boolean visible = false;
    private boolean showingGestures = true;
    // maps GUI visible gesture names to non-visible action names
    private Map<String, String> gestureMap = new HashMap<String, String>();
    private Map<String, HUDButton> buttonMap = new HashMap<String, HUDButton>();
    private HUDButton showGesturesButton;
    private HUD mainHUD;
    // map gestures to column, row locations on gesture HUD
    private String[][] gestures = {
        {BUNDLE.getString("AnswerCell"), BUNDLE.getString("AnswerCell_X"), "1"},
        {BUNDLE.getString("Sit"), BUNDLE.getString("Sit_X"), "2"},
        /*{"Take Damage", "0", "3"},*/
        {BUNDLE.getString("PublicSpeaking"), BUNDLE.getString("PublicSpeaking_X"), "0"},
        {BUNDLE.getString("Bow"), BUNDLE.getString("Bow_X"), "1"},
        {BUNDLE.getString("ShakeHands"), BUNDLE.getString("ShakeHands_X"), "2"},
        {BUNDLE.getString("Cheer"), BUNDLE.getString("Cheer_X"), "0"},
        {BUNDLE.getString("Clap"), BUNDLE.getString("Clap_X"), "1"},
        {BUNDLE.getString("Laugh"), BUNDLE.getString("Laugh_X"), "2"},
        {BUNDLE.getString("Wave"), BUNDLE.getString("Wave_X"), "2"},
        {BUNDLE.getString("RaiseHand"), BUNDLE.getString("RaiseHand_X"), "1"},
        {BUNDLE.getString("Follow"), BUNDLE.getString("Follow_X"), "0"},
        /*{"Left Wink", "4", "0"},*/
        {BUNDLE.getString("Wink"), BUNDLE.getString("Wink_X"), "0"},
        {BUNDLE.getString("No"), BUNDLE.getString("No_X"), "1"},
        {BUNDLE.getString("Yes"), BUNDLE.getString("Yes_X"), "2"}};
    private int leftMargin = 20;
    private int bottomMargin = 10;
    private int rowHeight = 30;
    private int columnWidth = 100;
    private final static String HIDE_GESTURES = BUNDLE.getString("HideGestures");
    private final static String SHOW_GESTURES = BUNDLE.getString("ShowGestures");

    /**
     * creates a new GestureHUD
     */
    public GestureHUD() {
        setAvatarCharacter(null);
    }

    /**
     * shows or hides the gesture HUD
     * @param visible if <tt>true</tt>, the HUD is shown, otherwise hidden
     */
    public void setVisible(final boolean visible) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (GestureHUD.this.visible == visible) {
                    return;
                }
                if (showGesturesButton == null) {
                    showGesturesButton = mainHUD.createButton(HIDE_GESTURES);
                    showGesturesButton.setDecoratable(false);
                    showGesturesButton.setLocation(leftMargin, bottomMargin);
                    showGesturesButton.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            showingGestures = SHOW_GESTURES.equals(showGesturesButton.getLabel());
                            showGesturesButton.setLabel(showingGestures ? HIDE_GESTURES : SHOW_GESTURES);
                            showGestureButtons(showingGestures);
                        }
                    });
                    mainHUD.addComponent(showGesturesButton);
                }
                GestureHUD.this.visible = visible;
                showGesturesButton.setVisible(visible);
                showGestureButtons(visible && showingGestures);
            }
        });
    }

    /**
     * shows or hides the gesture buttons
     * @param show if <tt>true</tt>, the gesture buttons are shown, otherwise
     * hidden
     */
    public void showGestureButtons(boolean show) {
        for (HUDButton button : buttonMap.values()) {
            button.setVisible(show);
        }
    }

    /**
     * returns <tt>true</tt>, when the HUD is visible, otherwise false
     * @return <tt>true</tt>, when the HUD is visible, otherwise false
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Invoke the Sit gesture.
     */
    private void doSitGesture(final WlAvatarCharacter avatar) {
        // Create a thread that sleeps and tells the sit action to stop.
        final Runnable stopSitRunnable = new Runnable() {

            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    logger.log(Level.WARNING, "Sleep failed.", ex);
                }
                avatar.triggerActionStop(TriggerNames.SitOnGround);
            }
        };

        // Spawn a thread to start the animation, which then spawns a thread
        // to stop the animation after a small sleep.
        new Thread() {

            @Override
            public void run() {
                avatar.triggerActionStart(TriggerNames.SitOnGround);
                new Thread(stopSitRunnable).start();
            }
        }.start();
    }

    /**
     * sets the avatar and activates supported gestures
     * @param avatar the avatar to set
     */
    public void setAvatarCharacter(final WlAvatarCharacter avatar) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (mainHUD == null) {
                    mainHUD = HUDManagerFactory.getHUDManager().getHUD("main");
                }

                // remove existing gesture buttons
                for (HUDButton button : buttonMap.values()) {
                    mainHUD.removeComponent(button);
                }
                buttonMap.clear();
                gestureMap.clear();

                // If we don't have an avatar, then just return
                if (avatar == null) {
                    return;
                }

                // Otherwise, figure out which gestures are supported. We want
                // to remove the "Male_" or "Female_" for now.
                for (String action : avatar.getAnimationNames()) {
                    String name = action;
                    if (action.startsWith("Male_") == true) {
                        name = name.substring(5);
                    } else if (action.startsWith("Female_") == true) {
                        name = name.substring(7);
                    }
                    // add to a map of user-friendly names to avatar animations
                    // e.g., "Shake Hands" -> "Male_ShakeHands"
                    gestureMap.put(BUNDLE.getString(name), action);
                }

                // Add the left and right wink
                if (avatar.getCharacterParams().isAnimatingFace()) {
                    gestureMap.put(BUNDLE.getString("Wink"), "RightWink");
                    gestureMap.put(BUNDLE.getString("Sit"), "Sit");
                }

                // Create HUD buttons for each of the actions
                for (String name : gestureMap.keySet()) {
                    int row = 0;
                    int column = 0;

                    // find the button row, column position for this gesture
                    for (String[] gesture : gestures) {
                        if (gesture[0].equals(name)) {
                            column = Integer.valueOf(gesture[1]);
                            row = Integer.valueOf(gesture[2]);
                            HUDButton button = mainHUD.createButton(name);
                            button.setDecoratable(false);
                            button.setPreferredTransparency(0.2f);
                            button.setLocation(leftMargin + column * columnWidth,
                                    bottomMargin + row * rowHeight);
                            mainHUD.addComponent(button);
                            buttonMap.put(name, button);

                            button.addActionListener(new ActionListener() {

                                public void actionPerformed(ActionEvent event) {
                                    String action = gestureMap.get(event.getActionCommand());
                                    logger.info("playing animation: " + event.getActionCommand());
                                    if (action.equals("Sit")) {
                                        doSitGesture(avatar);
                                    } else if (action.equals("RightWink")) {
                                        CharacterEyes eyes = avatar.getEyes();
                                        eyes.wink(false);
                                    } else {
                                        avatar.playAnimation(action);
                                    }
                                }
                            });

                            // OWL issue #125: set the button visible now or
                            // it won't show up when buttons are removed and
                            // re-added
                            button.setVisible(isVisible());
                            break;
                        }
                    }
                }
                setVisible(true);
            }
        });
    }
}
