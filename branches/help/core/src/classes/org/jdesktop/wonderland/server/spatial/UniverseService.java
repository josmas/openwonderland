/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.server.spatial;

import com.sun.sgs.kernel.ComponentRegistry;
import com.sun.sgs.service.Service;
import com.sun.sgs.service.Transaction;
import com.sun.sgs.service.TransactionParticipant;
import com.sun.sgs.service.TransactionProxy;
import java.util.Properties;

/**
 *
 * @author paulby
 */
public class UniverseService implements Service, TransactionParticipant {

    public UniverseService(Properties prop,
                           ComponentRegistry registry,
                           TransactionProxy transactionProxy) {

    }

    public String getName() {
        return "UniverseService";
    }

    public void ready() throws Exception {

    }

    public boolean shutdown() {
        return true; // Success
    }

    public boolean prepare(Transaction arg0) throws Exception {
        System.err.println("Prepare");
        return true;
    }

    public void commit(Transaction arg0) {
        System.err.println("Commit");
    }

    public void prepareAndCommit(Transaction arg0) throws Exception {
        System.err.println("Prepare&Commit");
    }

    public void abort(Transaction arg0) {
        System.err.println("abort");
    }

    public String getTypeName() {
        return getName();
    }

}
