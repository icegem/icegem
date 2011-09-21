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
package com.googlecode.icegem.expiration;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.execute.ResultSender;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;

/**
 * Expires the region entries according to the ExpirationPolicy specified.
 */
public class ExpirationFunction extends FunctionAdapter implements Declarable {

	private static final long serialVersionUID = -6448375948152121283L;

	private Logger logger = LoggerFactory.getLogger(ExpirationFunction.class);

	private ExpirationPolicy policy;

	/**
	 * Creates the instance of the ExpirationFunction and configures it with
	 * ExpirationPolicy.
	 * 
	 * @param policy
	 *            - the expiration policy
	 */
	public ExpirationFunction(ExpirationPolicy policy) {
		this.policy = policy;
	}

	public void init(Properties arg0) {
		// do nothing
	}

	public void execute(FunctionContext functionContext) {
		logger.debug("Starting expiration");

		long destroyedEntriesCount = 0;

		ResultSender<Serializable> resultSender = functionContext
			.getResultSender();

		try {
			if (functionContext instanceof RegionFunctionContext) {
				RegionFunctionContext context = (RegionFunctionContext) functionContext;

				Serializable arguments = context.getArguments();
				if (arguments instanceof ExpirationFunctionArguments) {
					ExpirationFunctionArguments expirationFunctionArguments = (ExpirationFunctionArguments) arguments;

					long packetDelay = expirationFunctionArguments
						.getPacketDelay();
					long packetSize = expirationFunctionArguments
						.getPacketSize();

					logger.debug("Expiration configured with packetSize = "
						+ packetSize + ", packetDelay = " + packetDelay);

					Region<Object, Object> region = PartitionRegionHelper
						.getLocalDataForContext(context);

					Set<Entry<Object, Object>> entrySet = region.entrySet();

					int numberOfEntries = entrySet.size();
					
					logger.debug("There are " + numberOfEntries + " entries to check");
					logger.debug("Starting the check");
					
					long packetCounter = 1;
					for (Entry<Object, Object> entry : entrySet) {

						if ((packetDelay > 0)
							&& ((packetCounter % packetSize) == 0)) {
							logger.debug("Checking the " + packetCounter + " of " + numberOfEntries + " entry.");
							
							Thread.sleep(packetDelay);
						}

						if (entry instanceof Region.Entry) {
							Region.Entry<Object, Object> regionEntry = (Region.Entry<Object, Object>) entry;
							if ((policy != null)
								&& policy.isExpired(regionEntry)) {
								
								Object key = entry.getKey();
								
								logger.trace("Destroing the entry with key " + key);
								
								region.destroy(key);
								destroyedEntriesCount++;
							}
						}

						packetCounter++;
					}
					
					logger.debug("The check is finished. " + destroyedEntriesCount + " entries have been destroyed.");

				} else {
					throw new IllegalArgumentException(
						"The specified arguments are of type \""
							+ arguments.getClass().getName()
							+ "\". Should be of type \""
							+ ExpirationFunctionArguments.class.getName()
							+ "\"");
				}
			}
		} catch (Throwable t) {
			logger.error("Throwable during the expiration processing", t);
			resultSender.sendException(t);
		}

		resultSender.lastResult(destroyedEntriesCount);
	}

	@Override
	public String getId() {
		return this.getClass().getName();
	}
}
