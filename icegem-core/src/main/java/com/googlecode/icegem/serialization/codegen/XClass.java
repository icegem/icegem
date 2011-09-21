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

import java.util.Collections;
import java.util.List;

import com.googlecode.icegem.serialization.AutoSerializable;
import com.googlecode.icegem.serialization.BeanVersion;

/**
 * Wrapper class for java.lang.Class + useful methods for code generation
 *
 * @author igolovach
 */

public class XClass {
    private final Class<?> clazz;
	private byte beanVersion;

    public XClass(Class<?> clazz) {
        this.clazz = clazz;
        
        BeanVersion annotation = getType().getAnnotation(BeanVersion.class);
        if (annotation != null) {
			beanVersion = annotation.value();
            // checks on positive value
            if (beanVersion < 1) {
                throw new RuntimeException("Value of annotation @BeanVersion must be positive, current value = " + beanVersion + " (class '" + clazz + "')");
            }
        } else {
            throw new RuntimeException("Class must be annotated with @BeanVersion: " + getType().getCanonicalName());
        }
    }

    public Class<?> getType() {
        return clazz;
    }

    
    /**
     * Returns properties in "natural" order, i.e. &quot;ORDER BY VERSION, DECLARING CLASS, NAME&quot; 
     * 
     * @return
     */
    public List<XProperty> getOrderedProperties() {
        final List<XProperty> result = Introspector.getProperties(clazz);
        Collections.sort(result, new XProperty.NaturalOrder());
        return result;
    }
    
    public byte getBeanVersion() {
        return beanVersion;
    }
    
    public byte getVersionHistoryLength() {
        AutoSerializable annotation = getType().getAnnotation(AutoSerializable.class);
		byte versionHistoryLength = annotation.versionHistoryLength();

        if (versionHistoryLength < 1) {
            throw new IllegalArgumentException("Version history length of annotation @AutoSerializable must be positive, current value = " + versionHistoryLength + " (class '" + clazz.getCanonicalName() + "')");
        }
        
        // At least the current version hash code is recorded to ensure proper match
        versionHistoryLength++;

        if(versionHistoryLength > getBeanVersion()) {
        	versionHistoryLength = getBeanVersion();
        }
        return versionHistoryLength;
    }

	public String getName() {
		return this.clazz.getName();
	}
	
    /**
     * Returns a hash code of class model for specified bean version based on fields that this model contains.
     *
     * @param version bean version to get metadata hashcode.
     * @return the hashcode (16bits)
     */
    public short getVersionModelHashCode(int version) {
    	List<XProperty> classFields = getOrderedProperties();
    	
        StringBuilder builder = new StringBuilder();
        for (XProperty field : classFields) {
            if (version == -1 || version >= field.getPropertyVersion()) {
                builder.append(field.getType()).append(field.getName());
            }
        }
        int hashCode = builder.toString().hashCode();
		return (short) ((hashCode & 0xFFFF ) ^ ((hashCode & 0xFFFF0000) >> 16)) ;
    }

}
