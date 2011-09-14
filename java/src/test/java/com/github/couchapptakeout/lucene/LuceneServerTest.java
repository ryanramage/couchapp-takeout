/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.lucene;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan
 */
public class LuceneServerTest {

    public LuceneServerTest() {
    }

    /**
     * Test of start method, of class LuceneServer.
     */
    @Test
    public void testStart() throws Exception {
        System.out.println("start");
        
        final LuceneServer instance = new LuceneServer();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    instance.start();
                } catch (ConfigurationException ex) {
                    Logger.getLogger(LuceneServerTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LuceneServerTest.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(LuceneServerTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
        Thread.sleep(1000);
        



        



    }


}