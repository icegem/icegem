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
package com.googlecode.icegem.cacheutils.signallistener;

import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.googlecode.icegem.cacheutils.Tool;
import com.googlecode.icegem.cacheutils.common.Utils;

/**
 * User: Artem Kondratyev, e-mail: kondratevae@gmail.com
 */
public class WaitforTool extends Tool {

	private static final int DEFAULT_CHECK_INTERVAL = 1000;
	private static final int DEFAULT_TIMEOUT = 60000;

	private String locators;
	private String regionNameToListen;
	private int timeout;
	private int checkInterval;
	private String keyToListen;

	/**
	 * @param regionToListen
	 *            region that will contain key.
	 * @param key
	 *            key for listening.
	 * @param timeout
	 *            how long util waits for key's appearance.
	 * @param checkInterval
	 *            how often util checks region.
	 * @return true if key was found, false otherwise.
	 * */
	public static boolean waitSignal(Region regionToListen, Object key,
		long timeout, long checkInterval) throws InterruptedException {
		if (regionToListen == null)
			throw new NullPointerException("region is null");
		if (checkInterval > timeout)
			throw new IllegalArgumentException("check interval ("
				+ checkInterval + ") is longer then timeout(" + timeout + ")");

		long fromTime = System.currentTimeMillis();
		// todo: diff time (sec, millisec, etc)
		while ((System.currentTimeMillis() - fromTime) <= timeout) {
			if (regionToListen.containsKeyOnServer(key)) {
				return true;
			}
			TimeUnit.MILLISECONDS.sleep(checkInterval);
		}
		return false;
	}

	protected Options constructGnuOptions() {
		Options options = new Options();
		options.addOption("region", true,
			"region where key appearance is checking");
		options.addOption("locators", true, "available locators");
		options.addOption("key", true, "key for checking");
		options.addOption("timeout", true, "check duration");
		options.addOption("checkInterval", true, "key's check interval");
		return options;
	}

	@Override
	protected void parseCommandLineArguments(String[] commandLineArguments) {
		CommandLineParser cmdParser = new GnuParser();
		Options options = constructGnuOptions();

		CommandLine cmd = null;
		try {
			cmd = cmdParser.parse(options, commandLineArguments);
		} catch (ParseException e) {
			throw new RuntimeException("error parsing cmd args", e);
		}

		if (!cmd.hasOption("region") || !cmd.hasOption("locators")
			|| !cmd.hasOption("key")) {
			printHelp(options);
            Utils.exitWithFailure();
		}

		locators = cmd.getOptionValue("locators");

		regionNameToListen = cmd.getOptionValue("region");

		timeout = DEFAULT_TIMEOUT;
		if (cmd.hasOption("timeout"))
			timeout = Integer.parseInt(cmd.getOptionValue("timeout"));
		else
			System.out.println("using default value for timeout: " + timeout);

		checkInterval = DEFAULT_CHECK_INTERVAL;
		if (cmd.hasOption("checkInterval"))
			checkInterval = Integer.parseInt(cmd
				.getOptionValue("checkInterval"));
		else
			System.out.println("using default value for check interval "
				+ checkInterval);

		keyToListen = cmd.getOptionValue("key");
	}

	protected void printHelp(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("waitfor [options]", options);
	}

	public void execute(String[] args, boolean debugEnabled, boolean quiet) {
		parseCommandLineArguments(args);
		
		ClientCacheFactory clientCacheFactory = new ClientCacheFactory();
		for (String locator : locators.split(",")) {
			String host = locator.trim().substring(0, locator.indexOf("["));
			int port = Integer.parseInt(locator.substring(
				locator.indexOf("[") + 1, locator.indexOf("]")));
			clientCacheFactory.addPoolLocator(host, port);
		}

		ClientCache client;
		client = clientCacheFactory.create();

		Region signalRegion = client.createClientRegionFactory(
			ClientRegionShortcut.PROXY).create(regionNameToListen);
		// example
		int result = 0;
		try {
			result = waitSignal(signalRegion, keyToListen, timeout,
				checkInterval) ? 0 : 1;
		} catch (InterruptedException e) {
			throw new RuntimeException("error waiting key", e);
		}
		System.out.println("status is " + result);
		client.close();
		
		System.exit(result);
	}
}
