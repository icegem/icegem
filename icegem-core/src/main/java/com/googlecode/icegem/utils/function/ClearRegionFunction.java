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
package com.googlecode.icegem.utils.function;

import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.execute.ResultSender;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;

/**
 * Function for clearing regions of different types.
 * 
 * @see com.googlecode.icegem.utils.CacheUtils for more details.
 * 
 * @author Andrey Stepanov aka standy
 * @author Alexey Kharlamov <aharlamov@gmail.com>
 */
public class ClearRegionFunction extends FunctionAdapter {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final static String FUNCTION_ID = ClearRegionFunction.class
			.getName();

	@Override
	public void execute(FunctionContext functionContext) {
		ResultSender<Boolean> rs = functionContext.getResultSender();
		RegionFunctionContext rctx = (RegionFunctionContext) functionContext;
		boolean wanSafe = (Boolean) functionContext.getArguments();

		Region<Object, Object> region = rctx.getDataSet();

		if(!wanSafe && !PartitionRegionHelper.isPartitionedRegion(region)) {
			region.clear();
		}
		
		Set<Object> keys ;
		if (PartitionRegionHelper.isPartitionedRegion(region)) {
			keys = PartitionRegionHelper.getLocalPrimaryData(region).keySet();
		} else {
			keys = region.keySet();
		}

		for (Object key : keys) {
			region.destroy(key);
		}

		rs.lastResult(true);
	}

	@Override
	public String getId() {
		return FUNCTION_ID;
	}
	
	@Override
	public boolean isHA() {
		return true;
	}
	
	@Override
	public boolean optimizeForWrite() {
		return true;
	}
}
