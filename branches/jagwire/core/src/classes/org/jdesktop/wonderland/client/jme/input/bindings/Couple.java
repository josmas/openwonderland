package org.jdesktop.wonderland.client.jme.input.bindings;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ryan
 */
public  class  Couple<K,V> {
    
    private K first;
    private V second;
    
    public Couple() { }
    
    public Couple(K first, V second) {
        this.first = first;
        this.second = second;
    }

    public K getFirst() {
        return first;
    }

    public void setFirst(K first) {
        this.first = first;
    }

    public V getSecond() {
        return second;
    }

    public void setSecond(V second) {
        this.second = second;
    }
    
    
    
    
}
