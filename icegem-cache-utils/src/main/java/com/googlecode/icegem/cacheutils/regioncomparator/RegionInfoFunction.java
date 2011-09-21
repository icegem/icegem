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
import com.gemstone.gemfire.internal.cache.PartitionedRegion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Artem Kondratyev, e-mail: kondratevae@gmail.com
 */
public class RegionInfoFunction extends FunctionAdapter {

    private static int BATCH_SIZE = 3;   //todo: for replicated region. see: GetKeysFunction
    private static final long serialVersionUID = -7733341987189336659L;

    @Override
    public void execute(FunctionContext functionContext) {
        String regionName = (String) functionContext.getArguments();
        Region region = CacheFactory.getAnyInstance().getRegion(regionName);
        if (region == null) {
            System.out.println("region " + regionName + " doesn't exist");
            functionContext.getResultSender().lastResult(new HashMap());
        }
        /*if (region == null)
            throw new NullPointerException("there's no such region \'" + regionName + "\' on this server");*/

        Map<String, Object> regionInfo = new HashMap<String, Object>();
        boolean isPartitioned = false;
        if (PartitionRegionHelper.isPartitionedRegion(region))
            isPartitioned = true;
        regionInfo.put("isPartitioned", isPartitioned);
        regionInfo.put("id", CacheFactory.getAnyInstance().getDistributedSystem().getDistributedMember().getId());

        if (isPartitioned) {
            /*int totalNumBuckets = PartitionRegionHelper.getPartitionRegionInfo(region).getCreatedBucketCount();*/
            int totalNumBuckets = PartitionRegionHelper.getPartitionRegionInfo(region).getConfiguredBucketCount();
            regionInfo.put("totalNumBuckets", totalNumBuckets);
        } else {
            if (region.keySet().size() > 0)
                regionInfo.put("totalNumBuckets", region.keySet().size() / BATCH_SIZE + 1);
            else
                regionInfo.put("totalNumBuckets", 0);
        }
        functionContext.getResultSender().lastResult((HashMap) regionInfo);
    }

    @Override
    public String getId() {
        return getClass().getName();
    }
}

