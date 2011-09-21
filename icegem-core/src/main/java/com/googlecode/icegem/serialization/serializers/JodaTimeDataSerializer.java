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
package com.googlecode.icegem.serialization.serializers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Method;

import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;

import com.gemstone.gemfire.DataSerializer;
import com.googlecode.icegem.SerializationID;

/**
 * User: akondratyev
 */
public class JodaTimeDataSerializer extends DataSerializer implements
		SerializationID {
	private static final int CUSTOM_TZ_ID = 63;
	private static final String[] TIMEZONE_IDS = new String[] { "UTC",
			"Europe/London", "America/New_York", "America/Chicago",
			"America/Los_Angeles", "Europe/Moscow", "Asia/Novosibirsk",
			"Asia/Tokyo", "GMT", "EST" };

	static {
		DataSerializer.register(JodaTimeDataSerializer.class);
	}

	public JodaTimeDataSerializer() {
	}

	@Override
	public Class<?>[] getSupportedClasses() {
		return new Class<?>[] { DateTime.class };
	}

	@Override
	public boolean toData(Object o, DataOutput dataOutput) throws IOException {
		if (o instanceof DateTime) {
			DateTime dt = (DateTime) o;
			dataOutput.writeLong(dt.getMillis());
			Chronology chronology = dt.getChronology();

			boolean customChronology = false;
			if (!chronology.getClass().getName()
					.equals(ISOChronology.class.getName())) {
				customChronology = true;
			}

			byte flags = 0;
			boolean customTimeZone = true;

			String timeZoneId = chronology.getZone().getID();
			for (byte i = 0; i < TIMEZONE_IDS.length; i++) {
				if (timeZoneId.equals(TIMEZONE_IDS[i])) {
					flags = i;
					customTimeZone = false;
					break;
				}
			}

			if (customTimeZone) {
				flags = CUSTOM_TZ_ID;
			}

			flags |= customChronology ? (1 << 7) : 0;
			dataOutput.write(flags);

			if (customChronology) {
				dataOutput.writeUTF(chronology.getClass().getName());
			}
			if (customTimeZone) {
				dataOutput.writeUTF(chronology.getZone().getID());
			}
			return true;
		}
		return false;
	}

	@Override
	public Object fromData(DataInput dataInput) throws IOException,
			ClassNotFoundException {
		long time = dataInput.readLong();
		byte flags = dataInput.readByte();

		boolean customChornology = ((flags & 0x80) != 0);
		byte timeZoneIndex = (byte) (flags & 0x7F);
		boolean customTimeZone = (timeZoneIndex == CUSTOM_TZ_ID);

		String chronologyClassName = null;
		DateTimeZone dateTimeZone;

		Chronology chronology;
		if (customChornology) {
			chronologyClassName = dataInput.readUTF();
		}
		
		if (customTimeZone) {
			dateTimeZone = DateTimeZone.forID(dataInput.readUTF());
		} else {
			if(timeZoneIndex >= TIMEZONE_IDS.length) {
				throw new IOException("Serialized form contains unknown TZ index");
			}
			
			String tzId = TIMEZONE_IDS[timeZoneIndex];
			dateTimeZone = DateTimeZone.forID(tzId);
		}

		if(chronologyClassName != null) {	
			Class<?> chronologyCls = Class.forName(chronologyClassName);
			try {
				
				
				Method factory = chronologyCls.getMethod("getInstance",
						new Class[] { DateTimeZone.class });

				chronology = (Chronology) factory.invoke(null,
						dateTimeZone);
			} catch (Exception e) {
				throw new RuntimeException("Failed to instantiate Joda Chronology");
			}
		} else {
			chronology = ISOChronology.getInstance(dateTimeZone);
		}

		return new DateTime(time, chronology);
	}

	@Override
	public int getId() {
		return JODA_TIME_DATA_SERIALIZER_ID;
	}
}
