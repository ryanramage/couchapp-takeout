/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.LockedException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.PreConditionFailedException;
import com.bradmcevoy.http.http11.auth.DigestResponse;
import com.ettrema.http.fs.NullSecurityManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.ViewResult;
import org.ektorp.ViewResult.Row;

/**
 *
 * @author ryan
 */

public class AllDocsDirectoryResource implements Resource, DigestResource,
        MakeCollectionableResource, PropFindableResource,  GetableResource, LockableResource, LockingCollectionResource{

    private CouchDbConnector connector;
    private NullSecurityManager security = new NullSecurityManager();



    String host;
    CouchResourceFactory factory;
    Date started = new Date();

    public AllDocsDirectoryResource(CouchDbConnector connector, String host, CouchResourceFactory factory) {
        System.out.println("All docs created eqauls");
        this.connector = connector;
        this.host = host;
        this.factory = factory;
    }


    @Override
    public String getUniqueId() {
        System.out.println("Get uni");
        return "_all_docs";
    }

    @Override
    public String getName() {
        System.out.println("Get name");
        return "/";
    }

    @Override
    public Object authenticate(String string, String string1) {
        System.out.println("Get authen");
        return security.authenticate(string, string);
    }

    @Override
    public boolean authorise(Request rqst, Method method, Auth auth) {
        System.out.println("Get auth");
        return security.authorise(rqst, method, auth, this);
    }

    @Override
    public String getRealm() {
        System.out.println("Get realm");
        return security.getRealm(host);
    }

    @Override
    public Date getModifiedDate() {
        System.out.println("Get modifiedi");
        return started;
    }

    @Override
    public String checkRedirect(Request rqst) {
        System.out.println("check redir");
        return null;
    }




    @Override
    public Object authenticate(DigestResponse dr) {
        System.out.println("auth dr");
        return security.authenticate(dr);
    }

    @Override
    public boolean isDigestAllowed() {
        System.out.println("Get is digest all");
        return true;
    }

    @Override
    public CollectionResource createCollection(String name) throws NotAuthorizedException, ConflictException, BadRequestException {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance);
        node.put("_id", name);
        connector.create(node);
        DocumentAttachmentCollection dac = new DocumentAttachmentCollection(node, host, connector);
        return dac;
    }

    @Override
    public Resource child(String id) {
        System.out.println("Get child: " + id);
        return new DocumentAttachmentCollection(id, host, connector);
    }

    @Override
    public List<? extends Resource> getChildren() {
        System.out.println("Get children");
        List<DocumentAttachmentCollection> results = new ArrayList<DocumentAttachmentCollection>();
        ViewQuery vq = new ViewQuery().allDocs().includeDocs(true);
        ViewResult vr = connector.queryView(vq);
        for (Row row : vr) {
            results.add(new DocumentAttachmentCollection((ObjectNode)row.getDocAsNode(),host, connector));
        }
        return results;
    }



    @Override
    public Date getCreateDate() {
        System.out.println("Get created");
        return started;
    }



    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {

        System.out.println("send content");
        ViewQuery vq = new ViewQuery().allDocs();


        InputStream is = connector.queryForStream(vq);

        IOUtils.copy(is, out);
        is.close();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        System.out.println("Get max age");
        return null;
    }

    @Override
    public String getContentType(String string) {
        System.out.println("Get cc type");
        return "application/json";
    }

    @Override
    public Long getContentLength() {
        System.out.println("Get c length");
        return null;
    }



    @Override
    public LockResult lock(LockTimeout lt, LockInfo li) throws NotAuthorizedException, PreConditionFailedException, LockedException {
        System.out.println("Lock");
        LockToken newToken = new LockToken(UUID.randomUUID().toString(), li, lt);
        return LockResult.success( newToken );
    }

    @Override
    public LockResult refreshLock(String string) throws NotAuthorizedException, PreConditionFailedException {
        System.out.println("Refresh");
        return LockResult.failed( LockResult.FailureReason.PRECONDITION_FAILED );
    }

    @Override
    public void unlock(String string) throws NotAuthorizedException, PreConditionFailedException {
        System.out.println("Unlock");
        return;
    }

    @Override
    public LockToken getCurrentLock() {
        System.out.println("Current");
        return null;
    }

    @Override
    public LockToken createAndLock(String string, LockTimeout lt, LockInfo li) throws NotAuthorizedException {
        ObjectMapper m = new ObjectMapper();
        ObjectNode n = m.createObjectNode();

        LockToken t = new LockToken(string, li, lt);
        return t;
    }




}
