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
package itest.com.googlecode.icegem.expiration;

import static org.junit.Assert.assertEquals;
import itest.com.googlecode.icegem.expiration.model.Transaction;
import itest.com.googlecode.icegem.expiration.model.TransactionProcessingError;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.Region.Entry;
import com.gemstone.gemfire.cache.RegionService;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.googlecode.icegem.expiration.ExpirationController;
import com.googlecode.icegem.expiration.ExpirationPolicy;
import com.googlecode.icegem.utils.CacheUtils;
import com.googlecode.icegem.utils.JavaProcessLauncher;
import com.googlecode.icegem.utils.ServerTemplate;

/**
 * <p>
 * Tests the ExpirationController for the following scenario:
 * <ol>
 * <li>Fill the transactions region with 5 transactions in different states</li>
 * <li>Fill the errors region with 2 errors associated with 2 last transactions</li>
 * <li>Wait 3 seconds (it is more than 1 second of the expiration time)</li>
 * <li>Create and run the ExpirationController which configured with
 * TransactionExpirationPolicy and can be recursive or not</li>
 * <ul>
 * <li>If recursive = true than in case of the transaction is expired we will
 * destroy also error associated with the transaction</li>
 * <li>If recursive = false than delete only transactions</li>
 * </ul>
 * </ol>
 * </p>
 */
public class ExpirationControllerTest implements Serializable {

	private static final long serialVersionUID = -1467927314327799826L;

	private static final long EXPIRATION_TIME = 1 * 1000;

	/** Field cacheServer1 */
	private static Process cacheServer1;
	/** Field cacheServer2 */
	private static Process cacheServer2;
	/** Field javaProcessLauncher */
	private static JavaProcessLauncher javaProcessLauncher = new JavaProcessLauncher();

	private class TransactionExpirationPolicy implements ExpirationPolicy {

		private static final long serialVersionUID = -8642198262421835809L;

		private long expirationTime;
		private boolean recursively;

		public TransactionExpirationPolicy(long expirationTime,
			boolean recursively) {
			this.expirationTime = expirationTime;
			this.recursively = recursively;
		}

		private boolean isTimeExpired(long finishedAt) {
			boolean timeExpired = false;

			long checkedAt = System.currentTimeMillis();
			long idleDuration = checkedAt - finishedAt;

			if (idleDuration > expirationTime) {
				timeExpired = true;
			}

			return timeExpired;
		}

		public boolean isExpired(Entry<Object, Object> entry) {

			boolean expired = false;

			Object key = entry.getKey();
			Object value = entry.getValue();

			if ((key instanceof Long) && (value instanceof Transaction)) {
				Long transactionId = (Long) key;
				Transaction transaction = (Transaction) value;

				boolean timeExpired = isTimeExpired(transaction.getFinishedAt());

				RegionService regionService = entry.getRegion()
					.getRegionService();
				Region<Long, TransactionProcessingError> errorsRegion = regionService
					.getRegion("errors");

				if (transaction.isProcessedSuccessfully() && timeExpired) {
					expired = true;
				} else {
					TransactionProcessingError error = errorsRegion
						.get(transactionId);

					if (error != null) {
						timeExpired = isTimeExpired(error.getResolvedAt());
						if (error.isResolved() && timeExpired) {
							expired = true;
						}
					}
				}

				if (expired && recursively) {
					TransactionProcessingError error = errorsRegion
						.get(transactionId);
					if (error != null) {
						errorsRegion.destroy(transactionId);
					}
				}

			}

			return expired;
		}

	}

	@BeforeClass
	public static void setUp() throws IOException, InterruptedException,
		TimeoutException {
		startCacheServers();
	}

	@AfterClass
	public static void tearDown() throws IOException, InterruptedException {
		stopCacheServers();
	}

	@Test
	public void testProcessNotRecursive() throws Throwable {
		fillData();
		assertThat(5, 2);
		Thread.sleep(3 * 1000);
		long destroyedEntriesNumber = expire(false);
		assertEquals(destroyedEntriesNumber, 2);
		assertThat(3, 2);
	}

	@Test
	public void testProcessRecursive() throws InterruptedException {
		fillData();
		assertThat(5, 2);
		Thread.sleep(3 * 1000);

		long destroyedEntriesNumber = expire(true);
		assertEquals(destroyedEntriesNumber, 2);
		assertThat(3, 1);
	}

	@Test
	public void testProcessLoad() throws InterruptedException {
		System.out.println("Smart expiration load test start");
		System.out.println("Before fillData");
		final int count = 10000;
		long startTime = System.currentTimeMillis();
		fillData(count);
		long finishTime = System.currentTimeMillis();
		System.out.println("Data filled in " + (finishTime - startTime) + "ms");
		assertThat(5 * count, 2 * count);
		Thread.sleep(3 * 1000);

		System.out.println("Before expire");
		startTime = System.currentTimeMillis();
		long destroyedEntriesNumber = expire(false, 1000, 1000);
		finishTime = System.currentTimeMillis();
		System.out.println("Expired in " + (finishTime - startTime) + "ms");
		assertEquals(destroyedEntriesNumber, 2 * count);
		assertThat(3 * count, 2 * count);
		System.out.println("Smart expiration load test finish");
	}

	private <K, V> Region<K, V> getRegion(ClientCache cache, String regionName) {
		ClientRegionFactory<K, V> clientRegionFactory = cache
			.createClientRegionFactory(ClientRegionShortcut.PROXY);

		Region<K, V> region = cache.getRegion(regionName);

		if (region == null) {
			region = clientRegionFactory.create(regionName);
		}

		return region;
	}

	private void fillData() {
		fillData(1);
	}

	private void fillData(long count) {
		ClientCache cache = new ClientCacheFactory()
			.addPoolLocator("localhost", 10355).set("log-level", "warning")
			.create();

		Region<Long, Transaction> transactionsRegion = getRegion(cache,
			"transactions");
		CacheUtils.clearRegion(transactionsRegion);
		Region<Long, TransactionProcessingError> errorsRegion = getRegion(
			cache, "errors");
		CacheUtils.clearRegion(errorsRegion);

		for (long i = 1, id = 1; i <= count; i++, id += 5) {
			if ((i % 1000) == 0) {
				System.out.println("Filling cycle number " + i);
			}

			Transaction notStartedTransaction = new Transaction();
			transactionsRegion.put(id, notStartedTransaction);

			Transaction startedTransaction = new Transaction();
			startedTransaction.begin();
			transactionsRegion.put(id + 1, startedTransaction);

			Transaction committedTransaction = new Transaction();
			committedTransaction.begin();
			committedTransaction.commit();
			transactionsRegion.put(id + 2, committedTransaction);

			Transaction rolledbackUnresolvedTransaction = new Transaction();
			rolledbackUnresolvedTransaction.begin();
			rolledbackUnresolvedTransaction.rollback();
			transactionsRegion.put(id + 3, rolledbackUnresolvedTransaction);

			Transaction rolledbackResolvedTransaction = new Transaction();
			rolledbackResolvedTransaction.begin();
			rolledbackResolvedTransaction.rollback();
			transactionsRegion.put(id + 4, rolledbackResolvedTransaction);

			TransactionProcessingError unresolvedError = new TransactionProcessingError(
				"Error during the transaction processing");
			errorsRegion.put(id + 3, unresolvedError);

			TransactionProcessingError resolvedError = new TransactionProcessingError(
				"Error during the transaction processing");
			resolvedError.setResolved();
			errorsRegion.put(id + 4, resolvedError);
		}

		cache.close();
	}

	private void assertThat(int transactionsNumber, int errorsNumber) {
		ClientCache cache = new ClientCacheFactory()
			.addPoolLocator("localhost", 10355).set("log-level", "warning")
			.create();

		Region<Long, Transaction> transactionsRegion = getRegion(cache,
			"transactions");

		Region<Long, TransactionProcessingError> errorsRegion = getRegion(
			cache, "errors");

		assertEquals(transactionsRegion.keySetOnServer().size(),
			transactionsNumber);
		assertEquals(errorsRegion.keySetOnServer().size(), errorsNumber);

		cache.close();
	}

	private long expire(boolean recursively, long packetSize, long packetDelay) {
		ClientCache cache = new ClientCacheFactory()
			.addPoolLocator("localhost", 10355).set("log-level", "warning")
			.create();

		Region<Long, Transaction> transactionsRegion = getRegion(cache,
			"transactions");

		ExpirationController expirationController = new ExpirationController();

		expirationController.setPacketSize(packetSize);
		expirationController.setPacketDelay(packetDelay);

		long destroyedEntriesNumber = expirationController.process(
			transactionsRegion, new TransactionExpirationPolicy(
				EXPIRATION_TIME, recursively));

		cache.close();

		return destroyedEntriesNumber;
	}

	private long expire(boolean recursively) {
		return expire(recursively, 1, 0);
	}

	private static void startCacheServers() throws IOException,
		InterruptedException {
		cacheServer1 = javaProcessLauncher
			.runWithConfirmation(
				"",
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=expirationServerProperties.properties" }, null);
		cacheServer2 = javaProcessLauncher
			.runWithConfirmation(
				"",
				ServerTemplate.class,
				new String[] { "-DgemfirePropertyFile=expirationServerProperties.properties" }, null);
	}

	private static void stopCacheServers() throws IOException,
		InterruptedException {
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer1);
		javaProcessLauncher.stopBySendingNewLineIntoProcess(cacheServer2);
	}

}
