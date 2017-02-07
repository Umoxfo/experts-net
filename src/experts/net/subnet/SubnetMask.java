/*
 * Copyright (C) 2013-2017 Makoto Sakaguchi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package experts.net.subnet;

import experts.net.subnet.SubnetUtils.IP;

/**
 * SubnetMask
 *
 * @author Makoto Sakaguchi
 * @version 2.0.1-SNAPSHOT
 */
public class SubnetMask {
	private static final int[] CLASS_A_DEFAULT_SUBNET_MASK = { 0, 0, 0, 0 };
	private static final int[] CLASS_B_DEFAULT_SUBNET_MASK = { 255, 0, 0, 0 };
	private static final int[] CLASS_C_DEFAULT_SUBNET_MASK = { 255, 255, 0, 0 };
	private static final int[] CLASS_D_DEFAULT_SUBNET_MASK = { 255, 255, 255, 0 };

	/**
	 * Wants to convert to IPv4 format (CIDR notation) subnet.
	 *
	 * @param cidr
	 *            CIDR value: 0 - 32
	 * @return Subnet mask in IPv4 format.
	 */
	public static String convertCIDRToMask(final int cidr) {
		if (cidr < 0 || cidr > IP.IPV4.getSize()) {
			throw new IllegalArgumentException("引数(cidr)は 0〜32 までの数値でなければなりません。(" + cidr + ")");
		}

		// Set default subnet mask
		int[] mask = new int[IP.IPV4.getGrups()];
		int index = cidr / 8;
		switch (index) {
			case 0:
				mask = CLASS_A_DEFAULT_SUBNET_MASK;
				break;
			case 1:
				mask = CLASS_B_DEFAULT_SUBNET_MASK;
				break;
			case 2:
				mask = CLASS_C_DEFAULT_SUBNET_MASK;
				break;
			case 3:
				mask = CLASS_D_DEFAULT_SUBNET_MASK;
				break;
		}// switch

		// Either default subnet mask
		int vlsmLength = cidr % 8;
		if (vlsmLength != 0) {
			// Set the variable-length subnet masking (VLSM) by bit shift
			mask[index] = IP.IPV4.getMaxRange() << Byte.SIZE - vlsmLength & IP.IPV4.getMaxRange();
		}

		// Separate by dots
		return toSubnetMaskString(mask);
	}// convertSubnetCIDRToIPv4

	/**
	 * Converts this subnet mask by integer array to a String.
	 *
	 * @param arry
	 *            Integer array of the subnet mask.
	 * @return a string representation of the subnet mask.
	 */
	private static String toSubnetMaskString(int[] arry) {
		StringBuilder buf = new StringBuilder();
		int iMax = arry.length - 1;

		for (int i = 0;; i++) {
			buf.append(arry[i]);

			if (i == iMax) {
				return buf.toString();
			}

			buf.append(".");
		}

		// Arrays.stream(arry).boxed().map(i ->
		// Integer.toString(i)).collect(Collectors.joining("."));
	}// toSubnetMaskString
}
