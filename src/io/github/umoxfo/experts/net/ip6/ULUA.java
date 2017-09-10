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

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
		createInterfaceIDByEUI64(address);
		subnetID = new short[]{DEFAULT_SUBNT_ID};
		generateGlobalID(getNTPTime(NTP_SERVER_ADDRESS));
	}//ULUA(byte[] address)

	/**
	 * Sets a Global ID field of the Local IPv6 unicast address that follows
	 * the <code>FD00::/8</code> prefix and 40-bit global identifier format.
	 *
	 * @param gID a colon-separated string for each four hexadecimal digits and up to 15 digits include colons
	 * @see IP6#setGlobalID(String)
	 */
	@Override
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

		/*
		 * Check prefix by short value
		 * Note: "tmp" is a short array
		 *
		 * (byte) (tmp.get(0) & 0xff00) != GLOBAL_ID_PREFIX ? throw new IllegalArgumentException("ULUA must be 0xfd00::/8.");
		 */
		globalID = IP6Utils.toShortArray(tmp);
	}//setGlobalID

	/**
	 * Sets a Subnet ID of the Local IPv6 unicast address that is 16-bit.
	 *
	 * @param sID a four-digit hexadecimal string
	 * @see IP6#setSubnetID(String)
	 */
	@Override
	public void setSubnetID(String sID) { subnetID = new short[]{(short) Integer.parseInt(sID, 16)}; }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setInterfaceID(String iID) {
		String[] tmp = iID.split(":");

		// Check length
		if (tmp.length > INTERFACE_ID_LENGTH) {
			throw new IllegalArgumentException("The Interface ID length of ULUA must be 64 bits.");
		}//if

		interfaceID = IP6Utils.toShortArray(tmp);
	}//setInterfaceID

	/*
	 * Converts ByteBuffer to an array of the Short type.
	 */
	private static short[] toArray(ByteBuffer buf) {
		int size = buf.limit() / 2;
		short[] shorts = new short[size];

		buf.rewind();
		for (int i = 0; i < size; i++) {
			shorts[i] = buf.getShort();
		}//for

		return shorts;
	}//toArray

	/**
	 * Returns NTP time stamp value
	 *
	 * @param address a NTP server address
	 * @return the current time of day in 64-bit NTP format
	 * @throws SocketException If the socket could not be opened which it might
	 *             be not available any ports.
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

		time.computeDetails();

		return time.getMessage().getTransmitTimeStamp().ntpValue();
	}//getNTPTime

	/**
	 * Generates a Global ID according to RFC 4193 Section 3.2.2.
	 *
	 * @param timeStamp a time stamp in 64-bit NTP format
	 */
	public void generateGlobalID(long timeStamp) {
		ByteBuffer buf = ByteBuffer.allocate(16);

		buf.putLong(timeStamp);
		for (short e : interfaceID) {
			buf.putShort(e);
		}//for

		byte[] digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1").digest(buf.array());
		} catch (NoSuchAlgorithmException e) {}

		buf.clear().limit(6);
		buf.put(GLOBAL_ID_PREFIX).put(digest, 15, 5);

		globalID = toArray(buf);
	}//generateGlobalID

	/**
	 * Creates an Interface ID by the Modified EUI-64 format (RFC 4291)
	 *
	 * @param macAddr a byte array containing the hardware address
	 */
	public void createInterfaceIDByEUI64(byte[] macAddr) {
		ByteBuffer buf = ByteBuffer.allocate(8);

		buf.put(macAddr, 0, 3).putShort(ADDITIONAL_VALUES).put(macAddr, 3, 3);

		buf.put(0, (byte) (buf.get(0) ^ 0x02));

		interfaceID = toArray(buf);
	}//createInterfaceIDByEUI64
}
