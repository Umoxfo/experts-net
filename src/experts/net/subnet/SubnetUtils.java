/*
 * Copyright (C) 2014-2017 Makoto Sakaguchi
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
package experts.net.subnet;

/**
 * SubnetUtils
 *
 * @author Makoto Sakaguchi
 * @version 0.0.1-SNAPSHOT
 */
public final class SubnetUtils {
	public static enum IP {
		IPV4(32, 4, 255), IPV6(128, 8, 0xffff);

		private final int size;
		private final int grups;
		private final int maxRange;

		private IP(int size, int grups, int maxRange) {
			this.size = size;
			this.grups = grups;
			this.maxRange = maxRange;
		}

		public int getSize() {
			return size;
		}

		public int getGrups() {
			return grups;
		}

		public int getMaxRange() {
			return maxRange;
		}
	}// IP

	public static class Subnet {
		public static class Mask {
			public static final String CLASS_A = "255.0.0.0";
			public static final String CLASS_B = "255.255.0.0";
			public static final String CLASS_C = "255.255.255.0";
		}

		public static class CIDR {
			public static final int CLASS_A = 8;
			public static final int CLASS_B = 16;
			public static final int CLASS_C = 24;
		}
	}// Subnet
}
