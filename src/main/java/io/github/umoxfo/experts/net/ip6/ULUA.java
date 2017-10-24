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
package io.github.umoxfo.experts.net.ip6;

import io.github.umoxfo.experts.net.util.NICUtils;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

import static io.github.umoxfo.experts.net.ip6.IP6Utils.createEUI64;
import static io.github.umoxfo.experts.net.ip6.IP6Utils.toTextFormat;

/**
 * This class represents an Unique Local IPv6 unicast addresses.
 * Defined by <a href="https://tools.ietf.org/rfc/rfc4193.txt">RFC&nbsp;4193:
 * <i>Unique Local IPv6 Unicast Addresses</i></a>
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class ULUA implements IP6 {
	private static final int GLOBAL_ID_LENGTH = 6;
	private static final byte GLOBAL_ID_PREFIX = (byte) 0xfd;

	private static final int SUBNET_ID_LENGTH = 2;

	private static final String NTP_SERVER_ADDRESS = "pool.ntp.org";

	/*
	 * An identifier of a site (a cluster of subnets/links).
	 * 48 bits, the {@code FD00::/8} prefix and 40-bit global identifier format,
	 * for Unique Local IPv6 Unicast Addresses (ULUA).
	 */
	private final byte[] globalID;

	/*
	 * An identifier of a subnet within the site.
	 * 16 bits for ULUA.
	 */
	private final byte[] subnetID;

	/*
	 * An identifier of interfaces on a link.
	 * 64 bits for Interface ID.
	 */
	private final byte[] interfaceID;

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
		this.subnetID = checkLength(subnetID, SUBNET_ID_LENGTH);
		this.interfaceID = checkLength(interfaceID, INTERFACE_ID_LENGTH);
	}//ULUA(byte[], byte[], byte[])

	/**
	 * Constructor that takes a hardware address in a byte array.
	 *
	 * @param address the hardware address of the machine that creates a Local IPv6 unicast address
	 */
	public ULUA(byte[] address) {
		interfaceID = createEUI64(address);
		subnetID = new byte[SUBNET_ID_LENGTH];
		globalID = generateGlobalID(NTP_SERVER_ADDRESS, interfaceID);
	}//ULUA(byte[])

	/**
	 * {@inheritDoc}
	 *
	 * <p> This is 48 bits, the {@code FD00::/8} prefix and 40-bit global identifier format,
	 * for Local IPv6 addresses.
	 */
	@Override
	public byte[] getGlobalID() {
		byte[] gID = new byte[globalID.length];

		System.arraycopy(globalID, 0, gID, 0, globalID.length);

		return gID;
	}//getGlobalID

	/**
	 * {@inheritDoc}
	 *
	 * <p> This is 16 bits for Local IPv6 addresses.
	 */
	@Override
	public byte[] getSubnetID() {
		byte[] sID = new byte[subnetID.length];

		System.arraycopy(subnetID, 0, sID, 0, subnetID.length);

		return sID;
	}//getSubnetID

	/**
	 * Returns the Interface ID in binary.
	 *
	 * @return the Interface ID in a byte array
	 */
	@Override
	public byte[] getInterfaceID() {
		byte[] iID = new byte[interfaceID.length];

		System.arraycopy(interfaceID, 0, iID, 0, interfaceID.length);

		return iID;
	}//getInterfaceID

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
		if (gID[1] != GLOBAL_ID_PREFIX) throw new IllegalArgumentException("ULUA must be with 0xfd00::/8 prefix.");

		return gID;
	}//checkGlobalID

	/*
	 * Returns the time stamp in the 64-bit NTP format from a NTP server.
	 *
	 * @param address a NTP server address
	 * @return the current time of day in 64-bit NTP format
	 * @throws IOException If an error occurs while retrieving the time.
	 */
	private static long getNTPTime(String address) throws IOException {
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
	 * @param address a NTP server address
	 * @param systemID the system-specific identifier, e.g. an EUI-64 identifier and system serial number
	 * @return the generated Global ID
	 */
	public static byte[] generateGlobalID(String address, byte[] systemID) {
		ByteBuffer buf = ByteBuffer.allocate(16);

		long timeStamp = 0;
		try {
			timeStamp = getNTPTime(address);
		} catch (IOException e) {
			timeStamp = TimeStamp.getCurrentTime().ntpValue();
		} finally {
			buf.putLong(timeStamp);
		}//try-catch

		if (systemID == null || systemID.length != 8) {
			try {
				systemID = IP6Utils.createEUI64(NICUtils.getMACAddress());
			} catch (NullPointerException | SocketException e) {
				/* If the hardware address is not obtained, used random numbers as the system-specific identifier. */
				systemID = new byte[8];
				SecureRandom random = new SecureRandom();
				random.setSeed(new SecureRandom().generateSeed(16));

				random.nextBytes(systemID);
			}//try-catch
		}//if
		buf.put(systemID);

		byte[] gID = SHA1.getSHA1Digest(buf.array());
		gID[0] = GLOBAL_ID_PREFIX;

		return gID;
	}//generateGlobalID

	/**
	 * Convert IPv6 binary address into a canonical format
	 *
	 * @return the IPv6 address in the colon 16-bit delimited hexadecimal format
	 * @see IP6Utils#toTextFormat(byte[])
	 */
	@Override
	public String toString() { return toTextFormat(globalID, subnetID, interfaceID); }
}
