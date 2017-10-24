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
package io.github.umoxfo.experts.net.subnet;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Convenience container for subnet summary information.
 *
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public class SubnetInfo {
	/**
	 * Returns {@code true} if the return value of {@link #getAddressCount()}
	 * includes the network and broadcast addresses. (ONLY USE in IPv4)
	 *
	 * @return {@code true} if the host count includes the network and broadcast addresses
	 */
	public boolean isInclusiveHostCount() { return false; }

	/**
	 * Sets to {@code true} if you want the return value of {@link #getAddressCount()}
	 * to include the network and broadcast addresses. (ONLY USE in IPv4)
	 *
	 * @param inclusiveHostCount {@code true} if network and broadcast addresses are to be included
	 */
	public void setInclusiveHostCount(boolean inclusiveHostCount) {}

	/**
	 * Returns {@code true} if the parameter {@code address} is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses if the address is IPv4 address.
	 *
	 * @param address a dot-delimited IPv4 address, e.g. {@code 192.168.0.1} or
	 *            a colon-hexadecimal IPv6 address, e.g. {@code 2001:db8::ff00:42:8329}
	 * @return {@code true} if in range, {@code false} otherwise
	 * @throws UnknownHostException see {@link InetAddress#getByName(String)}
	 */
	public boolean isInRange(String address) throws UnknownHostException { return false; }

	/**
	 * Returns {@code true} if the parameter {@code address} is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses if the address is IPv4 address.
	 *
	 * @param address the IP address in an integer
	 * @return {@code true} if it is in range
	 */
	public boolean isInRange(int address) { return false; }

	/**
	 * Returns {@code true} if the parameter {@code address} is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses if the address is IPv4 address.
	 *
	 * @param address the IP address in network byte order
	 * @return {@code true} if it is in range
	 */
	public boolean isInRange(byte[] address) { return false; }

	/**
	 * Returns the IP address.
	 ^ <ul style="list-style-type: none">
	 *     <li>IPv4 format: a dot-decimal format, e.g. {@code 192.168.0.1}</li>
	 *     <li>IPv6 format: a colon-hexadecimal format, e.g. {@code 2001:db8::ff00:42:8329}</li>
	 * </ul>
	 *
	 * @return the IP address in a string format
	 */
	public String getAddress() { return null; }

	/**
	 * Returns the CIDR suffixes, the count of consecutive 1 bits in the subnet mask.
	 * The range in IPv4 is 0-32, and in IPv6 is 0-128, actually 64 or less.
	 *
	 * @return the CIDR suffixes of the address in an integer
	 */
	public int getCIDR() { return 0; }

	/**
	 * Returns the netmask in the address. (ONLY USE IPv4)
	 *
	 * @return the netmask in a dot-decimal format
	 */
	public String getNetmask() { return null; }

	/**
	 * Returns the network address in the address. (ONLY USE IPv4)
	 *
	 * @return the network address in a dot-decimal format
	 */
	public String getNetworkAddress() { return null; }

	/**
	 * Returns the broadcast address in the address. (ONLY USE IPv4)
	 *
	 * @return the broadcast address in a dot-decimal format
	 */
	public String getBroadcastAddress() { return null; }

	/**
	 * Returns the CIDR notation, in which the address is followed by a slash and
	 * the count of counting the 1-bit population in the subnet mask.
	 * <ul style="list-style-type: none">
	 *     <li>IPv4 CIDR notation: e.g. {@code 192.168.0.1/24}</li>
	 *     <li>IPv6 CIDR notation: e.g. {@code 2001:db8::ff00:42:8329/48}</li>
	 * </ul>
	 *
	 * @return the CIDR notation of the address
	 */
	public String getCIDRNotation() { return null; }

	/**
	 * Returns the low address as a dotted or colon-separated IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
	 * the inclusive flag is {@code false}.
	 *
	 * @return the IP address in dotted format or in a colon 16-bit delimited hexadecimal format,
	 *         may be {@code 0.0.0.0} or {@code ::} if there is no valid address
	 */
	public String getLowAddress() { return null; }

	/**
	 * Returns the high address as a dotted or colon-separated IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
	 * the inclusive flag is {@code false}.
	 *
	 * @return the IP address in dotted format or in a colon 16-bit delimited
	 *         hexadecimal format, may be {@code 0.0.0.0} or {@code ::} if there is no valid address
	 */
	public String getHighAddress() { return null; }

	/**
	 * Returns the number of available addresses.
	 *
	 * @return the count of addresses in a string, may be zero
	 */
	public BigInteger getAddressCount() { return null; }

	/**
	 * Returns the number of available addresses.
	 * Will be zero for CIDR/31 and CIDR/32 if the address is IPv4 address and
	 * the inclusive flag is {@code false}.
	 *
	 * @return the count of addresses, may be zero
	 */
	public long getAddressCountLong() { return 0; }
}
