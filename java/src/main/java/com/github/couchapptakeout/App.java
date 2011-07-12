/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import org.bushe.swing.event.EventServiceLocator;
import org.bushe.swing.event.ThreadSafeEventService;

/**
 *
 * @author ryan.ramage
 */
public class App {
    public static void main(String[] args) {
        try {
            // for the event bus
            System.setProperty(EventServiceLocator.SERVICE_NAME_EVENT_BUS, ThreadSafeEventService.class.getName());
            UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());



        } catch (Exception ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
