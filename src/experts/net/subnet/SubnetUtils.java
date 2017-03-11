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
		IP4, 	IP6
	}// IP

	public static class Subnet {
		public static enum Type {
			MASK, CIDR
		}// Type

		public static class Mask {
			public static final int[] CLASS_LESS = {0, 0, 0, 0};
			public static final int[] CLASS_A = {255, 0, 0, 0};
			public static final int[] CLASS_B = {255, 255, 0, 0};
			public static final int[] CLASS_C = {255, 255, 255, 0};
			public static final int[] CLASS_FULL = {255, 255, 255, 255};
		}// Mask

		public static class CIDR {
			public static final int CLASS_A = 8;
			public static final int CLASS_B = 16;
			public static final int CLASS_C = 24;
		}// CIDR
	}// Subnet

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
		if (Integer.lowestOneBit(maskInt) - 1 != ~maskInt) {
			throw new IllegalArgumentException("Could not parse [" + mask + "]");
		}// if

		/* Calculates CIDR by counting the 1-bit population in the mask address */
		return Integer.bitCount(maskInt);
	}// toCIDR

	/**
	 * Converts CIDR-notation to a dotted decimal mask.
	 *
	 * @param cidr CIDR-notation value in range 0-32
	 * @return A subnet mask e.g. "255.255.0.0"
	 * @throws IllegalArgumentException if the parameter is invalid, i.e. out of range 0-32.
	 */
	public static String toMask(int cidr) {
		// Set the default subnet mask
		int[] mask = new int[4];
		int index = checkRange(cidr, 0, 32) / 8;
		switch (index) {
			case 0:
				mask = Subnet.Mask.CLASS_LESS;
				break;
			case 1:
				mask = Subnet.Mask.CLASS_A;
				break;
			case 2:
				mask = Subnet.Mask.CLASS_B;
				break;
			case 3:
				mask = Subnet.Mask.CLASS_C;
				break;
			case 4: return format(Subnet.Mask.CLASS_FULL);
		}// switch

		// Either default subnet mask
		int prefixSize = cidr % 8;
		if (prefixSize != 0) {
			/*
			 * Set the variable-length subnet masking (VLSM) by bit shift.
			 * Also, 255 << 8 - [out of classified default subnet mask bits] & 0xff.
			 */
			mask[index] = 255 >> prefixSize ^ 0xff;
		}// if

		// Separate by dots
		return format(mask);
	}// toMask

	/**
	 * Calculates the number of usable hosts from a network prefix of the address, which used CIDR value.
	 *
	 * @param prefix the prefix in range 0-32 (IPv4) or 0-64 (IPv6)
	 * @param terget a flag of the IP version
	 * @return Number of available hosts, including the gateway
	 */
	public static long numberOfHosts(int prefix, IP terget) {
		int addressSize = 0;
		int unavailableHosts = 0;
		switch (terget) {
			case IP4:
				addressSize = 32;
				unavailableHosts = 2; // network and broadcast addresses
				break;
			case IP6:
				addressSize = 64; // The maximum subnet bits in IPv6
				unavailableHosts = 1; // network address
				break;
		}// switch

		/*
		 * Calculate the number of hosts from the CIDR value
		 *
		 * The sizes of address - CIDR = host bits, and 2 ^ the bits = the number of hosts
		 */
		long hosts = (long) Math.pow(2, addressSize - prefix);

		// For routed subnets larger than 31 or 63, subtract 2 from the number of available hosts
		if (prefix < addressSize - 1) {
			hosts -= unavailableHosts;
		} else {
			hosts = 0;
		}//if-else

		// Return number of available hosts, including the gateway
		return hosts;
	}// numberOfHosts

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
			addr |= (n & 0xff) << 8 * (3 - i);
		}//for

		return addr;
	}//toInteger

	/**
	 * Convenience function to check integer boundaries.
	 * Checks if a value x is in the range [begin,end].
	 * Returns x if it is in range, throws an exception otherwise.
	 */
	static int checkRange(int value, int begin, int end) {
		if (value < begin || value > end) {
			throw new IllegalArgumentException("Value [" + value + "] not in range [" + 0 + ", " + end + "]");
		}// if

		return value;
	}// checkRange

	/*
	 * Converts a packed integer address into dotted decimal format
	 */
	static String format(int val) {
		int ret[] = new int[4];
		for (int i = 3; i >= 0; i--) {
			ret[i] = val >>> 8 * (3 - i) & 0xff;
		}//for

		return SubnetUtils.format(ret);
	}// format(int val)

	/*
	 * Converts a 4-element integer array into dotted decimal format.
	 */
	private static String format(int[] arry) {
		StringBuilder buf = new StringBuilder();
		int iMax = arry.length - 1;

		for (int i = 0; i <= iMax; i++) {
			buf.append(arry[i]);

			if (i != iMax) {
				buf.append(".");
			}// if
		}// for

		return buf.toString();
		// Arrays.stream(arry).mapToObj(Integer::toString).collect(Collectors.joining("."));
	}// format(int[] arry)
}
