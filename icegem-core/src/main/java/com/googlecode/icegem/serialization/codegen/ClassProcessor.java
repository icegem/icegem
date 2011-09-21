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

import static com.googlecode.icegem.serialization.codegen.CodeGenUtils.tab;

/**
 * Code generator for DataSerializer-s:
 * 1) getId()
 * 2) getSupportedClasses()
 * 3) toData()
 * 4) fromData() 
 * @author igolovach
 */

public class ClassProcessor {

    public String process(XClass xClass, String serializerClsName) {
        StringBuilder builder = new StringBuilder();
        
        // class header
        builder.append("public class ")
                .append(xClass.getType().getSimpleName())
                .append("DataSerializer extends com.gemstone.gemfire.DataSerializer {\n");

        builder.append(tab("public static final com.googlecode.icegem.serialization.codegen.VersionMap VERSION_METADATA;"));
        
        builder.append(new StaticConstructorGenerator().process(xClass, serializerClsName));
        builder.append("}\n");
        
        // getId()
        builder.append("\n");
        builder.append(tab(new MethodGetIdProcessor().process(xClass)));

        // getSupportedClasses()
        builder.append("\n");
        builder.append(tab(new MethodGetSupportedClassesProcessor().process(xClass)));

        // toData()
        builder.append("\n");
        builder.append(tab(new MethodToDataProcessor().process(xClass)));

        // fromData()
        builder.append("\n");
        builder.append(tab(new MethodFromDataProcessor().process(xClass)));

        // class end
        builder.append("}");

        return builder.toString();
    }
}
