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
package org.jdesktop.wonderland.client.jme.artimport;

import com.jme.app.mtgame.WorldManager;
import com.jme.app.mtgame.entity.ProcessorComponent;
import com.jme.math.Matrix3f;
import com.jme.math.Vector3f;
import com.jme.scene.Node;

/**
 *
 * @author paulby
 */
public class TransformProcessorComponent extends ProcessorComponent {


        private Matrix3f rotation;
        private Vector3f translation;
        private Node node;
        private WorldManager worldManager;
        private boolean updatePending = false;
        
        public TransformProcessorComponent(WorldManager worldManager, Node node) {
            this.node = node;
            this.worldManager = worldManager;
        }
        
        @Override
        public void compute(long conditions) {
            // Nothing to do
        }

        @Override
        public void commit(long conditions) {
            synchronized(this) {
                if (updatePending) {
                    node.setLocalRotation(rotation);
                    node.setLocalTranslation(translation);
                    updatePending = false;
                }

            }
            worldManager.addToUpdateList(node);
        }

        @Override
        public void initialize() {
            setArmingConditions(ProcessorComponent.NEW_FRAME_COND);
        }

        public void setTransform(Matrix3f rotation, Vector3f translation) {
            synchronized(this) {
                this.rotation = rotation;
                this.translation = translation;
                updatePending = true;
            }
        }
}
