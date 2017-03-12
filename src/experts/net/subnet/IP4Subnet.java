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
 * by Makoto Sakaguchi on March 11, 2017.
 */
package experts.net.subnet;

import java.util.ArrayList;

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
	IP4Subnet(String cidrNotation) {
		String[] addr = cidrNotation.split("/");

		address = SubnetUtils.toInteger(addr[0]);

		cidr = SubnetUtils.checkRange(Integer.parseInt(addr[1]), 0, NBITS);

		/* Create a binary netmask from the number of bits specification /x */
		netmask = (int) (UNSIGNED_INT_MASK << (NBITS - cidr));

		/* Calculate base network address */
		network = address & netmask;

		/* Calculate broadcast address */
		broadcast = network | ~netmask;
	}//IP4Subnet(String cidrNotation)

	/**
	 * Returns <code>true</code> if the return value of {@link IP4Subnet#getAddressCount()}
	 * includes the network and broadcast addresses.
	 *
	 * @return true if the host count includes the network and broadcast addresses
	 */
	public boolean isInclusiveHostCount() {
		return inclusiveHostCount;
	}// isInclusiveHostCount

	/**
	 * Set to <code>true</code> if you want the return value of {@link IP4Subnet#getAddressCount()}
	 * to include the network and broadcast addresses.
	 *
	 * @param inclusiveHostCount true if network and broadcast addresses are to be included
	 */
	public void setInclusiveHostCount(boolean inclusiveHostCount) {
		this.inclusiveHostCount = inclusiveHostCount;
	}// setInclusiveHostCount

	// long versions of the values (as unsigned int) which are more suitable for range checking
	private long networkLong() {
		return network & UNSIGNED_INT_MASK;
	}// networkLong

	private long broadcastLong() {
		return broadcast & UNSIGNED_INT_MASK;
	}// broadcastLong

	private int low() {
		return inclusiveHostCount ? network : (broadcastLong() - networkLong()) > 1 ? network + 1 : 0;
	}// low

	private int high() {
		return inclusiveHostCount ? broadcast : (broadcastLong() - networkLong()) > 1 ? broadcast - 1 : 0;
	}// high

	/*
	 * Converts a packed integer address into dotted decimal format
	 */
	private String format(int val) {
		int ret[] = new int[4];
		for (int i = 3; i >= 0; i--) {
			ret[i] = (val >>> (8 * (3 - i))) & 0xff;
		}//for

		return SubnetUtils.format(ret, '.');
	}//format

	/**
	 * Creates a dotted decimal address and a dotted decimal mask.
	 *
	 * @param address An IP address, e.g. "192.168.0.1"
	 * @param mask A dotted decimal netmask e.g. "255.255.0.0"
	 * @throws IllegalArgumentException
	 *             if the address or mask is invalid,
	 *             i.e. the address does not match n.n.n.n where n=1-3 decimal digits, or
	 *             the mask does not match n.n.n.n which n={0, 128, 192, 224, 240, 248, 252, 254, 255}
	 *             and after the 0-field, it is all zeros.
	 */
	public static IP4Subnet getBySubnet(String address, String mask) {
		return new IP4Subnet(address  + "/" + SubnetUtils.toCIDR(mask));
	}//getBySubnet

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses.
	 *
	 * @param address A dot-delimited IPv4 address, e.g. "192.168.0.1"
	 * @return True if in range, false otherwise
	 */
	@Override
	public boolean isInRange(String address) {
		return isInRange(SubnetUtils.toInteger(address));
	}// isInRange

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses.
	 *
	 * @param address An IPv4 address in binary
	 * @return True if in range, false otherwise
	 */
	public boolean isInRange(int address) {
		long addLong = address & UNSIGNED_INT_MASK;
		long lowLong = low() & UNSIGNED_INT_MASK;
		long highLong = high() & UNSIGNED_INT_MASK;
		return (addLong >= lowLong) && (addLong <= highLong);
	}// isInRange

	@Override
	public String getAddresss() {
		return format(address);
	}//getAddressString

	@Override
	public int getCIDR() {
		return cidr;
	}//getCIDRValue

	public String getNetmask() {
		return format(netmask);
	}//getNetmaskString

	public String getNetworkAddress() {
		return format(network);
	}//getNetworkAddressString

	public String getBroadcastAddress() {
		return format(broadcast);
	}//getBroadcastAddressString

	/**
	 *  Returns a single xxx.xxx.xxx.xxx/yy format by counting the 1-bit population in the mask address.
	 */
	@Override
	public String getCIDRNotation() {
		return format(address) + "/" + cidr;
	}//getCIDRNotation

	/**
	 * Return the low address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
	 *
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	@Override
	public String getLowAddress() {
		return format(low());
	}// getLowAddress

	/**
	 * Return the high address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
	 *
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	@Override
	public String getHighAddress() {
		return format(high());
	}// getHighAddress

	/**
	 * Get the count of available addresses.
	 * Will be zero for CIDR/31 and CIDR/32 if the inclusive flag is false.
	 *
	 * @return the count of addresses, may be zero.
	 */
	public long getAddressCountLong() {
		long b = broadcastLong();
		long n = networkLong();
		long count = (b - n) + (inclusiveHostCount ? 1 : -1);
		return count < 0 ? 0 : count;
	}// getAddressCountLong

	@Override
	public String[] getAllAddresses() {
		long ct = getAddressCountLong();
		ArrayList<String> addresses = new ArrayList<>();

		if (ct != 0) {
			int high = high();
			for (int addr = low(); addr <= high; addr++) {
				addresses.add(format(addr));
			}//for
		}//if

		addresses.trimToSize();
		return addresses.toArray(new String[addresses.size()]);
	}//getAllAddresses


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
	}// toString
}
