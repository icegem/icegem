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

@AutoSerializable(dataSerializerID = 14)
@BeanVersion(1)
public class WrapperBean {
    private Boolean boolean_;
    private Byte byte_;
    private Character character_;
    private Short short_;
    private Integer integer_;
    private Long long_;
    private Float float_;
    private Double double_;

    public Boolean getBoolean_() {
        return boolean_;
    }

    public void setBoolean_(Boolean boolean_) {
        this.boolean_ = boolean_;
    }

    public Byte getByte_() {
        return byte_;
    }

    public void setByte_(Byte byte_) {
        this.byte_ = byte_;
    }

    public Character getCharacter_() {
        return character_;
    }

    public void setCharacter_(Character character_) {
        this.character_ = character_;
    }

    public Short getShort_() {
        return short_;
    }

    public void setShort_(Short short_) {
        this.short_ = short_;
    }

    public Integer getInteger_() {
        return integer_;
    }

    public void setInteger_(Integer integer_) {
        this.integer_ = integer_;
    }

    public Long getLong_() {
        return long_;
    }

    public void setLong_(Long long_) {
        this.long_ = long_;
    }

    public Float getFloat_() {
        return float_;
    }

    public void setFloat_(Float float_) {
        this.float_ = float_;
    }

    public Double getDouble_() {
        return double_;
    }

    public void setDouble_(Double double_) {
        this.double_ = double_;
    }
}
