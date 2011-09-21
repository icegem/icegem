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

public class CyclicClassDef1Test extends TestParent {

    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(), CyclicClassDef1Bean.class);
    }

    @Test
    public void testWithNullField() {
        // create test bean
        CyclicClassDef1Bean expected = new CyclicClassDef1Bean();

        // Serialize / Deserialize
        CyclicClassDef1Bean actual = (CyclicClassDef1Bean) serializeAndDeserialize(expected);

        // assert Company correct
        assertEquals(actual.getData(), expected.getData());
        assertNull(actual.getNext());
    }

    @Test
    public void test()  {
        // create test bean
        CyclicClassDef1Bean expectedA = new CyclicClassDef1Bean();
        CyclicClassDef1Bean expectedB = new CyclicClassDef1Bean();
        CyclicClassDef1Bean expectedC = new CyclicClassDef1Bean();
        expectedA.setData(111);
        expectedA.setNext(expectedB);
        expectedB.setData(222);
        expectedB.setNext(expectedC);
        expectedC.setData(333);
        expectedC.setNext(null);

        // Serialize / Deserialize
        CyclicClassDef1Bean actualA = (CyclicClassDef1Bean) serializeAndDeserialize(expectedA);
        CyclicClassDef1Bean actualB = actualA.getNext();
        CyclicClassDef1Bean actualC = actualB.getNext();

        // assert Company correct
        assertEquals(actualA.getData(), expectedA.getData());
        assertEquals(actualB.getData(), expectedB.getData());
        assertEquals(actualC.getData(), expectedC.getData());
        assertNull(actualC.getNext());
    }
}
