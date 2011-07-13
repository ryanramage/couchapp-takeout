/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.File;

/**
 *
 * @author ryan.ramage
 */
public interface CouchDownloader {

     File downloadLatestCouch(File destDir);
     void cancelDownload();

}
