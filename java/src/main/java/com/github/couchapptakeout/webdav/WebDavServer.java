/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.http11.Http11ResponseHandler;
import com.ettrema.berry.Berry;
import com.ettrema.berry.simple.SimpletonServer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;

/**
 *
 * @author ryan
 */
public class WebDavServer {


    Berry berry;
    int port = 8080;


    public WebDavServer() {

    }

    public WebDavServer(int port) {
        this.port = port;
    }



    public void start(CouchDbConnector connector) throws InterruptedException {
        ResourceFactory resourceFactory = new CouchResourceFactory(connector);
        HttpManager httpManager = new HttpManager( resourceFactory );
        List httpAdapters = new ArrayList();

        Http11ResponseHandler responseHandler = httpManager.getResponseHandler();
	SimpletonServer simpletonServer = new SimpletonServer(100, 20, responseHandler);
	simpletonServer.setHttpPort(8080);


        httpAdapters.add( simpletonServer);
        berry = new Berry( httpManager, httpAdapters );
        berry.start();
    }

    public void stop() {
        berry.stop();
    }


    public static void main(String[] args) {

        String db = "webdav";
        if (args != null && args.length > 1 && StringUtils.isEmpty(args[0])) {
            db = args[0];
        }

        CouchDbConnector connector = createLocalConnector(db);
        final WebDavServer instance = new WebDavServer();
        try {
            instance.start(connector);
        } catch (InterruptedException ex) {
            Logger.getLogger(WebDavServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { instance.stop(); }
        });

    }


    protected static CouchDbConnector createLocalConnector(String db) {
        HttpClient httpClient = new StdHttpClient.Builder()
                                .host("localhost")
                                .port(5984).build();

        CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
        // if the second parameter is true, the database will be created if it doesn't exists
        return dbInstance.createConnector(db, true);

    }

}
