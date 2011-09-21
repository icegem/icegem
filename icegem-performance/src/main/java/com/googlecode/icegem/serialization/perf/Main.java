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
package com.googlecode.icegem.serialization.perf;

import com.googlecode.icegem.serialization.perf.impl.BeanFactory;
import com.googlecode.icegem.serialization.perf.impl.ExceptionalRunnable;
import com.googlecode.icegem.serialization.perf.impl.GemFireDeserializationRunnable;
import com.googlecode.icegem.serialization.perf.impl.GemFireSerializationRunnable;
import com.googlecode.icegem.serialization.perf.impl.JBossDeserializationRunnable;
import com.googlecode.icegem.serialization.perf.impl.JBossSerializationRunnable;
import com.googlecode.icegem.serialization.perf.impl.OOSDeserializationRunnable;
import com.googlecode.icegem.serialization.perf.impl.OOSSerializationRunnable;

import javassist.CannotCompileException;
import javassist.NotFoundException;

import java.io.IOException;
import java.io.InvalidClassException;

/**
 * @author igolovach
 */

public class Main {

    public static final int RUN_COUNT = 100000;
    public static final int WARM_COUNT = 10000;

    public static void main(String[] args) throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, IOException {

        System.out.println("--- FlatSlimPerfBean (" + RUN_COUNT + " serializations (Object -> byte[])) ---");
        testOneBeanSer(BeanFactory.createFlatSlim());
        System.out.println("--- FlatFatPerfBean (" + RUN_COUNT + " serializations (Object -> byte[])) ---");
        testOneBeanSer(BeanFactory.createFlatFat());

        System.out.println("--- FlatSlimPerfBean (" + RUN_COUNT + " DE-serializations (byte[] -> Object)) ---");
        testOneBeanDeser(BeanFactory.createFlatSlim());
        System.out.println("--- FlatFatPerfBean (" + RUN_COUNT + " DE-serializations (byte[] -> Object)) ---");
        testOneBeanDeser(BeanFactory.createFlatFat());

        System.out.println("--- FlatSlimPerfBean SIZE (Object -> byte[] -> length) ---");
        testOneBeanSize(BeanFactory.createFlatSlim());
        System.out.println("--- FlatFatPerfBean SIZE (Object -> byte[] -> length) ---");
        testOneBeanSize(BeanFactory.createFlatFat());
    }

    private static void testOneBeanSer(Object bean) throws InvalidClassException, CannotCompileException, InstantiationException, IllegalAccessException {
        final ExceptionalRunnable gemFireSer = new GemFireSerializationRunnable(bean);
        final ExceptionalRunnable oosSer = new OOSSerializationRunnable(bean);
        final ExceptionalRunnable jBossSer = new JBossSerializationRunnable(bean);

        testSpeed("GemFire", gemFireSer);
        testSpeed("JBoss", jBossSer);
        testSpeed("OOS", oosSer);
    }

    private static void testOneBeanDeser(Object bean) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException {
        final ExceptionalRunnable gemFireDeser = new GemFireDeserializationRunnable(bean);
        final ExceptionalRunnable oosDeser = new OOSDeserializationRunnable(bean);
        final ExceptionalRunnable jBossDeser = new JBossDeserializationRunnable(bean);

        testSpeed("GemFire", gemFireDeser);
        testSpeed("JBoss", jBossDeser);
        testSpeed("OOS", oosDeser);
    }

    private static void testOneBeanSize(Object bean) throws NotFoundException, CannotCompileException, InstantiationException, IllegalAccessException {
        final GemFireDeserializationRunnable gemFireDeser = new GemFireDeserializationRunnable(bean);
        final OOSDeserializationRunnable oosDeser = new OOSDeserializationRunnable(bean);
        final JBossDeserializationRunnable jBossDeser = new JBossDeserializationRunnable(bean);

        System.out.println("GemFire: " + gemFireDeser.size());
        System.out.println("JBoss: " + jBossDeser.size());
        System.out.println("OOS: " + oosDeser.size());
    }

    private static void testSpeed(String name, ExceptionalRunnable runnable) {
        runIt(runnable, WARM_COUNT);
        runIt(runnable, WARM_COUNT);
        runIt(runnable, WARM_COUNT);
        // real
        long dT = runIt(runnable, RUN_COUNT);
        System.out.println(name + ": " + dT / 1000000 + " ms");
    }

    public static long runIt(ExceptionalRunnable runnable, int counter) {
        int scaledCounter = counter / 10;

        long t0 = System.nanoTime();
        try {
            while (scaledCounter-- != 0) {
                runnable.run();
                runnable.run();
                runnable.run();
                runnable.run();
                runnable.run();
                //
                runnable.run();
                runnable.run();
                runnable.run();
                runnable.run();
                runnable.run();
            }
        } catch (Throwable t) {
            throw new RuntimeException("Never here!", t);
        }
        long t1 = System.nanoTime();

        return (t1 - t0);
    }
}
