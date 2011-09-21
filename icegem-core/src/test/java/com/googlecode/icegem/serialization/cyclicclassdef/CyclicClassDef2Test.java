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
package com.googlecode.icegem.serialization.cyclicclassdef;

import java.io.InvalidClassException;

import javassist.CannotCompileException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.icegem.serialization.HierarchyRegistry;
import com.googlecode.icegem.serialization.primitive.TestParent;
import static org.junit.Assert.*;

/**
 * @author igolovach
 */

public class CyclicClassDef2Test extends TestParent {

    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(), CyclicClassDef2BeanA.class, CyclicClassDef2BeanB.class);
    }

    @Test
    public void testWithNullField() {
        // create test bean
        CyclicClassDef2BeanA expected = new CyclicClassDef2BeanA();

        // Serialize / Deserialize
        CyclicClassDef2BeanA actual = (CyclicClassDef2BeanA) serializeAndDeserialize(expected);

        // assert Company correct
        assertEquals(actual.getData(),expected.getData());
        assertNull(actual.getNext());
    }

    @Test
    public void test() {
        // create test bean
        CyclicClassDef2BeanA expectedA0 = new CyclicClassDef2BeanA();
        CyclicClassDef2BeanB expectedB0 = new CyclicClassDef2BeanB();
        CyclicClassDef2BeanA expectedA1 = new CyclicClassDef2BeanA();
        CyclicClassDef2BeanB expectedB1 = new CyclicClassDef2BeanB();
        expectedA0.setData(111);
        expectedA1.setData(222);
        expectedB0.setData(333);
        expectedB1.setData(444);
        expectedA0.setNext(expectedB0);
        expectedB0.setNext(expectedA1);
        expectedA1.setNext(expectedB1);

        // Serialize / Deserialize
        CyclicClassDef2BeanA actualA0 = (CyclicClassDef2BeanA) serializeAndDeserialize(expectedA0);
        CyclicClassDef2BeanB actualB0 = actualA0.getNext();
        CyclicClassDef2BeanA actualA1 = actualB0.getNext();
        CyclicClassDef2BeanB actualB1 = actualA1.getNext();

        // assert Company correct
        assertEquals(actualA0.getData(),expectedA0.getData());
        assertEquals(actualA1.getData(),expectedA1.getData());
        assertEquals(actualB0.getData(),expectedB0.getData());
        assertEquals(actualB1.getData(),expectedB1.getData());
        assertEquals(actualA0.getNext(),actualB0);
        assertEquals(actualA1.getNext(),actualB1);
        assertEquals(actualB0.getNext(),actualA1);
        assertNull(actualB1.getNext());
    }
}
