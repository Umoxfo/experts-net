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
 * along with Experts Net.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.umoxfo.experts.net.ip6;

import io.github.umoxfo.experts.net.util.NICUtils;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * This class represents an Unique Local IPv6 unicast addresses.
 * Defined by <a href="https://tools.ietf.org/rfc/rfc4193.txt">RFC&nbsp;4193:
 * <i>Unique Local IPv6 Unicast Addresses</i></a>
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class ULUA extends IP6 {
	private static final int GLOBAL_ID_LENGTH = 6;
	private static final byte GLOBAL_ID_PREFIX = (byte) 0xfd;

	private static final int SUBNET_ID_LENGTH = 2;
	private static final byte[] DEFAULT_SUBNT_ID = {0x00, 0x00};

	private static final String NTP_SERVER_ADDRESS = "pool.ntp.org";

	/**
	 * Constructor that takes a Global ID field, a Subnet ID, an Interface ID for Unique Local IPv6 Unicast Addresses.
	 *
	 * <p> Global ID is an identifier of a site (a cluster of subnets/links).
	 * Local IPv6 addresses follow the {@code FD00::/8} prefix and 40-bit global identifier format.
	 * See <a href="https://tools.ietf.org/html/rfc4193#section-3.1">Section 3.1 of
	 * RFC 4193</a> for additional information.
	 *
	 * <p> Subnet ID is an identifier of a subnet within the site.
	 *
	 * <p> Interface ID is an identifier of interfaces on a link.
	 *
	 * @param globalID must be reasonably unique in 48 bits
	 * @param subnetID must be in 16 bits
	 * @param interfaceID must be in 64 bits
	 */
	public ULUA(byte[] globalID, byte[] subnetID, byte[] interfaceID) {
		this.globalID = checkGlobalID(globalID);
		this.subnetID = checkSubnetID(subnetID);
		this.interfaceID = checkInterfaceID(interfaceID);
	}//ULUA(short[], short[], short[])

	/**
	 * Constructor that takes a hardware address in a byte array.
	 *
	 * @param address the hardware address of the machine that creates a Local IPv6 unicast address
	 * @throws SocketException If the socket could not be opened which it might be not available any ports.
	 * @throws UnknownHostException If the host could not be found.
	 * @throws IOException If an error occurs while retrieving the time.
	 */
	public ULUA(byte[] address) throws IOException {
		interfaceID = IP6Utils.createEUI64(address);
		subnetID = DEFAULT_SUBNT_ID.clone();
		globalID = generateGlobalID(getNTPTime(NTP_SERVER_ADDRESS), interfaceID);
	}//ULUA(byte[])

	/**
	 * {@inheritDoc}
	 *
	 * <p> This is 48 bits, the {@code FD00::/8} prefix and 40-bit global identifier format,
	 * for Local IPv6 addresses.
	 *
	 * @return {@inheritDoc}
	 */
	@Override
	public byte[] getGlobalID() { return globalID; }

	/**
	 * {@inheritDoc}
	 *
	 * <p> This is 16 bits for Local IPv6 addresses.
	 *
	 * @return {@inheritDoc}
	 */
	@Override
	public byte[] getSubnetID() { return subnetID; }

	/** {@inheritDoc} */
	@Override
	public byte[] getInterfaceID() { return interfaceID; }

	/*
	 ^ Checks a Global ID field of the Local IPv6 unicast address that follows
	 * the FD00::/8 prefix and 40-bit global identifier format.
	 */
	private byte[] checkGlobalID(byte[] gID) {
		// Check length
		if (gID.length != GLOBAL_ID_LENGTH) {
			throw new IllegalArgumentException("The Global ID field of ULUA must be 48 bits.");
		}//if

		// Check prefix
		if (gID[1] != GLOBAL_ID_PREFIX) throw new IllegalArgumentException("ULUA must be wiht 0xfd00::/8 prefix.");

		return gID;
	}//checkGlobalID

	/*
	 * Checks a Subnet ID that is an identifier of a subnet within the site.
	 */
	private byte[] checkSubnetID(byte[] subnetID) {
		if (subnetID.length != SUBNET_ID_LENGTH) throw new IllegalArgumentException("Subnet ID must be 16 bits.");

		return subnetID;
	}//checkSubnetID(byte[])

	/**
	 * Returns the time stamp in the 64-bit NTP format from a NTP server.
	 *
	 * @param address a NTP server address
	 * @return the current time of day in 64-bit NTP format
	 * @throws IOException If an error occurs while retrieving the time.
	 * @throws UnknownHostException If the host could not be found.
	 * @throws SocketException If the socket could not be opened which it might be not available any ports.
	 */
	public static long getNTPTime(String address) throws IOException {
		NTPUDPClient client = new NTPUDPClient();
		client.setVersion(NtpV3Packet.VERSION_4);

		// throws SocketException
		client.open();
		// throws UnknownHostException from InetAddress.getByName and IOException from client.getTime
		TimeInfo time = client.getTime(InetAddress.getByName(address));
		client.close();

		return time.getMessage().getReceiveTimeStamp().ntpValue();
	}//getNTPTime

	/**
	 * Generates a Global ID according to <a
	 * href="https://tools.ietf.org/html/rfc4193#section-3.2.2">Section 3.2.2 of
	 * RFC 4193</a>.
	 *
	 * @param timeStamp the current time of day in 64-bit NTP format
	 * @param systemID the system-specific identifier, e.g. an EUI-64 identifier and system serial number
	 * @return the generated Global ID
	 */
	public static byte[] generateGlobalID(long timeStamp, byte[] systemID) {
		ByteBuffer buf = ByteBuffer.allocate(16);

		if (timeStamp == 0) timeStamp = TimeStamp.getCurrentTime().ntpValue();
		buf.putLong(timeStamp);

		if (systemID == null || systemID.length != 8) {
			try {
				systemID = IP6Utils.createEUI64(NICUtils.getMACAddress());
			} catch (NullPointerException | SocketException e) {
				/* If the hardware address is not obtained, used random numbers as the system-specific identifier. */
				systemID = new byte[8];
				try {
					SecureRandom.getInstanceStrong().nextBytes(systemID);
				} catch (NoSuchAlgorithmException e1) {
					new Random(timeStamp).nextBytes(systemID);
				}//try-catch
			}//try-catch
		}//if
		buf.put(systemID);

		byte[] digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1").digest(buf.array());
		} catch (NoSuchAlgorithmException ignored) {}

		byte[] tmp = {GLOBAL_ID_PREFIX, 0, 0, 0, 0, 0};
		System.arraycopy(digest,15, tmp, 1, 5);

		return tmp;
	}//generateGlobalID

/*
	@Override
	public String toString() {
		return toString(globalID, subnetID, interfaceID);
	}
*/
}
