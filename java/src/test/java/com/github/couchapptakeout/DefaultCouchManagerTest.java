/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import org.bushe.swing.event.EventBus;
import java.util.Random;
import org.bushe.swing.event.EventSubscriber;
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
    public void testDownloadWin() throws Exception {



        EventBus.subscribeStrongly(FileDownloader.class, new EventSubscriber<FileDownloader>() {
            @Override
            public void onEvent(FileDownloader t) {
                if (t.getStatus() == FileDownloader.COMPLETE) {
                    complete = true;
                }
                System.out.println(t.getStatus());
                System.out.println(t.getProgress());
            }
        });


        DefaultCouchManager manager = new DefaultCouchManager();
        long timestamp = System.currentTimeMillis();
        manager.downloadWin();

        while(!complete && ((System.currentTimeMillis() - timestamp) < 90000  )) {
            System.out.println("Sleep");
            Thread.sleep(1000);
        }

        if (!complete) fail("Download did not complete");
        
        

    }


}