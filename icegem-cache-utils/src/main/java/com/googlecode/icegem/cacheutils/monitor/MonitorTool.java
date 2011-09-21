/*
 * Icegem, Extensions library for VMWare vFabric GemFire
 * 
 * Copyright (c) 2010-2011, Grid Dynamics Consulting Services Inc. or third-party  
 * contributors as indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  
 * 
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License v3, as published by the Free Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * You should have received a copy of the GNU Lesser General Public License v3
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.googlecode.icegem.cacheutils.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.MessagingException;

import com.googlecode.icegem.utils.PropertiesHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.googlecode.icegem.cacheutils.Tool;
import com.googlecode.icegem.cacheutils.common.Utils;
import com.googlecode.icegem.cacheutils.monitor.controller.NodesController;
import com.googlecode.icegem.cacheutils.monitor.controller.event.NodeEventHandler;
import com.googlecode.icegem.cacheutils.monitor.utils.EmailService;

/**
 * Periodically checks the distributed system status and sends mail in case of
 * failure
 */
public class MonitorTool extends Tool {
	private static final String HELP_OPTION = "help";

	private static final String TIMEOUT_OPTION = "timeout";

	private static final String PERIOD_OPTION = "period";

	private static final String LOCATORS_OPTION = "locators";

	private static final String ALL_OPTION = "all";

	private static final String SERVER_OPTION = "server";

	private static final Logger log = Logger.getLogger(MonitorTool.class);

	private static final long DEFAULT_TIMEOUT = 3000;

	private static final long DEFAULT_PERIOD = 10000;

	private boolean allOption;
	private String serverHostOption;
	private int serverPortOption;
	private long timeout = DEFAULT_TIMEOUT;
	private long period = DEFAULT_PERIOD;
	private String locators = null;

	private NodesController nodesController;
	private PropertiesHelper propertiesHelper;
	private Timer timer;

	private List<NodeEventHandler> customEventHandlersList = new ArrayList<NodeEventHandler>();

	/**
	 * Periodically running task which checks the system status
	 */
	private class IsAliveTimerTask extends TimerTask {

		@Override
		public void run() {
			try {
				nodesController.update();
			} catch (Throwable t) {
				log.error(Utils.currentDate() + "  Throwable caught", t);
				t.printStackTrace();
				try {
					EmailService
						.getInstance()
						.send(
							propertiesHelper
								.getStringProperty("icegem.cacheutils.monitor.email.exception.subject"),
							propertiesHelper
								.getStringProperty(
									"icegem.cacheutils.monitor.email.exception.content",
									t.getMessage(), new Date()));
				} catch (MessagingException me) {
					me.printStackTrace();
				}
			}

		}

	}

	/**
	 * configuration
	 * */
	private void init() {
		try {
			log.info(Utils.currentDate() + "");
			log.info(Utils.currentDate()
				+ "  --------------------------------------------------");
			log.info(Utils.currentDate() + "  Monitoring tool started");
			log.info(Utils.currentDate()
				+ "  --------------------------------------------------");

			propertiesHelper = new PropertiesHelper("/monitoring.properties");

			nodesController = new NodesController(propertiesHelper, locators,
				timeout);

			nodesController.addNodeEventHandler(new LoggerNodeEventHandler());

			for (NodeEventHandler handler : customEventHandlersList) {
				nodesController.addNodeEventHandler(handler);
			}

			timer = new Timer();
		} catch (Throwable t) {
			Utils.exitWithFailure("Throwable caught during the initialization",
				t);
		}
	}

	/**
	 * Starts the checking task
	 */
	private void start() {
		try {
			timer.schedule(new IsAliveTimerTask(), 0, period);
		} catch (Throwable t) {
			Utils.exitWithFailure("Throwable caught during the startup", t);
		}
	}

	public void shutdown() {
		nodesController.shutdown();

		timer.cancel();
		timer = null;
	}

	public static boolean isServerAlive(String host, int port, long timeout) {
		boolean serverAlive = false;

		try {
			PropertiesHelper propertiesHelper = new PropertiesHelper(
				"/monitoring.properties");

			NodesController nodesController = new NodesController(
				propertiesHelper, null, timeout);

			serverAlive = nodesController.isServerAlive(host, port);

			nodesController.shutdown();
		} catch (Throwable t) {
			Utils.exitWithFailure(
				"Throwable caught during the check if the server alive", t);
		}

		return serverAlive;
	}

	public void addNodeEventHandler(NodeEventHandler handler) {
		customEventHandlersList.add(handler);
	}

	public void execute(String[] args, boolean debugEnabled, boolean quiet) {
		try {
			parseCommandLineArguments(args);

			if (serverHostOption != null) {
				boolean serverAlive = MonitorTool.isServerAlive(
					serverHostOption, serverPortOption, timeout);

				if (serverAlive) {
					System.out.println("alive");
					Utils.exitWithSuccess();
				}

				System.out.println("down");
				Utils.exitWithFailure();
			} else if (allOption) {
				init();
				start();
			} else {
				Utils
					.exitWithFailure("Cannot determine the mode of application");
			}
		} catch (Throwable t) {
			Utils.exitWithFailure("Unexpected throwable", t);
		}
	}

	protected void parseCommandLineArguments(String[] commandLineArguments) {
		Options options = constructGnuOptions();

		if (commandLineArguments.length < 1) {
			printHelp(options);
		}

		try {
			CommandLineParser parser = new GnuParser();
			CommandLine line = parser.parse(options, commandLineArguments);

			if (line.hasOption(HELP_OPTION)) {
				printHelp(options);
			}

			if (line.hasOption(TIMEOUT_OPTION)) {
				timeout = Long.parseLong(line.getOptionValue(TIMEOUT_OPTION));
			}

			if (line.hasOption(PERIOD_OPTION)) {
				period = Long.parseLong(line.getOptionValue(PERIOD_OPTION));
			}

			boolean allOptionTemp = line.hasOption(ALL_OPTION);
			String serverOptionTemp = line.getOptionValue(SERVER_OPTION);

			if (serverOptionTemp != null) {
				int indexOfPortStart = serverOptionTemp.indexOf('[');
				int indexOfPortEnd = serverOptionTemp.indexOf(']');
				serverHostOption = serverOptionTemp.substring(0,
					indexOfPortStart);
				String portString = serverOptionTemp.substring(
					indexOfPortStart + 1, indexOfPortEnd);
				serverPortOption = Integer.parseInt(portString);
			} else if (allOptionTemp) {
				allOption = allOptionTemp;

				if (line.hasOption(LOCATORS_OPTION)) {
					locators = line.getOptionValue(LOCATORS_OPTION);
				} else {
					Utils.exitWithFailure("The option --" + LOCATORS_OPTION
						+ " should be used when the option --" + ALL_OPTION
						+ " specified");
				}
			} else {
				Utils.exitWithFailure("The option --" + SERVER_OPTION
					+ " or --" + ALL_OPTION + " should be specified");
			}

		} catch (Throwable t) {
			Utils
				.exitWithFailure(
					"Throwable caught during the command-line arguments parsing",
					t);
		}
	}

	protected void printHelp(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("monitor <--" + HELP_OPTION + " | --"
			+ SERVER_OPTION + " [--" + TIMEOUT_OPTION + "] | --" + ALL_OPTION
			+ " <--" + LOCATORS_OPTION + "> [--" + PERIOD_OPTION + "] [--"
			+ TIMEOUT_OPTION + "] >", options);

		Utils.exitWithFailure();
	}

	protected Options constructGnuOptions() {
		final Options gnuOptions = new Options();

		gnuOptions
			.addOption(
				"s",
				SERVER_OPTION,
				true,
				"Check one server and exit with status 0 if server alive, or with status 1 if server is dead or down. Server should be in format host[port].")
			.addOption(
				"a",
				ALL_OPTION,
				false,
				"Periodically check all the servers related to locators specified in monitoring.properties file")
			.addOption("l", LOCATORS_OPTION, true,
				"List of locators in format host1[port1],host2[port2]")
			.addOption("p", PERIOD_OPTION, true,
				"Period between runs, ms. Default value is " + DEFAULT_PERIOD)
			.addOption("t", TIMEOUT_OPTION, true,
				"Timeout, ms. Default value is " + DEFAULT_TIMEOUT)
			.addOption("h", HELP_OPTION, false, "Print usage information");

		return gnuOptions;

	}

}
