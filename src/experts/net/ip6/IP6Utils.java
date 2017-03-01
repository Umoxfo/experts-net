/*
 * Experts Net
 * Copyright (C) 2014-2017 Makoto Sakaguchi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package experts.net.ip6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import experts.net.subnet.SubnetUtils.IP;

/**
 * IPV6Utils
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public class IP6Utils {
	/**
	 * Convert String array to List (Short).
	 *
	 * @param strArry
	 *            String array
	 * @return List (Short)
	 * @since 2.0.4
	 */
	public static List<Short> toShortList(String[] strArry) {
		Stream<String> arryStream = Arrays.stream(strArry);

		// Check values
		if (arryStream.map(i -> Integer.parseInt(i, 16)).anyMatch(i -> i > 0xffff)) {
			throw new IllegalArgumentException("Each group which is separated by colons must be within 16 bits.");
		} // if

		// Convert to the short list
		return arryStream.map(i -> (short) Integer.parseInt(i, 16)).collect(Collectors.toList());

		/*
		 * List<Short> buf = new ArrayList<>(strArry.length);
		 *
		 * Arrays.stream(strArry).forEach(i -> {
		 * int j = Integer.parseInt(i, 16);
		 *
		 * // Check values
		 * if (j > 0xffff) {
		 * throw new IllegalArgumentException("Each group which is separated by colons must be within 16 bits." );
		 * }//if
		 *
		 * // Convert to the short list
		 * buf.add((short) j);
		 * });
		 * return buf;
		 */
	}// toHexIntList

	/**
	 * Convert List (Short) to ArrayList (String).
	 *
	 * @param list
	 *            List (Short)
	 * @return ArrayList (String)
	 * @since 2.0.4
	 */
	private static List<String> toHexStringList(List<Short> list) {
		return list.stream().map(i -> Integer.toHexString(i & 0xffff)).collect(Collectors.toList());
	}// toHexStringArryList

	/**
	 * Consecutive sections of zeroes are replaced with a double colon (::).
	 *
	 * @param list
	 * @return String of an IPv6 address
	 */
	public static String buildIP6String(ArrayList<Short> list) {
		int fromIndex = 0;
		int toIndex = 0;
		int maxCnt = 0;

		// The longest run of consecutive 16-bit 0 fields MUST be shortened.
		int index = list.indexOf(0);
		while (index != -1 && index < 7) {
			int j = index + 1;
			while (j < IP.IP6.getGrups() && list.get(j) == 0) {
				j++;
			} // while

			int cnt = j - index;
			if (maxCnt < cnt) {
				fromIndex = index;
				toIndex = j;
				maxCnt = cnt;
			} // if

			if (j == 8) {
				break;
			}

			index = list.subList(++j, IP.IP6.getGrups()).indexOf(0) + j;
		} // while

		// The 4-digit hexadecimal each string
		LinkedList<String> buf = new LinkedList<>(toHexStringList(list));

		// Removing all leading zeroes. (RFC 5952)
		if (1 < maxCnt) {
			buf.subList(fromIndex, toIndex).clear();
			buf.add(fromIndex, "");
		} // if

		// Separated the array list with a colon ":"
		return String.join(":", buf);
	}// formatIP6String
}
