/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;

import java.awt.MenuItem;

/**
 *
 * @author ryan
 */
public class AddMenuItemEvent {
    MenuItem menu;

    public AddMenuItemEvent(MenuItem item) {
        this.menu = item;
    }

    public MenuItem getMenuItem() {
        return menu;
    }

}
