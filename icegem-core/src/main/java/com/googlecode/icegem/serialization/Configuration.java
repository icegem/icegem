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


/**
 * Global configuration of icegem facilities.
 *
 * @author igolovach
 */

public class Configuration {
	private static boolean JAVA_SERIALIZATION_ENABLED = !Boolean.getBoolean("icegem.serialization.java.native.disabled");
	
	private static boolean DISTRIBUTE_DESERIALIZER_REGISTRATION = !Boolean.getBoolean("icegem.serialization.distribute.registration.disabled");
	
    public Configuration() {
    }

    /**
     * Current Configuration: loaded at framework startup.
     * You can cache it: don't reloaded, don't change at runtime.
     */
    public static Configuration get() {
        return new Configuration();
    }

    public boolean isJavaSerializationEnabled() {
        return JAVA_SERIALIZATION_ENABLED;
    }
    
    public void setJavaSerializationEnabled(boolean value) {
    	JAVA_SERIALIZATION_ENABLED = value;
    }
    
    public boolean isDeserializerRegistrationDistributed() {
    	return DISTRIBUTE_DESERIALIZER_REGISTRATION ;
    }
    
    public void setDeserializerRegistrationDistributed(boolean value) {
    	DISTRIBUTE_DESERIALIZER_REGISTRATION = value;
    }

    /**
     * All *DataSerializer-s will be created in this package.
     * @return root package
     */
    public String getDataSerializerPackage() {
        return "com.googlecode.icegem.serialization.$$$";
    }
}
