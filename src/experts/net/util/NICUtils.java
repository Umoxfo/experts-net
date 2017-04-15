/*
 * Experts Net
 * Copyright (C) 2013-2017 Makoto Sakaguchi
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
 */
package experts.net.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.codec.binary.Hex;

/**
 * This class represents a hardware address, also MAC address, assigned to the interface.
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class NICUtils {
	/*
	 * Converts a byte array contains a hardware address to the printing MAC-48 addresses;
	 * which is six groups of two hexadecimal digits, separated by hyphens in transmission order,
	 * e.g. "00-1B-63-84-45-E6".
	 */
	private static String format(byte[] macAddr) {
		StringBuilder sb = new StringBuilder().append(Hex.encodeHex(macAddr, false));

		// Separate two hexadecimal digits by hyphens
		for (int i = 2; i < 17; i += 3) {
			sb.insert(i, '-');
		} // for

		return sb.toString();
	}// format

	/**
	 * Returns the hardware address (usually MAC address) of the interface
	 * that has the specified IP address or host name, e.g. "10.0.0.1" or "example.com".
	 *
	 * @param address the IP address or the host name
	 * @return a byte array containing the address, or {@code null}
	 *         if the address doesn't exist, is not accessible or
	 *         a security manager is set and the caller does not have
	 *         the permission {@link NetPermission}("getNetworkInformation")
	 * @throws SocketException If an I/O error occurs.
	 * @throws UnknownHostException If no IP address for the {@code host} could be found,
	 *             or a scope_id was specified for a global IPv6 address.
	 */
	public static byte[] getMACAddress(String address) throws SocketException, UnknownHostException {
		// Stored in a NIC network interface corresponding to the IP address or the host name
		NetworkInterface nic = NetworkInterface.getByInetAddress(InetAddress.getByName(address));

		// Returns byte[] the hardware address
		return nic.getHardwareAddress();
	}// getMACAddress

	/**
	 * Returns the hardware address (usually MAC address) of the interface
	 * that has the specified IP address or host name, e.g. "10.0.0.1" or "example.com"
	 * in the standard (IEEE 802) format for printing MAC-48 addresses in human-friendly form,
	 * e.g. "01-23-45-67-89-AB".
	 *
	 * @param address a host name or a textual representation of its IP address
	 * @return the address in the MAC address format, or {@code null}
	 *         if the address doesn't exist, is not accessible or
	 *         a security manager is set and the caller does not have
	 *         the permission {@link NetPermission}("getNetworkInformation")
	 * @throws SocketException If an I/O error occurs.
	 * @throws UnknownHostException If no IP address for the {@code host} could be found,
	 *             or a scope_id was specified for a global IPv6 address.
	 */
	public static String getMACAddressString(String address) throws SocketException, UnknownHostException {
		return format(getMACAddress(address));
	}// getMACAddressString
}
