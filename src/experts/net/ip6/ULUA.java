/*
 * Experts Net
 * Copyright (c) 2014-2017 Makoto Sakaguchi.
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
package experts.net.ip6;

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

/**
 * Unique Local IPv6 Unicast Addresses (RFC 4193)
 *
 * @author Makoto Sakaguchi
 * @version 2.0.6-dev
 */
public final class ULUA extends IP6 {
	private final int GLOBAL_ID_LENGTH = 3;
	private final String GLOBAL_ID_PREFIX_FORMAT = "fd";
	private final byte GLOBAL_ID_PREFIX = (byte) 0xfd;

	private final short DEFAULT_SUBNT_ID = 0x0000;

	private final int INTERFACE_ID_LENGTH = 8;
	private final short ADDITIONAL_VALUES = (short) 0xfffe;

	private final String NTP_SERVER_ADDRESS = "pool.ntp.org";

	/**
	 * Constructor that takes address of the machine.
	 *
	 * @param address MAC address of the machine that creates Unique Local IPv6 Unicast Addresses
	 * @throws SocketException If the socket could not be opened which it might be not available any ports.
	 * @throws UnknownHostException If the host could not be found.
	 * @throws IOException If an error occurs while retrieving the time.
	 */
	public ULUA(byte[] address) throws SocketException, UnknownHostException, IOException {
		createInterfaceIDByEUI64(address);
		subnetID = Arrays.asList(DEFAULT_SUBNT_ID);
		generateGlobalID(getNTPTime(NTP_SERVER_ADDRESS));
	}//ULUA

	/**
	 * Sets a global ID that is the hexadecimal maximum 12 digits.
	 *
	 * @param gID a global ID of the ULUA, the prefix (0xfd00::/8) and Global ID
	 * @see experts.net.ip6.IP6#setGlobalID(java.lang.String)
	 */
	@Override
	public void setGlobalID(String gID) {
		String[] tmp = gID.toLowerCase().split(":");

		// Check length
		if (tmp.length > GLOBAL_ID_LENGTH) {
			throw new IllegalArgumentException("The length of the prefix and Global ID of ULUA must be 48 bits.");
		}// if

		// Check prefix
		if (!tmp[0].startsWith(GLOBAL_ID_PREFIX_FORMAT)) {
			throw new IllegalArgumentException("ULUA must be 0xfd00::/8.");
		}// if

		/*
		 * Check prefix by short value
		 * tmp is a short array
		 *
		 * (byte) (tmp.get(0) & 0xff00) != GLOBAL_ID_PREFIX ? throw new IllegalArgumentException("ULUA must be 0xfd00::/8.");
		 */
		globalID = IP6Utils.toShortList(tmp);
	}//setGlobalID

	/**
	 * Sets a subnet ID that is the hexadecimal maximum four digits.
	 *
	 * @param sID a Subnet ID of the ULUA
	 * @see experts.net.ip6.IP6#setSubnetID(java.lang.String)
	 */
	@Override
	public void setSubnetID(String sID) {
		subnetID = Arrays.asList((short) Integer.parseInt(sID, 16));
	}//setSubnetID

	/**
	 * Sets a interface ID that is the hexadecimal maximum 16 digits.
	 *
	 * @param iID an Interface ID of the ULUA
	 * @see experts.net.ip6.IP6#interfaceID
	 */
	@Override
	public void setInterfaceID(String iID) {
		String[] tmp = iID.split(":");

		// Check length
		if (tmp.length > INTERFACE_ID_LENGTH) {
			throw new IllegalArgumentException("The Interface ID length of ULUA must be 64 bits.");
		} // if

		interfaceID = IP6Utils.toShortList(tmp);
	}//setInterfaceID

	/*
	 * Converts ByteBuffer to a list of the Short type.
	 */
	private static List<Short> toList(ByteBuffer buf) {
		int lim = buf.limit() / 2;
		List<Short> tmp = new ArrayList<>(lim);

		buf.rewind();
		for (int i = 0; i < lim; i++) {
			tmp.add(buf.getShort());
		} // for

		return tmp;
	}//toList

	/**
	 * Returns NTP time stamp value
	 *
	 * @param address a NTP server address
	 * @return the current time of day in 64-bit NTP format
	 * @throws SocketException If the socket could not be opened which it might be not available any ports.
	 * @throws UnknownHostException If the host could not be found.
	 * @throws IOException If an error occurs while retrieving the time.
	 */
	public static long getNTPTime(String address) throws SocketException, UnknownHostException, IOException {
		NTPUDPClient client = new NTPUDPClient();
		client.setVersion(NtpV3Packet.VERSION_4);

		// throws SocketException
		client.open();
		// throws UnknownHostException from InetAddress.getByName and IOException from client.getTime
		TimeInfo time = client.getTime(InetAddress.getByName(address));
		client.close();

		time.computeDetails();

		return time.getMessage().getTransmitTimeStamp().ntpValue();
	}//obtainNTPTime

	/**
	 * Generates a global ID according to RFC 4193 Section 3.2.2.
	 *
	 * @param timeStamp a time stamp in 64-bit NTP format
	 */
	public void generateGlobalID(long timeStamp) {
		ByteBuffer buf = ByteBuffer.allocate(16);

		buf.putLong(timeStamp);
		interfaceID.forEach(buf::putShort);

		byte[] digest = DigestUtils.sha1(buf.array());

		buf.clear().limit(6);
		buf.put(GLOBAL_ID_PREFIX).put(digest, 15, 5);

		globalID = toList(buf);
	}//generateGlobalID

	/**
	 * Creates Interface ID by the Modified EUI-64 format (RFC 4291)
	 *
	 * @param macAddr MAC (NIC) address of the network interface for setting the generated address
	 */
	public void createInterfaceIDByEUI64(byte[] macAddr) {
		ByteBuffer buf = ByteBuffer.allocate(INTERFACE_ID_LENGTH);

		buf.put(macAddr, 0, 3).putShort(ADDITIONAL_VALUES).put(macAddr, 3, 3);

		buf.put(0, (byte) (buf.get(0) ^ 0x02));

		interfaceID = toList(buf);
	}//createInterfaceIDByEUI64
}
