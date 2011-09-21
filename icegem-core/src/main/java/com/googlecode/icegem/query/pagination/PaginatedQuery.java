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
package com.googlecode.icegem.query.pagination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Collections;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.query.FunctionDomainException;
import com.gemstone.gemfire.cache.query.NameResolutionException;
import com.gemstone.gemfire.cache.query.Query;
import com.gemstone.gemfire.cache.query.QueryException;
import com.gemstone.gemfire.cache.query.QueryInvocationTargetException;
import com.gemstone.gemfire.cache.query.QueryService;
import com.gemstone.gemfire.cache.query.RegionNotFoundException;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.Struct;
import com.gemstone.gemfire.cache.query.TypeMismatchException;
import com.googlecode.icegem.utils.CacheUtils;

/**
 * This component allows to execute paginated queries both from client and
 * peer/server sides. It caches paginated query results in a help region and
 * allows to iterate on them using paginated query API.
 * 
 * See http://code.google.com/p/icegem/wiki/Documentation#Paginated_Query for
 * more details and examples.
 * 
 * RESTRICTIONS: 1). A query string for paginated query can be arbitrarily
 * complex but entry key must be part of projection list. 2). For partitioned
 * regions a query string must meet the requirements described in a GemFire
 * documentation for querying partitioned regions.
 * 
 * ORDER BY on Partitioned Regions A paginated query supports order by
 * functionality on partitioned regions. The fields specified in the order by
 * clause must be part of the projection list.
 * 
 * Limiting of results: Paginated query result can be limited. By default this
 * limit is 1000 entries. You can specify a custom limit value via paginated
 * query constructors argument. If query results exceeds this limit: - only a
 * specified limit number of entries will be cached and returned; - flag
 * 'limitExceeded' will be set to 'true'.
 * 
 * Examples:
 * 
 * SELECT * FROM /data.keySet; SELECT DISTINCT d.key, d.value.field1 FROM
 * /data.entrySet d ORDER BY d.value.field1;
 * 
 * @author Andrey Stepanov aka standy
 */
public class PaginatedQuery<V> {
	/** page size by default */
	public static final int DEFAULT_PAGE_SIZE = 20;
	/** Default limit on query result */
	public static final int DEFAULT_QUERY_LIMIT = 1000;
	/**
	 * number of page that will be store general information about paginated
	 * query (e.g. total number of query entries)
	 */
	public static final int PAGE_NUMBER_FOR_GENERAL_INFO = -1;
	/** name of a help region for storing information about paginated queries */
	public static final String PAGINATED_QUERY_INFO_REGION_NAME = "paginated_query_info";
	/** Field logger */
	private static final Logger logger = LoggerFactory
			.getLogger(PaginatedQuery.class);

	/** Field queryService */
	private QueryService queryService;
	/** region for querying */
	private Region<Object, V> queryRegion;
	/** help region for storing information about paginated queries */
	private Region<PageKey, List<Object>> paginatedQueryInfoRegion;

	/** Field currentPageNumber */
	private int pageSize;
	/** Field queryString */
    private String queryString;
	/** Field queryParams */
    private Object[] queryParams;
	/** limit on query result */
	private int queryLimit;
    /** flag that indicates that information has been loaded */
	private boolean infoLoaded;
	/** flag that indicates that limit has been exceeded */
	private boolean limitExceeded;
	/** Field totalNumberOfEntries */
	private int totalNumberOfEntries;

	/**
	 * Creates a new PaginatedQuery instance.
	 * 
	 * @param queryService
	 *            The service to run the query.
	 * @param queryLimit
	 *            limit on query result
	 * @param region
	 *            The region for querying.
	 * @param queryString
	 *            query string that must return entry keys
	 * @throws RegionNotFoundException
	 *             when query region or help region were not founded
	 */
	public PaginatedQuery(QueryService queryService, int queryLimit,
			Region<Object, V> region, String queryString)
			throws RegionNotFoundException {
		this(queryService, queryLimit, region, queryString, DEFAULT_PAGE_SIZE);
	}

	/**
	 * Creates a new PaginatedQuery instance.
	 * 
	 * @param queryService
	 *            The service to run the query.
	 * @param queryLimit
	 *            limit on query result
	 * @param region
	 *            The region for querying.
	 * @param queryString
	 *            query string that must return entry keys
	 * @param pageSize
	 *            size of page
	 * @throws RegionNotFoundException
	 *             when query region or help region were not founded
	 */
	public PaginatedQuery(QueryService queryService, int queryLimit,
			Region<Object, V> region, String queryString, int pageSize)
			throws RegionNotFoundException {
		this(queryService, queryLimit, region, queryString, new Object[] {},
				pageSize);
	}

	/**
	 * Creates a new PaginatedQuery instance.
	 * 
	 * @param queryService
	 *            The service to run the query.
	 * @param queryLimit
	 *            limit on query result
	 * @param region
	 *            The region for querying.
	 * @param queryString
	 *            query string that must return entry keys
	 * @param queryParameters
	 *            parameters for query execution
	 * @throws RegionNotFoundException
	 *             when query region or help region were not founded
	 */
	public PaginatedQuery(QueryService queryService, int queryLimit,
			Region<Object, V> region, String queryString,
			Object[] queryParameters) throws RegionNotFoundException {
		this(queryService, queryLimit, region, queryString, queryParameters,
				DEFAULT_PAGE_SIZE);
	}

	/**
	 * Creates a new PaginatedQuery instance.
	 * 
	 * @param queryService
	 *            The service to run the query.
	 * @param queryLimit
	 *            limit on query result
	 * @param region
	 *            The region for querying.
	 * @param queryString
	 *            query string that must return entry keys
	 * @param queryParameters
	 *            parameters for query execution
	 * @param pageSize
	 *            size of page
	 * @throws RegionNotFoundException
	 *             when query region or help region were not founded
	 */
	public PaginatedQuery(QueryService queryService, int queryLimit,
			Region<Object, V> region, String queryString,
			Object[] queryParameters, int pageSize)
			throws RegionNotFoundException {
		this.queryService = queryService;

		this.queryRegion = region;
		if (queryRegion == null) {
			throw new NullPointerException("Query region have to be provided");
		}

		RegionService regionService = queryRegion.getRegionService();
		paginatedQueryInfoRegion = regionService
				.getRegion(PAGINATED_QUERY_INFO_REGION_NAME);

		if (paginatedQueryInfoRegion == null) {
			RegionNotFoundException e = new RegionNotFoundException(
					"Help region ["
							+ PAGINATED_QUERY_INFO_REGION_NAME
							+ "] for storing "
							+ "information about paginated queries has not been found");
			logger.warn(e.getMessage());
			throw e;
		}

		if (pageSize < 5) {
			throw new IllegalArgumentException(
					"Page size must be greater than 4");
		}
		this.pageSize = pageSize;

		if (queryLimit < 1) {
			throw new IllegalArgumentException("Query limit must be positive");
		}
		this.queryLimit = queryLimit;

		this.queryString = CacheUtils.addQueryLimit(queryString,
				this.queryLimit);
		this.queryParams = queryParameters;
	}

	/**
	 * Creates a new PaginatedQuery instance.
	 * 
	 * @param queryService
	 *            The service to run the query.
	 * @param region
	 *            The region for querying
	 * @param queryString
	 *            query string that must return entry keys
	 * @throws RegionNotFoundException
	 *             when query region or help region were not founded
	 */
	public PaginatedQuery(QueryService queryService, Region<Object, V> region,
			String queryString) throws RegionNotFoundException {
		this(queryService, DEFAULT_QUERY_LIMIT, region, queryString,
				DEFAULT_PAGE_SIZE);
	}

	/**
	 * Creates a new PaginatedQuery instance.
	 * 
	 * @param queryService
	 *            The service to run the query.
	 * @param region
	 *            The region for querying.
	 * @param queryString
	 *            query string that must return entry keys
	 * @param pageSize
	 *            size of page
	 * @throws RegionNotFoundException
	 *             when query region or help region were not founded
	 */
	public PaginatedQuery(QueryService queryService,
			Region<Object, V> region, String queryString, int pageSize)
			throws RegionNotFoundException {
		this(queryService, DEFAULT_QUERY_LIMIT, region, queryString,
				new Object[] {}, pageSize);
	}

	/**
	 * Creates a new PaginatedQuery instance.
	 * 
	 * @param queryService
	 *            The service to run the query.
	 * @param region
	 *            The region for querying.
	 * @param queryString
	 *            query string that must return entry keys
	 * @param queryParameters
	 *            parameters for query execution
	 * @throws RegionNotFoundException
	 *             when query region or help region were not founded
	 */
	public PaginatedQuery(QueryService queryService, Region<Object, V> region,
			String queryString, Object[] queryParameters)
			throws RegionNotFoundException {
		this(queryService, DEFAULT_QUERY_LIMIT, region, queryString,
				queryParameters, DEFAULT_PAGE_SIZE);
	}

	/**
	 * Creates a new PaginatedQuery instance.
	 * 
	 * @param queryService
	 *            The service to run the query.
	 * @param region
	 *            The region for querying.
	 * @param queryString
	 *            query string that must return entry keys
	 * @param queryParameters
	 *            parameters for query execution
	 * @param pageSize
	 *            size of page
	 * @throws RegionNotFoundException
	 *             when query region or help region were not founded
	 */
	public PaginatedQuery(QueryService queryService, Region<Object, V> region,
			String queryString, Object[] queryParameters, int pageSize)
			throws RegionNotFoundException {
		this(queryService, DEFAULT_QUERY_LIMIT, region, queryString,
				queryParameters, pageSize);
	}

	/**
	 * Returns size of page.
	 * 
	 * @return page size
	 */
	public int getPageSize() {
		return this.pageSize;
	}

	/**
	 * Returns a total number of query entries.
	 * 
	 * @return total number of entries
	 * @throws com.gemstone.gemfire.cache.query.QueryException
	 *             during query execution
	 */
	public int getTotalNumberOfEntries() throws QueryException {
		prepareResultData(false);
		return totalNumberOfEntries;
	}

	/**
	 * Returns a total number of query pages.
	 * 
	 * @return total number of pages
	 * @throws com.gemstone.gemfire.cache.query.QueryException
	 *             during query execution
	 */
	public int getTotalNumberOfPages() throws QueryException {
		prepareResultData(false);
		if (isEmpty()) {
			return 1;
		}
		int total = totalNumberOfEntries / this.pageSize;
		if (totalNumberOfEntries % this.pageSize > 0) {
			total += 1;
		}
		return total;
	}

	/**
	 * Gets value of a flag that indicates excess of query limit.
	 * 
	 * @return boolean
	 * @throws com.gemstone.gemfire.cache.query.QueryException
	 *             during query execution
	 */
	public boolean isLimitExceeded() throws QueryException {
		prepareResultData(false);
		return limitExceeded;
	}

	/**
	 * Returns entries for a specified page number. Use getTotalNumberOfPages()
	 * method to know how many pages this query has.
	 * 
	 * @param pageNumber
	 *            number of page to return
	 * @throws com.gemstone.gemfire.cache.query.QueryException
	 *             during query execution
	 * 
	 * @return List<V> list of entries
	 */
	public List<V> page(int pageNumber) throws QueryException {
		List<Object> pageKeys = null;
		boolean firstTry = true;

		while (pageKeys == null) {
			prepareResultData(!firstTry);

			if (!pageExists(pageNumber)) {
				throw new IndexOutOfBoundsException("The page " + pageNumber
						+ "does not exists. " + +getTotalNumberOfPages()
						+ " pages available.");

			}

			PageKey pageKey = newKey(pageNumber);
			pageKeys = paginatedQueryInfoRegion.get(pageKey);

			if (pageKeys == null && firstTry) {
				firstTry = false;
			} else {
				break;
			}
		}

		if (pageKeys != null) {
			return getValues(pageKeys);
		} else {
			throw new RuntimeException(
					"Unable to load keys from cache. Too aggressive expiration policy?");
		}
	}

	/**
	 * Checks that a specified page number exists.
	 * 
	 * @param pageNumber
	 *            of type int
	 * @return boolean
	 * @throws com.gemstone.gemfire.cache.query.QueryException
	 *             during query execution
	 */
	public boolean pageExists(int pageNumber) throws QueryException {
		return pageNumber == 1
				|| !(pageNumber < 1 || pageNumber > getTotalNumberOfPages());
	}

	protected void storePage(int pageNumber, List<Object> page) {
		PageKey pageKey = newKey(pageNumber);
		paginatedQueryInfoRegion.put(pageKey, page);
	}

	private List<Object> extractKeys(SelectResults<Object> results) {
		List<Object> keys = new ArrayList<Object>(results.size());
		if (results.getCollectionType().getElementType().isStructType()) {
			for (Object result : results) {
				Object key;
				try {
					key = ((Struct) result).get("key");
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException(
							e.getMessage()
									+ " (hint: maybe you forgot to include entry key into query projection list)");
				}
				keys.add(key);
			}
		} else {
			keys = results.asList();
		}
		return keys;
	}

	/**
	 * Returns values for given keys.
	 * 
	 * @param entriesKeysForPage
	 *            of type List<Object>
	 * @return List<V>
	 */
	@SuppressWarnings("unchecked")
	private List<V> getValues(List<Object> entriesKeysForPage) {
		if (entriesKeysForPage.isEmpty()) {
			return Collections.emptyList();
		}
		Map<Object, V> entriesMap = queryRegion.getAll(entriesKeysForPage);

		List<V> entries = new ArrayList<V>(entriesKeysForPage.size());
		for (Object key : entriesKeysForPage) {
			entries.add(entriesMap.get(key));
		}
		return entries;
	}

	/**
	 * Handles throwable exceptions during query execution and replaces them by
	 * checked exception.
	 * 
	 * @param e
	 *            of type Throwable
	 * @throws com.gemstone.gemfire.cache.query.QueryException
	 *             checked exception
	 */
	private void handleException(Exception e) throws QueryException {
		throw new QueryException(
				"Exception has been thrown during query execution. "
						+ "Cause exception message: " + e.getMessage(), e);
	}

	/**
	 * Checks that query doesn't have results.
	 * 
	 * @return the empty (type boolean) of this PaginatedQuery object.
	 */
	private boolean isEmpty() {
		return totalNumberOfEntries == 0;
	}

	private PageKey newKey(int pageNumber) {
		return new PageKey(this.queryString, this.queryParams, this.queryLimit,
				this.pageSize, pageNumber);
	}

	/**
	 * Stores paginated query info if it has not been stored yet.
	 * 
	 * @param force
	 * 
	 * @throws com.gemstone.gemfire.cache.query.QueryException
	 *             during query execution
	 */
	@SuppressWarnings({ "unchecked" })
	private void prepareResultData(boolean force) throws QueryException {
		if (this.infoLoaded && !force) {
			return;
		}

		PageKey pageKey = newKey(PAGE_NUMBER_FOR_GENERAL_INFO);

		List<Object> queryInfo = null;
		if (!force) {
			queryInfo = paginatedQueryInfoRegion.get(pageKey);
		}

		if (queryInfo == null) {
			Query query = queryService.newQuery(this.queryString);
			SelectResults<Object> results = null;
			try {
				results = (SelectResults<Object>) query.execute(pageKey
						.getQueryParameters());
			} catch (FunctionDomainException e) {
				handleException(e);
			} catch (TypeMismatchException e) {
				handleException(e);
			} catch (NameResolutionException e) {
				handleException(e);
			} catch (QueryInvocationTargetException e) {
				handleException(e);
			}

			if (results.size() > queryLimit) {
				this.limitExceeded = true;
				this.totalNumberOfEntries = queryLimit;
				String msg = "Size of query results has exceeded limit ("
						+ queryLimit + "). Truncated.";
				logger.warn(msg);
			} else {
				limitExceeded = false;
				this.totalNumberOfEntries = results.size();
			}

			queryInfo = Arrays.asList(new Object[] { results.size(),
					limitExceeded });
			storePage(PAGE_NUMBER_FOR_GENERAL_INFO, queryInfo);

			List<Object> keys = extractKeys(results);
			storeResults(keys);
		} else {
			this.totalNumberOfEntries = (Integer) queryInfo.get(0);
			this.limitExceeded = (Boolean) queryInfo.get(1);
		}
		this.infoLoaded = true;
	}

	/**
	 * Stores paginated query pages and general info.
	 * 
	 * @param resultKeys
	 *            of type List<Object>
	 */
	private void storeResults(List<Object> resultKeys) {
		if (resultKeys.size() > queryLimit) {
			resultKeys = resultKeys.subList(0, queryLimit);
		}

		int keyNumber = 0;
		int pageNumber = 0;
		List<Object> page = new ArrayList<Object>();

		for (Object key : resultKeys) {
			if (keyNumber % getPageSize() == 0 && keyNumber != 0) {
				storePage(++pageNumber, page);
				page.clear();
			}
			page.add(key);
			keyNumber++;
		}

		if (page.size() > 0 || pageNumber == 0) {
			storePage(++pageNumber, page);
		}
	}
}
