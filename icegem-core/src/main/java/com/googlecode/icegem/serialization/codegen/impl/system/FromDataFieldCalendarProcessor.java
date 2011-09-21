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
package com.googlecode.icegem.serialization.codegen.impl.system;

import com.googlecode.icegem.serialization.codegen.XProperty;
import com.googlecode.icegem.serialization.codegen.impl.FromDataProcessor;

import java.util.GregorianCalendar;

import static com.googlecode.icegem.serialization.codegen.CodeGenUtils.firstLetterToUpperCase;
import static com.googlecode.icegem.serialization.codegen.CodeGenUtils.tab;

/**
 * Logic copied from Hessian: com.caucho.hessian.io.CalendarHandle
 *
 * @author igolovach
 */

//todo: if gregorian:
// todo: 1) new GregCalendar();
// todo: 2) let name be 'G'

// todo: what about other calendars (Yulian)?
public class FromDataFieldCalendarProcessor implements FromDataProcessor {
    public String process(XProperty field) {
        String fieldName = field.getName();
        return "if (in.readByte() != 0) {\n" +
                tab("String calendarClassName = in.readUTF();\n") +
                tab("java.util.Calendar newCalendar;\n") +

                tab("if (calendarClassName.equals(\"" + GregorianCalendar.class.getName() + "\")) {\n") +
                tab(tab("newCalendar = new java.util.GregorianCalendar();\n")) +
                tab("} else {\n") +
                tab(tab("try {\n")) +
                tab(tab(tab("Class calendarClass = Class.forName(in.readUTF());\n"))) +
                tab(tab(tab("newCalendar = (java.util.Calendar)calendarClass.newInstance();\n"))) +
                tab(tab("} catch (Throwable t) {\n")) +
                tab(tab(tab("throw new RuntimeException(\"Never here!\", t);\n"))) +
                tab(tab("}\n")) +
                tab("}\n") +

                tab("long timiInMillis = in.readLong();\n") +
                tab("newCalendar.setTimeInMillis(timiInMillis);\n") +
                tab("result.set" + firstLetterToUpperCase(fieldName) + "(newCalendar);\n") +
                "}\n";
    }
}
