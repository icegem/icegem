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

import javassist.CannotCompileException;
import javassist.NotFoundException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.icegem.serialization.HierarchyRegistry;
import static org.junit.Assert.*;

/**
 * @author igolovach
 */
public class PrimitiveTest extends TestParent {

    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(getContextClassLoader(), PrimitiveBean.class);
    }

    @Test
    public void testPrimitiveBean() throws NotFoundException, CannotCompileException, IOException, ClassNotFoundException {
        // create test bean
        PrimitiveBean expected = producePrimitiveBean();

        // Serialize / Deserialize
        PrimitiveBean actual = serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.isBool(), expected.isBool());
        assertEquals(actual.getBool(), expected.getBool());
        assertEquals(actual.getByt(), expected.getByt());
        assertEquals(actual.getSh(), expected.getSh());
        assertEquals(actual.getCh(), expected.getCh());
        assertEquals(actual.getIn(), expected.getIn());
        assertEquals(actual.getL(), expected.getL());
        assertEquals(actual.getF(), expected.getF(), 0.001f);
        assertEquals(actual.getD(), expected.getD(), 0.001);
        assertEquals(actual.isB1(), expected.isB1());
        assertEquals(actual.isB2(), expected.isB2());
        assertEquals(actual.isB3(), expected.isB3());
    }

    private PrimitiveBean producePrimitiveBean() {
        PrimitiveBean result = new PrimitiveBean();

        result.setBool(true);
        result.setByt((byte) 1);
        result.setSh((short) 2);
        result.setCh((char) 3);
        result.setIn(4);
        result.setL(5);
        result.setF(666.666f);
        result.setD(777.777d);

        return result;
    }
}
