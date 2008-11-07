/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.runner.wrapper;

import java.io.InputStreamReader;
import java.net.URL;

/**
 *
 * @author jkaplan
 */
public class Reader {
    public static void main(String[] args) {
        try {
            /* Open an HTTP connection to the Jersey RESTful service */
            URL url = new URL("http://localhost:8080/wonderland-web-runner/services/list");
            System.out.println("url = " + url.toExternalForm());
            RunnerListWrapper w = RunnerListWrapper.decode(new InputStreamReader(url.openStream()));
        
            for (RunnerWrapper rw : w.getRunners()) {
                System.out.println(rw);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
