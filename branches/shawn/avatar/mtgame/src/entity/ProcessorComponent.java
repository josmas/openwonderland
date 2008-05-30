/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/

package com.jme.app.mtgame.entity;

import com.jme.app.mtgame.EntityProcessController;

import java.util.HashMap;

/**
* This component represents the executional aspect of an entity.  The processor
* component is scheduled and executed by the EntityManager.  A processor
* component may choose to run in certain conditions (a frame, a timer, an
* external event, ...)
* 
* The processor runs in two phases - compute and commit.  Due to the lack of
* multithread safeness in the current JME API, local calculations should be
* done in the compute phase, where no live JME scene elements can be modified.
* In the commit phase, all live JME scene elements may be modified.
* 
* Processors may statically, or dynamically, chain in execution.  This allows
* a processor to make calculations based upon a previous processors calculations.
* 
* @author Doug Twilleager
*/
public abstract class ProcessorComponent extends EntityComponent {
   /**
    * A collection of conditions.  When a processor is executed, it is given the
    * conditions for which it was scheduled.
    * TODO: Keep this as a long?
    */
   public static long NEW_FRAME_COND =         0x0001;
   public static long TIMER_EXPIRED_COND =      0x0002;
   public static long INITIALIZE_COND =        0x0004;
   public static long DESTROY_COND =           0x0008;
   public static long AWTEVENT_COND =           0x0010;

   /**
    * A reference to the entity process controller
    */
   private EntityProcessController entityProcessController = null;
   
   /**
    * The current conditions that will trigger execution of this process.
    */
   private long armingConditions = 0;

   /**
    * The actual condition that have triggered this execution
    */
   private long triggerConditions = 0;

   /**
    * The next process in the chain of executing processes.
    */
   private ProcessorComponent nextInChain = null;
   
   /**
    * A flag to indicate that this processor wishes to run in the Render thread
    */
   private boolean runInRenderer = false;

   /**
    * The compute callback to be defined by the subclass.
    * 
    * @param condition The XOR of all conditions which triggered this process.
    */
   public abstract void compute(long condition);

   /**
    * The commit callback to be defined by the subclass.
    * 
    * @param condition The XOR of all conditions which triggered this process.
    */
   public abstract void commit(long condition);
   
   /**
    * The initialize callback allows the process to set itself up and set its
    * initial trigger condition
    */
   public abstract void initialize();

   /**
    * Add a processor to the chain of execution.
    */
   public void addToChain(ProcessorComponent proc) {
       ProcessorComponent currentPC = this;
       ProcessorComponent nextPC = nextInChain;
       ProcessorComponent tmpPC = null;

       while (nextPC != null) {
           tmpPC = currentPC;
           currentPC = nextPC;
           nextPC = tmpPC.nextInChain;
       }
       currentPC.nextInChain = proc;
   }

   /**
    * Remove a processor from the chain
    */
   public void removeFromChain(ProcessorComponent proc) {
       ProcessorComponent currentPC = this;
       ProcessorComponent prevPC = null;

       while (currentPC != proc) {
           prevPC = currentPC;
           currentPC = prevPC.nextInChain;
       }

       prevPC.nextInChain = currentPC.nextInChain;
       proc.nextInChain = null;
   }

   /**
    * Return the next processor component in the chain
    */
   public ProcessorComponent getNextInChain() {
       return (nextInChain);
   }
   
   /**
    * Parse the known attributes.
    */
   public void parseAttributes(HashMap attributes) {
       
   }
   
   /**
    * Set the entity process controller
    */
   public void setEntityProcessController(EntityProcessController epc) {
       entityProcessController = epc;
   }
  
   /**
    * Set the trigger conditions for this Process
    */
   public void setArmingConditions(long armingConditions) {
       this.armingConditions = armingConditions;
       entityProcessController.armProcessorComponent(this, armingConditions);
   }

   /**
    * gets the current arming conditions
    */
   public long getArmingConditions() {
       return(armingConditions);
   }
   
   /**
    * Add a condition to the list of conditions that have triggerd this process
    */
   public void addTriggerCondition(long condition) {
       triggerConditions |= condition;
   }
   
   /**
    * Clear the current trigger condition
    */
   public void clearTriggerConditions() {
       triggerConditions = 0;
   }
   
   /**
    * Get the current triggered conditions
    */
   public long getCurrentTriggerConditions() {
       return (triggerConditions);
   }
   
   /**
    * Set the Run in Renderer flag
    */
   public void setRunInRenderer(boolean flag) {
       runInRenderer = flag;
   }
   
   /**
    * Get the Run in Renderer flag
    */
   public boolean getRunInRenderer() {
       return (runInRenderer);
   }
   
}