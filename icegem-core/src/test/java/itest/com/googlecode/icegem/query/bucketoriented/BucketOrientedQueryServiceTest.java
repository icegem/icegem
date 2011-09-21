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
package itest.com.googlecode.icegem.query.bucketoriented;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import itest.com.googlecode.icegem.query.common.model.Person;
import itest.com.googlecode.icegem.query.common.utils.PersonUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.query.QueryException;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.Struct;
import com.googlecode.icegem.query.bucketoriented.BucketOrientedQueryService;
import com.googlecode.icegem.utils.JavaProcessLauncher;
import com.googlecode.icegem.utils.PropertiesHelper;
import com.googlecode.icegem.utils.ServerTemplate;

/**
 * Tests for bucket oriented query service.
 *
 * @author Andrey Stepanov aka standy
 */
public class BucketOrientedQueryServiceTest {
    /** GemFire properties file. */
    private static final String PROPERTY_FILE = "bucketOrientedServerProperties.properties";

    /** Locator port. */
    private static final int LOCATOR_PORT = 10355;

    /** Client cache. */
    private static ClientCache cache;

    /** Test cache region. */
    private static Region<Object, Object> data;
    
    /** Cache server 1. */
    private static Process cacheServer1;

    /** Cache server 2. */
    private static Process cacheServer2;

    /** Process launcher. */
    private static JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher(true, true, true);

    /**
     * @throws Exception If failed to prepare test.
     */
    @BeforeClass
    public static void setUp() throws Exception {
	startCacheServers();

	startClient();

	PersonUtils.populateRegionByPersons(data, 100);
    }

    /**
     * @throws Exception If failed to cleanup test.
     */
    @AfterClass
    public static void tearDown() throws Exception {
	cache.close();

	stopCacheServers();
    }

    /**
     * JUnit.
     * 
     * @throws QueryException If failed.
     */
    @Test
    public void testBucketDataRetrieveForExistedAndFakeKeys() throws QueryException {
	// Existing key.
	SelectResults<Object> resultsBasedOnExistedKey = BucketOrientedQueryService.executeOnBuckets(
		"SELECT * FROM /data", data, new HashSet<Object>(Arrays.asList(1)));

	checkResults(resultsBasedOnExistedKey, 10, new int[] { 1, 11 }, new int[] { 2 });

	// Fake key.
	SelectResults<Object> resultsBasedOnFakeKey = BucketOrientedQueryService.executeOnBuckets(
		"SELECT * FROM /data", data, new HashSet<Object>(Arrays.asList(101)));

	checkResults(resultsBasedOnFakeKey, 10, new int[] { 1 }, new int[] { 101, 2 });

	assertFalse(resultsBasedOnFakeKey.equals(resultsBasedOnExistedKey));
    }

    /**
     * @param results Query results to check.
     * @param size Expected size of the given results.
     * @param exist Social numbers for which persons should exist in the results.
     * @param notExist Social numbers for which persons should not exist in the results.
     */
    protected void checkResults(SelectResults<Object> results, int size, int[] exist, int[] notExist) {
	assertEquals(size, results.size());

	for (int i = 0; i < exist.length; i++) {
	    assertTrue(PersonUtils.containsPersonWithSocialNumber(results.asList(), exist[i]));
	}

	for (int i = 0; i < notExist.length; i++) {
	    assertFalse(PersonUtils.containsPersonWithSocialNumber(results.asList(), notExist[i]));
	}
    }

    /**
     * JUnit.
     * 
     * @throws QueryException If failed.
     */
    @Test
    public void testBucketsDataRetrieve() throws QueryException, InterruptedException {
	// One bucket.
	SelectResults<Object> resultsFromOneBucket = BucketOrientedQueryService.executeOnBuckets("SELECT * FROM /data",
		data, new HashSet<Object>(Arrays.asList(1)));

	checkResults(resultsFromOneBucket, 10, new int[] { 1 }, new int[] { 2 });

	// Two buckets.
	SelectResults<Object> resultsFromTwoBuckets = BucketOrientedQueryService.executeOnBuckets(
		"SELECT * FROM /data", data, new HashSet<Object>(Arrays.asList(1, 2)));

	checkResults(resultsFromTwoBuckets, 20, new int[] { 1, 2 }, new int[0]);

	resultsFromTwoBuckets = BucketOrientedQueryService.executeOnBuckets("SELECT * FROM /data", data,
		new HashSet<Object>(Arrays.asList(1, 11, 2)));

	checkResults(resultsFromTwoBuckets, 20, new int[] { 1, 2, 11 }, new int[0]);
    }

    /**
     * JUnit.
     * 
     * @throws QueryException If failed.
     */
    @Test
    public void testBucketDataRetrieveUsingQueryLimit() throws QueryException {
	// One bucket.
	SelectResults<Object> resultsFromOneBucket = BucketOrientedQueryService.executeOnBuckets(
		"SELECT * FROM /data limit 3", data, new HashSet<Object>(Arrays.asList(1)));

	checkResults(resultsFromOneBucket, 3, new int[0], new int[0]);

	// Two buckets.
	SelectResults<Object> resultsFromTwoBuckets = BucketOrientedQueryService.executeOnBuckets(
		"SELECT * FROM /data limit 3", data, new HashSet<Object>(Arrays.asList(1, 2)));

	checkResults(resultsFromTwoBuckets, 3, new int[0], new int[0]);

	resultsFromTwoBuckets = BucketOrientedQueryService.executeOnBuckets("SELECT * FROM /data LIMIT 21", data,
		new HashSet<Object>(Arrays.asList(1, 2)));
	
	checkResults(resultsFromTwoBuckets, 20, new int[0], new int[0]);
    }

    /**
     * JUnit.
     * 
     * @throws QueryException If failed.
     */
    @Test
    public void testComplexQuering() throws QueryException {
	SelectResults<Object> results = BucketOrientedQueryService.executeOnBuckets(
		"SELECT * FROM /data WHERE socialNumber = $1", new Object[] { 1 }, data,
		new HashSet<Object>(Arrays.asList(1)));

	assertEquals(results.size(), 1);

	Person person = (Person) data.get(1);

	assertEquals(results.asList().get(0).equals(person), true);
	assertEquals(results.getCollectionType().getElementType().resolveClass(), Object.class);

	results = BucketOrientedQueryService.executeOnBuckets("SELECT children FROM /data WHERE socialNumber = $1",
		new Object[] { 1 }, data, new HashSet<Object>(Arrays.asList(1)));

	assertEquals(results.size(), 1);
	assertEquals(results.asList().get(0).equals(person.getChildren()), true);
	assertEquals(results.getCollectionType().getElementType().resolveClass(), Object.class);

	results = BucketOrientedQueryService.executeOnBuckets("SELECT socialNumber FROM /data WHERE socialNumber = $1",
		new Object[] { 1 }, data, new HashSet<Object>(Arrays.asList(1)));

	assertEquals(results.size(), 1);
	assertEquals(results.asList().get(0).equals(1), true);
	assertEquals(results.getCollectionType().getElementType().resolveClass(), Object.class);

	results = BucketOrientedQueryService.executeOnBuckets(
		"SELECT socialNumber, children FROM /data WHERE socialNumber = $1", new Object[] { 1 }, data,
		new HashSet<Object>(Arrays.asList(1)));

	assertEquals(results.size(), 1);
	assertEquals(results.getCollectionType().getElementType().resolveClass(), Struct.class);

	results = BucketOrientedQueryService.executeOnBuckets(
		"SELECT socialNumber, children FROM /data WHERE socialNumber = $1", new Object[] { 1 }, data,
		new HashSet<Object>(Arrays.asList(2)));

	assertEquals(results.size(), 0);
	assertEquals(results.getCollectionType().getElementType().resolveClass(), Struct.class);
    }

    /**
     * JUnit.
     * 
     * @throws QueryException If succeeded.
     */
    @Test(expected = QueryException.class)
    public void testExecutionWithEmptyQueryString() throws QueryException {
	BucketOrientedQueryService.executeOnBuckets("", data, new HashSet<Object>(Arrays.asList(1)));
    }

    /**
     * JUnit.
     * 
     * @throws QueryException If succeeded.
     */
    @Test(expected = QueryException.class)
    public void testExecutionWithWrongQueryString() throws QueryException {
	BucketOrientedQueryService.executeOnBuckets("SELECT *", data, new HashSet<Object>(Arrays.asList(1)));
    }

    /**
     * JUnit.
     * 
     * @throws QueryException If succeeded.
     */
    @Test(expected = QueryException.class)
    public void testExecutionWithNullQueryString() throws QueryException {
	BucketOrientedQueryService.executeOnBuckets(null, data, new HashSet<Object>(Arrays.asList(1)));
    }

    /**
     * JUnit.
     * 
     * @throws QueryException If succeeded.
     */
    @Test(expected = QueryException.class)
    public void testExecutionWithNotExistedRegionQueryString() throws QueryException {
	BucketOrientedQueryService
		.executeOnBuckets("SELECT * FROM /data1", data, new HashSet<Object>(Arrays.asList(1)));
    }

    /**
     * Starts a client.
     * 
     * @throws java.io.IOException
     */
    private static void startClient() throws IOException {
	ClientCacheFactory clientCacheFactory = new ClientCacheFactory().addPoolLocator("localhost", LOCATOR_PORT);

	PropertiesHelper properties = new PropertiesHelper("/" + PROPERTY_FILE);

	cache = clientCacheFactory.set("log-level", properties.getStringProperty("log-level"))
		.set("license-file", properties.getStringProperty("license-file"))
		.set("license-type", properties.getStringProperty("license-type")).create();

	ClientRegionFactory<Object, Object> regionFactory = cache.createClientRegionFactory(ClientRegionShortcut.PROXY);

	data = regionFactory.create("data");
    }

    /**
     * Starts two cache servers for tests.
     *
     * @throws IOException when
     * @throws InterruptedException when
     */
    private static void startCacheServers() throws IOException, InterruptedException {
	String[] javaArgs = new String[] { "-DgemfirePropertyFile=" + PROPERTY_FILE };

	cacheServer1 = javaProcessLauncher.runWithConfirmation(ServerTemplate.class, javaArgs, null);
	cacheServer2 = javaProcessLauncher.runWithConfirmation(ServerTemplate.class, javaArgs, null);
    }

    /**
     * Stops cache servers.
     *
     * @throws Exception If failed to stop servers.
     */
    private static void stopCacheServers() throws Exception {
	javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer1);
	javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer2);
    }
}
