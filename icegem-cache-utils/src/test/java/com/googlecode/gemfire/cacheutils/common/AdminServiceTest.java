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
package com.googlecode.gemfire.cacheutils.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.admin.AdminException;
import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.Scope;
import com.googlecode.icegem.cacheutils.common.AdminService;
@Ignore
public class AdminServiceTest {
    private static final Logger log = LoggerFactory.getLogger(AdminServiceTest.class);
    private static final int LOCATOR_PORT = 11357;
    private Cache cache;
    private AdminService admin;
    private Set<String> expectedRegionNames = new HashSet<String>();
    private Set<String> reducedExpectedRegionNames = new HashSet<String>();

    //@BeforeClass
    public void before() throws Exception {
        startPeerCache();
        createRegions();
        admin = new AdminService(/*"localhost[" + LOCATOR_PORT + "]"*/null);
    }

    //@Test
    public void testGetRegionNames() throws AdminException {
        Map<String, String> regionNames = admin.getRegionNames("all", true);
        assertEquals(new TreeSet<String>(regionNames.keySet()), new TreeSet<String>(expectedRegionNames));
    }

    //@Test
    public void testGetRegionNamesReduced() throws AdminException {
        Map<String, String> regionNames = admin.getRegionNames("region1,region2", true);
        assertEquals(new TreeSet<String>(regionNames.keySet()), new TreeSet<String>(reducedExpectedRegionNames));
    }

    //@Test
    public void testGetRegionNamesReducedWithoutSubRegions() throws AdminException {
        Map<String, String> regionNames = admin.getRegionNames("region1,region2,subregion1OfSubregion3OfRegion3", false);
        assertEquals(new TreeSet<String>(regionNames.keySet()), new TreeSet<String>(Arrays.asList("region1","region2","subregion1OfSubregion3OfRegion3")));
    }

    //@AfterClass
    public void after() throws Exception {
        admin.close();
        cache.close();
    }

    //======================== PRIVATE

    private void startPeerCache() {

        cache = new CacheFactory().set("mcast-port", "0").set("start-locator", "localhost[" + LOCATOR_PORT + "]").create();
        log.info("Cache started successfully ");
    }

    private void createRegions() {
        AttributesFactory attributesFactory = new AttributesFactory();
        //attributesFactory.setScope(Scope.DISTRIBUTED_ACK);
        attributesFactory.setDataPolicy(DataPolicy.PARTITION);
        RegionAttributes regionAttributes1 = attributesFactory.create();

        AttributesFactory attributesFactory2 = new AttributesFactory();
        attributesFactory2.setScope(Scope.GLOBAL);
        attributesFactory2.setDataPolicy(DataPolicy.REPLICATE);
        RegionAttributes regionAttributes2 = attributesFactory2.create();

        AttributesFactory attributesFactory3 = new AttributesFactory();
        attributesFactory3.setScope(Scope.LOCAL);
        RegionAttributes regionAttributes3 = attributesFactory3.create();

        //REGION 1 WITH SUBREGIONS
        Region region1 = cache.createRegionFactory(RegionShortcut.REPLICATE).create("region1");

        Region subregion1OfRegion1 = region1.createSubregion("subregion1OfRegion1", regionAttributes2);
        Region subregion2OfRegion1 = region1.createSubregion("subregion2OfRegion1", regionAttributes2);


        //REGION 2 WITH SUBREGIONS
        Region region2 = cache.createRegionFactory(RegionShortcut.REPLICATE).create("region2");

        Region subregion1OfRegion2 = region2.createSubregion("subregion1OfRegion2", regionAttributes2);

        Region subregion1OfSubregion1OfRegion2 = subregion1OfRegion2.createSubregion("subregion1OfSubregion1OfRegion2", regionAttributes1);


        //REGION 3 WITH SUBREGIONS
        Region region3 = cache.createRegionFactory(RegionShortcut.REPLICATE).create("region3");

        Region subregion1OfRegion3 = region3.createSubregion("subregion1OfRegion3", regionAttributes2);
        Region subregion2OfRegion3 = region3.createSubregion("subregion2OfRegion3", regionAttributes2);
        Region subregion3OfRegion3 = region3.createSubregion("subregion3OfRegion3", regionAttributes2);

        Region subregion1OfSubregion3OfRegion3 = subregion1OfRegion3.createSubregion("subregion1OfSubregion3OfRegion3", regionAttributes2);
        Region subregion2OfSubregion3OfRegion3 = subregion1OfRegion3.createSubregion("subregion2OfSubregion3OfRegion3", regionAttributes2);

        Region subregion1OfSubregion2OfSubregion3OfRegion3 = subregion2OfSubregion3OfRegion3.createSubregion("subregion1OfSubregion2OfSubregion3OfRegion3", regionAttributes3);

        expectedRegionNames.add("region1");
        expectedRegionNames.add("region2");
        expectedRegionNames.add("region3");

        expectedRegionNames.add("/region1/subregion1OfRegion1");
        expectedRegionNames.add("/region1/subregion2OfRegion1");
        expectedRegionNames.add("/region2/subregion1OfRegion2");
        expectedRegionNames.add("/region3/subregion1OfRegion3");
        expectedRegionNames.add("/region3/subregion2OfRegion3");
        expectedRegionNames.add("/region3/subregion3OfRegion3");

        expectedRegionNames.add("/region2/subregion1OfRegion2/subregion1OfSubregion1OfRegion2");
        expectedRegionNames.add("/region3/subregion1OfRegion3/subregion1OfSubregion3OfRegion3");
        expectedRegionNames.add("/region3/subregion1OfRegion3/subregion2OfSubregion3OfRegion3");

        expectedRegionNames.add("/region3/subregion1OfRegion3/subregion2OfSubregion3OfRegion3/subregion1OfSubregion2OfSubregion3OfRegion3");

        ///===
        reducedExpectedRegionNames.add("region1");
        reducedExpectedRegionNames.add("region2");

        reducedExpectedRegionNames.add("/region1/subregion1OfRegion1");
        reducedExpectedRegionNames.add("/region1/subregion2OfRegion1");
        reducedExpectedRegionNames.add("/region2/subregion1OfRegion2");

        reducedExpectedRegionNames.add("/region2/subregion1OfRegion2/subregion1OfSubregion1OfRegion2");

        
    }

}
