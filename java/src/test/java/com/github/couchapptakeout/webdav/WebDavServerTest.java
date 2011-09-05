/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.util.SardineException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.StdHttpClient;
import org.ektorp.http.HttpClient;
import org.ektorp.CouchDbConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan
 */
public class WebDavServerTest {

    static CouchDbConnector connector;
    static WebDavServer instance;

    @BeforeClass
    public static void  start() throws InterruptedException {
        connector = createLocalConnector("choose");
        instance = new WebDavServer();
        instance.start(connector);
    }

    @AfterClass
    public static void finsih() {
        instance.stop();
    }


    /**
     * Test of start method, of class WebDavServer.
     */
    @Test
    public void testListResources() throws InterruptedException, SardineException {


        System.out.println("Checking!");
        Sardine sardine = SardineFactory.begin();
        List<DavResource> resources = sardine.getResources("http://localhost:8080/");
        System.out.println("returned!");
        for (DavResource res : resources)
        {
             System.out.println(res);
        }

    }


    /**
     * Test of start method, of class WebDavServer.
     */
    @Test
    public void saveAndDeleteFile() throws InterruptedException, SardineException, FileNotFoundException {


        System.out.println("Checking!");
        Sardine sardine = SardineFactory.begin();
        InputStream fis = new FileInputStream(new File("src/test/resources/local.ini"));
        sardine.put("http://localhost:8080/Come_As_You_Are/local.ini", fis, "text/plain");

        List<DavResource> list = sardine.getResources("http://localhost:8080/Come_As_You_Are/local.ini");
        assertEquals(1, list.size());
        DavResource resource = list.get(0);
        assertEquals("local.ini", resource.getName());

        sardine.delete("http://localhost:8080/Come_As_You_Are/local.ini");

        if (sardine.exists("http://localhost:8080/Come_As_You_Are/local.ini")) {
            fail("Failed to delete the file");
        }

    }


     /**
     * Test of start method, of class WebDavServer.
     */
    @Test
    public void createRootDoc() throws InterruptedException, SardineException, FileNotFoundException {


        System.out.println("create root doc!");
        Sardine sardine = SardineFactory.begin();
        sardine.createDirectory("http://localhost:8080/NewDir/");

        if (!sardine.exists("http://localhost:8080/NewDir/")) {
            fail("Failed to delete the file");
        }

        sardine.delete("http://localhost:8080/NewDir/");
        if (sardine.exists("http://localhost:8080/NewDir/")) {
            fail("Failed to delete the file");
        }

    }




    protected static CouchDbConnector createLocalConnector(String db) {
        HttpClient httpClient = new StdHttpClient.Builder()
                                .host("localhost")
                                .port(5984).build();

        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        // if the second parameter is true, the database will be created if it doesn't exists
        return dbInstance.createConnector("choose", false);
        
    }

}