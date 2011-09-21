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
package com.googlecode.icegem.cacheutils.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * TODO: Create appropriate javadoc. 
 */
public class FileService {
    /**
     * @param filename
     * @param o
     * @throws IOException
     */
    public static void writeObject(String filename, Object o) throws IOException {
	delete(filename);

	OutputStream file = new FileOutputStream(filename);
	OutputStream buffer = new BufferedOutputStream(file);
	ObjectOutput output = new ObjectOutputStream(buffer);
	
	try {
	    output.writeObject(o);
	} finally {
	    output.close();
	}
    }

    /**
     * @param filename
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readObject(String filename) throws IOException, ClassNotFoundException {
	InputStream file = new FileInputStream(filename);
	InputStream buffer = new BufferedInputStream(file);
	ObjectInput input = new ObjectInputStream(buffer);

	Object o = null;

	try {
	    o = input.readObject();
	} finally {
	    input.close();
	    
	    delete(filename);
	}

	return o;
    }

    /**
     * @param filename
     */
    private static void delete(String filename) {
	File file = new File(filename);
	
	file.delete();
    }
}
