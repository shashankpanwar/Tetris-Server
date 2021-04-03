package sfs2x.extensions.games.tris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.CreateRoomSettings.RoomExtensionSettings;
import com.smartfoxserver.v2.components.login.LoginAssistantComponent;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.db.DBConfig;
import com.smartfoxserver.v2.db.SFSDBManager;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import com.smartfoxserver.v2.entities.variables.SFSRoomVariable;
import com.smartfoxserver.v2.entities.variables.VariableType;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.extensions.SFSExtension;

import sfs2x.extensions.games.model.ControlGame;
import sfs2x.extensions.games.resource.GameStakes;

public class TrisExtension extends SFSExtension {
	static Logger log = Logger.getLogger(TrisExtension.class.getName());
	private static TrisGameBoard gameBoard;
	private static TrisGameBoard gameBoardSecond;
	private User whoseTurn;
	private User whoseTurnSecond;
	private volatile boolean gameStarted;
	private volatile boolean gameStartedSecond;
	private LastGameEndResponse lastGameEndResponse;
	private LastGameEndResponseSecond lastGameEndResponseSecond;
	private int moveCount;
	private int moveCountSecond;
	private final String version = "1.0.6";
	private LoginAssistantComponent lac;
	public static SFSDBManager sfs2xDB;
	public static final String DATABASE_ID = "dbID";
	private static final String PERSISTENCE_UNIT_NAME = "ELTrisExtension";
	private EntityManagerFactory emf;
	private EntityManager em;
	private static ConcurrentHashMap<String, User> gameMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, Integer> userAssignedID = new ConcurrentHashMap<>();
	public static List<Room> roomList = Collections.synchronizedList(new ArrayList<Room>());
	public ScheduledFuture<?> roomTask;
	static {
		if (gameMap == null) {
			gameMap = new ConcurrentHashMap<>();
		}
		if (userAssignedID == null) {
			userAssignedID = new ConcurrentHashMap<>();
		}
	}

	@Override
	public void init() {
		log.info("##############  Tris game Extension for SFS2X started, rel. " + version + " ######################");
		try {
			if (emf == null || em == null) {
				log.info("Initializing the entity");
				emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
				em = emf.createEntityManager();
			}
		} catch (Exception e) {
			log.error("Exception in TrisExtension persistence is -" + e.getMessage());
		}
		SmartFoxServer sfs = SmartFoxServer.getInstance();
		synchronized (sfs) {
			if (em != null) {
				ControlGame control = em.find(ControlGame.class, 1);
				em.getTransaction().begin();
				log.info("##############   control --  " + control + " ######################");
				if (control != null && control.getStatus() == 0) {
					log.info("##############   control.getStatus() --  " + control.getStatus()
							+ " ######################");
					roomTask = sfs.getTaskScheduler().scheduleAtFixedRate(new RoomGenerator(), 0, 10, TimeUnit.SECONDS);
					control.setStatus(1);

				}
				em.getTransaction().commit();
			} else {
				log.warn("##############################################");
				log.warn("##############     EntityManager is not defined      ##############");
				log.warn("##############################################");
			}
		}

		moveCount = 0;

		gameBoard = new TrisGameBoard();
		gameBoardSecond = new TrisGameBoard();
		// configureDB();
		trace("!@#!@#!@#!@# addRequestHandler #@#@#@#");
		addRequestHandler("move", MoveHandler.class);
		addRequestHandler("restart", RestartHandler.class);
		addRequestHandler("ready", ReadyHandler.class);
		addRequestHandler("create", CreateHandler.class);

		trace("!@#!@#!@#!@# addEventHandler #@#@#@#");
//	    addEventHandler(SFSEventType.USER_LOGIN, SignInEventHandler.class);

		addEventHandler(SFSEventType.ROOM_ADDED, onPrintEvent.class);
		addEventHandler(SFSEventType.ROOM_REMOVED, OnUserGoneHandler.class);
		addEventHandler(SFSEventType.USER_JOIN_ROOM, onPrintEvent.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, onPrintEvent.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, OnUserDisjoint.class);
		addEventHandler(SFSEventType.SPECTATOR_TO_PLAYER, OnSpectatorToPlayerHandler.class);
		addEventHandler(SFSEventType.ROOM_ADDED, OnRoomAdd.class);
//		addEventHandler(SFSEventType.USER_VARIABLES_UPDATE, onUserVaraibleUpdate.class);

		trace("!@#!@#!@#!@# second LoginAssistantComponent #@#@#@#");
		if (this.getGameRoom() != null) {
			if (!this.getGameRoom().isGame()) {
				log.info("##############  First this.getGameRoom().isGame()= " + this.getGameRoom().isGame()
						+ " ######################");
				lac = new LoginAssistantComponent(this);
				// Configure the component
				lac.getConfig().loginTable = "users";
				lac.getConfig().userNameField = "name";
				lac.getConfig().nickNameField = "name";
				lac.getConfig().useCaseSensitiveNameChecks = true;
				lac.getConfig().customPasswordCheck = true;
				lac.getConfig().preProcessPlugin = new LoginPreProcess(em);
			}
		} else {
			log.info("##############  Second LoginAssistantComponent ");
			lac = new LoginAssistantComponent(this);
			// Configure the component
			lac.getConfig().loginTable = "users";
			lac.getConfig().userNameField = "name";
			lac.getConfig().nickNameField = "name";
			lac.getConfig().useCaseSensitiveNameChecks = true;
			lac.getConfig().customPasswordCheck = true;
			lac.getConfig().preProcessPlugin = new LoginPreProcess(em);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		trace("Tris game destroyed!");
		if (roomTask != null)
			roomTask.cancel(true);
	}

	TrisGameBoard getGameBoard() {
		return gameBoard;
	}

	TrisGameBoard getGameBoardSecond() {
		return gameBoardSecond;
	}

	void setGameBoardSecond(TrisGameBoard gameBoardSecond) {
		this.gameBoardSecond = gameBoardSecond;
	}

	User getWhoseTurn() {
		return whoseTurn;
	}

	void setTurn(User user) {
		whoseTurn = user;
	}

	void updateTurn() {
		whoseTurn = getParentRoom().getUserByPlayerId(whoseTurn.getPlayerId() == 1 ? 2 : 1);
	}

	public void setWhoseTurnSecond(User user) {
		whoseTurnSecond = user;
	}

	public User getWhoseTurnSecond() {
		return whoseTurnSecond;
	}

	void updateTurnSecond(User user) {
		log.info("@#@# updateTurnSecond whoseTurnSecond- " + whoseTurnSecond
				+ " and Arrays.toString(getParentRoom().getPlayersList().toArray()) - "
				+ Arrays.toString(getParentRoom().getPlayersList().toArray()) + " and whoseTurnSecond.getPlayerId() - "
				+ whoseTurnSecond.getPlayerId());
		whoseTurnSecond = getParentRoom().getUserByPlayerId(whoseTurnSecond.getPlayerId() == 1 ? 2 : 1);
		log.info("user recieved - " + user + " and playerID - " + user.getPlayerId());
	}

	public int getMoveCount() {
		return moveCount;
	}

	public void increaseMoveCount() {
		++moveCount;
	}

	public int getMoveCountSecond() {
		return moveCountSecond;
	}

	public void increaseMoveCountSecond() {
		++moveCountSecond;
	}

	boolean isGameStarted() {
		return gameStarted;
	}

	public boolean isGameStartedSecond() {
		return gameStartedSecond;
	}

	public static Logger getLog() {
		return log;
	}

	public String getVersion() {
		return version;
	}

	public LoginAssistantComponent getLac() {
		return lac;
	}

	public static SFSDBManager getSfs2xDB() {
		return sfs2xDB;
	}

	public static String getDatabaseId() {
		return DATABASE_ID;
	}

	public static String getPersistenceUnitName() {
		return PERSISTENCE_UNIT_NAME;
	}

	public EntityManagerFactory getEmf() {
		return emf;
	}

	public EntityManager getEm() {
		return em;
	}

	public static ConcurrentHashMap<String, User> getGameMap() {
		return gameMap;
	}

	public static void setGameMap(ConcurrentHashMap<String, User> gameMap) {
		TrisExtension.gameMap = gameMap;
	}

	public static ConcurrentHashMap<String, Integer> getUserAssignedID() {
		return userAssignedID;
	}

	public static void setUserAssignedID(ConcurrentHashMap<String, Integer> userAssignedID) {
		TrisExtension.userAssignedID = userAssignedID;
	}

	public static List<Room> getRoomList() {
		return TrisExtension.roomList;
	}

	public static void setRoomList(List<Room> roomList) {
		TrisExtension.roomList = roomList;
	}

	void startGame() {
		if (gameStarted) {
			log.info("@#@@#@# Game is already started");
			throw new IllegalStateException("Game is already started!");
		}

		lastGameEndResponse = null;
		gameStarted = true;
		gameBoard.reset();
		log.info(" #@@#@# Game First getParentRoom().getPlayersList() - "
				+ Arrays.toString(getParentRoom().getPlayersList().toArray()));
		User player1 = getParentRoom().getUserByPlayerId(1);
		User player2 = getParentRoom().getUserByPlayerId(2);
		log.info("player1 - " + player1 + " and player 2 - " + player2);

		// No turn assigned? Let's start with player 1
		if (whoseTurn == null) {
			whoseTurn = player1;
			log.info("whoseTurn - " + whoseTurn.getName());
		}

		// Send START event to client
		ISFSObject resObj = new SFSObject();
		resObj.putInt("t", whoseTurn.getPlayerId());
		resObj.putUtfString("p1n", player1.getName());
		resObj.putInt("p1i", player1.getId());
		resObj.putUtfString("p2n", player2.getName());
		resObj.putInt("p2i", player2.getId());
		log.info("Sending Start Extension getParentRoom().getUserList() - " + getParentRoom().getUserList());
		send("start", resObj, getParentRoom().getUserList());
	}

	void startGameSecond() {
		if (gameStarted) {
			log.info("@#@@#@# Game Second is already started");
			throw new IllegalStateException("Game is already started!");
		}

		lastGameEndResponseSecond = null;
		gameStartedSecond = true;
		gameBoardSecond.reset();
		log.info(" #@@#@# Game Second getParentRoom().getPlayersList() - "
				+ Arrays.toString(getParentRoom().getPlayersList().toArray()));
		User player1 = getParentRoom().getUserByPlayerId(1);
		User player2 = getParentRoom().getUserByPlayerId(2);
		log.info(" Second player1 - " + player1 + " and player 2 - " + player2);

		// No turn assigned? Let's start with player 1
		if (whoseTurnSecond == null) {
			whoseTurnSecond = player1;
			log.info("whoseTurnSecond - " + whoseTurnSecond.getName());
		}

		// Send START event to client
		ISFSObject resObj = new SFSObject();
		resObj.putInt("t", whoseTurnSecond.getPlayerId());
		resObj.putUtfString("p1n", player1.getName());
		resObj.putInt("p1i", player1.getId());
		resObj.putUtfString("p2n", player2.getName());
		resObj.putInt("p2i", player2.getId());
		log.info("Sending Start Extension getParentRoom().getUserList() - " + getParentRoom().getUserList());
		send("start", resObj, getParentRoom().getUserList());
	}

	void stopGame() {
		stopGame(false);
	}

	void stopGame(boolean resetTurn) {
		gameStarted = false;
		moveCount = 0;
		whoseTurn = null;
	}

	void stopGameSecond() {
		stopGame(false);
	}

	void stopGameSecond(boolean resetTurn) {
		gameStartedSecond = false;
		moveCountSecond = 0;
		whoseTurnSecond = null;
	}

	Room getGameRoom() {
		return this.getParentRoom();
	}

	LastGameEndResponse getLastGameEndResponse() {
		return lastGameEndResponse;
	}

	void setLastGameEndResponse(LastGameEndResponse lastGameEndResponse) {
		this.lastGameEndResponse = lastGameEndResponse;
	}

	LastGameEndResponseSecond getLastGameEndResponseSecond() {
		return lastGameEndResponseSecond;
	}

	void setLastGameEndResponseSecond(LastGameEndResponseSecond lastGameEndResponse) {
		this.lastGameEndResponseSecond = lastGameEndResponse;
	}

	void updateSpectator(User user) {
		ISFSObject resObj = new SFSObject();
		User player1 = getParentRoom().getUserByPlayerId(1);
		User player2 = getParentRoom().getUserByPlayerId(2);

		resObj.putInt("t", whoseTurn == null ? 0 : whoseTurn.getPlayerId());
		resObj.putBool("status", gameStarted);
		resObj.putSFSArray("board", gameBoard.toSFSArray());

		if (player1 == null)
			resObj.putInt("p1i", 0); // <--- indicates no P1
		else {
			resObj.putInt("p1i", player1.getId());
			resObj.putUtfString("p1n", player1.getName());
		}

		if (player2 == null)
			resObj.putInt("p2i", 0); // <--- indicates no P2
		else {
			resObj.putInt("p2i", player2.getId());
			resObj.putUtfString("p2n", player2.getName());
		}

		send("specStatus", resObj, user);
	}

	void updateSpectatorSecond(User user) {
		ISFSObject resObj = new SFSObject();

		User player1 = getParentRoom().getUserByPlayerId(1);
		User player2 = getParentRoom().getUserByPlayerId(2);

		resObj.putInt("t", whoseTurnSecond == null ? 0 : whoseTurnSecond.getPlayerId());
		resObj.putBool("status", gameStartedSecond);
		resObj.putSFSArray("board", gameBoardSecond.toSFSArray());

		if (player1 == null)
			resObj.putInt("p1i", 0); // <--- indicates no P1
		else {
			resObj.putInt("p1i", player1.getId());
			resObj.putUtfString("p1n", player1.getName());
		}

		if (player2 == null)
			resObj.putInt("p2i", 0); // <--- indicates no P2
		else {
			resObj.putInt("p2i", player2.getId());
			resObj.putUtfString("p2n", player2.getName());

		}

		send("specStatus", resObj, user);
	}

	public void configureDB() {
		try {
			if (sfs2xDB == null) {
				log.info("Initializing the Configure DB");
				DBConfig cfg = new DBConfig();
				cfg.active = true;
				cfg.driverName = "com.mysql.cj.jdbc.Driver";
				cfg.connectionString = "jdbc:mysql://127.0.0.1:3306/sfs2x";
				cfg.userName = "root";
				cfg.password = "SH@SH@nK4";
				cfg.testSql = "SELECT * FROM sfs2x.users limit 1";
				// Create DBManager
				sfs2xDB = new SFSDBManager(cfg);
				Zone zone = getParentZone();
				sfs2xDB.init(zone);
			}
		} catch (Exception e) {
			log.error("Exception inside the TrisExtension is - " + e.getMessage());
		}
	}

	class RoomGenerator implements Runnable {
		Logger log = Logger.getLogger(RoomGenerator.class.getName());
		private AtomicInteger runningCycles = new AtomicInteger(0);
		private AtomicInteger gameCount = new AtomicInteger(0), roomCount = new AtomicInteger(5);
		private int stakes[] = new int[] { 100, 200, 400, 800, 1600 };
		private String extenID = "TrisExtension";
		private String extenClass = "sfs2x.extensions.games.tris.TrisExtension", groupId = "trisGames";

		@Override
		public void run() {
			try {
				runningCycles.getAndIncrement();
				log.info("Inside the running task. Cycle:  " + runningCycles.get());
				if (TrisExtension.getRoomList().size() < 5) {
					try {
						log.info("@#@#@@# RoomGenerator  TrisExtension.getRoomList().size - "
								+ TrisExtension.getRoomList().size());
						createRoom(false, null);
					} catch (Exception e) {
						log.error(" Exception insdie the RoomGenerator.class size 0  is - " + e.getMessage()
								+ " , TrisExtension.getRoomList().size()- " + TrisExtension.getRoomList().size());

					}
				} else {
					List<Room> roomList = TrisExtension.getRoomList();
					if (roomList.size() > 0) {
						log.info("@#@#@@# RoomGenerator  TrisExtension.getRoomList().size > 0  - "
								+ TrisExtension.getRoomList().size());
						List<Room> roomWithActive = roomList.stream().filter(room -> room.getPlayersList().size() > 0)
								.collect(Collectors.toList());
						for (Room room : roomWithActive) {
							try {

								createRoom(true, room);
							} catch (Exception e) {
								log.error(" Exception insdie the RoomGenerator.class size > 0  is - " + e.getMessage()
										+ " , TrisExtension.getRoomList().size()- "
										+ TrisExtension.getRoomList().size());
							}
						}
						log.info(" @#@#@# Done with traversing with List size - ***  - " + roomWithActive.size());
					}
				}

			} catch (Exception e) {
				log.error("Exception inside the RoomGenerator.class is -  " + e.getMessage());
			}
		}

		public void createRoom(boolean copy, Room room) throws SFSCreateRoomException {
			CreateRoomSettings crs = new CreateRoomSettings();
			log.info("******************************************");
	
			RoomExtensionSettings res = new CreateRoomSettings.RoomExtensionSettings(extenID, extenClass);
			List<RoomVariable> roomList = new LinkedList<RoomVariable>();
			if (!copy) {
				log.info("******************          Creating Room          ***********************");
				gameCount.getAndIncrement();
				RoomVariable round = null;
				if (gameCount.get() != 0) {
					if (GameStakes.StakeFirst.getId() == gameCount.get()) {
						round = new SFSRoomVariable("stake",  String.valueOf(GameStakes.StakeFirst.getStake()));
					} else if (GameStakes.StakeSecond.getId() == gameCount.get()) {
						round = new SFSRoomVariable("stake",  String.valueOf(GameStakes.StakeSecond.getStake()));
					} else if (GameStakes.StakeThird.getId() == gameCount.get()) {
						round = new SFSRoomVariable("stake",  String.valueOf(GameStakes.StakeThird.getStake()));
					} else if (GameStakes.StakeFourth.getId() == gameCount.get()) {
						round = new SFSRoomVariable("stake",  String.valueOf(GameStakes.StakeFourth.getStake()));
					} else {
						round = new SFSRoomVariable("stake",  String.valueOf(GameStakes.StakeFifth.getStake()));
					}

				}
				if (round != null) {
					round.setGlobal(true);
					round.setPrivate(true);
					roomList.add(round);
				}
				
				crs.setName("game" + gameCount.get());
			} else {
				log.info("******************          Creating Copy Room          ***********************");
				List<Room> availableRooms = getParentZone().getRoomList();
				boolean alreadyAvail = availableRooms.stream()
						.anyMatch(localRoom -> localRoom.getName().equalsIgnoreCase(room.getName()) && localRoom.getName().contains("_copy"));
				log.info("******************          alreadyAvail  -" + alreadyAvail);
				if (!alreadyAvail) {
					RoomVariable round = null;
					if (GameStakes.StakeFirst.getGameName().equals(room.getName())) {
						round = new SFSRoomVariable("stake",  String.valueOf(GameStakes.StakeFirst.getStake()));
					} else if (GameStakes.StakeSecond.getGameName().equals(room.getName())) {
						round = new SFSRoomVariable("stake", String.valueOf(GameStakes.StakeSecond.getStake()));
					} else if (GameStakes.StakeThird.getGameName().equals(room.getName())) {
						round = new SFSRoomVariable("stake", String.valueOf(GameStakes.StakeThird.getStake()));
					} else if (GameStakes.StakeFourth.getGameName().equals(room.getName())) {
						round = new SFSRoomVariable("stake", String.valueOf(GameStakes.StakeFourth.getStake()));
					} else {
						round = new SFSRoomVariable("stake", String.valueOf(GameStakes.StakeFifth.getStake()));
					}
					if (round != null) {
						round.setGlobal(true);
						round.setPrivate(true);
						roomList.add(round);
					}
					crs.setName(room.getName() + "_copy");
				} else {
					log.info(" ((((((((((( Room Already exist )))))))))))))))) ");

				}
			}
			if (crs.getName() != null && !crs.getName().isEmpty()) {
				crs.setGame(true);
				crs.setMaxUsers(2);
				crs.setMaxSpectators(2);
				crs.setGroupId(groupId);
				crs.setExtension(res);
				crs.setDynamic(true);
				crs.setRoomVariables(roomList);
				Room gameRoom = getParentZone().createRoom(crs);
				TrisExtension.getRoomList().add(gameRoom);
				log.info("@#@#@@# RoomGenerator room created i.e name - " + gameRoom.getName());
				log.info("$$$$$$$$$$$$$  gameRoom.getVariable(\"stake\")   "
						+ gameRoom.getVariable("stake").getStringValue());
			}
		}
	}

}
