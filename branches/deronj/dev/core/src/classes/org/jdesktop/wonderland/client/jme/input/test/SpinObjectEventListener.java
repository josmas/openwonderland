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
package org.jdesktop.wonderland.client.jme.input.test;

/**
 * An event listener which toggles the spinning state of an object when an button press event occurs over it.
 * In addition the global event mode must be WORLD. To use this in a scene just add this to any entities you 
 * wish to make input sensitive via this.addToEntity.
 * 
 * Example Usage:
 *
 * SpinObjectEventListener spinListener = new SpinObjectEventListener();
 * spinListener.addToEntity(entity);
 *
 * @author deronj
 */

@ExperimentalAPI
public interface SpinObjectEventListener extends WorldEventListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean consumeEvent (Event event, Entity entity) {
	if (super.consumeEvent(event, entity)) {
	    return false;
	}
	if (event.getID() != MouseButtonEvent3D.EVENT_ID) {
	    return false;
	}
	return ((MouseButtonEvent3D)buttonEvent).isPressed();
    }

    // Note: we don't override computeEvent because we don't do any computation in this listener.

    /**
     * {@inheritDoc}
     */
    public void commitEvent (Event event, Entity entity) {
	(MouseButtonEvent3D) buttonEvent = (MouseButtonEvent3D);
	Entity entity = buttonEvent.getEntity();
	if (entity.getComponent(SpinProcessor.class)) {
	    // Stop the spinning
	    entity.removeComponent(SpinProcessor.class);
	} else {
	    // Start the spinning
	    try {
		entity.addComponent(SpinProcessor.class, new SpinProcessor(entity));
	    } catch (InstantiationException ex) {
		// Can't spin an unspinnable entity
	    }
	}
    }

    /** 
     * A component which animates an object to spin around the Y axis.
     */
    private class SpinProcessor extends RotationProcessor {

	SpinProcessor (Entity entity) throws InstantiationException {
	    super("Spinner", WorldManager.getWorldManager(), getNode(), 10);
	}

	Node getNode () throws InstantiationException {
	    RenderComponent renderComp = entity.getComponent(RenderComponent.class);
	    if (renderComp == null) {
		throw InstantiationException();
	    }
	    Node node = renderComp.getSceneRoot();
	    if (node == null) {
		throw InstantiationException();
	    }
	    return node;
	}
    }
}

