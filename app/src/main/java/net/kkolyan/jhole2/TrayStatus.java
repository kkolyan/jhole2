package net.kkolyan.jhole2;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author NPlekhanov
 */
public class TrayStatus {
    public static void setup() {

        if (Desktop.isDesktopSupported()) {
            try {
                TrayIcon icon = new TrayIcon(ImageIO.read(JHoleClient.class.getClassLoader().getResource("jhole.gif")));
                MenuItem openConsole = new MenuItem("Console");
                openConsole.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            Desktop.getDesktop().browse(new URI("http://localhost:"+Console2.getPort()+"/log"));
                        } catch (Exception e1) {
                            throw new IllegalStateException(e1);
                        }
                    }
                });
                MenuItem exit = new MenuItem("Exit");
                exit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }
                });
                MenuItem openLog = new MenuItem("Open Log");
                openLog.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        List<File> files = new ArrayList<File>();
                        File logDir = new File("logs");
                        File[] logFiles = logDir.listFiles();
                        if (logFiles != null) {
                            for (File file: logFiles) {
                                if (file.getName().endsWith(".log")) {
                                    files.add(file);
                                }
                            }
                            File last = Collections.max(files, new Comparator<File>() {
                                @Override
                                public int compare(File o1, File o2) {
                                    return o1.getName().compareTo(o2.getName());
                                }
                            });
                            try {
                                Desktop.getDesktop().browse(last.toURI());
                            } catch (IOException e1) {
                                throw new IllegalStateException(e1);
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "Logs unavailable in " + logDir.getAbsoluteFile());
                        }
                    }
                });
                icon.setPopupMenu(new PopupMenu());
                icon.getPopupMenu().add(openLog);
                icon.getPopupMenu().add(openConsole);
                icon.getPopupMenu().add(exit);
                SystemTray.getSystemTray().add(icon);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (AWTException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
