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


/**
 * @author igolovach
 */

public class CodeGenUtils {
    public static String TAB = "    ";

    public static String tab(String str) {
        return tab(1, str);
    }

    public static String tab(int tabCount, String str) {
        String[] arr = str.split("\n");
        StringBuilder result = new StringBuilder();
        for (String s : arr) {
            for (int i = 1; i <= tabCount; i++) {
                result.append(TAB);
            }
            result.append(s).append("\n");
        }
        if (!str.endsWith("\n")) {
            return result.substring(0, result.length() - 1);
        }
        return result.toString();
    }

    public static String firstLetterToUpperCase(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    public static String firstLetterToLowerCase(String fieldName) {
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

    /**
     * <p> Integer[][].class -> "Integer[][]"
     * <p> int[][].class -> "int[][]"
     * <p> Naive: Integer[][].class.getName() -> "[[Ljava.lang.Integer;"
     * <p> Naive: int[][].class.getName() -> "[[LI;"
     */
    public static String className(Class<?> clazz) { //todo: rename
        //todo: is that correct algorithm?
        final String naiveName = clazz.getName();
        if (!clazz.isArray()) { //todo: what about enum
            return naiveName;
        } else {
            int count = 0;
            String ending = "";
            while (clazz.isArray()) {
                count++;
                ending += "[]";
                clazz = clazz.getComponentType();
            }
            if (clazz.isPrimitive()) {
                String primitiveClassName;
                if (clazz == boolean.class) {
                    primitiveClassName = "boolean";
                } else if (clazz == byte.class) {
                    primitiveClassName = "byte";
                } else if (clazz == char.class) {
                    primitiveClassName = "char";
                } else if (clazz == short.class) {
                    primitiveClassName = "short";
                } else if (clazz == int.class) {
                    primitiveClassName = "int";
                } else if (clazz == long.class) {
                    primitiveClassName = "long";
                } else if (clazz == float.class) {
                    primitiveClassName = "float";
                } else if (clazz == double.class) {
                    primitiveClassName = "double";
                } else {
                    throw new RuntimeException("Never here! - You try to generate code for Void[]...[]: clazz = " + clazz);
                }
                return primitiveClassName + ending;
            } else {
                return naiveName.substring(count + 1, naiveName.length() - 1) + ending;
            }
        }
    }
}
