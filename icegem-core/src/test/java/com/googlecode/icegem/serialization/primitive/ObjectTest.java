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
package com.googlecode.icegem.serialization.primitive;

import java.io.InvalidClassException;

import javassist.CannotCompileException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.icegem.serialization.HierarchyRegistry;
import static org.junit.Assert.*;

/**
 * @author igolovach
 */

public class ObjectTest extends TestParent {
    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(getContextClassLoader(), ObjectBean.class);
    }


    @Test
    public void testNull() {
        // create test bean
        ObjectBean expected = new ObjectBean();

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expected);

        // assert
        assertNull(actual.getObj());
    }

    @Test(expected = RuntimeException.class) //NotSerializableException wrapped in RuntimeException by TestParent
    public void testObject() {
        // create test bean
        ObjectBean expected = new ObjectBean();
        expected.setObj(new Object());

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expected);

        // assert
        assertNull(actual.getObj());
    }

    @Test
    public void testString() {
        // create test bean
        ObjectBean expected = new ObjectBean();
        expected.setObj("Hello!");

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expected);

        // assert
        assertTrue(actual.getObj() instanceof String);
        assertEquals(actual.getObj(), expected.getObj());
    }

    @Test
    public void testInteger(){
        // create test bean
        ObjectBean expected = new ObjectBean();
        expected.setObj(123456);

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expected);

        // assert
        assertTrue(actual.getObj() instanceof Integer);
        assertEquals(actual.getObj(), expected.getObj());
    }

    @Test(expected = StackOverflowError.class)
    public void testCycle1() {
        // create test bean
        ObjectBean expected = new ObjectBean();
        expected.setObj(expected);

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expected);
    }

    @Test(expected = StackOverflowError.class)
    public void testCycle2() {
        // create test bean
        ObjectBean expectedA = new ObjectBean();
        ObjectBean expectedB = new ObjectBean();
        expectedA.setObj(expectedB);
        expectedB.setObj(expectedA);

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expectedA);
    }

    @Test
    public void testSequence(){
        // create test bean
        ObjectBean expectedA = new ObjectBean();
        ObjectBean expectedB = new ObjectBean();
        ObjectBean expectedC = new ObjectBean();
        expectedA.setObj(expectedB);
        expectedB.setObj(expectedC);
        expectedC.setObj(null);

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expectedA);

        // assert
        assertTrue(actual.getObj() instanceof ObjectBean);
        assertTrue(((ObjectBean) actual.getObj()).getObj() instanceof ObjectBean);
        assertNull(((ObjectBean) ((ObjectBean) actual.getObj()).getObj()).getObj());
    }

    @Test
    public void testObjectArrayZeroLength() {
        // create test bean
        ObjectBean expected = new ObjectBean();
        Object[] expectedArr = new Object[0];
        expected.setObj(expectedArr);

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expected);

        // assert
        assertNotNull((Object[])actual.getObj());
        assertEquals(((Object[]) actual.getObj()).length, expectedArr.length);
    }

    @Test
    public void testObjectArrayWithNulls() {
        // create test bean
        ObjectBean expected = new ObjectBean();
        Object[] expectedArr = new Object[]{null, null, null};
        expected.setObj(expectedArr);

        // Serialize / Deserialize
        ObjectBean actual = (ObjectBean) serializeAndDeserialize(expected);

        // assert§
        assertTrue(actual.getObj() instanceof Object[]);
        assertEquals(((Object[]) actual.getObj()).length, expectedArr.length);
        assertEquals(((Object[]) actual.getObj()), expectedArr);
    }
}

