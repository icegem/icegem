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
package com.googlecode.icegem;

import com.googlecode.icegem.serialization.serializers.JodaTimeDataSerializer;
import com.googlecode.icegem.serialization.serializers.TimestampDataSerializer;
import com.googlecode.icegem.serialization.serializers.UUIDDataSerializer;

/**
 * is a collection of DataSerializable class IDs used by ICEGEM library.
 * 
 * @author Alexey Kharlamov <aharlamov@gmail.com>
 *
 */
public interface SerializationID {
	/**
	 * Base offset of serialization IDs.
	 */
	int BASE = 16000;
	
    /**
	 * {@link JodaTimeDataSerializer}
	 */
    int JODA_TIME_DATA_SERIALIZER_ID = BASE + 1;

    /**
	 * {@link TimestampDataSerializer}
	 */
    int TIMESTAMP_DATA_SERIALIZER_ID = BASE + 2;

    /**
	 * {@link UUIDDataSerializer}
	 */
    int UUID_DATA_SERIALIZER_ID = BASE + 3;
}
