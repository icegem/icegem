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
package itest.com.googlecode.icegem.cacheutils.monitor;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.icegem.cacheutils.Launcher;
import com.googlecode.icegem.cacheutils.monitor.MonitorTool;
import com.googlecode.icegem.cacheutils.monitor.controller.event.NodeEvent;
import com.googlecode.icegem.cacheutils.monitor.controller.event.NodeEventHandler;
import com.googlecode.icegem.utils.JavaProcessLauncher;
import com.googlecode.icegem.utils.PropertiesHelper;
import com.googlecode.icegem.utils.ServerTemplate;

import static org.junit.Assert.*;

public class MonitorToolTest {
	/** Field cacheServer1 */
	private static Process cacheServer1;
	/** Field cacheServer2 */
	private static Process cacheServer2;
	/** Field javaProcessLauncher */
	private static JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher(
		false, false, false);

	private class CountingNodeEventHandler implements NodeEventHandler {

		private int count = 0;

		public void handle(NodeEvent event) {
			count++;
		}

		public int getCount() {
			return count;
		}
	}

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException,
		TimeoutException {
		startCacheServers();
	}

	@AfterClass
	public static void tearDown() throws IOException, InterruptedException {
		stopCacheServers();
	}

	@Test
	public void testMainPositive() throws Exception {
		System.out.println("testMainPositive");

		PropertiesHelper propertiesHelper = new PropertiesHelper(
			"/monitorToolServerProperties40404.properties");

		MonitorTool tool = new MonitorTool();

		CountingNodeEventHandler handler = new CountingNodeEventHandler();
		tool.addNodeEventHandler(handler);

		tool.execute(
			new String[] { "-a", "-l",
				propertiesHelper.getStringProperty("locators") }, false, false);

		Thread.sleep(5 * 1000);

		assertEquals(handler.getCount(), 4);

		tool.shutdown();
	}

	@Test
	public void testMainNegativeNoLocators() throws Exception {
		System.out.println("testMainNegativeNoLocators");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "monitor", "-a" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testMainNegativeNoOptions() throws Exception {
		System.out.println("testMainNegativeNoOptions");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "monitor" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testMainNegativeIncorrectLocator() throws Exception {
		System.out.println("testMainNegativeIncorrectLocator");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "monitor", "-a", "-l",
				"localhost[-1]" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testMainNegativeLocatorIsNotSpecified() throws Exception {
		System.out.println("testMainNegativeLocatorIsNotSpecified");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher
			.runAndWaitProcessExitCode("", Launcher.class,
				vmArguments, new String[] { "monitor", "-a", "-l" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testIsServerAlivePositive() throws Exception {
		System.out.println("testIsServerAlivePositive");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "monitor", "-s",
				"localhost[40404]", "-t", "3000" });

		assertEquals(exitCode, 0);
	}

	@Test
	public void testIsServerAliveNegativeDownServer() throws Exception {
		System.out.println("testIsServerAliveNegativeDownServer");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "monitor", "-s",
				"localhost[50505]", "-t", "3000" });

		assertEquals(exitCode, 1);

	}

	@Test
	public void testIsServerAliveNegativeIncorrectServer() throws Exception {
		System.out.println("testIsServerAliveNegativeIncorrectServer");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "monitor", "-s",
				"localhost[-1]" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testIsServerAliveNegativeNoServer() throws Exception {
		System.out.println("testIsServerAliveNegativeNoServer");

		String[] vmArguments = new String[] { "-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			"", Launcher.class, vmArguments, new String[] { "monitor", "-s" });

		assertEquals(exitCode, 1);
	}

	private static void startCacheServers() throws IOException, InterruptedException {
		cacheServer1 = javaProcessLauncher
			.runWithConfirmation(
				"",
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=monitorToolServerProperties40404.properties" }, null);
		cacheServer2 = javaProcessLauncher
			.runWithConfirmation(
				"",
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=monitorToolServerProperties40405.properties" }, null);
	}

	private static void stopCacheServers() throws IOException, InterruptedException {
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer1);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer2);
	}

}
