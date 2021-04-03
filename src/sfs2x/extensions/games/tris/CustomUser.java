/**
 * 
 */
package sfs2x.extensions.games.tris;

import java.util.List;
import java.util.Map;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.buddylist.BuddyProperties;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.Zone;

/**
 * @author Shashank Panwar
 *
 */
public class CustomUser {

	private int userID;
	private ISession session;
	private String ipAddress;
	private String name;
	private BuddyProperties buddProperties;
	private boolean local;
	private boolean npc;
	private long loginTime;
	private Room lastJoinedRoom;
	private List<Room> joinedRooms;
	private List<Room> createdRoom;
	private Zone zone;
	private int playerId;
	private Map<Room,Integer> playerIds;
	private boolean player;
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public ISession getSession() {
		return session;
	}
	public void setSession(ISession session) {
		this.session = session;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BuddyProperties getBuddProperties() {
		return buddProperties;
	}
	public void setBuddProperties(BuddyProperties buddProperties) {
		this.buddProperties = buddProperties;
	}
	public boolean isLocal() {
		return local;
	}
	public void setLocal(boolean local) {
		this.local = local;
	}
	public boolean isNpc() {
		return npc;
	}
	public void setNpc(boolean npc) {
		this.npc = npc;
	}
	public long getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	public Room getLastJoinedRoom() {
		return lastJoinedRoom;
	}
	public void setLastJoinedRoom(Room lastJoinedRoom) {
		this.lastJoinedRoom = lastJoinedRoom;
	}
	public List<Room> getJoinedRooms() {
		return joinedRooms;
	}
	public void setJoinedRooms(List<Room> joinedRooms) {
		this.joinedRooms = joinedRooms;
	}
	public List<Room> getCreatedRoom() {
		return createdRoom;
	}
	public void setCreatedRoom(List<Room> createdRoom) {
		this.createdRoom = createdRoom;
	}
	public Zone getZone() {
		return zone;
	}
	public void setZone(Zone zone) {
		this.zone = zone;
	}
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public Map<Room, Integer> getPlayerIds() {
		return playerIds;
	}
	public void setPlayerIds(Map<Room, Integer> playerIds) {
		this.playerIds = playerIds;
	}
	public boolean isPlayer() {
		return player;
	}
	public void setPlayer(boolean player) {
		this.player = player;
	}
	
	
}
