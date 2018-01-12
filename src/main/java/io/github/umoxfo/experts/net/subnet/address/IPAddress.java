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
package io.github.umoxfo.experts.net.subnet.address;

/**
 * Utility methods for processing String objects containing IP addresses.
 */
public final class IPAddress {
	private IPAddress() { throw new IllegalStateException("Utility class"); }

	/*
	 * Convert an IPv4-Mapped address into the IPv6 address text format.
	 */
	private static String convertDottedQuadToHex(String ipString) {
		int beginMapped = ipString.lastIndexOf(':') + 1;
		String initialPart = ipString.substring(0, beginMapped);
		String dottedQuad = ipString.substring(beginMapped);

		byte[] quad = toNumericFormatIPv4(dottedQuad);

		String penultimate = Integer.toHexString(((quad[0] & 0xff) << 8) | (quad[1] & 0xff));
		String ultimate = Integer.toHexString(((quad[2] & 0xff) << 8) | (quad[3] & 0xff));

		return initialPart + penultimate + ":" + ultimate;
	}//convertDottedQuadToHex

	/*
	 * Convert IPv4 address in its textual presentation form into the numeric binary form.
	 */
	private static byte[] toNumericFormatV4(String address) {
		final int addressLength = address.length();
		byte[] res = new byte[4];

		int octets = 0;
		for (int i = 0; i < addressLength; i++) {
			// Check the number of dots (.), must be 4
			if (octets >= 4) throw new IPAddressFormatException('[' + address + "]: invalid IPv4 address");

			int pos = address.indexOf('.', i);
			if (pos == -1) pos = addressLength;

			// Check range of an octet
			int octet = Integer.parseInt(address.substring(i, pos));
			if (octet < 0 || octet > 255) throw new IPAddressFormatException('[' + address + "]: invalid IPv4 address");
			res[octets++] = (byte) octet;

			i = pos;
		}//for

		return res;
	}//toNumericFormatV4

	/*
	 * Convert IPv6 presentation level address to network order binary form.
	 * Any component of the string following a per-cent % is ignored.
	 */
	private static byte[] toNumericFormatV6(String address) {
		int addressLength = address.length();
		byte[] dst = new byte[16];

		// Leading :: requires some special handling.
		if (address.charAt(0) == ':' && address.charAt(1) != ':') {
			throw new IPAddressFormatException('[' + address + "]: invalid IPv6 address");
		}

		int pc = address.indexOf('%');
		if (pc != -1) {
			addressLength = pc;
		}//if-else

		int octets = 0;
		int index = 0;
		int doubleColon = -1;
		for (int i = 0; i < addressLength; i++) {
			if (octets >= 8) return new byte[0];

			int pos = address.indexOf(':', i);
			if (pos != -1) {
				if (pos == i) {
					if (pos != 1 && doubleColon != -1) throw new IPAddressFormatException('[' + address + "]: invalid IPv6 address");

					doubleColon = index;
					octets++;
					continue;
				}//if
			} else {
				pos = addressLength;
			}//if-else

			int octet = Integer.parseInt(address.substring(i, pos), 16);
			if (octet < 0 || octet > 0xffff) {
				throw new IPAddressFormatException("Value [" + Integer.toHexString(octet) + "] not in range [0, 0xffff]");
			}//if

			dst[index++] = (byte) (octet >> 8);
			dst[index++] = (byte) octet;

			i = pos;
			octets++;
		}//for

		if (doubleColon != -1) {
			for (int i = index - 1, j = 15; i >= doubleColon; i--) {
				dst[j--] = dst[i];
				dst[i] = 0;
			}//for
		}//if

		return dst;
	}//toNumericFormatV6

	/*
	 * Convert the given address into a byte array.
	 *
	 * @param address the IP address as a String.
	 * @return true if a valid address, false otherwise
	 * @throws IPAddressFormatException If the IP address is invalid.
	 */
	public static byte[] toNumericFormatAddress(String address) {
		int indexDot = address.indexOf('.');
		int indexColon = address.indexOf(':');

		// Colons must not appear after dots.
		if (indexDot == -1 && indexColon == -1 || ((indexDot != -1) && (indexDot < indexColon))) {
			throw new IPAddressFormatException('[' + address + "]: invalid IP address");
		}//if

		byte[] addr;
		if (indexColon == -1) {
			addr = toNumericFormatV4(address);
		} else {
			if (indexDot != -1) {
				address = convertDottedQuadToHex(address);
			}//if
			addr = toNumericFormatV6(address);
		}//if-else

		return addr;
	}//toNumericFormatAddress

	/*
	 * Convert IPv4 address in its textual presentation form into a byte array.
	 *
	 * @param src a String representing an IPv4 address in standard format
	 * @return a byte array representing the IPv4 numeric address
	 * @throws IPAddressFormatException If the IPv4 address is invalid.
	 */
	public static byte[] toNumericFormatIPv4(String address) {
		int indexDot = address.indexOf('.');

		if (indexDot == -1) {
			throw new IPAddressFormatException('[' + address + "] is invalid IPv4 address");
		}//if

		return toNumericFormatV4(address);
	}//toNumericFormatIPv4

	/*
	 * Convert IPv6 presentation level address to network order binary form.
	 * Any component of the string following a per-cent % is ignored.
	 *
	 * @param src a String representing an IPv6 address in textual format
	 * @return a byte array representing the IPv6 numeric address
	 * @throws IPAddressFormatException If the IPv6 address is invalid.
	 */
	public static byte[] toNumericFormatIPv6(String address) {
		int indexDot = address.indexOf('.');
		int indexColon = address.indexOf(':');

		// Colons must not appear after dots.
		if (indexColon == -1 || ((indexDot != -1) && (indexDot < indexColon))) {
			throw new IPAddressFormatException('[' + address + "] is invalid IPv6 address");
		}//if

		// IPv4-Mapped IPv6 Address
		if (indexDot != -1) address = convertDottedQuadToHex(address);

		return toNumericFormatV6(address);
	}//toNumericFormatIPv6
}
