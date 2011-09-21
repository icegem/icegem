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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.util.Arrays;

import javassist.CannotCompileException;
import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gemstone.gemfire.DataSerializer;
import com.googlecode.icegem.serialization.HierarchyRegistry;
import com.googlecode.icegem.serialization.primitive.TestParent;
import com.googlecode.icegem.serialization.versioning.beans.inheritance.v2.Son;
import com.googlecode.icegem.serialization.versioning.beans.manyVersions.v3.Car;
import com.googlecode.icegem.serialization.versioning.beans.modified.beanv2.Person;
import com.googlecode.icegem.serialization.versioning.beans.previousversion.beanv2.Company;
import com.googlecode.icegem.serialization.versioning.beans.singleversion.Dog;
import com.googlecode.icegem.serialization.versioning.beans.versionhistory.OldBean;
import com.googlecode.icegem.serialization.versioning.beans.versionhistory.v2.Keyboard;
import com.googlecode.icegem.serialization.versioning.beans.versionhistory.v2.Mouse;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v1.Bear;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v2.Man;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v2.Rabbit;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v2.Table;
import com.googlecode.icegem.serialization.versioning.beans.wrong.v2.Woman;

/**
 * @author akondratyev
 * @author Andrey Stepanov aka standy
 * @author Alexey Kharlamov <aharlamov@gmail.com>
 */
public class XDeserializeForVersioningTest extends TestParent {
	@BeforeClass
	public static void before() throws InvalidClassException,
			CannotCompileException {
		HierarchyRegistry.registerAll(Thread.currentThread()
				.getContextClassLoader(), Dog.class, Bear.class, Company.class,
				Son.class, Car.class, Person.class, Rabbit.class, Man.class,
				Woman.class, Table.class, Keyboard.class, Mouse.class,
				OldBean.class);
	}

	@Test
	public void deserializeSingleVersion() throws IOException,
			CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("dog.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"dog.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);

		Dog dog = DataSerializer.readObject(new DataInputStream(byteArray));
		Assert.assertEquals("Rex", dog.getName());
	}

	@Test(expected = ClassCastException.class)
	public void deserializeNewVersionOfClassByOldVersion() throws IOException,
			CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("bear.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"bear.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);

		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@Test
	public void deserializePreviousVersion() throws IOException,
			CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("simpleCompany.versionTest")
				.length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"simpleCompany.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);

		Company c2 = DataSerializer.readObject(new DataInputStream(byteArray));
		assertEquals(c2.getId(), 123);
		assertNull(c2.getName());
	}

	@Test
	public void deserializeWithInheritance() throws IOException,
			ClassNotFoundException {
		byte[] buf = new byte[(int) new File("son.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"son.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);

		Son son = DataSerializer.readObject(new DataInputStream(byteArray));
		assertEquals(son.getId(), 0);
		assertEquals(son.getName(), "son's name");
		assertEquals(son.getAge(), 23);
		assertNull(son.getSisters());
		assertNull(son.getBirthday());
		assertNull(son.getChildren());
		assertEquals(son.getBrothers(), Arrays.asList(4L, 3L, 5L, 1L));
	}

	@Test
	public void deserializeCarVersionThree() throws IOException,
			ClassNotFoundException {
		byte[] buf = new byte[(int) new File("cars.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"cars.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);
		Car car = DataSerializer.readObject(new DataInputStream(byteArray));
		assertEquals(car.getModel(), "golf");
		assertEquals(car.getVersion(), "5");
		assertEquals(car.getSeatCount(), 4);
		assertTrue(car.isSedan());
	}

	@Test(expected = ClassCastException.class)
	public void deserializeWithNewClassModelAndOldBeanVersion()
			throws IOException, CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("person.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"person.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);
		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@Test(expected = ClassCastException.class)
	public void deserializeByNewVersionWithMissedFieldVersion()
			throws IOException, CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("rabbit.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"rabbit.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);

		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@Test(expected = ClassCastException.class)
	public void deserializeByNewVersionWithDeletedField() throws IOException,
			CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("man.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"man.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);

		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@Test(expected = ClassCastException.class)
	public void deserializeByNewVersionWithModifiedFieldVersion()
			throws IOException, CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("woman.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"woman.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);

		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@Test(expected = ClassCastException.class)
	public void deserializeByNewVersionWithModifiedFieldType()
			throws IOException, CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("table.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"table.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);

		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@Test(expected = IOException.class)
	public void deserializeByVersionTwoWithSmallHistoryVersion()
			throws IOException, CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("keyboard.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"keyboard.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);
		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@Test(expected = ClassCastException.class)
	public void deserializeByVersionThreeWithLookupOnSecondVersionModelClass()
			throws IOException, CannotCompileException, ClassNotFoundException {
		byte[] buf = new byte[(int) new File("mouse.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"mouse.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);
		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@Test
	public void deserializeOldBean() throws Exception {
		byte[] buf = new byte[(int) new File("oldBean.versionTest").length()];
		DataInputStream in = new DataInputStream(new FileInputStream(
				"oldBean.versionTest"));
		in.readFully(buf);
		in.close();

		ByteArrayInputStream byteArray = new ByteArrayInputStream(buf);
		DataSerializer.readObject(new DataInputStream(byteArray));
	}

	@AfterClass
	public static void deleteDataFile() {
		for (File file : new File(".").listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".versionTest"))
					return true;
				return false;
			}
		})) {
			file.delete();
		}
	}
}
