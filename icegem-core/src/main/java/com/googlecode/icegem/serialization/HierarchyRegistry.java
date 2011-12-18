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
package com.googlecode.icegem.serialization;

import java.io.InvalidClassException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gemstone.gemfire.DataSerializer;
import com.gemstone.gemfire.internal.InternalDataSerializer;
import com.googlecode.icegem.serialization.codegen.DataSerializerGenerator;
import com.googlecode.icegem.serialization.serializers.RegisteredDataSerializers;

/**
 * Responsibility: <p> 1) generate DataSerializers by
 * com.googlecode.icegem.serialization.codegen.DataSerializerGenerator.generateDataSerializerClasses(...) <p> 2)
 * register in GemFire DataSerializers by com.gemstone.gemfire.DataSerializer.register(...) <p> 3) filter enums
 */
public class HierarchyRegistry {

    private static Logger logger = LoggerFactory.getLogger(HierarchyRegistry.class);

    public static synchronized void registerAll(ClassLoader classLoader, Class<?>... classArray)
            throws InvalidClassException, CannotCompileException {
        registerAll(classLoader, Arrays.asList(classArray), null);
    }

    public static synchronized void registerAll(ClassLoader classLoader, List<Class<?>> classList)
            throws InvalidClassException, CannotCompileException {
        registerAll(classLoader, classList, null);
    }

    public static synchronized void registerAll(ClassLoader classLoader, List<Class<?>> classList, String outputDir)
            throws InvalidClassException, CannotCompileException {
        // solve problem when one class is registered two or more times
        List<Class<?>> classListToGenerate = new LinkedList<Class<?>>();
        for (Class<?> clazz : classList) {
            if (!uniqueClass.contains(clazz)) {
                classListToGenerate.add(clazz);
                uniqueClass.add(clazz);
            } else {
                logger.debug("Duplicate registration of class {}. Skipping...", clazz);
            }
        }
        // generate classes of DataSerializers
        List<Class<?>> serializerClassList = DataSerializerGenerator.generateDataSerializerClasses(
        		classLoader,
                classListToGenerate, 
                outputDir);

        // register classes of DataSerializers in GemFire
        for (Class<?> clazz : serializerClassList) {
            InternalDataSerializer.register(clazz, Configuration.get().isDeserializerRegistrationDistributed());
        }

        registerDataSerializers();
    }

    public static void registerDataSerializers() {
        for (Class<?> clazz : RegisteredDataSerializers.getDataSerializers()) {
            DataSerializer.register(clazz);
        }
    }

    private static final Set<Class<?>> uniqueClass = new HashSet<Class<?>>();
}
