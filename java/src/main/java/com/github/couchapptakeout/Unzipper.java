/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.io.IOException;
import java.io.File;

/**
 *
 * @author ryan.ramage
 */
public interface Unzipper {

    void doUnzip(File zipfile, File directory) throws IOException;

}
