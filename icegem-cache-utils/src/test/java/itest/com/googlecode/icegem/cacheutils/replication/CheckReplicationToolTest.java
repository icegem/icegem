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
package itest.com.googlecode.icegem.cacheutils.replication;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.googlecode.icegem.cacheutils.Launcher;
import com.googlecode.icegem.utils.JavaProcessLauncher;
import com.googlecode.icegem.utils.PropertiesHelper;
import com.googlecode.icegem.utils.ServerTemplate;

/**
 * Starts three locators, three gateways. Each gateway is connected to separate
 * locator. Start replication measurement tool and expects that it will return 0
 * as exit code. This means that replication between all the clusters works.
 */
public class CheckReplicationToolTest {
	/** Field gatewayA */
	private static Process gatewayA;
	/** Field gatewayB */
	private static Process gatewayB;
	/** Field gatewayC */
	private static Process gatewayC;

	/** Field cacheServerA */
	private static Process cacheServerA;
	/** Field cacheServerB */
	private static Process cacheServerB;
	/** Field cacheServerC */
	private static Process cacheServerC;

	/** Field javaProcessLauncher */
	private static JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher(
		false, false, false);

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException,
		TimeoutException {
		startGateways();
	}

	@AfterClass
	public static void tearDown() throws IOException, InterruptedException {
		stopGateways();
	}

	@Test
	public void testMainPositive() throws Exception {
		System.out.println("testMainPositive");

		PropertiesHelper propertiesHelper = new PropertiesHelper(
			"/checkReplicationToolGatewayA.properties");

		String[] vmArguments = new String[] {
			"-Dgemfire.license-file="
				+ propertiesHelper.getStringProperty("license-file"),
			"-Dgemfire.license-type="
				+ propertiesHelper.getStringProperty("license-type"),
			"-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, vmArguments, new String[] { "-d", "-q",
				"check-replication", "-c", "clusterA=localhost[18081]", "-c",
				"clusterB=localhost[18082]", "-c", "clusterC=localhost[18083]",
				"-t", "30000" });

		assertEquals(exitCode, 0);
	}

	@Test
	public void testMainPositiveTwoClusters() throws Exception {
		System.out.println("testMainPositiveTwoClusters");

		PropertiesHelper propertiesHelper = new PropertiesHelper(
			"/checkReplicationToolGatewayA.properties");

		String[] vmArguments = new String[] {
			"-Dgemfire.license-file="
				+ propertiesHelper.getStringProperty("license-file"),
			"-Dgemfire.license-type="
				+ propertiesHelper.getStringProperty("license-type"),
			"-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, vmArguments, new String[] { "-d", "-q",
				"check-replication", "-c", "clusterA=localhost[18081]", "-c",
				"clusterB=localhost[18082]", "-t", "30000" });

		assertEquals(exitCode, 0);
	}

	@Test
	public void testMainPositiveWithWrongLocators() throws Exception {
		System.out.println("testMainPositiveWithWrongLocators");

		PropertiesHelper propertiesHelper = new PropertiesHelper(
			"/checkReplicationToolGatewayA.properties");

		String[] vmArguments = new String[] {
			"-Dgemfire.license-file="
				+ propertiesHelper.getStringProperty("license-file"),
			"-Dgemfire.license-type="
				+ propertiesHelper.getStringProperty("license-type"),
			"-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, vmArguments, new String[] { "check-replication",
				"-c",
				"clusterA=localhost[18081],localhost[18084],localhost[18085]",
				"-c", "clusterB=localhost[18082],localhost[18086]", "-c",
				"clusterC=localhost[18083],localhost[18087]", "-t", "30000" });

		assertEquals(exitCode, 0);
	}

	@Test
	public void testMainNegativeWrongLocator() throws Exception {
		System.out.println("testMainNegativeWrongLocator");

		PropertiesHelper propertiesHelper = new PropertiesHelper(
			"/checkReplicationToolGatewayA.properties");

		String[] vmArguments = new String[] {
			"-Dgemfire.license-file="
				+ propertiesHelper.getStringProperty("license-file"),
			"-Dgemfire.license-type="
				+ propertiesHelper.getStringProperty("license-type"),
			"-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, vmArguments, new String[] { "check-replication",
				"-c", "clusterA=localhost[18081]", "-c",
				"clusterB=localhost[18082]", "-c", "clusterD=localhost[18084]",
				"-t", "10000" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testMainNegativeSingleCluster() throws Exception {
		System.out.println("testMainNegativeSingleCluster");

		PropertiesHelper propertiesHelper = new PropertiesHelper(
			"/checkReplicationToolGatewayA.properties");

		String[] vmArguments = new String[] {
			"-Dgemfire.license-file="
				+ propertiesHelper.getStringProperty("license-file"),
			"-Dgemfire.license-type="
				+ propertiesHelper.getStringProperty("license-type"),
			"-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, vmArguments, new String[] { "check-replication",
				"-c", "clusterA=localhost[18081]", "-t", "10000" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testMainPositiveDefaultLicense() throws Exception {
		System.out.println("testMainPositiveDefaultLicense");

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, null, new String[] { "check-replication", "-c",
				"clusterA=localhost[18081]", "-c", "clusterB=localhost[18082]",
				"-c", "clusterC=localhost[18083]", "-t", "10000" });

		assertEquals(exitCode, 0);
	}

	@Test
	public void testMainNegativeEmptyParameters() throws Exception {
		System.out.println("testMainNegativeEmptyParameters");

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, null, new String[] { "check-replication" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testMainNegativeIncorrectRegion() throws Exception {
		System.out.println("testMainNegativeIncorrectRegion");

		PropertiesHelper propertiesHelper = new PropertiesHelper(
			"/checkReplicationToolGatewayA.properties");

		String[] vmArguments = new String[] {
			"-Dgemfire.license-file="
				+ propertiesHelper.getStringProperty("license-file"),
			"-Dgemfire.license-type="
				+ propertiesHelper.getStringProperty("license-type"),
			"-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, vmArguments, new String[] { "check-replication",
				"-c", "clusterA=localhost[18081]", "-c",
				"clusterB=localhost[18082]", "-c", "clusterC=localhost[18083]",
				"-t", "10000", "-r", "wrong" });

		assertEquals(exitCode, 1);
	}

	@Test
	public void testMainNegativeHelpLauncherArgument() throws Exception {
		System.out.println("testMainNegativeHelpLauncherArgument");

		PropertiesHelper propertiesHelper = new PropertiesHelper(
			"/checkReplicationToolGatewayA.properties");

		String[] vmArguments = new String[] {
			"-Dgemfire.license-file="
				+ propertiesHelper.getStringProperty("license-file"),
			"-Dgemfire.license-type="
				+ propertiesHelper.getStringProperty("license-type"),
			"-Dgemfire.log-level=none" };

		int exitCode = javaProcessLauncher.runAndWaitProcessExitCode(
			Launcher.class, vmArguments, new String[] { "-d", "-q", "-h",
				"check-replication", "-c", "clusterA=localhost[18081]", "-c",
				"clusterB=localhost[18082]", "-c", "clusterC=localhost[18083]",
				"-t", "30000" });

		assertEquals(exitCode, 1);
	}

	private static void startGateways() throws IOException, InterruptedException {
		gatewayA = javaProcessLauncher
			.runWithConfirmation(
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=checkReplicationToolGatewayA.properties" },
				null);
		gatewayB = javaProcessLauncher
			.runWithConfirmation(
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=checkReplicationToolGatewayB.properties" },
				null);
		gatewayC = javaProcessLauncher
			.runWithConfirmation(
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=checkReplicationToolGatewayC.properties" },
				null);

		cacheServerA = javaProcessLauncher
			.runWithConfirmation(
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=checkReplicationToolCacheServerA.properties" },
				null);
		cacheServerB = javaProcessLauncher
			.runWithConfirmation(
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=checkReplicationToolCacheServerB.properties" },
				null);
		cacheServerC = javaProcessLauncher
			.runWithConfirmation(
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=checkReplicationToolCacheServerC.properties" },
				null);
	}

	private static void stopGateways() throws IOException, InterruptedException {
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServerA);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServerB);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServerC);

		javaProcessLauncher.stopBySendingNewLineIntoProcess(gatewayA);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(gatewayB);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(gatewayC);
	}

}
