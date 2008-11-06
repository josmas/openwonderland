/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.modules.swingsettest.client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GraphicsEnvironment;
import org.jdesktop.wonderland.modules.swingsettest.client.swingset2.SwingSet2;

/**
 * A standalone program for the SwingSet2Panel (outside of Wonderland).
 */

public class TestSwingSet2Panel extends JFrame {

    public static void main(String[] args) {
        TestSwingSet2Panel testSwingSet2Panel = new TestSwingSet2Panel();
    }
    
    public TestSwingSet2Panel () {
        Frame frame = new Frame();
        // center the frame
        frame.setLocationRelativeTo(null);
        // show frame
        frame.setVisible(true);
    }

    class Frame extends JFrame {

        JPanel contentPane;
	SwingSet2 swingset = new SwingSet2(null, GraphicsEnvironment.
                                             getLocalGraphicsEnvironment().
                                             getDefaultScreenDevice().
                                             getDefaultConfiguration());

        // Construct the frame
        public Frame () {
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    dispose();
                    System.exit(0);
                }
            });

            contentPane = (JPanel) this.getContentPane();
            contentPane.setLayout(new BorderLayout());
            setTitle("TestSwingSet2Panel");

            contentPane.add(swingset, BorderLayout.NORTH);

            pack();
        }
    }
}
