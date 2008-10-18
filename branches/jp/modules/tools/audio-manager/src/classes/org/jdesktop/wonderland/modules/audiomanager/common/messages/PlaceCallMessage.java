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
package org.jdesktop.wonderland.modules.audiomanager.common.messages;

import org.jdesktop.wonderland.common.ExperimentalAPI;

import org.jdesktop.wonderland.common.messages.Message;

import org.jdesktop.wonderland.common.cell.CellID;

/**
 * The initial message that a client must send to the Wonderland server
 * in order to specify a communications protocol to use.
 * @author jprovino
 */
@ExperimentalAPI
public class PlaceCallMessage extends Message {
    private CellID cellID;
    private String sipURL;	      // URL of softphone to call
    private double x;	      	      // location of the call
    private double y;
    private double z;
    private double direction;	      // direction of avatar
    private boolean confirmAnswered;  // user has to press 1

    public PlaceCallMessage(CellID cellID, String sipURL, double x, double y,
	    double z, double direction, boolean confirmAnswered) {

	this.cellID = cellID;
	this.sipURL = sipURL;
	this.x = x;
	this.y = y;
	this.z = z;
	this.direction = direction;
	this.confirmAnswered = confirmAnswered;
    }

    public void setCellID(CellID cellID) {
        this.cellID = cellID;
    }

    public CellID getCellID() {
        return cellID;
    }

    public void setSipURL(String sipURL) {
	this.sipURL = sipURL;
    }

    public String getSipURL() {
	return sipURL;
    }

    public void setX(double x) {
	this.x = x;
    }

    public double getX() {
	return x;
    }

    public void setY(double y) {
	this.y = y;
    }

    public double getY() {
	return y;
    }

    public void setZ(double z) {
	this.z = z;
    }

    public double getZ() {
	return z;
    }

    public void setDirection(double direction) {
	this.direction = direction;
    }

    public double getDirection() {
	return direction;
    }

    public void setConfirmAnswered(boolean confirmAnswered) {	
	this.confirmAnswered = confirmAnswered;
    }

    public boolean getConfirmAnswered() {
	return confirmAnswered;
    }

}
