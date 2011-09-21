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

import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Collection;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Arrays;
import com.googlecode.icegem.serialization.HierarchyRegistry;
import static org.junit.Assert.*;

/**
 * @author igolovach
 */
@RunWith(Parameterized.class)
public class PrimitiveMultidimensionalArrayTest extends TestParent {

    // ----------------------- SYSTEM
    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(), PrimitiveMultidimensionalArrayBean.class);
    }

	private PrimitiveMultidimensionalArrayBean expected;

    public PrimitiveMultidimensionalArrayTest(PrimitiveMultidimensionalArrayBean expected) {
    	this.expected = expected;
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                new Object[]{produceEmpty()},
                new Object[]{produceFullStructureButEmptyData()},
                new Object[]{producePrimitiveArrayBean()},
        });
    }

    // ----------------------- TESTS
    public void testPrimitiveArrayBean() throws NotFoundException, CannotCompileException, IOException, ClassNotFoundException {
        // Serialize / Deserialize
        PrimitiveMultidimensionalArrayBean actual = serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.getBooleanArray(), expected.getBooleanArray());
        assertEquals(actual.getByteArray(),expected.getByteArray());
        assertEquals(actual.getShortArray(),expected.getShortArray());
        assertEquals(actual.getCharArray(),expected.getCharArray());
        assertEquals(actual.getIntArray(),expected.getIntArray());
        assertEquals(actual.getLongArray(),expected.getLongArray());
        assertEquals(actual.getFloatArray(),expected.getFloatArray());
        assertEquals(actual.getDoubleArray(),expected.getDoubleArray());
    }

    @Test
    public void testArrayBeanFieldIsNull() throws NotFoundException, CannotCompileException, IOException, ClassNotFoundException {
        // create test bean
        PrimitiveMultidimensionalArrayBean expected = new PrimitiveMultidimensionalArrayBean();

        // Serialize / Deserialize
        PrimitiveMultidimensionalArrayBean actual = serializeAndDeserialize(expected);

        // assert
        assertNull(actual.getBooleanArray());
        assertNull(actual.getByteArray());
        assertNull(actual.getShortArray());
        assertNull(actual.getCharArray());
        assertNull(actual.getIntArray());
        assertNull(actual.getLongArray());
        assertNull(actual.getFloatArray());
        assertNull(actual.getDoubleArray());
    }

    // ----------------------- PRODUCERS
    private static PrimitiveMultidimensionalArrayBean producePrimitiveArrayBean() {
        final PrimitiveMultidimensionalArrayBean result = new PrimitiveMultidimensionalArrayBean();

        result.setCharArray(new char[][][]{null, new char[][]{}, new char[][]{null, new char[]{}, new char[]{'a', '\u0000', '\uFFFF'}}});

        return result;
    }

    private static PrimitiveMultidimensionalArrayBean produceEmpty() {
        final PrimitiveMultidimensionalArrayBean result = new PrimitiveMultidimensionalArrayBean();

        result.setBooleanArray(new boolean[][][]{});
        result.setByteArray(new byte[][][]{});
        result.setShortArray(new short[][][]{});
        result.setCharArray(new char[][][]{});
        result.setIntArray(new int[][][]{});
        result.setLongArray(new long[][][]{});
        result.setFloatArray(new float[][][]{});
        result.setDoubleArray(new double[][][]{});

        return result;
    }

    private static PrimitiveMultidimensionalArrayBean produceFullStructureButEmptyData() {
        final PrimitiveMultidimensionalArrayBean result = new PrimitiveMultidimensionalArrayBean();

        //todo: all strings the same?
        result.setBooleanArray(new boolean[][][]{new boolean[][]{new boolean[]{}, new boolean[]{}}, new boolean[][]{new boolean[]{}, new boolean[]{}}});
        result.setByteArray(new byte[][][]{new byte[][]{new byte[]{}, new byte[]{}}, new byte[][]{new byte[]{}, new byte[]{}}});
        result.setShortArray(new short[][][]{null});
        result.setCharArray(new char[][][]{new char[][]{new char[]{}, new char[]{}}, new char[][]{new char[]{}, new char[]{}}});
        result.setIntArray(new int[][][]{});
        result.setLongArray(new long[][][]{});
        result.setFloatArray(new float[][][]{});
        result.setDoubleArray(new double[][][]{});

        return result;
    }
}
