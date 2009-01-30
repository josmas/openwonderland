/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client.cell;

/**
 *
 * @author jordanslott
 */
public interface CellComponentChangeListener {

    public void componentAdded(Cell cell, CellComponent component);
    public void componentRemoved(Cell cell, CellComponent component);
}
