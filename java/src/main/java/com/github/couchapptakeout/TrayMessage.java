/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.awt.TrayIcon;

/**
 *
 * @author ryan.ramage
 */
public class TrayMessage {
   private String title;
    private String message;
    private TrayIcon.MessageType type;


    public TrayMessage(String title, String message, TrayIcon.MessageType type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the type
     */
    public TrayIcon.MessageType getType() {
        return type;
    }
}
