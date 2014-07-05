package org.starsautohost.starsapi.block;

import org.starsautohost.starsapi.Util;


/**
 * Basic holder for a file block
 */
public class Block {
	public int type = BlockType.UNKNOWN_BAD;
	public int size = 0;
	public boolean encrypted = true;
	
	/**
	 * This holds the original block data 
	 */
	public byte[] data;
	
	/**
	 * This holds the decrypted block data
	 */
	public byte[] decryptedData;

	
	public int otherDataSize = 0;
	
	/**
	 * This holds possible other data after the block, like with the PLANETS
	 * block (probably the only case?)
	 */
	public byte[] otherData;
	
	
	/**
	 * Output this block for debugging
	 */
	@Override
	public String toString() {
		String s = "=> Block type: " + type + "; size: " + size + "\n";
		
		// Re-encode first two blocks from type and size
		// Bottom 8 bits of size become byte 1
		int byte1 = size;

		// Block type becomes top 6 bits of byte 2, top 2 (of 10) bits of size 
		// become the low 2 bits
		int byte2 = (size >> 8) | (type << 2);
		
		if(size > 0) {
			s += "-- Original Block --\n";
			s += byte1 + " " + byte2 + " " + Util.bytesToString(data, 0, size) + "\n";
			
			if(encrypted) {
				s += "-- Decrypted Block --\n";
				s += byte1 + " " + byte2 + " " + Util.bytesToString(decryptedData, 0, size) + "\n";
			}
		}
		
		if(otherDataSize > 0) {
			s += "-- Other Data --\n";
			s += Util.bytesToString(otherData, 0, otherDataSize) + "\n";
		}

		return s;
	}
}
