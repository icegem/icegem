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
package itest.com.googlecode.icegem;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.googlecode.icegem.utils.CacheUtils;
import com.googlecode.icegem.utils.JavaProcessLauncher;
import com.googlecode.icegem.utils.PropertiesHelper;
import com.googlecode.icegem.utils.ServerTemplate;

/**
 * TODO: It's not finished yet. The approach just to keep static methods looks
 * a bit ugly. There is an idea to extend {@link TestCase} class and make it more
 * elegant to use by child classes. We should also think is this a right package
 * for this class.
 * 
 * Abstract integration test. Contains the most frequently used utility methods
 * to perform integration tests (starting servers, clients etc.)   
 * 
 * @author Renat Akhmerov.
 */
public abstract class AbstractIntegrationTest {
    /** Default locator port. */
    private static final int DEFAULT_LOCATOR_PORT = 10355;

    /** Java process launcher. */
    private static final JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher();

    /** Cache servers. */
    private static Process[] cacheServers;

    /** Client cache. */
    private static ClientCache clientCache;

    /** Logger. */
    private final Logger log;

    /**
     * Default constructor.
     */
    protected AbstractIntegrationTest() {
	log = LoggerFactory.getLogger(getClass());
    }

    /**
     * Start required number of cache servers and client.
     * 
     * @param serverCnt Number of cache servers to start.
     * @param propFile GemFire property file.
     * @throws Exception If failed to start either cache servers or client.
     */
    protected static void startCacheServersAndClient(int serverCnt, String propFile) throws Exception {
	startCacheServers(serverCnt, propFile);

	startClient(propFile);
    }

    /**
     * Stops previously started cache servers and client.
     * 
     * @throws Exception If failed to start either cache servers or client.
     */
    protected static void stopCacheServersAndClient() throws Exception {
	stopClient();

	stopCacheServers();
    }
    
    /**
     * Starts cache servers.
     * 
     * @param count Number of servers to start.
     * @param propFile GemFire properties file name.
     * @throws Exception If failed to start required number of servers.
     */
    protected static void startCacheServers(int count, String propFile) throws Exception {
	if (propFile == null)
	    throw new IllegalArgumentException("Parameter 'propFile' should not be null.");

	if (cacheServers != null)
	    throw new IllegalStateException("Cache servers have already been started.");

	String[] javaArgs = new String[] { "-DgemfirePropertyFile=" + propFile };

	cacheServers = new Process[count];

	for (int i = 0; i < cacheServers.length; i++) {
	    System.out.println("Starting cache server number: " + i);

	    cacheServers[i] = javaProcessLauncher.runWithConfirmation("", ServerTemplate.class, javaArgs, null);
	}
    }

    /**
     * Stops cache servers.
     * 
     * @throws Exception If failed to stop at least one of the previously started server.
     */
    protected static void stopCacheServers() throws Exception {
	ensureCacheServers();

	int failures = 0;

	for (Process server : cacheServers) {
	    try {
		javaProcessLauncher.stopBySendingNewLineIntoProcess(server);
	    } catch (Exception e) {
		failures++;

		System.err.println("Failed to stop cache server process: " + server);

		e.printStackTrace(System.err);
	    }
	}

	if (failures > 0)
	    throw new Exception("Failed to stop " + failures + " of " + cacheServers.length + " servers.");

	cacheServers = null;

	System.out.println("Servers have been stopped successfully.");
    }

    /**
     * Starts a client.
     * 
     * @param propFile GemFire properties file name.
     * @throws Exception If failed to start client.
     */
    protected static ClientCache startClient(String propFile) throws Exception {
	return startClient(propFile, DEFAULT_LOCATOR_PORT);
    }

    /**
     * Starts a client.
     * 
     * @param propFile GemFire properties file name.
     * @param locatorPort Locator port.
     * @throws Exception If failed to start client.
     */
    protected static ClientCache startClient(String propFile, int locatorPort) throws Exception {
	if (propFile == null)
	    throw new IllegalArgumentException("Parameter 'propFile' should not be null.");

	if (clientCache != null)
	    throw new IllegalStateException("Client cache has already been started.");

	ClientCacheFactory clientCacheFactory = new ClientCacheFactory().addPoolLocator("localhost", locatorPort);

	PropertiesHelper properties = new PropertiesHelper("/" + propFile);

	clientCache = clientCacheFactory.set("log-level", properties.getStringProperty("log-level"))
		.set("license-file", properties.getStringProperty("license-file"))
		.set("license-type", properties.getStringProperty("license-type")).create();

	System.out.println("Client has been started successfully.");

	return clientCache;
    }

    /**
     * Creates client region.
     * 
     * @param regionName Region name.
     * @param shortcut Shortcut.
     * @return Client region.
     */
    protected static <K, V> Region<K, V> createClientRegion(String regionName, ClientRegionShortcut shortcut) {
	if (regionName == null)
	    throw new IllegalArgumentException("regionName must not be empty.");

	if (shortcut == null)
	    throw new IllegalArgumentException("shortcut must not be empty.");

	ensureClient();

	if (clientCache == null)
	    throw new IllegalStateException("Failed to create client region. Start client first.");

	System.out.println("Creating client region [name=" + regionName + ", shortcut=" + shortcut + "]");

	return clientCache.<K, V> createClientRegionFactory(shortcut).create(regionName);
    }

    /**
     * Clears all regions created on client side.
     */
    protected static void clearClientRegions() {
	ensureClient();

	System.out.println("Clearing all regions created on client side...");

	for (Region<?, ?> region : clientCache.rootRegions())
	    CacheUtils.clearRegion(region);
    }

    /**
     * Stops client.
     */
    protected static void stopClient() {
	ensureClient();

	clientCache.close();

	clientCache = null;

	System.out.println("Client has been stopped successfully.");
    }

    /**
     * Throws exception if cache servers have not been started.
     */
    private static void ensureCacheServers() {
	if (cacheServers == null)
	    throw new IllegalStateException("Cache servers have not been started.");
    }

    /**
     * Throws exception if client cache has not been started.
     */
    private static void ensureClient() {
	if (clientCache == null)
	    throw new IllegalStateException("Client cache has not been started.");
    }

    /**
     * @return client cache.
     */
    protected static ClientCache getClientCache() {
	return clientCache;
    }

    /**
     * Logs info message.
     * 
     * @param msg Message to log.
     */
    protected void info(String msg) {
	log.info(msg);
    }

    /**
     * Logs error message.
     * 
     * @param msg Message to log.
     */
    protected void error(String msg) {
	log.error(msg);
    }

    /**
     * Logs error message.
     * 
     * @param msg Message to log.
     * @param e Throwable according to the log entry.
     */
    protected void error(String msg, Throwable e) {
	log.error(msg, e);
    }
}
