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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.distributed.DistributedMember;

/**
 * Collects the partial destroyedEntriesCount values and sums them
 */
public class DestroyedEntriesCountCollector implements
	ResultCollector<Serializable, Serializable> {
	private Semaphore lock = new Semaphore(1);
	private long destroyedEntriesCount;

	public void addResult(DistributedMember member, Serializable value) {
		try {
			lock.acquire();

			if (value instanceof Long) {
				long partialCount = (Long) value;
				destroyedEntriesCount += partialCount;
			}

		} catch (InterruptedException e) {
			throw new FunctionException(e);
		} finally {
			lock.release();
		}

	}

	public void clearResults() {
		try {
			lock.acquire();

			destroyedEntriesCount = 0;

		} catch (InterruptedException e) {
			throw new FunctionException(e);
		} finally {
			lock.release();
		}
	}

	public void endResults() {
	}

	public Serializable getResult() throws FunctionException {
		try {
			lock.acquire();

			return destroyedEntriesCount;

		} catch (InterruptedException e) {
			throw new FunctionException(e);
		} finally {
			lock.release();
		}
	}

	public Serializable getResult(long timeout, TimeUnit timeUnit)
		throws FunctionException, InterruptedException {
		try {
			if (!lock.tryAcquire(timeout, timeUnit)) {
				throw new FunctionException("Timeout during the lock acquiring");
			}

			return destroyedEntriesCount;

		} catch (InterruptedException e) {
			throw new FunctionException(e);
		} finally {
			lock.release();
		}
	}

}
