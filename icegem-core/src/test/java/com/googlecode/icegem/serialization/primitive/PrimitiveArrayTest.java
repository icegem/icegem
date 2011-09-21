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
public class PrimitiveArrayTest extends TestParent {

    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(getContextClassLoader(), PrimitiveArrayBean.class);
    }
    
    PrimitiveArrayBean expected;
    
    public PrimitiveArrayTest(PrimitiveArrayBean expected) {
    	this.expected = expected;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                new Object[] {producePrimitiveArrayBean()},
                new Object[] {producePrimitiveArrayBeanFieldsZeroLength()},
        });
    }

    @Test
    public void testPrimitiveArrayBean() throws NotFoundException, CannotCompileException, IOException, ClassNotFoundException {
        // Serialize / Deserialize
        PrimitiveArrayBean actual = serializeAndDeserialize(expected);

        // assert
        assertTrue(Arrays.equals(actual.getBooleanArray(),expected.getBooleanArray()));
        assertTrue(Arrays.equals(actual.getByteArray(),expected.getByteArray()));
        assertTrue(Arrays.equals(actual.getShortArray(),expected.getShortArray()));
        assertTrue(Arrays.equals(actual.getCharArray(),expected.getCharArray()));
        assertTrue(Arrays.equals(actual.getIntArray(),expected.getIntArray()));
        assertTrue(Arrays.equals(actual.getLongArray(),expected.getLongArray()));
        assertTrue(Arrays.equals(actual.getFloatArray(),expected.getFloatArray()));
        assertTrue(Arrays.equals(actual.getDoubleArray(),expected.getDoubleArray()));
    }

    @Test
    public void testArrayBeanFieldIsNull() throws NotFoundException, CannotCompileException, IOException, ClassNotFoundException {
        // create test bean
        PrimitiveArrayBean expected = new PrimitiveArrayBean();

        // Serialize / Deserialize
        PrimitiveArrayBean actual = serializeAndDeserialize(expected);

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

    private static PrimitiveArrayBean producePrimitiveArrayBean() {
        final PrimitiveArrayBean result = new PrimitiveArrayBean();

        result.setBooleanArray(new boolean[]{true, false});
        result.setByteArray(new byte[]{0, -1, +2, -3, +4, -5, +6, -7, +8, -9});
        result.setShortArray(new short[]{0, -1, +2, -3, +4, -5, +6, -7, +8, -9});
        result.setCharArray(new char[]{0, 1, +2, 3, +4, 5, +6, 7, +8, 9});
        result.setIntArray(new int[]{0, -1, +2, -3, +4, -5, +6, -7, +8, -9});
        result.setLongArray(new long[]{0, -1, +2, -3, +4, -5, +6, -7, +8, -9});
        result.setFloatArray(new float[]{0, -1.1f, +2.2f, -3.3f, +4.4f, -5.5f, +6.6f, -7.7f, +8.8f, -9.9f});
        result.setDoubleArray(new double[]{0, -1.11d, +2.22d, -3.33d, +4.44d, -5.55d, +6.66d, -7.77d, +8.88d, -9.99d});

        return result;
    }

    private static PrimitiveArrayBean producePrimitiveArrayBeanFieldsZeroLength() {
        final PrimitiveArrayBean result = new PrimitiveArrayBean();

        result.setBooleanArray(new boolean[]{});
        result.setByteArray(new byte[]{});
        result.setShortArray(new short[]{});
        result.setCharArray(new char[]{});
        result.setIntArray(new int[]{});
        result.setLongArray(new long[]{});
        result.setFloatArray(new float[]{});
        result.setDoubleArray(new double[]{});

        return result;
    }
}
