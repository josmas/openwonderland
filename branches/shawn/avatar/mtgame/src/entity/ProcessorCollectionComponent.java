/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.jme.app.mtgame.entity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This entity component consists of a collection of processor componants
 * 
 * @author Doug Twilleager
 */
public class ProcessorCollectionComponent extends EntityComponent {
    /**
     * The list of ProcessorComponent's
     */
    private ArrayList processors = new ArrayList();
    
    /**
     * The default constructor
     */
    public ProcessorCollectionComponent() {
    }
    
    /**
     * Add a processor
     */
    public void addProcessor(ProcessorComponent pc) {
        processors.add(pc);
    }
    
    /**
     * Get the processors
     * @return
     */
    public ProcessorComponent[] getProcessors() {
        ProcessorComponent[] procs = new ProcessorComponent[0];
        
        procs = (ProcessorComponent[]) processors.toArray(procs);
        return(procs);
    }
    
   /**
    * Parse the known attributes.
    */
   public void parseAttributes(HashMap attributes) {
   }
}
