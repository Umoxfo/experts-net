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

import java.math.BigInteger;
import java.util.ArrayList;

import experts.net.ip6.IP6Utils;

/**
 * Convenience container for IPv6 subnet summary information.
 *
 * @author Apache Commons Net
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 * @since 2.0.6
 */
public final class IP6Subnet extends SubnetInfo {
	private static final int NBITS = 128;

	private final short[] ip6Address;
	private final int cidr;

	/*
	 * Constructor that takes an IPv6 address with CIDR of a string, e.g. "2001:db8:0:0:0:ff00:42:8329/48"
	 */
	IP6Subnet(String cidrNotation) {
		String[] tmp = cidrNotation.split("/");

		ip6Address = toArray(tmp[0]);
		cidr = SubnetUtils.checkRange(Integer.parseInt(tmp[1]), 0, NBITS);
	}//IP6Subnet

	/*
	 * Creates the minimum address in the network
	 * to which the address belongs, it has all-zero in the host fields.
	 */
	private short[] low() {
		short[] addr = new short[8];

		// Copy of the network prefix in the address
		int index = cidr / 16;
		for (int i = 0; i <= index; i++) {
			addr[i] = ip6Address[i];
		}// for

		// Set the out of the network prefix bits.
		addr[index] &= (0xffff >> (cidr % 16)) ^ 0xffff;
		return addr;
	}//low

	/*
	 * Creates the maximum address in the network
	 * to which the address belongs, it has all-ones in the host fields.
	 */
	private short[] high() {
		short[] highAddr = new short[8];

		// Copy of the network prefix in the address
		int index = cidr / 16;
		for (int i = 0; i <= index; i++) {
			highAddr[i] = ip6Address[i];
		}// for

		// Fill the following fields with 1-bits
		for (int i = index + 1; i < 8; i++) {
			highAddr[i] = (short) 0xffff;
		}//for

		// Set the out of the network prefix bits
		highAddr[index] |= 0xffff >> (cidr % 16);

		return highAddr;
	}//high

	private static short[] toArray(String address) {
		short[] ret = new short[8];
		String[] addrArry = address.split(":");

		/* Initialize the internal fields from the supplied CIDR */
		for (int i = 0; i < addrArry.length; i++) {
			ret[i] = (short) Integer.parseInt(addrArry[i], 16);
		} // for

		return ret;
	}//toArray

	/*
	 * Converts a packed integer address into dotted decimal format
	 */
	private static String format(short[] val) {
		ArrayList<Short> al = new ArrayList<>(val.length);
		for (short i : val) {
			al.add(i);
		}// for

		return IP6Utils.buildIP6String(al);
	}//format

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet.
	 *
	 * @param address a colon-delimited address, e.g. "2001:db8:0:0:0:ff00:42:8329"
	 * @return true if in range, false otherwise
	 */
	@Override
	public boolean isInRange(String address) {
		return isInRange(toArray(address));
	}// isInRange(String address)

	/**
	 * Returns true if the parameter <code>address</code> is in the
	 * range of usable endpoint addresses for this subnet.
	 *
	 * @param address an IPv6 address in binary
	 * @return true if in range, false otherwise
	 */
	@Override
	public boolean isInRange(short[] address) {
		int prefixSize = cidr / 16;
		short[] lowAddress = low();
		short[] highAddress = high();

		// Have the same network prefix groups
		for (int i = 0; i < prefixSize; i++) {
			//(address[i] ^ lowAddress[i]) != (address[i] ^ highAddress[i]) && (lowAddress[i] ^ highAddress[i]) != 0
			if ((address[i] ^ lowAddress[i]) != (lowAddress[i] ^ highAddress[i])) {
				return false;
			}//if
		}//for

		//The host identifier is in range between the lowest and the hightest addresses
		prefixSize++;
		int addr = address[prefixSize] & 0xffff;
		int lowAddr = lowAddress[prefixSize] & 0xffff;
		int highAddr = highAddress[prefixSize] & 0xffff;

		return (addr >= lowAddr) && (addr <= highAddr);
	}//isInRange(short[] address)

	/**
	 * Gets the <code>address</code>, that is a colon 16-bit delimited hexadecimal format
	 * for IPv6 addresses, e.g. "2001:db8::ff00:42:8329".
	 *
	 * @return a string of the IP address
	 */
	@Override
	public String getAddresss() {
		return format(ip6Address);
	}// getAddress

	/**
	 * Gets the CIDR suffixes, the count of consecutive 1-bit in the subnet mask.
	 * The IPv6 address is between 0 and 128, but it is actually less than 64.
	 *
	 * @return the CIDR suffixes of the address in an integer.
	 */
	@Override
	public int getCIDR() {
		return cidr;
	}//getCIDR

	/**
	 * Returns an IPv6-CIDR notation, in which the address is followed by a slash character (/) and
	 * the count of counting the 1-bit population in the subnet mask.
	 *
	 * @return the CIDR notation of the address, e.g. "2001:db8::ff00:42:8329/48"
	 */
	@Override
	public String getCIDRNotation() {
		return format(ip6Address) + "/" + cidr;
	}//getCIDRNotation

	/**
	 * Returns the low address as a colon-separated IP address.
	 *
	 * @return the IP address in a colon 16-bit delimited hexadecimal format,
	 * may be "::" if there is no valid address
	 */
	@Override
	public String getLowAddress() {
		return format(low());
	}//getLowAddress

	/**
	 * Returns the high address as a colon-separated IP address.
	 *
	 * @return the IP address in a colon 16-bit delimited hexadecimal format,
	 * may be "::" if there is no valid address
	 */
	@Override
	public String getHighAddress() {
		return format(high());
	}//getHighAddress

	/**
	 * Returns the count of available addresses.
	 *
	 * @return the count of addresses in a string, may be zero
	 */
	@Override
	public String getAddressCount() {
		return new BigInteger("2").pow(128 - cidr).toString();
	}//getAddressCount

	/**
	 * Returns subnet summary information of the address,
	 * which includes an IP address by CIDR-Notation, the first and
	 * the last addresses of the network, and the number of available addresses
	 * in the network which includes all-zero and all-ones in the host fields,
	 * known as network or broadcast addresses.
	 */
	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append("CIDR-Notation:\t[").append(getCIDRNotation()).append("]")
		.append("First Address:\t[").append(getLowAddress()).append("]\n")
		.append("Last Address:\t[").append(getHighAddress()).append("]\n")
		.append("# Addresses:\t[").append(getAddressCount()).append("]\n");

		return buf.toString();
	}//toString
}
