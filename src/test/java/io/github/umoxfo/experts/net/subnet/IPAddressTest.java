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

import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class IPAddressTest {
	private final byte[] IP4 = {(byte) 192, (byte) 168, 0, 1};
	private final byte[] IP6 = {0x20, 0x1, 0xd, (byte) 0xb8, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2, 0x0, 0x0, 0x0, 0x0, (byte) 0x9a,
	                            (byte) 0xbc};
	private final byte[] MAPPED_ADDRESS = {0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, (byte) 0xff, (byte) 0xff, (byte) 0xc0,
	                                       (byte) 0xa8, 0x0, 0x1};
	private ThreadLocalRandom random;

	@BeforeEach
	void setUp() { random = ThreadLocalRandom.current(); }

	@Nested
	@DisplayName("IP address")
	class Address {
		@Test
		@DisplayName("is Valid")
		void testValidAddress() {
			assertAll(() -> assertArrayEquals(IP4, IPAddress.toNumericFormatAddress("192.168.0.1"), "IPv4 address"),
			          () -> assertArrayEquals(IP6, IPAddress.toNumericFormatAddress("2001:db8::2:0:0:9abc"), "IPv6 address"),
			          () -> assertArrayEquals(MAPPED_ADDRESS, IPAddress.toNumericFormatAddress("::ffff:192.168.0.1"),
			                                  "IPv4-mapped IPv6 address"));
		}//isValid

		@Test
		@DisplayName("is Invalid")
		void testInvalidAddress() {
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
		@Test
		@DisplayName("is Valid")
		void isValid() {
			byte[] rowAddress = new byte[4];
			random.nextBytes(rowAddress);

			StringJoiner sj = new StringJoiner(".");
			for (int i = 0; i < 4; i++) {
				sj.add(Integer.toString(rowAddress[i] & 0xff));
			}//for

			assertArrayEquals(rowAddress, IPAddress.toNumericFormatIPv4(sj.toString()));
		}//isValid

		@Nested
		@DisplayName("is Invalid")
		class Invalid {
			private Stream<String> addr;

			@BeforeEach
			void setUp() {
				addr = random.ints(4, 256, Short.MAX_VALUE).mapToObj(Integer::toString);
			}

			@Test
			@DisplayName("Operation Error")
			void testWrongMethod() {
				assertThrows(IPAddressFormatException.class, () -> IPAddress.toNumericFormatIPv4("2001:db8::2:0:0:9abc"));
			}//testWrongMethod

			@Test
			@DisplayName("No Dots")
			void testNoDotsError() {
				assertThrows(IPAddressFormatException.class, () -> IPAddress.toNumericFormatIPv4(addr.collect(joining(""))));
			}//testNoDotsError

			@Test
			@DisplayName("Out of Range")
			void testOutOfRangeError() {
				assertThrows(IPAddressFormatException.class, () -> IPAddress.toNumericFormatIPv4(addr.collect(joining("."))));
			}//testOutOfRangeError

			@Test
			@DisplayName("Length")
			void testLengthError() {
				assertThrows(IPAddressFormatException.class,
				             () -> IPAddress.toNumericFormatIPv4(random.ints(5, 0, 256)
				                                                       .mapToObj(Integer::toString)
				                                                       .collect(joining("."))));
			}//testLengthError
		}//Invalid
	}//IPv4Address

	@Nested
	@DisplayName("IPv6 address")
	class IPv6Address {
		@Nested
		@DisplayName("is Valid")
		class Valid {
			private final byte[] LINK_LOCAL_ADDRESS = {(byte) 0xfe, (byte) 0x80, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1, 0x23, 0x45, 0x67,
			                                           (byte) 0x89, (byte) 0xab, (byte) 0xcd, (byte) 0xef};

			private byte[] rowAddress = new byte[16];
			private String address;

			@BeforeEach
			void setUp() {
				random.nextBytes(rowAddress);

				StringJoiner sj = new StringJoiner(":");
				for (int i = 0; i < 16; i += 2) {
					sj.add(Integer.toHexString(((rowAddress[i] & 0xff) << 8) | (rowAddress[i + 1] & 0xff)));
				}//for
				address = sj.toString();
			}//setUp

			@Test
			@DisplayName("Standard IPv6 address")
			void testStandardAddress() {
				assertArrayEquals(rowAddress, IPAddress.toNumericFormatIPv6(address));
			}//testStandardAddress

			@Test
			@DisplayName("IPv4-mapped IPv6 address")
			void testMappedAddress() {
				assertArrayEquals(MAPPED_ADDRESS, IPAddress.toNumericFormatIPv6("::ffff:192.168.0.1"),
				                  "Expect to ignore following a percent '%'");
			}//testMappedAddress

			@Test
			@DisplayName("Zero Compression")
			void testZeroCompression() {
				assertArrayEquals(IP6, IPAddress.toNumericFormatIPv6("2001:db8::2:0:0:9abc"));
			}//testZeroCompression

			@Test
			@DisplayName("Ignore Interface number")
			void testIgnoreInterface() {
				assertArrayEquals(LINK_LOCAL_ADDRESS, IPAddress.toNumericFormatIPv6("fe80::0123:4567:89ab:cdef%fxp0"),
				                  "Expect to ignore following a percent '%'");
			}//testIgnoreInterface
		}//Valid

		@Nested
		@DisplayName("is Invalid")
		class Invalid {
			private Stream<String> testAddr;

			@BeforeEach
			void setUp() {
				testAddr = random.ints(8, 0x10000, Integer.MAX_VALUE).mapToObj(Integer::toHexString);
			}

			@Test
			@DisplayName("Operation Error")
			void testWrongMethod() {
				assertThrows(IPAddressFormatException.class, () -> IPAddress.toNumericFormatIPv6("192.168.0.1"));
			}//testWrongMethod

			@TestFactory
			@DisplayName("No Colons")
			Stream<DynamicTest> testNoDotsError() {
				return Stream.of(testAddr.collect(joining("")))
				             .map(addr -> dynamicTest("Addr: " + addr, () -> assertThrows(IPAddressFormatException.class,
				                                                                          () -> IPAddress.toNumericFormatIPv6(addr))));
			}//testNoDotsError

			@TestFactory
			@DisplayName("Out of Range")
			Stream<DynamicTest> testOutOfRangeError() {
				return Stream.of(testAddr.collect(joining(":")))
				             .map(addr -> dynamicTest("Addr: " + addr, () -> assertThrows(IPAddressFormatException.class,
				                                                                          () -> IPAddress.toNumericFormatIPv6(addr))));
			}//testNoDotsError

			@Test
			@DisplayName("Beginning a Colon")
			void testLeadDoubleColonError() {
				assertThrows(IPAddressFormatException.class, () -> IPAddress.toNumericFormatIPv6(":2001:db8:0:2:0:9abc"));
			}//testLeadDoubleColonError

			@Test
			@DisplayName("Zero Compression")
			void testZeroCompressionError() {
				assertThrows(IPAddressFormatException.class, () -> IPAddress.toNumericFormatIPv6("2001:db8::2::9abc"));
			}//testZeroCompressionError

			@TestFactory
			@DisplayName("Length")
			Stream<DynamicTest> testLengthError() {
				return Stream.of(random.ints(9, 0x0, 0xffff).mapToObj(Integer::toHexString).collect(joining(":")))
				             .map(addr -> dynamicTest("Addr: " + addr, () -> assertThrows(IPAddressFormatException.class,
				                                                                          () -> IPAddress.toNumericFormatIPv6(addr))));
			}//testLengthError
		}//Invalid
	}//IPv6Address
}
