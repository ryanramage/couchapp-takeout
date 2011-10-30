/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import com.github.couchapptakeout.events.utils.DefaultUnzipper;
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




    @Test
    public void testFindFreePort() throws Exception{
        int port = DefaultCouchManager.findFreePort();
        System.out.println("Port: " + port);
        assertTrue(port > 0);
    }


    public void testInstallCouchDbEmbedded() throws CouchDbInstallException {
        DefaultCouchManager manager = new DefaultCouchManager();
        BasicCouchDownloader bcd = new BasicCouchDownloader("http://couchdb-binary-releases.googlecode.com/svn/trunk");
        DefaultUnzipper unzipper = new DefaultUnzipper();
        manager.setCouchDownloader(bcd);
        manager.setUnzipper(unzipper);

        manager.installCouchDbEmbedded();
    }


}