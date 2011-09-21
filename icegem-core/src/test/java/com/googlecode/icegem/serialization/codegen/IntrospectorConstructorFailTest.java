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

import java.io.IOException;
import java.io.InvalidClassException;
import java.lang.reflect.Constructor;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author igolovach
 */
@RunWith(Parameterized.class)
public class IntrospectorConstructorFailTest {

	Class<?> clazz;
	
	public IntrospectorConstructorFailTest(Class<?> clazz) {
		this.clazz = clazz;
	}
	
    @Parameters
    public static Collection<Object[]> dataConstructorFail() {
        return Arrays.asList(new Object[][]{
                // no-arg
                new Object[]{Integer.class},
                // public
                new Object[]{ClassWithNoPublicConstructor.class},
                // throws exceptions
                new Object[]{ClassWithWithConstructorThrowsExceptions.class},
        });
    }

    @Test(expected = InvalidClassException.class)
    public void testCheckValidConstructor_fail() throws InvalidClassException {
        Constructor<?> c = Introspector.checkConstructorNoArg(clazz);
        Introspector.checkConstructorWithoutExceptions(c);
        Introspector.checkConstructorPublic(c);
    }

    @Test(expected = InvalidClassException.class)
    public void testCheckClassIsPublic_fail() throws InvalidClassException {
        Introspector.checkClassIsPublic(NonPublicClass.class);
    }

    private static class NonPublicClass {
    }

    public static class ClassWithNoPublicConstructor {
        protected ClassWithNoPublicConstructor() {
        }
    }

    public static class ClassWithWithConstructorThrowsExceptions {
        protected ClassWithWithConstructorThrowsExceptions() throws IOException {
        }
    }
}
