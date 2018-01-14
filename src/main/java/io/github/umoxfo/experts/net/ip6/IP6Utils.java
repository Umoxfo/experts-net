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
package io.github.umoxfo.experts.net.ip6;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;

/**
 * A class that helps to generate IPv6 address.
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class IP6Utils {
	private static final short EUI64_ADDITIONAL_BITS = (short) 0xfffe;

	private IP6Utils() { throw new IllegalStateException("Utility class"); }

	/**
	 * Creates an IEEE EUI-64 identifier from an IEEE 48-bit MAC identifier.
	 * See <a href="https://tools.ietf.org/html/rfc4291#appendix-A">Appendix A:
	 * Creating Modified EUI-64 Format Interface Identifiers</a> of RFC 4291.
	 *
	 * @param macAddr the byte array containing a hardware address
	 * @return the Interface ID by the EUI-64 format
	 */
	public static byte[] createEUI64(byte[] macAddr) {
		byte[] eui64 = new byte[8];

		ByteBuffer.wrap(eui64).put(macAddr, 0, 3).putShort(EUI64_ADDITIONAL_BITS).put(macAddr, 3, 3);
		eui64[0] ^= 0x02;

		return eui64;
	}//createEUI64

	/*
	 * Combine byte arrays of Global ID, Subnet ID, and Interface ID to a single array
	 * before formatting to IPv6 address.
	 */
	static String toTextFormat(byte[] globalID, byte[] subnetID, byte[] interfaceID) {
		// Collect into a single array
		byte[] addr = new byte[16];

		System.arraycopy(globalID, 0, addr, 0, globalID.length);
		System.arraycopy(subnetID, 0, addr, globalID.length, subnetID.length);
		System.arraycopy(interfaceID, 0, addr, (globalID.length + subnetID.length), interfaceID.length);

		return toTextFormat(addr);
	}//toTextFormat(byte[] globalID, byte[] subnetID, byte[] interfaceID)

	/**
	 * Convert IPv6 binary address into a canonical format based on
	 * <a href="https://www.rfc-editor.org/rfc/rfc5952.txt">RFC 5952:
	 * A Recommendation for IPv6 Address Text Representation</a>.
	 *
	 * <p>Consecutive sections of zeroes are replaced with a double colon (::).
	 *
	 * @param address a binary IPv6 address
	 * @return a String representing an IPv6 address in the colon 16-bit delimited hexadecimal format
	 */
	public static String toTextFormat(byte[] address) {
		// Set into the Array List
		ArrayList<Integer> al =  new ArrayList<>(8);
		for (int i = 0; i < address.length; i += 2) {
			al.add(((address[i] & 0xff) << 8) | (address[i + 1] & 0xff));
		}//for

		/*
		 * The longest run of consecutive 16-bit 0 fields MUST be shortened based on RFC5952.
		 */
		int fromIndex = 0;
		int toIndex = 0;
		int bestRunLength = -1;
		int runStart = -1;
		for (int i = 0; i < 9; i++) {
			if (i < 8 && al.get(i) == 0) {
				if (runStart < 0) runStart = i;
			} else if (runStart >= 0) {
				int length = i - runStart;
				if (length > bestRunLength) {
					fromIndex = runStart;
					toIndex = i;
					bestRunLength = length;
				}//if

				runStart = -1;
			}//if-else if
		}//for

		// Remove the longest part of zeros
		if (bestRunLength > 1) {
			al.subList(fromIndex, toIndex).clear();
			al.add(fromIndex, null);
		}//if

		// Convert to a hexadecimal string being separated with colons for each 4-digit
		return al.stream().map(i -> (i != null) ? Integer.toHexString(i) : "")
		         .collect(collectingAndThen(joining(":"), ip6 -> ip6.endsWith(":") ? ip6.concat(":") : ip6));

/*		// Set into an integer array
		int[] addr = new int[8];
		for (int i = 0; i < 8; i++) {
			int j = i << 1;
			addr[i] = ((address[j] & 0xff) << 8) | (address[j + 1] & 0xff);
		}//for

		int bestRunStart = -1;
		int bestRunLength = -1;
		int runStart = -1;
		for (int i = 0; i < 9; i++) {
			if (i < 8 && addr[i] == 0) {
				if (runStart < 0) runStart = i;
			} else if (runStart >= 0) {
				int runLength = i - runStart;
				if (runLength > bestRunLength) {
					bestRunStart = runStart;
					bestRunLength = runLength;
				}//if

				runStart = -1;
			}//if-elseIf
		}//for

		StringBuilder buf = new StringBuilder(39);
		boolean omitZero = bestRunLength > 1;
		boolean lastWasNumber = false;
		for (int i = 0; i < 8; i++) {
			if (omitZero && i == bestRunStart) {
				buf.append("::");
				i += bestRunLength - 1;
				lastWasNumber = false;
			} else {
				if (lastWasNumber) {
					buf.append(':');
				}
				buf.append(Integer.toHexString(addr[i]));
				lastWasNumber = true;
			}//if-else
		}//for

		return buf.toString();*/
	}//toTextFormat(byte[])

	/**
	 * Returns the Unique Local IPv6 Unicast Address of the hardware address.
	 *
	 * @param nicAddress a hardware address of the machine that creates Unique Local IPv6 Unicast Addresses
	 * @return the Unique Local IPv6 Unicast Address
	 */
	public static String getULUAByHardwareAddress(byte[] nicAddress) {
		return new ULUA(nicAddress).toString();
	}//getUniqueLocalUnicastAddressByHardwareAddress
}
