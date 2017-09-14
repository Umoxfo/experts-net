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
 * along with Experts Net.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.umoxfo.experts.net.subnet;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * This class that performs some subnet calculations given IP address in CIDR notation.
 *
 * <p>
 * For IPv4 address subnet, especially Classless Inter-Domain Routing (CIDR),
 * refer to <a href="https://tools.ietf.org/html/rfc4632">RFC4632</a>.
 * </p>
 * <p>
 * For IPv6 address subnet, refer to
 * <a href="https://tools.ietf.org/html/rfc4291#section-2.3">
 * Section 2.3 of RFC 4291</a>.
 * </p>
 *
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.5
 */
public final class SubnetUtils {
	public enum IP {
		IPv4(4, 32),
		IPv6(16, 128);

		private final int fieldLength;
		private final int bits;

		IP(int fL, int nBits) {
			fieldLength = fL;
			bits = nBits;
		}//IP
	}//IP

	/*
	 * Converts a dotted decimal format address to a packed integer format
	 */
	private static int toInteger(String address) {
		String[] addrArry = address.split("\\.");

		// Check the length of the array, must be 4
		if (addrArry.length != 4) {
			throw new IllegalArgumentException("Could not parse [" + address + "]");
		}//if

		/* Check range of each element and convert to integer */
		int addr = 0;
		for (int i = 0; i < 4; i++) {
			int n = checkRange(Integer.parseInt(addrArry[i]), 255);
			addr = (addr << 8) | n;
		}//for

		return addr;
	}//toInteger

	/*
	 * Checks if a value x is in the range [begin, end].
	 * Returns x if it is in range, throws an exception otherwise.
	 */
	private static int checkRange(int value, int max) {
		if ((value < 0) || (value > max)) {
			throw new IllegalArgumentException("Value [" + value + "] not in range [" + 0 + ", " + max + "]");
		}//if

		return value;
	}//checkRange

	/*
	 * Converts a packed integer address into a dotted decimal format.
	 */
	static String format(int address) {
		StringBuilder buf = new StringBuilder();

		for (int i = 0; i < 4; i++) {
			buf.append((address >>> (8 * (3 - i))) & 0xff);

			if (i != 3) {
				buf.append('.');
			}//if
		}//for

		return buf.toString();
	}//format

	/**
	 * Creates subnet summary information based on the provided in CIDR notation of IPv4 or IPv6 address,
	 * e.g. "192.168.0.1/16" for IPv4 or "2001:db8:0:0:0:ff00:42:8329/46" for IPv6
	 *
	 * @param cidrNotation an IPv4 or IPv6 address in CIDR notation
	 * @return a SubnetInfo object, which is implication of {@link IP4Subnet} or
	 *         {@link IP6Subnet}, created from the IP address.
	 * @throws UnknownHostException see {@link InetAddress#getByName(String host)}
	 * @throws SecurityException if a security manager exists and its checkConnect method doesn't allow the operation
	 */
	public static SubnetInfo getByCIDRNotation(String cidrNotation) throws UnknownHostException {
		if (cidrNotation.contains("/")) {
			String[] arry = Pattern.compile("/").split(cidrNotation);

			byte[] address = InetAddress.getByName(arry[0]).getAddress();
			int cidr = Integer.parseInt(arry[1]);

			if (address.length == IP.IPv4.fieldLength) {
				return new IP4Subnet(address, checkRange(cidr, IP.IPv4.bits));
			} else {
				return new IP6Subnet(address, checkRange(cidr, IP.IPv6.bits));
			}//if-else
		} else {
			throw new IllegalArgumentException("Could not parse [" + cidrNotation + "]");
		}//if
	}//getByCIDRNotation

	/**
	 * Creates subnet summary information, given a dotted decimal address and a dotted decimal mask.
	 *
	 * @param address An IP address, e.g. "192.168.0.1"
	 * @param mask A dotted decimal netmask e.g. "255.255.0.0"
	 * @return a IP4Subnet object created from the IP address.
	 * @throws UnknownHostException see {@link InetAddress#getByName(String host)}
	 * @throws IllegalArgumentException
	 *             if the address or mask is invalid,
	 *             i.e. the address does not match n.n.n.n where n=1-3 decimal digits, or
	 *             the mask does not match n.n.n.n which n=[0, 128, 192, 224, 240, 248, 252, 254, 255]
	 *             and after the 0-field, it is all zeros.
	 */
	public static IP4Subnet getByMask(String address, String mask) throws UnknownHostException {
		return new IP4Subnet(InetAddress.getByName(address).getAddress(), toCIDR(mask));
	}//getByMask

	/**
	 * Converts a dotted decimal mask to CIDR.
	 *
	 * @param mask a dotted decimal subnet mask, e.g. "255.255.0.0"
	 * @return CIDR value of the mask
	 * @throws IllegalArgumentException
	 *             if the parameter is invalid,
	 *             i.e. does not match n.n.n.n which n=[0, 128, 192, 224, 240, 248, 252, 254, 255]
	 *             and after the 0-field, it is all zeros.
	 */
	public static int toCIDR(String mask) {
		int maskInt = toInteger(mask);

		/*
		 * Check the subnet mask
		 *
		 * An IPv4 subnet mask must consist of a set of contiguous 1-bits followed by a block of 0-bits.
		 * If the mask follows the format, the numbers of subtracting one from the lowest one bit of the mask,
		 * see Hacker's Delight section 2.1, equals to the bitwise complement of the mask.
		 */
		if ((Integer.lowestOneBit(maskInt) - 1) != ~maskInt) {
			throw new IllegalArgumentException("Could not parse [" + mask + "]");
		}//if

		/* Calculates CIDR by counting the 1-bit population in the mask address */
		return Integer.bitCount(maskInt);
	}//toCIDR

	/**
	 * Converts IPv4 CIDR to a dotted decimal mask.
	 *
	 * @param cidr CIDR value in range 0-32
	 * @return a subnet mask e.g. 255.255.0.0
	 * @throws IllegalArgumentException if the parameter is invalid, i.e. out of range 0-32.
	 */
	public static String toMask(int cidr) {
		/*
		 * Create a binary netmask from the number of bits specification /x
		 * An IPv4 netmask consists of 32 bits, a contiguous sequence
		 * of the specified number of ones followed by all zeros.
		 * So, it can be obtained by shifting an unsigned integer (32 bits) to the left by
		 * the number of trailing zeros which is (32 - the # bits specification).
		 * Note that there is no unsigned left shift operator, so we have to use
		 * a long to ensure that the left-most bit is shifted out correctly.
		 */
		return format((int) (0xFFFF_FFFFL << (32 - checkRange(cidr, IP.IPv4.bits))));
	}//toMask

	/**
	 * Calculates the number of usable hosts from a network prefix of the address, which used CIDR value.
	 *
	 * @param prefix the prefix in range 0-32 (IPv4) or 0-128 (IPv6)
	 * @param target a flag of the IP version
	 * @return Number of available hosts, including the gateway
	 */
	public static String getHostCount(int prefix, IP target) {
		/* The sizes of address, either 32 or 128, - prefix is host bits, and "2 ^ the bits" is the number of hosts */
		String hosts = "";
		switch (target) {
			case IPv4:
				long hl = (long) Math.pow(2, IP.IPv4.bits - checkRange(prefix, IP.IPv4.bits));

				// Length of the network prefix is larger than 31, subtract 2 from the number of available hosts
				if (prefix < 31) {
					hl -= 2;
				} else {
					hl = 0;
				}//if-else

				hosts = Long.toString(hl);
				break;
			case IPv6:
				// The maximum subnet bits in IPv6
				hosts = BigInteger.valueOf(2).pow(IP.IPv6.bits - checkRange(prefix, IP.IPv6.bits)).toString();
				break;
		}//switch

		return hosts;
	}//getHostCount
}
