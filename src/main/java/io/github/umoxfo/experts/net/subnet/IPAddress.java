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
package io.github.umoxfo.experts.net.subnet;

import java.util.regex.Pattern;

/**
 * Utility methods for processing String objects containing IP addresses.
 */
public class IPAddress {
	private static final String IPV4_MAPPED_IPV6_ADDRESS = "::ffff:(\\d{1,3}\\.){3}\\d{1,3}";

	/**
	 * Validate the given IPv4 or IPv6 address.
	 *
	 * @param address the IP address as a String.
	 * @return true if a valid address, false otherwise
	 */
	static boolean isValid(String address) { return isValidIPv4(address) || isValidIPv6(address); }

	/*
	 * Converts IPv4 address in its textual presentation form
	 * into its numeric binary form.
	 *
	 * @param src a String representing an IPv4 address in standard format
	 * @return a byte array representing the IPv4 numeric address
	 */
	static byte[] toNumericFormatIPv4(String address) {
		final int addressLength = address.length();

		if (addressLength == 0 || address.indexOf('.') == -1) return null;

		byte[] res = new byte[4];

		int octets = 0;
		for (int i = 0; i < addressLength; i++) {
			if (octets >= 4) return null;

			int pos = address.indexOf('.', i);
			if (pos == -1) pos = addressLength;

			try {
				int octet = Integer.parseInt(address.substring(i, pos));
				if (octet < 0 || octet > 255) return null;

				res[octets++] = (byte) octet;
			} catch (NumberFormatException ex) { return null; }

			i = pos;
		}//for

		return res;
	}//toNumericFormatIPv4

	/*
	 * Validate the given IPv4 address.
	 *
	 * @param address the IP address as a String.
	 * @return true if a valid IPv4 address, false otherwise
	 */
	static boolean isValidIPv4(String address) { return toNumericFormatIPv4(address) != null; }

	/*
	 * Convert IPv6 presentation level address to network order binary form.
	 * credit:
	 *  Converted from C code from Solaris 8 (inet_pton)
	 *
	 * Any component of the string following a per-cent % is ignored.
	 *
	 * @param src a String representing an IPv6 address in textual format
	 * @return a byte array representing the IPv6 numeric address
	 */
	static byte[] toNumericFormatIPv6(String address) {
		int addressLength = address.length();
		byte[] dst = new byte[16];

		// Shortest valid string is "::", hence at least 2 chars
		if (addressLength < 2 || address.indexOf(':') == -1) return null;

		// Leading :: requires some special handling.
		if (address.charAt(0) == ':' && address.charAt(1) != ':') return null;

		int pc = address.indexOf('%');
		if (pc == addressLength -1) {
			return null;
		} else if (pc != -1) {
			addressLength = pc;
		}//if-else

		// IPv4-Mapped address
		if (address.indexOf('.') != -1) {
			if (Pattern.matches(IPV4_MAPPED_IPV6_ADDRESS, address)) {
				dst = toNumericFormatIPv4(address.substring(address.lastIndexOf(':') + 1));
			}//if

			return dst;
		}//if

		int octets = 0;
		int index = 0;
		int doubleColon = -1;
		boolean doubleColonFound = false;
		for (int i = 0; i < addressLength; i++) {
			if (octets >= 8) return null;

			int pos = address.indexOf(':', i);
			if (pos != -1) {
				if (i == pos) {
					if (doubleColonFound) return null;

					doubleColon = index;
					doubleColonFound = true;
					octets++;
					continue;
				}//if
			} else {
				pos = addressLength;
			}//if-else

			try {
				int octet = Integer.parseInt(address.substring(i, pos), 16);
				if (octet < 0 || octet > 0xffff) return null;

				dst[index++] = (byte) (octet >> 8);
				dst[index++] = (byte) octet;
			} catch (NumberFormatException ex) { return null; }

			i = pos;
			octets++;
		}//for

		if (doubleColonFound) {
			for (int i = index - 1, j = 15; i >= doubleColon; i--) {
				dst[j--] = dst[i];
				dst[i] = 0;
			}//for
		}//if

		return dst;
	}//toNumericFormatIPv6

	/*
	 * Validate the given IPv6 address.
	 *
	 * @param address the IP address as a String.
	 * @return true if a valid IPv6 address, false otherwise
	 */
	static boolean isValidIPv6(String address) { return toNumericFormatIPv6(address) != null; }

	private static boolean isMaskValue(String component, int size) {
		try {
			int value = Integer.parseInt(component);

			return value >= 0 && value <= size;
		} catch (NumberFormatException e) { return false; }
	}//isMaskValue

	/**
	 * Validate the given IPv4 or IPv6 address and netmask.
	 *
	 * @param address the IP address as a String.
	 * @return true if a valid address with netmask, false otherwise
	 */
	static boolean isValidWithNetmask(String address) {
		return isValidIPv4WithNetmask(address) || isValidIPv6WithNetmask(address);
	}//isValidWithNetmask

	static boolean isValidIPv4WithNetmask(String address) {
		int index = address.indexOf('/');

		if (index > 0) {
			if (isValidIPv4(address.substring(0, index))) {
				String mask = address.substring(index + 1);
				if (isValidIPv4(mask) || isMaskValue(mask, 32)) return true;
			}
		}

		return false;
	}//isValidIPv4WithNetmask

	static boolean isValidIPv6WithNetmask(String address) {
		int index = address.indexOf('/');

		if (index > 0) {
			if (isValidIPv6(address.substring(0, index))) {
				String mask = address.substring(index + 1);
				if (isMaskValue(mask, 128)) return true;
			}
		}

		return false;
	}//isValidIPv6WithNetmask
}
