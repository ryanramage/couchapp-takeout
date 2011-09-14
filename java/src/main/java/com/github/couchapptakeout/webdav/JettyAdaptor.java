/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.ettrema.berry.HttpAdapter;
import com.ettrema.berry.RequestConsumer;
import com.ettrema.berry.jetty.JettyServletRequest;
import com.ettrema.berry.jetty.JettyServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;

/**
 *
 * @author ryan
 */
public class JettyAdaptor implements HttpAdapter {


    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JettyAdaptor.class);
    Server server;

    RequestConsumer rc;

    @Override
    public void setRequestConsumer(RequestConsumer rc) {
        this.rc = rc;
    }

    @Override
    public Integer getHttpPort() {
        return 8080;
    }

    @Override
    public void start() {



        server = new Server();
        final SelectChannelConnector connector = new SelectChannelConnector();
        connector.setHost("localhost");
        connector.setPort(8080);

        log.info("Accepting connections with " + connector);

        server.setConnectors(new Connector[]{connector});
        server.setStopAtShutdown(true);
        server.setSendServerVersion(false);



        server.setHandler(new JettyHandler());
        try {
            server.start();
            server.join();
        } catch (Exception ex) {
            Logger.getLogger(JettyAdaptor.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    private class JettyHandler extends AbstractHandler {
        @Override
        public void handle(String target, HttpServletRequest baseRequest, HttpServletResponse response, int i) throws IOException, ServletException {
            JettyServletRequest req = new JettyServletRequest(baseRequest);
            JettyServletResponse resp = new JettyServletResponse(response);

            rc.onRequest(req, resp);

        }

    }
}
