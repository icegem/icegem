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
package com.googlecode.icegem.cacheutils.replication;

import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import com.googlecode.icegem.cacheutils.Tool;
import com.googlecode.icegem.cacheutils.common.Utils;

/**
 * The main class of the replication measurement tool. It parses command-line
 * options, configures and creates the guest nodes, and collects their
 * responses.
 * 
 * Responses with exit code 0 in case of all the relations established, 1
 * otherwise.
 */
public class CheckReplicationTool extends Tool {

	/* Name of timeout option */
	private static final String TIMEOUT_OPTION = "timeout";

	/* Name of cluster option */
	private static final String CLUSTER_OPTION = "cluster";

	/* Name of region option */
	private static final String REGION_OPTION = "region";

	/* Name of help option */
	private static final String HELP_OPTION = "help";

	/* Default timeout is 1 minute */
	private static final long DEFAULT_TIMEOUT = 60 * 1000;

	/* Additional timeout */
	private static final long DELTA_TIMEOUT = 10 * 1000;

	/* Default region name is "proxy" */
	private static final String DEFAULT_REGION_NAME = "proxy";

	/* Waiting timeout */
	private static long timeout = DEFAULT_TIMEOUT;

	/* Technical region name */
	private static String regionName = DEFAULT_REGION_NAME;

	/* Clusters properties */
	private Properties clustersProperties;

	/* Debug enabled flag */
	private boolean debugEnabled;

	/**
	 * Contains processing operation
	 */
	private class ProcessorTask implements Runnable {

		private int exitCode;
		private Properties clustersProperties;
		private long timeout;
		private String regionName;
		private boolean debugEnabled;
		private boolean quiet;

		public ProcessorTask(Properties clustersProperties, long timeout,
			String regionName, boolean debugEnabled, boolean quiet) {
			this.clustersProperties = clustersProperties;
			this.timeout = timeout;
			this.regionName = regionName;
			this.debugEnabled = debugEnabled;
			this.quiet = quiet;
		}

		public void run() {
			ReplicationProcessor processor = new ReplicationProcessor(
				clustersProperties, timeout, regionName, debugEnabled, quiet);

			exitCode = 1;
			try {
				exitCode = processor.process();
			} catch (Throwable t) {
				debug(
					"CheckReplicationTool.ProcessorTask#run(): Throwable caught with message = "
						+ t.getMessage(), t);
			}

		}

		public int getExitCode() {
			return exitCode;
		}
	}

	/**
	 * Runs the tool. All the tools run in this way.
	 */
	public void execute(String[] args, boolean debugEnabled, boolean quiet) {
		try {
			this.debugEnabled = debugEnabled;

			debug("CheckReplicationTool#execute(String[]): args = "
				+ Arrays.asList(args));

			parseCommandLineArguments(args);

			System.out.println("Connecting...");

			debug("CheckReplicationTool#execute(String[]): Creating CheckReplicationTool.ProcessorTask with parameters: clustersProperties = "
				+ clustersProperties
				+ ", timeout = "
				+ timeout
				+ ", regionName = " + regionName);

			ProcessorTask task = new ProcessorTask(clustersProperties, timeout,
				regionName, debugEnabled, quiet);

			debug("CheckReplicationTool#execute(String[]): Starting CheckReplicationTool.ProcessorTask");

			Utils.execute(task, timeout + DELTA_TIMEOUT);

			int exitCode = task.getExitCode();

			debug("CheckReplicationTool#execute(String[]): CheckReplicationTool.ProcessorTask finished with exitCode = "
				+ exitCode);

			if (exitCode == 0) {
				Utils.exitWithSuccess();
			}

			Utils.exitWithFailure();
		} catch (Throwable t) {
			debug(
				"CheckReplicationTool#execute(String[]): Throwable caught with message = "
					+ t.getMessage(), t);

			Utils.exitWithFailure("Unexpected throwable", t);
		}
	}

	/**
	 * Parses command-line arguments and sets the local variables
	 * 
	 * @param commandLineArguments
	 *            - the list of command-line arguments
	 */
	protected void parseCommandLineArguments(String[] commandLineArguments) {
		Options options = constructGnuOptions();

		if (commandLineArguments.length < 1) {
			printHelp(options);
		}

		CommandLineParser parser = new GnuParser();
		try {
			CommandLine line = parser.parse(options, commandLineArguments);

			if (line.hasOption(HELP_OPTION)) {
				printHelp(options);
			}

			if (line.hasOption(REGION_OPTION)) {
				regionName = line.getOptionValue(REGION_OPTION);
			}

			if (line.hasOption(TIMEOUT_OPTION)) {
				String timeoutString = line.getOptionValue(TIMEOUT_OPTION);
				timeout = Long.parseLong(timeoutString);
			}

			if (line.hasOption(CLUSTER_OPTION)) {
				clustersProperties = line.getOptionProperties(CLUSTER_OPTION);

				if (clustersProperties.keySet().size() < 2) {
					Utils
						.exitWithFailure("At least two clusters should be defined");
				}
			} else {
				Utils.exitWithFailure("No clusters defined");
			}

		} catch (Throwable t) {
			Utils
				.exitWithFailure(
					"Throwable caught during the command-line arguments parsing",
					t);
		}
	}

	/**
	 * Prints help if requested, or in case of any misconfiguration
	 * 
	 * @param options
	 *            - the GNU options
	 */
	protected void printHelp(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("check-replication [options]", options);

		Utils.exitWithFailure();
	}

	/**
	 * Constructs the set of GNU options
	 * 
	 * @return - the constructed options
	 */
	protected Options constructGnuOptions() {
		final Options gnuOptions = new Options();

		gnuOptions
			.addOption("t", TIMEOUT_OPTION, true,
				"Timeout, ms. Default timeout is " + DEFAULT_TIMEOUT)
			.addOption(
				"r",
				REGION_OPTION,
				true,
				"The name of region for this test. Default name is \""
					+ DEFAULT_REGION_NAME + "\"")
			.addOption("h", HELP_OPTION, false, "Print usage information");

		@SuppressWarnings("static-access")
		Option locatorsOption = OptionBuilder
			.hasArgs()
			.withDescription(
				"Cluster name and list of its locators. "
					+ "There should be at least two clusters. "
					+ "Example: -c cluster1=host1[port1],host2[port2] -c cluster2=host3[port3]")
			.withValueSeparator().withArgName("cluster=locators")
			.withLongOpt(CLUSTER_OPTION).create("c");

		gnuOptions.addOption(locatorsOption);

		return gnuOptions;
	}

	/**
	 * Prints debug information if the debug is enabled
	 * 
	 * @param message
	 *            - the debug message
	 */
	private void debug(String message) {
		debug(message, null);
	}

	/**
	 * Prints debug information if the debug is enabled
	 * 
	 * @param message
	 *            - the debug message
	 * @param t
	 *            - the instance of Throwable
	 */
	private void debug(String message, Throwable t) {
		if (debugEnabled) {
			System.err.println("0 [CheckReplicationTool] " + message);

			if (t != null) {
				t.printStackTrace(System.err);
			}
		}
	}
}
