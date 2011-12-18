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
package itest.com.googlecode.icegem.cacheutils.signallistener;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.client.ClientCache;
import com.gemstone.gemfire.cache.client.ClientCacheFactory;
import com.googlecode.icegem.cacheutils.signallistener.WaitforTool;
import com.googlecode.icegem.utils.JavaProcessLauncher;
import com.googlecode.icegem.utils.PropertiesHelper;
import com.googlecode.icegem.utils.ServerTemplate;

/**
 * User: Artem Kondratev kondratevae@gmail.com
 */

public class SignalListenerTest {

    private static Process node;
    private static Region signalRegion;
    private static ClientCache clientCache;
    private static JavaProcessLauncher launcher = new JavaProcessLauncher();

    @BeforeClass
    public static void init() throws IOException, InterruptedException {
        node = launcher.runWithConfirmation("",
                ServerTemplate.class,
                new String[] {"-DgemfirePropertyFile=signalListener.properties"}, null);

        PropertiesHelper properties = new PropertiesHelper("/signalListener.properties");

        clientCache = new ClientCacheFactory()
                .set("cache-xml-file", "signal-client.xml")
                .set("log-level", properties.getStringProperty("log-level"))
                .set("license-file", properties.getStringProperty("license-file"))
                .set("license-type", properties.getStringProperty("license-type"))
                .create();

        signalRegion = clientCache.getRegion("signal-region");
        if (signalRegion == null)
            throw new NullPointerException("check your configuration, there is no \'signal-region\'");
        signalRegion.put("existedSignalKey", 0);
    }

    @Test
    public void signalAppeared() throws InterruptedException {
        assertTrue(WaitforTool.waitSignal(signalRegion, "existedSignalKey", 5000, 1000));
    }

    @Test
    public void signalTimeout() throws InterruptedException {
        assertFalse(WaitforTool.waitSignal(signalRegion, "absentSignalKey", 5000, 1000));

    }

    @AfterClass
    public static void close() throws IOException, InterruptedException {
        launcher.stopBySendingNewLineIntoProcess(node);
        if (clientCache != null)
            clientCache.close();
    }
}
