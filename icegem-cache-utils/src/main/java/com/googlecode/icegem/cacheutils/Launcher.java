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
package com.googlecode.icegem.cacheutils;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.googlecode.icegem.cacheutils.common.Utils;
import com.googlecode.icegem.cacheutils.comparator.CompareTool;
import com.googlecode.icegem.cacheutils.monitor.MonitorTool;
import com.googlecode.icegem.cacheutils.replication.CheckReplicationTool;
import com.googlecode.icegem.cacheutils.signallistener.WaitforTool;
import com.googlecode.icegem.cacheutils.updater.UpdateTool;

/**
 * Aggregates cache utilities and handles the common options.
 */
public class Launcher {
    /** */
    private static final String DEBUG_OPTION = "debug";

    /** */
    private static final String QUIET_OPTION = "quiet";

    /** */
    private static final String HELP_OPTION = "help";

    /** */
    private static final boolean DEFAULT_DEBUG_ENABLED = false;

    /** */
    private static final boolean DEFAULT_QUIET = false;

    /** */
    private static boolean debugEnabled = DEFAULT_DEBUG_ENABLED;

    /** */
    private static boolean quiet = DEFAULT_QUIET;

    /**
     * Represents the utility.
     */
    private enum Command {
	/** */
	COMPARE("compare", new CompareTool()),

	/** */
	MONITOR("monitor", new MonitorTool()),

	/** */
	CHECK_REPLICATION("check-replication", new CheckReplicationTool()),

	/** */
	UPDATE("update", new UpdateTool()),

	/** */
	WAITFOR("waitfor", new WaitforTool());

	/** Command name. */
	private String name;

	/** Command executable. */
	private Executable exec;

	/**
	 * @param name Command name.
	 * @param exec Command executable.
	 */
	private Command(String name, Executable exec) {
	    this.name = name;
	    this.exec = exec;
	}

	/**
	 * @return Command name.
	 */
	public String getName() {
	    return name;
	}

	/**
	 * @return Command executable.
	 */
	public Executable getExec() {
	    return exec;
	}

	/**
	 * Gets tool by name.
	 * 
	 * @param commandName
	 *            - The name of the command.
	 * @return - found tool or null if the utility is not found.
	 */
	public static Executable getUtil(String commandName) {
	    Executable result = null;

	    for (Command command : Command.values()) {
		if (command.getName().equals(commandName.trim())) {
		    result = command.getExec();
		    
		    break;
		}
	    }

	    return result;
	}
    }

    /**
     * Prints help and exits.
     */
    private static void printHelp() {
	Options options = constructGnuOptions();
	
	printHelp(options);
    }

    /**
     * Parses command line arguments.
     * 
     * @param commandLineArguments
     *            - The arguments.
     */
    private static void parseCommandLineArguments(String[] commandLineArguments) {
	Options options = constructGnuOptions();

	CommandLineParser parser = new GnuParser();
	
	try {
	    CommandLine line = parser.parse(options, commandLineArguments);

	    if (line.hasOption(HELP_OPTION)) {
		printHelp(options);
	    }

	    if (line.hasOption(DEBUG_OPTION)) {
		debugEnabled = true;
	    }

	    if (!debugEnabled && line.hasOption(QUIET_OPTION)) {
		quiet = true;
	    }
	} catch (Throwable t) {
	    printHelp(options);
	}
    }

    /**
     * Prints help if requested.
     * 
     * @param options
     *            - The GNU options.
     */
    private static void printHelp(final Options options) {
	StringBuilder sb = new StringBuilder();

	sb.append("java -jar icegem-cache-utils-<version>.jar [options] <");

	Command[] commands = Command.values();
	
	for (int i = 0; i < commands.length; i++) {
	    sb.append(commands[i].getName());
	    
	    if (i < (commands.length - 1)) {
		sb.append(" | ");
	    }
	}

	sb.append("> [command_specific_options]");

	HelpFormatter formatter = new HelpFormatter();
	
	formatter.printHelp(sb.toString(), options);

	Utils.exitWithFailure();
    }

    /**
     * Constructs the set of GNU options.
     * 
     * @return - The constructed options.
     */
    private static Options constructGnuOptions() {
	final Options gnuOptions = new Options();

	gnuOptions.addOption("d", DEBUG_OPTION, false, "Print debug information")
		.addOption("q", QUIET_OPTION, false, "Quiet output. Doesn't work if --" + DEBUG_OPTION + " specified.")
		.addOption("h", HELP_OPTION, false, "Print usage information");

	return gnuOptions;
    }

    /**
     * Looks for the index of command in the specified array.
     * 
     * @param args
     *            - Arguments.
     * @return - Index of command, or -1 if command is not found in the array.
     */
    private static int findCommandIndex(String[] args) {
	int commandIndex = -1;

	for (int i = 0; i < args.length; i++) {
	    for (Command command : Command.values()) {
		if (command.getName().equals(args[i].trim())) {
		    commandIndex = i;
		    
		    break;
		}
	    }
	}

	return commandIndex;
    }

    /**
     * Extracts the launcher arguments from the all arguments array.
     * 
     * @param args
     *            - All arguments.
     * @param commandIndex
     *            - Index of the command.
     * @return - Launcher arguments.
     */
    private static String[] extractLauncherArgs(String[] args, int commandIndex) {
	String[] launcherArgs = new String[commandIndex];

	System.arraycopy(args, 0, launcherArgs, 0, commandIndex);

	return launcherArgs;
    }

    /**
     * Extracts the command-specific arguments from the all arguments array.
     * 
     * @param args
     *            - All arguments.
     * @param commandIndex
     *            - Index of the command.
     * @return - Command-specific arguments.
     */
    private static String[] extractCommandArgs(String[] args, int commandIndex) {
	String[] commandArgs = new String[args.length - commandIndex - 1];

	System.arraycopy(args, commandIndex + 1, commandArgs, 0, args.length - commandIndex - 1);

	return commandArgs;
    }

    /**
     * The entry point of the application.
     * 
     * @param args
     *            - All arguments.
     * @throws Exception
     */
    public static void main(String[] args) {
	try {
	    int commandIndex = findCommandIndex(args);

	    if (commandIndex < 0) {
		printHelp();
	    }

	    String[] launcherArgs = extractLauncherArgs(args, commandIndex);
	    String[] commandArgs = extractCommandArgs(args, commandIndex);

	    parseCommandLineArguments(launcherArgs);

	    debug("Launcher#main(): args = " + Arrays.asList(args));
	    debug("Launcher#main(): launcherArgs = " + Arrays.asList(launcherArgs));
	    debug("Launcher#main(): commandArgs = " + Arrays.asList(commandArgs));

	    String commandName = args[commandIndex];

	    Executable tool = Command.getUtil(commandName);
	    
	    if (tool != null) {
		tool.execute(commandArgs, debugEnabled, quiet);
	    } else {
		debug("Launcher#main(): Command \"" + commandName + "\" not found");
		
		printHelp();
	    }
	} catch (Throwable t) {
	    debug("Launcher#main(): Throwable caught with message = " + t.getMessage(), t);
	    
	    Utils.exitWithFailure("Unexpected throwable", t);
	}
    }

    /**
     * Prints debug information if debug is enabled.
     * 
     * @param message
     *            - Debug message.
     */
    private static void debug(String message) {
	debug(message, null);
    }

    /**
     * Prints debug information if debug is enabled.
     * 
     * @param message
     *            - Debug message.
     * @param t
     *            - Instance of Throwable.
     */
    private static void debug(String message, Throwable t) {
	if (debugEnabled) {
	    System.err.println("0 [Launcher] " + message);

	    if (t != null) {
		t.printStackTrace(System.err);
	    }
	}
    }
}
