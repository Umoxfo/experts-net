/*
 * Copyright (c) 2018. Makoto Sakaguchi
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

class SHA1 {
	/* Constants defined in FIPS 180-4, section 4.2.1 */
	private static final int ROUND1_KT = 0x5a827999;
	private static final int ROUND2_KT = 0x6ed9eba1;
	private static final int ROUND3_KT = 0x8f1bbcdc;
	private static final int ROUND4_KT = 0xca62c1d6;

	private SHA1() { throw new IllegalStateException("Utility class"); }

	// TEST ACCESSOR
	@SuppressWarnings("SameParameterValue")
	static int[] getDigest(byte[] input) { return padMessage(input); }

	/*
	 * Returns the least significant 40-bit message digest of a specified byte array.
	 *
	 * Performs a final update on the digest using the array,
	 * then completes the digest computation.
	 *
	 * @param input the input to be updated before the digest is completed
	 */
	static byte[] getSHA1Digest(byte[] input) {
		int[] hash = padMessage(input);

		byte[] digest = new byte[5];
		digest[0] = (byte) hash[3];
		digest[1] = (byte) (hash[4] >>> 24);
		digest[2] = (byte) (hash[4] >>> 16);
		digest[3] = (byte) (hash[4] >>> 8);
		digest[4] = (byte) hash[4];

		return digest;
	}//getSHA1Digest

	/*
	 * According to the standard, the message must be padded to the next
	 * even multiple of 512 bits. The first padding bit must be a '1'.
	 * The last 64 bits represent the length of the original message.
	 * All bits in between should be 0. This helper function will pad
	 * the message according to those rules by filling the output array
	 * accordingly. When it returns, it can be assumed that the
	 * message digest has been computed.
	 */
	private static int[] padMessage(byte[] data) {
		byte[] messageBlock = new byte[64];
		System.arraycopy(data, 0, messageBlock, 0, data.length);

		// Add a '1' to the message block before the 0-padding and length.
		messageBlock[data.length] = (byte) 0x80;

		// Store the message length as the last 8 octets
		long bitsProcessed = data.length * 8L;
		for (int i = 56, cnt = 7; i < 64; i++, cnt--) {
			messageBlock[i] = (byte) (bitsProcessed >>> (8 * cnt));
		}//for

		return processBlock(messageBlock);
	}//padMessage

	/*
	 ^ This helper function will process the next 512 bits of
	 * the message stored in the work array.
	 */
	private static int[] processBlock(byte[] msg) {
		/* Initial Hash Values: FIPS 180-4 section 5.3.1 */
		int[] intermediateHash = {0x67452301,
		                          0xefcdab89,
		                          0x98badcfe,
		                          0x10325476,
		                          0xc3d2e1f0};

		// Initialize the first 16 elements (32-bit words) in the array 'W' (word sequence)
		int[] W = new int[80];
		for (int outer = 0; outer < 16; outer++) {
			int j = outer * 4;

			W[outer] = (msg[j++] & 0xff) << 24;
			W[outer] |= (msg[j++] & 0xff) << 16;
			W[outer] |= (msg[j++] & 0xff) << 8;
			W[outer] |= msg[j] & 0xff;
		}//for

		// The first 16 elements have the byte stream, compute the rest of the buffer
		for (int i = 16; i < 80; i++) {
			int temp = W[i - 3] ^ W[i - 8] ^ W[i - 14] ^ W[i - 16];
			W[i] = (temp << 1) | (temp >>> 31);
		}//for

		// Initialize hash value for this chunk:
		int a = intermediateHash[0];
		int b = intermediateHash[1];
		int c = intermediateHash[2];
		int d = intermediateHash[3];
		int e = intermediateHash[4];

		// Round 1
		for (int i = 0; i < 20; i++) {
			int ch = (b & (c ^ d)) ^ d;
			int temp = ((a << 5) | (a >>> 27)) + ch + e + ROUND1_KT + W[i];
			e = d;
			d = c;
			c = (b << 30) | (b >>> 2);
			b = a;
			a = temp;
		}//for

		// Round 2
		for (int i = 20; i < 40; i++) {
			int parity = b ^ c ^ d;
			int temp = ((a << 5) | (a >>> 27)) + parity + e + ROUND2_KT + W[i];
			e = d;
			d = c;
			c = (b << 30) | (b >>> 2);
			b = a;
			a = temp;
		}//for

		// Round 3
		for (int i = 40; i < 60; i++) {
			int maj = (b & (c | d)) | (c & d);
			int temp = ((a << 5) | (a >>> 27)) + maj + e + ROUND3_KT + W[i];
			e = d;
			d = c;
			c = (b << 30) | (b >>> 2);
			b = a;
			a = temp;
		}//for

		// Round 4
		for (int i = 60; i < 80; i++) {
			int parity = b ^ c ^ d;
			int temp = ((a << 5) | (a >>> 27)) + parity + e + ROUND4_KT + W[i];
			e = d;
			d = c;
			c = (b << 30) | (b >>> 2);
			b = a;
			a = temp;
		}//for

		intermediateHash[0] += a;
		intermediateHash[1] += b;
		intermediateHash[2] += c;
		intermediateHash[3] += d;
		intermediateHash[4] += e;

		return intermediateHash;
	}//processBlock
}
