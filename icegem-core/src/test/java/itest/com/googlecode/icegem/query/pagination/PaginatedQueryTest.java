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
package itest.com.googlecode.icegem.query.pagination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import itest.com.googlecode.icegem.query.common.model.Person;
import itest.com.googlecode.icegem.query.common.utils.PersonUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.client.ServerOperationException;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.QueryException;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
import com.googlecode.icegem.query.pagination.PageKey;
import com.googlecode.icegem.query.pagination.PaginatedQuery;
import com.googlecode.icegem.utils.CacheUtils;
import com.googlecode.icegem.utils.JavaProcessLauncher;
import com.googlecode.icegem.utils.PropertiesHelper;
import com.googlecode.icegem.utils.ServerTemplate;

/**
 * Tests for paginated query.
 * 
 * @author Andrey Stepanov aka standy
 */
public class PaginatedQueryTest {
    /** Field LOCATOR_PORT */
    private static final int LOCATOR_PORT = 10355;
    
    /** Field cache */
    private static ClientCache cache;
    
    /** Region for querying */
    private static Region data;
    
    /** Help region for storing information about paginated queries */
    private static Region<PageKey, List<Object>> paginatedQueryInfo;
    
    /** Field cacheServer1 */
    private static Process cacheServer1;
    
    /** Field cacheServer2 */
    private static Process cacheServer2;
    
    /** Field javaProcessLauncher */
    private static JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher();
    
    /** */
    private static QueryService queryService;

    @BeforeClass
    public static void setUp() throws IOException, InterruptedException, TimeoutException {
	startCacheServers();
	startClient();
	CacheUtils.clearRegion(data);
    }

    @AfterClass
    public static void tearDown() throws IOException, InterruptedException {
	cache.close();
	stopCacheServers();
    }

    @Before
    public void after() throws InterruptedException, IOException {
	CacheUtils.clearRegion(data);
	CacheUtils.clearRegion(paginatedQueryInfo);
    }

    @Test
    public void testLoadNthPageBySeparateQuery() throws Exception {
	PersonUtils.populateRegionByPersons(data, 100);

	PaginatedQuery<Person> query;

	query = new PaginatedQuery<Person>(queryService, data, "SELECT * FROM /data.keySet", 10);
	assertEquals(10, query.getTotalNumberOfPages());

	query = new PaginatedQuery<Person>(queryService, data, "SELECT * FROM /data.keySet", 10);
	List<Person> result = query.page(7);
    }

    @Test
    public void testCreation() throws FunctionDomainException, QueryInvocationTargetException, TypeMismatchException,
	    NameResolutionException {
	PaginatedQuery query = new PaginatedQuery(queryService, data, "SELECT * FROM /data.keySet");
	assertEquals(query.getPageSize(), PaginatedQuery.DEFAULT_PAGE_SIZE);
    }

    @Test(expected = NullPointerException.class)
    public void testCreationForNotExistingQueryRegion() throws FunctionDomainException, QueryInvocationTargetException,
	    TypeMismatchException, NameResolutionException {
	new PaginatedQuery(queryService, null, "SELECT * FROM /data.keySet");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationWithWrongPageSize() throws FunctionDomainException, QueryInvocationTargetException,
	    TypeMismatchException, NameResolutionException {
	new PaginatedQuery(queryService, data, "SELECT * FROM /data.keySet", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreationWithWrongQueryLimit() throws FunctionDomainException, QueryInvocationTargetException,
	    TypeMismatchException, NameResolutionException {
	int queryLimit = -1;
	new PaginatedQuery(queryService, queryLimit, data, "SELECT * FROM /data.keySet");
    }

    @Test
    public void testGetPageSize() throws FunctionDomainException, QueryInvocationTargetException,
	    TypeMismatchException, NameResolutionException {
	PaginatedQuery query = new PaginatedQuery(queryService, data, "SELECT * FROM /data.keySet");
	assertEquals(query.getPageSize(), PaginatedQuery.DEFAULT_PAGE_SIZE);
	query = new PaginatedQuery(queryService, data, "SELECT * FROM /data.keySet", 10);
	assertEquals(query.getPageSize(), 10);
    }

    @Test
    public void testGetTotalNumberOfEntries() throws QueryException {
	String queryString = "SELECT * FROM /data.keySet";
	PageKey pageKey = new PageKey(CacheUtils.addQueryLimit(queryString, PaginatedQuery.DEFAULT_QUERY_LIMIT),
		new Object[0], PaginatedQuery.DEFAULT_QUERY_LIMIT, PaginatedQuery.DEFAULT_PAGE_SIZE,
		PaginatedQuery.PAGE_NUMBER_FOR_GENERAL_INFO);
	PaginatedQuery query = new PaginatedQuery(queryService, data, queryString);
	int totalNumberOfEntries = query.getTotalNumberOfEntries();
	assertEquals(0, totalNumberOfEntries);

	List<Object> pageKeys = paginatedQueryInfo.get(pageKey);
	assertNotNull(pageKeys);
	assertEquals(pageKeys.get(0), 0);

	CacheUtils.clearRegion(paginatedQueryInfo);
	PersonUtils.populateRegionByPersons(data, 10);

	query = new PaginatedQuery(queryService, data, queryString);
	totalNumberOfEntries = query.getTotalNumberOfEntries();
	assertEquals(totalNumberOfEntries, 10);

	pageKeys = paginatedQueryInfo.get(pageKey);
	assertNotNull(pageKeys);
	assertEquals(totalNumberOfEntries, pageKeys.get(0));
    }

    @Test
    public void testGetTotalNumberOfPages() throws QueryException {
	PaginatedQuery query = new PaginatedQuery(queryService, data, "SELECT * FROM /data.keySet");
	assertEquals(query.getTotalNumberOfPages(), 1);

	PersonUtils.populateRegionByPersons(data, 100);
	query = new PaginatedQuery(queryService, data, "SELECT * FROM /data.keySet", 10);
	assertEquals(query.getTotalNumberOfPages(), 100 / 10);

	CacheUtils.clearRegion(data);
	CacheUtils.clearRegion(paginatedQueryInfo);

	PersonUtils.populateRegionByPersons(data, 101);
	query = new PaginatedQuery(queryService, data, "SELECT * FROM /data.keySet");
	assertEquals(query.getTotalNumberOfPages(), 100 / PaginatedQuery.DEFAULT_PAGE_SIZE + 1);
    }

    @Test
    public void testPageMethodForEmptyResults() throws QueryException {
	String queryString = "SELECT * FROM /data.keySet";
	int pageSize = 20;
	PaginatedQuery<Person> query = new PaginatedQuery<Person>(queryService, (Region<Object, Person>) data,
		queryString, pageSize);

	int pageNumber = 1;
	PageKey pageKey = new PageKey(CacheUtils.addQueryLimit(queryString, PaginatedQuery.DEFAULT_QUERY_LIMIT),
		new Object[] {}, PaginatedQuery.DEFAULT_QUERY_LIMIT, pageSize, pageNumber);

	List<Person> pageEntries = query.page(pageNumber);

	List<Object> pageKeys = paginatedQueryInfo.get(pageKey);
	assertNotNull(pageKeys);
	assertEquals(pageKeys.size(), 0);
	assertEquals(pageEntries.size(), 0);
    }

    @Test
    public void testPageMethodForNotFullPage() throws QueryException {
	int numberOfEntriesForPopulation = 10;
	PersonUtils.populateRegionByPersons(data, numberOfEntriesForPopulation);
	String queryString = "SELECT DISTINCT d.key, d.value.socialNumber FROM /data.entrySet d ORDER BY d.value.socialNumber";
	int pageSize = 20;
	PaginatedQuery<Person> query = new PaginatedQuery<Person>(queryService, data, queryString, pageSize);

	int pageNumber = 1;
	PageKey pageKey = new PageKey(CacheUtils.addQueryLimit(queryString, PaginatedQuery.DEFAULT_QUERY_LIMIT),
		new Object[] {}, PaginatedQuery.DEFAULT_QUERY_LIMIT, pageSize, pageNumber);

	List<Person> pageEntries = query.page(pageNumber);

	List<Object> pageKeys = paginatedQueryInfo.get(pageKey);
	assertNotNull(pageKeys);
	assert pageKeys != null;
	assertEquals(pageKeys.size(), numberOfEntriesForPopulation);
	assertTrue(pageKeys.contains(1));
	assertTrue(pageKeys.contains(10));
	assertEquals(pageKeys.get(0), 1);
	assertEquals(pageKeys.get(9), 10);

	assertEquals(pageEntries.size(), numberOfEntriesForPopulation);
	assertTrue(PersonUtils.containsPersonWithSocialNumber(pageEntries, 1));
	assertTrue(PersonUtils.containsPersonWithSocialNumber(pageEntries, 10));
	assertEquals(pageEntries.get(0).getSocialNumber(), 1);
	assertEquals(pageEntries.get(9).getSocialNumber(), 10);
    }

    @Test
    public void testPageMethod() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 100);
	String queryString = "SELECT DISTINCT d.key, d.value.socialNumber FROM /data.entrySet d ORDER BY d.value.socialNumber";
	int pageSize = 20;
	PaginatedQuery<Person> query = new PaginatedQuery<Person>(queryService, data, queryString, pageSize);

	int pageNumber = 1;
	PageKey pageKey = new PageKey(CacheUtils.addQueryLimit(queryString, PaginatedQuery.DEFAULT_QUERY_LIMIT),
		new Object[] {}, PaginatedQuery.DEFAULT_QUERY_LIMIT, pageSize, pageNumber);

	List<Person> pageEntries = query.page(pageNumber);

	List<Object> pageKeys = paginatedQueryInfo.get(pageKey);
	assertNotNull(pageKeys);
	assert pageKeys != null;
	assertEquals(pageKeys.size(), pageSize);
	assertTrue(pageKeys.contains(1));
	assertTrue(pageKeys.contains(20));
	assertEquals(pageKeys.get(0), 1);
	assertEquals(pageKeys.get(19), 20);

	assertEquals(pageEntries.size(), pageSize);
	assertTrue(PersonUtils.containsPersonWithSocialNumber(pageEntries, 1));
	assertTrue(PersonUtils.containsPersonWithSocialNumber(pageEntries, 20));
	assertEquals(pageEntries.get(0).getSocialNumber(), 1);
	assertEquals(pageEntries.get(19).getSocialNumber(), 20);

	pageNumber = 5;
	pageKey.setPageNumber(pageNumber);
	pageEntries = query.page(pageNumber);

	pageKeys = paginatedQueryInfo.get(pageKey);
	assertNotNull(pageKeys);
	assert pageKeys != null;
	assertEquals(pageKeys.size(), pageSize);
	assertTrue(pageKeys.contains(81));
	assertTrue(pageKeys.contains(100));
	assertEquals(pageKeys.get(0), 81);
	assertEquals(pageKeys.get(19), 100);

	assertEquals(pageEntries.size(), pageSize);
	assertTrue(PersonUtils.containsPersonWithSocialNumber(pageEntries, 81));
	assertTrue(PersonUtils.containsPersonWithSocialNumber(pageEntries, 100));
	assertEquals(pageEntries.get(0).getSocialNumber(), 81);
	assertEquals(pageEntries.get(19).getSocialNumber(), 100);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNotExistedPage() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 10);
	String queryString = "SELECT * FROM /data.keySet";
	PaginatedQuery query = new PaginatedQuery(queryService, data, queryString);

	int pageNumber = 2;
	PageKey pageKey = new PageKey(queryString, new Object[] {}, PaginatedQuery.DEFAULT_QUERY_LIMIT,
		PaginatedQuery.DEFAULT_PAGE_SIZE, pageNumber);

	query.page(pageNumber);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQueryOrderByWithoutKeyInProjection() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 100);
	PaginatedQuery query = new PaginatedQuery(queryService, data,
		"SELECT DISTINCT e.value.socialNumber, e.value.socialNumber FROM /data.entrySet e ORDER BY e.value.socialNumber");
	query.page(1);
    }

    @Test(expected = ServerOperationException.class)
    public void testQueryOrderByWithoutOrderByFieldInProjection() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 100);
	PaginatedQuery query = new PaginatedQuery(queryService, data,
		"SELECT DISTINCT e.key FROM /data.entrySet e ORDER BY e.value.socialNumber");
	query.page(1);
    }

    @Test
    public void testPaginatedComplexQuering() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 100);
	PaginatedQuery query = new PaginatedQuery(
		queryService,
		data,
		"SELECT DISTINCT e.key, e.value.socialNumber FROM /data.entrySet e WHERE e.value.socialNumber = $1 ORDER BY e.value.socialNumber",
		new Object[] { 1 });

	List pageEntries = query.page(1);

	assertEquals(pageEntries.size(), 1);
	assertTrue(PersonUtils.containsPersonWithSocialNumber(pageEntries, 1));
    }

    @Test
    public void testQueryLimit() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 100);
	int queryLimit = 50;
	PaginatedQuery<Person> query = new PaginatedQuery<Person>(queryService, queryLimit, data,
		"SELECT * FROM /data.keySet");

	List<Person> results = query.page(1);
	assertEquals(query.getTotalNumberOfPages(), 3);
	assertEquals(query.getTotalNumberOfEntries(), queryLimit);
	assertEquals(query.isLimitExceeded(), true);
    }

    @Test
    public void testInitialQueryLimitThatLowerThanPaginatedQueryLimit() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 100);
	int queryLimit = 50;
	int initialLimit = 10;
	PaginatedQuery<Person> query = new PaginatedQuery<Person>(queryService, queryLimit, data,
		"SELECT * FROM /data.keySet limit " + initialLimit);
	List<Person> results = query.page(1);
	assertEquals(query.getTotalNumberOfPages(), 1);
	assertEquals(results.size(), initialLimit);
	assertEquals(query.isLimitExceeded(), false);

	query = new PaginatedQuery<Person>(queryService, queryLimit, data, "SELECT * FROM /data.keySet LIMIT "
		+ initialLimit);
	results = query.page(1);
	assertEquals(query.getTotalNumberOfPages(), 1);
	assertEquals(results.size(), initialLimit);
	assertEquals(query.isLimitExceeded(), false);
    }

    @Test
    public void testInitialQueryLimitThatHigherThanPaginatedQueryLimit() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 100);
	int queryLimit = 60;
	int initialLimit = 70;
	PaginatedQuery<Person> query = new PaginatedQuery<Person>(queryService, queryLimit, data,
		"SELECT * FROM /data.keySet limit " + initialLimit);
	List<Person> results = query.page(1);
	assertEquals(query.getTotalNumberOfPages(), 3);
	assertEquals(query.getTotalNumberOfEntries(), queryLimit);
	assertEquals(query.isLimitExceeded(), true);
    }

    @Test
    public void testInitialQueryLimitThatEqualToPaginatedQueryLimit() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 100);
	int queryLimit = 10;
	int initialLimit = 10;
	PaginatedQuery<Person> query = new PaginatedQuery<Person>(queryService, queryLimit, data,
		"SELECT * FROM /data.keySet limit " + initialLimit);
	List<Person> results = query.page(1);
	assertEquals(query.getTotalNumberOfPages(), 1);
	assertEquals(results.size(), initialLimit);
    }

    @Test
    public void testInitialQueryLimitThatEqualToPaginatedQueryLimitForSmallSizeOfEntries() throws QueryException {
	PersonUtils.populateRegionByPersons(data, 5);
	int queryLimit = 6;
	int initialLimit = 10;
	PaginatedQuery<Person> query = new PaginatedQuery<Person>(queryService, queryLimit, data,
		"SELECT * FROM /data.keySet limit " + initialLimit);
	List<Person> results = query.page(1);
	assertEquals(query.getTotalNumberOfPages(), 1);
	assertEquals(results.size(), 5);
    }

    @Test
    public void testExpiredPageLoad() throws Exception {
	PersonUtils.populateRegionByPersons(data, 100);
	PaginatedQuery query;
	query = new PaginatedQuery(queryService, data,
		"SELECT DISTINCT e.key, e.value.socialNumber FROM /data.entrySet e ORDER BY e.value.socialNumber");

	List pageEntries = query.page(1);
	assertEquals(pageEntries.size(), 20);
	assertTrue(PersonUtils.containsPersonWithSocialNumber(pageEntries, 1));

	Thread.sleep(7000);

	query = new PaginatedQuery(queryService, data,
		"SELECT DISTINCT e.key, e.value.socialNumber FROM /data.entrySet e ORDER BY e.value.socialNumber");

	List refreshedResults = query.page(1);
	assertEquals(pageEntries, refreshedResults);
    }

    /**
     * Starts a client.
     * 
     * @throws java.io.IOException
     */
    private static void startClient() throws IOException {
	ClientCacheFactory clientCacheFactory = new ClientCacheFactory().addPoolLocator("localhost", LOCATOR_PORT);

	PropertiesHelper properties = new PropertiesHelper("/paginatedQueryServerProperties.properties");

	cache = clientCacheFactory.set("log-level", properties.getStringProperty("log-level"))
		.set("license-file", properties.getStringProperty("license-file"))
		.set("license-type", properties.getStringProperty("license-type")).create();

	ClientRegionFactory<Object, Object> regionFactory = cache.createClientRegionFactory(ClientRegionShortcut.PROXY);
	data = regionFactory.create("data");

	queryService = cache.getQueryService();

	ClientRegionFactory<PageKey, List<Object>> regionFactoryForHelpRegion = cache
		.createClientRegionFactory(ClientRegionShortcut.PROXY);
	paginatedQueryInfo = regionFactoryForHelpRegion.create(PaginatedQuery.PAGINATED_QUERY_INFO_REGION_NAME);
    }

    /**
     * Starts two cache servers for tests.
     * 
     * @throws IOException
     *             when
     * @throws InterruptedException
     *             when
     */
    private static void startCacheServers() throws IOException, InterruptedException {
	cacheServer1 = javaProcessLauncher.runWithConfirmation("",
		ServerTemplate.class, new String[] { "-DgemfirePropertyFile=paginatedQueryServerProperties.properties" }, null);
	cacheServer2 = javaProcessLauncher.runWithConfirmation("",
		ServerTemplate.class, new String[] { "-DgemfirePropertyFile=paginatedQueryServerProperties.properties" }, null);
    }

    /**
     * Stops cache servers.
     * 
     * @throws IOException
     *             when
     * @throws InterruptedException
     */
    private static void stopCacheServers() throws IOException, InterruptedException {
	javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer1);
	javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer2);
    }
}
