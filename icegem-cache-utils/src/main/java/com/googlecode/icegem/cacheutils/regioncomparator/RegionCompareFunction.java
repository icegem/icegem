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

import java.io.Serializable;
import java.util.*;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;

public class RegionCompareFunction extends FunctionAdapter {
    private static final long serialVersionUID = 4569716549835836666L;

    public void execute(FunctionContext fc) {
        Region region = null;
        try {
            Object[] argsArray = (Object[]) fc.getArguments();
            String regionPath = (String) argsArray[0];
            Object[] keySetOnServer = (Object[]) argsArray[1];
            Cache cache = CacheFactory.getAnyInstance();
            region = cache.getRegion(regionPath);
            if (region != null) {
                Map distributedEntriesMap = new HashMap(region.getAll(Arrays.asList(keySetOnServer)));
                Object[] localEntries = region.entrySet().toArray();
                Map localEntriesMap = new HashMap();

                for (Object entry : localEntries) {
                    localEntriesMap.put(((Map.Entry) entry).getKey(), ((Map.Entry) entry).getValue());
                }
                Object[][] result = compareRegions(distributedEntriesMap, localEntriesMap, region);
                fc.getResultSender().lastResult((Serializable) result);
            } else {
                Object[][] result = new Object[4][1];
                result[0][0] = null;
                result[1][0] = null;
                result[2][0] = null;
                result[3][0] = cache.getDistributedSystem().getDistributedMember().getId();
                fc.getResultSender().lastResult((Serializable) result);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            Object[][] result = new Object[4][1];
            result[0][0] = null;
            result[1][0] = null;
            result[2][0] = null;
            result[3][0] = "Exception " + ex.getMessage() + ex.getCause().getMessage() + " was thrown on node " + region.getCache().getDistributedSystem().getDistributedMember().getId();
            fc.getResultSender().lastResult((Serializable) result);
        }

    }

    private Object[][] compareRegions(Map distributedEntries, Map localEntries, Region region) {
        if (distributedEntries.equals(localEntries)) {
            Object[][] result = new Object[4][1];
            result[0][0] = null;
            result[1][0] = null;
            result[2][0] = null;
            result[3][0] = region.getCache().getDistributedSystem().getDistributedMember().getId();
            return result;
        }
        Set missing = new HashSet(distributedEntries.keySet());
        missing.removeAll(localEntries.keySet());

        Set extra = new HashSet(localEntries.keySet());
        extra.removeAll(distributedEntries.keySet());

        Set different = new HashSet();

        distributedEntries.keySet().removeAll(missing);
        localEntries.keySet().removeAll(extra);

        for (Object key : distributedEntries.keySet()) {
            if (!distributedEntries.get(key).equals(localEntries.get(key)))
                different.add(key);

        }
        Object[][] result = new Object[4][1];
        result[0][0] = missing.toArray();
        result[1][0] = extra.toArray();
        result[2][0] = different.toArray();
        result[3][0] = region.getCache().getDistributedSystem().getDistributedMember().getId();
        return result;
    }

    public String getId() {
        return getClass().getName();
    }

}
