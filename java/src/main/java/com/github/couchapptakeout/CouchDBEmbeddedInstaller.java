/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

/**
 *
 * @author ryan.ramage
 */
public interface CouchDBEmbeddedInstaller {
    boolean isCouchEmbeddedInstalled();
    void installCouchEmbedded();
    boolean isCouchEmbeddedLatestRelease();
    void updateCouchEmbedded();
}
