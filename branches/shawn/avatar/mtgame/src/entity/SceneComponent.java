/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/

package com.jme.app.mtgame.entity;

import com.jme.scene.Node;
import java.util.HashMap;

/**
* This is an entity component that implements the visual representation of 
* an entity.
* 
* @author Doug Twilleager
*/
public class SceneComponent extends EntityComponent {
   /**
    * The base node for the JME Scene Graph
    */
   private Node sceneRoot = null;

   /**
    * The default constructor
    */
   public SceneComponent() {
   }

   /**
    * Set the scene root.
    */
   public void setSceneRoot(Node scene) {
       sceneRoot = scene;
   }
   
   /**
    * Get the scene root
    */
   public Node getSceneRoot() {
       return (sceneRoot);
   }
   
   /**
    * Parse the known attributes.
    */
   public void parseAttributes(HashMap attributes) {
       
   }
}