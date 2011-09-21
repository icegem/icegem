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
package com.googlecode.icegem.cacheutils.common;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.gemstone.gemfire.cache.client.ClientRegionFactory;
import com.gemstone.gemfire.cache.client.ClientRegionShortcut;
import com.googlecode.icegem.serialization.AutoSerializable;
import com.googlecode.icegem.serialization.HierarchyRegistry;

/**
 * TODO: Create appropriate javadoc.
 * 
 * Connects to distributed system as client in order to perform some tasks on a certain regions.
 */
public class PeerCacheService {
    /** */
    private static final Logger log = LoggerFactory.getLogger(PeerCacheService.class);

    /** */
    private ClientCache cache;

    /** */
    private List<String> scanPackages = new ArrayList<String>();

    /** */
    private ClientRegionFactory proxyRegionFactory;

    /**
     * @param serverOptionsString
     * @param scanPackages
     * @throws Exception
     */
    public PeerCacheService(String serverOptionsString, List<String> scanPackages) throws Exception {
	if (scanPackages != null) {
	    this.scanPackages = scanPackages;

	    registerSerializers();
	}

	ClientCacheFactory clientCacheFactory = new ClientCacheFactory();

	String[] serverOptions = serverOptionsString.split(",");

	for (String serverOption : serverOptions) {
	    if (serverOption != null) {
		String serverHost = serverOption.substring(0, serverOption.indexOf("["));
		String serverPort = serverOption.substring(serverOption.indexOf("[") + 1, serverOption.indexOf("]"));

		clientCacheFactory.addPoolServer(serverHost, Integer.parseInt(serverPort));
	    }
	}

	clientCacheFactory.set("log-level", "none");

	this.cache = clientCacheFactory.create();
    }

    /**
     * Creates regions structure similar to server region structure.
     *
     * @param regionNames names of the regions to reconstruct.
     * @return set of created regions.
     */
    public Set<Region<?, ?>> createRegions(Map<String, String> regionNames) {
	Set<Region<?, ?>> regions = new HashSet<Region<?, ?>>();

	proxyRegionFactory = cache.createClientRegionFactory(ClientRegionShortcut.PROXY);

	for (String regionPath : regionNames.keySet()) {
	    Region region = createRegion(regionPath, regionNames.get(regionPath));

	    regions.add(region);
	}

	return regions;
    }

    /**
     * @param regionNames Region names represented by mapping {regionPath => regionName}.
     * @return Region.
     */
    public Region<?, ?> createRegion(Map<String, String> regionNames) {
	proxyRegionFactory = cache.createClientRegionFactory(ClientRegionShortcut.PROXY);

	Region region = null;

	for (Map.Entry<String, String> e : regionNames.entrySet()) {
	    region = createRegion(e.getKey(), e.getValue());
	}

	return region;
    }

    /**
     * @param regionPath
     * @param regionName
     * @return
     */
    private Region<?, ?> createRegion(String regionPath, String regionName) {
	Region region = null;

	if (regionPath.equals("/" + regionName)) {
	    region = proxyRegionFactory.create(regionName);
	} else {
	    Region parentRegion = cache.getRegion(regionPath.substring(0, regionPath.lastIndexOf("/" + regionName)));

	    if (parentRegion != null)
		region = parentRegion.createSubregion(regionName, parentRegion.getAttributes());
	}

	return region;
    }

    /**
     * 
     */
    public void close() {
	cache.close();
    }

    /**
     * @throws Exception
     */
    private void registerSerializers() throws Exception {
	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

	registerClasses(classLoader);
    }

    /**
     * @param classLoader
     * @throws Exception
     */
    private void registerClasses(ClassLoader classLoader) throws Exception {
	List<Class<?>> classesFromPackages = new ArrayList<Class<?>>();

	for (String pack : scanPackages) {
	    log.info("Scan package " + pack + " for classes marked by @AutoSerializable");

	    ClassPathScanningCandidateComponentProvider ppp = new ClassPathScanningCandidateComponentProvider(false);

	    ppp.addIncludeFilter(new AnnotationTypeFilter(AutoSerializable.class));

	    Set<BeanDefinition> candidateComponents = ppp.findCandidateComponents(pack);

	    for (BeanDefinition beanDefinition : candidateComponents) {
		String className = beanDefinition.getBeanClassName();

		final Class<?> clazz = Class.forName(className);

		classesFromPackages.add(clazz);
	    }
	}

	try {
	    HierarchyRegistry.registerAll(classLoader, classesFromPackages);
	} catch (InvalidClassException e) {
	    final String msg = "Some class from list " + classesFromPackages + " is nor serializable. Cause: "
		    + e.getMessage();

	    log.error(msg);

	    throw new RuntimeException(msg, e);
	} catch (CannotCompileException e) {
	    final String msg = "Can't compile DataSerializer classes for some classes from list " + classesFromPackages
		    + ". Cause: " + e.getMessage();

	    log.error(msg);

	    throw new RuntimeException(msg, e);
	}
    }
}
