package org.starsautohost.starsapi.block;

import java.util.Arrays;

import org.starsautohost.starsapi.Util;

/**
 * This class will parse the header block and fill out all header data.
 * 
 * Yes.  I deliberately left out getters/setters. 
 */
public class FileHeaderBlock {
	
	// Original header block
	private Block block;
	
	// Header data
	public byte[] magicNumberData;
	public String magicNumberString;
	
	public int gameId;
	public int versionMajor;
	public int versionMinor;
	public int versionIncrement;
	public int turn;
	public int year;
	public int playerNumber;  // zero-indexed
	public int encryptionSalt;
	public int fileType;
	
	public boolean turnSubmitted;
	public boolean hostUsing;
	public boolean multipleTurns;
	public boolean gameOver;
	public boolean shareware;

	
	public FileHeaderBlock(Block headerBlock) {
		this.block = headerBlock;
		
		parseBlock();
	}
	
	
	/**
	 * Parse the given block according to rules found at:
	 *    http://wiki.starsautohost.org/wiki/FileHeaderBlock
	 *    
	 * Bytes are offset 2 as the type and size have already been parsed off.
	 * E.g. the magic number is bytes 0-3 
	 */
	private void parseBlock() {
		magicNumberData = Arrays.copyOfRange(block.data, 0, 4);
		magicNumberString = new String(magicNumberData);
		
		// Game id is 4 bytes (swapped)
		gameId = (Util.ubyteToInt(block.data[7]) << 24)
				| (Util.ubyteToInt(block.data[6]) << 16)
				| (Util.ubyteToInt(block.data[5]) << 8)
				| Util.ubyteToInt(block.data[4]);
		
		// Version data block is two bytes (swapped)
		int versionData = (Util.ubyteToInt(block.data[9]) << 8) | Util.ubyteToInt(block.data[8]);
		versionMajor = versionData >> 12;         // First 4 bits
		versionMinor = (versionData >> 5) & 0x7F; // Middle 7 bits
		versionIncrement = versionData & 0x1F;    // Last 5 bits
		
		// Turn is next two bytes (swapped)
		turn = (Util.ubyteToInt(block.data[11]) << 8) | Util.ubyteToInt(block.data[10]);
		year = 2400 + turn;
		
		// Player data next, 2 bytes swapped
		int playerData = (Util.ubyteToInt(block.data[13]) << 8) | Util.ubyteToInt(block.data[12]);
		encryptionSalt = playerData >> 5;  // First 11 bits
		playerNumber = playerData & 0x1F;  // Last 5 bits
		
		// File type is next byte
		fileType = Util.ubyteToInt(block.data[14]);
		
		// Flags use the last byte of the file header block.  The bits are used like so:
		//   UUU43210
		// Where 'U' is unused. and 43210 correspond to the bit shifts below
		int flags = Util.ubyteToInt(block.data[15]);
		turnSubmitted = (flags & 1) > 0;
		hostUsing =     (flags & (1 << 1)) > 0;
		multipleTurns = (flags & (1 << 2)) > 0;
		gameOver =      (flags & (1 << 3)) > 0;
		shareware =     (flags & (1 << 4)) > 0;
	}
	
	
	@Override
	public String toString() {
		// This is about the most inefficient way to do this
		String s = "";
		
		s += "FileHeaderBlock:\n";
		s += "Magic Number Data: " + Util.bytesToString(magicNumberData, 0, 4) + "\n";
		s += "Magic Number String: " + magicNumberString + "\n";
		s += "Game ID: " + Integer.toHexString(gameId) + "\n";
		s += "Version: " + versionMajor + "." + versionMinor + "." + versionIncrement + "\n"; 
		s += "Turn: " + turn + "; Year: " + year + "\n";
		s += "Player Number: " + playerNumber + "; Displayed as: " + (playerNumber + 1) + "\n";
		s += "Encryption Salt: " + Integer.toHexString(encryptionSalt) + "\n";
		s += "File Type: " + fileType + "\n";
		s += "Flags:\n";
		s += "  Turn Submitted: " + turnSubmitted + "\n";
		s += "  File in use by Host: " + hostUsing + "\n";
		s += "  Multiple Turns in file: " + multipleTurns + "\n";
		s += "  GameOver: " + gameOver + "\n";
		s += "  Shareware: " + shareware + "\n";
		
		return s;
	}
}
