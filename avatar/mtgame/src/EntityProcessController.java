/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame;

import java.util.ArrayList;
import com.jme.app.mtgame.entity.*;

/**
 * This is the controller for all entity processes.  It handles triggers and
 * schedules the processes on the various threads.  Processes can be scheduled
 * on the entity processors, or the render thread.
 * 
 * @author Doug Twilleager
 */
public class EntityProcessController extends Thread {
    /**
     * The number of Entity Processor Threads
     */
    private int numEntityProcessors = 0;
    
    /**
     * The number of processors on the client machine.
     */
    private int numProcessors = 1;
    
    /**
     * The array of EntityProcessors
     */
    private EntityProcessor[] entityProcessor = null;
    
    /**
     * The number of threads currently running
     */
    private int numProcessorsWorking = 0;
    
    /**
     * The list of entities that wish to be triggered on every render frame
     */
    private ArrayList newFrameArmed = new ArrayList();
    
        
    /**
     * The list of entities that wish to be triggered on awt events
     */
    private ArrayList awtEventsArmed = new ArrayList();
    
    /**
     * The list of entities that with to be triggered after an amount of 
     * time has elapsed.
     */
    private ArrayList timeElapseArmed = new ArrayList();
    
    /**
     * The current list of triggered processors
     */
    
    private ArrayList processorsTriggered = new ArrayList();
    
    /**
     * An instant snapshot of processors we are processing this frame.
     */
    private ArrayList currentProcessors = null;
    
    /**
     * The systems RenderManager
     */
    RenderManager renderManager = null;
    
    /**
     * A flag to indicate whether we should run
     */
    private boolean done = false;
    
    /**
     * A flag to say whether or not we are waiting for work
     */
    private boolean waiting = false;
    
    /**
     * The number of available processors.  
     */
    private int availableProcessors = 0;
    
    /**
     * The default constructor
     */
    public EntityProcessController(RenderManager renderManager) {
        this.renderManager = renderManager;
                
        // Set the entity process controller for the render manager.
        renderManager.setEntityProcessController(this);
        
        numProcessors = Runtime.getRuntime().availableProcessors();
        
        // Just double it for now.
        numEntityProcessors = 2*numProcessors;
        
        // For initialization, all threads are running
        numProcessorsWorking = numEntityProcessors;
        
        entityProcessor = new EntityProcessor[numEntityProcessors];
        for (int i=0; i<numEntityProcessors; i++) {
            entityProcessor[i] = new EntityProcessor(this, i);
            entityProcessor[i].initialize();
        }

    }
    
    /**
     * Initialize the process controller
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
     * Initialize the controller
     */
    private synchronized void initController() {
        // For now, just notify the manager that we are ready
        notify();
    }
    
    /**
     * The main run loop
     */
    public void run() {
        ProcessorComponent[] runList = null;
        
        initController();
        while (!done) {
            // Gather the list of processor components to execute
            // This includes any chained processors
            runList = waitForProcessorsTriggered();

            // Hand off work until we are done with the compute phase
            for (int i=0; i<runList.length; i++) {
                // Assign the task.  This will wait for an available processor
                dispatchTask(runList[i]);
            }
            
            // Now, let the renderer complete the commit phase
            renderManager.runCommitList(runList);
            armProcessors(runList);
        } 
    }
    
    /**
     * This method hands runList work off to the worker threads unit it is done.
     */
    public synchronized void dispatchTask(ProcessorComponent pc) {
        int i=0;
        
        // Wait if no one is available
        if (availableProcessors == 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
        
        // Find the first available thread.
        for (i = 0; i < entityProcessor.length; i++) {
            // The processor will return true if it accepted the task
            //System.out.println("Trying to give task to " + i);
            //System.out.println("Processor: " + i + ", " + pc);
            if (entityProcessor[i].isAvailable()) {
                entityProcessor[i].setAvailable(false);
                entityProcessor[i].runTask(pc);
                availableProcessors--;
                //System.out.println(entityProcessor[i] + " accepted task: " + pc);
                break;
            }
        }
    }
    
    /**
     * This simply tells us that the processor is ready for work.
     */
    public synchronized void notifyDone(EntityProcessor ep) {
        ep.setAvailable(true);
        availableProcessors++;
        notify();
    }
    
    /**
     * Add an entity to be potentially processed
     */
    public void addEntity(Entity e) {
        ProcessorComponent pc = 
                (ProcessorComponent) e.getComponent(ProcessorComponent.class);
        ProcessorCollectionComponent pcc = 
                (ProcessorCollectionComponent)e.getComponent(ProcessorCollectionComponent.class);

        if (pc != null) {
            pc.setEntityProcessController(this);
            pc.initialize();
        }

        if (pcc != null) {
            ProcessorComponent[] procs = pcc.getProcessors();
            for (int i = 0; i < procs.length; i++) {
                procs[i].setEntityProcessController(this);
                procs[i].initialize();
            }
        }
    }
    
    /**
     * Add a processor component to the appropriate lists of possible arms
     */
    public void armProcessorComponent(ProcessorComponent pc, long triggerConditions) {
        boolean pendingTrigger = false;
        
        if ((triggerConditions & ProcessorComponent.NEW_FRAME_COND) != 0) {
            synchronized (newFrameArmed) {
                if (!newFrameArmed.contains(pc)) {
                    newFrameArmed.add(pc);
                }
            }
        }
        
        if ((triggerConditions & ProcessorComponent.TIMER_EXPIRED_COND) != 0) {
            synchronized (timeElapseArmed) {
                if (!timeElapseArmed.contains(pc)) {
                    timeElapseArmed.add(pc);
                }               
            }
        }        
                
        if ((triggerConditions & ProcessorComponent.AWTEVENT_COND) != 0) {
            synchronized (awtEventsArmed) {
                if (!awtEventsArmed.contains(pc)) {
                    if (pc instanceof AWTEventProcessorComponent) {
                        AWTEventProcessorComponent apc = (AWTEventProcessorComponent)pc;
                        if (apc.eventsPending()) {
                            pendingTrigger = true;
                        }
                    }
                    awtEventsArmed.add(pc);
                }
            }
            if (pendingTrigger) {
                triggerAWTEvent();
            }
        }
    }
    
    /**
     * This method waits for processors to trigger
     */
    private synchronized ProcessorComponent[] waitForProcessorsTriggered() {
        ProcessorComponent[] runList = new ProcessorComponent[0];
        
        if (processorsTriggered.size() == 0) {
            waiting = true;
            try {
                wait();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            waiting = false;
        }
        
        runList = (ProcessorComponent[]) processorsTriggered.toArray(runList);
        processorsTriggered.clear();
        
        return(runList);
    }
    
    /**
     * This re-arms processors once they are done commiting
     */
    private void armProcessors(ProcessorComponent[] runList) {
        for (int i=0; i<runList.length; i++) {
            armProcessorComponent(runList[i], runList[i].getArmingConditions());
        }
    }
    
    /**
     * Trigger everyone waiting on a new frame
     */
    public synchronized void triggerNewFrame() {
        ProcessorComponent pc = null;
        boolean anyTriggered = false;

        synchronized (newFrameArmed) {
            int length = newFrameArmed.size();
            for (int i = 0; i < length; i++) {
                pc = (ProcessorComponent) newFrameArmed.remove(0);
                pc.addTriggerCondition(ProcessorComponent.NEW_FRAME_COND);

                if (pc.getRunInRenderer()) {
                    renderManager.addTriggeredProcessor(pc);
                } else {
                    if (!processorsTriggered.contains(pc)) {
                        processorsTriggered.add(pc);
                        anyTriggered = true;
                    }
                }

            }
            if (anyTriggered && waiting) {
                notify();
            }
        }
    }

        
    /**
     * Trigger everyone waiting on a new frame
     */
    public synchronized void triggerAWTEvent() {
        AWTEventProcessorComponent apc = null;
        int index = 0;
        boolean anyTriggered = false;

        synchronized (awtEventsArmed) {
            int length = awtEventsArmed.size();
            for (int i = 0; i < length; i++) {
                apc = (AWTEventProcessorComponent) awtEventsArmed.get(index);
                if (apc.eventsPending()) {
                    apc.addTriggerCondition(ProcessorComponent.AWTEVENT_COND);
                    if (apc.getRunInRenderer()) {
                        renderManager.addTriggeredProcessor(apc);
                    } else {
                        if (!processorsTriggered.contains(apc)) {
                            processorsTriggered.add(apc);
                            anyTriggered = true;
                        }
                    }
                    awtEventsArmed.remove(index);
                } else {
                    // Just go to the next
                    index++;
                }

            }
            if (anyTriggered && waiting) {
                notify();
            }
        }
    }
}
