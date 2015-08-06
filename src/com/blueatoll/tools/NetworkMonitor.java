package com.blueatoll.tools;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class NetworkMonitor
{
    public static void main(String[] args)
    {
        try
        {
            NetworkMonitor networkMon = new NetworkMonitor();
            networkMon.createAndAddApplicationToSystemTray();
            networkMon.startProcess();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This method creates the AWT items and add it to the System tray.
     * 
     * @throws IOException
     */
    private void createAndAddApplicationToSystemTray() throws IOException
    {
        // Check the SystemTray support
        if (!SystemTray.isSupported())
        {
            System.out.println("SystemTray is not supported");
            return;
        }

        final PopupMenu popup = new PopupMenu();
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        InputStream inputStream = classLoader
                .getResourceAsStream("com/blueatoll/tools/netmon.png");
        Image img = ImageIO.read(inputStream);

        final TrayIcon trayIcon = new TrayIcon(img, TOOL_TIP);
        this.processTrayIcon = trayIcon;
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a popup menu components
        MenuItem aboutItem = new MenuItem("About");

        CheckboxMenuItem autoSizeCheckBox = new CheckboxMenuItem(
                "Set auto size");
        CheckboxMenuItem toolTipCheckBox = new CheckboxMenuItem("Set tooltip");

        Menu displayMenu = new Menu("Display");

        MenuItem errorItem = new MenuItem("Error");
        MenuItem warningItem = new MenuItem("Warning");
        MenuItem infoItem = new MenuItem("Info");
        MenuItem noneItem = new MenuItem("None");

        MenuItem exitItem = new MenuItem("Exit");

        // Add components to popup menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(autoSizeCheckBox);
        popup.add(toolTipCheckBox);
        popup.addSeparator();
        popup.add(displayMenu);
        displayMenu.add(errorItem);
        displayMenu.add(warningItem);
        displayMenu.add(infoItem);
        displayMenu.add(noneItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        // Setting toolTipCheck and autoSizeCheckBox state as true
        toolTipCheckBox.setState(true);
        autoSizeCheckBox.setState(true);
        trayIcon.setImageAutoSize(true);

        try
        {
            tray.add(trayIcon);
        }
        catch (AWTException e)
        {
            System.out.println("TrayIcon could not be added.");
            return;
        }

        // Add listener to trayIcon.
        trayIcon.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(
                        null,
                        "This dialog box is run from System Tray");
            }
        });

        // Add listener to aboutItem.
        aboutItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(
                        null,
                        "This dialog box is run from the About menu item");
            }
        });

        // Add listener to autoSizeCheckBox.
        autoSizeCheckBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                int autoSizeCheckBoxId = e.getStateChange();
                if (autoSizeCheckBoxId == ItemEvent.SELECTED)
                {
                    trayIcon.setImageAutoSize(true);
                }
                else
                {
                    trayIcon.setImageAutoSize(false);
                }
            }
        });

        // Add listener to toolTipCheckBox.
        toolTipCheckBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                int toolTipCheckBoxId = e.getStateChange();
                if (toolTipCheckBoxId == ItemEvent.SELECTED)
                {
                    trayIcon.setToolTip(TOOL_TIP);
                }
                else
                {
                    trayIcon.setToolTip(null);
                }
            }
        });

        // Create listener for Display menu items.
        ActionListener listener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                MenuItem item = (MenuItem) e.getSource();
                System.out.println(item.getLabel());
                if ("Error".equals(item.getLabel()))
                {
                    trayIcon.displayMessage(
                            MESSAGE_HEADER,
                            "This is an error message",
                            TrayIcon.MessageType.ERROR);
                }
                else if ("Warning".equals(item.getLabel()))
                {
                    trayIcon.displayMessage(
                            MESSAGE_HEADER,
                            "This is a warning message",
                            TrayIcon.MessageType.WARNING);
                }
                else if ("Info".equals(item.getLabel()))
                {
                    trayIcon.displayMessage(
                            MESSAGE_HEADER,
                            "This is an info message",
                            TrayIcon.MessageType.INFO);
                }
                else if ("None".equals(item.getLabel()))
                {
                    trayIcon.displayMessage(
                            MESSAGE_HEADER,
                            "This is an ordinary message",
                            TrayIcon.MessageType.NONE);
                }
            }
        };

        // Add listeners to Display menu items.
        errorItem.addActionListener(listener);
        warningItem.addActionListener(listener);
        infoItem.addActionListener(listener);
        noneItem.addActionListener(listener);

        // Add listener to exitItem.
        exitItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                tray.remove(trayIcon);
                System.exit(0);
            }
        });
    }

    /**
     * This method will start a thread that will show a popup message from the
     * system tray after every 10 secs.
     */

    private void startProcess()
    {
        for (int ipCount = 0; ipCount < ipAddresses.length; ipCount++)
        {
            final int index = ipCount;
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    monitorNetwork(
                            ipAddresses[index][0],
                            ipAddresses[index][1],
                            ipAddresses[index][2]);
                }
            });

            thread.start();
        }
    }

    private void monitorNetwork(String ipType, String ip, String timeout)
    {
        if (usePing)
            testIP(ipType, ip, timeout);
        else
            testURL(ipType, ip, timeout);
    }

    private void testURL(String ipType, String url, String timeout)
    {
        String result = "";
        int processReturnValue = 0;
        boolean wasLastStatusError = false;
        HttpURLConnection connection = null;
        int code = -1;

        String ip = url.startsWith("http://") ? url : ("http://" + url);

        while (processReturnValue != -1)
        {
            try
            {
                URL siteURL = new URL(ip);
                connection = (HttpURLConnection) siteURL.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                code = connection.getResponseCode();

                if (code >= 400)
                    processReturnValue = 1;

                result = String.valueOf(code) + ": "
                        + connection.getResponseMessage();
            }
            catch (UnknownHostException | SocketTimeoutException e)
            {
                result = e.toString();
                processReturnValue = 1;
            }
            catch (Exception e)
            {
                result = e.toString();
                processReturnValue = -1;
            }
            finally
            {
                if (connection != null)
                {
                    connection.disconnect();
                    connection = null;
                }
                try
                {
                    logStatus(
                            url,
                            ipType,
                            String.valueOf(System.currentTimeMillis()),
                            String.valueOf(code),
                            result);

                    Thread.sleep(Long.parseLong(timeout));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            switch (processReturnValue)
            {
                case 0: // No error
                    if (wasLastStatusError)
                    {
                        processTrayIcon.displayMessage(
                                "Network Connection Established to " + ipType
                                        + " Network (" + url + ")",
                                result,
                                TrayIcon.MessageType.INFO);

                        wasLastStatusError = false;
                    }
                    break;
                case 1: // Connection error
                    if (!wasLastStatusError)
                    {
                        processTrayIcon.displayMessage(
                                "Network Connection Error to " + ipType
                                        + " Network (" + url + ")",
                                result,
                                TrayIcon.MessageType.ERROR);

                        wasLastStatusError = true;
                    }
                    break;
                case -1: // Configuration or programming error
                    if (!wasLastStatusError)
                    {
                        processTrayIcon.displayMessage(
                                "Configuration or Programming Error to "
                                        + ipType + " Network (" + url + ")",
                                result,
                                TrayIcon.MessageType.ERROR);

                        wasLastStatusError = true;
                    }
                    break;
            } // end case

        } // end while
    }

    private void testIP(String ipType, String ip, String timeout)
    {
        Process p = null;
        int processReturnValue = 0;
        boolean wasLastStatusError = false;

        while (processReturnValue != -1)
        {
            try
            {
                p = Runtime.getRuntime().exec(
                        "ping -t -w " + timeout + " " + ip);

                BufferedReader inputStream = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));

                String pingMessage = "";
                // reading output stream of the command
                while ((pingMessage = inputStream.readLine()) != null)
                {
                    for (int i = 0; i < pingResult.length; i++)
                    {
                        if (pingMessage.contains(pingResult[i][1]))
                        {
                            if (pingResult[i][0].equals("Error"))
                            {
                                if (!wasLastStatusError)
                                {
                                    processTrayIcon.displayMessage(
                                            "Network Connection Error",
                                            pingResult[i][2] + ": "
                                                    + pingMessage,
                                            TrayIcon.MessageType.ERROR);

                                    wasLastStatusError = true;
                                }
                            }
                            else if (pingResult[i][0].equals("Success"))
                            {
                                if (wasLastStatusError)
                                {
                                    processTrayIcon.displayMessage(
                                            "Network Connection Established",
                                            pingResult[i][2],
                                            TrayIcon.MessageType.INFO);

                                    wasLastStatusError = false;
                                }
                            }
                            else if (pingResult[i][0].equals("Wait"))
                            {
                                wasLastStatusError = false;
                            }
                            else
                            {
                                if (wasLastStatusError)
                                {
                                    processTrayIcon.displayMessage(
                                            "Unknown Network Message",
                                            pingMessage,
                                            TrayIcon.MessageType.WARNING);

                                    wasLastStatusError = false;
                                }
                            }

                            break; // Out of for loop
                        } // end if

                        logStatus(
                                ip,
                                ipType,
                                String.valueOf(System.currentTimeMillis()),
                                pingResult[i][0],
                                pingMessage);
                    } // end for
                } // end while
            }
            catch (Exception e)
            {
                e.printStackTrace();
                processReturnValue = -1;
            }

            processReturnValue = p.exitValue();
            try
            {
                Thread.sleep(Long.parseLong(timeout));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void logStatus(
            String ip,
            String ipType,
            String timeStamp,
            String messageType,
            String message) throws IOException
    {
        Files.write(
                Paths.get(ipType + "." + ip + ".csv"),
                (timeStamp
                        + ","
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new Date(Long.parseLong(timeStamp)))
                        + ", " + messageType + "," + message + "\n").getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    private static String[][] ipAddresses =
    {
            {
                    "Open", "www.google.com", "3000"
            },
            {
                    "VPN", "gitlab.medseek.com", "5000"
            }
    };
    private static final String[][] pingResult =
    {
            {
                    "Error", "Request timed out",
                    "The network connection may be unavailable"
            },
            {
                    "Error", "Ping request could not find",
                    "A VPN may not be connected or established"
            },
            {
                    "Wait", "Pinging",
                    "Detecting network connection availability"
            },
            {
                    "Success", "Reply from", "Network connection available"
            }
    };

    private boolean usePing = false;
    private String TOOL_TIP = "Blue Atoll Network Monitor";
    private String MESSAGE_HEADER = "Blue Atoll Network Monitor";
    private TrayIcon processTrayIcon = null;
}