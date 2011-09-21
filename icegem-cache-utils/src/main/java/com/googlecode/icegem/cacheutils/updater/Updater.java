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
package com.googlecode.icegem.cacheutils.updater;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

import com.gemstone.gemfire.cache.Region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates entries in given regions.
 */
public class Updater {
	private static final Logger log = LoggerFactory.getLogger(Updater.class);
    private CountDownLatch done;

	public void updateRegions(Set<Region<?, ?>> regions) {
        done = new CountDownLatch(regions.size());
		ExecutorService executor = Executors.newFixedThreadPool(regions.size());
		for (Region<?, ?> region : regions)
			executor.execute(new UpdateRunner(region));
        try {
            done.await();
        } catch (InterruptedException e) {
            log.info("Some error ocurred. Will stop updating." + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }

	private class UpdateRunner implements Runnable {
		private Region region;

		public UpdateRunner(Region region) {
			this.region = region;
		}

		public void run() {
			try {
				for (Object key : region.keySetOnServer()) {
					Object value = region.get(key);
					region.put(key, value);
                    //log.info("-----------------------key-value " + key + "-" + value);
				}
				log.info("Update of region " + region.getName() + " successful");
			} catch (Throwable t) {
				log.info("Update of region " + region.getName() + " failed");
				log.error("Exception occured in region " + region.getName() + "\n"
						+ t.getMessage());
			} finally {
			    done.countDown();
			}
		}
	}

}
