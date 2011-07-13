/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ryan.ramage
 */
public class BasicCouchDownloaderTest {

    String rootUrl = "http://couchdb-binary-releases.googlecode.com/svn/trunk";

    public BasicCouchDownloaderTest() {
    }


    /**
     * Test of download method, of class BasicCouchDownloader.
     */

    // move to an integration type test

    public void testDownload() throws Exception {
        BasicCouchDownloader dl = new BasicCouchDownloader(rootUrl);
        File dest = new File("target");

        dl.downloadLatestCouch(dest);
    }

    /**
     * Test of getLatestVersion method, of class BasicCouchDownloader.
     */
    @Test
    public void testGetLatestVersion() {
        BasicCouchDownloader dl = new BasicCouchDownloader(rootUrl);
        String latestVersion = dl.getLatestVersion();
        assertEquals("1.1.0", latestVersion);
    }

}