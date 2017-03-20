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
 * by Makoto Sakaguchi form March 11, 2017 to March 14, 2017.
 */
package experts.net.subnet;

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
	private static final long UNSIGNED_INT_MASK = 0x0FFFF_FFFFL;
	private static final int NBITS = 32;

	private final int address;
	private final int cidr;
	private final int netmask;
	private final int network;
	private final int broadcast;

	/* Whether the broadcast/network address on IPv4 or the network address on IPv6 are included in host count */
	private boolean inclusiveHostCount = false;

	/*
	 * Constructor that takes a CIDR-notation string, e.g. "192.168.0.1/16"
	 *
	 * @param cidrNotation A CIDR-notation string, e.g. "192.168.0.1/16"
	 * @throws IllegalArgumentException
	 *             if the parameter is invalid,
	 *             i.e. does not match n.n.n.n/m where n=1-3 decimal digits, m is in range 0-32
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
	 * Returns <code>true</code> if the return value of {@link IP4Subnet#getAddressCount()}
	 * includes the network and broadcast addresses.
	 *
	 * @return true if the host count includes the network and broadcast addresses
	 */
	@Override
	public boolean isInclusiveHostCount() {
		return inclusiveHostCount;
	}//isInclusiveHostCount

	/**
	 * Set to <code>true</code> if you want the return value of {@link IP4Subnet#getAddressCount()}
	 * to include the network and broadcast addresses.
	 *
	 * @param inclusiveHostCount true if network and broadcast addresses are to be included
	 */
	@Override
	public void setInclusiveHostCount(boolean inclusiveHostCount) {
		this.inclusiveHostCount = inclusiveHostCount;
	}//setInclusiveHostCount

	// long versions of the values (as unsigned int) which are more suitable for range checking
	private long networkLong() {
		return network & UNSIGNED_INT_MASK;
	}//networkLong

	private long broadcastLong() {
		return broadcast & UNSIGNED_INT_MASK;
	}//broadcastLong

	/*
	 * Creates the minimum address in the network to which the address belongs.
	 *
	 * inclusiveHostCount
	 *  - true the network address
	 *  - false the first address of the available as host addresses or 0 if no corresponding address.
	 */
	private int low() {
		return inclusiveHostCount ? network : (broadcastLong() - networkLong()) > 1 ? network + 1 : 0;
	}//low

	/*
	 * Creates the minimum address in the network to which the address belongs.
	 *
	 * inclusiveHostCount
	 *  - true the network address
	 *  - false the last address of the available as host addresses or 0 if no corresponding address.
	 */
	private int high() {
		return inclusiveHostCount ? broadcast : (broadcastLong() - networkLong()) > 1 ? broadcast - 1 : 0;
	}//high

	/*
	 * Converts a raw address in network byte order to a packed integer format
	 */
	private static int toInteger(byte[] address) {
		/* Check range of each element and convert to integer */
		int addr = 0;
		for (int i = 0; i < 4; i++) {
			int n = SubnetUtils.checkRange((address[i] & 0xff), 0, 255);
			addr |= (n & 0xff) << (8 * (3 - i));
		}//for

		return addr;
	}//toInteger

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses.
	 *
	 * @param address a dot-delimited IPv4 address, e.g. "192.168.0.1"
	 * @return true if in range, false otherwise
	 */
	@Override
	public boolean isInRange(String address) {
		return isInRange(SubnetUtils.toInteger(address));
	}//isInRange

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses.
	 *
	 * @param address an IPv4 address in binary
	 * @return true if in range, false otherwise
	 */
	@Override
	public boolean isInRange(int address) {
		long addLong = address & UNSIGNED_INT_MASK;
		long netLong = networkLong();
		long broadLong = broadcastLong();
		return (addLong > netLong) && (addLong < broadLong);
	}//isInRange

	@Override
	public String getAddresss() {
		return SubnetUtils.format(address, '.');
	}//getAddressString

	@Override
	public int getCIDR() {
		return cidr;
	}//getCIDRValue

	@Override
	public String getNetmask() {
		return SubnetUtils.format(netmask, '.');
	}//getNetmaskString

	@Override
	public String getNetworkAddress() {
		return SubnetUtils.format(network, '.');
	}//getNetworkAddressString

	@Override
	public String getBroadcastAddress() {
		return SubnetUtils.format(broadcast, '.');
	}//getBroadcastAddressString

	/**
	 * Returns a CIDR notation, in which the address is followed by a slash character (/) and
	 * the count of counting the 1-bit population in the subnet mask.
	 *
	 * @return the CIDR notation of the address, e.g. "192.168.0.1/24"
	 */
	@Override
	public String getCIDRNotation() {
		return SubnetUtils.format(address, '.') + "/" + cidr;
	}//getCIDRNotation

	/**
	 * Return the low address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
	 *
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	@Override
	public String getLowAddress() {
		return SubnetUtils.format(low(), '.');
	}//getLowAddress

	/**
	 * Return the high address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
	 *
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	@Override
	public String getHighAddress() {
		return SubnetUtils.format(high(), '.');
	}//getHighAddress

	/**
	 * Returns the count of available addresses.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
	 *
	 * @return the count of addresses, may be zero.
	 */
	@Override
	public long getAddressCountLong() {
		long b = broadcastLong();
		long n = networkLong();
		long count = (b - n) + (inclusiveHostCount ? 1 : -1);
		return count < 0 ? 0 : count;
	}//getAddressCountLong

	/**
	 * Returns subnet summary information of the address,
	 * which includes an IP address by CIDR-Notation with the netmask,
	 * network address, broadcast address, the first and last addresses of the network,
	 * and the number of available addresses in the network which includes
	 * the network and broadcast addresses if the inclusive flag is true.
	 */
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append("CIDR-Notation:\t[").append(getCIDRNotation()).append("]")
		.append(" Netmask: [").append(getNetmask()).append("]\n")
		.append("Network:\t[").append(getNetworkAddress()).append("]\n")
		.append("Broadcast:\t[").append(getBroadcastAddress()).append("]\n")
		.append("First Address:\t[").append(getLowAddress()).append("]\n")
		.append("Last Address:\t[").append(getHighAddress()).append("]\n")
		.append("# Addresses:\t[").append(getAddressCountLong()).append("]\n");

		return buf.toString();
	}//toString
}
