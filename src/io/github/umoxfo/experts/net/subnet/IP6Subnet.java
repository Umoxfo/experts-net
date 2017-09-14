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
package io.github.umoxfo.experts.net.subnet;

import io.github.umoxfo.experts.net.ip6.IP6Utils;

import java.math.BigInteger;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Convenience container for IPv6 subnet summary information.
 *
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public final class IP6Subnet extends SubnetInfo {
	private final short[] ip6Address;
	private final int cidr;

	/*
	 * Constructor that takes a raw IPv6 address and CIDR,
	 * e.g. "2001:db8:0:0:0:ff00:42:8329/48"
	 */
	IP6Subnet(byte[] address, int cidr) {
		ip6Address = toShortArray(address);
		this.cidr = cidr;
	}//IP6Subnet

	/*
	 * Creates the minimum address in the network
	 * to which the address belongs, it has all-zero in the host fields.
	 */
	private short[] low() {
		short[] lowAddr = new short[8];

		// Copy of the network prefix in the address
		int index = cidr / 16;
		System.arraycopy(ip6Address, 0, lowAddr, 0, index + 1);

		// Set the out of the network prefix bits.
		lowAddr[index] &= 0xffff << (16 - (cidr % 16));

		return lowAddr;
	}//low

	/*
	 * Creates the maximum address in the network
	 * to which the address belongs, it has all-ones in the host fields.
	 */
	private short[] high() {
		short[] highAddr = new short[8];

		// Copy of the network prefix in the address
		int index = cidr / 16;
		System.arraycopy(ip6Address, 0, highAddr, 0, index + 1);

		// Set the network prefix bits
		highAddr[index] |= 0xffff >> (cidr % 16);

		// Fill the following fields with 1-bits
		for (int i = index + 1; i < 8; i++) highAddr[i] = (short) 0xffff;
		//IntStream.range(index + 1, 8).forEach(i -> highAddr[i] = (short) 0xffff);

		return highAddr;
	}//high


	/**
	 * Returns true if the parameter <code>address</code> is in
	 * the range of usable endpoint addresses for this subnet.
	 *
	 * @param address a colon-delimited address, e.g. 2001:db8:0:0:0:ff00:42:8329
	 * @return true if in range, false otherwise
	 * @throws UnknownHostException see {@link java.net.InetAddress#getByName(String host)}
	 */
	@Override
	public boolean isInRange(String address) throws UnknownHostException {
		InetAddress ia = InetAddress.getByName(address);

		if (ia instanceof Inet6Address) {
			throw new IllegalArgumentException(address + " is not IPv6 address.");
		}//if

		return isInRange(toShortArray(ia.getAddress()));
	}//isInRange(String address)

	/**
	 * Returns true if the parameter <code>address</code> is in
	 * the range of usable endpoint addresses for this subnet.
	 *
	 * @param address an IPv6 address in binary
	 * @return true if in range, false otherwise
	 */
	@Override
	public boolean isInRange(short[] address) {
		int prefixSize = cidr / 16;

		// Have the same network prefix
		for (int i = 0; i < prefixSize; i++) {
			if (address[i] != ip6Address[i]) return false;
		}//for

		// The host identifier is in range between the lowest and the highest addresses
		int addr = address[prefixSize] & 0xffff;
		int lowAddr = low()[prefixSize] & 0xffff;
		int highAddr = high()[prefixSize] & 0xffff;

		return (addr >= lowAddr) && (addr <= highAddr);
	}//isInRange(short[] address)

	/**
	 * Returns the <code>address</code>, that is a colon 16-bit delimited
	 * hexadecimal format for IPv6 addresses, e.g. 2001:db8::ff00:42:8329.
	 *
	 * @return a string of the IP address
	 */
	@Override
	public String getAddress() { return IP6Utils.toTextFormat(ip6Address); }

	/**
	 * Returns the CIDR suffixes, the count of consecutive 1-bit in the subnet mask.
	 * The IPv6 address is between 0 and 128, but it is actually less than 64.
	 *
	 * @return the CIDR suffixes of the address in an integer.
	 */
	@Override
	public int getCIDR() { return cidr; }

	/**
	 * Returns an IPv6-CIDR notation, in which the address is followed by a slash character and
	 * the count of counting the 1-bit population in the subnet mask.
	 *
	 * @return the CIDR notation of the address, e.g. 2001:db8::ff00:42:8329/48
	 */
	@Override
	public String getCIDRNotation() { return IP6Utils.toTextFormat(ip6Address) + "/" + cidr; }

	/**
	 * Returns the low address as a colon-separated IP address.
	 *
	 * @return the IP address in a colon 16-bit delimited hexadecimal format,
	 *         may be "::" if there is no valid address
	 */
	@Override
	public String getLowAddress() { return IP6Utils.toTextFormat(low()); }

	/**
	 * Returns the high address as a colon-separated IP address.
	 *
	 * @return the IP address in a colon 16-bit delimited hexadecimal format,
	 *         may be "::" if there is no valid address
	 */
	@Override
	public String getHighAddress() { return IP6Utils.toTextFormat(high()); }

	/**
	 * Returns the count of available addresses.
	 *
	 * @return the count of addresses in a string, may be zero
	 */
	@Override
	public String getAddressCount() { return new BigInteger("2").pow(128 - cidr).toString(); }

	/**
	 * Returns subnet summary information of the address,
	 * which includes an IP address by CIDR-Notation, the first and
	 * the last addresses of the network, and the number of available addresses
	 * in the network which includes all-zero and all-ones in the host fields,
	 * known as network or broadcast addresses.
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("CIDR-Notation:\t[").append(getCIDRNotation()).append("]\n")
		   .append("First Address:\t[").append(getLowAddress()).append("]\n")
		   .append("Last Address:\t[").append(getHighAddress()).append("]\n")
		   .append("# Addresses:\t[").append(getAddressCount()).append("]");

		return buf.toString();
	}//toString
}
