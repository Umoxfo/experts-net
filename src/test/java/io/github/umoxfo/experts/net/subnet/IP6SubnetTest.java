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

package io.github.umoxfo.experts.net.subnet;

import io.github.umoxfo.experts.net.ip6.IP6Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IP6SubnetTest {
	private static final byte[] TEST_ADDRESS = {0x20, 0x1, 0xd, (byte) 0xb8, 0x3c, 0xd, 0x5b, 0x6d, 0x60, (byte) 0xb9, 0x4d,
	                                            (byte) 0x9e, (byte) 0xc3, (byte) 0xa5, 0x56, (byte) 0xc9};
	private static final int CIDR = 58;

	private static IP6Subnet ip6Subnet;

	@BeforeAll
	static void init() { ip6Subnet = new IP6Subnet(TEST_ADDRESS, CIDR); }

	@Test
	void testParseSimpleAddress() {
		assertAll(() -> assertEquals("2001:db8:3c0d:5b40::", ip6Subnet.getLowAddress()),
		          () -> assertEquals("2001:db8:3c0d:5b7f:ffff:ffff:ffff:ffff", ip6Subnet.getHighAddress()),
		          () -> assertEquals("1180591620717411303424", ip6Subnet.getAddressCount().toString()));
	}//testParseSimpleAddress

	@Nested
	@DisplayName("An address")
	class AddressRange {
		byte[] targetAddress = new byte[16];

		@BeforeEach
		void setUp() {
			System.arraycopy(TEST_ADDRESS, 0, targetAddress, 0, 8);

			byte[] ramAddr = new byte[8];
			new Random().nextBytes(ramAddr);
			System.arraycopy(ramAddr, 0, targetAddress, 8, 8);
		}//setUp

		@Test
		@DisplayName("is in range")
		void testInRange() { assertTrue(ip6Subnet.isInRange(targetAddress), IP6Utils.toTextFormat(targetAddress)); }

		@Test
		@DisplayName("is out of range")
		void testOutOfRange() {
			assertAll(() -> {
				          targetAddress[7] = 0x3f;

				          assertFalse(ip6Subnet.isInRange(targetAddress), "Below the low address");
			          },
			          () -> {
				          targetAddress[7] = (byte) 0x80;

				          assertFalse(ip6Subnet.isInRange(targetAddress), "Over the high address");
			          });
		}//isInRange
	}//AddressRange
}
