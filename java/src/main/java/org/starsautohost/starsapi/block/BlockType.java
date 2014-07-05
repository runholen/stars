package org.starsautohost.starsapi.block;

/**
 * Taken from http://wiki.starsautohost.org/wiki/Technical_Information
 */
public class BlockType {
	public static int FILE_FOOTER = 0;
	public static int MANUAL_SMALL_LOAD_UNLOAD_TASK = 1;
	public static int MANUAL_MEDIUM_LOAD_UNLOAD_TASK = 2;
	public static int WAYPOINT_DELETE = 3;
	public static int WAYPOINT_ADD = 4;
	public static int WAYPOINT_CHANGE_TASK = 5;
	public static int PLAYER = 6;
	public static int PLANETS = 7;
	public static int FILE_HEADER = 8; // (unencrypted)
	public static int FILE_HASH = 9;
	public static int WAYPOINT_REPEAT_ORDERS = 10;
	public static int UNKNOWN_BLOCK_11 = 11;
	public static int EVENTS = 12;
	public static int PLANET = 13;
	public static int PARTIAL_PLANET = 14;
	public static int UNKNOWN_BLOCK_15 = 15;
	public static int FLEET = 16;
	public static int PARTIAL_FLEET = 17;
	public static int UNKNOWN_BLOCK_18 = 18;
	public static int WAYPOINT_TASK = 19;
	public static int WAYPOINT = 20;
	public static int FLEET_NAME = 21;
	public static int UNKNOWN_BLOCK_22 = 22;
	public static int MOVE_SHIPS = 23;
	public static int FLEET_SPLIT = 24;
	public static int MANUAL_LARGE_LOAD_UNLOAD_TASK = 25;
	public static int DESIGN = 26;
	public static int DESIGN_CHANGE = 27;
	public static int PRODUCTION_QUEUE = 28;
	public static int PRODUCTION_QUEUE_CHANGE = 29;
	public static int BATTLE_PLAN = 30;
	public static int BATTLE = 31; // (content isn't decoded yet)
	public static int COUNTERS = 32;
	public static int MESSAGES_FILTER = 33;
	public static int RESEARCH_CHANGE = 34;
	public static int PLANET_CHANGE = 35;
	public static int CHANGE_PASSWORD = 36;
	public static int FLEETS_MERGE = 37;
	public static int PLAYERS_RELATION_CHANGE = 38;
	public static int BATTLE_CONTINUATION = 39; // (content isn't decoded yet)
	public static int MESSAGE = 40;
	public static int AI_H_FILE_RECORD = 41;
	public static int SET_FLEET_BATTLE_PLAN = 42;
	public static int OBJECT = 43;
	public static int RENAME_FLEET = 44;
	public static int PLAYER_SCORES = 45;
	public static int SAVE_AND_SUBMIT = 46;
	
	// Default
	public static int UNKNOWN_BAD = -1;
}
