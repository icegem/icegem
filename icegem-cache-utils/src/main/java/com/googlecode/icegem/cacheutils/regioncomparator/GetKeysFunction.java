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

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.internal.cache.ForceReattemptException;
import com.gemstone.gemfire.internal.cache.PartitionedRegion;
import com.gemstone.gemfire.internal.cache.PartitionedRegionHelper;
import com.gemstone.gemfire.internal.cache.partitioned.PRLocallyDestroyedException;

import java.util.*;

/**
 * return keys for a particular bucket or all keys for replicated region
 * User: Artem Kondratyev, e-mail: kondratevae@gmail.com
 */
public class GetKeysFunction extends FunctionAdapter {

    private static final long serialVersionUID = -1467135292875589062L;
    private static int BATCH_SIZE = 3;                  //todo: how to customize this parameter. also it exists in regioninfofunction. ugly.

    @Override
    public void execute(FunctionContext functionContext) {
        Map<String, Object> args = (HashMap) functionContext.getArguments();
        String regionName = (String) args.get("regionName");
        Region region = CacheFactory.getAnyInstance().getRegion(regionName);
        if (region == null) {
            functionContext.getResultSender().lastResult(new HashSet());
            return;
        }

        int bucket = 0;
        if (PartitionRegionHelper.isPartitionedRegion(region)) {
            bucket = (Integer) args.get("bucket");                                              //todo: NPE if actual region is different from parameter (replicate vs partition)
            PartitionedRegion pr = (PartitionedRegion) region;
            Set keys = new HashSet();
            if (pr.getDataStore().getAllLocalPrimaryBucketIds().contains(bucket)) {
                try {
                    keys.addAll(pr.getDataStore().getKeysLocally(bucket));
                } catch (Exception e) {
                    throw new RuntimeException("error getting local keys for bucket " + bucket, e);
                }
            }
            functionContext.getResultSender().lastResult((HashSet) keys);
            return;
        }

        //todo: it's ugly. better: to ask a particular batch of keys (between oldBatch upper bound and nextBatch lower bound)
        Set keys = region.keySet();
        Iterator iterator = keys.iterator();
        Set keysBatch = new HashSet(BATCH_SIZE);
        while(iterator.hasNext()) {
            keysBatch.add(iterator.next());
            if ((keysBatch.size() + 1) % BATCH_SIZE  == 0) {
                functionContext.getResultSender().sendResult((HashSet) keysBatch);
                keysBatch = new HashSet(BATCH_SIZE);
            }
        }
        functionContext.getResultSender().lastResult((HashSet) keysBatch);
    }

    @Override
    public String getId() {
        return getClass().getName();
    }
}
