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
 * Defined by <a href="https://tools.ietf.org/rfc/rfc4193.txt"><i>RFC&nbsp;4193:
 * Unique Local IPv6 Unicast Addresses</i></a>
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class ULUA extends IP6 {
	private static final int GLOBAL_ID_LENGTH = 3;
	private static final String GLOBAL_ID_PREFIX_FORMAT = "fd";
	private static final byte GLOBAL_ID_PREFIX = (byte) 0xfd;

	private static final short DEFAULT_SUBNT_ID = 0x0000;

	private static final int INTERFACE_ID_LENGTH = 4;
	private static final short ADDITIONAL_VALUES = (short) 0xfffe;

	private static final String NTP_SERVER_ADDRESS = "pool.ntp.org";

	private byte[] systemSpecific = new byte[8];

	public ULUA() {
		globalID = new short[GLOBAL_ID_LENGTH];
		subnetID = new short[]{DEFAULT_SUBNT_ID};
		interfaceID = new short[INTERFACE_ID_LENGTH];
	}//ULUA

	/**
	 * Constructor that takes a hardware address in a byte array.
	 *
	 * @param address MAC address of the machine that creates a Local IPv6 unicast address
	 * @throws SocketException If the socket could not be opened which it might be not available any ports.
	 * @throws UnknownHostException If the host could not be found.
	 * @throws IOException If an error occurs while retrieving the time.
	 */
	public ULUA(byte[] address) throws IOException {
		interfaceID = createInterfaceIDByEUI64(address);
		subnetID = new short[]{DEFAULT_SUBNT_ID};
		globalID = generateGlobalID(getNTPTime(NTP_SERVER_ADDRESS), interfaceID);
	}//ULUA(byte[] address)

	/**
	 * Sets a Global ID field of the Local IPv6 unicast address that follows
	 * the <code>FD00::/8</code> prefix and 40-bit global identifier format.
	 *
	 * @param gID four hexadecimal digits and up to 15 digits include colons
	 * @see IP6#setGlobalID(short[])
	 */
	public void setGlobalID(short[] gID) {
		// Check length
		if (gID.length > GLOBAL_ID_LENGTH) {
			throw new IllegalArgumentException("The length of the prefix and Global ID of ULUA must be 48 bits.");
		}//if

		// Check prefix
		if ((gID[0] >>> 8) != GLOBAL_ID_PREFIX) throw new IllegalArgumentException("ULUA must be 0xfd00::/8.");

		globalID = gID;
	}//setGlobalID(short[])

	/**
	 * Sets a Global ID field of the Local IPv6 unicast address that follows
	 * the <code>FD00::/8</code> prefix and 40-bit global identifier format.
	 *
	 * @param gID a colon-separated string for each four hexadecimal digits and up to 15 digits include colons
	 * @see IP6#setGlobalID(short[])
	 */
	public void setGlobalID(String gID) {
		String[] tmp = gID.split(":");

		// Check length
		if (tmp.length > GLOBAL_ID_LENGTH) {
			throw new IllegalArgumentException("The length of the prefix and Global ID of ULUA must be 48 bits.");
		}//if

		// Check prefix
		if (!tmp[0].toLowerCase().startsWith(GLOBAL_ID_PREFIX_FORMAT)) {
			throw new IllegalArgumentException("ULUA must be 0xfd00::/8.");
		}//if

		globalID = IP6Utils.toShortArray(tmp);
	}//setGlobalID(String)

	/**
	 * Sets a Subnet ID that is an identifier of a subnet within the site.
	 *
	 * @param sID four hexadecimal digits
	 * @see IP6#setSubnetID(short[])
	 */
	@Override
	public void setSubnetID(short[] sID) {
		if (sID.length != 1) throw new AssertionError("Subnet ID must be 16 bits.");

		subnetID = sID;
	}//setSubnetID(short[])

	/**
	 * Sets a Subnet ID of the Local IPv6 unicast address that is 16-bit.
	 *
	 * @param sID a four-digit hexadecimal string
	 * @see IP6#setSubnetID(short[])
	 */
	public void setSubnetID(String sID) {
		int n = Integer.parseInt(sID, 16);

		if (n > 0xffff) throw new IllegalArgumentException("Subnet ID must be 16 bits.");

		subnetID = new short[]{(short) n};
	}//setSubnetID(String)

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInterfaceID(short[] iID) {
		if (iID.length != INTERFACE_ID_LENGTH) {
			throw new IllegalArgumentException("The length ot the Interface ID must be 64 bits.");
		}//if

		interfaceID = iID;
	}//setInterfaceID(short[])

	/**
	 * Sets an Interface ID that is used to identify interfaces on a link.
	 *
	 * @param iID the text representation of IPv6 address,
	 *            a colon-separated string for each four hexadecimal digits, and up to 19 digits include colons
	 */
	public void setInterfaceID(String iID) {
		String[] tmp = iID.split(":");

		// Check length
		if (tmp.length > INTERFACE_ID_LENGTH) {
			throw new IllegalArgumentException("The Interface ID length of ULUA must be 64 bits.");
		}//if

		interfaceID = IP6Utils.toShortArray(tmp);
	}//setInterfaceID(String)

	/*
	 * Converts the byte array to an short array.
	 */
	private static short[] toArray(byte[] id) {
		final int ln = id.length / 2;
		short[] ret = new short[ln];

		for (int i = 0; i < ln; i++) {
			int j = i << 1;
			ret[i] = (short) ((id[j] << 8) | (id[j + 1] & 0xff));
		}//for

		return ret;
	}//toArray

	/**
	 * Returns the time stamp in the 64-bit NTP format from a NTP server.
	 *
	 * @param address a NTP server address
	 * @return the current time of day in 64-bit NTP format
	 * @throws SocketException If the socket could not be opened which it might be not available any ports.
	 * @throws UnknownHostException If the host could not be found.
	 * @throws IOException If an error occurs while retrieving the time.
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
	 * href="https://www.rfc-editor.org/rfc/rfc5952.txt">Section 3.2.2 of
	 * <i>RFC 4193: Unique Local IPv6 Unicast Addresses</i></a>.
	 *
	 * @param timeStamp the current time of day in 64-bit NTP format
	 * @param systemID the system-specific identifier, e.g. an EUI-64 identifier and system serial number
	 * @return the generated Global ID
	 */
	public short[] generateGlobalID(long timeStamp, short[] systemID) throws NullPointerException {
		ByteBuffer buf = ByteBuffer.allocate(16);

		if (timeStamp == 0) timeStamp = TimeStamp.getCurrentTime().ntpValue();
		buf.putLong(timeStamp);

		if (systemID != null) {
			for (int i = 0; i < 8; i++) buf.putShort(systemID[i]);
			//IntStream.range(0, 8).map(i -> systemID[i] & 0xffff).forEach(i -> buf.putShort((short) i));
		} else {
			try {
				createInterfaceIDByEUI64(NICUtils.getMACAddress());
			} catch (NullPointerException | SocketException e) {
				/* If the hardware address is not obtained, used random numbers as the system-specific identifier. */
				try {
					SecureRandom.getInstanceStrong().nextBytes(systemSpecific);
				} catch (NoSuchAlgorithmException e1) {
					new Random(timeStamp).nextBytes(systemSpecific);
				}//try-catch
			}//try-catch

			buf.put(systemSpecific);
		}//if-else

		byte[] digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1").digest(buf.array());
		} catch (NoSuchAlgorithmException e) {}

		byte[] tmp = {GLOBAL_ID_PREFIX, 0, 0, 0, 0, 0};
		System.arraycopy(digest,15, tmp, 1, 5);

		return toArray(tmp);
	}//generateGlobalID

	/**
	 * Creates an IEEE EUI-64 identifier from an IEEE 48-bit MAC identifier.
	 * See <a href="https://www.rfc-editor.org/rfc/rfc4291.txt"><i>Appendix A:
	 * Creating Modified EUI-64 Format Interface Identifiers</i></a>
	 * of <i>RFC 4291: IPv6 Addressing Architecture</i>.
	 *
	 * @param macAddr a byte array containing the hardware address
	 * @return the Interface ID by the EUI-64 format
	 */
	public short[] createInterfaceIDByEUI64(byte[] macAddr) {
		ByteBuffer buf = ByteBuffer.wrap(systemSpecific);

		buf.put(macAddr, 0, 3).putShort(ADDITIONAL_VALUES).put(macAddr, 3, 3);
		systemSpecific[0] ^= 0x02;

		return toArray(systemSpecific);
	}//createInterfaceIDByEUI64
}
