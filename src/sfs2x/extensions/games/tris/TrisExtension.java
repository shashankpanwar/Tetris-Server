package sfs2x.extensions.games.tris;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import com.smartfoxserver.v2.components.login.LoginAssistantComponent;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.db.DBConfig;
import com.smartfoxserver.v2.db.SFSDBManager;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.Zone;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

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
	private static ConcurrentHashMap<String,User> gameMap = new ConcurrentHashMap<>();
	static
	{
		if(gameMap == null)
		{
			gameMap = new ConcurrentHashMap<>();
		}
	}
	
	@Override
	public void init() {
		log.info("##############  Tris game Extension for SFS2X started, rel. " + version + " ######################");
		emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		em = emf.createEntityManager();
		moveCount = 0;

		gameBoard = new TrisGameBoard();
		gameBoardSecond = new TrisGameBoard();
		configureDB();
		trace("!@#!@#!@#!@# addRequestHandler #@#@#@#");
		addRequestHandler("move", MoveHandler.class);
		addRequestHandler("restart", RestartHandler.class);
		addRequestHandler("ready", ReadyHandler.class);
		addRequestHandler("create", CreateHandler.class);

		trace("!@#!@#!@#!@# addEventHandler #@#@#@#");
//	    addEventHandler(SFSEventType.USER_LOGIN, SignInEventHandler.class);
		addEventHandler(SFSEventType.ROOM_ADDED, OnUserGoneHandler.class);
		addEventHandler(SFSEventType.ROOM_REMOVED, OnUserGoneHandler.class);
		addEventHandler(SFSEventType.USER_JOIN_ROOM, OnUserGoneHandler.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, OnUserGoneHandler.class);
		addEventHandler(SFSEventType.USER_LEAVE_ROOM, OnUserGoneHandler.class);
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
		log.info("@#@# updateTurnSecond whoseTurnSecond- "+whoseTurnSecond + " and Arrays.toString(getParentRoom().getPlayersList().toArray()) - "+Arrays.toString(getParentRoom().getPlayersList().toArray())+" and whoseTurnSecond.getPlayerId() - "+whoseTurnSecond.getPlayerId());
		whoseTurnSecond = getParentRoom().getUserByPlayerId(whoseTurnSecond.getPlayerId() == 1 ? 2 : 1);
		log.info("user recieved - "+user + " and playerID - "+user.getPlayerId());
		if(whoseTurnSecond == null)
		{
			//log.info("Updating whoseturn second for whoseTurnSecond - "+whoseTurnSecond +" to  user -"+user);
			List<User> userList = getParentRoom().getUserList();
			for(User userLocal:userList)
			{
				log.info("Before Updating userLocal.getName() - "+userLocal.getName() + " and  user.getName() - "+user.getName());
				if(!userLocal.getName().equalsIgnoreCase(user.getName()))
				{
					log.info("Final Updating whoseturn second for user - "+userLocal + " playerId-  "+userLocal.getPlayerId());
					if(user.getPlayerId() == userLocal.getPlayerId())
					{
						int id = userLocal.getPlayerId() == 1 ? 2 : 1;
						userLocal.setPlayerId(id, userLocal.getLastJoinedRoom());
						log.info(" Final update after userLocal.getPlayerId() - "+userLocal.getPlayerId());
					}
					whoseTurnSecond = userLocal;
				}
			}
		}
		log.info("@#@# updateTurnSecond whoseTurnSecond- "+whoseTurnSecond );
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

	void startGame() {
		if (gameStarted) {
			log.info("@#@@#@# Game is already started");
			throw new IllegalStateException("Game is already started!");
		}

		lastGameEndResponse = null;
		gameStarted = true;
		gameBoard.reset();
		log.info(" #@@#@# Game First getParentRoom().getPlayersList() - "+Arrays.toString(getParentRoom().getPlayersList().toArray()));
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
		log.info(" #@@#@# Game Second getParentRoom().getPlayersList() - "+Arrays.toString(getParentRoom().getPlayersList().toArray()));
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

}
