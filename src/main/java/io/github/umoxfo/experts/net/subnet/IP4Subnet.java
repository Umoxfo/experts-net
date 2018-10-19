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
package io.github.umoxfo.experts.net.subnet;

import io.github.umoxfo.experts.net.subnet.address.IPAddress;

/**
 * Convenience container for IPv4 subnet summary information.
 *
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public final class IP4Subnet extends SubnetInfo {
	/* Mask to convert unsigned int to a long (i.e. keep 32 bits) */
	private static final long UNSIGNED_INT_MASK = 0xFFFF_FFFFL;
	private static final int NBITS = 32;

	private final int address;
	private final int cidr;
	private final int netmask;
	private final int network;
	private final int broadcast;

	/*
	 * Whether the broadcast/network address on IPv4 or the network address on
	 * IPv6 are included in host count
	 */
	private boolean inclusiveHostCount = false;

	/*
	 * Constructor that takes CIDR-notation, e.g. "192.168.0.1/16".
	 */
	IP4Subnet(byte[] address, int cidr) {
		this.address = toInteger(address);

		this.cidr = cidr;

		/* Create a binary netmask from the number of bits specification /x */
		netmask = (int) (UNSIGNED_INT_MASK << (NBITS - cidr));

		/* Calculate base network address */
		network = this.address & netmask;

		/* Calculate broadcast address */
		broadcast = network | ~netmask;
	}//IP4Subnet

	/**
	 * Returns {@code true} if the return value of {@link #getAddressCount()}
	 * includes the network and broadcast addresses.
	 *
	 * @return {@code true} if the host count includes the network and broadcast addresses
	 */
	@Override
	public boolean isInclusiveHostCount() { return inclusiveHostCount; }

	/**
	 * Set to {@code true} if you want the return value of {@link #getAddressCount()}
	 * to include the network and broadcast addresses.
	 *
	 * @param inclusiveHostCount {@code true} if network and broadcast addresses are included
	 */
	@Override
	public void setInclusiveHostCount(boolean inclusiveHostCount) { this.inclusiveHostCount = inclusiveHostCount; }

	@Override
	public String getAddress() { return SubnetUtils.format(address); }

	@Override
	public int getCIDR() { return cidr; }

	@Override
	public String getNetmask() { return SubnetUtils.format(netmask); }

	@Override
	public String getNetworkAddress() { return SubnetUtils.format(network); }

	@Override
	public String getBroadcastAddress() { return SubnetUtils.format(broadcast); }

	/**
	 * Returns a CIDR notation, in which the address is followed by
	 * a slash character and the count of counting the 1-bit population in the subnet mask.
	 *
	 * @return the CIDR notation of the address, e.g. {@code 192.168.0.1/24}
	 */
	@Override
	public String getCIDRNotation() { return SubnetUtils.format(address) + '/' + cidr; }

	/**
	 * Return the low address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is {@code false}.
	 *
	 * @return the IP address in dotted format, may be {@code 0.0.0.0} if there is no valid address
	 */
	@Override
	public String getLowAddress() { return SubnetUtils.format(low()); }

	/**
	 * Return the high address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is {@code false}.
	 *
	 * @return the IP address in dotted format, may be {@code 0.0.0.0} if there is no valid address
	 */
	@Override
	public String getHighAddress() { return SubnetUtils.format(high()); }

	/**
	 * Returns the number of available addresses.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is {@code false}.
	 *
	 * @return the number of addresses, may be zero.
	 */
	@Override
	public long getAddressCountLong() {
		long count = (long) Math.pow(2, 32 - cidr);

		if (!inclusiveHostCount) {
			// Length of the network prefix is larger than 31, subtract 2 from the number of available hosts
			if (cidr < 31) {
				count -= 2;
			} else {
				count = 0;
			}//if-else
		}//if

		return count;
	}//getAddressCount

	/*
	 * Converts a raw address in network byte order to a packed integer format
	 */
	private static int toInteger(byte[] address) {
		/* Check range of each element and convert to integer */
		int addr = (address[0] & 0xff) << 24;
		addr |= (address[1] & 0xff) << 16;
		addr |= (address[2] & 0xff) << 8;
		addr |= address[3] & 0xff;

		return addr;
	}//toInteger

	/**
	 * Returns {@code true} if the parameter {@code address} is in
	 * the range of usable endpoint addresses for this subnet.
	 * This excludes the network and broadcast addresses.
	 *
	 * @param address a dot-delimited IPv4 address, e.g. {@code 192.168.0.1}
	 * @return {@code true} if in range, {@code false} otherwise
	 */
	@Override
	public boolean isInRange(String address) {
		return isInRange(toInteger(IPAddress.toNumericFormatIPv4(address)));
	}//isInRange(String)

	/**
	 * Returns {@code true} if the parameter {@code address} is in
	 * the range of usable endpoint addresses for this subnet.
	 * This excludes the network and broadcast addresses.
	 *
	 * @param address an IPv4 address in binary
	 * @return {@code true} if in range, {@code false} otherwise
	 */
	@Override
	public boolean isInRange(int address) {
		long addLong = address & UNSIGNED_INT_MASK;

		return (addLong > networkLong()) && (addLong < broadcastLong());
	}//isInRange(int)

	// long versions of the values (as unsigned int) which are more suitable for range checking
	private long networkLong() { return network & UNSIGNED_INT_MASK; }

	private long broadcastLong() { return broadcast & UNSIGNED_INT_MASK; }

	/**
	 * Returns the subnet summary information of the address,
	 * which includes an IP address by CIDR-Notation with the netmask,
	 * network address, broadcast address, the first and last addresses of the network,
	 * and the number of available addresses in the network which includes
	 * the network and broadcast addresses if the inclusive flag is {@code true}.
	 */
	@Override
	public String toString() {
		@SuppressWarnings("StringBufferReplaceableByString")
		StringBuilder buf = new StringBuilder();
		buf.append("CIDR-Notation:\t[").append(getCIDRNotation()).append(']')
		   .append(" Netmask: [").append(getNetmask()).append("]\n")
		   .append("Network:\t[").append(getNetworkAddress()).append("]\n")
		   .append("Broadcast:\t[").append(getBroadcastAddress()).append("]\n")
		   .append("First Address:\t[").append(getLowAddress()).append("]\n")
		   .append("Last Address:\t[").append(getHighAddress()).append("]\n")
		   .append("# Addresses:\t[").append(getAddressCountLong()).append(']');

		return buf.toString();
	}//toString

	/*
	 * Creates the minimum address in the network to which the address belongs.
	 *
	 * inclusiveHostCount
	 * - true the network address
	 * - false the first address of the available as host addresses or 0 if no corresponding address.
	 */
	private int low() {
		if (inclusiveHostCount) {
			return network;
		} else {
			if ((broadcastLong() - networkLong()) > 1) {
				return network + 1;
			} else {
				return 0;
			}//if-else
		}//if
	}

	/*
	 * Creates the minimum address in the network to which the address belongs.
	 *
	 * inclusiveHostCount
	 * - true the network address
	 * - false the last address of the available as host addresses or 0 if no corresponding address.
	 */
	private int high() {
		if (inclusiveHostCount) {
			return broadcast;
		} else {
			if ((broadcastLong() - networkLong()) > 1) {
				return broadcast - 1;
			} else {
				return 0;
			}//if-else
		}//if-else
	}
}
