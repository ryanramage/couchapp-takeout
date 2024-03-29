/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import org.apache.commons.lang.SystemUtils;

/**
 *
 * @author ryan
 */
public class OSUtils {
    
    public static String getGenericOSName() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "win";
        }
        else if (SystemUtils.IS_OS_MAC_OSX) {
            return "osx";

        } else if (SystemUtils.IS_OS_LINUX) {
            return "linux";
        }
        return null;
    }

    public static String getCouchIniLocation() {
         if (SystemUtils.IS_OS_WINDOWS) {
            return "etc/couchdb/local.ini";
        }
        else if (SystemUtils.IS_OS_MAC_OSX) {
            return "couchdb_trunk/etc/couchdb/local.ini";

        } else if (SystemUtils.IS_OS_LINUX) {
            return "couchdb_trunk/etc/couchdb/local.ini";
        }
        return null;
    }

    public static String getCouchBinLocation() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "bin/couchdb.bat";
        }
        else if (SystemUtils.IS_OS_MAC_OSX) {
            return "couchdb_trunk/bin/couchdb";

        } else if (SystemUtils.IS_OS_LINUX) {
            return "couchdb_trunk/bin/couchdb";
        }
        return null;
    }


}
