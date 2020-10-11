package net.sf.zoftwhere.text;

public class UTF_8 {

	public static int codePointCount(final byte[] input) {
		int count = 0;
		final int size = input != null ? input.length : 0;
		for (int i = 0; i < size; i++) {
			final int b = input[i] & 0b1111_1111;
			if ((b & 0b1000_0000) == 0x0) {
				count += 1;
			}
			else if ((b & 0b1110_0000) == 0b1100_0000) {
				count += 1;
			}
			else if ((b & 0b1111_0000) == 0b1110_0000) {
				count += 1;
			}
			else if ((b & 0b1111_1000) == 0b1111_0000) {
				count += 1;
			}
			else {
				count += 0;
			}
		}
		return count;
	}

	public static String getCodePointString(byte[] array, int index) {
		int b = array[index] & 0xff;
		byte[] codepoint;
		if ((b & 0b1000_0000) == 0x0) {
			codepoint = new byte[1];
			codepoint[0] = array[index];
		}
		else if ((b & 0b1110_0000) == 0b1100_0000) {
			codepoint = new byte[2];
			codepoint[0] = array[index];
			codepoint[1] = array[index + 1];
		}
		else if ((b & 0b1111_0000) == 0b1110_0000) {
			codepoint = new byte[3];
			codepoint[0] = array[index];
			codepoint[1] = array[index + 1];
			codepoint[2] = array[index + 2];
		}
		else if ((b & 0b1111_1000) == 0b1111_0000) {
			codepoint = new byte[4];
			codepoint[0] = array[index];
			codepoint[1] = array[index + 1];
			codepoint[2] = array[index + 2];
			codepoint[3] = array[index + 3];
		}
		else {
			return null;
		}

		return new String(codepoint);
	}
}
