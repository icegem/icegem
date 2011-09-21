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
package com.googlecode.icegem.serialization.collection;

import static org.junit.Assert.*;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import javassist.CannotCompileException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.googlecode.icegem.serialization.HierarchyRegistry;
import com.googlecode.icegem.serialization.primitive.TestParent;

/**
 * @author igolovach
 */

public class ListTest extends TestParent {

    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(getContextClassLoader(), ListBean.class);
    }

    @Test
    public void testNull() {
        // create test bean
        ListBean expected = new ListBean();

        // Serialize / Deserialize
        ListBean actual = (ListBean) serializeAndDeserialize(expected);

        // assert
        assertNull(actual.getArrayList());
        assertNull(actual.getLinkedList());
        assertNull(actual.getList());
    }

    @Test
    public void testEmpty() {
        // create test bean
        ListBean expected = new ListBean();
        expected.setArrayList(new ArrayList());
        expected.setLinkedList(new LinkedList());
        expected.setList(new ArrayList());

        // Serialize / Deserialize
        ListBean actual = (ListBean) serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.getArrayList().size(), 0);
        assertEquals(actual.getLinkedList().size(), 0);

        assertTrue(expected.getList().getClass().isAssignableFrom(actual.getList().getClass()));
        assertEquals(actual.getList().size(), 0);
    }

    @Test
    public void testWithData() {
        // create test bean
        ListBean expected = new ListBean();
        expected.setArrayList(new ArrayList());
        expected.getArrayList().add("Hello");
        expected.getArrayList().add(new Date());
        expected.setLinkedList(new LinkedList());
        expected.getArrayList().add("Hello-2");
        expected.getArrayList().add(new Date(123456789));
        expected.setList(new LinkedList());
        expected.getArrayList().add("Hello-3");
//        expected.getArrayList().add(Locale.US); //todo: uncomment

        // Serialize / Deserialize
        ListBean actual = (ListBean) serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.getArrayList(), expected.getArrayList());
        assertEquals(actual.getLinkedList(), expected.getLinkedList());
        assertEquals(actual.getList(), expected.getList());
    }

    @Test
    @Ignore
    //todo: test "new ArrayList/LinkedList/List() {}"
    //todo: test Locale
    public void testArrayListSubclass() {
        // create test bean
        ListBean expected = new ListBean();
        expected.setArrayList(new ArrayList() {
        });
        expected.getArrayList().add("Hello");
        expected.getArrayList().add(new Date() {
        });
        expected.setLinkedList(new LinkedList());
        expected.getArrayList().add("Hello-2");
        expected.getArrayList().add(new Date(123456789));
        expected.setList(new LinkedList() {
        });
        expected.getArrayList().add("Hello-3");
        expected.getArrayList().add(Locale.US);

        // Serialize / Deserialize
        ListBean actual = (ListBean) serializeAndDeserialize(expected);

        // as)sert
        assertTrue(actual.getArrayList() instanceof ArrayList);
        assertEquals(actual.getArrayList(), expected.getArrayList());
    }
}

