package de.rwth_aachen.afu.raspager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Deque;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.rwth_aachen.afu.raspager.c9000.C9000Transmitter;

final class RasPagerService {
	private static final Logger log = Logger.getLogger(RasPagerService.class.getName());

	// TODO Get rid of all these static global vars
	private ThreadWrapper<Server> server;
	private boolean running = false;

	private Timer timer = new Timer();
	private final Deque<Message> messages = new ConcurrentLinkedDeque<>();
	private final C9000Transmitter transmitter = new C9000Transmitter();
	private final Configuration config;
	private final RasPagerWindow window;
	private Scheduler scheduler;

	public RasPagerService(Configuration config, boolean startService, boolean withTrayIcon)
			throws FileNotFoundException, IOException {
		this.config = config;

		if (!startService) {
			window = new RasPagerWindow(this, withTrayIcon);
		} else {
			window = null;
		}
	}

	public Configuration getConfig() {
		return config;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isServerRunning() {
		return server != null;
	}

	public C9000Transmitter getTransmitter() {
		return transmitter;
	}


	public void startScheduler(boolean searching) {
		try {
			transmitter.init(config);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Failed to init transmitter.", ex);

			String msg = ex.getMessage();
			if (msg == null || msg.isEmpty()) {
				msg = ex.getClass().getName();
			}

			if (window != null) {
				window.showError("Failed to init transmitter", msg);
			}

			return;
		}

		int period = 100;
		scheduler = new Scheduler(messages, transmitter);

		if (window != null) {
			scheduler.setUpdateTimeSlotsHandler(window::updateTimeSlots);
		}

		if (server != null) {
			server.getJob().setGetTimeHandler(scheduler::getTime);
			server.getJob().setTimeCorrectionHandler(scheduler::correctTime);
			server.getJob().setTimeSlotsHandler(scheduler::setTimeSlots);

			if (window != null) {
				server.getJob().setConnectionHandler(() -> {
					window.setStatus(true);
				});

				server.getJob().setDisconnectHandler(() -> {
					window.setStatus(false);
				});
			}
		}

		timer = new Timer();
		timer.schedule(scheduler, 100, period);
	}

	public void stopScheduler() {
		if (scheduler != null) {
			scheduler.cancel();
			scheduler = null;
		}

		try {
			transmitter.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Failed to close transmitter.", e);
		}
	}

	public void startServer(boolean join) {
		if (server == null) {
			int port = config.getInt(ConfigKeys.NET_PORT, 1337);
			String[] masters = null;
			if (config.contains(ConfigKeys.NET_MASTERS)) {
				String v = config.getString(ConfigKeys.NET_MASTERS);
				masters = v.split(" +");
			}

			Server srv = new Server(port, masters);
			// Register event handlers
			srv.setAddMessageHandler(messages::push);
			// Create new server thread
			server = new ThreadWrapper<Server>(srv);
		}

		// start scheduler (not searching)
		startScheduler(false);

		server.start();

		running = true;
		log.info("Server is running.");

		if (join) {
			try {
				server.join();
			} catch (InterruptedException e) {
				log.log(Level.SEVERE, "Server thread interrupted.", e);
			}

			stopServer(true);
		}

		if (window != null) {
			window.setStatus(false);
		}
	}

	public void stopServer(boolean error) {
		log.info("Server is shutting down.");

		// if there was no error, halt server
		if (server != null) {
			server.getJob().shutdown();
		}

		server = null;

		// set running to false
		running = false;

		// stop scheduler
		stopScheduler();

		messages.clear();

		log.info("Server stopped.");
	}

	public void serverError(String message) {
		// set running to false
		running = false;

		// stop scheduler
		stopScheduler();

		server = null;

		if (window != null) {
			window.showError("Server Error", message);
		}
	}

	public void run() {
		if (window == null) {
			startServer(true);
		}
	}

	public void shutdown() {
		try {
			if (transmitter != null) {
				transmitter.close();
			}
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Failed to close transmitter.", t);
		}

		timer.cancel();
	}

	public RasPagerWindow getWindow() {
		// TODO replace
		return window;
	}
}
