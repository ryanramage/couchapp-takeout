/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.couchapptakeout;


import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;

/**
 * This class represents the tray.
 *
 * @author ryan
 */
public class Tray   {


    public static final String MENU_SEPERATOR = "SEPERATOR";


    private String appName;
    private TrayIcon trayIcon;

    private Image baseImage;


    protected Tray() {
        // for unit tests
    }

    public Tray(ImageIcon icon, String appName,  List popupItems) {
        if (icon == null) {
            baseImage = createImage("/plate.png");
        } else {
            this.baseImage = icon.getImage();
        }
        this.appName = appName;
        SystemTray tray = SystemTray.getSystemTray();
        if (!SystemTray.isSupported()) {
            throw new RuntimeException("Tray is not supported");
        }
        trayIcon = new TrayIcon(baseImage, appName);
        final PopupMenu popup = createMenu(popupItems);
        trayIcon.setPopupMenu(popup);
        trayIcon.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                    if (MouseEvent.BUTTON3 == e.getButton()) return;
                    try {
                        Robot robot = new Robot();
                        // RIGHT CLICK
                        robot.mousePress(InputEvent.BUTTON3_MASK);
                        robot.mouseRelease(InputEvent.BUTTON3_MASK);
                    } catch (Exception exe) {
                        System.out.println("error=" + exe);
                    }
            }

            @Override
            public void mousePressed(MouseEvent e) {
               
            }

            @Override
            public void mouseReleased(MouseEvent e) {
               
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                
            }

            @Override
            public void mouseExited(MouseEvent e) {
                
            }
        });

        try {
            tray.add(trayIcon);
            registerEvents();
        } catch (AWTException e) {
            throw new RuntimeException("Cant start tray");
        }

    }


   protected void registerEvents() {

       EventBus.subscribeStrongly(TrayMessage.class, new EventSubscriber<TrayMessage>() {
            @Override
            public void onEvent(TrayMessage t) {
                System.out.println("Tray Message: " + t.getMessage());
                trayIcon.displayMessage(appName, t.getMessage(), t.getType());
            }
        });

        EventBus.subscribeStrongly(ExitApplicationMessage.class, new EventSubscriber<ExitApplicationMessage>() {
            @Override
            public void onEvent(ExitApplicationMessage t) {
                SystemTray tray = SystemTray.getSystemTray();
                tray.remove(trayIcon);
            }
        });
        EventBus.subscribeStrongly(AddMenuItemEvent.class, new EventSubscriber<AddMenuItemEvent>() {
            @Override
            public void onEvent(AddMenuItemEvent t) {
                trayIcon.getPopupMenu().add(t.getMenuItem());
            }
        });

   }



    protected final PopupMenu createMenu(List popupItems) {
        final PopupMenu popup = new PopupMenu("Menu");
        for (Object object : popupItems) {
            if (object instanceof MenuItem) {
                popup.add((MenuItem)object);
            }
            if (MENU_SEPERATOR.equals(object)) {
                popup.addSeparator();
            }
        }
        return popup;
    }




    //Obtain the image URL
    protected static Image createImage(String path) {
        URL imageURL = Tray.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL)).getImage();
        }

    }



}
