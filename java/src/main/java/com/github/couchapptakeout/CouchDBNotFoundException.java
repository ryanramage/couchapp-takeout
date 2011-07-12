/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

/**
 *
 * @author ryan.ramage
 */
public class CouchDBNotFoundException extends Exception {

    /**
     * Creates a new instance of <code>CouchDBNotFoundException</code> without detail message.
     */
    public CouchDBNotFoundException() {
    }


    /**
     * Constructs an instance of <code>CouchDBNotFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CouchDBNotFoundException(String msg) {
        super(msg);
    }
}
