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
package io.github.umoxfo.experts.net.ip6;

import java.util.ArrayList;

/**
 * Convenience container for IPv6 address summary information.
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public abstract class IP6 {
	/**
	 * 48 bits for Unique Local IPv6 Unicast Addresses (ULUA) or 64 bits or less for Global Unicast Address (GUA).
	 */
	short[] globalID;

	/**
	 * 16 bits for ULUA or 64 - n bits (these n bits equal GUA of the Global ID bits length) for GUA.
	 */
	short[] subnetID;

	/**
	 * 64 bits for ULUA and GUA.
	 */
	short[] interfaceID;

	/**
	 * Returns the Global ID represented by short type list.
	 *
	 * @return list of the Global ID
	 */
	public short[] getGlobalID() { return globalID; }

	/**
	 * Sets the Global ID of IPv6 address.
	 *
	 * @param globalID a global ID of an IP address
	 */
	public abstract void setGlobalID(String globalID);

	/**
	 * Returns the Subnet ID.
	 *
	 * @return list of the Subnet ID
	 */
	public short[] getSubnetID() { return subnetID; }

	/**
	 * Sets the Subnet ID of IPv6 address.
	 *
	 * @param subnetID a subnet ID of an IP address
	 */
	public abstract void setSubnetID(String subnetID);

	/**
	 * Returns the Interface ID represented by short type list.
	 *
	 * @return list of the Interface ID
	 */
	public short[] getInterfaceID() { return interfaceID; }

	/**
	 * Sets the Interface ID of IPv6 address.
	 *
	 * @param interfaceID an interface ID of an IP address
	 */
	public abstract void setInterfaceID(String interfaceID);

	/**
	 * Build the IPv6 address.
	 *
	 * @return the IPv6 address in the colon 16-bit delimited hexadecimal format
	 */
	@Override
	public String toString() {
		ArrayList<Short> ipv6 = new ArrayList<>(8);

		// Collect into a single array
		short[] addr = new short[8];

		System.arraycopy(globalID, 0, addr, 0, globalID.length);
		System.arraycopy(subnetID, 0, addr, globalID.length, subnetID.length);
		System.arraycopy(interfaceID, 0, addr, (globalID.length + subnetID.length), interfaceID.length);

		// Set into the Array List
		for (short s : addr) {
			ipv6.add(s);
		}//for

		/*
		ipv6.addAll(Arrays.asList(globalID));
		ipv6.addAll(Arrays.asList(subnetID));
		ipv6.addAll(Arrays.asList(interfaceID));
		*/

		// Replace consecutive sections of zeros to a double colon (::)
		return IP6Utils.toTextFormat(ipv6);
	}//toString
}
