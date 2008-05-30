/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/

package com.jme.app.mtgame.entity;

import java.util.HashMap;
import java.util.ArrayList;
import com.jme.math.Vector3f;
import com.jme.math.Quaternion;
import com.jme.bounding.BoundingBox;
import com.jme.math.TransformMatrix;

/**
* The Entity object is the base object for all things in the game.  It uses
* a component based architecture which allows an Entity to acquire and adapt
* to new features dynamically.
* 
* @author Doug Twilleager
*/
public class Entity {
   /**
    * The name of the entity
    */
   private String name = null;
   /**
    * The component map contains all features of the entity.  The map is 
    * indexed by a string.  The default strings are declared above.
    */
   private HashMap componentMap = new HashMap();

   /**
    * The location of this Entity
    */
   private Vector3f position = new Vector3f();

   /**
    * A Transform that may be applied to this Entity
    */
   private TransformMatrix transform = new TransformMatrix();

   /**
    * The bounds of this entity.
    */
   private BoundingBox bounds = null;

   /**
    * The space in which this entity currently resides.
    */
   private Space currentSpace = null;

   /**
    * The partition within the current space which this entity resides
    * TODO: Should this be an object?
    */
   private int partition = 0;

   /**
    * The Sub-entities for this Entity.  It is just a list of entities
    * with a link back to their parent.
    */
   private ArrayList subEntities = new ArrayList();
   
   /**
    * The list of spaces that I belong in
    */
   private ArrayList spaces = new ArrayList();

   /**
    * If this is a subEntity, it has a link to it's parent.
    * The default is null - no parent.
    */
   private Entity parent = null;

   /**
    * The default Entity constructor.
    */
   public Entity(String name, HashMap attributeMap) {
       this.name = name;
       parseAttributes(attributeMap);
   }

   /**
    * Parse Entity Attributes
    * @param attributeMap
    */
   private void parseAttributes(HashMap attributeMap) {
       
   }
   
   /**
    * Add a component to the component map
    */
   public void addComponent(Class key, EntityComponent component) {
       componentMap.put(key, component);
       component.setEntity(this);
   }
   
   /**
    * Get a component from the map.
    */
   public EntityComponent getComponent(Class key) {
       return((EntityComponent)componentMap.get(key));
   }
   
   /**
    * Remove a component from the map
    */
   public void removeComponent(Class key) {
       EntityComponent c = (EntityComponent) componentMap.remove(key);
       if (c != null) {
           c.setEntity(null);
       }
   }

   /**
    * Set the position of this entity.
    * 
    * @param x The x coordinate in local space
    * @param y The y coordinate in local space
    * @param z The z coordinate in local space
    */
   public void setPosition(float x, float y, float z) {
       position.set(x, y, z);
   }

   /**
    * Get the position of this entity.
    * 
    * @param out The Vector3f to copy the position into.
    */
   public void getPosition(Vector3f out) {
       out.x = position.x;
       out.y = position.y;
       out.z = position.z;
   }

   /**
    * Set the transform for this entity.
    * 
    * @param quat The rotational component of the transform
    * @param trans The translational component of the transform
    * @param scale The scale component of the transform (currently not used)
    */
   public void setTransform(Quaternion quat, Vector3f trans, Vector3f scale) {
       transform.set(quat, trans);
   }

   /**
    * Get the transform for this entity.
    * 
    * @param out The TransformMatrix to copy the transform into.
    */
   public void getTransform(TransformMatrix out) {
       out.set(transform);
   }

   /**
    * Set the local bounds for this entity.  It is a BoundingBox.
    * 
    * @param in The BoundingBox to copy the data from.
    */
   public void setBounds(BoundingBox in) {
       in.clone(bounds);
   }

   /**
    * Get the local BoundingBox for this entity.
    * 
    * @param out The BoundingBox to copy the data into.
    */
   public void getBounds(BoundingBox out) {
       bounds.clone(out);
   }

   /**
    * Set the parent of an Entity
    */
   public void setParent(Entity entity) {
       parent = entity;
   }

   /**
    * Add a SubEntity to this entity.  It is managed by the parent Entity.
    */
   public void addEntity(Entity entity) {
       entity.setParent(this);
       subEntities.add(entity);
   }

   /**
    * Remove a SubEntity from the sub entity list
    */
   public void removeEntity(Entity entity) {
       subEntities.remove(entity);
       entity.setParent(null);
   }
   
   /**
    * Add this space to the list of spaces that this entity belongs in
    */
   public void addToSpaceList(Space s) {
       if (!spaces.contains(s)) {
           System.out.println(this + " is being added to " + s);
           spaces.add(s);
       }
   }
   
   /**
    * Remove this space from the list - it's okay for the space to not be on
    * the list.
    */
   public void removeFromSpaceList(Space s) {
       if (spaces.contains(s)) {
           System.out.println(this + " is being removed from " + s);
           spaces.remove(s);
       }
   }
   
   public String toString() {
       return(name);
   }
}