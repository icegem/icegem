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
package com.googlecode.icegem.serialization.primitive;

import com.googlecode.icegem.serialization.AutoSerializable;
import com.googlecode.icegem.serialization.BeanVersion;

/**
 * Bean for test purposes.
 * Contains fields of all types.
 *
 * @author igolovach
 */

@AutoSerializable(dataSerializerID = 12)
@BeanVersion(1)
public class PrimitiveBean {
    private boolean bool;
    private byte byt;
    private char ch;
    private short sh;
    private int in;
    private long l;
    private float f;
    private double d;
    private Boolean b1 = Boolean.FALSE;
    private Boolean b3 = Boolean.TRUE;
    private boolean b2 = true;


    public boolean isBool() {
        return bool;
    }

    public boolean getBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public byte getByt() {
        return byt;
    }

    public void setByt(byte byt) {
        this.byt = byt;
    }

    public char getCh() {
        return ch;
    }

    public void setCh(char ch) {
        this.ch = ch;
    }

    public short getSh() {
        return sh;
    }

    public void setSh(short sh) {
        this.sh = sh;
    }

    public int getIn() {
        return in;
    }

    public void setIn(int in) {
        this.in = in;
    }

    public long getL() {
        return l;
    }

    public void setL(long l) {
        this.l = l;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }

    public double getD() {
        return d;
    }

    public void setD(double d) {
        this.d = d;
    }

	public Boolean isB1() {
		return b1;
	}

	public void setB1(Boolean b1) {
		this.b1 = b1;
	}

	public boolean isB2() {
		return b2;
	}

	public void setB2(boolean b2) {
		this.b2 = b2;
	}

	public Boolean isB3() {
		return b3;
	}

	public void setB3(Boolean b3) {
		this.b3 = b3;
	}

    /*    public boolean getBoolean() {
        return bool;
    }

    public void setBoolean(boolean _boolean) {
        this.bool = _boolean;
    }

    public byte getByte() {
        return byt;
    }

    public void setByte(byte _byte) {
        this.byt = _byte;
    }

    public char getChar() {
        return ch;
    }

    public void setChar(char _char) {
        this.ch = _char;
    }

    public short getShort() {
        return sh;
    }

    public void setShort(short _short) {
        this.sh = _short;
    }

    public int getInt() {
        return in;
    }

    public void setInt(int _int) {
        this.in = _int;
    }

    public long getLong() {
        return l;
    }

    public void setLong(long _long) {
        this.l = _long;
    }

    public float getFloat() {
        return f;
    }

    public void setFloat(float _float) {
        this.f = _float;
    }

    public double getDouble() {
        return d;
    }

    public void setDouble(double _double) {
        this.d = _double;
    }*/
}
