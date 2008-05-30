/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/

package com.jme.app.mtgame.entity;

import java.util.HashMap;

/**
* This is the common base class for all EntityComponents
* 
* @author Doug Twilleager
*/
public abstract class EntityComponent {
   /**
    * The Entity of this component
    */
   private Entity entity = null;

   /**
    * The registry table of all known entity components
    */
   private static HashMap componentRegistry = new HashMap();

   /**
    * Set the entity of this component
    */
   public void setEntity(Entity ent) {
       entity = ent;
   }

   /**
    * Get the entity for this component
    */
   public Entity getEntity() {
       return (entity);
   }
   
   /**
    * Register a component type
    */
   public void registerComponent(Class key, String classname) {
       Class cl = null;
       
       try {
           cl = Class.forName(classname);
       } catch (ClassNotFoundException e) {
           System.out.println(e);
       }
       componentRegistry.put(key, cl);
   }
   
   /**
    * The EntityComponent factory
    */
   public static EntityComponent createComponent(Class key) {
       EntityComponent ec = null;
       
       Class cl = (Class)componentRegistry.get(key);
       
       try {           
       try {
           ec = (EntityComponent) cl.newInstance();
       } catch (IllegalAccessException e) {
           System.out.println(e);
       }
       } catch (InstantiationException e) {
           System.out.println(e);
       }
       
       return (ec);
   }
   
   /**
    * Parse the known attributes.
    */
   public abstract void parseAttributes(HashMap attributes);
   
}