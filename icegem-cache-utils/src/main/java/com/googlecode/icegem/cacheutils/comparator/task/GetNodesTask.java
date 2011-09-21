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
package com.googlecode.icegem.cacheutils.comparator.task;

import java.util.List;
import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.gemstone.gemfire.cache.client.Pool;
import com.gemstone.gemfire.cache.client.PoolFactory;
import com.gemstone.gemfire.cache.client.PoolManager;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.googlecode.icegem.cacheutils.common.FileService;
import com.googlecode.icegem.cacheutils.comparator.CompareTool;
import com.googlecode.icegem.cacheutils.comparator.function.GetNodesFunction;
import com.googlecode.icegem.cacheutils.comparator.function.GetNodesFunctionArguments;
import com.googlecode.icegem.cacheutils.comparator.function.GetNodesResultCollector;
import com.googlecode.icegem.cacheutils.comparator.model.Node;

public class GetNodesTask {

	private Pool createLocatorsPool(String locators) {
		PoolFactory poolFactory = PoolManager.createFactory();

		for (String locator : locators.split(",")) {
			String host = locator.substring(0, locator.indexOf("["));
			int port = Integer.parseInt(locator.substring(
				locator.indexOf("[") + 1, locator.indexOf("]")));

			poolFactory.addLocator(host, port);
		}

		return poolFactory.create("pool-" + locators);
	}

	private Pool createServerPool(String server) {
		String host = server.substring(0, server.indexOf("["));
		int port = Integer.parseInt(server.substring(server.indexOf("[") + 1,
			server.indexOf("]")));

		return PoolManager.createFactory().addServer(host, port)
			.create("pool-" + host + "-" + port);
	}

	@SuppressWarnings("unchecked")
	private Set<Node> getNodesPartition(String locators, String regionName,
		int loadFactor, List<String> packages, long[] ids, int shift) {
		ClientCache cache = new ClientCacheFactory().set("log-level", "none")
			.create();

		ClientRegionFactory<?, ?> clientRegionFactory = cache
			.createClientRegionFactory(ClientRegionShortcut.PROXY);

		Pool pool = createLocatorsPool(locators);

		clientRegionFactory.setPoolName(pool.getName());

		Region<?, ?> region = clientRegionFactory.create(regionName);

		GetNodesResultCollector collector = (GetNodesResultCollector) FunctionService
			.onRegion(region)
			.withCollector(new GetNodesResultCollector())
			.withArgs(
				new GetNodesFunctionArguments(loadFactor, packages, ids, shift))
			.execute(new GetNodesFunction());

		Set<Node> nodesSet = (Set<Node>) collector.getResult();

		cache.close();

		return nodesSet;
	}

	@SuppressWarnings("unchecked")
	private Set<Node> getNodesReplicate(String server, String regionName,
		int loadFactor, List<String> packages, long[] ids, int shift) {
		ClientCache cache = new ClientCacheFactory()
			.set("log-level", "warning").create();

		ClientRegionFactory<?, ?> clientRegionFactory = cache
			.createClientRegionFactory(ClientRegionShortcut.PROXY);

		Pool pool = createServerPool(server);

		clientRegionFactory.setPoolName(pool.getName());

		Region<?, ?> region = clientRegionFactory.create(regionName);

		GetNodesResultCollector collector = (GetNodesResultCollector) FunctionService
			.onRegion(region)
			.withCollector(new GetNodesResultCollector())
			.withArgs(
				new GetNodesFunctionArguments(loadFactor, packages, ids, shift))
			.execute(new GetNodesFunction());

		Set<Node> nodesSet = (Set<Node>) collector.getResult();

		cache.close();

		return nodesSet;
	}

	public static void main(String[] args) throws Throwable {
		try {
			GetNodesTaskArguments arguments = (GetNodesTaskArguments) FileService.readObject(args[0]);
			
			String mode = arguments.getMode();
			String address = arguments.getAddress();
			String regionName = arguments.getRegionName();
			String filename = arguments.getFilename();
			int loadFactor = arguments.getLoadFactor();
			List<String> packages = arguments.getPackages();
			long[] ids = arguments.getIds();
			int shift = arguments.getShift();

			GetNodesTask calculator = new GetNodesTask();

			Set<Node> nodesSet = null;

			if (CompareTool.PARTITION.equals(mode)) {
				nodesSet = calculator.getNodesPartition(address, regionName,
					loadFactor, packages, ids, shift);
			} else {
				nodesSet = calculator.getNodesReplicate(address, regionName,
					loadFactor, packages, ids, shift);
			}

			FileService.writeObject(filename, nodesSet);
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

}
