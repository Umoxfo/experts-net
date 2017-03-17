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
 */
package experts.net.subnet;

/**
 * Convenience container for subnet summary information.
 *
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public interface SubnetInfo {
	/**
	 * Returns <code>true</code> if the return value of {@link SubnetInfo#getAddressCount()}
	 * includes the network and broadcast addresses. (ONLY USE in IPv4)
	 * @return true if the host count includes the network and broadcast addresses
	 */
	boolean isInclusiveHostCount();

	/**
	 * Sets to <code>true</code> if you want the return value of {@link SubnetInfo#getAddressCount()}
	 * to include the network and broadcast addresses. (ONLY USE in IPv4)
	 * @param inclusiveHostCount true if network and broadcast addresses are to be included
	 */
	void setInclusiveHostCount(boolean inclusiveHostCount);

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses if the address is an IPv4 address.
	 * @param address A dot-delimited IPv4 address, e.g. "192.168.0.1", or
	 *  a colon-hexadecimal IPv6 address, e.g. "2001:db8::ff00:42:8329"
	 * @return True if in range, false otherwise
	 */
	boolean isInRange(String address);

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses if the address is an IPv4 address.
	 * @param address the address to check
	 * @return true if it is in range
	 */
	boolean isInRange(int address);

	/**
	 * Gets the IP address.
	 * IPv4 format: a dot-decimal format, e.g. "192.168.0.1"
	 * IPv6 format: a colon-hexadecimal format, e.g. "2001:db8::ff00:42:8329"
	 * @return a string of the IP address
	 */
	String getAddresss();

	/**
	 * Gets the CIDR suffixes, the count of consecutive 1 bits in the subnet mask.
	 * The range in IPv4 is 0-32, and in IPv6 is 0-128, actually 64 or less.
	 * @return the CIDR suffixes of the address in an integer.
	 */
	int getCIDR();

	/**
	 * Gets a netmask in the address. (ONLY USE IPv4)
	 * @return a string of netmask in a dot-decimal format.
	 */
	String getNetmask();

	/**
	 * Gets a network address in the address. (ONLY USE IPv4)
	 * @return a string of a network address in a dot-decimal format.
	 */
	String getNetworkAddress();

	/**
	 * Gets a broadcast address in the address. (ONLY USE IPv4)
	 * @return a string of a broadcast address in a dot-decimal format.
	 */
	String getBroadcastAddress();

	/**
	 * Gets a CIDR notation, in which the address is followed by a slash character (/) and
	 * the count of counting the 1-bit population in the subnet mask.
	 * IPv4 CIDR notation: e.g. "192.168.0.1/24"
	 * IPv6 CIDR notation: e.g. "2001:db8::ff00:42:8329/48"
	 * @return the CIDR notation of the address and the subnet mask
	 */
	String getCIDRNotation();

	/**
	 * Returns the low address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the address is an IPv4 address and the inclusive flag is false.
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	String getLowAddress();

	/**
	 * Returns the high address as a dotted address in IPv4 or a colon-hexadecimal address in IPv6.-
	 * Will be zero for CIDR/31 and CIDR/32 if the address is an IPv4 address and the inclusive flag is false.
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	String getHighAddress();

	/**
	 * Returns the count of available addresses.
	 * Will be zero for CIDR/31 and CIDR/32 if the address is an IPv4 address and the inclusive flag is false.
	 * @return the count of addresses in a string, may be zero.
	 */
	String getAddressCount();

	/**
	 * Returns the count of available addresses.
	 * Will be zero for CIDR/31 and CIDR/32 if the address is an IPv4 address and the inclusive flag is false.
	 * @return the count of addresses, may be zero.
	 */
	long getAddressCountLong();
}
