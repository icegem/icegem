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
package com.googlecode.icegem.serialization.codegen.impl;

import com.googlecode.icegem.serialization.codegen.XProperty;
import com.googlecode.icegem.serialization.codegen.impl.primitive.*;
import com.googlecode.icegem.serialization.codegen.impl.primitivearray.*;
import com.googlecode.icegem.serialization.codegen.impl.system.*;
import com.googlecode.icegem.serialization.codegen.impl.wrapper.*;
import java.util.*;

/**
 * Generate code that dispatched by 'SOME TYPE' and
 * 1) read 'SOME TYPE' field value from DataInput
 * 2) set it to bean
 * for method DataSerializer.fromData(...)
 *
 * @author igolovach
 */

public class FromDataFieldProcessor {
    private Map<Class<?>, FromDataProcessor> map = new HashMap<Class<?>, FromDataProcessor>();

    public FromDataFieldProcessor() { //todo: what if different CLs?
        // primitive
        map.put(boolean.class, new FromDataPrimitiveProcessor("readBoolean"));
        map.put(byte.class, new FromDataPrimitiveProcessor("readByte"));
        map.put(char.class, new FromDataPrimitiveProcessor("readChar"));
        map.put(short.class, new FromDataPrimitiveProcessor("readShort"));
        map.put(int.class, new FromDataPrimitiveProcessor("readInt"));
        map.put(long.class, new FromDataPrimitiveProcessor("readLong"));
        map.put(float.class, new FromDataPrimitiveProcessor("readFloat"));
        map.put(double.class, new FromDataPrimitiveProcessor("readDouble"));
        // wrapper
        map.put(Boolean.class, new FromDataFieldWrapperProcessor("Boolean", "readBoolean"));
        map.put(Byte.class, new FromDataFieldWrapperProcessor("Byte", "readByte"));
        map.put(Character.class, new FromDataFieldWrapperProcessor("Character", "readChar"));
        map.put(Short.class, new FromDataFieldWrapperProcessor("Short", "readShort"));
        map.put(Integer.class, new FromDataFieldWrapperProcessor("Integer", "readInt"));
        map.put(Long.class, new FromDataFieldWrapperProcessor("Long", "readLong"));
        map.put(Float.class, new FromDataFieldWrapperProcessor("Float", "readFloat"));
        map.put(Double.class, new FromDataFieldWrapperProcessor("Double", "readDouble"));
        // primitive[]
        map.put(boolean[].class, new FromDataFieldPrimitiveArrayProcessor("readBooleanArray"));
        map.put(byte[].class, new FromDataFieldPrimitiveArrayProcessor("readByteArray"));
        map.put(short[].class, new FromDataFieldPrimitiveArrayProcessor("readShortArray"));
        map.put(char[].class, new FromDataFieldPrimitiveArrayProcessor("readCharArray"));
        map.put(int[].class, new FromDataFieldPrimitiveArrayProcessor("readIntArray"));
        map.put(long[].class, new FromDataFieldPrimitiveArrayProcessor("readLongArray"));
        map.put(float[].class, new FromDataFieldPrimitiveArrayProcessor("readFloatArray"));
        map.put(double[].class, new FromDataFieldPrimitiveArrayProcessor("readDoubleArray"));
        // system
        map.put(String.class, new FromDataFieldStringProcessor());
        map.put(Date.class, new FromDataFieldDateProcessor());
        map.put(Calendar.class, new FromDataFieldCalendarProcessor());
    }

    public String process(XProperty field) {
        final Class<?> fieldClass = field.getType();

        // predefined
        if (map.get(fieldClass) != null) {
            return map.get(fieldClass).process(field);
        }

        // concrete enum class (not Enum)
        if (Enum.class.isAssignableFrom(fieldClass) && fieldClass != Enum.class) {
            return new FromDataFieldConcreteEnumProcessor().process(field);
        }

        return new FromDataFieldResolveClassByGemFireProcessor().process(field);
    }
}
