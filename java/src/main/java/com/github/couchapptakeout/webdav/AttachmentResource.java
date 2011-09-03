/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.ettrema.http.fs.NullSecurityManager;
import java.util.Date;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author ryan
 */
public class AttachmentResource implements Resource {


    String name;
    String host;
    CouchDbConnector connector;
    String docId;
    private NullSecurityManager security = new NullSecurityManager();
    


    AttachmentResource(String name, String host, CouchDbConnector connector, String docId) {
        this.name = name;
        this.connector = connector;
        this.docId = docId;
        this.host = host;
    }


    @Override
    public String getUniqueId() {
        return docId + "/" + name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object authenticate(String user, String password) {
        return security.authenticate(user, password);
    }

    @Override
    public boolean authorise(Request rqst, Method method, Auth auth) {
        return security.authorise(rqst, method, auth, this);
    }

    @Override
    public String getRealm() {
        return security.getRealm(host);
    }

    @Override
    public Date getModifiedDate() {
        return new Date();
    }

    @Override
    public String checkRedirect(Request rqst) {
        return null;
    }

}
