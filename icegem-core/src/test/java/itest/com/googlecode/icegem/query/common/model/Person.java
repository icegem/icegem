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
package itest.com.googlecode.icegem.query.common.model;

import java.io.Serializable;
import java.util.List;

/**
 * Simple domain model class for tests.
 *
 * @author Andrey Stepanov aka standy
 */
public class Person implements Serializable {
    /** Field serialVersionUID  */
    private static final long serialVersionUID = -930346814776120969L;

    /** Field socialNumber  */
    private int socialNumber;

    /** Field children  */
    private List<String> children;

    /**
     * Constructor Person creates a new Person instance.
     *
     * @param socialNumber of type String
     * @param children of type List<String>
     */
    public Person(int socialNumber, List<String> children) {
	this.socialNumber = socialNumber;
	this.children = children;
    }

    /**
     * Method getSocialNumber returns the socialNumber of this Person object.
     *
     * @return the socialNumber (type String) of this Person object.
     */
    public int getSocialNumber() {
	return socialNumber;
    }

    /**
     * Method setSocialNumber sets the socialNumber of this Person object.
     *
     * @param socialNumber the socialNumber of this Person object.
     *
     */
    public void setSocialNumber(int socialNumber) {
	this.socialNumber = socialNumber;
    }

    /**
     * Method getChildren returns the children of this Person object.
     *
     * @return the children (type List<String>) of this Person object.
     */
    public List<String> getChildren() {
	return children;
    }

    /**
     * Method setChildren sets the children of this Person object.
     *
     * @param children the children of this Person object.
     *
     */
    public void setChildren(List<String> children) {
	this.children = children;
    }

    /**
     * Method toString.
     * @return String
     */
    @Override
    public String toString() {
	return socialNumber + " : " + children;
    }

    @Override
    public boolean equals(Object o) {
	if (this == o)
	    return true;
	if (!(o instanceof Person))
	    return false;

	Person person = (Person) o;

	if (socialNumber != person.socialNumber)
	    return false;
	if (children != null ? !children.equals(person.children) : person.children != null)
	    return false;

	return true;
    }

    @Override
    public int hashCode() {
	int result = socialNumber;
	result = 31 * result + (children != null ? children.hashCode() : 0);
	return result;
    }
}
