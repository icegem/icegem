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
package com.googlecode.icegem.serialization._enum;

import java.util.List;

import com.googlecode.icegem.serialization.AutoSerializable;

/**
 * @author igolovach
 */
@AutoSerializable(dataSerializerID = 102)
public class FieldEnumBean {
    private SimpleEnumBean simpleEnumBean;
    private ExtendedFinalEnumBean extendedFinalEnumBean;
    private ExtendedMutableEnumBean extendedMutableEnumBean;
    private Enum enumField;
    private Enum[] enumArray;
    private Object objectField;
    private Object[] objectArray;
    private SimpleEnumBean[] simpleEnumBeanArray;
    private List list;

    public SimpleEnumBean getSimpleEnumBean() {
        return simpleEnumBean;
    }

    public void setSimpleEnumBean(SimpleEnumBean simpleEnumBean) {
        this.simpleEnumBean = simpleEnumBean;
    }

    public ExtendedFinalEnumBean getExtendedFinalEnumBean() {
        return extendedFinalEnumBean;
    }

    public void setExtendedFinalEnumBean(ExtendedFinalEnumBean extendedFinalEnumBean) {
        this.extendedFinalEnumBean = extendedFinalEnumBean;
    }

    public ExtendedMutableEnumBean getExtendedMutableEnumBean() {
        return extendedMutableEnumBean;
    }

    public void setExtendedMutableEnumBean(ExtendedMutableEnumBean extendedMutableEnumBean) {
        this.extendedMutableEnumBean = extendedMutableEnumBean;
    }

    public Enum getEnumField() {
        return enumField;
    }

    public void setEnumField(Enum enumField) {
        this.enumField = enumField;
    }

    public Enum[] getEnumArray() {
        return enumArray;
    }

    public void setEnumArray(Enum[] enumArray) {
        this.enumArray = enumArray;
    }

    public Object getObjectField() {
        return objectField;
    }

    public void setObjectField(Object objectField) {
        this.objectField = objectField;
    }

    public Object[] getObjectArray() {
        return objectArray;
    }

    public void setObjectArray(Object[] objectArray) {
        this.objectArray = objectArray;
    }

    public SimpleEnumBean[] getSimpleEnumBeanArray() {
        return simpleEnumBeanArray;
    }

    public void setSimpleEnumBeanArray(SimpleEnumBean[] simpleEnumBeanArray) {
        this.simpleEnumBeanArray = simpleEnumBeanArray;
    }

    public List getList() {
        return list;
    }

    public void setList(List list) {
        this.list = list;
    }
}
