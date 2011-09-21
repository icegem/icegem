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

import java.util.Set;

import org.junit.Test;

import com.googlecode.icegem.cacheutils.monitor.controller.event.NodeEvent;
import com.googlecode.icegem.cacheutils.monitor.controller.event.NodeEventHandler;

import static org.junit.Assert.*;

public class NodesContainerTest {

	private NodesContainer createNodesContainer() {
		NodesContainer container = new NodesContainer();

		Node aliveNode = new Node("127.0.0.1", 40404, null);
		aliveNode.markAsAlive();
		container.add(aliveNode);

		Node deadNode = new Node("127.0.0.1", 40405, null);
		deadNode.markAsDead();
		container.add(deadNode);
		container.add(new Node("192.168.1.100", 40404, null));

		return container;
	}

	@Test
	public void testFindByHostAndPort() {
		NodesContainer container = createNodesContainer();

		final String host = "127.0.0.1";
		final int port = 40405;

		Node node = container.find(host, port);

		assertNotNull(node);
		assertEquals(node.getHost(), host);
		assertEquals(node.getPort(), port);
	}

	@Test
	public void testFindByHost() {
		NodesContainer container = createNodesContainer();

		final String host = "127.0.0.1";

		Set<Node> nodesSet = container.find(host);

		assertNotNull(nodesSet);
		assertEquals(nodesSet.size(), 2);
		for (Node node : nodesSet) {
			assertEquals(node.getHost(), host);
		}
	}

	@Test
	public void testGetAll() {
		NodesContainer container = createNodesContainer();

		Set<Node> allNodesSet = container.getAll();

		assertNotNull(allNodesSet);
		assertEquals(allNodesSet.size(), 3);
	}

	@Test
	public void testGetAllNotDead() {
		NodesContainer container = createNodesContainer();

		Set<Node> notDeadNodesSet = container.getAllNotDead();

		assertNotNull(notDeadNodesSet); 
		assertEquals(notDeadNodesSet.size(), 2);
		for (Node node : notDeadNodesSet) {
			assertFalse(node.getStatus() == NodeStatus.DEAD);
		}
	}

	@Test
	public void testGetAllDead() {
		NodesContainer container = createNodesContainer();

		Set<Node> deadNodesSet = container.getAllDead();

		assertNotNull(deadNodesSet); 
		assertEquals(deadNodesSet.size(), 1);
		for (Node node : deadNodesSet) {
			assertEquals(node.getStatus(), NodeStatus.DEAD);
		}
	}

	@Test
	public void testAdd() {
		NodesContainer container = createNodesContainer();

		container.add(new Node("192.168.0.1", 40404, null));

		Set<Node> allNodesSet = container.getAll();

		assertNotNull(allNodesSet); 
		assertEquals(allNodesSet.size(), 4);
	}

	@Test
	public void testRemove() {
		NodesContainer container = createNodesContainer();

		Node node = container.getAll().iterator().next();
		container.remove(node);

		Set<Node> allNodesSet = container.getAll();

		assertNotNull(allNodesSet);
		assertEquals(allNodesSet.size(), 2);
	}

	@Test
	public void testMarkAsAlive() {
		NodesContainer container = createNodesContainer();

		for (Node node : container.getAll()) {
			container.markAsAlive(node);
		}

		Set<Node> allNodesSet = container.getAll();

		assertNotNull(allNodesSet);
		assertEquals(allNodesSet.size(), 3);
		for (Node node : allNodesSet) {
			assertEquals(node.getStatus(), NodeStatus.ALIVE);
		}
	}

	@Test
	public void testMarkAsDead() {
		NodesContainer container = createNodesContainer();

		for (Node node : container.getAll()) {
			container.markAsDead(node);
		}

		Set<Node> allNodesSet = container.getAll();

		assertNotNull(allNodesSet);
		assertEquals(allNodesSet.size(), 3);
		for (Node node : allNodesSet) {
			assertEquals(node.getStatus(), NodeStatus.DEAD);
		}
	}

	private class CountingNodeEventHandler implements NodeEventHandler {

		private int handledEventsCount = 0;

		public void handle(NodeEvent event) {
			handledEventsCount++;
		}

		public int getHandledEventsCount() {
			return handledEventsCount;
		}
	}

	@Test
	public void testAddNodeEventHandler() {
		NodesContainer container = createNodesContainer();

		CountingNodeEventHandler handler = new CountingNodeEventHandler();

		container.addNodeEventHandler(handler);

		Node node = new Node("192.168.0.1", 40404, null);
		container.add(node);
		container.markAsAlive(node);
		container.markAsDead(node);
		container.remove(node);

		assertEquals(handler.getHandledEventsCount(), 4);
	}

}
