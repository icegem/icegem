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
package com.googlecode.icegem.serialization.versioning;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;

import javassist.CannotCompileException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.DataSerializer;
import com.googlecode.icegem.serialization.HierarchyRegistry;
import com.googlecode.icegem.serialization.primitive.TestParent;
import com.googlecode.icegem.serialization.versioning.beans.inheritance.v1.Son;
import com.googlecode.icegem.serialization.versioning.beans.manyVersions.v1.Car;
import com.googlecode.icegem.serialization.versioning.beans.modified.beanv1.Person;
import com.googlecode.icegem.serialization.versioning.beans.previousversion.beanv1.Company;
import com.googlecode.icegem.serialization.versioning.beans.singleversion.Dog;
import com.googlecode.icegem.serialization.versioning.beans.versionhistory.OldBean;
import com.googlecode.icegem.serialization.versioning.beans.versionhistory.v1.Keyboard;
import com.googlecode.icegem.serialization.versioning.beans.versionhistory.v1.Mouse;
import com.googlecode.icegem.serialization.versioning.beans.wrong.Bird;
import com.googlecode.icegem.serialization.versioning.beans.wrong.Cat;
import com.googlecode.icegem.serialization.versioning.beans.wrong.Computer;
import com.googlecode.icegem.serialization.versioning.beans.wrong.Fish;
import com.googlecode.icegem.serialization.versioning.beans.wrong.Pig;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v1.Man;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v1.Rabbit;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v1.Table;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v1.Woman;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v2.Bear;

/**
 * User: akondratyev
 * @author Andrey Stepanov aka standy
 */
public class SerializeForVersioningTest extends TestParent {
    @BeforeClass
    public static void register() throws InvalidClassException, CannotCompileException {
        HierarchyRegistry.registerAll(Thread.currentThread().getContextClassLoader(),
                Dog.class, Bear.class, Company.class, Son.class,
                Car.class, Person.class, Rabbit.class, Man.class,
                Woman.class, Table.class, Keyboard.class, Mouse.class,
                OldBean.class);
    }

    @Test
    public void serializeClassWithSingleVersion() throws IOException, CannotCompileException {
        Dog dog = new Dog("Rex");
        DataSerializer.writeObject(dog, new DataOutputStream(new FileOutputStream("dog.versionTest")));
    }

    @Test(expected = RuntimeException.class)
    public void serializeClassWithNegativeVersion() throws IOException, CannotCompileException {
        HierarchyRegistry.registerAll(getContextClassLoader(), Cat.class);
        Cat cat = new Cat("Murka");
        DataSerializer.writeObject(cat, new DataOutputStream(new FileOutputStream("cat.versionTest")));
    }

    @Test(expected = RuntimeException.class)
    public void serializeClassWithNegativeFiledVersion() throws IOException, CannotCompileException {
        HierarchyRegistry.registerAll(getContextClassLoader(), Bird.class);
        Bird bird = new Bird("Kesha");
        DataSerializer.writeObject(bird, new DataOutputStream(new FileOutputStream("bird.versionTest")));
    }

    @Test(expected = RuntimeException.class)
    public void serializeClassWithoutBeanVersionAnnotation() throws IOException, CannotCompileException {
        HierarchyRegistry.registerAll(getContextClassLoader(), Fish.class);
        Fish bird = new Fish("Fish");
        DataSerializer.writeObject(bird, new DataOutputStream(new FileOutputStream("fish.versionTest")));
    }

    @Test
    public void serializeClassWithNewVersion() throws IOException, CannotCompileException {
        Bear bear = new Bear();
        DataSerializer.writeObject(bear, new DataOutputStream(new FileOutputStream("bear.versionTest")));
    }

    @Test
    public void serializeSimpleCompany() throws IOException, CannotCompileException {
        Company company = new Company();
        company.setId(123);
        DataSerializer.writeObject(company, new DataOutputStream(new FileOutputStream("simpleCompany.versionTest")));
    }

    @Test
    public void serializeWithInheritance() throws CannotCompileException, IOException {
        Son son = new Son();
        son.setName("son's name");
        son.setAge(23);
        son.setBrothers(new ArrayList<Long>(Arrays.asList(4L, 3L, 5L, 1L)));
        DataSerializer.writeObject(son, new DataOutputStream(new FileOutputStream("son.versionTest")));
    }
    
    @Test
    public void serializeCarVersionOne() throws IOException {
        Car car = new Car("golf", "5");
        DataSerializer.writeObject(car, new DataOutputStream(new FileOutputStream("cars.versionTest")));
    }

    @Test
    public void serializePersonVersionOne() throws IOException {
        Person person = new Person(123);
        DataSerializer.writeObject(person, new DataOutputStream(new FileOutputStream("person.versionTest")));
    }

    @Test
    public void serializeClassVerisonOne() throws IOException, CannotCompileException {
        DataSerializer.writeObject(new Rabbit(), new DataOutputStream(new FileOutputStream("rabbit.versionTest")));
    }

    @Test
    public void serializeManClassVerisonOne() throws IOException, CannotCompileException {
        DataSerializer.writeObject(new Man(), new DataOutputStream(new FileOutputStream("man.versionTest")));
    }

    @Test
    public void serializeWomanClassVerisonOne() throws IOException, CannotCompileException {
        DataSerializer.writeObject(new Woman(), new DataOutputStream(new FileOutputStream("woman.versionTest")));
    }

    @Test
    public void serializeTableClassVerisonOne() throws IOException, CannotCompileException {
        DataSerializer.writeObject(new Table(), new DataOutputStream(new FileOutputStream("table.versionTest")));
    }

    @Test(expected = InvalidClassException.class)
    public void serializeClassWithoutAutoSerializableAnnotation() throws IOException, CannotCompileException {
        HierarchyRegistry.registerAll(getContextClassLoader(), Pig.class);
        DataSerializer.writeObject(new Pig(), new DataOutputStream(new FileOutputStream("pig.versionTest")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void serializeClassWithNegativeVersionHistoryLength() throws IOException, CannotCompileException {
        HierarchyRegistry.registerAll(getContextClassLoader(), Computer.class);
        DataSerializer.writeObject(new Computer(), new DataOutputStream(new FileOutputStream("computer.versionTest")));
    }

    @Test
    public void serializeKeyboardClassVersionOne() throws IOException, CannotCompileException {
        DataSerializer.writeObject(new Keyboard(), new DataOutputStream(new FileOutputStream("keyboard.versionTest")));
    }

    @Test
    public void serializeMouseClassVersionOne() throws IOException, CannotCompileException {
        DataSerializer.writeObject(new Mouse(), new DataOutputStream(new FileOutputStream("mouse.versionTest")));
    }

    @Test
    public void serializeOldBean() throws IOException {
        OldBean oldman = new OldBean();
        oldman.setS1("adsf");
        oldman.setS2("adsf");
        oldman.setS3("adsf");
        oldman.setS4("adsf");
        DataSerializer.writeObject(oldman, new DataOutputStream(new FileOutputStream("oldBean.versionTest")));
    }

}
