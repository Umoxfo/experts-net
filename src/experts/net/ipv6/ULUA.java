/*
 * Experts Net
 * Copyright (c) 2014-2017 Makoto Sakaguchi.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package experts.net.ipv6;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;

import experts.net.nic.MACAddr;

/**
 * @author Makoto
 *
 */
public class ULUA extends IPV6Utils {
	private final int GLOBAL_ID_LENGTH = 3;
	private final String GLOBAL_ID_PREFIX_FORMAT = "fd";
	private final byte GLOBAL_ID_PREFIX = (byte) 0xfd;

	private final short DEFAULT_SUBNT_ID = 0x0000;

	private final int INTERFACE_ID_LENGTH = 8;
	private final short ADDITIONAL_VALUES = (short) 0xfffe;

	private final int SEED_BYTES = 16;
	private final String NTP_SERVER_ADDRESS = "pool.ntp.org";

	public ULUA(String address) throws SocketException, UnknownHostException, IOException {
		createInterfaceIDByEUI64(MACAddr.getMACAddress(address));
		subnetID = Arrays.asList(DEFAULT_SUBNT_ID);
		generateGlobalID(obtainNTPTime(NTP_SERVER_ADDRESS));
	}// ULUA(String address)

	/**
	 * @param a global ID field of the ULUA, the prefix (0xfd00::/8) and Global ID
	 * @see experts.net.ipv6.IPV6Utils#setGlobalID(java.lang.String)
	 */
	@Override
	public void setGlobalID(String gID) {
		String[] tmp = gID.toLowerCase().split(":");

		// Check length
		if (tmp.length > GLOBAL_ID_LENGTH) {
			throw new IllegalArgumentException("The length of the prefix and Global ID of ULUA must be 48 bits.");
		} // if

		// Check prefix
		if (!tmp[0].startsWith(GLOBAL_ID_PREFIX_FORMAT)) {
			throw new IllegalArgumentException("ULUA must be 0xfd00::/8.");
		} // if

		globalID = IPV6Utils.toHexIntList(tmp);
	}// setGlobalID

	/**
	 * @param Subnet ID of the ULUA
	 * @see experts.net.ipv6.IPV6Utils#setSubnetID(int)
	 */
	@Override
	public void setSubnetID(String sID) {
		subnetID = Arrays.asList((short) Integer.parseInt(sID, 16));
	}// setSubnetID

	/**
	 * @param Interface ID of the ULUA
	 * @see experts.net.ipv6.IPV6Utils#setInterfaceID(java.lang.String)
	 */
	@Override
	public void setInterfaceID(String iID) {
		String[] tmp = iID.split(":");

		// Check length
		if (tmp.length > INTERFACE_ID_LENGTH) {
			throw new IllegalArgumentException("The Interface ID length of ULUA must be 64 bits.");
		} // if

		interfaceID = IPV6Utils.toHexIntList(tmp);
	}// setInterfaceID

	/**
	 * Obtain NTP time stamp value
	 *
	 * @param address a NTP server address
	 * @return the current time of day in 64-bit NTP format
	 * @throws UnknownHostException
	 * @throws SocketException
	 * @throws IOException
	 */
	public static long obtainNTPTime(String address) throws UnknownHostException, SocketException, IOException {
		NTPUDPClient client = new NTPUDPClient();
		client.setVersion(NtpV3Packet.VERSION_4);

		client.open();
		TimeInfo time = client.getTime(InetAddress.getByName(address));
		client.close();

		time.computeDetails();

		return time.getMessage().getTransmitTimeStamp().ntpValue();
	}// obtainNTPTime

	/**
	 * Generate Global ID according to RFC 4193 Section 3.2.2.
	 *
	 * @param timeStamp 64-bit NTP format
	 */
	public final void generateGlobalID(long timeStamp) {
		ByteBuffer buf = ByteBuffer.allocate(SEED_BYTES);

		buf.putLong(timeStamp);
		interfaceID.forEach(buf::putShort);

		byte[] digest = DigestUtils.sha1(buf.array());

		buf = ByteBuffer.allocate(6);
		buf.put(GLOBAL_ID_PREFIX).put(digest, 15, 5);

		globalID = toList(buf);
	}// generateGlobalID

	/**
	 * Create Interface ID by the Modified EUI-64 format (RFC 4291)
	 *
	 * @param macAddr MAC (NIC) address
	 */
	public final void createInterfaceIDByEUI64(byte[] macAddr) {
		ByteBuffer buf = ByteBuffer.allocate(INTERFACE_ID_LENGTH);

		buf.put(macAddr, 0, 3).putShort(ADDITIONAL_VALUES).put(macAddr, 3, 3);

		buf.put(0, (byte) (buf.get(0) ^ 0x02));

		interfaceID = toList(buf);
	}// createInterfaceIDByEUI64

	/**
	 * Convert ByteBuffer to Short List
	 *
	 * @param ByteBuffer
	 * @return Short List
	 */
	private static List<Short> toList(ByteBuffer buf) {
		int cap = buf.capacity() / 2;
		List<Short> tmp = new ArrayList<>(cap);

		buf.rewind();
		for (int i = 0; i < cap; i++) {
			tmp.add(buf.getShort());
		} // for

		return tmp;
	}// toList
}
