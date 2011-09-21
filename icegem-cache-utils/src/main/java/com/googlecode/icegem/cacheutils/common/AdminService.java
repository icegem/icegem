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

import java.util.*;

import com.gemstone.gemfire.admin.*;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.googlecode.icegem.utils.PropertiesHelper;

/**
 * TODO: Create appropriate javadoc. 
 * 
 * Connects to the distributed system and explore its' structure.
 */
public class AdminService {
    private AdminDistributedSystem admin;
    private Map<String, String> regionNames;
    private Set<SystemMember> systemMembers = new HashSet<SystemMember>();
    private DistributedSystem connection;

    public AdminService(String locator) throws Exception {
	this(locator, true);
    }

    public AdminService(String locator, boolean logToFile) throws Exception {
	this.admin = adminCreateAndConnect(locator, logToFile);
	systemMembers.addAll(new HashSet<SystemMember>(Arrays.asList(admin.getCacheVms())));
	systemMembers.addAll(new HashSet<SystemMember>(Arrays.asList(admin.getSystemMemberApplications())));
    }

    public Map<String, String> getRegionNames(String regionNamesOption, boolean withSubRegionsOption)
	    throws AdminException {
	regionNames = new TreeMap<String, String>();
	if (regionNamesOption.equals("all"))
	    return getSystemRegionNames(null, true);
	String[] regionNamesArray = regionNamesOption.split(",");
	return getSystemRegionNames(Arrays.asList(regionNamesArray), withSubRegionsOption);
    }

    public Map<String, SystemMemberRegion> getMemberRegionMap(String name) throws AdminException {
	Map<String, SystemMemberRegion> result = new HashMap<String, SystemMemberRegion>();
	for (SystemMember member : systemMembers) {
	    if (!member.hasCache())
		continue;
	    SystemMemberCache cache = member.getCache();
	    SystemMemberRegion region = cache.getRegion(name);
	    if (region != null) {
		result.put(member.getId(), region);
	    }
	}
	return result;
    }

    public void close() {
	admin.disconnect();
	connection.disconnect();
    }

    private Map<String, String> getSystemRegionNames(List<String> regionNamesToFind, boolean withSubRegionsOption)
	    throws AdminException {
	for (SystemMember member : systemMembers) {
	    if (!member.hasCache())
		continue;
	    SystemMemberCache cache = member.getCache();
	    for (Object name : cache.getRootRegionNames()) {
		getSubregionsNameRequrcively(cache, "/" + name, regionNamesToFind, withSubRegionsOption);
	    }
	}
	return regionNames;
    }

    private void getSubregionsNameRequrcively(SystemMemberCache cache, String path, List<String> regionNamesToFind,
	    boolean withSubRegionsOption) throws AdminException {
	SystemMemberRegion region = cache.getRegion(path);
	if (region != null) {
	    if (regionNamesToFind == null
		    || regionNamesToFind.contains(region.getName())
		    || (withSubRegionsOption && new ArrayList<String>(Arrays.asList(path.split("/")))
			    .removeAll(regionNamesToFind)))
		regionNames.put(path, region.getName());
	    Set<String> subregionsPaths = region.getSubregionFullPaths();
	    for (String subregionsPath : subregionsPaths) {
		getSubregionsNameRequrcively(cache, subregionsPath, regionNamesToFind, withSubRegionsOption);
	    }
	}
    }

    private AdminDistributedSystem adminCreateAndConnect(String locator, boolean logToFile) throws Exception {
	PropertiesHelper propertiesHelper = new PropertiesHelper("/adminservice.properties");
	Properties props = new Properties();
	props.setProperty("mcast-port", "0");
	props.setProperty("license-type",
		propertiesHelper.getStringProperty("icegem.cacheutils.adminservice.gemfire.license.type"));
	props.setProperty("license-file",
		propertiesHelper.getStringProperty("icegem.cacheutils.adminservice.gemfire.license.file"));
	if (locator != null)
	    props.setProperty("locators", locator);
	if (logToFile) {
	    props.setProperty("log-file", "admin.log");
	} else {
	    props.setProperty("log-level", "none");
	}

	AdminDistributedSystemFactory.setEnableAdministrationOnly(false);
	connection = DistributedSystem.connect(props);
	DistributedSystemConfig config = AdminDistributedSystemFactory.defineDistributedSystem(connection, null);
	AdminDistributedSystem admin = AdminDistributedSystemFactory.getDistributedSystem(config);
	admin.connect();
	long timeout = 30 * 1000;
	try {
	    if (!admin.waitToBeConnected(timeout)) {
		String s = "Could not connect after " + timeout + "ms";
		throw new Exception(s);
	    }
	} catch (InterruptedException ex) {
	    String s = "Interrupted while waiting to be connected";
	    throw new Exception(s, ex);
	}
	return admin;
    }

    public AdminDistributedSystem getAdmin() {
	return admin;
    }

    public void setAdmin(AdminDistributedSystem admin) {
	this.admin = admin;
    }
}
