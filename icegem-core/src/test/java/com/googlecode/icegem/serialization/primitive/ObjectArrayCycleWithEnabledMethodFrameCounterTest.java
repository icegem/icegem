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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.icegem.serialization.HierarchyRegistry;
import com.googlecode.icegem.serialization.codegen.MethodFrameCounter;

/**
 * @author Andrey Stepanov aka standy
 */
public class ObjectArrayCycleWithEnabledMethodFrameCounterTest extends TestParent {

    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // enable method frame counter by setting property
        MethodFrameCounter.ENABLED = true;
        // register
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(), ObjectArrayBean.class);
    }

    @AfterClass
    public static void after() {
        MethodFrameCounter.ENABLED = false;
    }

    @Test(expected = RuntimeException.class)
    public void testCycle1() {
        // create test bean
        ObjectArrayBean expected = new ObjectArrayBean();
        expected.setObjArr(new Object[]{expected});

        // Serialize / Deserialize
        serializeAndDeserialize(expected);
    }

    @Test(expected = RuntimeException.class)
    public void testCycle2() {
        // create test bean
        ObjectArrayBean expectedA = new ObjectArrayBean();
        ObjectArrayBean expectedB = new ObjectArrayBean();
        expectedA.setObjArr(new Object[]{expectedB});
        expectedB.setObjArr(new Object[]{expectedA});

        // Serialize / Deserialize
        serializeAndDeserialize(expectedA);
    }
}
