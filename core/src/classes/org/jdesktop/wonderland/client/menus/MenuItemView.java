/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import org.jdesktop.wonderland.client.utils.Observable;
import org.jdesktop.wonderland.client.utils.Observer;

/**
 *
 * @author Ryan
 */
public class MenuItemView implements ActionListener {

    private JMenuItem menuItem;
    private Observable observable;

    public MenuItemView(String caption) {
        observable = new Observable();


        menuItem = new JMenuItem(caption);
        menuItem.addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        observable.fire("action-performed", ae);
    }

    public void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void removeObserver(Observer observer) {
        observable.removeObserver(observer);
    }
    
    public JMenuItem getMenuItem() {
        return menuItem;
    }
}
