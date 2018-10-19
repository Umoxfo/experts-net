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
package io.github.umoxfo.experts.net.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;

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
		{0x00, 0x03, (byte) 0xFF},

		/* Hyper-V */
		{0x00, 0x15, 0x5D},

		/* TAP-Windows */
		{0x00, (byte) 0xFF, 0x69}
	};
	private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

	private NICUtils() { throw new IllegalStateException("Utility class"); }

	/**
	 * Returns the hardware address (usually MAC address) of the host interface
	 *
	 * @return a byte array containing the address, or {@code null}
	 *
	 * @throws SocketException If an I/O error occurs.
	 * @see NetworkInterface#getHardwareAddress()
	 */
	public static byte[] getMACAddress() throws SocketException {
		byte[] macAddress = null;

		Iterator<NetworkInterface> nis = NetworkInterface.networkInterfaces().iterator();
		while (nis.hasNext()) {
			NetworkInterface ni = nis.next();
			if (ni.isUp() && !ni.isLoopback()) macAddress = NICUtils.checkValidity(ni.getHardwareAddress());
		}//while

		return macAddress;
	}//getMACAddress

	/*
	 * Returns false if the hardware address is not from a virtual machine.
	 */
	private static byte[] checkValidity(byte[] mac) {
		for (byte[] vm : VIRTUAL_MACHINE_MAC_ADDRESSES) {
			if (vm[0] == mac[0] && vm[1] == mac[1] && vm[2] == mac[2]) return null;
		}//for

		return mac;
	}//checkValidity

	/**
	 * Returns the hardware address (usually MAC address) of the interface
	 * that has the specified IP address or host name, e.g. {@code 10.0.0.1} or {@code example.com}
	 * in the standard (IEEE 802) format for printing MAC-48 addresses in human-friendly form,
	 * e.g. {@code 01-23-45-67-89-AB}.
	 *
	 * @param address a host name or a textual representation of its IP address
	 * @return the address in the MAC address format, or {@code null}
	 *
	 * @throws SocketException If an I/O error occurs.
	 * @throws UnknownHostException If no IP address for the {@code host} could be found
	 * @see #getMACAddress(String)
	 */
	public static String getMACAddressString(String address) throws SocketException, UnknownHostException {
		return format(getMACAddress(address));
	}//getMACAddressString

	/**
	 * Returns the hardware address (usually MAC address) of the interface
	 * that has the specified IP address or host name, e.g. {@code 10.0.0.1} or {@code example.com}.
	 *
	 * @param address the IP address or the host name
	 * @return a byte array containing the address, or {@code null}
	 *
	 * @throws SocketException If an I/O error occurs.
	 * @throws UnknownHostException If no IP address for the {@code host} could be found
	 * @see InetAddress#getByName(String)
	 * @see NetworkInterface#getHardwareAddress
	 */
	public static byte[] getMACAddress(String address) throws SocketException, UnknownHostException {
		// Hardware address of the network interface corresponding to IP address or a host name
		return NetworkInterface.getByInetAddress(InetAddress.getByName(address)).getHardwareAddress();
	}//getMACAddress(String address)

	/*
	 * Converts a byte array contains a hardware address to the printing MAC-48 addresses,
	 * which is six groups of two hexadecimal digits, separated by hyphens in transmission order:
	 * e.g. 00-1B-63-84-45-E6.
	 */
	private static String format(byte[] macAddr) {
		StringBuilder sb = new StringBuilder(17);

		sb.append(DIGITS[(macAddr[0] >>> 4) & 0xf]).append(DIGITS[macAddr[0] & 0xf]);
		for (int i = 1; i < 6; i++) {
			sb.append('-').append(DIGITS[(macAddr[i] >>> 4) & 0xf]).append(DIGITS[macAddr[i] & 0xf]);
		}//for

		return sb.toString();
	}//format
}
