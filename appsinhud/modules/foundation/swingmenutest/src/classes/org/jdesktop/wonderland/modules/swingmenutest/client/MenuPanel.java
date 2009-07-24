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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 *
 * @author  dj
 */
public class MenuPanel extends JPanel {

    /** Creates new form MenuPanel */
    public MenuPanel() {
        initComponents();
    }

    private void initComponents() {

	/*
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
	*/

        JMenuBar menuBar = new JMenuBar();
	add(menuBar);

	/////////////////
	// Init Menu 1 //
	/////////////////

        JMenu menu1 = new JMenu();
        menuBar.add(menu1);
        menu1.setText("Menu1"); 

	JMenuItem menu1Item1 = new JMenuItem();
        menu1Item1.setText("Item1"); 
        menu1Item1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu1 Item1");
            }
        });
        menu1.add(menu1Item1);
	
	JMenuItem menu1Item2 = new JMenuItem();
        menu1Item2.setText("Item2"); 
        menu1Item2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu1 Item2");
            }
        });
        menu1.add(menu1Item2);

	JMenuItem menu1Item3 = new JMenuItem();
        menu1Item3.setText("Item3"); 
        menu1Item3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu1 Item3");
            }
        });
        menu1.add(menu1Item3);
	
	/////////////////
	// Init Menu 2 //
	/////////////////

        JMenu menu2 = new JMenu();
        menuBar.add(menu2);
        menu2.setText("Menu2"); 

	JMenuItem menu2Item1 = new JMenuItem();
        menu2Item1.setText("Item1"); 
        menu2Item1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu2 Item1");
            }
        });
        menu2.add(menu2Item1);
	
	JMenuItem menu2Item2 = new JMenuItem();
        menu2Item2.setText("Item2"); 
        menu2Item2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu2 Item2");
            }
        });
        menu2.add(menu2Item2);

	JMenuItem menu2Item3 = new JMenuItem();
        menu2Item3.setText("Item3"); 
        menu2Item3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu2 Item3");
            }
        });
        menu2.add(menu2Item3);	

	/////////////////
	// Init Menu 3 //
	/////////////////

        JMenu menu3 = new JMenu();
        menuBar.add(menu3);
        menu3.setText("Menu3"); 

	JMenuItem menu3Item1 = new JMenuItem();
        menu3Item1.setText("Item1"); 
        menu3Item1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu3 Item1");
            }
        });
        menu3.add(menu3Item1);
	
	JMenuItem menu3Item2 = new JMenuItem();
        menu3Item2.setText("Item2"); 
        menu3Item2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu3 Item2");
            }
        });
        menu3.add(menu3Item2);

	JMenuItem menu3Item3 = new JMenuItem();
        menu3Item3.setText("Item3"); 
        menu3Item3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.err.println("Selected Menu3 Item3");
            }
        });
        menu3.add(menu3Item3);	
    }
}
