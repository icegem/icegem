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

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;

import java.util.*;

/**
 * extract keys from region:
 *  for partitioned by buckets
 *  for replicated by batch size
 *
* User: Artem Kondratyev, e-mail: kondratevae@gmail.com
*/
class KeyExtractor {

    private String regionName;
    private Pool sourcePool;
    private boolean isPartitioned;
    private int totalBatches;
    private int currentBatch = 0;
    private List<Set> keysBatch;

    KeyExtractor(String regionName, Pool sourcePool, boolean isPartitioned, int totalBatches) {
        assert regionName != null;
        assert sourcePool != null;
        this.regionName = regionName;
        this.sourcePool = sourcePool;
        this.isPartitioned = isPartitioned;
        this.totalBatches = totalBatches;
        keysBatch = new ArrayList<Set>();
        if (!isPartitioned) {
            Map args = new HashMap();
            args.put("regionName", regionName);
            ResultCollector resultCollector = FunctionService.onServer(sourcePool)
                    .withArgs((HashMap) args)
                    .execute(new GetKeysFunction());
            for(Object keys: (List) resultCollector.getResult()) {
                keysBatch.add((Set) keys);
            }
        }
    }

    public boolean hasKeys() {
        return currentBatch <= totalBatches;
    }

    public Set getNextKeysBatch() {
        Set result = new HashSet();
        if (isPartitioned) {
            Map args = new HashMap();
            args.put("regionName", regionName);
            while (result.isEmpty() && hasKeys()) {     //todo: less then configured buckets count                                 //iterate over empty buckets
                args.put("bucket", currentBatch);
                currentBatch++;
                List keySearchResult = (List) FunctionService.onServers(sourcePool)
                    .withArgs((HashMap) args)
                    .execute(new GetKeysFunction())
                    .getResult();
                for (Object keySearchResultForMember: keySearchResult) {
                    result.addAll((Set)keySearchResultForMember);
            }
            }
            System.out.println("found keys for bucket " + currentBatch);
            return result;
        }

        return keysBatch.get(currentBatch++);
    }
}
