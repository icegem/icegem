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

import com.gemstone.gemfire.cache.client.Pool;
import com.googlecode.icegem.cacheutils.common.Utils;

/**
 * Represents the node - the set of information related to the instance of
 * GemFire cache server.
 */
public class Node {
	private String host;
	private int port;
	private Pool pool;
	private NodeStatus status;
	private long statusChangedAt = -1;

	public Node(String host, int port, Pool pool) {
		this.host = host;
		this.port = port;
		this.pool = pool;
		setStatus(NodeStatus.NEW);
	}

	public void markAsAlive() {
		setStatus(NodeStatus.ALIVE);
	}

	public void markAsDead() {
		setStatus(NodeStatus.DEAD);
	}

	private void setStatus(NodeStatus status) {
		if (!status.equals(this.status)) {
			this.status = status;
			statusChangedAt = System.currentTimeMillis();
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public Pool getPool() {
		return pool;
	}

	public NodeStatus getStatus() {
		return status;
	}

	public long getStatusChangedAt() {
		return statusChangedAt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port != other.port)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		sb.append(host).append(":").append(port).append(", ");
		// sb.append(pool).append(", ");
		sb.append(status).append(", ");
		sb.append(Utils.dateToString(statusChangedAt));
		sb.append("]");

		return sb.toString();
	}

}
