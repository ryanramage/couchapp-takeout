/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan.ramage
 */
public class AppTest {

    public AppTest() {
    }


    /**
     * Test of main method, of class App.
     */
    @Test
    public void testMain() {
    }


    @Test
    public void createLocalDatabaseNameTest() {
        App app = new App("choose.iriscouch.com", "choose", 80, null, null);
        String localDbName = app.createLocalDbName();
        assertEquals("choose(choose_iriscouch_com)", localDbName);
    }

    @Test
    public void createLocalDatabaseNameTestPort() {
        App app = new App("choose.iriscouch.com", "choose", 5984, null, null);
        String localDbName = app.createLocalDbName();
        assertEquals("choose(choose_iriscouch_com-5984)", localDbName);
    }

    @Test
    public void createLocalDatabaseNameLowerCase() {
        App app = new App("choose.irisCouch.com", "choose", 5984, null, null);
        String localDbName = app.createLocalDbName();
        assertEquals("choose(choose_iriscouch_com-5984)", localDbName);
    }


    // Some scenarios to consider

    // one app, one user
    // many apps, one user
    // many users, many apps



}