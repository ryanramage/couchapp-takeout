/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.IOException;

/**
 *
 * @author ryan.ramage
 */
public interface Unzipper {

    void doUnzip(String inputZip, String destinationDirectory) throws IOException;

}
