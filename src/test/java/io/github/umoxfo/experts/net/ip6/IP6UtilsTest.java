/*
 * Copyright (c) 2017. Makoto Sakaguchi
 * This file is part of Experts Net.
 *
 * Experts Net is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Experts Net is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.umoxfo.experts.net.ip6;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IP6UtilsTest {
	@Test
	void createEUI64() {
	}

	@Test
	void toTextFormat() {
		final byte[] address = {(byte) 0x20, 0x01, 0x0d, (byte) 0xb8, 0x00, 0x00, 0x00, 0x00, 0x12, 0x34, 0x00, 0x00, 0x00, 0x00,
		                        (byte) 0x9a, (byte) 0xbc};

		assertAll(() -> assertEquals("2001:db8::1234:0:0:9abc", IP6Utils.toTextFormat(address)),
		          () -> {
			          address[4] = 0x1;
			          assertEquals("2001:db8:100:0:1234::9abc", IP6Utils.toTextFormat(address));
		          });
	}//toTextFormat
}
