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
package com.googlecode.icegem.serialization.codegen;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Arrays;
import com.googlecode.icegem.serialization.primitive.TestParent;
import static org.junit.Assert.*;

/**
 * @author igolovach
 */
@RunWith(Parameterized.class)
public class CodeGenUtilsTest extends TestParent {

	String name;
	Class<?> clazz; 
	
	public CodeGenUtilsTest(Class<?> clazz, String name) {
		this.clazz = clazz;
		this.name = name;
	}
	
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                // Integer
                new Object[] {Integer.class, "java.lang.Integer"},
                new Object[] {Integer[].class, "java.lang.Integer[]"},
                new Object[] {Integer[][].class, "java.lang.Integer[][]"},
                new Object[] {Integer[][][].class, "java.lang.Integer[][][]"},
                // Integer
                new Object[] {boolean.class, "boolean"},
                new Object[] {boolean[].class, "boolean[]"},
                new Object[] {boolean[][].class, "boolean[][]"},
                new Object[] {boolean[][][].class, "boolean[][][]"},
                // Integer
                new Object[] {byte.class, "byte"},
                new Object[] {byte[].class, "byte[]"},
                new Object[] {byte[][].class, "byte[][]"},
                new Object[] {byte[][][].class, "byte[][][]"},
                // Integer
                new Object[] {short.class, "short"},
                new Object[] {short[].class, "short[]"},
                new Object[] {short[][].class, "short[][]"},
                new Object[] {short[][][].class, "short[][][]"},
                // Integer
                new Object[] {char.class, "char"},
                new Object[] {char[].class, "char[]"},
                new Object[] {char[][].class, "char[][]"},
                new Object[] {char[][][].class, "char[][][]"},
                // Integer
                new Object[] {int.class, "int"},
                new Object[] {int[].class, "int[]"},
                new Object[] {int[][].class, "int[][]"},
                new Object[] {int[][][].class, "int[][][]"},
                // Integer
                new Object[] {long.class, "long"},
                new Object[] {long[].class, "long[]"},
                new Object[] {long[][].class, "long[][]"},
                new Object[] {long[][][].class, "long[][][]"},
                // Integer
                new Object[] {float.class, "float"},
                new Object[] {float[].class, "float[]"},
                new Object[] {float[][].class, "float[][]"},
                new Object[] {float[][][].class, "float[][][]"},
                // Integer
                new Object[] {double.class, "double"},
                new Object[] {double[].class, "double[]"},
                new Object[] {double[][].class, "double[][]"},
                new Object[] {double[][][].class, "double[][][]"},
        });
    }
    
    @Test
    public void testClassName() {
        assertEquals(CodeGenUtils.className(clazz),name);
        // #1
        assertEquals(CodeGenUtils.className(char[].class),"char[]");
        // #2
        assertEquals(CodeGenUtils.className(char[][].class),"char[][]");
        // #10
        assertEquals(CodeGenUtils.className(char[][][][][][][][][][].class),"char[][][][][][][][][][]");
    }
}
