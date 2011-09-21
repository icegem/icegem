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

import com.gemstone.gemfire.cache.execute.*;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.cache.query.*;
import com.gemstone.gemfire.cache.query.internal.DefaultQuery;
import com.gemstone.gemfire.internal.cache.LocalDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Function for executing query on a specified set of buckets.
 * Do not call this function directly. Use bucket oriented query service instead.
 *
 * @see com.googlecode.icegem.query.bucketoriented.BucketOrientedQueryService
 *
 * @author Andrey Stepanov aka standy
 */
public class BucketOrientedQueryFunction extends FunctionAdapter {
    /** Function ID. */
    private final static String FUNCTION_ID = BucketOrientedQueryFunction.class.getName();

    /** Serial version UID. */
    private static final long serialVersionUID = -8818891792083706794L;

    /** Logger. */
    private Logger logger = LoggerFactory.getLogger(BucketOrientedQueryFunction.class);

    /**
     * Executes query using arguments query string and query parameters.
     *
     * @param functionContext Function context.
     */
    @Override
    @SuppressWarnings({ "ThrowableInstanceNeverThrown", "unchecked" })
    public void execute(FunctionContext functionContext) {
	ResultSender<Serializable> resultSender = functionContext.getResultSender();

	RegionFunctionContext regionFunctionContext = (RegionFunctionContext) functionContext;

	if (functionContext.getArguments() == null) {
	    handleException(new FunctionException("You must specify function argument for query execution."),
		    resultSender, null);

	    return;
	}

	if (!(functionContext.getArguments() instanceof BucketOrientedQueryFunctionArgument)) {
	    handleException(new FunctionException("Function arguments must be of type "
		    + BucketOrientedQueryFunctionArgument.class.getName() + "."), resultSender, null);

	    return;
	}

	BucketOrientedQueryFunctionArgument argument = (BucketOrientedQueryFunctionArgument) functionContext
		.getArguments();

	LocalDataSet localData = (LocalDataSet) PartitionRegionHelper.getLocalDataForContext(regionFunctionContext);

	QueryService queryService = localData.getCache().getQueryService();

	String queryStr = argument.getQueryString();

	try {
	    Query query = queryService.newQuery(queryStr);

	    SelectResults<?> result = (SelectResults<?>) localData.executeQuery((DefaultQuery) query,
		    argument.getQueryParameters(), localData.getBucketSet());

	    resultSender.lastResult((Serializable) formatResults(result));
	} catch (Exception e) {
	    handleException(e, resultSender, queryStr);
	}
    }

    /**
     * Gets function id.
     *
     * @return Function id.
     */
    @Override
    public String getId() {
	return FUNCTION_ID;
    }

    /**
     * If you use redundancy for partitioned region then GemFire will send this
     * function to those members that contain primary or redundant copy of bucket(s).
     * It can increase number of members that will execute this function.
     * But if you want to send this function only to those members that store primary
     * copy of bucket, you must enable a function option "optimizeForWrite".
     *
     * See a forum link
     * http://forums.gemstone.com/viewtopic.php?f=3&t=496&hilit=bucket+Id&sid=f3b823b748bb253e5019e489c8480fbd
     * for details.
     *
     * @return boolean
     */
    @Override
    public boolean optimizeForWrite() {
	return false;
    }

    /**
     * Handles exceptions during query execution.
     *
     * @param e Exception to handle.
     * @param resultSender of type ResultSender<Serializable>
     */
    @SuppressWarnings({ "ThrowableInstanceNeverThrown" })
    private void handleException(Throwable e, ResultSender<Serializable> resultSender, String queryString) {
	logger.error("Failed to execute bucket oriented query" + (queryString != null ? ": " + queryString : "."), e);

	resultSender.sendException(new FunctionException(e.getMessage()));
    }

    /**
     * Formats results of query execution.
     *
     * @param selectResults of type SelectResults
     * @return List<Object>
     */
    private List<Object> formatResults(SelectResults<?> selectResults) {
	List<Object> results = new ArrayList<Object>(selectResults.size() + 1);

	results.addAll(selectResults.asList());
	results.add(selectResults.getCollectionType().getElementType());

	return results;
    }
}
