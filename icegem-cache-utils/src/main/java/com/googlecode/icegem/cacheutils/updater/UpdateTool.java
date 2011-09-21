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
package com.googlecode.icegem.cacheutils.updater;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.admin.AdminException;
import com.gemstone.gemfire.cache.Region;
import com.googlecode.icegem.cacheutils.Tool;
import com.googlecode.icegem.cacheutils.common.AdminService;
import com.googlecode.icegem.cacheutils.common.PeerCacheService;
import com.googlecode.icegem.cacheutils.common.Utils;

public class UpdateTool extends Tool {
	private static final Logger log = LoggerFactory.getLogger(UpdateTool.class);
	private static boolean withSubRegionsOption;
	private static String regionsOption;
	private static String locatorOption;
    private static String serverOption;
    private static List<String> scanPackagesOption;
	
	protected void parseCommandLineArguments(String[] commandLineArguments) {
		Options options = constructGnuOptions();
		if (commandLineArguments.length < 1) {
			printHelp(options);
            Utils.exitWithSuccess();
		}
		CommandLineParser parser = new GnuParser();
	    try {
	        CommandLine line = parser.parse( options, commandLineArguments );
	        if(line.hasOption("regions")) {
	        	withSubRegionsOption = line.hasOption("subregions");
	        	regionsOption = line.getOptionValue("regions");
	        	locatorOption = line.getOptionValue("locator");
                serverOption = line.getOptionValue("server");
                if(line.hasOption("packages"))
                    scanPackagesOption = Arrays.asList(line.getOptionValue("packages").split(","));
	        } else if (line.hasOption("all")) {
	        	regionsOption = "all";
	        	locatorOption = line.getOptionValue("locator");
                serverOption = line.getOptionValue("server");
                if(line.hasOption("packages"))
                    scanPackagesOption = Arrays.asList(line.getOptionValue("packages").split(","));
	        } else if (line.hasOption("help")) {
	        	printHelp(options);
	            Utils.exitWithSuccess();
	        } else {
	        	printHelp(options);
	            Utils.exitWithSuccess();
	        }
	    }
	    catch(ParseException exp) {
	        System.err.println( "Parsing options failed. " + exp.getMessage() );
	        printHelp(options);
            Utils.exitWithSuccess();
	    }
	}
	
	protected void printHelp(final Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter
			.printHelp(
				"update <--regions <--locator> <--server> [--subregions] [--packages]  | --all <--locator> <--server> [--packages] | --help>",
				options);
	}
	
	protected Options constructGnuOptions() {
		final Options gnuOptions = new Options();

		gnuOptions.addOption("r", "regions", true, "Enumerate regions to be updated here. Delimiter is a comma sign. Example: region1,region2,region3...")
				.addOption("c", "subregions", false, "Indicate whether to update all subregions of mentioned regions")
				.addOption("a", "all", false, "Update all regions in system")
				.addOption("l", "locator", true, "Locator of GemFire system. Example: host[port]")
                .addOption("s", "server", true, "Server of GemFire system. Example: host[port]")
                .addOption("p", "packages", true, "Enumerate packages to scan for @AutoSerializable model classes. Delimiter is a comma sign.")
				.addOption("h", "help", false, "Print usage information");
		return gnuOptions;
	}

	private void filterRegions(Set<Region<?,?>> regionsSet) {
		Set<Region<?,?>> childRegionsSet = new HashSet<Region<?,?>>();
		for (Region<?, ?> region : regionsSet) {
			if (region.getParentRegion() != null) {
				childRegionsSet.add(region);
			}
		}
		
		regionsSet.removeAll(childRegionsSet);
	}

    public void execute(String[] args, boolean debugEnabled, boolean quiet) {
		try {
			parseCommandLineArguments(args);
			log.info("Connecting to the system as admin member...");
			AdminService admin = null;
			try {
				admin = new AdminService(locatorOption, false);
			} catch (Exception e) {
				log.info("Failed to connect to the system. " + e.getMessage());
				Utils.exitWithFailure("Failed to connect to the system");
			}
			log.info("Collect system regions...");
			Map<String, String> regionNames = null;
			try {
				regionNames = admin.getRegionNames(regionsOption,
					withSubRegionsOption);
			} catch (AdminException e) {
				log.info("Failed to get system regions. " + e.getMessage());
				admin.close();
				Utils.exitWithFailure("Failed to get system regions");
			}
			log.info("Found following system regions: " + regionNames.values());
			log.info("Closing admin member...");
			admin.close();

			log.info("Connecting to system regions...");
			PeerCacheService peerCacheService = null;
			try {
				peerCacheService = new PeerCacheService(serverOption,
					scanPackagesOption);
			} catch (Exception e) {
				log.info("Failed to startup updater cache. " + e.getMessage());
				Utils.exitWithFailure("Failed to startup updater cache");
			}
			Set<Region<?, ?>> regions = peerCacheService
				.createRegions(regionNames);

			log.info("withSubRegionsOption = " + withSubRegionsOption);
			if (!withSubRegionsOption) {
				log.info("Regions size before filtering: " + regions.size());
				filterRegions(regions);
				log.info("Regions size after filtering: " + regions.size());
			}

			Updater updater = new Updater();
			log.info("Updating regions...");
			updater.updateRegions(regions);
			log.info("Regions update finished successfuly");
			log.info("Closing client cache...");
			peerCacheService.close();
		} catch (Throwable t) {
			Utils.exitWithFailure("Unexpected throwable", t);
		}
    }
}
