/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.client.utils;

/**
 *
 * @author Ryan
 */
public interface Observer {
    public void eventObserved(String property, Object value);
}
