package org.starsautohost.starsapi.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.starsautohost.starsapi.Util;
import org.starsautohost.starsapi.block.Block;
import org.starsautohost.starsapi.block.BlockType;
import org.starsautohost.starsapi.block.FileHeaderBlock;


/**
 * @author raptor
 * 
 * Algorithms ruthlessly taken from the decompiled StarsHostEditor 0.3 .NET 
 * DLL.  Thanks to all those who did the hard work in a disassembler to 
 * figure these out.
 * 
 * Note:   Requires JRE 1.6 or later
 * Note 2: Java does not have unsigned data types which made some of this work
 *         a little trickier.  (see Util.ubyteToInt)
 */
public class Decryptor   
{
	private static int BLOCK_HEADER_SIZE = 2;  // bytes
	private static int BLOCK_MAX_SIZE = 1024;  // bytes
	
	/**
	 * The first 64 prime numbers, after '2' (so all are odd).
	 * These are used as starting seeds to the random number
	 * generator.
	 * 
	 * IMPORTANT:  One number here is not prime (279).  I thought
	 * it should be replaced with 269, which is prime.  But newer
	 * versions of StarHostEditor and related code use 279.  I have
	 * been unable to trigger it as a starting seed so I don't know 
	 * which is better.
	 */
    private int[] primes = new int[] { 
    		3, 5, 7, 11, 13, 17, 19, 23, 
    		29, 31, 37, 41, 43, 47, 53, 59,
    		61, 67, 71, 73, 79, 83, 89, 97,
    		101, 103, 107, 109, 113, 127, 131, 137,
    		139, 149, 151, 157, 163, 167, 173, 179,
    		181, 191, 193, 197, 199, 211, 223, 227,
    		229, 233, 239, 241, 251, 257, 263, 279,
    		271, 277, 281, 283, 293, 307, 311, 313 
    };


	private FileHeaderBlock fileHeaderBlock = null;
	private StarsRNG random = null;
    
	
	/**
	 * Stars Random Number Generator
	 * 
	 * This needs to be seeded and is used for decryption.
	 * 
	 * Each new random number uses new seeds based on previous seeds and
	 * some possibly random constants
	 */
	private class StarsRNG {
		// We'll use 'long' for our seeds to avoid signed-integer problems
		private long seedA;
		private long seedB;
		
		private int rounds;
		
		public StarsRNG(int prime1, int prime2, int initRounds) {
			seedA = prime1;
			seedB = prime2;
			rounds = initRounds;
			
			// Now initialize a few rounds
			for(int i = 0; i < rounds; i++)
				nextRandom();
		}
		
		/**
		 * Get the next random number with this seeded generator
		 * 
		 * @return
		 */
		public long nextRandom() {
			// First, calculate new seeds using some constants
			long seedApartA = (seedA % 53668) * 40014;
			long seedApartB = (seedA / 53668) * 12211;  // integer division OK
			long newSeedA = seedApartA - seedApartB;
			
			long seedBpartA = (seedB % 52774) * 40692;
			long seedBpartB = (seedB / 52774) * 3791;
			long newSeedB = seedBpartA - seedBpartB;
			
			// If negative add a whole bunch (there's probably some weird bit math
			// going on here that the disassembler didn't make obvious)
			if(newSeedA < 0)
				newSeedA += 0x7fffffab;

			if(newSeedB < 0)
				newSeedB += 0x7fffff07;
			
			// Set our new seeds
			seedA = newSeedA;
			seedB = newSeedB;
			
			// Generate "random" number.  This will fit into an unsigned 32bit integer
			// We use 'long' because...  java...
			long randomNumber = seedA - seedB;
			if(seedA < seedB)
				randomNumber += 0x100000000l;  // 2^32

			// DEBUG
//			System.out.println("seed1: " + seedA + "; seed2: " + seedB);
//			System.out.println("rand: " + randomNumber);
			
			// Now return our random number
			return randomNumber;
		}
		
		
		@Override
		public String toString() {
			String s = "Random Number Generator:\n";
			
			s += "Seed 1: " + seedA + "\n";
			s += "Seed 2: " + seedB + "\n";
			s += "Rounds: " + rounds + "\n";
			
			return s;
		}
	}
	
	
	/**
	 * Initialize the decryption system by seeding and initializing a
	 * random number generator
	 * 
	 * @throws Exception
	 */
	private void initDecryption() throws Exception {
		int salt = fileHeaderBlock.encryptionSalt;
		
		// Use two prime numbers as random seeds.
		// First one comes from the lower 5 bits of the salt
		int index1 = salt & 0x1F;
		// Second index comes from the next higher 5 bits
		int index2 = (salt >> 5) & 0x1F;
		
		// Adjust our indexes if the highest bit (bit 11) is set
		// If set, change index1 to use the upper half of our primes table
		if((salt >> 10) == 1)
			index1 += 32;
		// Else index2 uses the upper half of the primes table
		else
			index2 += 32;
			
		// Determine the number of initialization rounds from 4 other data points
		// 0 or 1 if shareware (I think this is correct, but may not be - so far
		// I have not encountered a shareware flag
		int part1 = fileHeaderBlock.shareware ? 1 : 0;
		
		// Lower 2 bits of player number, plus 1
		int part2 = (fileHeaderBlock.playerNumber & 0x3) + 1;
		
		// Lower 2 bits of turn number, plus 1
		int part3 = (fileHeaderBlock.turn & 0x3) + 1;
		
		// Lower 2 bits of gameId, plus 1
		int part4 = (fileHeaderBlock.gameId & 0x3) + 1;
		
		// Now put them all together, this could conceivably generate up to 65 
		// rounds  (4 * 4 * 4) + 1
		int rounds = (part4 * part3 * part2) + part1;
		
		// Now initialize our random number generator
		random = new StarsRNG(primes[index1], primes[index2], rounds);
	}


	/**
	 * Decrypt the given block.
	 * 
	 * The first call to this will be the File Header Block which will
	 * be used to initialize the decryption system
	 * 
	 * @param block
	 * @throws Exception
	 */
	private void decryptBlock(Block block) throws Exception {
		// If it's a header block, it's unencrypted and will be used to 
		// initialize the decryption system
		if(block.type == BlockType.FILE_HEADER) { // Not encrypted
			fileHeaderBlock = new FileHeaderBlock(block);
			block.encrypted = false;
			
			initDecryption();
			// DEBUG
//			System.out.println(fileHeaderBlock);
//			System.out.println(random);
			
			return;
		}
		
		// Now decrypt, processing 4 bytes at a time
		for(int i = 0; i < block.size; i+=4) {
			// Swap bytes:  4 3 2 1
			long chunk = (Util.ubyteToInt(block.data[i+3]) << 24)
					| (Util.ubyteToInt(block.data[i+2]) << 16)
					| (Util.ubyteToInt(block.data[i+1]) << 8)
					| Util.ubyteToInt(block.data[i]);
			
//			System.out.println("chunk: " + chunk);
			
			// XOR with a random number
			long decryptedChunk = chunk ^ random.nextRandom();
//			System.out.println("dechunk: " + decryptedChunk);
			
			// Write out the decrypted data, swapped back
			block.decryptedData[i] =  (byte) (decryptedChunk & 0xFF);
			block.decryptedData[i+1] =  (byte) ((decryptedChunk >> 8)  & 0xFF);
			block.decryptedData[i+2] =  (byte) ((decryptedChunk >> 16)  & 0xFF);
			block.decryptedData[i+3] =  (byte) ((decryptedChunk >> 24)  & 0xFF);
		}
	}
		
	
	/**
	 * This will detect and return a block with its type, size, and block of the 
	 * given block from the given data
	 * 
	 * Details of a header block bitwise: XXXXXXXX YYYYYYZZ
     *   (XXXXXXXX is a first byte, YYYYYYZZ is a second byte) 
     *   
	 * Where:
     *   YYYYYY is a block type.
     *   ZZXXXXXXXX is a block size. 
	 * 
	 * @param currentIndex
	 * @param fileBytes
	 * @return
	 */
	private Block parseBlock(int currentIndex, byte[] fileBytes) {
		Block block = new Block();

		// We have to do a bitwise AND with 0xFF to convert from unsigned byte to int
		int byte1 = Util.ubyteToInt(fileBytes[currentIndex]);
		int byte2 = Util.ubyteToInt(fileBytes[currentIndex+1]);

		int lowBits = byte2 & 3;
		
		block.size = (lowBits << 8) | byte1;
		block.type = byte2 >> 2;
		
		// We must have a padded byte array because decryption works on 4
		// bytes at a time
		int paddedLength = (block.size + 3) & ~0x03;  // Tricky way to round up to a multiple of 4
		block.data = new byte[paddedLength];
		block.decryptedData = new byte[paddedLength];
		
		// Now copy out our data
		System.arraycopy(fileBytes, currentIndex + 2, block.data, 0, block.size);
		
		return block;
	}


	/**
	 * Some blocks have more data at the end of the block (like PLANETS).  
	 * Detect this, parse the data, and return the size of the data.
	 * 
	 * Requires decryption to have been done on the block data
	 * 
	 * @param startIndex
	 * @param fileBytes
	 * @param block
	 * @return
	 */
	private int postProcessBlock(int startIndex, byte[] fileBytes, Block block) {
		int size = 0;
		
		if(block.type == BlockType.PLANETS) {
			// Planet size is determined by swapping bytes 10 and 11
			// the decrypted block and concatenating their bits, e.g.:
			//   byte 10 - XXXXXXXX
			//   byte 11 - YYYYYYYY
			// planetSize = YYYYYYYYXXXXXXXX
			int planetSize = (Util.ubyteToInt(block.decryptedData[11]) << 8) 
					| Util.ubyteToInt(block.decryptedData[10]);
			
			// There are 4 bytes per planet
			size = planetSize * 4;
			
			block.otherData = Arrays.copyOfRange(fileBytes, startIndex, startIndex + size);
		}
		
		block.otherDataSize = size;
		
		return size;
	}
	

	/**
	 * Read in a Stars! file to decrypt it.  This returns a List of all blocks found
	 * within.  Each encrypted block will be decrypted.
	 * 
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Block> readFile(String filename) throws Exception {
		
		// Read in the full file to a byte array...  we have the RAM
		File file = new File(filename);
		FileInputStream fileInputStream = new FileInputStream(file);
		
		byte[] fileBytes = new byte[(int) file.length()];
		
		fileInputStream.read(fileBytes);
		fileInputStream.close();
		
		
		// Round 1: Block-parsing
		ArrayList<Block> blockList = new ArrayList<Block>();
		
		// Index where we start to read the next block
		int currentIndex = 0;
		
		while(currentIndex < file.length()) {
			// Initial parse of our block
			Block block = parseBlock(currentIndex, fileBytes);
			
			decryptBlock(block);

			// Advance our read index
			currentIndex = currentIndex + block.size + BLOCK_HEADER_SIZE;
			
			// Check to see if we need to grab even more data before the next block
			int dataSize = postProcessBlock(currentIndex, fileBytes, block);
			
			// Advance the index again
			currentIndex = currentIndex + dataSize;

			// DEBUG
//			System.out.println(block);
			
			// Bounds checking
			if(block.size > BLOCK_MAX_SIZE)
				throw new Exception("Bad block size: " + block.size);
			
			if(block.type == BlockType.UNKNOWN_BAD || block.type > BlockType.SAVE_AND_SUBMIT || block.type < BlockType.FILE_FOOTER)
				throw new Exception("Bad block type encountered: " + block.type);

			
			// Store block for later parsing
			blockList.add(block);
		}
		
		return blockList;
	}
	
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		Decryptor d = new Decryptor();
		
		// This list has everything you want!
		ArrayList<Block> blockList = d.readFile("game.xy");
		
		for(Block block: blockList)
			System.out.println(block);
		
		// Do something with the block list
		
		System.out.println("Done");
	}
}


