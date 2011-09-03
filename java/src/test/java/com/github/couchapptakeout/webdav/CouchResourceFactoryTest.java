/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout.webdav;

import com.bradmcevoy.http.Resource;
import com.github.couchapptakeout.webdav.CouchResourceFactory.ResourceType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan
 */
public class CouchResourceFactoryTest {



    /**
     * Test of determineType method, of class CouchResourceFactory.
     */
    @Test
    public void testDetermineTypeEmptyUrl() {
        String url = "";
        CouchResourceFactory instance = new CouchResourceFactory(null);
        ResourceType expResult = ResourceType.ALLDOCS;
        ResourceType result = instance.determineType(url);
        assertEquals(expResult, result);
    }

    /**
     * Test of determineType method, of class CouchResourceFactory.
     */
    @Test
    public void testDetermineTypeNullUrl() {
        String url = null;
        CouchResourceFactory instance = new CouchResourceFactory(null);
        ResourceType expResult = ResourceType.ALLDOCS;
        ResourceType result = instance.determineType(url);
        assertEquals(expResult, result);
    }

    /**
     * Test of determineType method, of class CouchResourceFactory.
     */
    @Test
    public void testDetermineTypeNullSlash() {
        String url = "/";
        CouchResourceFactory instance = new CouchResourceFactory(null);
        ResourceType expResult = ResourceType.ALLDOCS;
        ResourceType result = instance.determineType(url);
        assertEquals(expResult, result);
    }


    /**
     * Test of determineType method, of class CouchResourceFactory.
     */
    @Test
    public void testDetermineTypeDocUrl() {
        String url = "doc";
        CouchResourceFactory instance = new CouchResourceFactory(null);
        ResourceType expResult = ResourceType.DOCUMENT;
        ResourceType result = instance.determineType(url);
        assertEquals(expResult, result);
    }

        /**
     * Test of determineType method, of class CouchResourceFactory.
     */
    @Test
    public void testDetermineTypeAttachmentUrl() {
        String url = "doc/attachment.jpg";
        CouchResourceFactory instance = new CouchResourceFactory(null);
        ResourceType expResult = ResourceType.ATTACHMENT;
        ResourceType result = instance.determineType(url);
        assertEquals(expResult, result);
    }

        /**
     * Test of determineType method, of class CouchResourceFactory.
     */
    @Test
    public void testDetermineTypeAttachmentDeepUrl() {
        String url = "doc/folder1/folder2/attachment.jpg";
        CouchResourceFactory instance = new CouchResourceFactory(null);
        ResourceType expResult = ResourceType.ATTACHMENT;
        ResourceType result = instance.determineType(url);
        assertEquals(expResult, result);
    }


    @Test
    public void testSplitDocAndAttachmentDeep() {
        String url = "doc/folder1/folder2/attachment.jpg";
        CouchResourceFactory instance = new CouchResourceFactory(null);
        String[] daa = instance.splitDocAndAttachment(url);
        assertEquals("doc", daa[0]);
        assertEquals("folder1/folder2/attachment.jpg", daa[1]);
    }

    @Test
    public void testSplitDocAndAttachment() {
        String url = "doc/attachment.jpg";
        CouchResourceFactory instance = new CouchResourceFactory(null);
        String[] daa = instance.splitDocAndAttachment(url);
        assertEquals("doc", daa[0]);
        assertEquals("attachment.jpg", daa[1]);
    }
}