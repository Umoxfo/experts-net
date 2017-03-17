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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IPV6Utils
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class IP6Utils {
	private static final short ZERO = 0;
	//private static final String ZERO_FIELDS = "(0\\:){2,}";

	/**
	 * Converts String array to List (Short).
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
	 * Consecutive sections of zeroes are replaced with a double colon (::).
	 *
	 * @param list
	 * @return String of an IPv6 address
	 */
	public static String buildIP6String(List<Short> list) {
		int fromIndex = 0;
		int toIndex = 0;
		int maxCnt = 0;

		/* The longest run of consecutive 16-bit 0 fields MUST be shortened based on RFC5952. */
		// Find the longest zero fields
		int index = list.indexOf(ZERO);
		int lastIndex = list.lastIndexOf(ZERO);
		while (index < lastIndex) {
			int j = index + (list.subList(index + 1, lastIndex).indexOf(ZERO) + 1);
			while ((j <= lastIndex) && (list.get(j) == ZERO)) {
				j++;
			} // while

			int cnt = j - index;
			if (maxCnt < cnt) {
				fromIndex = index;
				toIndex = j;
				maxCnt = cnt;
			} // if

			index =  j + 1;
		} // while

		// Convert to a list of the 4-digit hexadecimal each string
		ArrayList<String> buf = new ArrayList<>(list.stream().map(i -> Integer.toHexString(i & 0xffff)).collect(Collectors.toList()));

		// Remove all leading zeroes
		if (1 < maxCnt) {
			buf.subList(fromIndex, toIndex).clear();
			buf.add(fromIndex, "");
		} // if

		// Separated the array list with a colon ":"
		return String.join(":", buf);

		/*
		// Convert to the 4-digit hexadecimal string that is separated with ":"
		String address = list.stream().map(i -> Integer.toHexString(i & 0xffff)).collect(Collectors.joining(":"));

		// The longest run of consecutive 0 fields MUST be shortened based on RFC 5952.
		String regex = "";
		int maxLength = 0;

		// Find the longest zero fields
		Matcher match = Pattern.compile(ZERO_FIELDS).matcher(address);
		while (match.find()) {
			String reg = match.group();
			int len = reg.length();

			if (maxLength < len) {
				regex = reg;
				maxLength = len;
			}//if
		}//while

		// Remove all leading zeroes
		return address.replace(regex, ":");
		 */
	}// formatIP6String
}
