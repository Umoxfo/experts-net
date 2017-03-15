/*
 * Experts Net
 * Copyright (C) 2017 Makoto Sakaguchi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * NOTE:
 * This part of the program includes the code of "Apache Commons Net,"
 * which developed at The Apache Software Foundation (http://www.apache.org/)
 * and be distributed in the Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0),
 * by Makoto Sakaguchi on February 20, 2017.
 */
package experts.net.subnet;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * A class that performs some subnet calculations given a network address and a subnet mask.
 *
 * @see "https://tools.ietf.org/html/rfc4632"
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.5
 */
public final class SubnetUtils {
	public static enum IP {
		IPv4, IPv6
	}// IP

	public static final class Subnet {
		public static final class CIDR {
			public static final int CLASS_A = 8;
			public static final int CLASS_B = 16;
			public static final int CLASS_C = 24;
		}// CIDR
	}// Subnet

	private static final String IPV4_ADDRESS = "(\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}";
	private static final String IPV6_ADDRESS = "([0-9a-f]{1,4}\\:){7}[0-9a-f]{1,4}/\\d{1,3}";

	/**
	 * Constructor that takes a CIDR-notation string that both IPv4 and IPv6 allow,
	 * e.g. "192.168.0.1/16" or "2001:db8:0:0:0:ff00:42:8329/46"
	 *
	 * NOTE: IPv6 address does NOT allow to omit consecutive sections of zeros in the current version.
	 *
	 * @param cidrNotation An IPv4 or IPv6 address
	 * @return The class of SubnetInfo
	 */
	/*
	 * @throws UnknownHostException {@link InetAddress.getByName(String host)}
	 * @throws SecurityException if a security manager exists and its checkConnect method doesn't allow the operation
	 */
	public static SubnetInfo getByCIDRNortation(String cidrNotation) {
		if (Pattern.matches(IPV4_ADDRESS, cidrNotation)) {
			return new IP4Subnet(cidrNotation);
		} else if (Pattern.matches(IPV6_ADDRESS, cidrNotation)) {
			return new IP6Subnet(cidrNotation);
		} else {
			throw new IllegalArgumentException("Could not parse [" + cidrNotation + "]");
		}//if

		/*
		if (cidrNotation.contains("/")) {
			String[] arry = cidrNotation.split("/");

			byte[] address = InetAddress.getByName(arry[0]).getAddress();
			int cidr = Integer.parseInt(arry[1]);

			if (address.length == 4) {
				return new IP4Subnet(address, cidr);
			} else {
				return new IP6Subnet(address, cidr);
			}//if-else
		} else {
			throw new IllegalArgumentException("Could not parse [" + cidrNotation + "]");
		}//if-else
		 */
	}//getByCIDRNortation

	/**
	 * Converts a dotted decimal mask to CIDR.
	 *
	 * @param mask
	 *            A dotted decimal subnet mask, e.g. "255.255.0.0"
	 * @return An integer of CIDR value
	 * @throws IllegalArgumentException
	 *             if the parameter is invalid,
	 *             i.e. does not match n.n.n.n which n={0, 128, 192, 224, 240, 248, 252, 254, 255}
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
		}// if

		/* Calculates CIDR by counting the 1-bit population in the mask address */
		return Integer.bitCount(maskInt);
	}// toCIDR

	/**
	 * Converts CIDR to a dotted decimal mask.
	 *
	 * @param cidr CIDR value in range 0-32
	 * @return A subnet mask e.g. "255.255.0.0"
	 * @throws IllegalArgumentException if the parameter is invalid, i.e. out of range 0-32.
	 */
	public static String toMask(int cidr) {
		int[] mask = new int[4];
		int index = checkRange(cidr, 0, 32) / 8;

		// Set the default subnet mask of CIDR
		Arrays.fill(mask, 0, index, 255);

		// Either a default subnet mask
		int prefixSize = cidr % 8;
		if (prefixSize != 0) {
			/*
			 * Set the out of classified default subnet mask bits by bit shift.
			 * Also, 255 << 8 - [out of classified default subnet mask bits] & 0xff.
			 */
			mask[index] = (255 >> prefixSize) ^ 0xff;
		}// if

		// Separate by dots
		return format(mask, '.');
	}// toMask

	/**
	 * Calculates the number of usable hosts from a network prefix of the address, which used CIDR value.
	 *
	 * @param prefix the prefix in range 0-32 (IPv4) or 0-64 (IPv6)
	 * @param terget a flag of the IP version
	 * @return Number of available hosts, including the gateway
	 */
	public static String getHostCount(int prefix, IP target) {
		String hosts = "";

		/*
		 * Calculate the number of hosts from the CIDR value
		 *
		 * The sizes of address - CIDR = host bits, and 2 ^ the bits = the number of hosts
		 */
		switch (target) {
			case IPv4:
				long hl = (long) Math.pow(2, 32 - prefix);

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
				hosts = new BigInteger("2").pow(128 - prefix).toString();
				break;
		}// switch

		return hosts;
	}//getHostCount

	/*
	 * Convert a dotted decimal format address to a packed integer format
	 */
	static int toInteger(String address) {
		String[] addrArry = address.split("\\.");

		// Check the length of the array, must be 4
		if (addrArry.length != 4) {
			throw new IllegalArgumentException("Could not parse [" + address + "]");
		}// if

		/* Check range of each element and convert to integer */
		int addr = 0;
		for (int i = 0; i < 4; i++) {
			int n = checkRange(Integer.parseInt(addrArry[i]), 0, 255);
			addr |= (n & 0xff) << (8 * (3 - i));
		}//for

		return addr;
	}//toInteger

	/**
	 * Convenience function to check integer boundaries.
	 * Checks if a value x is in the range [begin,end].
	 * Returns x if it is in range, throws an exception otherwise.
	 */
	static int checkRange(int value, int begin, int end) {
		if ((value < begin) || (value > end)) {
			throw new IllegalArgumentException("Value [" + value + "] not in range [" + 0 + ", " + end + "]");
		}// if

		return value;
	}// checkRange

	/*
	 * Converts a 4-element integer array into dotted decimal format.
	 */
	static String format(int[] arry, char symbol) {
		StringBuilder buf = new StringBuilder();
		int iMax = arry.length - 1;

		for (int i = 0; i <= iMax; i++) {
			buf.append(arry[i]);

			if (i != iMax) {
				buf.append(symbol);
			}// if
		}// for

		return buf.toString();
		// Arrays.stream(arry).mapToObj(Integer::toString).collect(Collectors.joining(symbol));
	}// format(int[] arry)
}
