/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import java.util.LinkedList;
import com.jme.app.mtgame.entity.*;


/**
 * This class represents a thread for processing entities.  The EntityProcessController
 * places entities on this processors queue to be scheduled.
 * 
 * @author Doug Twilleager
 */
public class EntityProcessor extends Thread {
    /**
     * The processor number
     */
    private int processorNumber = -1;
    
    /**
     * A boolean telling when us when to quit
     */
    private boolean done = false;
    
    /**
     * This flag indicates whether or not we are waiting for a task
     */
    private boolean waiting = true;
    
    /**
     * The Queue of Processor Components to be run.
     */
    private LinkedList queue = new LinkedList();
    
    /**
     * A flag indicating that we are available
     */
    private boolean available = false;
    
    /**
     * The name for this processor
     */
    private String name = null;
    
    /**
     * A reference back to the EntityProcessController
     */
    private EntityProcessController entityProcessController = null;
    
    /**
     * The default constructor
     */
    public EntityProcessor(EntityProcessController epc, int procNumber) {
        entityProcessController = epc;
        processorNumber = procNumber;
        name = "Processor " + procNumber;
    }
    
    /**
     * This starts the processor, and waits to be notified when the
     * thread is ready.
     */
    public synchronized void initialize() {
        this.start();
        try {
            wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    } 
    
    /**
     * Initialize the processor
     */
    private synchronized void initProcessor() {
        // For now, just notify the controller that we are ready
        available = true;
        notify();
    }
    
    public void run() {
        ProcessorComponent entityProcess = null;
        
        initProcessor();
        while (!done) {            
            // This synchonized method will block until there's something to do.
            entityProcess = getNextProcessorComponent();
            
            // Now compute this process and all of it's chains.
            entityProcess.compute(entityProcess.getCurrentTriggerConditions());
            
            entityProcess = entityProcess.getNextInChain();
            while (entityProcess != null) {
                entityProcess.compute(entityProcess.getCurrentTriggerConditions());
                entityProcess = entityProcess.getNextInChain();
            }
        }
    }
    
    /**
     * This method places a task on the processesor queue - if the processor
     * is waiting
     */
    public synchronized boolean runTask(ProcessorComponent pc) {
        if (waiting) {
            //System.out.println("Processor " + processorNumber + " grabbing task: " + pc);
            queue.add(pc);
            waiting = false;
            notify();
            return (true);
        } else {
            return (false);
        }
    }
    
    public boolean isAvailable() {
        return(available);
    }
    
    public void setAvailable(boolean flag) {
        available = flag;
    }
    
        
    public String toString() {
        return (name);
    }
    
    /** 
     * 
     * @return
     */
    private synchronized ProcessorComponent getNextProcessorComponent() {
        ProcessorComponent entityProcess = null;
           
        if (queue.isEmpty()) {
            waiting = true;
            entityProcessController.notifyDone(this);
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        } 
        entityProcess = (ProcessorComponent) queue.removeFirst();
        return (entityProcess);
    } 

}
