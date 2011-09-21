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
package com.googlecode.icegem.serialization._inheritance.transientgetter;

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

public class TransientGetterTest extends TestParent {

    @BeforeClass
    public static void beforeClass() throws InvalidClassException, CannotCompileException {
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(), Bean.class, MarkedChildOfMarkedParent.class, MarkedChildOfNotMarkedParent.class, NotMarkedChildOfMarkedParent.class, NotMarkedChildOfNotMarkedParent.class);
    }

    @Test
    public void testEmpty() {
        final Bean expected = new Bean();

        // Serialize / Deserialize
        Bean actual = serializeAndDeserialize(expected);
    }

    @Test
    public void testMarkedParentMarkedChild() {
        final Bean expected = new Bean();
        expected.setMarkedParent(new MarkedChildOfMarkedParent());
        expected.getMarkedParent().setData(123);

        // Serialize / Deserialize
        Bean actual = serializeAndDeserialize(expected);

        // assert: type
        assertTrue(actual.getMarkedParent() instanceof MarkedChildOfMarkedParent);
        // assert: data
        assertEquals((actual.getMarkedParent().getData()), 0);
    }

    @Test
    public void testMarkedParentNotMarkedChild() {
        final Bean expected = new Bean();
        expected.setMarkedParent(new NotMarkedChildOfMarkedParent());
        expected.getMarkedParent().setData(123);

        // Serialize / Deserialize
        Bean actual = serializeAndDeserialize(expected);

        // assert: type
        assertTrue(actual.getMarkedParent() instanceof NotMarkedChildOfMarkedParent);
        // assert: data
        assertEquals(actual.getMarkedParent().getData(), expected.getMarkedParent().getData());
    }

    @Test
    public void testNotMarkedParentMarkedChild() {
        final Bean expected = new Bean();
        expected.setNotMarkedParent(new MarkedChildOfNotMarkedParent());
        expected.getNotMarkedParent().setData(123);

        // Serialize / Deserialize
        Bean actual = serializeAndDeserialize(expected);

        // assert: type
        assertTrue(actual.getNotMarkedParent() instanceof MarkedChildOfNotMarkedParent);
        // assert: data
        assertEquals(actual.getNotMarkedParent().getData(), 0);
    }

    @Test
    public void testNotMarkedParentNotMarkedChild() {
        final Bean expected = new Bean();
        expected.setNotMarkedParent(new NotMarkedChildOfNotMarkedParent());
        expected.getNotMarkedParent().setData(123);

        // Serialize / Deserialize
        Bean actual = serializeAndDeserialize(expected);

        // assert: type
        assertTrue(actual.getNotMarkedParent() instanceof NotMarkedChildOfNotMarkedParent);
        // assert: data
        assertEquals(actual.getNotMarkedParent().getData(),expected.getNotMarkedParent().getData());
    }


    @Test
    public void testTransient() throws Exception{
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(), ImplForInterfaceForTransient.class);

        Object obj = new ImplForInterfaceForTransient();
        serializeAndDeserialize(obj);
    }
}
