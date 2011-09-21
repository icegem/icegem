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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * This is optimized version of map to use during de/serialization for metadata validation.
 * 
 * @author Alexey Kharlamov <aharlamov@gmail.com>
 *
 */
public class VersionMap {
	
	private short[] hashCodes;
	
	private byte baseVersion;
	
	private String className;
	
	public VersionMap(String className, int baseVersion, int size) {
		this.className = className;
		this.baseVersion = (byte) baseVersion;
		this.hashCodes = new short[size];
	}

	public void put(byte version, short hash) {
		int offset = baseVersion - version;
		if(offset < 0 || offset >= hashCodes.length) {
			throw new IllegalArgumentException("Version " + version + " is not supposed to be stored");
		}
		hashCodes[offset] = hash;
	}
	
	public void validate(byte version, short hash) {
		int offset = baseVersion - version;
		if(offset >= 0 && offset < hashCodes.length) {
			if(hash != this.hashCodes[offset]) {
				String message = String.format("Metadata of class %s version %s different in serializer and binary form. Check all " +
						"properties have @SinceVersion notification and no property have been deleted or mutated",
						this.className, version);
				throw new ClassCastException(message);
			}
		}
	}
	
	public void writeAll(DataOutput out) throws IOException {
		for(int i = 0; i < hashCodes.length; i++) {
			out.writeShort(hashCodes[i]);
		}
	}
	
	public void readAndCheck(DataInput in, byte actualVersion, byte len) throws IOException {
		for(int i = 0 ; i < len; i++) {
			short hashFromBinary = in.readShort();
			validate((byte) (actualVersion - i), hashFromBinary);
		}
	}
}
