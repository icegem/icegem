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
package com.googlecode.icegem.cacheutils.monitor.controller;

import java.util.List;

import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.googlecode.icegem.cacheutils.monitor.function.ZeroFunction;

public class FunctionExecutionThread extends Thread {
	private Pool pool;
	private int zero = -1;

	public FunctionExecutionThread(Pool pool) {
		this.pool = pool;
	}

	@Override
	public void run() {
		try {
			zero = executeZeroFunction(pool);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Executes the "zero" function using pool for the concrete server. 
	 * 
	 * @param pool
	 *            - the pool for the concrete server
	 * @return 0 in case of function executed without problems, -1 otherwise
	 * @throws FunctionException
	 * @throws InterruptedException
	 */
	private int executeZeroFunction(Pool pool) throws FunctionException,
		InterruptedException {
		int result = -1;

		ResultCollector<?, ?> collector = FunctionService.onServer(pool)
			.execute(new ZeroFunction());

		List<?> functionResult = (List<?>) collector.getResult();
		if ((functionResult != null) && (functionResult.size() == 1)
			&& (functionResult.get(0) instanceof Integer)) {
			result = (Integer) functionResult.get(0);
		}

		return result;
	}

	public int getZero() {
		return zero;
	}
}
