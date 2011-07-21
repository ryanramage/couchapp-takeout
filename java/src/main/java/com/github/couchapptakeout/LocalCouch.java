/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;

/**
 *
 * @author ryan.ramage
 */
public interface LocalCouch {

    void setCredentials(String username, String password);

    CouchDbInstance getCouchInstance() throws CouchDBNotFoundException;
    CouchDbConnector getCouchConnector(String name, CouchDbInstance instance);
    void installCouchDbEmbedded() throws CouchDbInstallException;
    int getCouchPort();
    
}
