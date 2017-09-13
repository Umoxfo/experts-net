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
package io.github.umoxfo.experts.net.ip6;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A class that helps to generate IPv6 address.
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class IP6Utils {
	private static final short ZERO = 0;

	/*
	 * Converts String array to Short list.
	 */
	static short[] toShortArray(String[] strArry) {
		short[] buf = new short[strArry.length];

		for (int i = 0, ln = strArry.length; i < ln; i++) {
			int n = Integer.parseInt(strArry[i], 16);

			// Check values
			if (n > 0xffff) {
				throw new IllegalArgumentException("Each group which is separated by colons must be within 16 bits.");
			}//if

			buf[i] = (short) n;
		}//for

		return buf;
	}//toShortArray

	/**
	 * Convert IPv6 binary address into a canonical format in RFC5952.
	 *
	 * Consecutive sections of zeroes are replaced with a double colon (::).
	 *
	 * @param addr a binary IPv6 address
	 * @return an IPv6 address in the colon 16-bit delimited hexadecimal format
	 */
	public static String toTextFormat(short[] addr) {
		// Set into the Array List
		ArrayList<Short> al =  new ArrayList<>(8);
		for (int i = 0; i < 8; i++) al.add(addr[i]);
		//ArrayList<Short> al = IntStream.range(0, 8).mapToObj(i -> addr[i]).collect(Collectors.toCollection(ArrayList::new));

		/*
		 * The longest run of consecutive 16-bit 0 fields MUST be shortened based on RFC5952.
		 */
		// Find the longest zero fields
		final int lastIndex = al.lastIndexOf(ZERO);

		int fromIndex = 0;
		int toIndex = 0;
		int maxCnt = 0;
		for (int i = 0; i < lastIndex; i++) {
			if (al.get(i) == ZERO) {
				int j = i + 1;
				while ((j <= lastIndex) && (al.get(j) == ZERO)) j++;

				int cnt = j - i;
				if (maxCnt < cnt) {
					fromIndex = i;
					toIndex = j;
					maxCnt = cnt;
				}//if

				i = j;
			}//if
		}//for

		// Remove the longest part of zeros
		if (1 < maxCnt) {
			al.subList(fromIndex, toIndex).clear();
			al.add(fromIndex, null);
		}//if

		// Convert to a hexadecimal string being separated with colons for each 4-digit
		String ip6 = al.stream().map(i -> {
			String str = "";
			if (i != null) str = Integer.toHexString(i & 0xffff);

			return str;
		}).collect(Collectors.joining(":"));

		return ip6.endsWith(":") ? ip6.concat(":") : ip6;
	}//format

	/**
	 * Returns the Unique Local IPv6 Unicast Address of the hardware address.
	 *
	 * @param nicAddress a hardware address of the machine that creates Unique Local IPv6 Unicast Addresses
	 * @return the Unique Local IPv6 Unicast Address
	 * @throws SocketException If the socket could not be opened which it might be not available any ports.
	 * @throws UnknownHostException If the host could not be found.
	 * @throws IOException If an error occurs while retrieving the time.
	 */
	public static String getULUAByHardwareAddress(byte[] nicAddress) throws IOException {
		return new ULUA(nicAddress).toString();
	}//getUniqueLocalUnicastAddressByHardwareAddress
}
