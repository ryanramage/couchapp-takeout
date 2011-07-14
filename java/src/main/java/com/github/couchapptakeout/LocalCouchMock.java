/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.impl.StdCouchDbConnector;

/**
 *
 * @author ryan.ramage
 */
public class LocalCouchMock implements LocalCouch  {

    private CouchDbInstance instance = null;

    public LocalCouchMock() {

    }

    public LocalCouchMock(CouchDbInstance instance) {
        this.instance = instance;
    }

    @Override
    public CouchDbInstance getCouchInstance() throws CouchDBNotFoundException {
        if (instance != null) return instance;
        else throw  new CouchDBNotFoundException();
    }

    @Override
    public void installCouchDbEmbedded() throws CouchDbInstallException {
        // noop
    }

    @Override
    public CouchDbConnector getCouchConnector(String name, CouchDbInstance instance) {
        return  new StdCouchDbConnector(name, instance);
    }

    @Override
    public int getCouchPort() {
        return 5984;
    }

}
