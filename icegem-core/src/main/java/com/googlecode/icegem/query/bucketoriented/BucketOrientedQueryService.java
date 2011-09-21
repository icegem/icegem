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
package com.googlecode.icegem.query.bucketoriented;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.query.QueryException;
import com.gemstone.gemfire.cache.query.SelectResults;
import com.gemstone.gemfire.cache.query.internal.ResultsCollectionWrapper;
import com.gemstone.gemfire.cache.query.types.ObjectType;

/**
 * Query service that allows to execute OQL queries on a specified set of buckets.
 * This service can be used both on client and server/peer sides.
 * <p>
 * Note: this service works only on partition regions.
 * 
 * @author Andrey Stepanov aka standy
 */
public class BucketOrientedQueryService {
    /** Field logger. */
    private static Logger logger = LoggerFactory.getLogger(BucketOrientedQueryService.class);

    /**
     * Executes a particular query on specified region using a set of keys that represents buckets. The set of buckets is
     * determined by keys of entries that are stored in such buckets: - real and fake keys can be used (such key should
     * have the same routing object as bucket's keys have); - it will be enough to specify one key for each bucket. Work
     * of this method is based on execution of function.
     * 
     * @see BucketOrientedQueryFunction
     * @param queryString OQL query string.
     * @param region Partitioned region on which query will be executed.
     * @param keys Set of keys that specify buckets.
     * @return Query results as instance of {@link SelectResults}.
     * @throws QueryException When in case of query execution failure.
     */
    @SuppressWarnings({ "unchecked" })
    public static SelectResults<Object> executeOnBuckets(String queryString, Region region, Set<Object> keys)
	    throws QueryException {
	return executeOnBuckets(queryString, null, region, keys);
    }

    /**
     * Executes a particular query with parameters on specified region using a set of keys that represents buckets. The
     * set of buckets is determined by keys of entries that are stored in such buckets: - real and fake keys can be used
     * (such key should have the same routing object as bucket's keys have); - it will be enough to specify one key for
     * each bucket. Work of this method is based on execution of function.
     * 
     * @see BucketOrientedQueryFunction
     * @param queryString OQL query string.
     * @param queryParameters Query parameters.
     * @param region Partitioned region on which query will be executed.
     * @param keys Set of keys that specify buckets.
     * @return Query results as instance of {@link SelectResults}.
     * @throws QueryException When in case of query execution failure.
     */
    @SuppressWarnings({ "unchecked" })
    public static SelectResults<Object> executeOnBuckets(String queryString, Object[] queryParameters, Region region,
	    Set<Object> keys) throws QueryException {
	if ((queryString == null) || (queryString.length() == 0)) {
	    throw new QueryException("You must specify query string for execution.");
	}

	int limit = extractLimit(queryString);

	BucketOrientedQueryFunctionArgument functionArgument = new BucketOrientedQueryFunctionArgument(queryString,
		queryParameters);

	BucketOrientedQueryFunction function = new BucketOrientedQueryFunction();

	FunctionService.registerFunction(function);

	List<List<Object>> queryResults;

	try {
	    queryResults = (List<List<Object>>) FunctionService.onRegion(region).withFilter(keys)
		    .withArgs(functionArgument).execute(function).getResult();
	} catch (FunctionException e) {
	    logger.error("Failed to execute bucket oriented query function: " + function, e);

	    throw new QueryException(e.getMessage());
	}
	
	return formatSelectResults(queryResults, limit);
    }

    /**
     * Extracts limit value from query string.
     * 
     * @param queryString OQL query string.
     * @return Value of 'limit' clause.
     */
    private static int extractLimit(String queryString) {
	int limitIndex = queryString.lastIndexOf("limit");

	if (limitIndex == -1) {
	    limitIndex = queryString.lastIndexOf("LIMIT");
	}

	if (limitIndex == -1) {
	    return limitIndex;
	}

	String limitValue = queryString.substring(limitIndex + 5);

	return Integer.parseInt(limitValue.trim());
    }

    /**
     * Collects and formats query results into SelectResults. Limits query results based on limit value.
     * 
     * @param queryResults Query results from several nodes.
     * @param limit Query results limit.
     * @return Aggregated query results represented by instance of {@link SelectResults}.
     */
    @SuppressWarnings({ "unchecked" })
    private static SelectResults<Object> formatSelectResults(List<List<Object>> queryResults, int limit) {
	List<Object> list = new ArrayList<Object>();

	ObjectType baseElementType = null;

	for (List<Object> queryResult : queryResults) {
	    ObjectType elementType = (ObjectType) queryResult.remove(queryResult.size() - 1);

	    if (baseElementType == null) {
		baseElementType = elementType;
	    } else if (!baseElementType.equals(elementType)) {
		throw new IllegalStateException("Collection types for query result are different.");
	    }

	    list.addAll(queryResult);

	    if (limit != -1 && list.size() >= limit) {
		break;
	    }
	}

	return limit == -1 ? new ResultsCollectionWrapper(baseElementType, list) : new ResultsCollectionWrapper(
		baseElementType, list, limit);
    }
}
