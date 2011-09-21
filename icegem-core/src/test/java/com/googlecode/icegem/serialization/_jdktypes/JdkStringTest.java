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
package com.googlecode.icegem.serialization._jdktypes;

import java.io.InvalidClassException;
import java.util.Collection;

import javassist.CannotCompileException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Arrays;
import com.googlecode.icegem.serialization.HierarchyRegistry;
import com.googlecode.icegem.serialization.primitive.TestParent;
import static org.junit.Assert.*;

/**
 * @author igolovach
 */
@RunWith(Parameterized.class)
public class JdkStringTest extends TestParent { //todo: what if field of type sql.Date/Time/Timestamp ?

	String string;
	
	public JdkStringTest(String string) {
		this.string = string;
	}
	
    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(getContextClassLoader(), _JdkTypesBean.class);
    }

    /**
     * Read about <a href="DataInput.html#modified-utf-8">modified UTF-8</a> format.
     */
    @Parameters
    public static Collection<Object[]> dataString() {
        return Arrays.asList(new Object[][]{
                // simple
                new Object[]{""},
                new Object[]{" "},
                new Object[]{"Hello World!"},
                // isolate 2 byte '0' in modified UTF-8
                new Object[]{nCopy('\u0000', 1)},
                new Object[]{nCopy('\u0000', 100)},
                new Object[]{nCopy('\u0000', 10000)},
                new Object[]{nCopy('\u0000', 100000)},
                // minimal 1-byte in modified UTF-8
                new Object[]{nCopy('\u0001', 1)},
                new Object[]{nCopy('\u0001', 100)},
                new Object[]{nCopy('\u0001', 10000)},
                new Object[]{nCopy('\u0001', 100000)},
                // maximal 1-byte in modified UTF-8
                new Object[]{nCopy('\u007F', 1)},
                new Object[]{nCopy('\u007F', 100)},
                new Object[]{nCopy('\u007F', 10000)},
                new Object[]{nCopy('\u007F', 100000)},
                // minimal 2-byte in modified UTF-8
                new Object[]{nCopy('\u0080', 1)},
                new Object[]{nCopy('\u0080', 100)},
                new Object[]{nCopy('\u0080', 10000)},
                new Object[]{nCopy('\u0080', 100000)},
                // maximal 2-byte in modified UTF-8
                new Object[]{nCopy('\u07FF', 1)},
                new Object[]{nCopy('\u07FF', 100)},
                new Object[]{nCopy('\u07FF', 10000)},
                new Object[]{nCopy('\u07FF', 100000)},
                // minimal 3-byte in modified UTF-8
                new Object[]{nCopy('\u0800', 1)},
                new Object[]{nCopy('\u0800', 100)},
                new Object[]{nCopy('\u0800', 10000)},
                new Object[]{nCopy('\u0800', 100000)},
                // maximal 3-byte in modified UTF-8
                new Object[]{nCopy('\uFFFF', 1)},
                new Object[]{nCopy('\uFFFF', 100)},
                new Object[]{nCopy('\uFFFF', 10000)},
                new Object[]{nCopy('\uFFFF', 100000)},
        });
    }
    
    @Test
    public void testString() {
        // create test bean
        _JdkTypesBean expected = new _JdkTypesBean();
        expected.setString(string);

        // Serialize / Deserialize
        _JdkTypesBean actual = serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.getString(), expected.getString());
    }

    private static String nCopy(char c, int n) { //todo: slow
        StringBuilder buff = new StringBuilder(n);
        for (int k = 0; k < n; k++) {
            buff.append(c);
        }
        return buff.toString();
    }
}

