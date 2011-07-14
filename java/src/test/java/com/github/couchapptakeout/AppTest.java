/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import org.junit.Before;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author ryan.ramage
 */
public class AppTest {

    App app;
    LocalCouch mock ;
    CouchDbInstance couchMock ;
    CouchDbConnector connectorMock;


    public AppTest() {
    }



    @Test
    public void createLocalDatabaseNameTest() {
        App app2 = new App("choose.iriscouch.com", "choose", 80, null);
        String localDbName = app2.createLocalDbName();
        assertEquals("choose(choose_iriscouch_com)", localDbName);
    }

    @Test
    public void createLocalDatabaseNameTestPort() {
        App app2 = new App("choose.iriscouch.com", "choose", 5984, null);
        String localDbName = app2.createLocalDbName();
        assertEquals("choose(choose_iriscouch_com-5984)", localDbName);
    }

    @Test
    public void createLocalDatabaseNameLowerCase() {
        App app2 = new App("choose.irisCouch.com", "choose", 5984, null);
        String localDbName = app2.createLocalDbName();
        assertEquals("choose(choose_iriscouch_com-5984)", localDbName);
    }

    @Test
    public void testGetSrcReplicationUrl() {
        App app2 = new App("choose.iriscouch.com", "choose", 81, null);
        String result = app2.getSrcReplicationUrl(false);
        assertEquals("http://choose.iriscouch.com:81/choose", result);
    }

    /**
     * Test of loadNeeded method, of class App.
     */
    @Test
    public void testLoadNeededNoPassword() throws Exception {
        App app = new App("choose.irisCouch.com", "choose", 5984, null);
        setupMocksFor(app);
        replay(mock);
        replay(couchMock);
        replay(connectorMock);

        app.loadNeeded(false);

    }


    protected void setupMocksFor(App app) throws CouchDBNotFoundException {
        mock = createMock(LocalCouch.class);
        couchMock = createMock(CouchDbInstance.class);
        connectorMock = createNiceMock(CouchDbConnector.class);

        expect(mock.getCouchInstance()).andReturn(couchMock);
        expect(mock.getCouchConnector(app.createLocalDbName(), couchMock)).andReturn(connectorMock);
        app.setLocalCouchManager(mock);
    }
 

    // Some scenarios to consider

    // one app, one user
    // many apps, one user
    // many users, many apps



}