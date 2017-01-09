package de.rwth_aachen.afu.raspager;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.List;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;



public class RasPagerWindow extends JFrame {
	private static final Logger log = Logger.getLogger(RasPagerWindow.class.getName());
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
	private JTextField delay;

	private final RasPagerService app;
	// private final Configuration config;
	// private final SDRTransmitter transmitter;
	private TimeSlots timeSlots = new TimeSlots();
	private final ResourceBundle texts;

	// constructor
	public RasPagerWindow(RasPagerService app, boolean withTrayIcon) {
		this.app = app;

		// Load locale stuff
		texts = ResourceBundle.getBundle("MainWindow");

		// set window preferences
		setTitle("RasPagerC9000");
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
				if (app.isRunning() && !showConfirmResource("askQuitTitle", "askQuitText")) {
					return;
				}

				if (app.isRunning()) {
					app.stopServer(false);
				}

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
		main.setPreferredSize(new Dimension(640, 440));
		main.setBounds(0, 0, WIDTH, HEIGHT);
		getContentPane().add(main, BorderLayout.SOUTH);


		// slot display bounds
		Rectangle slotDisplayBounds = new Rectangle(10, 68, 30, 260);

		// slot display label
		JLabel slotDisplayLabel = new JLabel(texts.getString("slotDisplayLabel"));
		slotDisplayLabel.setBounds(slotDisplayBounds.x - 2, slotDisplayBounds.y - 38, 50, 18);
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
					if (timeSlots.get(i)) {
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
				// TODO fix
				// if (state.scheduler == null) {
				// return;
				// }
				return;

				// Color color = g.getColor();
				// g.setColor(Color.green);
				//
				// // get slot count
				// int slot = TimeSlots.getSlotIndex(state.scheduler.getTime());
				// int slotCount = timeSlots.getSlotCount(String.format("%1x",
				// slot).charAt(0));
				//
				// // draw current slots (from slot to slot + slotCount) with
				// // different color
				// for (int i = 0; i < slotCount; i++) {
				// g.fillRect(x + 1, (slot + i) * step + 1, width - x - 1, step
				// - 1);
				// }
				//
				// g.setColor(Color.yellow);
				//
				// g.fillRect(x + 1, slot * step + 1, width - x - 1, step - 1);
				//
				// g.setColor(color);
			}
		};
		slotDisplay.setBounds(slotDisplayBounds);
		main.add(slotDisplay);

		// status display label
		JLabel statusDisplayLabel = new JLabel(texts.getString("statusDisplayLabel"));
		statusDisplayLabel.setBounds(200, 10, 60, 18);
		main.add(statusDisplayLabel);

		// status display
		statusDisplay = new JLabel(texts.getString("statusDisplayDis"));
		statusDisplay.setBounds(new Rectangle(263, 10, 120, 18));
		main.add(statusDisplay);

		// server start button
		startButton = new JButton(texts.getString("startButtonStart"));
		startButton.addActionListener((e) -> {
			if (app.isRunning()) {
				app.stopServer(false);
				startButton.setText(texts.getString("startButtonStart"));

			} else {
				app.startServer(false);
				startButton.setText(texts.getString("startButtonStop"));
			}
		});
		startButton.setBounds(new Rectangle(475, 10, 150, 18));
		main.add(startButton);

		// configuration panel
		JPanel configurationPanel = new JPanel(null);
		configurationPanel.setBorder(new TitledBorder(null, texts.getString("configurationPanel"), TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		configurationPanel.setBounds(new Rectangle(200, 30, 625, 352));
		main.add(configurationPanel);

		// master list bounds
		Rectangle masterListBounds = new Rectangle(0, 30, 150, 200);

		// master list label
		JLabel masterListLabel = new JLabel(texts.getString("masterListLabel"));
		masterListLabel.setBounds(12, 20, 70, 18);
		configurationPanel.add(masterListLabel);

		// master list
		masterList = new List();
		masterList.setName("masterList");

		// master list pane
		JScrollPane masterListPane = new JScrollPane(masterList);
		masterListPane.setBounds(new Rectangle(12, 38, 150, 218));
		configurationPanel.add(masterListPane);

		// port bounds
		Rectangle portBounds = new Rectangle(50, masterListBounds.y + masterListBounds.height + 15, 50, 18);

		// port label
		JLabel portLabel = new JLabel("Port:");
		portLabel.setBounds(12, 268, 50, 18);
		configurationPanel.add(portLabel);

		// port
		port = new JTextField();
		port.setBounds(new Rectangle(50, 268, 50, 18));
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
		JButton applyButton = new JButton(texts.getString("applyButton"));
		applyButton.addActionListener((e) -> {
			setConfig();
		});
		applyButton.setBounds(new Rectangle(12, 325, 130, 18));
		configurationPanel.add(applyButton);

		configButtonBounds.x += configButtonBounds.width + 10;
		configButtonBounds.width = 100;

		// config load button
		JButton loadButton = new JButton(texts.getString("loadButton"));
		loadButton.addActionListener((event) -> {
			JFileChooser fileChooser = new JFileChooser("");
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();

				try {
					app.getConfig().load(file.getPath());
				} catch (Exception e) {
					log.log(Level.SEVERE, "Invalid configuration file.", e);
					showErrorResource("invalidConfigTitle", "invalidConfigText");

					return;
				}

				loadConfig();
			}
		});

		loadButton.setBounds(new Rectangle(153, 325, 100, 18));
		configurationPanel.add(loadButton);

		configButtonBounds.x += configButtonBounds.width + 10;
		configButtonBounds.width = 120;

		// config save button
		JButton saveButton = new JButton(texts.getString("saveButton"));
		saveButton.addActionListener((event) -> {
			JFileChooser fileChooser = new JFileChooser("");
			if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();

				try {
					setConfig();
					app.getConfig().save(file.getPath());
				} catch (Exception ex) {
					log.log(Level.SEVERE, "Failed to save configuration file.", ex);
					showErrorResource("failedConfigTitle", "failedConfigText");

					return;
				}
			}
		});

		saveButton.setBounds(new Rectangle(265, 325, 110, 18));
		configurationPanel.add(saveButton);

		JPanel masterPanel = new JPanel();
		masterPanel.setBorder(new TitledBorder(null, texts.getString("masterPanel"), TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		masterPanel.setBounds(174, 22, 183, 92);
		configurationPanel.add(masterPanel);
		masterPanel.setLayout(null);

		// master name field
		masterIP = new JTextField();
		masterIP.setBounds(12, 20, 159, 18);
		masterPanel.add(masterIP);

		// master add button
		JButton masterAdd = new JButton(texts.getString("masterAdd"));
		masterAdd.setBounds(12, 42, 159, 18);
		masterPanel.add(masterAdd);

		// master remove button
		JButton masterRemove = new JButton(texts.getString("masterRemove"));
		masterRemove.setBounds(12, 64, 159, 18);
		masterPanel.add(masterRemove);

		masterRemove.addActionListener((e) -> {
			if (masterList.getSelectedItem() != null && showConfirmResource("delMasterTitle", "delMasterText")) {
				masterList.remove(masterList.getSelectedIndex());
			}
		});

		masterAdd.addActionListener((e) -> {
			String master = masterIP.getText();
			if (master.isEmpty()) {
				return;
			}

			// check if master is already in list
			for (String m : masterList.getItems()) {
				if (m.equalsIgnoreCase(master)) {
					showErrorResource("addMasterFailTitle", "addMasterFailText");
					return;
				}
			}

			masterList.add(master);
			masterIP.setText("");
		});

		// show window
		pack();
		setVisible(true);

		loadConfig();

		// create tray icon if requested
		if (withTrayIcon) {
			Image trayImage = Toolkit.getDefaultToolkit().getImage("icon.ico");

			PopupMenu trayMenu = new PopupMenu(texts.getString("trayMenu"));
			MenuItem menuItem = new MenuItem(texts.getString("trayMenuShow"));
			menuItem.addActionListener((e) -> {
				setExtendedState(Frame.NORMAL);
				setVisible(true);
			});
			trayMenu.add(menuItem);

			trayIcon = new TrayIcon(trayImage, "RasPager", trayMenu);
			try {
				SystemTray.getSystemTray().add(trayIcon);
			} catch (AWTException e) {
				log.warning("Failed to add tray icon.");
			}
		}
	}

	// set connection status
	public void setStatus(boolean connected) {
		if (connected) {
			statusDisplay.setText(texts.getString("statusDisplayCon"));
		} else {
			statusDisplay.setText(texts.getString("statusDisplayDis"));
		}
	}


	public void setConfig() {
		Configuration config = app.getConfig();

		config.setInt(ConfigKeys.NET_PORT, Integer.parseInt(port.getText()));
		config.setString(ConfigKeys.NET_MASTERS, String.join(" ", masterList.getItems()));


		if (app.isRunning()) {
			if (showConfirmResource("cfgRunningTitle", "cfgRunningText")) {
				app.stopServer(false);
				app.startServer(false);

				startButton.setText("Server stoppen");
			}
		}
	}

	public void loadConfig() {
		Configuration config = app.getConfig();

		port.setText(Integer.toString(config.getInt(ConfigKeys.NET_PORT, 1337)));

		// load masters
		masterList.removeAll();
		String value = config.getString(ConfigKeys.NET_MASTERS, null);
		if (value != null && !value.isEmpty()) {
			Arrays.stream(value.split(" +")).forEach((m) -> masterList.add(m));
		}
	}


	public void showError(String title, String message) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
	}

	private void showErrorResource(String title, String text) {
		JOptionPane.showMessageDialog(null, texts.getString(text), texts.getString(title), JOptionPane.ERROR_MESSAGE);
	}

	private boolean showConfirmResource(String title, String message) {
		return JOptionPane.showConfirmDialog(this, texts.getString(message), texts.getString(title),
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	public void updateTimeSlots(TimeSlots slots) {
		this.timeSlots = slots;
		slotDisplay.repaint();
	}
}