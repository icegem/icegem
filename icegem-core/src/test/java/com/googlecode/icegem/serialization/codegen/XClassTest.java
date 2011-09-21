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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;


public class XClassTest {

	@Test
	public void testGetOrderedProperties() {
		XClass xClazz = new XClass(SortOrderStub2.class);
		
		List<XProperty> props = xClazz.getOrderedProperties();
		
		assertEquals(5, props.size());
		
		assertPropertyEquals(props.get(0), 1, SortOrderStub1.class, "field1");
		assertPropertyEquals(props.get(1), 1, SortOrderStub2.class, "field4");
		assertPropertyEquals(props.get(2), 2, SortOrderStub1.class, "field2");
		assertPropertyEquals(props.get(3), 2, SortOrderStub2.class, "field5");
		assertPropertyEquals(props.get(4), 3, SortOrderStub1.class, "field3");
	}

	@Test
	public void testBeanVersion() {
		XClass xClazz = new XClass(SortOrderStub2.class);
		assertEquals(3, xClazz.getVersionHistoryLength());
	}
	
	@Test
	public void testVersionHistory() {
		XClass xClazz = new XClass(SortOrderStub2.class);
		assertEquals(3, xClazz.getVersionHistoryLength());
	}
	
	private void assertPropertyEquals(XProperty prop, int version,
			Class<?> declared, String name) {
		assertEquals(prop.getPropertyVersion(), version);
		assertEquals(prop.getDeclaringClass(), declared);
		assertEquals(prop.getName(), name);
	}

}
