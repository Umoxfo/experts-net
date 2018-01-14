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

import io.github.umoxfo.experts.net.subnet.address.IPAddress;
import io.github.umoxfo.experts.net.subnet.address.IPAddressFormatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class IPAddressTest {
	@Nested
	@DisplayName("IP address")
	class Address {
		private final byte[] IP4 = {(byte) 192, (byte) 168, 0, 1};
		private final byte[] IP6 = {0x20, 0x1, 0xd, (byte) 0xb8, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2, 0x0, 0x0, 0x0, 0x0, (byte) 0x9a,
		                            (byte) 0xbc};
		private final byte[] MAPPED_ADDRESS = {0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, (byte) 0xff, (byte) 0xff, (byte) 0xc0,
		                                       (byte) 0xa8, 0x0, 0x1};

		@Test
		@DisplayName("is Valid")
		void isValid() {
			assertAll(() -> assertArrayEquals(IP4, IPAddress.toNumericFormatAddress("192.168.0.1"), "IPv4 address"),
			          () -> assertArrayEquals(IP6, IPAddress.toNumericFormatAddress("2001:db8::2:0:0:9abc"), "IPv6 address"),
			          () -> assertArrayEquals(MAPPED_ADDRESS, IPAddress.toNumericFormatAddress("::ffff:192.168.0.1"),
			                                  "IPv4-mapped IPv6 address"));
		}//isValid

		@Test
		@DisplayName("is Invalid")
		void isNotValid() {
			assertAll(() -> assertThrows(IPAddressFormatException.class,
			                             () -> IPAddress.toNumericFormatAddress("ffff19216801"), "No dots and colons"),
			          () -> assertThrows(IPAddressFormatException.class,
			                             () -> IPAddress.toNumericFormatAddress("192.168.0.1:ffff:1"), "Dots before colons"),
			          () -> assertThrows(IPAddressFormatException.class,
			                             () -> IPAddress.toNumericFormatAddress("2001:db8::2::9abc"), "Invalid format"));
		}//isValid
	}//Address

	@Nested
	@DisplayName("IPv4 address")
	class IPv4Address {
		private Random random;

		@BeforeEach
		void init() { random = new Random(); }

		@Test
		@DisplayName("is Valid")
		void isValid() {
			String addr = random.ints(4, 0, 256)
			                    .mapToObj(Integer::toString).collect(joining("."));

			assertNotNull(IPAddress.toNumericFormatIPv4(addr));
		}//isValid

		@TestFactory
		@DisplayName("is Invalid")
		Stream<DynamicTest> isInValid() {
			String[] addr = random.ints(4, 256, Integer.MAX_VALUE)
			                      .mapToObj(Integer::toString).toArray(String[]::new);

			//@formatter:off
			return Stream.of(
				dynamicTest("No Dots", () -> assertThrows(IPAddressFormatException.class,
				                                          () -> IPAddress.toNumericFormatIPv4(String.join("", addr)))),
				dynamicTest("Out of Range", () -> assertThrows(IPAddressFormatException.class,
				                                               () -> IPAddress.toNumericFormatIPv4(String.join(".", addr)))),
				dynamicTest("Too many dots",
				            () -> assertThrows(IPAddressFormatException.class,
				                               () -> {
					                                String tmp = String.join(".", addr) + '.' + random.nextInt();
					                                IPAddress.toNumericFormatIPv4(tmp);
				                               }))
			);
			//@formatter:on
		}//isInValid
	}//IPv4Address

	@Nested
	@DisplayName("IPv6 address")
	class IPv6Address {
		private Random random;

		@BeforeEach
		void init() { random = new Random(); }

		@Test
		@DisplayName("is Valid")
		void isValid() {
			String addr = random.ints(8, 0, 0xffff)
			                    .mapToObj(Integer::toHexString).collect(joining(":"));

			assertAll(() -> assertNotNull(IPAddress.toNumericFormatIPv6(addr)),
			          () -> assertNotNull(IPAddress.toNumericFormatIPv6("2001:db8::2:0:0:9abc"), "Zero Compression"),
			          () -> assertNotNull(IPAddress.toNumericFormatIPv6("fe80::0123:4567:89ab:cdef%fxp0"),
			                              "Expect to ignore following a percent '%'"),
			          () -> {
				          String tmp = random.ints(4, 0, 256)
				                             .mapToObj(Integer::toString).collect(joining(".", "::ffff:", ""));
				          assertNotNull(IPAddress.toNumericFormatIPv6(tmp), "IPv4-Mapped IPv6 Address");
			          });
		}//isValid

		@TestFactory
		@DisplayName("is Invalid")
		Stream<DynamicTest> isInValid() {
			String[] addr = random.ints(8, 0x10000, Integer.MAX_VALUE)
			                      .mapToObj(Integer::toHexString).toArray(String[]::new);

			//@formatter:off
			return Stream.of(
				dynamicTest("No Colons", () -> assertThrows(IPAddressFormatException.class,
			                                            () -> IPAddress.toNumericFormatIPv6(String.join("", addr)))),
				dynamicTest("Zero Compression", () -> assertThrows(IPAddressFormatException.class,
				                                                   () -> {
					                                                   String tmp = ':' + String.join(":", addr);
					                                                   IPAddress.toNumericFormatIPv6(tmp);
				                                                   })),
				dynamicTest("Many double colons", () -> assertThrows(IPAddressFormatException.class,
			                                                     () -> IPAddress.toNumericFormatIPv6("2001:db8::2::9abc"))),
				dynamicTest("Out of Range", () -> assertThrows(IPAddressFormatException.class,
			                                               () -> IPAddress.toNumericFormatIPv6(String.join(":", addr)))),
				dynamicTest("Too many dots",
				            () -> assertThrows(IPAddressFormatException.class,
				                               () -> {
					                                String tmp = String.join(":", addr) + ':' + random.nextInt();
					                                IPAddress.toNumericFormatIPv6(tmp);
				                               }))
			);
			//@formatter:on
		}//isInValid
	}//IPv6Address
}
