/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

/**
 *
 * @author ryan
 */
public class ShowApplicationUrlMessage {

    private String relativeUrl;

    public ShowApplicationUrlMessage(String relativeUrl){
        this.relativeUrl = relativeUrl;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }
}
