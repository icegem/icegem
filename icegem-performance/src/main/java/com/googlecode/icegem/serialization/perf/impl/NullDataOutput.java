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
package com.googlecode.icegem.serialization.perf.impl;

import java.io.DataOutput;
import java.io.IOException;

/**
 * @author igolovach
 */

public class NullDataOutput implements DataOutput {

    public void write(int b) throws IOException {
        // NOP
    }

    public void write(byte[] b) throws IOException {
        // NOP
    }

    public void write(byte[] b, int off, int len) throws IOException {
        // NOP
    }

    public void writeBoolean(boolean v) throws IOException {
        // NOP
    }

    public void writeByte(int v) throws IOException {
        // NOP
    }

    public void writeShort(int v) throws IOException {
        // NOP
    }

    public void writeChar(int v) throws IOException {
        // NOP
    }

    public void writeInt(int v) throws IOException {
        // NOP
    }

    public void writeLong(long v) throws IOException {
        // NOP
    }

    public void writeFloat(float v) throws IOException {
        // NOP
    }

    public void writeDouble(double v) throws IOException {
        // NOP
    }

    public void writeBytes(String s) throws IOException {
        // NOP
    }

    public void writeChars(String s) throws IOException {
        // NOP
    }

    public void writeUTF(String s) throws IOException {
        // NOP
    }
}
