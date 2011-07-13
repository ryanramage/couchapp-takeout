/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan.ramage
 */
public class DefaultCouchManagerTest {

    public DefaultCouchManagerTest() {
    }

     Boolean complete = false;


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
    public void testSetEmbeddedCouchPort() throws Exception {
        DefaultCouchManager manager = new DefaultCouchManager();
        int random = new Random().nextInt();
        manager.setEmbeddedCouchPort("src/test/resources/local-write.ini", random);

        // now check
        int port = manager.getEmbeddedCouchPort("src/test/resources/local-write.ini");
        System.out.println("random: " + random);
        assertEquals(random, port);
    }


    @Test
    public void testFindFreePort() throws Exception{
        int port = DefaultCouchManager.findFreePort();
        System.out.println("Port: " + port);
        assertTrue(port > 0);
    }

    @Test
    public void testInstallCouchDbEmbedded() throws CouchDbInstallException {
        DefaultCouchManager manager = new DefaultCouchManager();
        BasicCouchDownloader bcd = new BasicCouchDownloader("http://couchdb-binary-releases.googlecode.com/svn/trunk");
        DefaultUnzipper unzipper = new DefaultUnzipper();
        manager.setCouchDownloader(bcd);
        manager.setUnzipper(unzipper);

        manager.installCouchDbEmbedded();
    }


}