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
package com.googlecode.icegem.serialization._jdktypes;

import java.io.InvalidClassException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Vector;

import javassist.CannotCompileException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Arrays;
import com.googlecode.icegem.serialization.HierarchyRegistry;
import com.googlecode.icegem.serialization.primitive.TestParent;

/**
 * @author igolovach
 */
@RunWith(Parameterized.class)
public class JdkTypesTest extends TestParent {

	private JdkTypesBean expected;

	public JdkTypesTest(JdkTypesBean expected) {
		this.expected  = expected;
	}
	
    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(), JdkTypesBean.class);
    }

    @Parameters
    public static Collection<Object[]> data() throws UnknownHostException {
        return Arrays.asList(new Object[][]{
                // null fieldsJdkTypesTest.java
               // new Object[]{new JdkTypesBean()},
                // initialize diff field groups of bean
                new Object[]{produceCommon()},
                new Object[]{produceOldCollections()},
                new Object[]{produceCollectionsAPILists()},
                new Object[]{produceCollectionsAPIMaps()},
                new Object[]{produceCollectionsAPISets()},
        });
    }

    @Test
    public void test() {

        // Serialize / Deserialize
        JdkTypesBean actual = serializeAndDeserialize(expected);

        // assert
        assert actual.equals(expected);
    }

    private static JdkTypesBean produceCommon() throws UnknownHostException {
        final JdkTypesBean result = new JdkTypesBean();

        result.setTimestamp(new Timestamp(new Date().getTime()));
        result.setObject(123);
        result.setClazz(Integer.class);
        result.setString("Hello");
        result.setDate(new Date(123456));
        //result.setFile(new File("c:/a/b/c.txt")); //todo: but "/a/b/c.txt" - not! see how realized file transfer in GemFire
        result.setInetAddress(InetAddress.getByAddress(new byte[]{1, 2, 3, 4}));
        result.setInet4Address((Inet4Address) InetAddress.getByAddress(new byte[]{1, 2, 3, 4}));
        result.setInet6Address(Inet6Address.getByAddress("hello", new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6}, 1));
        // properties
        final Properties properties = new Properties();
        properties.setProperty("abc", "def");
        result.setProperties(properties);

        UUID uuid = new UUID(293847234L, 239728374L);
        result.setUuid(uuid);

        return result;
    }

    private static JdkTypesBean produceOldCollections() {
        final JdkTypesBean result = new JdkTypesBean();

        // Hashtable
        final Hashtable<Object, Object> hashtable = new Hashtable<Object, Object>();
        hashtable.put("123", 123);
        result.setHashtable(hashtable);
        // Vector
        final Vector<Object> vector = new Vector<Object>();
        vector.add("456");
        result.setVector(vector);
        // Stack
        final Stack<Object> stack = new Stack<Object>();
        stack.add("789");
        result.setStack(stack);

        return result;
    }

    private static JdkTypesBean produceCollectionsAPILists() {
        final JdkTypesBean result = new JdkTypesBean();

        // List
        final List<Object> list = new ArrayList<Object>();
        list.add("123");
        result.setList(list);
        // ArrayList
        final ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add("456");
        result.setArrayList(arrayList);
        // ArrayList
        final LinkedList<Object> linkedList = new LinkedList<Object>();
        linkedList.add("789");
        result.setLinkedList(linkedList);

        return result;
    }

    private static JdkTypesBean produceCollectionsAPIMaps() {
        final JdkTypesBean result = new JdkTypesBean();

        // Map
        final Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("123", 456.789d);
        result.setMap(map);
        // HashMap
        final HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
        hashMap.put("123", 456.789d);
        result.setHashMap(hashMap);
        // HashMap
        final TreeMap<Object, Object> treeMap = new TreeMap<Object, Object>();
        treeMap.put("123", 456.789d);
        result.setTreeMap(treeMap);
        // Map
        final IdentityHashMap<Object, Object> identityHashMap = new IdentityHashMap<Object, Object>();
        identityHashMap.put("123", 456.789d);
        result.setIdentityHashMap(identityHashMap);

        return result;
    }

    private static JdkTypesBean produceCollectionsAPISets() {
        final JdkTypesBean result = new JdkTypesBean();

        // Set
        final Set<Object> set = new HashSet<Object>();
        set.add("123");
        result.setSet(set);
        // Set
        final LinkedHashSet<Object> linkedHashSet = new LinkedHashSet<Object>();
        linkedHashSet.add("123");
        result.setLinkedHashSet(linkedHashSet);
        // HashSet
        final HashSet<Object> hashSet = new HashSet<Object>();
        hashSet.add("123");
        result.setHashSet(hashSet);
        // Set
        final TreeSet<Object> treeSet = new TreeSet<Object>();
        treeSet.add("123");
        result.setTreeSet(treeSet);

        return result;
    }
}

