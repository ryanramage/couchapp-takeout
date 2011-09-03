/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.StdHttpClient;
import org.ektorp.http.HttpClient;
import org.ektorp.CouchDbConnector;
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
public class WebDavServerTest {



    /**
     * Test of start method, of class WebDavServer.
     */
    @Test
    public void testStart() throws InterruptedException {
        System.out.println("start");
        CouchDbConnector connector = createConnector();
        WebDavServer instance = new WebDavServer();
        instance.start(connector);


    }


    protected CouchDbConnector createConnector() {
        HttpClient httpClient = new StdHttpClient.Builder()
                                .host("localhost")
                                .port(5984).build();

        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        // if the second parameter is true, the database will be created if it doesn't exists
        return dbInstance.createConnector("choose", false);
        
    }

}