/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import org.ektorp.CouchDbInstance;

/**
 *
 * @author ryan.ramage
 */
public interface LocalCouch {

    CouchDbInstance getCouchInstance() throws CouchDBNotFoundException;
    void installCouchDbEmbedded() throws CouchDbInstallException;
    
}
