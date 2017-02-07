/*
 * Copyright (C) 2013-2017 Makoto Sakaguchi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * @version 0.2.1-SNAPSHOT
 */
public class MACAddr {
	private byte[] macAddr;

	/**
	 * Constructs a newly allocated MAC address object value represented by the
	 * string in IP address or the host name (e.g. "10.0.0.1" or "example").
	 *
	 * @param address
	 *            IP address or the host name
	 * @throws java.net.SocketException
	 * @throws java.net.UnknownHostException
	 */
	public MACAddr(String address) throws SocketException, UnknownHostException {
		macAddr = getMACAddress(address);
	}// MACAddr

	/**
	 * Returns the value of this MACAddr as a byte array.
	 *
	 * @return the MAC address (string array) represented by this object.
	 */
	public byte[] getMacAddr() {
		return macAddr;
	}// getMacAddr

	/**
	 * Set the MAC address.
	 *
	 * @param address
	 *            IP address or the host name
	 * @throws java.net.SocketException
	 * @throws java.net.UnknownHostException
	 */
	public void setMacAddr(String address) throws SocketException, UnknownHostException {
		macAddr = getMACAddress(address);
	}// setMacAddr

	/**
	 * Returns the MAC address from the IP address or the host name (e.g.
	 * "10.0.0.1" or "example").
	 *
	 * @param address
	 *            IPv4 address or the host name
	 * @return a byte array containing the MAC address
	 * @throws java.net.SocketException
	 * @throws java.net.UnknownHostException
	 */
	public static byte[] getMACAddress(String address) throws SocketException, UnknownHostException {
		// Stored in a nic network interface corresponding to the IP address or
		// the Host name
		NetworkInterface nic = NetworkInterface.getByInetAddress(InetAddress.getByName(address));

		// Returns byte[] the MAC address
		return nic.getHardwareAddress();
	}// getMACAddress

	/**
	 * Convert this MACAddr object to MAC address string "%x:%x:%x:%x:%x:%x"
	 * format.
	 *
	 * @return the raw MAC address "%x:%x:%x:%x:%x:%x" in a string format.
	 */
	@Override
	public String toString() {
		// Converting to a String MAC address obtained
		StringBuilder sb = new StringBuilder().append(Hex.encodeHex(macAddr, false));

		for (int i = 2; i < 17; i += 3) {
			sb.insert(i, ':');
		} // for

		return sb.toString();
	}// toString
}
