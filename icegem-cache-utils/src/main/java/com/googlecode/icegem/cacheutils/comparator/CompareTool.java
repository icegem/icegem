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
package com.googlecode.icegem.cacheutils.comparator;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import com.googlecode.icegem.cacheutils.Tool;
import com.googlecode.icegem.cacheutils.common.FileService;
import com.googlecode.icegem.cacheutils.common.Stopwatch;
import com.googlecode.icegem.cacheutils.common.Utils;
import com.googlecode.icegem.cacheutils.comparator.model.ComparisonResult;
import com.googlecode.icegem.cacheutils.comparator.model.Node;
import com.googlecode.icegem.cacheutils.comparator.task.GetNodesTask;
import com.googlecode.icegem.cacheutils.comparator.task.GetNodesTaskArguments;
import com.googlecode.icegem.utils.JavaProcessLauncher;

public class CompareTool extends Tool {

	public static final String PARTITION = "partition";
	public static final String REPLICATE = "replicate";

	private static final String PACKAGES_OPTION = "packages";
	private static final String TARGET_SERVER_OPTION = "target-server";
	private static final String TARGET_REGION_OPTION = "target-region";
	private static final String TARGET_LOCATORS_OPTION = "target-locators";
	private static final String SOURCE_SERVER_OPTION = "source-server";
	private static final String SOURCE_REGION_OPTION = "source-region";
	private static final String SOURCE_LOCATORS_OPTION = "source-locators";
	private static final String LOAD_FACTOR_OPTION = "load-factor";
	private static final String HELP_OPTION = "help";

	private static final String SOURCE_INPUT_FILENAME = "source-input-object";
	private static final String TARGET_INPUT_FILENAME = "target-input-object";
	private static final String SOURCE_OUTPUT_FILENAME = "source-output-object";
	private static final String TARGET_OUTPUT_FILENAME = "target-output-object";

	private static final int DEFAULT_LOAD_FACTOR = 50;

	private String sourceRegionName;
	private String sourceServer = null;
	private String sourceLocators = null;
	private String targetRegionName;
	private String targetServer = null;
	private String targetLocators = null;
	private List<String> packages;
	private int loadFactor = DEFAULT_LOAD_FACTOR;
	private Stopwatch stopwatch = new Stopwatch();

	private JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher(
		false, false, false);

	public void execute(String[] args, boolean debugEnabled, boolean quiet) {
		try {

			parseCommandLineArguments(args);

			compare(new long[] { 0 }, 64);

		} catch (Throwable t) {
			Utils.exitWithFailure("Unexpected throwable", t);
		}
	}

	private void compare(long[] ids, int shift) throws IOException,
		InterruptedException, ClassNotFoundException {

		stopwatch.start();
		long[] childIds = getDifferentChildrenIds(ids, shift);
		stopwatch.stop();

		System.out.println("level #" + ((80 - shift) / 16)
			+ ", number of different entries detected: " + childIds.length + " in "
			+ stopwatch.getDuration() + "ms");

		if (childIds.length > 0) {
			if (shift > 32) {
				compare(childIds, shift - 16);
			} else {
				stopwatch.start();
				ComparisonResult result = calculateResult(childIds);
				stopwatch.stop();

				System.out.println("level #4, number of different entries detected: [extra: "
					+ result.getExtra().size() + ", missed: "
					+ result.getMissed().size() + ", different: "
					+ result.getDifferent().size() + "] in "
					+ stopwatch.getDuration() + "ms");

				System.out.println(result);
				Utils.exitWithFailure();
			}
		} else {
			System.out.println("equal");
			Utils.exitWithSuccess();
		}

	}

	private Process runProcess(String mode, String address, String regionName,
		String inputFilename, String outputFilename, long[] ids, int shift)
		throws IOException, InterruptedException {

		Process process;

		GetNodesTaskArguments arguments = new GetNodesTaskArguments();
		arguments.setAddress(address);
		arguments.setRegionName(regionName);
		arguments.setFilename(outputFilename);
		arguments.setLoadFactor(loadFactor);
		arguments.setPackages(packages);
		arguments.setIds(ids);
		arguments.setShift(shift);
		arguments.setMode(mode);

		FileService.writeObject(inputFilename, arguments);

		process = javaProcessLauncher.runWithoutConfirmation(
			"", GetNodesTask.class, null, new String[] { inputFilename });

		return process;
	}

	@SuppressWarnings("unchecked")
	private ComparisonResult calculateResult(long[] ids) throws IOException,
		InterruptedException, ClassNotFoundException {
		ComparisonResult result = new ComparisonResult();

		Process sourceProcess;
		Process targetProcess;

		if (isPartitioned()) {

			sourceProcess = runProcess(PARTITION, sourceLocators,
				sourceRegionName, SOURCE_INPUT_FILENAME,
				SOURCE_OUTPUT_FILENAME, ids, 16);

			targetProcess = runProcess(PARTITION, targetLocators,
				targetRegionName, TARGET_INPUT_FILENAME,
				TARGET_OUTPUT_FILENAME, ids, 16);

		} else {

			sourceProcess = runProcess(REPLICATE, sourceServer,
				sourceRegionName, SOURCE_INPUT_FILENAME,
				SOURCE_OUTPUT_FILENAME, ids, 16);

			targetProcess = runProcess(REPLICATE, targetServer,
				targetRegionName, TARGET_INPUT_FILENAME,
				TARGET_OUTPUT_FILENAME, ids, 16);

		}

		sourceProcess.waitFor();
		targetProcess.waitFor();

		Set<Node> sourceNodesSet = (Set<Node>) FileService
			.readObject(SOURCE_OUTPUT_FILENAME);
		Set<Node> targetNodesSet = (Set<Node>) FileService
			.readObject(TARGET_OUTPUT_FILENAME);

		Map<Object, Long> sourceDataToHashcodeMap = new HashMap<Object, Long>();
		Map<Object, Long> targetDataToHashcodeMap = new HashMap<Object, Long>();

		for (Node node : sourceNodesSet) {
			for (Node child : node.getChildren()) {
				sourceDataToHashcodeMap.put(child.getData(),
					child.getHashcode());
			}
		}

		for (Node node : targetNodesSet) {
			for (Node child : node.getChildren()) {
				targetDataToHashcodeMap.put(child.getData(),
					child.getHashcode());
			}
		}

		Set<Object> dataSet = new HashSet<Object>();
		dataSet.addAll(sourceDataToHashcodeMap.keySet());
		dataSet.addAll(targetDataToHashcodeMap.keySet());

		for (Object data : dataSet) {
			Long sourceHashcode = sourceDataToHashcodeMap.get(data);
			Long targetHashcode = targetDataToHashcodeMap.get(data);

			if (sourceHashcode == null) {
				result.addMissed(data);
			} else if (targetHashcode == null) {
				result.addExtra(data);
			} else if (sourceHashcode.longValue() != targetHashcode.longValue()) {
				result.addDifferent(data);
			} else {
				throw new IllegalStateException("The entry with key = " + data
					+ " neither extra, missed nor different");
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private long[] getDifferentChildrenIds(long[] ids, int shift)
		throws IOException, InterruptedException, ClassNotFoundException {
		Process sourceProcess;
		Process targetProcess;

		if (isPartitioned()) {

			sourceProcess = runProcess(PARTITION, sourceLocators,
				sourceRegionName, SOURCE_INPUT_FILENAME,
				SOURCE_OUTPUT_FILENAME, ids, shift);

			targetProcess = runProcess(PARTITION, targetLocators,
				targetRegionName, TARGET_INPUT_FILENAME,
				TARGET_OUTPUT_FILENAME, ids, shift);

		} else {

			sourceProcess = runProcess(REPLICATE, sourceServer,
				sourceRegionName, SOURCE_INPUT_FILENAME,
				SOURCE_OUTPUT_FILENAME, ids, shift);

			targetProcess = runProcess(REPLICATE, targetServer,
				targetRegionName, TARGET_INPUT_FILENAME,
				TARGET_OUTPUT_FILENAME, ids, shift);

		}

		sourceProcess.waitFor();
		targetProcess.waitFor();

		Set<Node> sourceNodesSet = (Set<Node>) FileService
			.readObject(SOURCE_OUTPUT_FILENAME);
		Set<Node> targetNodesSet = (Set<Node>) FileService
			.readObject(TARGET_OUTPUT_FILENAME);

		Map<Long, Node> sourceIdToNodeMap = new HashMap<Long, Node>();
		Map<Long, Node> targetIdToNodeMap = new HashMap<Long, Node>();

		for (Node node : sourceNodesSet) {
			sourceIdToNodeMap.put(node.getId(), node);
		}

		for (Node node : targetNodesSet) {
			targetIdToNodeMap.put(node.getId(), node);
		}

		Set<Long> idsSet = new HashSet<Long>();
		idsSet.addAll(sourceIdToNodeMap.keySet());
		idsSet.addAll(targetIdToNodeMap.keySet());

		Set<Long> differentIdsSet = new HashSet<Long>();
		for (Long id : idsSet) {
			Node sourceNode = sourceIdToNodeMap.get(id);
			Node targetNode = targetIdToNodeMap.get(id);

			if (sourceNode == null) {
				differentIdsSet.addAll(targetNode.getChildrenIdsSet());
			} else if (targetNode == null) {
				differentIdsSet.addAll(sourceNode.getChildrenIdsSet());
			} else {
				long sourceHashcode = sourceNode.getHashcode();
				long targetHashcode = targetNode.getHashcode();

				if (sourceHashcode != targetHashcode) {
					Node[] sourceChildren = sourceNode.getChildren();
					Node[] targetChildren = targetNode.getChildren();

					Map<Long, Node> childrenSourceIdToNodeMap = new HashMap<Long, Node>();
					Map<Long, Node> childrenTargetIdToNodeMap = new HashMap<Long, Node>();

					for (Node node : sourceChildren) {
						childrenSourceIdToNodeMap.put(node.getId(), node);
					}

					for (Node node : targetChildren) {
						childrenTargetIdToNodeMap.put(node.getId(), node);
					}

					Set<Long> childrenIdsSet = new HashSet<Long>();
					childrenIdsSet.addAll(childrenSourceIdToNodeMap.keySet());
					childrenIdsSet.addAll(childrenTargetIdToNodeMap.keySet());

					for (Long childId : childrenIdsSet) {
						Node childSourceNode = childrenSourceIdToNodeMap
							.get(childId);
						Node childTargetNode = childrenTargetIdToNodeMap
							.get(childId);

						if (childSourceNode == null) {
							differentIdsSet.add(childTargetNode.getId());
						} else if (childTargetNode == null) {
							differentIdsSet.add(childSourceNode.getId());
						} else if (childSourceNode.getHashcode() != childTargetNode
							.getHashcode()) {
							differentIdsSet.add(childSourceNode.getId());
						}
					}
				}
			}
		}

		long[] childIds = new long[differentIdsSet.size()];
		int i = 0;
		for (Long id : differentIdsSet) {
			childIds[i++] = id;
		}

		return childIds;
	}

	private boolean isPartitioned() {
		return ((sourceLocators != null) && (targetLocators != null));
	}

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

			if (line.hasOption(SOURCE_REGION_OPTION)) {
				sourceRegionName = line.getOptionValue(SOURCE_REGION_OPTION);
			} else {
				printHelp(options);
			}

			if (line.hasOption(TARGET_REGION_OPTION)) {
				targetRegionName = line.getOptionValue(TARGET_REGION_OPTION);
			} else {
				printHelp(options);
			}

			if (line.hasOption(SOURCE_SERVER_OPTION)
				&& line.hasOption(TARGET_SERVER_OPTION)) {

				sourceServer = line.getOptionValue(SOURCE_SERVER_OPTION);
				targetServer = line.getOptionValue(TARGET_SERVER_OPTION);

			} else if (line.hasOption(SOURCE_LOCATORS_OPTION)
				&& line.hasOption(TARGET_LOCATORS_OPTION)) {

				sourceLocators = line.getOptionValue(SOURCE_LOCATORS_OPTION);
				targetLocators = line.getOptionValue(TARGET_LOCATORS_OPTION);

			} else {
				printHelp(options);
			}

			if (line.hasOption(PACKAGES_OPTION)) {
				packages = Arrays.asList(line.getOptionValue(PACKAGES_OPTION)
					.split(","));
			}

			if (line.hasOption(LOAD_FACTOR_OPTION)) {
				String loadFactorString = line
					.getOptionValue(LOAD_FACTOR_OPTION);

				try {
					loadFactor = Integer.parseInt(loadFactorString);
				} catch (Throwable t) {
					Utils.exitWithFailure("Cannot parse the "
						+ LOAD_FACTOR_OPTION + " option, value = "
						+ loadFactorString);
				}

				if ((loadFactor < 1) || (loadFactor > 100)) {
					Utils.exitWithFailure("The " + LOAD_FACTOR_OPTION
						+ " option, value = " + loadFactorString
						+ " is out of range [1, 100]");
				}
			}

		} catch (Throwable t) {
			Utils
				.exitWithFailure(
					"Throwable caught during the command-line arguments parsing",
					t);
		}
	}

	protected void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("compare <--" + SOURCE_REGION_OPTION + "> <--"
			+ TARGET_REGION_OPTION + "> < --" + SOURCE_SERVER_OPTION + " --"
			+ TARGET_SERVER_OPTION + " | --" + SOURCE_LOCATORS_OPTION + " --"
			+ TARGET_LOCATORS_OPTION + " > [--" + PACKAGES_OPTION + "] [--"
			+ LOAD_FACTOR_OPTION + "]", options);

		Utils.exitWithFailure();
	}

	protected Options constructGnuOptions() {
		final Options gnuOptions = new Options();
		gnuOptions
			.addOption("sr", SOURCE_REGION_OPTION, true,
				"The name of source region")
			.addOption("ss", SOURCE_SERVER_OPTION, true,
				"Source server in format host[port]")
			.addOption("sl", SOURCE_LOCATORS_OPTION, true,
				"Source cluster locators in format host1[port1],host2[port2]")
			.addOption("tr", TARGET_REGION_OPTION, true,
				"The name of target region")
			.addOption("ts", TARGET_SERVER_OPTION, true,
				"Target server in format host[port]")
			.addOption("tl", TARGET_LOCATORS_OPTION, true,
				"Target cluster locators in format host1[port1],host2[port2]")
			.addOption(
				"lf",
				LOAD_FACTOR_OPTION,
				true,
				"The percent of time the comparator tries to use on each server. "
					+ "The possible values range [1, 100]. Default value is "
					+ DEFAULT_LOAD_FACTOR + ".")
			.addOption(
				"c",
				PACKAGES_OPTION,
				true,
				"Enumerate packages to scan for @AutoSerializable model classes. Delimiter is a comma sign.")
			.addOption("h", HELP_OPTION, false, "Print usage information");
		return gnuOptions;
	}

}
