/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.github.couchapptakeout.App;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author ryan
 */
public class CouchResourceFactory implements ResourceFactory{

    public enum ResourceType {
        ALLDOCS, DOCUMENT, ATTACHMENT
    }
   

    private CouchDbConnector connector;
    String contextPath;

    public CouchResourceFactory(CouchDbConnector connector) {
        this.connector = connector;
    }

      public String getSupportedLevels() {
        return "1,2";
     }


    @Override
    public Resource getResource(String host, String url) {
        System.out.println("Request " + host + " url " + url);

        url = trimContext(url);
        System.out.println("Request " + host + " url " + url);

        if ("favicon.ico".equals(url)) return null;

        ResourceType rt = determineType(url);

        System.out.println("Resource type: " + rt.name());

        try {
            switch(rt) {
                case ALLDOCS: return new AllDocsDirectoryResource(connector,host, this);
                case DOCUMENT: return new DocumentAttachmentCollection(url, host, connector);
                case ATTACHMENT: {
                    String[] daa = splitDocAndAttachment(url);
                    return new AttachmentResource(daa[1], host, connector, daa[0]);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        System.out.println("Why here? Why now?");
        return null;


    }


    public String trimContext(String url) {
        //url = url.replaceFirst( "/root", "");
        if (url.indexOf("/") == 0) {
            url = url.substring(1);
        }


        return url;
    }

    public String[] splitDocAndAttachment(String url) {
        String doc = url.substring(0, url.indexOf("/"));
        String attachment = url.substring(url.indexOf("/") + 1, url.length());
        return new String[] { doc, attachment};
    }

    public ResourceType determineType (String url) {

        if (StringUtils.isEmpty(url)) return ResourceType.ALLDOCS;

        String[] results = url.split("/");
        if (results.length == 1) {
            return ResourceType.DOCUMENT;
        }
        if (results.length > 1) {
            return ResourceType.ATTACHMENT;
        }
        return ResourceType.ALLDOCS;
    }






}
