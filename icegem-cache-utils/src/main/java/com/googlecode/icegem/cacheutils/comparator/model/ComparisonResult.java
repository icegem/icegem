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
package com.googlecode.icegem.cacheutils.comparator.model;

import java.util.HashSet;
import java.util.Set;

public class ComparisonResult {
	private Set<Object> extra = new HashSet<Object>();
	private Set<Object> missed = new HashSet<Object>();
	private Set<Object> different = new HashSet<Object>();

	public Set<Object> getExtra() {
		return extra;
	}

	public Set<Object> getMissed() {
		return missed;
	}

	public Set<Object> getDifferent() {
		return different;
	}

	public void addExtra(Object o) {
		extra.add(o);
	}

	public void addMissed(Object o) {
		missed.add(o);
	}

	public void addDifferent(Object o) {
		different.add(o);
	}

	public void addAllExtra(Set<Object> set) {
		extra.addAll(set);
	}

	public void addAllMissed(Set<Object> set) {
		missed.addAll(set);
	}

	public void addAllDifferent(Set<Object> set) {
		different.addAll(set);
	}

	public boolean isEmpty() {
		return extra.isEmpty() && missed.isEmpty() && different.isEmpty();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("extra = ").append(extra).append("\n");
		sb.append("missed = ").append(missed).append("\n");
		sb.append("different = ").append(different);

		return sb.toString();
	}
}
