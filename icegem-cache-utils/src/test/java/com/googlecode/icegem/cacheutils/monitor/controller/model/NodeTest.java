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
package com.googlecode.icegem.cacheutils.monitor.controller.model;

import org.junit.Test;

import com.gemstone.gemfire.cache.client.Pool;
import com.googlecode.icegem.cacheutils.common.Utils;
import static org.junit.Assert.*;


public class NodeTest {

	private static final String HOST = "127.0.0.1";
	private static final int PORT = 40404;
	private static final Pool POOL = null;

	private Node createNode() {
		return new Node(HOST, PORT, POOL);
	}

	@Test
	public void testMarkAsAlive() {
		Node node = createNode();

		node.markAsAlive();

		assertEquals(node.getStatus(), NodeStatus.ALIVE);
	}

	@Test
	public void testMarkAsDead() {
		Node node = createNode();

		node.markAsDead();

		assertEquals(node.getStatus(), NodeStatus.DEAD);
	}

	@Test
	public void testGetHost() {
		Node node = createNode();

		assertEquals(node.getHost(), HOST);
	}

	@Test
	public void testGetPort() {
		Node node = createNode();

		assertEquals(node.getPort(), PORT);
	}

	@Test
	public void testGetPool() {
		Node node = createNode();

		assertNull(node.getPool());
	}

	@Test
	public void testGetStatus() {
		Node node = createNode();
		assertEquals(node.getStatus(), NodeStatus.NEW);

		node.markAsAlive();
		assertEquals(node.getStatus(), NodeStatus.ALIVE);

		node.markAsDead();
		assertEquals(node.getStatus(), NodeStatus.DEAD);
	}

	@Test
	public void testGetStatusChangedAt() {
		Node node = createNode();

		node.markAsAlive();
		long firstStatusChangedAt = node.getStatusChangedAt();

		node.markAsAlive();
		long secondStatusChangedAt = node.getStatusChangedAt();

		assertTrue(firstStatusChangedAt>-1);
		assertTrue(secondStatusChangedAt>-1);
		assertEquals(secondStatusChangedAt, firstStatusChangedAt);

		node.markAsDead();
		firstStatusChangedAt = node.getStatusChangedAt();

		node.markAsDead();
		secondStatusChangedAt = node.getStatusChangedAt();

		assertTrue(firstStatusChangedAt>-1);
		assertTrue(secondStatusChangedAt>-1);
		assertEquals(secondStatusChangedAt, firstStatusChangedAt);
	}

	@Test
	public void testToString() {
		Node node = createNode();

		assertEquals(node.toString(), 
			"[" + node.getHost() + ":" + node.getPort() + ", "
				+ node.getStatus() + ", "
				+ Utils.dateToString(node.getStatusChangedAt()) + "]");
	}
}
