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

public class ObjectArrayTest extends TestParent {

    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(), ObjectArrayBean.class);
    }

    @Test
    public void testNull() {
        // create test bean
        ObjectArrayBean expected = new ObjectArrayBean();

        // Serialize / Deserialize
        ObjectArrayBean actual = (ObjectArrayBean) serializeAndDeserialize(expected);

        // assert
        assertNull(actual.getObjArr());
    }

    @Test(expected = RuntimeException.class) //NotSerializableException wrapped in RuntimeException by TestParent
    public void testObject(){
        // create test bean
        ObjectArrayBean expected = new ObjectArrayBean();
        expected.setObjArr(new Object[]{new Object()});

        // Serialize / Deserialize
        ObjectArrayBean actual = (ObjectArrayBean) serializeAndDeserialize(expected);
    }

    @Test
    public void testString(){
        // create test bean
        ObjectArrayBean expected = new ObjectArrayBean();
        final Object[] expectedArr = {null, "Hello!"};
        expected.setObjArr(expectedArr);

        // Serialize / Deserialize
        ObjectArrayBean actual = (ObjectArrayBean) serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.getObjArr().length,expectedArr.length);
    }

    @Test(expected = StackOverflowError.class)
    public void testCycle1() {
        // create test bean
        ObjectArrayBean expected = new ObjectArrayBean();
        expected.setObjArr(new Object[]{expected});

        // Serialize / Deserialize
        ObjectArrayBean actual = (ObjectArrayBean) serializeAndDeserialize(expected);
    }

    @Test(expected = StackOverflowError.class)
    public void testCycle2() {
        // create test bean
        ObjectArrayBean expectedA = new ObjectArrayBean();
        ObjectArrayBean expectedB = new ObjectArrayBean();
        expectedA.setObjArr(new Object[]{expectedB});
        expectedB.setObjArr(new Object[]{expectedA});

        // Serialize / Deserialize
        ObjectArrayBean actual = (ObjectArrayBean) serializeAndDeserialize(expectedA);
    }

    @Test
    public void testSequence(){
        // create test bean
        ObjectArrayBean expectedA = new ObjectArrayBean();
        ObjectArrayBean expectedB = new ObjectArrayBean();
        ObjectArrayBean expectedC = new ObjectArrayBean();
        expectedA.setObjArr(new Object[]{"Hi!", null, 123, expectedB});
        expectedB.setObjArr(new Object[]{expectedC, expectedC, expectedC});
        expectedC.setObjArr(new Object[0]);

        // Serialize / Deserialize
        ObjectArrayBean actualA = (ObjectArrayBean) serializeAndDeserialize(expectedA);

        // assert
        assertEquals(actualA.getObjArr()[0],expectedA.getObjArr()[0]);
        assertEquals(actualA.getObjArr()[1],expectedA.getObjArr()[1]);
        assertEquals(actualA.getObjArr()[2],expectedA.getObjArr()[2]);
        ObjectArrayBean actualB = (ObjectArrayBean) actualA.getObjArr()[3];
        ObjectArrayBean actualC0 = (ObjectArrayBean) actualB.getObjArr()[0];
        ObjectArrayBean actualC1 = (ObjectArrayBean) actualB.getObjArr()[1];
        ObjectArrayBean actualC2 = (ObjectArrayBean) actualB.getObjArr()[2];
        assertEquals(actualC0.getObjArr(),expectedC.getObjArr());
        assertEquals(actualC1.getObjArr(),expectedC.getObjArr());
        assertEquals(actualC2.getObjArr(),expectedC.getObjArr());
    }

    @Test
    public void testObjectArrayZeroLength() {
        // create test bean
        ObjectArrayBean expected = new ObjectArrayBean();
        Object[] expectedArr = new Object[0];
        expected.setObjArr(expectedArr);

        // Serialize / Deserialize
        ObjectArrayBean actual = (ObjectArrayBean) serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.getObjArr().length, expectedArr.length);
    }

    @Test
    public void testObjectArrayWithNulls() {
        // create test bean
        ObjectArrayBean expected = new ObjectArrayBean();
        Object[] expectedArr = new Object[]{null, null, null};
        expected.setObjArr(expectedArr);

        // Serialize / Deserialize
        ObjectArrayBean actual = (ObjectArrayBean) serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.getObjArr(), expectedArr);
    }

    @Test
    public void testManyDimensionalObjectArray() {
        // create test bean
        ObjectArrayBean expected = new ObjectArrayBean();
        Object[] expectedArr = new Object[][][]{
                new Object[][]{
                        new Object[]{"0", "1"}, new Object[]{"a", "b"}
                },
                new Object[][]{
                        new Object[]{"2", "3"}, new Object[]{"c", "d"}
                },
                new Object[][]{
                        new Object[]{"4", "5"}, new Object[]{"e", "f"}
                },
                new Object[][]{
                        new Object[]{"6", "7"}, new Object[]{"g", "h"}
                }
        };
        expected.setObjArr(expectedArr);

        // Serialize / Deserialize
        ObjectArrayBean actual = (ObjectArrayBean) serializeAndDeserialize(expected);

        // assert
        Object[][][] actualArr = (Object[][][])actual.getObjArr();
        assertEquals(actualArr, expectedArr);
    }
}

