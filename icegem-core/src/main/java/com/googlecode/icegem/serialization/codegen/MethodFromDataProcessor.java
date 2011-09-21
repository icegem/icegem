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

import java.util.List;

import com.googlecode.icegem.serialization.codegen.impl.FromDataFieldProcessor;

/**
 * Code generator for DataSerializer method fromData()
 *
 * @author igolovach
 */

public class MethodFromDataProcessor {

    public String process(XClass element) {
        if (Enum.class.isAssignableFrom(element.getType())) {
            if (element.getType() == Enum.class) {
                throw new RuntimeException("Never here!"); //todo: correct ex? more info?
            } else {
                return processEnum(element);
            }
        } else {
            return processNotEnum(element);
        }
    }

    private String processNotEnum(XClass xClass) {
        StringBuilder builder = new StringBuilder();

        List<XProperty> fields = xClass.getOrderedProperties();
        final String className = xClass.getType().getName();
        int currentBeanVersion = xClass.getBeanVersion();
        
        // method header
        builder.append("public Object fromData(java.io.DataInput in) throws java.io.IOException, ClassNotFoundException {\n");
        
        builder.append(tab("// checks header version\n"))
        		.append(tab("byte header = in.readByte();\n"))
        		.append(tab("byte actualHeaderVersion = (header & 0xF0) >> 4;\n"))
        		.append(tab("byte versionHistoryLength = (header & 0x0F);\n"))
                .append(tab("if (actualHeaderVersion !=" + CONST.HEADER_VERSION + ") {\n"))
                .append(tab(2, "throw new ClassCastException(\"Unknown binary header version: \" + actualHeaderVersion);\n"))
                .append(tab("}\n"))
                .append("\n");

        builder.append(tab("// checks bean version\n"))
                .append(tab("byte currentVersion = " + currentBeanVersion + "; \n"))
                .append(tab("byte actualVersion = in.readByte();\n"))
                .append(tab("if (currentVersion < actualVersion) {\n"))
                .append(tab(2, "throw new ClassCastException(\"Current bean version is less than serialized: "))
                .append("current is '\" + currentVersion + \"', actual is '\" + actualVersion + \"'\");\n")
                .append(tab("}\n"))
                .append("\n");

        
        builder.append(tab("VERSION_METADATA.readAndCheck(in, actualVersion, versionHistoryLength);\n"))
        		.append(tab("\n"));

        builder.append(tab("// create 'empty' bean\n"))
                .append(tab(className + " result = new " + className + "();\n")); 

        for (XProperty field : fields) {
            if (field.getPropertyVersion() > 1) {
                builder.append(tab("if (actualVersion == currentVersion || actualVersion >= " + field.getPropertyVersion() + ") {"));
                attachFieldReader(builder, field);
                builder.append(tab("}"));
            }   else {
                attachFieldReader(builder, field);
            }
        }
        builder.append("\n");

        // method end
        builder.append(tab("// return 'full' bean\n"));
        builder.append(tab("return result;\n"))
                .append("}\n");
        return builder.toString();
    }

    private static void attachFieldReader(StringBuilder builder, XProperty field) {
        builder.append("\n");
        builder.append(tab("// byte[] -> this." + field.getName() + "\n")); //todo: can be collision between parent/child field names
        builder.append(tab(new FromDataFieldProcessor().process(field)));
    }

    private String processEnum(XClass element) {
        StringBuilder builder = new StringBuilder();

        final String className = element.getType().getName();
        builder.append("public Object fromData(java.io.DataInput in) throws java.io.IOException, ClassNotFoundException {\n")
                .append(tab("// read only 'name'\n"))
                .append(tab("return Enum.valueOf(" + className + ".class, in.readUTF());\n")) //todo: need int / short_int
                .append("}\n");

        return builder.toString();
    }
}
