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
import org.ektorp.CouchDbConnector;

/**
 *
 * @author ryan
 */
public class WebDavServer {





    public void start(CouchDbConnector connector) throws InterruptedException {
        ResourceFactory resourceFactory = new CouchResourceFactory(connector);
        HttpManager httpManager = new HttpManager( resourceFactory );
        List httpAdapters = new ArrayList();

        Http11ResponseHandler responseHandler = httpManager.getResponseHandler();
	SimpletonServer simpletonServer = new SimpletonServer(100, 20, responseHandler);
	simpletonServer.setHttpPort(8080);


        httpAdapters.add( simpletonServer);
        Berry berry = new Berry( httpManager, httpAdapters );
        berry.start();


        Thread.sleep(60000);

    }


}
