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
package experts.net.subnet;

import experts.net.subnet.SubnetUtils.IP;

/**
 * Host
 *
 * @author Makoto Sakaguchi
 * @version 2.0.1-SNAPSHOT
 */
public class Hosts {

	/**
	 * Calculate the number of usable hosts from the CIDR.
	 *
	 * @param cidr
	 *            CIDR value
	 * @return Number of available hosts, including the gateway.
	 */
	public static int numberOfHosts(int cidr, IP terget) {
		int size = 0;
		switch (terget) {
			case IPV4:
				size = IP.IPV4.getSize();
				break;
			case IPV6:
				size = IP.IPV6.getSize();
				break;
		}// switch

		// Calculate the number of hosts from the subnet-mask
		int hosts = (int) Math.pow(2, size - cidr);

		// For routed subnets larger than 31 or 127, subtract 2 from the number
		// of available hosts
		if (cidr < size - 1) {
			hosts -= 2;
		}

		// Return number of available hosts, including the gateway
		return hosts;
	}// numberOfHosts

	/**
	 * Calculate the number of total usable hosts from the CIDR.
	 *
	 * @param cidr
	 *            CIDR value
	 * @param prefix
	 *            Prefix size of the subnet
	 *
	 * @return Number of total available hosts, including the gateway.
	 */
	public static int numberOfTotalHosts(int cidr, int prefix) {
		// Multiply the usable hosts per subnet by the calculated available
		// subnets.
		return numberOfHosts(prefix, IP.IPV4) * (prefix - cidr + 1);
	}// numberOfHosts
}
