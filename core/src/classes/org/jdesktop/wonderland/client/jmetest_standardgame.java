/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.client;

import com.jme.bounding.BoundingSphere;
import com.jme.input.MouseInput;
import com.jme.input.MouseInputListener;
import com.jme.intersection.BoundingPickResults;
import com.jme.intersection.PickResults;
import com.jme.math.Ray;
import com.jme.math.Vector2f;
import com.jme.math.Vector3f;
import com.jme.scene.shape.Box;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueueManager;
import com.jmex.game.StandardGame;
import com.jmex.game.state.DebugGameState;
import com.jmex.game.state.GameStateManager;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 *
 * @author paulby
 */
public class jmetest_standardgame {

    public jmetest_standardgame() {
        final StandardGame game = new StandardGame("Test");
        game.start();

        final DebugGameState gameState = new MyGameState();
        GameStateManager.getInstance().attachChild(gameState);
        gameState.setActive(true);
        
        final Box box = new Box("Box", new Vector3f(), 1f, 1f, 1f);
        box.setRandomColors();
        box.setModelBound(new BoundingSphere());
        box.updateModelBound();
        
        
        GameTaskQueueManager.getManager().update(new Callable<Object>() {

            public Object call() throws Exception {
                box.lock();
                MouseInput.get().setCursorVisible(true);                
                return null;
            }
            
        });
        
        MouseInput.get().addListener(new MouseInputListener() {
            PickResults pr = new BoundingPickResults();

            public void onButton(int button, boolean pressed, int x, int y) {
                Logger.getAnonymousLogger().info("onButton");
            }

            public void onWheel(int wheelDelta, int x, int y) {
                Logger.getAnonymousLogger().info("onWheel");
            }

            public void onMove(int xDelta, int yDelta, int newX, int newY) {
                final Vector2f screenPos = new Vector2f();
                // Get the position that the mouse is pointing to
                screenPos.set(newX, newY);
                // Get the world location of that X,Y value
                
//                        Vector3f worldCoords = game.getDisplay().getWorldCoordinates(screenPos, 0);
//                        Vector3f worldCoords2 = game.getDisplay().getWorldCoordinates(screenPos, 1);
//                        Logger.getAnonymousLogger().info( screenPos.toString() );
////                         Create a ray startig from the camera, and going in the direction
////                         of the mouse's location
//                        Ray mouseRay = new Ray(worldCoords, worldCoords2
//                                        .subtractLocal(worldCoords).normalizeLocal());
////                         Does the mouse's ray intersect the box's world bounds?
//                        pr.clear();
//                        gameState.getRootNode().findPick(mouseRay, pr);
//
//                        for (int i = 0; i < pr.getNumber(); i++) {
//                                pr.getPickData(i).getTargetMesh().setRandomColors();
//                        }
                        
            }
            
        });
        
        box.updateRenderState();
        gameState.getRootNode().attachChild(box);
    }
    
    public static void main(String[] args) {
        new jmetest_standardgame();
    }
    
    class MyGameState extends DebugGameState {
        @Override
        public void update(float tpf) {
            super.update(tpf);
            
            PickResults pr = new BoundingPickResults();
            if (MouseInput.get().isButtonDown(0)) {
                Vector2f screenPos = new Vector2f();
                // Get the position that the mouse is pointing to

                screenPos.set(MouseInput.get().getXAbsolute(), MouseInput.get().getYAbsolute());

                Vector3f worldCoords = DisplaySystem.getDisplaySystem().getWorldCoordinates(screenPos, 0);
                Vector3f worldCoords2 = DisplaySystem.getDisplaySystem().getWorldCoordinates(screenPos, 1);
                Logger.getAnonymousLogger().info( screenPos.toString() );
//                         Create a ray starting from the camera, and going in the direction
//                         of the mouse's location
                Ray mouseRay = new Ray(worldCoords, worldCoords2
                                .subtractLocal(worldCoords).normalizeLocal());
//                         Does the mouse's ray intersect the box's world bounds?
                pr.clear();
                getRootNode().findPick(mouseRay, pr);

                for (int i = 0; i < pr.getNumber(); i++) {
                        pr.getPickData(i).getTargetMesh().setRandomColors();
                }
            }
        }
    }
}
