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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SHA1Test {
	private static final byte[] MESSAGE = {0x35, 0x52, 0x69, 0x4c, (byte) 0xdf, 0x66, 0x3f, (byte) 0xd9, 0x4b, 0x22, 0x47, 0x47,
	                                       (byte) 0xac, 0x40, 0x6a, (byte) 0xaf};
	private static final int[] MD = {0xa150de92, 0x7454202d, 0x94e656de, 0x4c7c0ca6, 0x91de955d};

	@Test
	void testSHA1() { assertArrayEquals(MD, SHA1.getDigest(MESSAGE)); }
}
