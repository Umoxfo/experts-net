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
package experts.net.nic;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.commons.codec.binary.Hex;

/**
 * MAC Address
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public class MACAddr {
	/**
	 * Obtains the hardware address (usually MAC) of an interface which is from an IP address or a host name (e.g. "10.0.0.1" or "example").
	 *
	 * @param address
	 *            the String of the IP address or the host name
	 * @return a byte array contains the address
	 * @throws java.net.SocketException
	 * @throws java.net.UnknownHostException
	 */
	public static byte[] getMACAddress(String address) throws SocketException, UnknownHostException {
		// Stored in a NIC network interface corresponding to the IP address or the host name
		NetworkInterface nic = NetworkInterface.getByInetAddress(InetAddress.getByName(address));

		// Returns byte[] the hardware address
		return nic.getHardwareAddress();
	}// getMACAddress

	/**
	 * Obtains a string of the hardware address (usually MAC) of an interface which is from an IP address or a host name (e.g. "10.0.0.1" or "example").
	 *
	 * @param address
	 *            the String of the IP address or the host name
	 * @return a String represent of the MAC address format
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public static String toMACAddressString(String address) throws SocketException, UnknownHostException {
		return format(getMACAddress(address));
	}// toMACAddressString

	/**
	 * Converts a byte array contains a hardware address to a String of the form: %x:%x:%x:%x:%x:%x
	 */
	private static final String format(byte[] macAddr) {
		// Converting to a String MAC address obtained
		StringBuilder sb = new StringBuilder().append(Hex.encodeHex(macAddr, false));

		for (int i = 2; i < 17; i += 3) {
			sb.insert(i, ':');
		}// for

		return sb.toString();
	}// format
}
