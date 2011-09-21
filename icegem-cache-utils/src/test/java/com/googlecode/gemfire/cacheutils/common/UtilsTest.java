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
package com.googlecode.gemfire.cacheutils.common;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import static org.junit.Assert.*;

import com.googlecode.icegem.cacheutils.common.Utils;
import com.googlecode.icegem.cacheutils.monitor.controller.model.Node;

public class UtilsTest {

	private static final String HOST = "127.0.0.1";
	private static final int PORT = 40404;

	@Test
	public void testToKeyByNode() {
		Node node = new Node(HOST, PORT, null);
		String key = Utils.toKey(node);

		assertNotNull(key);
		assertEquals(key, HOST + ":" + PORT);
	}

	@Test
	public void testToKeyByHostAndPort() {
		String key = Utils.toKey(HOST, PORT);

		assertNotNull(key);
		assertEquals(key, HOST + ":" + PORT);
	}

	@Test
	public void testIsSocketAlive() throws IOException {
		final int port = 54321;

		boolean socketAlive = Utils.isSocketAlive("127.0.0.1", port);
		assertFalse(socketAlive);

		ServerSocket serverSocket = new ServerSocket(port);

		socketAlive = Utils.isSocketAlive("127.0.0.1", port);
		assertTrue(socketAlive);

		serverSocket.close();

		socketAlive = Utils.isSocketAlive("127.0.0.1", port);
		assertFalse(socketAlive);
	}

	@Test
	public void testExecute() {
		final long delay = 10 * 1000;
		final long timeout = 1000;

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					sleep(delay);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		};

		long testStartTime = System.currentTimeMillis();
		Utils.execute(thread, timeout);
		long testFinishTime = System.currentTimeMillis();
		long delta = testFinishTime - testStartTime;

		assertTrue(delta < delay);
	}

	@Test
	public void testDateToStringByDate() {
		Date date = new Date();

		String actual = Utils.dateToString(date);

		assertNotNull(actual);

		String expected = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			.format(date);
		assertEquals(actual, expected);
	}

	@Test
	public void testDateToStringByLong() {
		long time = System.currentTimeMillis();

		String actual = Utils.dateToString(time);

		
		assertNotNull(actual);

		String expected = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
			.format(new Date(time));
		assertEquals(actual, expected);
	}

	@Test
	public void testCurrentDate() {
		String currentDate = Utils.currentDate();
		assertNotNull(currentDate);
	}
}
