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

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.googlecode.icegem.serialization.HierarchyRegistry;

/**
 * Server template for using in tests.
 * 
 * Use
 * {@link JavaProcessLauncher#runWithConfirmation(String, Class, String[], String[])} to
 * launch this cache server from tests. All peer/server configurations should be
 * passed via properties file.
 * 
 * @see JavaProcessLauncher
 * 
 * @author Andrey Stepanov aka standy
 */
public class ServerTemplate {
	/** Cache. */
	private static Cache cache;

	/**
	 * Server entry point.
	 * 
	 * @param args
	 *            of type String[]
	 */
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			loadClasses(args[0]);
		}

		startCacheServer();

		ConsoleUtils
				.waitForEnter(JavaProcessLauncher.PROCESS_STARTUP_COMPLETED);

		stopCacheServer();
	}

	private static void loadClasses(String classes) throws Exception {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		String[] names = classes.split("\\,");
		Class<?>[] classArray = new Class<?>[names.length];
		
		for(int i = 0; i<names.length; i++) {
			classArray[i] = classLoader.loadClass(names[i]);
		}
		
		HierarchyRegistry.registerAll(classLoader, classArray);
	}

	/**
	 * Starts cache server.
	 */
	public static void startCacheServer() {
		cache = new CacheFactory().create();
	}

	/**
	 * Stops cache server.
	 */
	public static void stopCacheServer() {
		cache.close();
	}
}
