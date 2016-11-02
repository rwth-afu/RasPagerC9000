package funkrufSlave;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

public class MainWindow extends JFrame {
    private static final long serialVersionUID = 1L;


    private JPanel main;
    private final int WIDTH = 633;
    private final int HEIGHT = 450;

    private TrayIcon trayIcon;
    private List masterList;
    private JLabel statusDisplay;
    private JButton startButton;
    private JTextField masterIP;
    private JTextField port;
    private Canvas slotDisplay;

    private Log log = null;


    // write message into log file (log level normal)
    private void log(String message, int type) {
        log(message, type, Log.NORMAL);
    }

    // write message with given log level into log file
    private void log(String message, int type, int logLevel) {
        if (this.log != null) {
            this.log.println(message, type, logLevel);
        }
    }

    // constructor
    public MainWindow(Log log) {
        // set log
        this.log = log;

        // set window preferences
        setTitle("RasPager C9000");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // window listener
        addWindowListener(new WindowListener() {

            @Override
            public void windowActivated(WindowEvent arg0) {
            }

            @Override
            public void windowClosed(WindowEvent arg0) {
                System.exit(0);
            }

            @Override
            public void windowClosing(WindowEvent event) {
                // if server is running, ask to quit
                if (Main.running && !showConfirm("Beenden", "Der Server laeuft zur Zeit. Wollen Sie wirklich beenden?")) {
                    return;
                }

                // if server is running
                if (Main.running) {
                    // stop server
                    Main.stopServer();
                }

                // close log and serial port
                Main.closeLog();

                // dispose window
                dispose();
            }

            @Override
            public void windowDeactivated(WindowEvent arg0) {
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
                setVisible(true);
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
                setVisible(false);
            }

            @Override
            public void windowOpened(WindowEvent arg0) {
            }

        });

        // main panel
        main = new JPanel(null);
        main.setPreferredSize(new Dimension(490, 315));
        main.setBounds(0, 0, WIDTH, HEIGHT);
        getContentPane().add(main);

        // slot display label
        JLabel slotDisplayLabel = new JLabel("Slots");
        slotDisplayLabel.setBounds(12, 12, 50, 18);
        main.add(slotDisplayLabel);

        // slot display
        slotDisplay = new Canvas() {

            private static final long serialVersionUID = 1L;

            @Override
            public void paint(Graphics g) {
                super.paint(g);
                int width = getWidth() - 1;
                int height = getHeight() - 1;
                int x = 15;

                // draw border
                g.drawRect(x, 0, width - x, height);


                int step = getHeight() / 16;

                for (int i = 0, y = step; i < 16; y += step, i++) {

                    Font font = g.getFont();
                    Color color = g.getColor();

                    // if this is allowed slot
                    if (Main.timeSlots.getSlotsArray()[i]) {
                        // change font and color
                        g.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
                        g.setColor(Color.green);
                    }

                    g.drawString("" + Integer.toHexString(i).toUpperCase(), 0, y);
                    g.setFont(font);
                    g.setColor(color);

                    // draw line
                    if (i < 16 - 1) {
                        g.drawLine(x, y, width, y);
                    }

                }

                // if scheduler does not exist, function ends here
                if (Main.scheduler == null) {
                    return;
                }

                Color color = g.getColor();
                g.setColor(Color.green);

                // get slot count
                int slot = Main.timeSlots.getCurrentSlotInt(Main.scheduler.getTime());
                int slotCount = Main.timeSlots.checkSlot(String.format("%1x", slot).charAt(0));


                // draw current slots (from slot to slot + slotCount) with different color
                for (int i = 0; i < slotCount; i++) {
                    g.fillRect(x + 1, (slot + i) * step + 1, width - x - 1, step - 1);
                }

                g.setColor(Color.yellow);

                g.fillRect(x + 1, slot * step + 1, width - x - 1, step - 1);

                g.setColor(color);

            }

        };
        slotDisplay.setBounds(new Rectangle(22, 36, 30, 260));
        main.add(slotDisplay);

        // status display label
        JLabel statusDisplayLabel = new JLabel("Status:");
        statusDisplayLabel.setBounds(80, 12, 60, 18);
        main.add(statusDisplayLabel);

        // status display
        statusDisplay = new JLabel("getrennt");
        statusDisplay.setBounds(new Rectangle(143, 12, 120, 18));
        main.add(statusDisplay);

        // server start button
        startButton = new JButton("Server starten");
        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (Main.running) {
                    // stop server

                    Main.stopServer();
                    startButton.setText("Server starten");

                } else {
                    // start server

                    Main.startServer(false);
                    startButton.setText("Server stoppen");

                }

            }

        });
        startButton.setBounds(new Rectangle(319, 12, 150, 18));
        main.add(startButton);


        // configuration panel
        JPanel configurationPanel = new JPanel(null);
        configurationPanel.setBorder(new TitledBorder(null, "Konfiguration", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        configurationPanel.setBounds(new Rectangle(80, 32, 389, 268));
        main.add(configurationPanel);


        // master list bounds
        Rectangle masterListBounds = new Rectangle(0, 30, 150, 200);

        // master list label
        JLabel masterListLabel = new JLabel("Master");
        masterListLabel.setBounds(12, 20, 70, 18);
        configurationPanel.add(masterListLabel);

        // master list
        masterList = new List();
        masterList.setName("masterList");

        // master list pane
        JScrollPane masterListPane = new JScrollPane(masterList);
        masterListPane.setBounds(new Rectangle(12, 38, 150, 191));
        configurationPanel.add(masterListPane);

        // port bounds
        Rectangle portBounds = new Rectangle(50, masterListBounds.y + masterListBounds.height + 15, 50, 18);

        // port label
        JLabel portLabel = new JLabel("Port:");
        portLabel.setBounds(174, 126, 50, 18);
        configurationPanel.add(portLabel);

        // port
        port = new JTextField();
        port.setBounds(new Rectangle(212, 126, 50, 18));
        port.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent event) {
                char key = event.getKeyChar();

                // check if key is between 0 and 9
                if (key > '9' || key < '0') {
                    event.consume();
                }

            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
            }
        });
        configurationPanel.add(port);

        // config button bounds
        Rectangle configButtonBounds = new Rectangle(0, portBounds.y + portBounds.height + 20, 130, 18);

        // config apply button
        JButton applyButton = new JButton("Übernehmen");
        applyButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // set the config
                setConfig();
            }

        });
        applyButton.setBounds(new Rectangle(12, 241, 130, 18));
        configurationPanel.add(applyButton);


        configButtonBounds.x += configButtonBounds.width + 10;
        configButtonBounds.width = 100;


        // config load button
        JButton loadButton = new JButton("Laden");
        loadButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                // show open dialog
                JFileChooser fileChooser = new JFileChooser("");
                if (fileChooser.showOpenDialog(Main.mainWindow) == JFileChooser.APPROVE_OPTION) {

                    // get file name
                    File file = fileChooser.getSelectedFile();

                    try {
                        // try to load config
                        Main.config.load(file.getPath());

                    } catch (InvalidConfigFileException e) {
                        // catch errors
                        showError("Config laden", "Die Datei ist keine gueltige Config-Datei!");
                        log("Load Config # Keine gueltige config-Datei", Log.ERROR);

                        return;
                    }

                    // load config (means showing the config)
                    loadConfig();

                }

            }

        });
        loadButton.setBounds(new Rectangle(153, 241, 100, 18));
        configurationPanel.add(loadButton);


        configButtonBounds.x += configButtonBounds.width + 10;
        configButtonBounds.width = 120;


        // config save button
        JButton saveButton = new JButton("Speichern");
        saveButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                // show save dialog
                JFileChooser fileChooser = new JFileChooser("");
                if (fileChooser.showSaveDialog(Main.mainWindow) == JFileChooser.APPROVE_OPTION) {

                    // get file name
                    File file = fileChooser.getSelectedFile();

                    try {
                        // try to save config
                        setConfig();

                        Main.config.save(file.getPath());


                    } catch (FileNotFoundException e) {
                        // catch errors
                        showError("Config speichern", "Die Datei konnte nicht gespeichert werden!");

                        log("Save Config # Konnte config-Datei nicht speichern", Log.ERROR);

                        return;
                    }

                }

            }
        });
        saveButton.setBounds(new Rectangle(265, 241, 110, 18));
        configurationPanel.add(saveButton);

        JPanel panel_2 = new JPanel();
        panel_2.setBorder(new TitledBorder(null, "Neuer Master", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        panel_2.setBounds(174, 22, 183, 92);
        configurationPanel.add(panel_2);
        panel_2.setLayout(null);

        // master name field
        masterIP = new JTextField();
        masterIP.setBounds(12, 20, 159, 18);
        panel_2.add(masterIP);

        // master add button
        JButton masterAdd = new JButton("Hinzufügen");
        masterAdd.setBounds(12, 42, 159, 18);
        panel_2.add(masterAdd);

        // master remove button
        JButton masterRemove = new JButton("Löschen");
        masterRemove.setBounds(12, 64, 159, 18);
        panel_2.add(masterRemove);
        masterRemove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // check if there is a selection
                if (masterList.getSelectedItem() != null) {
                    // ask to remove
                    if (showConfirm("Master loeschen", "Soll der ausgewaehlt Master wirklich geloescht werden?")) {
                        // remove master
                        masterList.remove(masterList.getSelectedIndex());
                    }
                }

            }
        });


        // serial port bounds
        masterAdd.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                String master = masterIP.getText();
                String[] masters = masterList.getItems();

                // is textfield empty?
                if (master.equals("")) {
                    return;
                }

                // check if master is already in list
                for (int i = 0; i < masters.length; i++) {
                    if (masters[i].equals(master)) {

                        showError("Master hinzufuegen", "Master ist bereits in der Liste vorhanden!");
                        return;

                    }
                }

                // add master
                masterList.add(master);
                // clear textfield
                masterIP.setText("");

            }

        });

        // show window
        pack();
        setVisible(true);

        loadConfig();

        // create tray icon
        Image trayImage = Toolkit.getDefaultToolkit().getImage("icon.ico");

        PopupMenu trayMenu = new PopupMenu("FunkrufSlave");
        MenuItem menuItem = new MenuItem("Anzeigen");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // show window
                setExtendedState(Frame.NORMAL);
                setVisible(true);
            }

        });
        trayMenu.add(menuItem);


        trayIcon = new TrayIcon(trayImage, "FunkrufSlave", trayMenu);
        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            log("Kann TrayIcon nicht erstellen!", Log.INFO);
        }

    }

    // set connection status
    public void setStatus(boolean status) {
        this.statusDisplay.setText(status ? "verbunden" : "getrennt");
    }

    public void setConfig() {
        // set port
        Main.config.setPort(Integer.parseInt(port.getText()));

        // set master
        String master = "";
        for (int i = 0; i < masterList.getItemCount(); i++) {
            master += (i > 0 ? " " : "") + masterList.getItem(i);
        }
        Main.config.setMaster(master);

        if (Main.running) {
            if (showConfirm("Config uebernehmen", "Der Server laeuft bereits. Um die Einstellungen zu uebernehmen, muss der Server neugestartet werden. Soll er jetzt neugestartet werden?")) {
                Main.stopServer();
                Main.startServer(false);

                startButton.setText("Server stoppen");
            }
        }
    }

    public void loadConfig() {
        // load port
        port.setText("" + Main.config.getPort());

        // load master
        masterList.removeAll();

        String[] master = Main.config.getMaster();
        if (master != null) {
            for (int i = 0; i < master.length; i++) {
                masterList.add(master[i]);
            }
        }
    }

    // reset buttons
    public void resetButtons() {
        startButton.setText("Server starten");
        setStatus(false);
    }

    public void showError(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public boolean showConfirm(String title, String message) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void drawSlots() {
        slotDisplay.repaint();
    }
}