package funkrufSlave;

import java.awt.*;
import java.util.Deque;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Main {
    public static final String VERSION = "1.3";

    public static ServerThread server;
    public static Deque<Message> messageQueue;
    public static Timer timer;
    public static Scheduler scheduler;
    public static TimeSlots timeSlots;

    public static MainWindow mainWindow = null;
    public static boolean gui = true;

    public static boolean running = false;

    public static Config config = null;
    public static Log log = null;

    private static void log(String message, int type) {
        log(message, type, Log.NORMAL);
    }

    private static void log(String message, int type, int logLevel) {
        if (log != null) {
            log.println(message, type, logLevel);
        }
    }

    private static void printHelp() {
        System.out.println("syntax: FunkrufSlave.jar [-nogui] [-logfile=logfile] [-configfile=configfile] [-v] [-loglevel=loglevel]");
        System.out.println("-nogui\t\t\tConsole only, no gui");
        System.out.println("-logfile=logfile\tWrite log information into logfile");
        System.out.println("-configfile=configfile\tLoad given configfile and use it");
        System.out.println("-v, -verbose\t\tWrite log information to console (if in combination with -logfile, log information will be written to logfile AND console)");
        System.out.println("-loglevel=loglevel\tOverwrite loglevel with the given one");
    }

    // parse command line arguments
    private static void parseArguments(String[] args) {
        // valid parameters
        // -nogui, -logfile=, -configfile=, -v, -verbose

        String logfile = "";
        String configfile = "";
        int loglevel = -1;
        boolean verbose = false;

        for (String arg : args) {
            String[] parts = arg.split("=");

            if (parts[0].equals("-nogui")) {
                log("Parameter: nogui", Log.INFO);
                gui = false;
            } else if (parts[0].equals("-logfile") && parts.length == 2) {
                log("Parameter: logFile", Log.INFO);
                logfile = parts[1];
            } else if (parts[0].equals("-loglevel") && parts.length == 2) {
                log("Parameter: loglevel", Log.INFO);
                try {
                    loglevel = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    log("Loglevel hat kein gültiges Format!", Log.ERROR);
                    loglevel = -1;
                }
            } else if (parts[0].equals("-configfile") && parts.length == 2) {
                log("Parameter: configFile", Log.INFO);
                configfile = parts[1];
            } else if (parts[0].equals("-v") || parts[0].equals("-verbose")) {
                log("Parameter: verbose", Log.INFO);
                verbose = true;
            } else if (parts[0].equals("-version")) {
                System.out.println("FunkrufSlave - Version " + VERSION);

                System.exit(0);
            } else if (parts[0].equals("--help") || parts[0].equals("-help") || parts[0].equals("-h")) {
                printHelp();
                System.exit(0);
            } else {
                log("Parameter: Invalid # " + arg, Log.ERROR);
            }
        }

        if (!logfile.equals("") || verbose) {
            log = new Log(logfile, verbose);
        }

        if (!configfile.equals("")) {
            try {
                config = new Config(log, configfile);
            } catch (InvalidConfigFileException e) {
                log(e.getMessage(), Log.ERROR);
            }
        } else if (!gui) {
            System.out.println("Damit der FunkrufSlave ohne GUI gestartet werden kann, muss eine Konfigurationsdatei angegeben werden! -configfile=/foo/bar");
            System.exit(1);
        }

        if (loglevel != -1) {
            if (log != null) {
                log.setLogLevel(loglevel);
            }
        }
    }

    // set connection status
    public static void setStatus(boolean status) {
        if (mainWindow != null) {
            mainWindow.setStatus(status);
        }
    }

    // initialize
    private static void initialize() {
        // initialize timeSlots
        timeSlots = new TimeSlots();
    }

    // start scheduler (or search scheduler)
    private static void startScheduler(boolean searching) {
        if (timer == null) {
            timer = new Timer();
            scheduler = new Scheduler(log);
        }

        timer.schedule(scheduler, 100, 100);
    }

    // stop scheduler
    private static void stopScheduler() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (scheduler != null) {
            scheduler.cancel();
            scheduler = null;
        }
    }

    // start server (and join if needed)
    public static void startServer(boolean join) {
        if (messageQueue == null) {
            // initialize messageQueue
            messageQueue = new ConcurrentLinkedDeque<>();
        }

        if (server == null) {
            // initialize server thread
            server = new ServerThread(config.getPort(), messageQueue, log);
        }

        // start scheduler (not searching)
        startScheduler(false);

        // start server
        server.start();

        // set running to true
        running = true;
        log("Server is running.", Log.INFO);

        // if join is true
        if (join) {
            try {
                // join server thread
                server.join(0);
            } catch (InterruptedException e) {
                log("ServerThread interrupted", Log.INFO);
            }

            // stop server
            stopServer();
        }

        // set connection status to false
        if (mainWindow != null) {
            mainWindow.setStatus(false);
        }

    }

    // stop server
    public static void stopServer() {
        log("Server is going to shutdown.", Log.INFO);

        // if there was no error, halt server
        if (server != null) {
            server.halt();
        }

        server = null;

        // set running to false
        running = false;

        // stop scheduler
        stopScheduler();

        if (messageQueue != null) {
            messageQueue = null;
        }

        log("Server halted.", Log.INFO);

        // if there is no gui, the log has to be closed
        if (!gui) {
            log.close();
        } else {
            // if there is a gui, the start button has to be reseted
            if (mainWindow != null) {
                mainWindow.resetButtons();
            }
        }
    }

    // call on server error
    public static void serverError(String message) {
        // set running to false
        running = false;

        // stop scheduler
        stopScheduler();

        server = null;

        if (mainWindow != null) {
            // show error and reset start button
            mainWindow.showError("Server Error", message);
            mainWindow.resetButtons();
        }
    }

    // draw slots
    public static void drawSlots() {
        if (mainWindow != null) {
            mainWindow.drawSlots();
        }
    }

    // close log
    public static void closeLog() {
        if (log != null) {
            log.close();
        }
    }

    // remove socket thread from list
    public static void removeSocketThread(SocketThread thread) {
        if (server != null) {
            server.removeSocketThread(thread);
        }
    }

    // main
    public static void main(String[] args) {
        // write name, version and authors
        System.out.println("RasPager C9000 - Version " + VERSION + "\nby Ralf Wilke, Michael Delissen und Marvin Menzerath, powered by IHF RWTH Aachen\nNew Versions at https://github.com/rwth-afu/RasPagerC9000/releases\n");

        // parse arguments
        parseArguments(args);

        // load config, if not loaded
        if (config == null) {
            config = new Config(log);
            config.loadDefault();
        }

        // initialize
        initialize();

        // init DataSender
        Main.config.setDataSender(new DataSender());

        // set running to false
        running = false;

        // if gui
        if (gui && !GraphicsEnvironment.isHeadless()) {
            // create mainWindow
            mainWindow = new MainWindow(log);
        } else {
            // if no gui, start server and join
            startServer(true);
        }
    }
}
