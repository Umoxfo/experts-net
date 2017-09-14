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
package io.github.umoxfo.experts.net.util;

import org.apache.commons.codec.binary.Hex;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * This class represents a hardware address, also MAC address, assigned to the interface.
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class NICUtils {
	private static final byte[][] VIRTUAL_MACHINE_MAC_ADDRESSES = {
		/* VMWare */
		{0x00, 0x05, 0x69},
		{0x00, 0x1C, 0x14},
		{0x00, 0x0C, 0x29},
		{0x00, 0x50, 0x56},

		/* VirtualBox */
		{0x08, 0x00, 0x27},
		{0x0A, 0x00, 0x27},

		/* Virtual-PC*/
		{0x00, 0x03, (byte)0xFF},

		/* Hyper-V */
		{0x00, 0x15, 0x5D},

		/* TAP-Windows */
		{0x00, (byte)0xFF, 0x69}
	};

	/*
	 * Converts a byte array contains a hardware address to the printing MAC-48 addresses;
	 * which is six groups of two hexadecimal digits, separated by hyphens in transmission order,
	 * e.g. "00-1B-63-84-45-E6".
	 */
	private static String format(byte[] macAddr) {
		StringBuilder sb = new StringBuilder(17).append(Hex.encodeHex(macAddr, false));

		// Separate two hexadecimal digits by hyphens
		for (int i = 2; i < 17; i += 3) {
			sb.insert(i, '-');
		}//for

/*		for (int i = 0; i < 6; i++) {
			sb.append(String.format("%02X%s", macAddr[i], (i < 5) ? "-" : ""));
		}*/

		return sb.toString();
	}//format

	/*
	 * Returns {@code false} if the hardware address is not from a virtual machine.
	 */
	private static byte[] checkValidity(byte[] mac) {
		for (byte[] vm: VIRTUAL_MACHINE_MAC_ADDRESSES) {
			if (vm[0] == mac[0] && vm[1] == mac[1] && vm[2] == mac[2]) return null;
		}//for

		return mac;
	}//checkValidity

	/**
	 * Returns the hardware address (usually MAC address) of the host interface
	 *
	 * @return a byte array containing the address, or {@code null}
	 *         if the address doesn't exist, is not accessible or
	 *         a security manager is set and the caller does not have
	 *         the permission {@link java.net.NetPermission}("getNetworkInformation")
	 * @throws SocketException If an I/O error occurs.
	 */
	public static byte[] getMACAddress() throws SocketException {
		byte[] macAddress = null;

		Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
		while (nis.hasMoreElements()) {
			NetworkInterface ni = nis.nextElement();
			if (ni.isUp() && !ni.isLoopback()) macAddress = NICUtils.checkValidity(ni.getHardwareAddress());
		}//while

		return macAddress;
	}//getMACAddress

	/**
	 * Returns the hardware address (usually MAC address) of the interface
	 * that has the specified IP address or host name, e.g. "10.0.0.1" or "example.com".
	 *
	 * @param address the IP address or the host name
	 * @return a byte array containing the address, or {@code null}
	 *         if the address doesn't exist, is not accessible or
	 *         a security manager is set and the caller does not have
	 *         the permission {@link java.net.NetPermission}("getNetworkInformation")
	 * @throws SocketException If an I/O error occurs.
	 * @throws UnknownHostException If no IP address for the {@code host} could be found,
	 *                              or a scope_id was specified for a global IPv6 address.
	 */
	public static byte[] getMACAddress(String address) throws SocketException, UnknownHostException {
		// Hardware address of the network interface corresponding to IP address or a host name
		return NetworkInterface.getByInetAddress(InetAddress.getByName(address)).getHardwareAddress();
	}//getMACAddress(String address)

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
	 *         the permission {@link java.net.NetPermission}("getNetworkInformation")
	 * @throws SocketException If an I/O error occurs.
	 * @throws UnknownHostException If no IP address for the {@code host} could be found,
	 *             or a scope_id was specified for a global IPv6 address.
	 */
	public static String getMACAddressString(String address) throws SocketException, UnknownHostException {
		return format(getMACAddress(address));
	}//getMACAddressString
}
