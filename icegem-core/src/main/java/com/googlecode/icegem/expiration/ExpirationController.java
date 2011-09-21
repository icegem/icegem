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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionService;

/**
 * <p>
 * The class which can expire region entries using custom policy (strategy).
 * </p>
 * 
 * <p>
 * Example:
 * 
 * <pre>
 * 
 * Region<Long, Data> dataRegion = ...;
 * Region<Long, Error> errorsRegion = ...;
 * 
 * ExpirationController expirationController = new ExpirationController();
 * 
 * long destroyedEntriesNumberForData = expirationController.process(dataRegion,
 * 	new ExpirationPolicy() {
 * 
 * 		public boolean isExpired(Entry&lt;Object, Object&gt; entry) {
 * 			// TODO: Add some logic
 * 			return false;
 * 		}
 * 	});
 * 
 * long destroyedEntriesNumberForErrors = expirationController.process(errorsRegion,
 * 	new ExpirationPolicy() {
 * 
 * 		public boolean isExpired(Entry&lt;Object, Object&gt; entry) {
 * 			// TODO: Add some logic
 * 			return false;
 * 		}
 * 	});
 * 
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * You can get access to the other regions inside of
 * <code>isExpired(Entry&lt;Object, Object&gt;)</code> method in the following
 * way:
 * 
 * <pre>
 * new ExpirationPolicy() {
 * 
 *   public boolean isExpired(Entry&lt;Object, Object&gt; entry) {
 *     RegionService regionService = entry.getRegion().getRegionService();
 *     Region<Long, TransactionProcessingError> errorsRegion = regionService.getRegion("errors");
 * 
 *     // TODO: Add some logic
 *     return false;
 *   }
 * });
 * </pre>
 * 
 * </p>
 * 
 */
public class ExpirationController {

	private Logger logger = LoggerFactory.getLogger(ExpirationController.class);

	private static final long DEFAULT_PACKET_SIZE = 1;
	private static final long DEFAULT_PACKET_DELAY = 0;

	private long packetSize = DEFAULT_PACKET_SIZE;
	private long packetDelay = DEFAULT_PACKET_DELAY;

	/**
	 * Gets the size of the consistently expired entries packet
	 * 
	 * @return - the packet size
	 */
	public long getPacketSize() {
		return packetSize;
	}

	/**
	 * Gets the size of the consistently expired entries packet
	 * 
	 * @param packetSize
	 *            - the packet size
	 */
	public void setPacketSize(long packetSize) {
		this.packetSize = packetSize;
	}

	/**
	 * Gets the delay in processing after the packetSize entries, milliseconds
	 * 
	 * @return - the packet delay
	 */
	public long getPacketDelay() {
		return packetDelay;
	}

	/**
	 * Sets the delay in processing after the packetSize entries, milliseconds
	 * 
	 * @param packetDelay
	 *            - the packet delay
	 */
	public void setPacketDelay(long packetDelay) {
		this.packetDelay = packetDelay;
	}

	/**
	 * Applies the specified policy on the specified region and returns number
	 * of destroyed entries.
	 * 
	 * @param region
	 *            - the region
	 * @param policy
	 *            - the expiration policy
	 * @return - the number of destroyed region entries
	 */
	public long process(Region<?, ?> region, ExpirationPolicy policy) {

		long destroyedEntriesNumber = 0;

		try {

			if (region == null) {
				throw new IllegalStateException("The Region cannot be null");
			}

			if (policy == null) {
				throw new IllegalArgumentException(
					"The ExpirationPolicy cannot be null");
			}

			logger
				.info("Running ExpirationController process with parameters region = "
					+ region
					+ ", policy = "
					+ policy
					+ ", packetSize = "
					+ packetSize + ", packetDelay = " + packetDelay);

			DestroyedEntriesCountCollector collector = (DestroyedEntriesCountCollector) FunctionService
				.onRegion(region)
				.withArgs(
					new ExpirationFunctionArguments(packetSize, packetDelay))
				.withCollector(new DestroyedEntriesCountCollector())
				.execute(new ExpirationFunction(policy));

			Object result = collector.getResult();
			if (result instanceof Long) {
				destroyedEntriesNumber = (Long) result;
			}

			logger
				.info("ExpirationController process with parameters region = "
					+ region + ", policy = " + policy + ", packetSize = "
					+ packetSize + ", packetDelay = " + packetDelay
					+ " has destroyed " + destroyedEntriesNumber + " entries");

		} catch (RuntimeException re) {
			logger.error("RuntimeException during processing", re);
			throw re;
		}

		return destroyedEntriesNumber;
	}
}
