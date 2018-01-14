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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class IP4SubnetTest {
	private static final byte[] TEST_ADDRESS = {(byte) 192, (byte) 168, 15, 7};
	private static final int[] CIDR = {8, 16, 24, 29, 30, 31, 32};

	private static List<IP4Subnet> ip4SubnetList;

	private static Stream<Arguments> createTestData(List<String> list1, List<String> list2, List<?> list3) {
		Iterator<String> iter1 = list1.iterator();
		Iterator<String> iter2 = list2.iterator();
		Iterator<?> iter3 = list3.iterator();

		return ip4SubnetList.stream().map(obj -> Arguments.of(obj, iter1.next(), iter2.next(), iter3.next()));
	}//createTestData

	@BeforeAll
	static void initAll() {
		ip4SubnetList = Arrays.stream(CIDR).mapToObj(c -> new IP4Subnet(TEST_ADDRESS, c))
		                      .collect(collectingAndThen(toList(), Collections::unmodifiableList));
	}//initAll

	static Stream<Arguments> parseSimpleCIDRProvider() {
		List<String> masks = List.of("255.0.0.0", "255.255.0.0", "255.255.255.0", "255.255.255.248",
		                             "255.255.255.252", "255.255.255.254", "255.255.255.255");
		List<String> netwk = List.of("192.0.0.0", "192.168.0.0", "192.168.15.0", "192.168.15.0", "192.168.15.4",
		                             "192.168.15.6", "192.168.15.7");
		List<String> bcast = List.of("192.255.255.255", "192.168.255.255", "192.168.15.255", "192.168.15.7",
		                             "192.168.15.7", "192.168.15.7", "192.168.15.7");

		return createTestData(masks, netwk, bcast);
	}//parseSimpleNetmaskProvider

	static Stream<Arguments> parseSimpleCIDRExclusiveProvider() {
		List<String> lowAd = List.of("192.0.0.1", "192.168.0.1", "192.168.15.1", "192.168.15.1", "192.168.15.5",
		                             "0.0.0.0", "0.0.0.0");
		List<String> highAd = List.of("192.255.255.254", "192.168.255.254", "192.168.15.254", "192.168.15.6",
		                              "192.168.15.6", "0.0.0.0", "0.0.0.0");
		List<Long> usableAd = List.of(16777214L, 65534L, 254L, 6L, 2L, 0L, 0L);

		return createTestData(lowAd, highAd, usableAd);
	}//parseSimpleNetmaskExclusiveProvider

	static Stream<Arguments> parseSimpleCIDRInclusiveProvider() {
		List<String> lowAd = List.of("192.0.0.0", "192.168.0.0", "192.168.15.0", "192.168.15.0", "192.168.15.4",
		                             "192.168.15.6", "192.168.15.7");
		List<String> highAd = List.of("192.255.255.255", "192.168.255.255", "192.168.15.255", "192.168.15.7",
		                              "192.168.15.7", "192.168.15.7", "192.168.15.7");
		List<Long> usableAd = List.of(16777216L, 65536L, 256L, 8L, 4L, 2L, 1L);

		return createTestData(lowAd, highAd, usableAd);
	}//parseSimpleNetmaskInclusiveProvider

	@ParameterizedTest
	@MethodSource("parseSimpleCIDRProvider")
	void testSimpleCIDR(IP4Subnet ip4Subnet, String netmask, String network, String broadcast) {
		assertAll(() -> assertEquals(netmask, ip4Subnet.getNetmask()),
		          () -> assertEquals(network, ip4Subnet.getNetworkAddress()),
		          () -> assertEquals(broadcast, ip4Subnet.getBroadcastAddress()));
	}//testParseSimpleNetmask

	@ParameterizedTest
	@MethodSource("parseSimpleCIDRExclusiveProvider")
	void testSimpleCIDRExclusive(IP4Subnet ip4Subnet, String lowAd, String highAd, long usableAd) {
		ip4Subnet.setInclusiveHostCount(false);

		assertAll(() -> assertEquals(lowAd, ip4Subnet.getLowAddress()),
		          () -> assertEquals(highAd, ip4Subnet.getHighAddress()),
		          () -> assertEquals(usableAd, ip4Subnet.getAddressCountLong()));
	}//testParseSimpleNetmaskExclusive

	@ParameterizedTest
	@MethodSource("parseSimpleCIDRInclusiveProvider")
	void testSimpleCIDRInclusive(IP4Subnet ip4Subnet, String lowAd, String highAd, long usableAd) {
		ip4Subnet.setInclusiveHostCount(true);

		assertAll(() -> assertEquals(lowAd, ip4Subnet.getLowAddress()),
		          () -> assertEquals(highAd, ip4Subnet.getHighAddress()),
		          () -> assertEquals(usableAd, ip4Subnet.getAddressCountLong()));
	}//testParseSimpleNetmaskInclusive

	@TestFactory
	Stream<DynamicTest> testInvalidInRange() {
		byte[] address = {(byte) 0xc0, (byte) 0xa8, 0x1, 0x0};

		return new Random().ints(4, 0, 33)
			.mapToObj(n -> new IP4Subnet(address, n))
			.map(obj -> dynamicTest("CIDR Notation: " + obj.getCIDRNotation(),
			                        () -> assertFalse(obj.isInRange("0.0.0.0"))));
	}//testInvalidInRange
}
