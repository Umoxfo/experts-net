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
import experts.net.subnet.SubnetUtils.Subnet;

/**
 * CIDR
 *
 * @author Makoto Sakaguchi
 * @version 2.0.1-SNAPSHOT
 */
public class CIDR {

	/**
	 * I converted to CIDR notation (IPv4 format) subnet.
	 *
	 * @param mask
	 *            Subnet Mask
	 * @return The value of the CIDR notation.
	 */
	public static int convertSubnetIPv4ToCIDR(String mask) {
		// Removal period (".")
		String[] maskArry = mask.split("\\.");

		// Check the length of the array
		if (maskArry.length != IP.IPV4.getGrups()) {
			throw new IllegalArgumentException("引数(subnet)は IPv4形式ではありません。(" + mask + ")");
		}

		// Retrun CIDR of the mask
		switch (mask) {
			case Subnet.Mask.CLASS_A:
				return Subnet.CIDR.CLASS_A;
			case Subnet.Mask.CLASS_B:
				return Subnet.CIDR.CLASS_B;
			case Subnet.Mask.CLASS_C:
				return Subnet.CIDR.CLASS_C;
			default:
				return convertCIDR(maskArry);
		}
	}// convertSubnetIPv4ToCIDR

	/**
	 * The calculated CIDR from variable-length subnet masking (VLSM).
	 *
	 * @param maskArry
	 *            VLSM of string array
	 * @return CIDR of the mask
	 */
	private static int convertCIDR(String[] maskArry) {
		// Convert the subnet mask to Integer.
		int cidr = 0;
		for (String str : maskArry) {
			cidr = (cidr << Byte.SIZE) + Integer.parseInt(str);
		}

		/*
		 * Check the Subnet Mask
		 */
		// The expected number of zero bits
		int ntz = Integer.numberOfTrailingZeros(cidr);
		// The actual number of zero bits
		int bitCnt = Integer.bitCount(~cidr);
		if (ntz != bitCnt) {
			throw new IllegalArgumentException("引数(subnet)は サブネットマスクではありません。");
		}

		// Count the number of bits
		return Integer.bitCount(cidr);
	}// convertCIDR
}
