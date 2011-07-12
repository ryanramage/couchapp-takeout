/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan.ramage
 */
public class DefaultCouchManagerTest {

    public DefaultCouchManagerTest() {
    }


    /**
     * Test of getEmbeddedCouchPort method, of class DefaultCouchManager.
     */
    @Test
    public void testGetEmbeddedCouchPort() throws Exception {
        DefaultCouchManager manager = new DefaultCouchManager();
        int port = manager.getEmbeddedCouchPort("src/test/resources/local.ini");
        assertEquals(81, port);
    }

    @Test
    public void testFindFreePort() throws Exception{
        int port = DefaultCouchManager.findFreePort();
        System.out.println("Port: " + port);
        assertTrue(port > 0);
    }


}