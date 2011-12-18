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
package itest.com.googlecode.icegem.cacheutils.comparator;

import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.googlecode.icegem.cacheutils.Launcher;
import com.googlecode.icegem.utils.JavaProcessLauncher;
import com.googlecode.icegem.utils.ServerTemplate;

public class CompareToolReplicateTest {
	private static final int NUMBER_OF_ENTRIES = 1000;
	private static final int NUMBER_OF_ENTRIES_PERFORMANCE = 100000;

	private static final String SERVER_40804 = "localhost[40804]";
	private static final String SERVER_40805 = "localhost[40805]";

	private static final String REGION_NAME = "data";

	/** Field cacheServer1 */
	private static Process cacheServer1;
	/** Field cacheServer2 */
	private static Process cacheServer2;
	/** Field javaProcessLauncher */
	/** Field cacheServer3 */
	private static Process cacheServer3;
	/** Field cacheServer4 */
	private static Process cacheServer4;
	/** Field javaProcessLauncher */
	private static JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher(
		true, true, false);

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException,
		TimeoutException {
		startCacheServers();
	}

	@Before
	public void beforeMethod() {
		fillData(SERVER_40804, REGION_NAME, NUMBER_OF_ENTRIES);
		fillData(SERVER_40805, REGION_NAME, NUMBER_OF_ENTRIES);
	}

	@AfterClass
	public static void tearDown() throws IOException, InterruptedException {
		stopCacheServers();
	}

	private void fillData(String address, String regionName, int numberOfEntries) {
		String host = address.substring(0, address.indexOf("["));
		int port = Integer.parseInt(address.substring(address.indexOf("[") + 1,
			address.indexOf("]")));

		ClientCache clientCache = new ClientCacheFactory()
			.addPoolServer(host, port).set("log-level", "none").create();

		ClientRegionFactory<Long, User> clientRegionFactory = clientCache
			.createClientRegionFactory(ClientRegionShortcut.PROXY);

		Region<Long, User> region = clientRegionFactory.create(regionName);

		for (int i = 1; i <= numberOfEntries; i++) {
			if ((i % 10000) == 0) {
				System.out.println("Region \"" + regionName + "\" of server \""
					+ address + "\": Filling record #" + i);
			}
			region.put(new Long(i), new User(i, "Ivan " + i + "th", i % 100,
				i % 2 == 0));
		}

		clientCache.close();
	}

	private void changeData(String address, String regionName)
		throws InterruptedException {

		String host = address.substring(0, address.indexOf("["));
		int port = Integer.parseInt(address.substring(address.indexOf("[") + 1,
			address.indexOf("]")));

		ClientCache clientCache = new ClientCacheFactory()
			.addPoolServer(host, port).set("log-level", "none").create();

		ClientRegionFactory<Long, User> clientRegionFactory = clientCache
			.createClientRegionFactory(ClientRegionShortcut.PROXY);

		Region<Long, User> region = clientRegionFactory.create(regionName);

		region.put(new Long(1), new User(2, "Ivan 2nd", 2, true));
		region.destroy(new Long(2));
		region.put(new Long(1000000000), new User(1000000000,
			"Ivan 1000000000nd", 2, true));

		clientCache.close();
	}

	@Test
	public void testMainPositive() throws FileNotFoundException, IOException,
		InterruptedException {
		System.out.println("testMainPositive");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		long startTime = System.currentTimeMillis();
		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "compare", "-sr",
				REGION_NAME, "-tr", REGION_NAME, "-ss", SERVER_40804, "-ts",
				SERVER_40805, "-c",
				"itest.com.googlecode.icegem.cacheutils.comparator" });

		long finishTime = System.currentTimeMillis();

		System.out.println("Compared in " + (finishTime - startTime) + "ms");

		assertEquals(0, exitCode);
	}

	@Test
	public void testMainPerformance() throws FileNotFoundException,
		IOException, InterruptedException {
		System.out.println("testMainPerformance");

		fillData(SERVER_40804, REGION_NAME, NUMBER_OF_ENTRIES_PERFORMANCE);
		fillData(SERVER_40805, REGION_NAME, NUMBER_OF_ENTRIES_PERFORMANCE);

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		long startTime = System.currentTimeMillis();
		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "compare", "-sr",
				REGION_NAME, "-tr", REGION_NAME, "-ss", SERVER_40804, "-ts",
				SERVER_40805, "-lf", "80", "-c",
				"itest.com.googlecode.icegem.cacheutils.comparator" });

		long finishTime = System.currentTimeMillis();

		System.out.println("Compared in " + (finishTime - startTime) + "ms");

		assertEquals(0, exitCode);

		changeData("localhost[40804]", REGION_NAME);

		startTime = System.currentTimeMillis();
		exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "compare", "-sr",
				REGION_NAME, "-tr", REGION_NAME, "-ss", SERVER_40804, "-ts",
				SERVER_40805, "-lf", "50", "-c",
				"itest.com.googlecode.icegem.cacheutils.comparator" });

		finishTime = System.currentTimeMillis();

		System.out.println("Compared in " + (finishTime - startTime) + "ms");

		assertEquals(1, exitCode);

	}

	@Test
	public void testMainNegative() throws FileNotFoundException, IOException,
		InterruptedException {
		System.out.println("testMainNegative");

		changeData("localhost[40804]", REGION_NAME);

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		long startTime = System.currentTimeMillis();
		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "compare", "-sr",
				REGION_NAME, "-tr", REGION_NAME, "-ss", SERVER_40804, "-ts",
				SERVER_40805, "-c",
				"itest.com.googlecode.icegem.cacheutils.comparator" });

		long finishTime = System.currentTimeMillis();

		System.out.println("Compared in " + (finishTime - startTime) + "ms");

		assertEquals(1, exitCode);
	}

	private static void startCacheServers() throws IOException,
		InterruptedException {
		cacheServer1 = javaProcessLauncher
			.runWithConfirmation(
				"",
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=compareToolReplicateServerProperties40804.properties" }, null);
		cacheServer2 = javaProcessLauncher
			.runWithConfirmation(
				"",
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=compareToolReplicateServerProperties40805.properties" }, null);
		cacheServer3 = javaProcessLauncher
			.runWithConfirmation(
				"",
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=compareToolReplicateServerProperties40814.properties" }, null);
		cacheServer4 = javaProcessLauncher
			.runWithConfirmation(
				"",
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=compareToolReplicateServerProperties40815.properties" }, null);
	}

	private static void stopCacheServers() throws IOException,
		InterruptedException {
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer1);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer2);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer3);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer4);
	}
}
