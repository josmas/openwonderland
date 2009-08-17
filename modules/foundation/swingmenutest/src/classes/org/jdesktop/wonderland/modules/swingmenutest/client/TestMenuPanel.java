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
package org.jdesktop.wonderland.modules.swingmenutest.client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A standalone program for the MenuPanel (outside of Wonderland).
 */

public class TestMenuPanel extends JFrame {

    public static void main(String[] args) {
        TestMenuPanel testMenuPanel = new TestMenuPanel();
    }
    
    public TestMenuPanel () {
        Frame frame = new Frame();
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        frame.setVisible(true);
    }

    class Frame extends JFrame {

        JPanel contentPane;
        JPanel menuPanel = new MenuPanel();

        // Construct the frame
        public Frame () {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());
            setTitle("TestMenuPanel");

            contentPane.add(menuPanel, BorderLayout.NORTH);

            pack();
        }
    }
}
