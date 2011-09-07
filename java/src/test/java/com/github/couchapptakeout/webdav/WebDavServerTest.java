/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;
import java.io.IOException;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.After;
import org.junit.Before;
import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.googlecode.sardine.util.SardineException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.easymock.EasyMock;
import org.ektorp.impl.StdCouchDbInstance;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.StdHttpClient;
import org.ektorp.http.HttpClient;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

/**
 *
 * @author ryan
 */
public class WebDavServerTest {


    WebDavServer instance;
    CouchDbInstance couchMock ;
    CouchDbConnector connector;

    @Before
    public  void  start() throws InterruptedException {
        connector = createMockConnector("choose");
        instance = new WebDavServer();
        instance.start(connector);
    }

    @After
    public void finsih() {
        instance.stop();
    }


    /**
     * Test of start method, of class WebDavServer.
     */
    @Test
    public void testListResources() throws InterruptedException, SardineException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode r = (ObjectNode)mapper.createObjectNode();
        r.put("total_rows", 1);
        r.put("offset", 0);
        ArrayNode rows = mapper.createArrayNode();
        r.put("rows", rows);

        {
            ObjectNode n = mapper.createObjectNode();
            n.put("id", "alpha");
            n.put("key", "alpha");
            n.put("value", "asd");
            n.put("doc", mapper.readTree("{\"_id\" : \"alpha\", \"_rev\" : \"1234\"}"));


            rows.add(n);
        }



        ViewResult vr = new ViewResult(r);


        expect(connector.queryView((ViewQuery) EasyMock.anyObject())).andReturn(vr);
        replay(connector);

        Sardine sardine = SardineFactory.begin();
        List<DavResource> resources = sardine.getResources("http://localhost:8080/");

        // THere should only be one directory call alpha

        for (DavResource dr : resources) {
            System.out.println(dr);
        }



        assertEquals(2, resources.size());
        DavResource dav = resources.get(1);
        System.out.println(dav.toString());
        assertEquals("alpha", dav.getName());



    }


    /**
     * Test of start method, of class WebDavServer.
     */
    @Test
    public void saveFile() throws InterruptedException, SardineException, FileNotFoundException, IOException {

        expect(connector.contains("Come_As_You_Are")).andReturn(Boolean.TRUE).anyTimes();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode NewDir = (ObjectNode) mapper.readTree("{ \"_id\" : \"Come_As_You_Are\", \"_rev\" : \"1234\" , \"_attachments\" : { \"local.ini\" : {}  }     }");
       

        expect(connector.get(ObjectNode.class, "Come_As_You_Are")).andReturn(NewDir).times(3);


        expect(connector.createAttachment("Come_As_You_Are", "local.ini", null)).andReturn("22222");


        ObjectNode NewDirWAttach = (ObjectNode) mapper.readTree("{ \"_id\" : \"Come_As_You_Are\", \"_rev\" : \"1234\" , \"_attachments\" : { \"local.ini\" : {}  }     }");

        expect(connector.get(JsonNode.class, "Come_As_You_Are")).andReturn(NewDirWAttach);
        

        //expect(connector.contains("NewDir")).andReturn(Boolean.TRUE); // for the delete request
        replay(connector);


        Sardine sardine = SardineFactory.begin();
        InputStream fis = new FileInputStream(new File("src/test/resources/local.ini"));
        sardine.put("http://localhost:8080/Come_As_You_Are/local.ini", fis, "text/plain");



    }


     /**
     * Test of start method, of class WebDavServer.
     */
    @Test
    public void createRootDoc() throws InterruptedException, SardineException, FileNotFoundException {

        expect(connector.contains("id")).andReturn(Boolean.FALSE);
        replay(connector);

        System.out.println("create root doc!");
        Sardine sardine = SardineFactory.begin();
        sardine.createDirectory("http://localhost:8080/NewDir/");
    }

     /**
     * Test of start method, of class WebDavServer.
     */
    @Test
    public void deleteRootDoc() throws InterruptedException, SardineException, FileNotFoundException, IOException {

        expect(connector.contains("NewDir")).andReturn(Boolean.TRUE).anyTimes();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode NewDir = (ObjectNode) mapper.readTree("{ \"_id\" : \"NewDir\", \"_rev\" : \"1234\"       }");

        expect(connector.get(ObjectNode.class, "NewDir")).andReturn(NewDir);

        //expect(connector.contains("NewDir")).andReturn(Boolean.TRUE); // for the delete request
        replay(connector);

        System.out.println("create root doc!");
        Sardine sardine = SardineFactory.begin();
        sardine.delete("http://localhost:8080/NewDir/");
    }


    protected  CouchDbConnector createMockConnector(String db) {



        return createNiceMock(CouchDbConnector.class);


    }

}