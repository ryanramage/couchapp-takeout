/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

/**
 *
 * @author ryan.ramage
 */
public class CouchDbInstallException extends Exception {

    /**
     * Creates a new instance of <code>CouchDbInstallException</code> without detail message.
     */
    public CouchDbInstallException() {
    }


    /**
     * Constructs an instance of <code>CouchDbInstallException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CouchDbInstallException(String msg) {
        super(msg);
    }
}
