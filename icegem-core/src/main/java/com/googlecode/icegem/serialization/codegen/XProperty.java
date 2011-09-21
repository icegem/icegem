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

import java.util.Comparator;

/**
 * Wrapper class for java.lang.reflect.Field + useful methods for code
 * generation
 * 
 * @author igolovach
 */

public class XProperty { // todo: check field is serialized? (like in XClass
							// constructor)

	public static class NaturalOrder implements Comparator<XProperty> {

		public int compare(XProperty o1, XProperty o2) {
			if (o1.propertyVersion < o2.propertyVersion) {
				return -1;
			} else if (o1.propertyVersion > o2.propertyVersion) {
				return 1;
			}

			String thisSignature = o1.getDeclaringClass() + ":"
					+ o1.getName();
			String otherSignature = o1.getDeclaringClass() + ":"
					+ o2.getName();

			return thisSignature.compareTo(otherSignature);
		}

	}

	private final String name;
	private final boolean isBoolean;
	private final Class<?> type;
	private final Class<?> declaringClass;
	private final int propertyVersion;

	public XProperty(String name, Class<?> type, Class<?> declaringClass,
			int propVersion, boolean aBoolean) {
		this.name = name;
		this.type = type;
		this.declaringClass = declaringClass;
		this.propertyVersion = propVersion;
		isBoolean = aBoolean;
	}

	public boolean isBoolean() {
		return isBoolean;
	}

	public int getPropertyVersion() {
		return propertyVersion;
	}

	public Class<?> getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Class<?> getDeclaringClass() {
		return declaringClass;
	}
}
