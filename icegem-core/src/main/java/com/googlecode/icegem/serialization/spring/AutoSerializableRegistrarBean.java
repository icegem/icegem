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
package com.googlecode.icegem.serialization.spring;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.googlecode.icegem.serialization.AutoSerializable;
import com.googlecode.icegem.serialization.HierarchyRegistry;

/**
 * @author igolovach
 */
public class AutoSerializableRegistrarBean implements InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(AutoSerializableRegistrarBean.class.getName());

    private List<Class<?>> registeredClasses = new ArrayList<Class<?>>();
    private List<String> scanPackages = new ArrayList<String>();

    public void setRegisteredClasses(List<Class<?>> registeredClasses) {
        this.registeredClasses = registeredClasses;
    }

    public void setScanPackages(List<String> scanPackages) {
        this.scanPackages = scanPackages;
    }

    public void afterPropertiesSet() throws Exception {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); //todo: ok?
        registerClasses(classLoader);
    }

    private void registerClasses(ClassLoader classLoader) throws ClassNotFoundException {

        List<Class<?>> toRegister = new ArrayList<Class<?>>();

        for (String pack : scanPackages) {
            logger.debug("Scan {}.* for @AutoSerializable classes", pack);
            ClassPathScanningCandidateComponentProvider ppp = new ClassPathScanningCandidateComponentProvider(false);
            ppp.addIncludeFilter(new AnnotationTypeFilter(AutoSerializable.class));
            Set<BeanDefinition> candidateComponents = ppp.findCandidateComponents(pack);
            for (BeanDefinition beanDefinition : candidateComponents) {
                String className = beanDefinition.getBeanClassName();
                final Class<?> clazz = Class.forName(className);
                toRegister.add(clazz);
            }
        }

        toRegister.addAll(registeredClasses);
        logger.info("All classes that will be registered in GemFire: " + toRegister);

        try {
            HierarchyRegistry.registerAll(classLoader, toRegister);
        } catch (InvalidClassException e) {
            final String msg = "Some class from list " + toRegister + " is nor serializable. Cause: " + e.getMessage();
            throw new IllegalArgumentException(msg, e);
        } catch (CannotCompileException e) {
            final String msg = "Can't compile DataSerializer classes for some classes from list " + toRegister + ". Cause: " + e.getMessage();
            throw new IllegalArgumentException(msg, e);
        }
    }
}
