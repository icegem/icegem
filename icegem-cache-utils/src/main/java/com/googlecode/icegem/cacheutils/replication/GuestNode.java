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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.googlecode.icegem.cacheutils.common.Utils;
import com.googlecode.icegem.utils.PropertiesHelper;

/**
 * The client cache which is used for replication test. Connects to the cluster
 * represented by a locator, puts name of the locator as a key and current time
 * as a value to the technical region and waits events from other GuestNodes.
 * After that calculates time of replication ant prints it to the system output.
 * 
 * Returns 0 as exit code in case of all the expected events received, 1
 * otherwise.
 * 
 */
public class GuestNode {
	/* Prefix of the gemfire system properties */
	private static final String GEMFIRE_PREFIX = "gemfire.";

	/* Polling period, ms */
	private static final int CHECK_PERIOD = 50;

	/* Prefix of the check replication tool keys */
	private static final String KEY_PREFIX = "check-replication-";

	/* Postfix of the check replication tool keys used for the startedAt entries */
	private static final String KEY_POSTFIX_STARTED_AT = "-startedAt";
	/* Postfix of the check replication tool keys used for the sentAt entries */
	private static final String KEY_POSTFIX_SENT_AT = "-sentAt";
	/*
	 * Postfix of the check replication tool keys used for the receivedAt
	 * entries
	 */
	private static final String KEY_POSTFIX_DURATION = "-receivedAt";

	/* Local cluster name */
	private String localClusterName;

	/* Cache instance */
	private ClientCache clientCache;

	/* Technical region instance */
	private Region<String, Long> region;

	/* The name of technical region */
	private String regionName;

	/* Clusters' properties */
	private Properties clustersProperties;

	/* Debug enabled flag */
	private boolean debugEnabled;

	/* Quiet flag */
	private boolean quiet;

	/* The time at which the processing has started */
	private long processingStartedAt;

	/**
	 * Creates the instance of guest node
	 * 
	 * @param cluster
	 *            - the name of local cluster
	 * @param clustersProperties
	 *            - the clusters' properties
	 * @param regionName
	 *            - the name of the technical region
	 * @param debugEnabled
	 *            - the debug enabled flag
	 * @param quiet
	 *            - the quiet flag
	 * @param processingStartedAt
	 *            - the time at which the processing has started
	 */
	private GuestNode(String cluster, Properties clustersProperties,
		String regionName, boolean debugEnabled, boolean quiet,
		long processingStartedAt) {

		debug("GuestNode#GuestNode(String, Properties, String, String, String): Creating instance with parameters: cluster = "
			+ cluster
			+ ", clustersProperties = "
			+ clustersProperties
			+ ", regionName = " + regionName);

		this.localClusterName = cluster;
		this.clustersProperties = clustersProperties;
		this.regionName = regionName;
		this.debugEnabled = debugEnabled;
		this.quiet = quiet;
		this.processingStartedAt = processingStartedAt;

		debug("GuestNode#GuestNode(String, Properties, String, String, String): Creating RelationsController");

		init();
	}

	/**
	 * Creates the startedAt key
	 * 
	 * @param clusterName
	 *            - the name of cluster
	 * @return - the startedAt key
	 */
	private String createStartedAtKey(String clusterName) {
		return KEY_PREFIX + clusterName + KEY_POSTFIX_STARTED_AT;
	}

	/**
	 * Creates the sentAt key
	 * 
	 * @param clusterName
	 *            - the name of cluster
	 * @return - the sentAt key
	 */
	private String createSentAtKey(String clusterName) {
		return KEY_PREFIX + clusterName + KEY_POSTFIX_SENT_AT;
	}

	/**
	 * Creates the receivedAt key
	 * 
	 * @param fromClusterName
	 *            - the name of cluster from which the entry received
	 * @param toClusterName
	 *            - the name of cluster on which the entry received
	 * @return - the receivedAt key
	 */
	private String createReceivedAtKey(String fromClusterName,
		String toClusterName) {

		return KEY_PREFIX + fromClusterName + "-" + toClusterName
			+ KEY_POSTFIX_DURATION;
	}

	/**
	 * Initializes the technical region
	 */
	private void init() {
		try {
			debug("GuestNode#init(): Creating Cache");

			ClientCacheFactory clientCacheFactory = new ClientCacheFactory();

			Properties gemfireProperties = PropertiesHelper.filterProperties(
				System.getProperties(), GEMFIRE_PREFIX);

			for (Object keyObject : gemfireProperties.keySet()) {
				String key = (String) keyObject;
				String value = gemfireProperties.getProperty(key);

				String name = key.substring(GEMFIRE_PREFIX.length());

				debug("GuestNode#init(): Configuring ClientCacheFactory with key = "
					+ name + ", value = " + value);

				clientCacheFactory.set(name, value);
			}

			clientCacheFactory.setPoolSubscriptionEnabled(true);

			String locators = clustersProperties.getProperty(localClusterName);
			String[] locatorsArray = locators.split(",");
			for (String locator : locatorsArray) {
				String locatorHost = locator.substring(0, locator.indexOf("["));

				String locatorPortString = locator.substring(
					locator.indexOf("[") + 1, locator.indexOf("]"));
				int locatorPort = Integer.parseInt(locatorPortString);

				debug("GuestNode#init(): Adding locator to pool: locatorHost = "
					+ locatorHost + ", locatorPort = " + locatorPort);

				clientCacheFactory.addPoolLocator(locatorHost, locatorPort);
			}

			clientCache = clientCacheFactory.create();

			ClientRegionFactory<String, Long> clientRegionFactory = clientCache
				.createClientRegionFactory(ClientRegionShortcut.PROXY);

			region = clientCache.getRegion(regionName);

			debug("GuestNode#init(): Get region with name = " + regionName
				+ ": region = " + region);

			if (region == null) {
				region = clientRegionFactory.create(regionName);
			}
			debug("GuestNode#init(): Create region with name = " + regionName
				+ ": region = " + region);

		} catch (Throwable t) {
			debug(
				"GuestNode#init(): Throwable caught with message = "
					+ t.getMessage(), t);

		}
	}

	/**
	 * Wait until the other clients started
	 */
	private void waitForStarted() {
		debug("GuestNode#waitForStarted(): Waiting for other clusters started");

		while (true) {
			boolean othersStarted = true;

			for (Object key : clustersProperties.keySet()) {
				String clusterName = (String) key;

				Long startedAt = region.get(createStartedAtKey(clusterName));

				debug("GuestNode#waitForStarted(): Checking startedAt: startedAt = "
					+ startedAt
					+ ", processingStartedAt = "
					+ processingStartedAt);

				if ((startedAt == null)
					|| (startedAt.longValue() < processingStartedAt)) {

					othersStarted = false;
					break;

				}
			}

			if (othersStarted) {
				break;
			}

			try {
				TimeUnit.MILLISECONDS.sleep(CHECK_PERIOD);
			} catch (InterruptedException e) {
			}
		}

		debug("GuestNode#waitForStarted(): Other clusters started");
	}

	/**
	 * Wait until received all the entries from other clients. Put the
	 * receivedAt entries to cache.
	 */
	private void waitForSent() {
		debug("GuestNode#waitForSent(): Waiting for other clusters sent");

		while (true) {
			boolean othersSent = true;

			Map<String, Long> clusterNameToReceivedAtMap = new HashMap<String, Long>();
			for (Object key : clustersProperties.keySet()) {
				String clusterName = (String) key;

				if (localClusterName.equals(clusterName)) {
					continue;
				}

				Long sentAt = region.get(createSentAtKey(clusterName));
				long receivedAt = System.currentTimeMillis();

				if ((sentAt != null)
					&& (sentAt.longValue() > processingStartedAt)) {

					clusterNameToReceivedAtMap.put(clusterName, receivedAt);
				}
			}

			for (Object key : clustersProperties.keySet()) {
				String clusterName = (String) key;

				if (localClusterName.equals(clusterName)) {
					continue;
				}

				Long receivedAt = clusterNameToReceivedAtMap.get(clusterName);

				if (receivedAt == null) {

					if (othersSent) {
						othersSent = false;
					}

				} else {
					region.put(
						createReceivedAtKey(clusterName, localClusterName),
						receivedAt);
				}
			}

			if (othersSent) {
				break;
			}

			try {
				TimeUnit.MILLISECONDS.sleep(CHECK_PERIOD);
			} catch (InterruptedException e) {
			}
		}

		debug("GuestNode#waitForSent(): Other clusters sent");
	}

	/**
	 * Wait until all the clients received all the receivedAt entries.
	 */
	private void waitForConnected() {
		debug("GuestNode#waitForConnected(): Waiting for all the clusters connected");

		while (true) {
			boolean connected = true;

			for (Object fromKey : clustersProperties.keySet()) {
				String fromClusterName = (String) fromKey;

				for (Object toKey : clustersProperties.keySet()) {
					String toClusterName = (String) toKey;

					if (fromClusterName.equals(toClusterName)) {
						continue;
					}

					Long receivedAt = region.get(createReceivedAtKey(
						fromClusterName, toClusterName));

					if (receivedAt == null) {
						connected = false;
						break;
					}
				}
			}

			if (connected) {
				break;
			}

			try {
				TimeUnit.MILLISECONDS.sleep(CHECK_PERIOD);
			} catch (InterruptedException e) {
			}
		}

		debug("GuestNode#waitForConnected(): All the clusters connected");
	}

	/**
	 * The processing task. Contains the main algorithm of the GuestNode:
	 * <ul>
	 * <li>Put startedAt entry with a special startedAt key and date of the
	 * start as the value</li>
	 * <li>Wait until in the local region will be all the startedAt entries from
	 * other clients</li>
	 * <li>Put sentAt entry with a special sentAt key and date of the start as
	 * the value</li>
	 * <li>Wait until in the local region will be all the sentAt entries from
	 * other clients. When such entry received, put new special entry receivedAt
	 * into the region</li>
	 * <li>Wait until in the local region will be all the receivedAt entries
	 * from other clients</li>
	 * </ul>
	 * 
	 * See the special keys formats in the appropriate methods.
	 */
	private class ProcessingTask implements Runnable {
		private boolean connected = false;

		public void run() {
			try {
				region.put(createStartedAtKey(localClusterName),
					System.currentTimeMillis());

				waitForStarted();

				region.put(createSentAtKey(localClusterName),
					System.currentTimeMillis());

				waitForSent();

				waitForConnected();

				connected = true;
			} catch (Throwable t) {
				connected = false;
			}
		}

		public boolean isConnected() {
			return connected;
		}
	}

	/**
	 * Waits for processing finished.
	 * 
	 * @param timeout
	 *            - the timeout in milliseconds
	 * @return - true if the connection process finished successfully, false
	 *         otherwise
	 */
	public boolean waitFor(long timeout) {
		debug("GuestNode#waitFor(long): Waiting for task finish with timeout = "
			+ timeout);

		ProcessingTask connectionCheckTask = new ProcessingTask();

		Utils.execute(connectionCheckTask, timeout);

		boolean connected = connectionCheckTask.isConnected();

		debug("GuestNode#waitFor(long): Task finished connected = " + connected);

		return connected;
	}

	/**
	 * Finalizes work with the guest node
	 */
	public void close() {
		try {
			debug("GuestNode#close(): Closing the cache");

			clientCache.close();

			debug("GuestNode#close(): Cache closed = " + clientCache.isClosed());
		} catch (Throwable t) {
			debug(
				"GuestNode#close(): Throwable caught with message = "
					+ t.getMessage(), t);
		}
	}

	/**
	 * Prints the current state of connections
	 * 
	 * @param connected
	 */
	public void printState(boolean connected) {
		if (!quiet) {

			StringBuilder sb = new StringBuilder();

			if (connected) {

				sb.append(localClusterName).append(" <= ");

				Iterator<Object> it = clustersProperties.keySet().iterator();
				while (it.hasNext()) {
					String clusterName = (String) it.next();

					if (localClusterName.equals(clusterName)) {
						continue;
					}

					Long sentAt = region.get(createSentAtKey(clusterName));
					Long receivedAt = region.get(createReceivedAtKey(
						clusterName, localClusterName));

					long duration = receivedAt - sentAt;

					sb.append("[").append(clusterName).append(", ")
						.append(duration).append("ms]");
				}

			} else {

				sb.append("Connection process is not finished for ").append(
					localClusterName);

			}

			System.out.println(sb.toString());

		}
	}

	/**
	 * Configures and starts the guest node
	 * 
	 * @param args
	 *            - the configuration arguments
	 */
	public static void main(String[] args) {
		try {
			if (args.length != 7) {
				Utils.exitWithFailure();
			}

			String cluster = args[0];
			Properties clustersProperties = PropertiesHelper
				.stringToProperties(args[1]);
			long timeout = Long.parseLong(args[2]);
			String regionName = args[3];
			boolean debugEnabled = ("true".equals(args[4]) ? true : false);
			boolean quiet = ("true".equals(args[5]) ? true : false);
			long processingStartedAt = Long.parseLong(args[6]);

			GuestNode guestNode = new GuestNode(cluster, clustersProperties,
				regionName, debugEnabled, quiet, processingStartedAt);

			boolean connected = guestNode.waitFor(timeout);

			guestNode.printState(connected);

			guestNode.close();

			if (connected) {
				Utils.exitWithSuccess();
			}

			Utils.exitWithFailure();
		} catch (Throwable t) {
			Utils.exitWithFailure();
		}
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
			long currentTime = System.currentTimeMillis();
			long timeSinceProcessingStart = currentTime - processingStartedAt;
			System.err.println(timeSinceProcessingStart + " ["
				+ localClusterName + "] " + message);

			if (t != null) {
				t.printStackTrace(System.err);
			}
		}
	}
}
