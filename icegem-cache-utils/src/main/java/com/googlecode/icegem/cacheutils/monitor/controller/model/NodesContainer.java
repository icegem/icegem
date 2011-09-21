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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.googlecode.icegem.cacheutils.common.Utils;
import com.googlecode.icegem.cacheutils.monitor.controller.event.NodeEvent;
import com.googlecode.icegem.cacheutils.monitor.controller.event.NodeEventHandler;
import com.googlecode.icegem.cacheutils.monitor.controller.event.NodeEventType;

/**
 * Container for the detected nodes.
 */
public class NodesContainer {
	private Map<String, Node> socketToNodeMap = new HashMap<String, Node>();
	private List<NodeEventHandler> handlers;

	public NodesContainer() {
		handlers = new ArrayList<NodeEventHandler>();
	}

	/**
	 * Finds the node by host and port.
	 * 
	 * @param host
	 *            - the host.
	 * @param port
	 *            - the port.
	 * @return - the found node, or null otherwise.
	 */
	public Node find(String host, int port) {
		return socketToNodeMap.get(Utils.toKey(host, port));
	}

	/**
	 * Finds the set of nodes by host.
	 * 
	 * @param host
	 *            - the host.
	 * @return - the found nodes.
	 */
	public Set<Node> find(String host) {
		Set<Node> resultSet = new HashSet<Node>();

		if (host != null) {
			for (Node node : socketToNodeMap.values()) {
				if (host.equals(node.getHost())) {
					resultSet.add(node);
				}
			}
		}

		return resultSet;
	}

	/**
	 * Gets all the nodes.
	 * 
	 * @return - all the nodes.
	 */
	public Set<Node> getAll() {
		return new HashSet<Node>(socketToNodeMap.values());
	}

	/**
	 * Gets all the nodes not in status DEAD.
	 * 
	 * @return - all the nodes not in status DEAD.
	 */
	public Set<Node> getAllNotDead() {
		Set<Node> allNotDeadNodesSet = new HashSet<Node>();

		for (Node node : socketToNodeMap.values()) {
			if (!NodeStatus.DEAD.equals(node.getStatus())) {
				allNotDeadNodesSet.add(node);
			}
		}

		return allNotDeadNodesSet;
	}

	/**
	 * Gets all the nodes in status DEAD.
	 * 
	 * @return - all the nodes in status DEAD.
	 */
	public Set<Node> getAllDead() {
		Set<Node> allNotDeadNodesSet = new HashSet<Node>();

		for (Node node : socketToNodeMap.values()) {
			if (NodeStatus.DEAD.equals(node.getStatus())) {
				allNotDeadNodesSet.add(node);
			}
		}

		return allNotDeadNodesSet;
	}

	/**
	 * Adds one more node to the container.
	 * 
	 * @param node
	 *            - the node.
	 */
	public void add(Node node) {
		socketToNodeMap.put(Utils.toKey(node), node);
		sendEvent(node, NodeEventType.ADDED);
	}

	/**
	 * Removes the specified node.
	 * 
	 * @param node
	 *            - the node.
	 */
	public void remove(Node node) {
		socketToNodeMap.remove(Utils.toKey(node));
		sendEvent(node, NodeEventType.REMOVED);
	}

	/**
	 * Marks the node as ALIVE in case of its status is not ALIVE.
	 * 
	 * @param node
	 *            - the node.
	 */
	public void markAsAlive(Node node) {
		if (!NodeStatus.ALIVE.equals(node.getStatus())) {
			socketToNodeMap.get(Utils.toKey(node)).markAsAlive();
			sendEvent(node, NodeEventType.MARKED_AS_ALIVE);
		}
	}

	/**
	 * Marks the node as DEAD in case of its status is not DEAD.
	 * 
	 * @param node
	 *            - the node.
	 */
	public void markAsDead(Node node) {
		if (!NodeStatus.DEAD.equals(node.getStatus())) {
			socketToNodeMap.get(Utils.toKey(node)).markAsDead();
			sendEvent(node, NodeEventType.MARKED_AS_DEAD);
		}
	}

	/**
	 * Adds one more instance of NodeEventHandler to the list of event handlers.
	 * 
	 * @param handler
	 *            - the handler.
	 */
	public void addNodeEventHandler(NodeEventHandler handler) {
		handlers.add(handler);
	}

	/**
	 * Sends the event to all the handlers.
	 * 
	 * @param node
	 *            - the event node.
	 * @param type
	 *            - the event type.
	 */
	private void sendEvent(Node node, NodeEventType type) {
		NodeEvent event = new NodeEvent(node, type);
		for (NodeEventHandler handler : handlers) {
			handler.handle(event);
		}
	}

}
