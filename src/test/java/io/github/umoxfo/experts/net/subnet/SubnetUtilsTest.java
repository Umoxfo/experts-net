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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.umoxfo.experts.net.subnet.SubnetUtils.IP;
import static io.github.umoxfo.experts.net.subnet.SubnetUtils.getHostCount;
import static io.github.umoxfo.experts.net.subnet.SubnetUtils.toCIDR;
import static io.github.umoxfo.experts.net.subnet.SubnetUtils.toMask;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class SubnetUtilsTest {
	private final List<String> NETMASK = List.of("0.0.0.0", "128.0.0.0", "192.0.0.0", "224.0.0.0", "240.0.0.0", "248.0.0.0",
	                                             "252.0.0.0", "254.0.0.0", "255.0.0.0", "255.128.0.0", "255.192.0.0", "255.224.0.0",
	                                             "255.240.0.0", "255.248.0.0", "255.252.0.0", "255.254.0.0", "255.255.0.0",
	                                             "255.255.128.0", "255.255.192.0", "255.255.224.0", "255.255.240.0", "255.255.248.0",
	                                             "255.255.252.0", "255.255.254.0", "255.255.255.0", "255.255.255.128",
	                                             "255.255.255.192", "255.255.255.224", "255.255.255.240", "255.255.255.248",
	                                             "255.255.255.252", "255.255.255.254", "255.255.255.255");
	private final List<Long> HOSTS = List.of(4294967294L, 2147483646L, 1073741822L, 536870910L, 268435454L, 134217726L,
	                                         67108862L, 33554430L, 16777214L, 8388606L, 4194302L, 2097150L, 1048574L, 524286L,
	                                         262142L, 131070L, 65534L, 32766L, 16382L, 8190L, 4094L, 2046L, 1022L, 510L, 254L,
	                                         126L, 62L, 30L, 14L, 6L, 2L, 0L, 0L);

	@Nested
	@DisplayName("IPv4 Subnet")
	class IP4 {
		private IntStream cidrs;

		@BeforeEach
		void setUp() { cidrs = ThreadLocalRandom.current().ints(8, 0, 33); }

		@TestFactory
		@DisplayName("Netmask Conversion")
		Stream<DynamicNode> testNetmaskConversion() {
			return cidrs.mapToObj(cidr -> dynamicContainer("Test Case: " + cidr, Stream.of(
				dynamicTest("To Mask", () -> assertEquals(NETMASK.get(cidr), toMask(cidr))),
				dynamicTest("To CIDR", () -> assertEquals(cidr, toCIDR(NETMASK.get(cidr)))))));
		}//testNetmaskConversion

		@Nested
		@DisplayName("Hosts")
		class Hosts {
			@Test
			@DisplayName("Border Hosts")
			void testIP4BorderHosts() {
				assertAll(() -> assertEquals(0, getHostCount(31, IP.IPv4).intValue()),
				          () -> assertEquals(0, getHostCount(32, IP.IPv4).intValue()));
			}//testIP4BorderHosts

			@TestFactory
			@DisplayName("Random Host Number")
			Stream<DynamicTest> testIP4Hosts() {
				return cidrs.mapToObj(cidr -> dynamicTest("CIDR: " + cidr, () -> assertEquals(HOSTS.get(cidr), getHostCount(cidr, IP.IPv4))));
			}//testIP4Subnet
		}//Hosts
	}//IP4
}
