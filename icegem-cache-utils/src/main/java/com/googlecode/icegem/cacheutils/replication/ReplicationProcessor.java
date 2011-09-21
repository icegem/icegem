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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.googlecode.icegem.utils.PropertiesHelper;
import com.googlecode.icegem.utils.JavaProcessLauncher;

/**
 * Coordinates, configures and runs the set of guest nodes
 */
public class ReplicationProcessor {

	/* Clusters properties */
	private Properties clustersProperties;

	/* Timeout, milliseconds */
	private long timeout;

	/* Name of the technical region */
	private final String regionName;

	/* Debug enabled flag */
	private final boolean debugEnabled;

	/* Quiet flag */
	private final boolean quiet;

	/* Field javaProcessLauncher */
	private JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher(
		true, true, false);

	/* The time at which the processing has started */
	private long processingStartedAt;

	/**
	 * Configures and creates the instance of replication processor
	 * 
	 * @param clustersProperties
	 *            - the clusters' properties
	 * @param timeout
	 *            - the timeout, milliseconds
	 * @param regionName
	 *            - the name of technical region
	 * @param debugEnabled
	 *            - the debug enabled flag
	 * @param quiet
	 *            - the quiet flag
	 */
	public ReplicationProcessor(Properties clustersProperties, long timeout,
		String regionName, boolean debugEnabled, boolean quiet) {

		processingStartedAt = System.currentTimeMillis();

		this.clustersProperties = clustersProperties;
		this.timeout = timeout;
		this.regionName = regionName;
		this.debugEnabled = debugEnabled;
		this.quiet = quiet;
	}

	/**
	 * Creates, configures and runs the guest nodes and collects information
	 * form them
	 * 
	 * @return - 0 in case of success, 1 otherwise
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public int process() throws IOException, InterruptedException {
		debug("ReplicationProcessor#process(): Processing start");

		Properties gemfireProperties = PropertiesHelper.filterProperties(
			System.getProperties(), "gemfire.");
		String[] vmOptions = PropertiesHelper
			.propertiesToVMOptions(gemfireProperties);

		debug("ReplicationProcessor#process(): vmOptions = "
			+ Arrays.asList(vmOptions));

		List<Process> processesList = new ArrayList<Process>();
		for (Object keyObject : clustersProperties.keySet()) {
			String cluster = (String) keyObject;
			String clustersPropertiesString = PropertiesHelper
				.propertiesToString(clustersProperties);

			debug("ReplicationProcessor#process(): Starting GuestNode with parameters: cluster = "
				+ cluster
				+ ", clustersPropertiesString = "
				+ clustersPropertiesString
				+ ", timeout = "
				+ timeout
				+ ", regionName = " + regionName);

			Process process = javaProcessLauncher.runWithoutConfirmation(
				GuestNode.class,
				vmOptions,
				new String[] { cluster, clustersPropertiesString,
					String.valueOf(timeout), regionName,
					String.valueOf(debugEnabled), String.valueOf(quiet),
					String.valueOf(processingStartedAt) });

			debug("ReplicationProcessor#process(): Adding GuestNode to processList");

			processesList.add(process);
		}

		debug("ReplicationProcessor#process(): Waiting for processes finish");
		int mainExitCode = 0;
		int processNumber = 0;
		for (Process process : processesList) {
			debug("ReplicationProcessor#process(): Waiting for process #"
				+ processNumber);

			int exitCode = process.waitFor();

			if (exitCode != 0) {
				mainExitCode = 1;
			}
			debug("ReplicationProcessor#process(): Process #" + processNumber
				+ " finished with exitCode = " + exitCode);

			processNumber++;
		}

		debug("ReplicationProcessor#process(): Processing finished with mainExitCode = "
			+ mainExitCode);

		return mainExitCode;
	}

	/**
	 * Prints debug information if the debug is enabled
	 * 
	 * @param message
	 *            - the debug message
	 */
	private void debug(String message) {
		if (debugEnabled) {
			long currentTime = System.currentTimeMillis();
			long timeSinceProcessingStart = currentTime - processingStartedAt;
			System.err.println(timeSinceProcessingStart
				+ " [ReplicationProcessor] " + message);
		}
	}

}
