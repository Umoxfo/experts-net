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

package io.github.umoxfo.experts.net.subnet.address;

/**
 * Thrown to indicate that the application has attempted to convert
 * an IP address string to a byte array, but that is not the appropriate format.
 *
 * @author Makoto Sakaguchi
 * @since 2.0.6
 */
public class IPAddressFormatException extends IllegalArgumentException {
	private static final long serialVersionUID = -6781334552032809018L;

	/**
	 * Constructs a {@code NumberFormatException} with no detail message.
	 */
	public IPAddressFormatException() { }

	/**
	 * Constructs a {@code NumberFormatException} with the specified detail message.
	 *
	 * @param s the detail message.
	 */
	public IPAddressFormatException(String s) { super(s); }
}
