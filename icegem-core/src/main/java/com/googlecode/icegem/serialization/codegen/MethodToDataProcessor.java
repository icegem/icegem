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

import com.googlecode.icegem.serialization.codegen.impl.ToDataFieldProcessor;

/**
 * Code generator for DataSerializer method toData()
 *
 * @author igolovach
 */

public class MethodToDataProcessor {

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

        List<XProperty> props = xClass.getOrderedProperties();
        final String className = xClass.getType().getName();
        // method header
        builder.append("public boolean toData(Object obj, java.io.DataOutput out) throws java.io.IOException {\n")
                .append(tab("try {\n"))
                .append(tab(2, "// check arg is of correct type\n"))
                .append(tab(2, "if (obj.getClass() != " + className + ".class) {return false;}\n"));

        builder.append(tab(2, "// increment thread-local method-frame counter\n"))
                .append(tab(2, "if (com.googlecode.icegem.serialization.codegen.MethodFrameCounter.ENABLED) {\n"))
                .append(tab(3, "com.googlecode.icegem.serialization.codegen.MethodFrameCounter.enterFrame(\"" + className + "\");\n"))
                .append(tab(2, "}\n"));

        builder.append(tab(2, "// convert to concrete type\n"))
                .append(tab(2, className)).append(" concrete = (").append(className).append(") obj;\n");

        builder.append("\n");

        // write header version and version history lenght
        byte versionHistoryLength = xClass.getVersionHistoryLength();
        
        byte header = (byte) ((CONST.HEADER_VERSION << 4) | versionHistoryLength);
        
        builder.append(tab(2, "// write header version and version history lenght\n"));
        builder.append(tab(2, "out.writeByte(" + header + ");\n"));

        int beanVersion = xClass.getBeanVersion();
        
        builder.append(tab(2, "// bean version\n"))
        	.append(tab(2, "out.writeByte(" + beanVersion + ");\n"))
        	.append("\n");
        
        builder.append(tab(2, "// write class model control hash codes\n"));
        builder.append(tab(2, "VERSION_METADATA.writeAll(out);\n"));

        for (XProperty prop : props) {
            builder.append("\n");
            builder.append(tab(2, "// this." + prop.getName() + " -> byte[]\n")); //todo: can be name collision between parent/child fields
            builder.append(tab(2, new ToDataFieldProcessor().process(prop)));
        }
        builder.append("\n");

        // method end
        builder.append(tab(2, "return true;\n"))
                // ensure that exit frame will be called
                .append(tab("} finally {\n"))
                .append(tab(2, "if (com.googlecode.icegem.serialization.codegen.MethodFrameCounter.ENABLED) {\n"))
                .append(tab(3, "// decrement thread-local method-frame counter\n"))
                .append(tab(3, "com.googlecode.icegem.serialization.codegen.MethodFrameCounter.exitFrame(\"" + className + "\");\n"))
                .append(tab(2, "}\n"))

                .append(tab("}\n"))
                .append("}\n");

        return builder.toString();
    }

    private String processEnum(XClass element) {

        StringBuilder builder = new StringBuilder();

        final String className = element.getType().getName();
        // method header
        builder.append("public boolean toData(Object obj, java.io.DataOutput out) throws java.io.IOException {\n")
                .append(tab("// check arg is of correct type\n"))
                .append(tab("if (obj.getClass() != " + className + ".class) {return false;}\n"))
                .append(tab("// convert to concrete type\n"))
                .append(tab(className)).append(" concrete = (").append(className).append(") obj;\n");

        builder.append(tab("// write only 'name'\n"));
        builder.append(tab("out.writeUTF(concrete.name());\n")); //todo: int/short_int?

        // method end
        builder.append(tab("return true;\n"))
                .append("}\n");

        return builder.toString();
    }
}
