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
package com.googlecode.icegem.serialization._jdktypes;

import java.io.InvalidClassException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import javassist.CannotCompileException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.gemstone.bp.edu.emory.mathcs.backport.java.util.Arrays;
import com.googlecode.icegem.serialization.HierarchyRegistry;
import com.googlecode.icegem.serialization.primitive.TestParent;
import static org.junit.Assert.*;

/**
 * @author igolovach
 */
@RunWith(Parameterized.class)
public class JdkCalendarTest extends TestParent { //todo: what if field of type sql.Date/Time/Timestamp ?

	Calendar calendar;
	
	public JdkCalendarTest(Calendar calendar) {
		this.calendar = calendar;
	}
	
    @BeforeClass
    public static void before() throws InvalidClassException, CannotCompileException {
        // register
        HierarchyRegistry.registerAll(getContextClassLoader(), _JdkTypesBean.class);
    }

    @Parameters
    public static Collection<Object[]> dataCalendar() {
        //todo: more strange calendars?
        Calendar c0 = Calendar.getInstance();
        c0.setTimeInMillis(1234567890L);
        Calendar c1 = Calendar.getInstance(new Locale("th", "TH")); //todo: killer calendar
        c1.setTimeInMillis(1234567890L);
        Calendar c2 = Calendar.getInstance(new Locale("ja", "JP", "JP")); //todo: killer calendar
        c2.setTimeInMillis(1234567890L);

        return Arrays.asList(new Object[][]{
                new Object[]{c0},
                new Object[]{c1},
                new Object[]{c2},
        });
    }

    @Test //todo: enable
    @Ignore
    public void testCalendar() {
        // create test bean
        _JdkTypesBean expected = new _JdkTypesBean();
        expected.setCalendar(calendar);

        // Serialize / Deserialize
        _JdkTypesBean actual = serializeAndDeserialize(expected);

        // assert
        assertEquals(actual.getCalendar(), expected.getCalendar());
    }
}

