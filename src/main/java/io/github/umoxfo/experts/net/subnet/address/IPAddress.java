/*
 * Copyright (c) 2018. Makoto Sakaguchi
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

	/**
	 * Convert an IP address into a byte array.
	 *
	 * @param address a string representing an IP address in textual format
	 * @return a byte array representing the IP numeric address
	 *
	 * @throws IPAddressFormatException If the IP address is invalid.
	 */
	public static byte[] toNumericFormatAddress(String address) {
		switch (getAddressType(address)) {
			case IPv4:
				return toNumericFormatV4(address);
			case IPv6:
				return toNumericFormatV6(address);
			case IPv4MappedIPv6:
				return toNumericFormatV6(convertDottedQuadToHex(address));
			default:
				throw new IPAddressFormatException('[' + address + "]: invalid IP address");
		}//switch
	}//toNumericFormatAddress

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

	/**
	 * Convert an IPv4 address in its textual presentation form into a byte array.
	 *
	 * @param address a string representing an IPv4 address in textual format
	 * @return a byte array representing the IPv4 numeric address
	 *
	 * @throws IPAddressFormatException If the IPv4 address is invalid.
	 */
	public static byte[] toNumericFormatIPv4(String address) {
		if (!getAddressType(address).equals(AddressType.IPv4)) {
			throw new IPAddressFormatException('[' + address + "] is invalid IPv4 address");
		}//if

		return toNumericFormatV4(address);
	}//toNumericFormatIPv4

	/*
	 * Convert IPv4 address in its textual presentation form into the numeric binary form.
	 */
	private static byte[] toNumericFormatV4(String address) {
		int addressLength = address.length();
		byte[] des = new byte[4];

		int octets = 0;
		for (int i = 0; i < addressLength; i++) {
			// Check the number of dots (.), must be 4
			if (octets >= 4) throw new IPAddressFormatException('[' + address + "]: invalid IPv4 address");

			int pos = address.indexOf('.', i);
			if (pos == -1) pos = addressLength;

			// Check range of an octet
			int octet = Integer.parseInt(address.substring(i, pos));
			if (octet < 0 || octet > 255) throw new IPAddressFormatException('[' + address + "]: invalid IPv4 address");
			des[octets++] = (byte) octet;

			i = pos;
		}//for

		return des;
	}//toNumericFormatV4

	/*
	 * Convert IPv6 presentation level address to network order binary form.
	 * Any component of the string following a per-cent % is ignored.
	 */
	private static byte[] toNumericFormatV6(String address) {
		int addressLength = address.length();
		byte[] dst = new byte[16];

		// If the first character is a colon, the second is also a colon.
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
			if (octets >= 8) throw new IPAddressFormatException('[' + address + "]: invalid IPv6 address");

			int pos = address.indexOf(':', i);
			if (pos != -1) {
				if (pos == i) {
					if (pos != 1 && doubleColon != -1) {
						throw new IPAddressFormatException('[' + address + "]: invalid IPv6 address");
					}

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
	 * Identify the address format
	 */
	private static AddressType getAddressType(String address) {
		int indexDot = address.indexOf('.');
		int indexColon = address.indexOf(':');

		if (indexDot > 0) {
			// IPv4 or IPv4-Mapped IPv6 address
			if (indexColon == -1) {
				return AddressType.IPv4;
			} else if (indexDot > indexColon) {
				return AddressType.IPv4MappedIPv6;
			}//if-elseIf
		} else if (indexColon != -1) { // IPv6 address
			return AddressType.IPv6;
		}//if-elseIf

		return AddressType.Unknown;
	}//checkAddressFormat

	/**
	 * Convert an IPv6 presentation level address to network order binary form.
	 * Any component of the string following a per-cent % is ignored.
	 *
	 * @param address a string representing an IPv6 address in textual format
	 * @return a byte array representing the IPv6 numeric address
	 *
	 * @throws IPAddressFormatException If the IPv6 address is invalid.
	 */
	public static byte[] toNumericFormatIPv6(String address) {
		switch (getAddressType(address)) {
			case IPv6:
				return toNumericFormatV6(address);
			case IPv4MappedIPv6:
				return toNumericFormatV6(convertDottedQuadToHex(address));
			default:
				throw new IPAddressFormatException('[' + address + "] is invalid IPv6 address");
		}//switch
	}//toNumericFormatIPv6

	private enum AddressType {
		IPv4,
		IPv6,
		IPv4MappedIPv6,
		Unknown;
	}
}
