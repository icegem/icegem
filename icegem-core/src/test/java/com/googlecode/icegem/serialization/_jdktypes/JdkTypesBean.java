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

import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
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

import com.googlecode.icegem.serialization.AutoSerializable;
import com.googlecode.icegem.serialization.BeanVersion;

/**
 * @author igolovach
 */

@AutoSerializable(dataSerializerID = 644648778)
@BeanVersion(1)
public class JdkTypesBean {
    //todo: Region, Enum, Number?
    // common
    private Object object;
    private Class clazz;
    private String string;
    private Date date;
    private Timestamp timestamp;
    private File file;
    private InetAddress inetAddress;
    private Inet4Address inet4Address;
    private Inet6Address inet6Address;
    private Properties properties;
    private UUID uuid;
    // old collections
    private Hashtable hashtable;
    private Vector vector;
    private Stack stack;
    // Collections API: lists
    // todo: List
    private List list;
    private ArrayList arrayList;
    private LinkedList linkedList;
    // Collections API: maps
    // todo: Map?
    private Map map;
    private HashMap hashMap;
    private TreeMap treeMap;
    private IdentityHashMap identityHashMap;
    // Collections API: sets
    // todo: Set?
    private Set set;
    private LinkedHashSet linkedHashSet;
    private HashSet hashSet;
    private TreeSet treeSet;

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public Inet4Address getInet4Address() {
        return inet4Address;
    }

    public void setInet4Address(Inet4Address inet4Address) {
        this.inet4Address = inet4Address;
    }

    public Inet6Address getInet6Address() {
        return inet6Address;
    }
     public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setInet6Address(Inet6Address inet6Address) {
        this.inet6Address = inet6Address;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Hashtable getHashtable() {
        return hashtable;
    }

    public void setHashtable(Hashtable hashtable) {
        this.hashtable = hashtable;
    }

    public Vector getVector() {
        return vector;
    }

    public void setVector(Vector vector) {
        this.vector = vector;
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public ArrayList getArrayList() {
        return arrayList;
    }

    public void setArrayList(ArrayList arrayList) {
        this.arrayList = arrayList;
    }

    public LinkedList getLinkedList() {
        return linkedList;
    }

    public void setLinkedList(LinkedList linkedList) {
        this.linkedList = linkedList;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public HashMap getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap hashMap) {
        this.hashMap = hashMap;
    }

    public TreeMap getTreeMap() {
        return treeMap;
    }

    public void setTreeMap(TreeMap treeMap) {
        this.treeMap = treeMap;
    }

    public IdentityHashMap getIdentityHashMap() {
        return identityHashMap;
    }

    public void setIdentityHashMap(IdentityHashMap identityHashMap) {
        this.identityHashMap = identityHashMap;
    }

    public Set getSet() {
        return set;
    }

    public void setSet(Set set) {
        this.set = set;
    }

    public LinkedHashSet getLinkedHashSet() {
        return linkedHashSet;
    }

    public void setLinkedHashSet(LinkedHashSet linkedHashSet) {
        this.linkedHashSet = linkedHashSet;
    }

    public HashSet getHashSet() {
        return hashSet;
    }

    public void setHashSet(HashSet hashSet) {
        this.hashSet = hashSet;
    }

    public TreeSet getTreeSet() {
        return treeSet;
    }

    public void setTreeSet(TreeSet treeSet) {
        this.treeSet = treeSet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JdkTypesBean that = (JdkTypesBean) o;

        if (arrayList != null ? !arrayList.equals(that.arrayList) : that.arrayList != null) return false;
        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (file != null ? !file.equals(that.file) : that.file != null) return false;
        if (hashMap != null ? !hashMap.equals(that.hashMap) : that.hashMap != null) return false;
        if (hashSet != null ? !hashSet.equals(that.hashSet) : that.hashSet != null) return false;
        if (hashtable != null ? !hashtable.equals(that.hashtable) : that.hashtable != null) return false;
        // special case: compare 'new HashMap(arg)' not 'arg'
        if (identityHashMap != null ? !new HashMap(identityHashMap).equals(new HashMap(that.identityHashMap)) : that.identityHashMap != null)
            return false;
        //
        if (inet4Address != null ? !inet4Address.equals(that.inet4Address) : that.inet4Address != null) return false;
        if (inet6Address != null ? !inet6Address.equals(that.inet6Address) : that.inet6Address != null) return false;
        if (inetAddress != null ? !inetAddress.equals(that.inetAddress) : that.inetAddress != null) return false;
        if (linkedHashSet != null ? !linkedHashSet.equals(that.linkedHashSet) : that.linkedHashSet != null)
            return false;
        if (linkedList != null ? !linkedList.equals(that.linkedList) : that.linkedList != null) return false;
        if (list != null ? !list.equals(that.list) : that.list != null) return false;
        if (map != null ? !map.equals(that.map) : that.map != null) return false;
        if (object != null ? !object.equals(that.object) : that.object != null) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        if (set != null ? !set.equals(that.set) : that.set != null) return false;
        if (stack != null ? !stack.equals(that.stack) : that.stack != null) return false;
        if (string != null ? !string.equals(that.string) : that.string != null) return false;
        if (treeMap != null ? !treeMap.equals(that.treeMap) : that.treeMap != null) return false;
        if (treeSet != null ? !treeSet.equals(that.treeSet) : that.treeSet != null) return false;
        if (vector != null ? !vector.equals(that.vector) : that.vector != null) return false;

        return true;
    }
}
