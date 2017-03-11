/*
 * Experts Net
 * Copyright (c) 2017 Makoto Sakaguchi.
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
 *
 * NOTE:
 * This part of the program includes the code of "Apache Commons Net,"
 * which developed at The Apache Software Foundation (http://www.apache.org/)
 * and be distributed in the Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0),
 * by Makoto Sakaguchi on February 20, 2017.
 */
package experts.net.subnet;

import java.util.regex.Pattern;

/**
 * Convenience container for subnet summary information.
 *
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public class SubnetInfo {
	private static final String IPV4_ADDRESS = "(\\d{1,3}\\.){3}\\d{1,3}/\\d{1,2}";
	private static final String IPV6_ADDRESS = "([0-9a-f]{1,4}\\:){7}[0-9a-f]{1,4}/\\d{1,2}";

	int address = 0;
	int cidr = 0;

	/**
	 * Constructor that takes a CIDR-notation string that both IPv4 and IPv6 allow,
	 * e.g. "192.168.0.1/16" or "2001:db8:0:0:0:ff00:42:8329/46"
	 *
	 * NOTE: IPv6 address does NOT allow to omit consecutive sections of zeros in the current version.
	 *
	 * @param address An IPv4 or IPv6 address
	 * @return The class of SubnetInfo
	 */
	public static SubnetInfo getByCIDRNortation(String cidrNotation) {
		if (Pattern.matches(IPV4_ADDRESS, cidrNotation)) {
			return new IP4(cidrNotation);
		} else if (Pattern.matches(IPV6_ADDRESS, cidrNotation)) {
			return null;
		} else {
			throw new IllegalArgumentException("Could not parse [" + cidrNotation + "]");
		}//if
	}//getByCIDRNortation

	public String getAddresss() {
		return null;
	}//getAddressString

	public int getCIDR() {
		return cidr;
	}//getCIDRValue

	/**
	 * Returns an IP address/CIDR format by counting the 1-bit population in the mask address.
	 * IP address: A dot-decimal notation as 192.168.0.1 in IPv4 or
	 * eight groups of four hexadecimal digits and the groups are separated by colons, i.e. 2001:db8:0:0:0:ff00:42:8329 in IPv6
	 * CIDR: 0-32 in IPv4 or 0-128 in IPv6
	 */
	public String getCIDRNotation() {
		return null;
	}//getCIDRNotation

	/**
	 * Return the low address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 in IPv4 if the inclusive flag is false.
	 *
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	public String getLowAddress() {
		return null;
	}// getLowAddress

	/**
	 * Return the high address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 in IPv4 if the inclusive flag is false.
	 *
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	public String getHighAddress() {
		return null;
	}// getHighAddress

	/**
	 * Get the count of available addresses.
	 * Will be zero for CIDR/31 and CIDR/32 in IPv4 if the inclusive flag is false.
	 *
	 * @return the count of addresses, may be zero.
	 */
	public long getAddressCountLong() {
		return 0;
	}//getAddressCountLong

	public String[] getAllAddresses() {
		return null;
	}//getAllAddresses

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses.
	 *
	 * @param address A dot-delimited IPv4 address, e.g. "192.168.0.1", or
	 * an IPv6 address is four hexadecimal digits and the groups are separated by colons, e.g. "2001:db8:0:0:0:ff00:42:8329"
	 * @return True if in range, false otherwise
	 */
	public boolean isInRange(String address) {
		return false;
	}// isInRange
}
