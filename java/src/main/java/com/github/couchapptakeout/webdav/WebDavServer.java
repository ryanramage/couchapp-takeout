/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.http11.Http11ResponseHandler;
import com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler;
import com.ettrema.berry.Berry;
import com.ettrema.berry.simple.SimpletonServer;
import com.ettrema.http.fs.SimpleSecurityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        SimpleSecurityManager security = new SimpleSecurityManager();
        security.setRealm("aRealm");
        Map<String,String> map = new HashMap<String, String>();
        map.put("me", "pwd");
        security.setNameAndPasswords(map);






        CouchResourceFactory resourceFactory = new CouchResourceFactory(connector);
        resourceFactory.setSecurityManager(security);
        resourceFactory.setMaxAgeSeconds(3600l);


        AuthenticationService authService = new AuthenticationService();
        authService.setDisableDigest(false);
        authService.setDisableBasic(true);


        com.bradmcevoy.http.webdav.DefaultWebDavResponseHandler dwdrh = new DefaultWebDavResponseHandler(authService);


        HttpManager httpManager = new HttpManager( resourceFactory, dwdrh, authService );
        List httpAdapters = new ArrayList();

        Http11ResponseHandler responseHandler = httpManager.getResponseHandler();


        JettyAdaptor jettyAdaptor = new JettyAdaptor();




        httpAdapters.add( jettyAdaptor);
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
