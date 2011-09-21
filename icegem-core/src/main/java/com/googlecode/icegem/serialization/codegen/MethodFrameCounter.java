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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This counter is a build-in mechanism for detecting circular references.
 * By default it is disabled. It can be enabled by using java property.
 * This property can be set via JVM parameters
 * -Dicegem.serialization.trace.methodframes=true
 * or from the code using the following command:
 * System.setProperty(MethodFrameCounter.SYSTEM_PROPERTY_NAME, "true");
 *
 * Analog of javassist.runtime.Cflow
 *
 * @author igolovach
 * @author Andrey Stepanov
 */
public class MethodFrameCounter extends ThreadLocal<AtomicInteger> {
    public final static String SYSTEM_PROPERTY_NAME = "icegem.serialization.trace.methodframes";
    public static boolean ENABLED = Boolean.getBoolean(SYSTEM_PROPERTY_NAME);

    public final static int MAX_STACK_DEPTH = 256;
    public final static String STACK_OVERFLOW_MSG = "Too deep method frame stack - " + MAX_STACK_DEPTH + ". Perhaps cyclic references in serialized object have been found.";

    private static final ThreadLocal<AtomicInteger> local = new ThreadLocal<AtomicInteger>() {
        protected synchronized AtomicInteger initialValue() {
            return new AtomicInteger(0);
        }
    };

    private static final ThreadLocal<List<String>> classNames = new ThreadLocal<List<String>>() {
        protected synchronized List<String> initialValue() {
            return new ArrayList<String>();
        }
    };

    /**
     * Increment the counter.
     * @param className name of class to enter.
     */
    public static void enterFrame(String className) {
        int counter = local.get().incrementAndGet();
        classNames.get().add(className);
        if (counter == MAX_STACK_DEPTH) {
            throw new RuntimeException(STACK_OVERFLOW_MSG + getClassNames());
        }
    }

    /**
     * Decrement the counter and remove class name from the list.
     * @param className name of class to exit from.
     */
    public static void exitFrame(String className) {
        int counter = local.get().decrementAndGet();
        if (counter < 0) {
            String errorMessage = "Method frame counter is less then 0. Some programming error: count(exitFrame) > count(enterFrame)."
                    + getClassNames();
            clearCounter();
            throw new RuntimeException(errorMessage);
        }
        String frameToExit = classNames.get().remove(classNames.get().size() - 1);
        if (!className.equals(frameToExit)) {
            throw new RuntimeException("Method frame counter try to exit from the class '" + className
                    + "' but must exit from the class '" + frameToExit + "' first." + getClassNames());
        }
    }


    /**
     * Clear the counter and list of class names.
     */
    private static void clearCounter() {
        local.get().set(0);
        classNames.get().clear();
    }

    /**
     * Creates string with all classes that have been entered by method frame counter.
     * @return String
     */
    private static String getClassNames() {
        StringBuilder result = new StringBuilder("\nMethod frame counter enter to the following classes:\n");
        for (String className : classNames.get()) {
            result.append(className).append("\n");
        }
        return result.toString();
    }
}
