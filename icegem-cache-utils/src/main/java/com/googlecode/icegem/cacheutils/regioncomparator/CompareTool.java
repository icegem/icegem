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
package com.googlecode.icegem.cacheutils.regioncomparator;

import java.util.*;
import java.util.concurrent.*;

import com.gemstone.gemfire.admin.AdminDistributedSystem;
import com.gemstone.gemfire.admin.AdminDistributedSystemFactory;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.client.internal.AutoConnectionSourceImpl;
import com.gemstone.gemfire.cache.client.internal.PoolImpl;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.internal.ServerLocation;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.icegem.cacheutils.Tool;
import com.googlecode.icegem.cacheutils.common.Utils;

public class CompareTool extends Tool {
    private static final Logger log = LoggerFactory.getLogger(CompareTool.class);
    private static String serversOption = "";
    private static String regionName;
    private static List<String> scanPackagesOption;
    private static Properties locatorsProperties = new Properties();

    protected void parseCommandLineArguments(String[] commandLineArguments) {
        Options options = constructGnuOptions();
        if (commandLineArguments.length < 1) {
            printHelp(options);
            Utils.exitWithSuccess();
        }
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine line = parser.parse(options, commandLineArguments);
            if (!line.hasOption("region") || line.hasOption("help")
                    || !(line.hasOption("locators") || line.hasOption("servers"))) {
                printHelp(options);
                Utils.exitWithSuccess();
            }
            if (line.hasOption("packages"))
                scanPackagesOption = Arrays.asList(line.getOptionValue("packages").split(","));
            regionName = line.getOptionValue("region");
            if (line.hasOption("locators"))
                locatorsProperties = line.getOptionProperties("locators");
            if (line.hasOption("servers"))
                serversOption = line.getOptionValue("servers");

        } catch (ParseException exp) {
            System.err.println("Parsing options failed. " + exp.getMessage());
            printHelp(options);
            Utils.exitWithSuccess();
        }
    }

    protected void printHelp(final Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("compare [options]", options);
    }

    protected Options constructGnuOptions() {
        final Options gnuOptions = new Options();
        Option locators = OptionBuilder.hasArgs()
                .withDescription("Locators of GemFire system. For intra-cluster checking. Example: host1[port1],host2[port2]")
                .withValueSeparator()
                .withArgName("cluster=locators")
                .withLongOpt("locators")
                .create("l");
        gnuOptions
                .addOption("r", "region", true, "Region path to be compared. Only replicated region could be used. Example: /region1/region2")
                .addOption("s", "servers", true, "Servers of GemFire system. For multi-cluster systems. Example: host1[port1],host2[port2]")
                .addOption(locators)
                .addOption("c", "packages", true, "Enumerate packages to scan for @AutoSerializable model classes. Delimiter is a comma sign.")
                .addOption("h", "help", false, "Print usage information");
        return gnuOptions;
    }

    public void execute(String[] args, boolean debugEnabled, boolean quiet) {
        AdminDistributedSystem adminDs = AdminDistributedSystemFactory.getDistributedSystem(
                AdminDistributedSystemFactory.defineDistributedSystem()
        );
        adminDs.connect();

        parseCommandLineArguments(args);

        List<Pool> poolList = new ArrayList<Pool>();
        if (serversOption != null  && serversOption.length() > 0)
            for (String serverOption : serversOption.split(",")) {
                String serverHost = serverOption.substring(0, serverOption.indexOf("["));
                String serverPort = serverOption.substring(serverOption.indexOf("[") + 1, serverOption.indexOf("]"));
                poolList.add(PoolManager.createFactory().addServer(serverHost,Integer.parseInt(serverPort))
                            .create("poolTo" + serverHost + serverPort));
            }
        if (locatorsProperties != null && !locatorsProperties.isEmpty())
            for (Object poolOption : locatorsProperties.keySet()) {
                String locator = (String) locatorsProperties.get(poolOption);
                String serverHost = locator.substring(0, locator.indexOf("["));
                String serverPort = locator.substring(locator.indexOf("[") + 1, locator.indexOf("]"));
                poolList.add(PoolManager.createFactory().addLocator(serverHost, Integer.parseInt(serverPort))          //todo: check when we have two identical locators options: exception a pool name already exist
                        .create("poolTo" + serverHost + serverPort));
            }

        //todo: insert checking that each cluster contains region and one's type is equal (Partitioned, Replicated)

        boolean partitioned = false;        //todo: insert CLI usage  + throw exception if real region has another type


        List<ServerLocation> serverFromPool = new ArrayList<ServerLocation>();
        List<Pool> emptyPools = new ArrayList<Pool>(); //contains pool with no available servers
        for (Pool pool : poolList) {
            List<ServerLocation> allServers = null;
            if (!pool.getLocators().isEmpty())
                allServers = ((AutoConnectionSourceImpl) ((PoolImpl) pool).getConnectionSource()).findAllServers();  //todo: ConnectionError if locator doesn't exist
            else if (!pool.getServers().isEmpty())
                allServers = Arrays.asList((((PoolImpl) pool).getConnectionSource()).findServer(Collections.emptySet()));

            if (allServers != null)
                serverFromPool.addAll(allServers);
            else {
                log.info("not found servers on locator {}", pool);
                emptyPools.add(pool);
            }
        }
        poolList.removeAll(emptyPools);

        if (serverFromPool.size() == 0) {
            log.info("no servers available");
            return;
        }

        printServerLocationDetails(serverFromPool);

        //source for comparison //todo: if this node doesn't contain region! it's problem
        Pool sourcePool;
        if (!partitioned) {
            int randomServerLocation = new Random().nextInt(serverFromPool.size());
            sourcePool = PoolManager.createFactory()
                    .addServer(serverFromPool.get(randomServerLocation).getHostName(),
                            serverFromPool.get(randomServerLocation).getPort()).create("target");
        } else {
            sourcePool = poolList.get(0);
            poolList.remove(0);
        }


        FunctionService.registerFunction(new RegionInfoFunction());
        ResultCollector regionInfoResult = FunctionService.onServers(sourcePool)
                .withArgs(regionName)
                .execute(new RegionInfoFunction());

        Map regionInfo = (HashMap) ((ArrayList) regionInfoResult.getResult()).get(0);
        System.out.println("region info: " + regionInfo);

        int totalNumBuckets = (Integer) regionInfo.get("totalNumBuckets");
        //log.debug("total keys' batch counts is ", totalNumBuckets);
        System.out.println("total keys' batch counts is " + totalNumBuckets);
        KeyExtractor keyExtractor = new KeyExtractor(regionName, sourcePool, partitioned, totalNumBuckets);

        Map<String, Map<String, Set>> clusterDifference = new HashMap<String, Map<String, Set>>();          //key: memeberId list: absent keys, diff values

        List<PoolResult> taskResults = new ArrayList<PoolResult>();
        List<Future<PoolResult>> collectTasks = new ArrayList<Future<PoolResult>>(poolList.size());
        ExecutorService executorService = Executors.newFixedThreadPool(poolList.size());
        while (keyExtractor.hasKeys()) {
            Set keys = keyExtractor.getNextKeysBatch();
            System.out.println("keys to check: " + keys);
            for (Pool nextPool : poolList)
                collectTasks.add(executorService.submit(new CollectorTask(keys, nextPool, regionName)));
            System.out.println("active tasks: " + collectTasks.size());
            try {
                //for (Future<ResultCollector> futureTask : collectTasks) {
                for (Future<PoolResult> futureTask : collectTasks) {
                    taskResults.add(futureTask.get());
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            } catch (ExecutionException ee) {
                ee.printStackTrace();
            }
            collectTasks.clear();

            System.out.println("compare contents..");
            //getting source contents
            Map sourceData = new HashMap();

            //getting source map
            FutureTask<PoolResult> ft = new FutureTask<PoolResult>(new CollectorTask(keys, sourcePool, regionName));
            ft.run();
            try {
                PoolResult rc = ft.get();
                List poolResult = (List) rc.getResultCollector().getResult();
                for (Object singleResult : poolResult) {
                    sourceData.putAll((Map) ((HashMap) singleResult).get("map"));
                }
            } catch (Exception e) {
                throw new RuntimeException("error getting key-hash from pool: " + sourcePool, e);
            }
            //todo: aggregate members' data from one cluster

            System.out.println("source data is: " + sourceData);
            //for (ResultCollector taskResultFromPool : taskResults) {
            for (PoolResult taskResultFromPool : taskResults) {
                List poolResult = (ArrayList) taskResultFromPool.getResultCollector().getResult();
                if (!partitioned) {
                    for (Object resultFromMember : poolResult) {
                        Map result = (HashMap) resultFromMember;
                        String memberId = (String) result.get("memberId");
                        if (regionInfo.get("id").equals(result.get("memberId")))                   //for replicated region
                            continue;
                        Map<String, Set> aggregationInfo = compareAndAggregate(sourceData, (HashMap) result.get("map"));
                        System.out.println("result of comparing is: " + aggregationInfo);
                        if (!clusterDifference.containsKey(memberId)) {
                            aggregationInfo.put("absentKeys", new HashSet());
                            clusterDifference.put(memberId, aggregationInfo);
                        } else {
                            Map<String, Set> difference = clusterDifference.get(memberId);
                            difference.get("absentKeys").addAll((Set) result.get("absentKeys"));
                            difference.get("diffValues").addAll(aggregationInfo.get("diffValues"));
                            clusterDifference.put(memberId, difference);
                        }
                    }
                } else {
                    Map targetData = new HashMap();
                    Set absentKeysFromPool = new HashSet();

                    //aggregate data from different members with partition region
                    for (Object resultFromMember: poolResult) {
                        targetData.putAll((Map) ((HashMap) resultFromMember).get("map"));
                        absentKeysFromPool.addAll((Set) ((HashMap) resultFromMember).get("absentKeys"));
                    }

                    Map<String, Set> aggregationInfo = compareAndAggregate(sourceData, targetData);
                    System.out.println("result of comparing is: " + aggregationInfo);
                    String keyForPartitionRegionType = taskResultFromPool.getPool().toString();
                    if (!clusterDifference.containsKey(keyForPartitionRegionType)) {
                            clusterDifference.put(keyForPartitionRegionType, aggregationInfo);
                        } else {
                            Map<String, Set> difference = clusterDifference.get(keyForPartitionRegionType);
                            difference.get("absentKeys").addAll(aggregationInfo.get("absentKeys"));
                            difference.get("diffValues").addAll(aggregationInfo.get("diffValues"));
                            clusterDifference.put(keyForPartitionRegionType, difference);
                        }
                }
            }

            taskResults.clear();
        }

        System.out.println("____________________________");
        System.out.println("difference: ");
        System.out.println(clusterDifference);
        executorService.shutdown();
        adminDs.disconnect();
    }

    /**
     * compare two snapshots from source and target maps
     * format: key: absentKeys or map
     */
    private static Map<String, Set> compareAndAggregate(Map sourceMap, Map targetMap) {
        //keys witch values are different
        System.out.println("compare maps");
        System.out.println("source: " + sourceMap);
        System.out.println("target: " + targetMap);
        Map<String, Set> aggregationInfo = new HashMap<String, Set>();
        Set<Object> keysForDiffValues = new HashSet<Object>();
        for (Object regionKey : targetMap.keySet()) {
            if (!targetMap.get(regionKey).equals(sourceMap.get(regionKey)))      //we compare not original values, but it's hash code
                keysForDiffValues.add(regionKey);
        }

        aggregationInfo.put("diffValues", keysForDiffValues);
        return aggregationInfo;
    }

    private void printServerLocationDetails(List<ServerLocation> serverLocationList) {
        for (ServerLocation server : serverLocationList) {
            System.out.println("host: " + server.getHostName());
            System.out.println("port: " + server.getPort());
            System.out.println("----------------------");
        }
    }
}
