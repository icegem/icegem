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
package com.googlecode.icegem.utils;

import java.io.Serializable;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.execute.ResultSender;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;

/**
 * 
 * @author Renat Akhmerov.
 */
public class RegionSizeFunction extends FunctionAdapter {
    /** Serial version UID. */
    private static final long serialVersionUID = 2939897587860693898L;

    @Override
    public String getId() {
	return RegionSizeFunction.class.getName();
    }

    /**
     * NOTE: This method should return true so that the function would not be sent
     * to nodes which don't have any primary data.   
     */
    @Override
    public boolean optimizeForWrite() {
	return true;
    }
    
    @Override
    public void execute(FunctionContext ctx) {
	ResultSender<Serializable> sndr = ctx.getResultSender();

	if (!(ctx instanceof RegionFunctionContext)) {
	    sndr.sendException(new FunctionException("Function context must be of type RegionFunctionContext."));
	    
	    return;
	}
	    
	RegionFunctionContext regionCtx = (RegionFunctionContext) ctx;

	Region<Object, Object> region = regionCtx.getDataSet();

	Region<Object, Object> localRegion = region;

	if (region.getAttributes().getDataPolicy().withPartitioning()) {
	    localRegion = PartitionRegionHelper.getLocalPrimaryData(region);
	}

	sndr.lastResult(localRegion.size());
    }
}
