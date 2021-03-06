/*
 * Experts Net
 * Copyright (c) 2017 Makoto Sakaguchi.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package experts.net.ip6;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Makoto Sakaguchi
 *
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public abstract class IP6 {
	/**
	 * 48 bits devited by 16bits of each for Unique Local IPv6 Unicast Addresses (ULUA) or 64 bits or less for Global Unicast Address (GUA).
	 */
	protected List<Short> globalID;

	/**
	 * 16 bits for ULUA or 64 - n bits (these n bits equal GUA of the Global ID bits length) for GUA.
	 */
	protected List<Short> subnetID;

	/**
	 * 64 bits for ULUA and GUA.
	 */
	protected List<Short> interfaceID;

	/**
	 * Returns the Global ID represented by short type list.
	 *
	 * @return list of the Global ID
	 */
	public List<Short> getGlobalID() {
		return globalID;
	}// getGlobalID

	/**
	 * Sets the Global ID of IPv6 address.
	 *
	 * @param globalID
	 */
	abstract public void setGlobalID(String globalID);

	/**
	 * Returns the Subnet ID represented by short type list.
	 *
	 * @return list of the Subnet ID
	 */
	public List<Short> getSubnetID() {
		return subnetID;
	}// getSubnetID

	/**
	 * Sets the Subnet ID of IPv6 address.
	 *
	 * @param subntID
	 */
	abstract public void setSubnetID(String subnetID);

	/**
	 * Returns the Interface ID represented by short type list.
	 *
	 * @return list of the Interface ID
	 */
	public List<Short> getInterfaceID() {
		return interfaceID;
	}// getInterfaceID

	/**
	 * Sets the Interface ID of IPv6 address.
	 *
	 * @param interfaceID
	 */
	abstract public void setInterfaceID(String interfaceID);

	/**
	 * Build the IPv6 address.
	 *
	 * @return a string representation of the IPv6 address
	 */
	@Override
	public String toString() {
		ArrayList<Short> ipv6 = new ArrayList<>(8);

		// Set Global ID
		ipv6.addAll(globalID);

		// Set Subnet ID
		ipv6.addAll(subnetID);

		// Set Interface ID
		ipv6.addAll(interfaceID);

		// Replace consecutive sections of zeros to a double colon (::).
		return IP6Utils.buildIP6String(ipv6);
	}// toString
}
