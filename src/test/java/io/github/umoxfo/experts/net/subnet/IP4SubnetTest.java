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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class IP4SubnetTest {
	private static final byte[] TEST_ADDRESS = {(byte) 192, (byte) 168, 15, 7};

	@ParameterizedTest
	@CsvFileSource(resources = "/IPv4Address.csv", numLinesToSkip = 1)
	void testSimpleCIDR(int cidr, String netmask, String network, String broadcast, String excLowAd, String excHighAd,
	                    long excUsableAd, String incLowAd, String incHighAd, long incUsableAd) {
		IP4Subnet ip4Subnet = new IP4Subnet(TEST_ADDRESS, cidr);

		assertAll(() -> {
			          assertAll("CIDR",
			                    () -> assertEquals(netmask, ip4Subnet.getNetmask()),
			                    () -> assertEquals(network, ip4Subnet.getNetworkAddress()),
			                    () -> assertEquals(broadcast, ip4Subnet.getBroadcastAddress()));
		          },
		          () -> {
			          ip4Subnet.setInclusiveHostCount(false);
			          assertAll("Exclude",
			                    () -> assertEquals(excLowAd, ip4Subnet.getLowAddress()),
			                    () -> assertEquals(excHighAd, ip4Subnet.getHighAddress()),
			                    () -> assertEquals(excUsableAd, ip4Subnet.getAddressCountLong()));
		          },
		          () -> {
			          ip4Subnet.setInclusiveHostCount(true);
			          assertAll("Include",
			                    () -> assertEquals(incLowAd, ip4Subnet.getLowAddress()),
			                    () -> assertEquals(incHighAd, ip4Subnet.getHighAddress()),
			                    () -> assertEquals(incUsableAd, ip4Subnet.getAddressCountLong()));
		          });
	}//testSimpleCIDR

	@TestFactory
	Stream<DynamicTest> testInvalidInRange() {
		byte[] address = {(byte) 192, (byte) 168, 1, 0};

		return ThreadLocalRandom.current().ints(4, 0, 33)
		                        .mapToObj(n -> new IP4Subnet(address, n))
		                        .map(obj -> dynamicTest("CIDR Notation: " + obj.getCIDRNotation(),
		                                                () -> assertFalse(obj.isInRange("0.0.0.0"))));
	}//testInvalidInRange
}
