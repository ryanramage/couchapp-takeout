/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.DigestResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PropPatchableResource;
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
import com.bradmcevoy.http.webdav.PropPatchHandler.Fields;
import com.sun.swing.internal.plaf.basic.resources.basic;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author ryan
 */
public class MemoryResource implements Resource, LockableResource, DigestResource,  DeletableResource, GetableResource, MoveableResource, PropFindableResource, PropPatchableResource{
        protected Date createdDate ;
        protected Date dateModified;
        protected byte[] bytes;
        protected String contentType = "application/octet-stream";

    String name;
    CouchResourceFactory couchResourceFactory;
    String host;

    public  MemoryResource  (String name, String host, CouchResourceFactory couchFactory) {
        this.name = name;
        this.couchResourceFactory = couchFactory;
        this.host = host;
    }


    @Override
    public String getUniqueId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object authenticate(String user, String password) {
        return couchResourceFactory.getSecurityManager().authenticate(user, password);
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {
        return couchResourceFactory.getSecurityManager().authorise(request, method, auth, this);
    }

    @Override
    public String getRealm() {
        return couchResourceFactory.getRealm(host);
    }

    @Override
    public Date getModifiedDate() {
        return  dateModified;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public LockResult lock(LockTimeout lt, LockInfo li) throws NotAuthorizedException, PreConditionFailedException, LockedException {
        System.out.println("Attachment collecton  Lock");
        LockToken newToken = new LockToken(UUID.randomUUID().toString(), li, lt);
        return LockResult.success( newToken );
    }

    @Override
    public LockResult refreshLock(String string) throws NotAuthorizedException, PreConditionFailedException {
        System.out.println("Attachment collecton  Refresh");
        return LockResult.failed( LockResult.FailureReason.PRECONDITION_FAILED );
    }

    @Override
    public void unlock(String string) throws NotAuthorizedException, PreConditionFailedException {
        System.out.println("Attachment collecton Unlock");
        return;
    }

    @Override
    public LockToken getCurrentLock() {
        System.out.println("Attachment collecton Current");
        return null;
    }

    @Override
    public Object authenticate(DigestResponse digestRequest) {
        return couchResourceFactory.getSecurityManager().authenticate(digestRequest);
    }

    @Override
    public boolean isDigestAllowed() {
        return couchResourceFactory.isDigestAllowed();
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException, BadRequestException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        IOUtils.copy(bais, out);
        bais.close();
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        return couchResourceFactory.getMaxAgeSeconds();
    }

    @Override
    public String getContentType(String accepts) {
        return contentType;
    }

    @Override
    public Long getContentLength() {
        return new Long(bytes.length);
    }

    @Override
    public void moveTo(CollectionResource rDest, String name) throws ConflictException, NotAuthorizedException, BadRequestException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getCreateDate() {
        return createdDate;
    }

    @Override
    public void setProperties(Fields fields) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
