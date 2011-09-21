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

import com.gemstone.gemfire.admin.AdminDistributedSystem;
import com.gemstone.gemfire.admin.AdminDistributedSystemFactory;
import com.gemstone.gemfire.admin.AdminException;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.*;
import com.gemstone.gemfire.cache.client.internal.AutoConnectionSourceImpl;
import com.gemstone.gemfire.cache.client.internal.ExplicitConnectionSourceImpl;
import com.gemstone.gemfire.cache.client.internal.PoolImpl;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.internal.ServerLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * User: Artem Kondratyev, e-mail: kondratevae@gmail.com
 */
public class HashCodeCollector {

    private static Logger logger = LoggerFactory.getLogger(HashCodeCollector.class);

    private static String REGION_NAME = "data"; //todo: insert from cli

    public static void main(String[] args) throws AdminException {
        AdminDistributedSystem adminDs = AdminDistributedSystemFactory.getDistributedSystem(
                AdminDistributedSystemFactory.defineDistributedSystem()
        );
        adminDs.connect();

        //connect to different clusters. test data                             todo: insert cli usage
        Pool poolForCluster1 = PoolManager.createFactory().addLocator("localhost", 10330).create("cluster1");
        Pool poolForCluster2 = PoolManager.createFactory().addLocator("localhost", 10332).create("cluster2");
        //Pool poolForServer =  PoolManager.createFactory().addServer("localhost", 40405).create("node");
        List<Pool> poolList = new ArrayList<Pool>();
        poolList.add(poolForCluster1);
        poolList.add(poolForCluster2);
        //poolList.add(poolForServer);

        //todo: insert checking that each cluster contains region and one's type is equal (Partitioned, Replicated)

        boolean partitioned = true;        //todo: insert CLI usage  + throw exception if real region has another type


        List<ServerLocation> serverFromPool = new ArrayList<ServerLocation>();
        List<Pool> emptyPools = new ArrayList<Pool>(); //contains pool with no available servers
        for (Pool pool : poolList) {
            List<ServerLocation> allServers = null;
            if (!pool.getLocators().isEmpty())
                allServers = ((AutoConnectionSourceImpl) ((PoolImpl) pool).getConnectionSource()).findAllServers();
            else if (!pool.getServers().isEmpty())
                allServers = Arrays.asList((((PoolImpl) pool).getConnectionSource()).findServer(Collections.emptySet()));

            if (allServers != null)
                serverFromPool.addAll(allServers);
            else {
                logger.info("not found servers on locator {}", pool);
                emptyPools.add(pool);
            }
        }
        poolList.removeAll(emptyPools);

        if (serverFromPool.size() == 0) {
            logger.info("no servers available");
            return;
        }

        for (ServerLocation server : serverFromPool) {
            System.out.println("host: " + server.getHostName());
            System.out.println("port: " + server.getPort());
            System.out.println("----------------------");
        }

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
                .withArgs(REGION_NAME)
                .execute(new RegionInfoFunction());

        Map regionInfo = (HashMap) ((ArrayList) regionInfoResult.getResult()).get(0);
        System.out.println("region info: " + regionInfo);

        int totalNumBuckets = (Integer) regionInfo.get("totalNumBuckets");
        //logger.debug("total keys' batch counts is ", totalNumBuckets);
        System.out.println("total keys' batch counts is " + totalNumBuckets);
        KeyExtractor keyExtractor = new KeyExtractor(REGION_NAME, sourcePool, partitioned, totalNumBuckets);


        Map<String, Map<String, Set>> clusterDifference = new HashMap<String, Map<String, Set>>();          //key: memeberId list: absent keys, diff values

        //List<ResultCollector> taskResults = new ArrayList<ResultCollector>();
        List<PoolResult> taskResults = new ArrayList<PoolResult>();
        //List<Future<ResultCollector>> collectTasks = new ArrayList<Future<ResultCollector>>(poolList.size());
        List<Future<PoolResult>> collectTasks = new ArrayList<Future<PoolResult>>(poolList.size());
        ExecutorService executorService = Executors.newFixedThreadPool(poolList.size());
        while (keyExtractor.hasKeys()) {
            Set keys = keyExtractor.getNextKeysBatch();
            System.out.println("keys to check: " + keys);
            for (Pool nextPool : poolList)
                collectTasks.add(executorService.submit(new CollectorTask(keys, nextPool, REGION_NAME)));
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
            //getting original contents
            Map originalData = new HashMap();
            /*           for (ResultCollector taskResultFromPool : taskResults) {
                List memberResult = (ArrayList) taskResultFromPool.getResult();
                if (!partitioned)
                    for (Object result : memberResult) {                        //if cluster has several nodes with replicated region... todo: adopt for partitioned
                        if (regionInfo.get("id").equals(((HashMap) result).get("memberId"))) {
                            originalData = (HashMap) ((HashMap) result).get("map");
                            break;
                        }
                    }
                else {
                    FutureTask<ResultCollector> ft = new FutureTask<ResultCollector>(new CollectorTask(keys, sourcePool, REGION_NAME));
                    try {
                        ResultCollector rc = ft.get();
                    } catch (Exception e) {
                        throw new RuntimeException("error getting key-hashcode from pool: " + sourcePool, e);
                    }
                }
            }*/
            //FutureTask<ResultCollector> ft = new FutureTask<ResultCollector>(new CollectorTask(keys, sourcePool, REGION_NAME));
            FutureTask<PoolResult> ft = new FutureTask<PoolResult>(new CollectorTask(keys, sourcePool, REGION_NAME));
            ft.run();
            try {
                //ResultCollector rc = ft.get();
                PoolResult rc = ft.get();
                List poolResult = (List) rc.getResultCollector().getResult();
                for (Object singleResult : poolResult) {
                    originalData.putAll((Map) ((HashMap) singleResult).get("map"));
                }
            } catch (Exception e) {
                throw new RuntimeException("error getting key-hash from pool: " + sourcePool, e);
            }

            /*    if (true)
            return;

        //todo: aggregate members' data from one cluster
        if (partitioned)
            aggregateSingleClusterMemberData(taskResults);*/

            System.out.println("original data is: " + originalData);
            //for (ResultCollector taskResultFromPool : taskResults) {
            for (PoolResult taskResultFromPool : taskResults) {
                List poolResult = (ArrayList) taskResultFromPool.getResultCollector().getResult();
                if (!partitioned) {
                    for (Object resultFromMember : poolResult) {
                        Map result = (HashMap) resultFromMember;
                        if (regionInfo.get("id").equals(result.get("memberId")))                   //for replicated region
                            continue;
                      /*  System.out.println("member id: " + result.get("memberId"));
                        System.out.println("absent keys is " + result.get("absentKeys"));
                        System.out.println("it's data is: " + result.get("map"));*/
                        String memberId = (String) result.get("memberId");
                        Map memberData = (HashMap) result.get("map");
                        Map memberDataCopy = new HashMap();
                        memberDataCopy.putAll(memberData);
                        memberDataCopy.keySet().retainAll(originalData.keySet());
                        Map<String, Set> aggregationInfo = new HashMap<String, Set>();      //contains aggregation info for a member
                        //keys witch values are different
                        Set keysForDiffValues = new HashSet();
                        Set absentKeys = new HashSet();
                        for (Object regionKey : memberDataCopy.keySet())
                            if (!memberDataCopy.get(regionKey).equals(originalData.get(regionKey)))      //we compare not original values, but it's hash code
                                keysForDiffValues.add(regionKey);

                        aggregationInfo.put("diffValues", keysForDiffValues);
                        //absent keys
                        memberData.keySet().removeAll(memberDataCopy.keySet());

                        absentKeys.addAll(memberData.keySet());
                        absentKeys.addAll((HashSet) result.get("absentKeys"));
                        aggregationInfo.put("absentKeys", absentKeys);

                       /* if (!clusterDifference.containsKey(memberId)) {
                            clusterDifference.put(memberId, aggregationInfo);
                        } else {
                            Map<String, Set> difference = clusterDifference.get(memberId);
                            difference.get("absentKeys").addAll(aggregationInfo.get("absentKeys"));
                            difference.get("diffValues").addAll(aggregationInfo.get("diffValues"));
                            clusterDifference.put(memberId, difference);
                        }*/
                         if (!clusterDifference.containsKey(memberId)) {
                            clusterDifference.put(memberId, aggregationInfo);
                        } else {
                            Map<String, Set> difference = clusterDifference.get(memberId);
                            difference.get("absentKeys").addAll(aggregationInfo.get("absentKeys"));
                            difference.get("diffValues").addAll(aggregationInfo.get("diffValues"));
                            clusterDifference.put(memberId, difference);
                        }
                    }
                } else {
                    Map targetMap = new HashMap();
                    Set absentPoolKeys = new HashSet();     //aggregate absent keys from pool members
                    for (Object memberResult : poolResult) {
                        targetMap.putAll((Map) ((HashMap) memberResult).get("map"));
                        absentPoolKeys.addAll((Set) (((HashMap)memberResult).get("absentKeys")));
                    }
                    Map memberDataCopy = new HashMap();
                    memberDataCopy.putAll(targetMap);
                    memberDataCopy.keySet().retainAll(originalData.keySet());
                    Map<String, Set> aggregationInfo = new HashMap<String, Set>();      //contains aggregation info for a member
                    //keys witch values are different
                    Set keysForDiffValues = new HashSet();
                    Set absentKeys = new HashSet();
                    for (Object regionKey : memberDataCopy.keySet())
                        if (!memberDataCopy.get(regionKey).equals(originalData.get(regionKey)))      //we compare not original values, but it's hash code
                            keysForDiffValues.add(regionKey);

                    aggregationInfo.put("diffValues", keysForDiffValues);
                    //absent keys
                    targetMap.keySet().removeAll(memberDataCopy.keySet());

                    absentKeys.addAll(targetMap.keySet());
                    absentKeys.addAll(absentPoolKeys);
                    //aggregationInfo.put("absentKeys", absentKeys);

                    if (!clusterDifference.containsKey(taskResultFromPool.getPool().toString())) {
                        clusterDifference.put(taskResultFromPool.getPool().toString(), aggregationInfo);
                    } else {
                        Map<String, Set> difference = clusterDifference.get(taskResultFromPool.getPool().toString());
                      //  difference.get("absentKeys").addAll(aggregationInfo.get("absentKeys"));
                        difference.get("diffValues").addAll(aggregationInfo.get("diffValues"));
                        clusterDifference.put(taskResultFromPool.getPool().toString(), difference);
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
     * only for partitioned region!
     */
    private static void aggregateSingleClusterMemberData(List<ResultCollector> taskResults) {
        for (ResultCollector singleCluster : taskResults) {
            List membersResult = (List) singleCluster.getResult();
            for (Object resultFromNode : membersResult) {
                System.out.print(((HashMap) resultFromNode).get("ds") + ":");
            }
            System.out.println();
        }
    }

    /*static class CollectorTask implements Callable<ResultCollector> {
        private HashSet keys;
        private Pool pool;
        private String regionName;

        CollectorTask(HashSet keys, Pool pool, String regionName) {
            this.keys = keys;
            this.pool = pool;
            this.regionName = regionName;
        }

        public ResultCollector call() throws Exception {
            Map args = new HashMap();
            args.put("regionName", regionName);
            args.put("keys", keys);
            return FunctionService.onServers(pool)
                    .withArgs((HashMap) args)
                    .execute(new HashCodeCollectorFunction());
        }
    }*/
}

