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

import java.util.ArrayList;

/**
 * Convenience container for subnet summary information.
 *
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public class SubnetInfo {
	/* Mask to convert unsigned int to a long (i.e. keep 32 bits) */
	private static final long UNSIGNED_INT_MASK = 0x0FFFFFFFFL;

	private int address = 0;
	private int cidr = 0;
	private int netmask = 0;
	private int network = 0;
	private int broadcast = 0;

	/* Whether the broadcast/network address on IPv4 or the network address on IPv6 are included in host count */
	private boolean inclusiveHostCount = false;

	public String getAddress() {
		return format(address);
	}//getAddress

	public int getCIDR() {
		return cidr;
	}//getCIDR

	public String getNetmask() {
		return format(netmask);
	}//getNetmask

	public String getNetworkAddress() {
		return format(network);
	}//getNetworkAddress

	public String getBroadcastAddress() {
		return format(broadcast);
	}//getBroadcastAddress

	/**
	 * Returns <code>true</code> if the return value of {@link SubnetInfo#getAddressCount()}
	 * includes the network and broadcast addresses.
	 *
	 * @return true if the host count includes the network and broadcast addresses
	 */
	public boolean isInclusiveHostCount() {
		return inclusiveHostCount;
	}// isInclusiveHostCount

	/**
	 * Set to <code>true</code> if you want the return value of {@link SubnetInfo#getAddressCount()}
	 * to include the network and broadcast addresses.
	 *
	 * @param inclusiveHostCount true if network and broadcast addresses are to be included
	 */
	public void setInclusiveHostCount(boolean inclusiveHostCount) {
		this.inclusiveHostCount = inclusiveHostCount;
	}// setInclusiveHostCount

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses.
	 *
	 * @param address A dot-delimited IPv4 address, e.g. "192.168.0.1"
	 * @return True if in range, false otherwise
	 */
	public boolean isInRange(String address) {
		return isInRange(SubnetUtils.toInteger(address));
	}// isInRange

	// long versions of the values (as unsigned int) which are more suitable for range checking
	private long networkLong() {
		return network & UNSIGNED_INT_MASK;
	}// networkLong

	private long broadcastLong() {
		return broadcast & UNSIGNED_INT_MASK;
	}// broadcastLong

	private int low() {
		return inclusiveHostCount ? network : broadcastLong() - networkLong() > 1 ? network + 1 : 0;
	}// low

	private int high() {
		return inclusiveHostCount ? broadcast : broadcastLong() - networkLong() > 1 ? broadcast - 1 : 0;
	}// high

	/**
	 * Converts a packed integer address into dotted decimal format
	 *
	 * @param val an address in binary
	 * @return A dot-delimited address
	 */
	private String format(int val) {
		return null;
	}//format

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet. This excludes the
	 * network and broadcast addresses.
	 *
	 * @param address An IPv4 or IPv6 address in binary
	 * @return True if in range, false otherwise
	 */
	public boolean isInRange(int address) {
		return true;
	}//isRange

	/**
	 * Returns an IP address/CIDR format by counting the 1-bit population in the mask address.
	 * IP address: A dot-decimal notation as 192.168.0.1 in IPv4 or
	 * four hexadecimal digits and the groups are separated by colons, i.e. 2001:db8:0:0:0:ff00:42:8329 in IPv6
	 * CIDR: 0-32 in IPv4 or 0-128 in IPv6
	 */
	public String getCIDRNotation() {
		return format(address) + "/" + cidr;
	}//getCIDRNotation

	/**
	 * Return the low address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 on IPv4 or CIDR/64 on IPv6 if the inclusive flag is false.
	 *
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	public String getLowAddress() {
		return format(low());
	}// getLowAddress

	/**
	 * Return the high address as a dotted IP address.
	 * Will be zero for CIDR/31 and CIDR/32 on IPv4 or CIDR/64 on IPv6 if the inclusive flag is false.
	 *
	 * @return the IP address in dotted format, may be "0.0.0.0" if there is no valid address
	 */
	public String getHighAddress() {
		return format(high());
	}// getHighAddress

	/**
	 * Get the count of available addresses.
	 * Will be zero for CIDR/31 and CIDR/32 on IPv4 or CIDR/64 on IPv6 if the inclusive flag is false.
	 *
	 * @return the count of addresses, may be zero.
	 */
	public long getAddressCountLong() {
		return 0;
	}//getAddressCountLong

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

}
