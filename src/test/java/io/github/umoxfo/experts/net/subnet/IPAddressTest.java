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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class IPAddressTest {
	@TestFactory
	Collection<DynamicNode> testIPv4Address() {
		Random random = new Random();

		String addr = random.ints(4, 0, 256)
		                    .mapToObj(Integer::toString).collect(Collectors.joining("."));
		String cidrNotation = addr + "/" + random.nextInt(33);

		String invalidAddress = random.ints(4, 256, Integer.MAX_VALUE)
		                              .mapToObj(Integer::toString).collect(Collectors.joining("."));
		String invalidCIDRNotation = addr + "/" + (random.nextInt() + 32);

		//@formatter:off
		return Arrays.asList(
			dynamicContainer("Valid", Stream.of(
				dynamicTest("Address", () -> assertTrue(IPAddress.isValidIPv4(addr))),
				dynamicTest("CIDR-Notation", () -> assertTrue(IPAddress.isValidIPv4WithNetmask(cidrNotation)))
			)),
			dynamicContainer("Invalid", Stream.of(
				dynamicTest("Address", () -> assertFalse(IPAddress.isValidIPv4(invalidAddress))),
				dynamicTest("CIDR-Notation", () -> assertFalse(IPAddress.isValidIPv4WithNetmask(invalidCIDRNotation)))
			)));
		//@formatter:on
	}//testIPv4Address

	@Nested
	@DisplayName("IPv6 address")
	class IPv6Address {
		@TestFactory
		@DisplayName("is Valid")
		Stream<DynamicTest> isValid() {
			Random random = new Random();

			String addr = random.ints(8, 0, 0x10000)
			                    .mapToObj(Integer::toHexString).collect(Collectors.joining(":"));
			String cidrNotation = addr + "/" + random.nextInt(129);

			return Stream.of(
				dynamicTest("Address", () -> assertTrue(IPAddress.isValidIPv6(addr))),
				dynamicTest("CIDR-Notation", () -> assertTrue(IPAddress.isValidIPv6WithNetmask(cidrNotation))));
		}//isValid

		@TestFactory
		@DisplayName("is Invalid")
		Stream<DynamicTest> isInValid() {
			Random random = new Random();

			String addr = random.ints(8, 0x10000, Integer.MAX_VALUE)
			                    .mapToObj(Integer::toHexString).collect(Collectors.joining(":"));
			String cidrNotation = addr + "/" + (random.nextInt() + 128);

			return Stream.of(
				dynamicTest("Address", () -> assertFalse(IPAddress.isValidIPv6(addr))),
				dynamicTest("CIDR-Notation", () -> assertFalse(IPAddress.isValidIPv6WithNetmask(cidrNotation))));
		}//isInValid
	}//IPv6Address
}
