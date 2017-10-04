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
package io.github.umoxfo.experts.net.ip6;

/**
 * Convenience container for IPv6 address summary information.
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public abstract class IP6 {
	static final int INTERFACE_ID_LENGTH = 8;

	/*
	 * an identifier of a site (a cluster of subnets/links).
	 * 48 bits for Unique Local IPv6 Unicast Addresses (ULUA),
	 * the {@code FD00::/8} prefix and 40-bit global identifier format, or
	 * 64 bits or less for Global Unicast Address (GUA).
	 */
	byte[] globalID;

	/*
	 * an identifier of a subnet within the site.
	 * 16 bits for ULUA or 64 - n bits (these n bits equal GUA of the Global ID bits length) for GUA.
	 *
	 */
	byte[] subnetID;

	/*
	 * an identifier of interfaces on a link.
	 * 64 bits for Interface ID.
	 */
	byte[] interfaceID;

	/**
	 * Returns the Global ID in binary.
	 * See <a href="https://tools.ietf.org/html/rfc4193#section-3.2">Section 3.2 of
	 * RFC 4193</a> for additional information.
	 *
	 * @return the Global ID in a byte array
	 */
	public abstract byte[] getGlobalID();

	/**
	 * Returns the Subnet ID in binary.
	 *
	 * @return the Subnet ID in a byte array
	 */
	public abstract byte[] getSubnetID();

	/**
	 * Returns the Interface ID in binary.
	 *
	 * @return the Interface ID in a byte array
	 */
	public byte[] getInterfaceID() { return interfaceID; }

	/*
	 * Sets an Interface ID which is used to identify interfaces on a link.
	 *
	 * @param id must be up to 64 bits
	 */
	byte[] checkInterfaceID(byte[] id) {
		if (id.length != INTERFACE_ID_LENGTH) throw new IllegalArgumentException("Interface ID must be 64 bits.");

		return id;
	}//checkInterfaceID

	/**
	 * Convert IPv6 binary address into a canonical format
	 *
	 * @return the IPv6 address in the colon 16-bit delimited hexadecimal format
	 * @see IP6Utils#toTextFormat(byte[])
	 */
	@Override
	public String toString() {
		// Collect into a single array
		byte[] addr = new byte[16];

		System.arraycopy(globalID, 0, addr, 0, globalID.length);
		System.arraycopy(subnetID, 0, addr, globalID.length, subnetID.length);
		System.arraycopy(interfaceID, 0, addr, (globalID.length + subnetID.length), interfaceID.length);

		// Replace consecutive sections of zeros to a double colon (::)
		return IP6Utils.toTextFormat(addr);
	}//toString
}
