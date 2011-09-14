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
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ektorp.AttachmentInputStream;
import org.ektorp.CouchDbConnector;

/**
 *
 * @author ryan
 */
public class DocumentAttachmentCollection implements CollectionResource, Resource, DigestResource,
        PutableResource, PropFindableResource,  GetableResource, DeletableResource, MoveableResource, LockableResource, LockingCollectionResource {


    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DocumentAttachmentCollection.class);

    private ObjectNode node;
    private CouchDbConnector connector;
    String host;
    private Date d = new Date();

    String id;
    CouchResourceFactory couchFactory;


    public DocumentAttachmentCollection(ObjectNode node, CouchDbConnector connector, String host, String id, CouchResourceFactory couchFactory) {
        this.node = node;
        this.connector = connector;
        this.host = host;
        this.id = id;
        this.couchFactory = couchFactory;
    }


    private void setId(String id) {
        System.out.println("Doc attach col: " + id);
        this.id = id;
//        if (id.startsWith("._")) {
//            throw new RuntimeException("Not a mac drive");
//        }
//
//        if (id.endsWith("/")) {
//            this.id  = this.id.substring(0, id.length() - 1);
//        }
//        if (id.startsWith("_design:")) {
//            this.id  = this.id.replaceFirst("_design:", "_design/");
//        }
    }


    DocumentAttachmentCollection(String id, String host, CouchDbConnector connector, CouchResourceFactory couchFactory) {
        
        setId(id);
        try {
            System.out.println("Looking for doc: " + this.id);
            ObjectNode node = connector.get(ObjectNode.class, this.id);
            this.node = node;
            this.connector = connector;
            this.host = host;
            this.couchFactory = couchFactory;
            log.info("node: " + node);


        } catch (Exception e) {
            System.out.println("The doc: " + this.id + " is not found");
            throw new IllegalArgumentException("The doc: " + this.id + " is not found");
            //throw new
        }
    }

    DocumentAttachmentCollection(ObjectNode node, String host, CouchDbConnector connector, CouchResourceFactory couchFactory) {
        this.node = node;
        this.connector = connector;
        this.host = host;
        setId(node.get("_id").getTextValue());
        this.couchFactory = couchFactory;
    }


    @Override
    public Resource child(String name) {
        System.out.println("doc get child!");
        return new AttachmentResource(name, host, connector, node.get("_id").getTextValue(), couchFactory);
    }

    @Override
    public List<? extends Resource> getChildren() {
        System.out.println("doc get children");
        List<AttachmentResource> result = new ArrayList<AttachmentResource>();
        JsonNode attachments = node.get("_attachments");
        if (attachments == null) return result;
        for (Iterator<String> i = attachments.getFieldNames(); i.hasNext();) {
            result.add(new AttachmentResource(i.next(), host, connector, node.get("_id").getTextValue(), couchFactory));
        }                
        return result;
    }

    @Override
    public String getUniqueId() {
        System.out.println("doc get uniq id");
        return id;
    }

    @Override
    public String getName() {
        System.out.println("doc get id");
        //return node.get("_id").getTextValue();
        return id;
    }

    @Override
    public Object authenticate(String user, String password) {
        System.out.println("doc get auth");
        return couchFactory.getSecurityManager().authenticate(user, password);
    }


    @Override
    public boolean authorise(Request rqst, Method method, Auth auth) {
        System.out.println("doc get auth2");
        return couchFactory.getSecurityManager().authorise(rqst, method, auth, this);
    }

    @Override
    public String getRealm() {
        System.out.println("doc get realm");
        return couchFactory.getSecurityManager().getRealm(host);
    }

    @Override
    public Date getModifiedDate() {
        System.out.println("doc get modified");
        return d;
    }

    @Override
    public String checkRedirect(Request rqst) {
        System.out.println("doc get name");
        return null;
    }

    @Override
    public boolean isDigestAllowed() {
        System.out.println("doc get is digest ");
        return couchFactory.isDigestAllowed();
    }

    @Override
    public Resource createNew(String name, InputStream in, Long length, String contentType) throws IOException, ConflictException, NotAuthorizedException, BadRequestException {
        System.out.println("Create New, before copy bytes to " + name);



//        File dest = new File(new File("/Users/ryan/webdav"), name);
//            FileOutputStream out = null;
//        try {
//                out = new FileOutputStream(dest);
//                IOUtils.copy(in, out);
//        } finally {
//                IOUtils.closeQuietly(out);
//        }

        try {
            byte[] bytes = IOUtils.toByteArray(in);  // whole lotta mem
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            IOUtils.closeQuietly(in);

            log.info("After copy. Create new attachment. Length: " + bytes.length);

            

            ObjectNode node = connector.get(ObjectNode.class, this.id);
            AttachmentInputStream ais = new AttachmentInputStream(name, bais, contentType);
            connector.createAttachment(node.get("_id").getTextValue(), node.get("_rev").getTextValue(), ais);
            AttachmentResource.dates.put(id + name, new Date());
            return new AttachmentResource(name, host, connector, node.get("_id").getTextValue(), couchFactory);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Date getCreateDate() {
        System.out.println("doc get created date");
        return d;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> map, String string) throws IOException, NotAuthorizedException, BadRequestException {
        System.out.println("doc get send content");
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {
        System.out.println("doc get max age");
         return couchFactory.getMaxAgeSeconds();
    }

    @Override
    public String getContentType(String string) {
        System.out.println("doc get created content type");
        //return "application/json";
        return "httpd/unix-directory";
    }

    @Override
    public Long getContentLength() {
        System.out.println("doc get content lenght");
        return null;
    }

    @Override
    public Object authenticate(DigestResponse dr) {
        System.out.println("doc get auth dr");
        return couchFactory.getSecurityManager().authenticate(dr);
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {
        connector.delete(id, node.get("_rev").getTextValue());
    }

    @Override
    public void moveTo(CollectionResource cr, String newName) throws ConflictException, NotAuthorizedException, BadRequestException {
        // can only move to a All AllDocs Dir
        if (cr instanceof AllDocsDirectoryResource) {
            // perform copy/rename

            // for now, just copy the doc...

            //ObjectNode onode = (ObjectNode) node;
            //onode.r

            connector.create(newName, node);
            connector.delete(node);

            //node = connector.get(JsonNode.class, newName);


        } else throw new RuntimeException("Can only rename a doc");


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
    public LockToken createAndLock(String name, LockTimeout lt, LockInfo li) throws NotAuthorizedException {
        System.out.println("Attachment collecton create and lock");
        byte[] byes = {};

        ByteArrayInputStream bas = new ByteArrayInputStream(byes);
        try {
            createNew(name, bas, 0l, "application/binary");

            
        } catch (IOException ex) {
            Logger.getLogger(DocumentAttachmentCollection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConflictException ex) {
            Logger.getLogger(DocumentAttachmentCollection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadRequestException ex) {
            Logger.getLogger(DocumentAttachmentCollection.class.getName()).log(Level.SEVERE, null, ex);
        }


        LockToken t = new LockToken(name, li, lt);
        return t;
    }

}
