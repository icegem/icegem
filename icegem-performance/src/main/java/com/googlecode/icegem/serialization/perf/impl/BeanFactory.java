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
package com.googlecode.icegem.serialization.perf.impl;

/**
 * @author igolovach
 */

public class BeanFactory {

    public static FlatSlimPerfBean createFlatSlim() {
        final FlatSlimPerfBean result = new FlatSlimPerfBean();

        result.setId(1234567890);
        result.setName("Hello from bean! How are you doing?");

        return result;
    }

    public static FlatFatPerfBean createFlatFat() {
        final FlatFatPerfBean result = new FlatFatPerfBean();

        result.setD0(123.567d);
        result.setD1(123.567d);
        result.setD2(123.567d);

        result.setIp0(1);
        result.setIp0(12345);
        result.setIp0(1234567890);

        result.setLp0(1L);
        result.setLp1(1234567890L);
        result.setLp2(1234567890123456789L);

        result.setIw0(1);
        result.setIw0(12345);
        result.setIw0(1234567890);

        result.setLw0(1L);
        result.setLw1(1234567890L);
        result.setLw2(1234567890123456789L);

        result.setIntArr0(new int[]{1});
        result.setIntArr1(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
        result.setIntArr2(new int[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
        });

        result.setString0("0");
        result.setString1("1, 2, 3, 4, 5, 6, 7, 8, 9, 0,");
        result.setString2(
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0," +
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 0,"
        );

        return result;
    }
}
