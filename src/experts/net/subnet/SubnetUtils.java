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
public class SubnetUtils {
	public static enum IP {
		IPV4(32, 4, 255),
		IPV6(128, 8, 0xffff);

		private final int size;
		private final int grups;
		private final int maxRange;

		private IP(int size, int grups, int maxRange) {
			this.size = size;
			this.grups = grups;
			this.maxRange = maxRange;
		}

		public int getSize() {
			return size;
		}

		public int getGrups() {
			return grups;
		}

		public int getMaxRange() {
			return maxRange;
		}
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
	 * Converts a dotted decimal mask to CIDR-notation.
	 *
	 * @param mask
	 *            A dotted decimal subnet mask, e.g. "255.255.0.0"
	 * @return A CIDR-notation integer
	 * @throws IllegalArgumentException
	 *             if the parameter is invalid,
	 *             i.e. does not match n.n.n.n which n=[0, 128, 192, 224, 240, 248, 252, 254, 255] and all fields after the number other than 255 are zero
	 */
	public static int toCIDR(String mask) {
		// Check the length of the array and range of each element and convert to integer
		int maskInt = toInteger(mask);

		/*
		 * Calculates CIDR from variable-length subnet masking (VLSM).
		 */
		/* Check the Subnet Mask */
		if (Integer.numberOfTrailingZeros(maskInt) != Integer.bitCount(~maskInt)) {
			throw new IllegalArgumentException("Could not parse [" + mask + "]");
		}// if

		return Integer.bitCount(maskInt);
	}// toCIDR

	/**
	 * Converts CIDR-notation to a dotted decimal mask.
	 *
	 * @param cidr CIDR-notation value in range 0-32
	 * @return A subnet mask e.g. "255.255.0.0"
	 * @throws IllegalArgumentException
	 *             if the parameter is invalid
	 */
	public static String toMask(int cidr) {
		// Set the default subnet mask
		int[] mask = new int[IP.IPV4.getGrups()];
		int index = checkRange(cidr, 0, IP.IPV4.getSize()) / 8;
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
			mask[index] = IP.IPV4.getMaxRange() >> prefixSize ^ 0xff;
		}// if

		// Separate by dots
		return format(mask);
	}// toMask

	/**
	 * Calculates the number of usable hosts from a network prefix of the address, which used CIDR value.
	 *
	 * @param prefix the prefix in range 0-32 (IPv4) or 0-128 (IPv6)
	 * @param terget a flag of the IP version
	 * @return Number of available hosts, including the gateway
	 */
	public static long numberOfHosts(int prefix, IP terget) {
		int addressSize = 0;
		switch (terget) {
			case IPV4:
				addressSize = IP.IPV4.getSize();
				break;
			case IPV6:
				addressSize = IP.IPV6.getSize();
				break;
		}// switch

		// Calculate the number of hosts from the CIDR notation
		long hosts = (long) Math.pow(2, addressSize - prefix);

		// For routed subnets larger than 31 or 127, subtract 2 from the number of available hosts
		if (prefix < addressSize - 1) {
			hosts -= 2;
		} else {
			hosts = 0;
		}//if-else

		// Return number of available hosts, including the gateway
		return hosts;
	}// numberOfHosts

	/*
	 * Convert a dotted decimal format address to a packed integer format
	 */
	static int toAddressInteger(String address) {
		return toInteger(address);
	}// toAddressInteger

	/*
	 * Convert a dotted decimal format address to a packed integer format
	 */
	private static int toInteger(String address) {
		String[] addrArry = address.split("\\.");

		// Check the length of the array
		if (addrArry.length != IP.IPV4.getGrups()) {
			throw new IllegalArgumentException("Could not parse [" + address + "]");
		}// if

		/* Check range of each element and convert to integer */
		int addr = 0;
		for (int i = 0; i < 4; i++) {
			int n = SubnetUtils.checkRange(Integer.parseInt(addrArry[i]), 0, 255);
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
	 * Converts a 4-element integer array into dotted decimal format.
	 */
	static String format(int[] arry) {
		StringBuilder buf = new StringBuilder();
		int iMax = arry.length - 1;

		for (int i = 0;; i++) {
			buf.append(arry[i]);

			if (i == iMax) {
				return buf.toString();
			}// if

			buf.append(".");
		}// for

		// Arrays.stream(arry).mapToObj(Integer::toString).collect(Collectors.joining("."));
	}// format
}
