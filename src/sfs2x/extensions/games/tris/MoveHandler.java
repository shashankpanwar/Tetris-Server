package sfs2x.extensions.games.tris;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSRuntimeException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;

public class MoveHandler extends BaseClientRequestHandler {
	static Logger log = Logger.getLogger(MoveHandler.class.getName());
	private static final String CMD_WIN = "win";
	private static final String CMD_TIE = "tie";
	private static final String CMD_MOVE = "move";
	@Override
	public void handleClientRequest(User user, ISFSObject params) {
		try {

			TrisExtension gameExt = (TrisExtension) getParentExtension();
			// Check params
			if (!params.containsKey("x") || !params.containsKey("y"))
				throw new SFSRuntimeException("Invalid request, one mandatory param is missing. Required 'x' and 'y'");

			if (gameExt.getGameRoom().getName().equalsIgnoreCase("game1")) {
				TrisGameBoard board = gameExt.getGameBoard();
				gameExt.trace("********************  MoveHandler First***********************************");
				int moveX = params.getInt("x");
				int moveY = params.getInt("y");

				log.info(String.format(" Handling move from player %s. (%s, %s) = %s and Game- %s ", user.getPlayerId(),
						moveX, moveY, board.getTileAt(moveX, moveY), gameExt.getGameRoom().getName()));

				if (gameExt.isGameStarted()) {
					log.info("MoveHandler First gameExt.isGameStarted()" + gameExt.isGameStarted()
							+ " gameExt.getWhoseTurn()- " + gameExt.getWhoseTurn() + " and  user-" + user.getName());
					if (gameExt.getWhoseTurn() == null) {
						log.info("MoveHandler First whoseturn is  null and **Updating** - gameExt.getGameMap().get(user.getName()) -- "
								+ gameExt.getGameMap().get(gameExt.getGameRoom().getName()));
						if (gameExt.getGameMap().get((gameExt.getGameRoom().getName())) != null) {
							log.info("MoveHandler First setting the turn whoseturn is null ");
							gameExt.setTurn(gameExt.getGameMap().get(gameExt.getGameRoom().getName()));
						}
					}
					if (gameExt.getWhoseTurn() == user) {
						log.info("MoveHandler First whoseturn matched user and board.getTileAt(moveX, moveY)- "
								+ board.getTileAt(moveX, moveY));
						if (board.getTileAt(moveX, moveY) == Tile.EMPTY) {
							log.info("MoveHandler First equal empty user.getPlayerId()- " + user.getPlayerId());
							if (gameExt.getGameMap().get(gameExt.getGameRoom().getName()) != null) {
								log.info("MoveHandler First getting the value for playerId -  "
										+ gameExt.getGameRoom().getName());
								User userGame = gameExt.getGameMap().get(gameExt.getGameRoom().getName());
								Map<Room, Integer> localMap = userGame.getPlayerIds();
								log.info("MoveHandler First Usergame - " + userGame.getName()
										+ " and  user.getPlayerId() - " + user.getPlayerId());
								final User innerUser = user;
								localMap.forEach((Room, Id) -> {
									if (Room.getName().equalsIgnoreCase(gameExt.getGameRoom().getName())) {
										log.info(" user.getPlayerId() -  "+innerUser.getPlayerId() +" id - "+Id);
										if (innerUser.getPlayerId() != Id) {
											log.info(String.format("Updating First the playerId-%s to id- %s for user- %s",
													innerUser.getPlayerId(), Id, innerUser.getName()));
											innerUser.setPlayerId(Id, userGame.getLastJoinedRoom());
											log.info(String.format("MoveHandler First user.setPlayerId(Id, userGame.getLastJoinedRoom());",Id,userGame.getLastJoinedRoom()));
										}
									}

								});
								user = innerUser;
								log.info("**************** First user name - "+user.getName()+" and playerId -"+user.getPlayerId());

							}
							// Set game board tile
							board.setTileAt(moveX, moveY, user.getPlayerId() == 1 ? Tile.GREEN : Tile.RED);

							// Send response
							ISFSObject respObj = new SFSObject();
							respObj.putInt("x", moveX);
							respObj.putInt("y", moveY);
							respObj.putInt("t", user.getPlayerId());
							log.info("MoveHandler First move command before CMD_MOVE- "+CMD_MOVE+ " gameExt.getGameRoom().getUserList() - "+gameExt.getGameRoom().getUserList());
							send(CMD_MOVE, respObj, gameExt.getGameRoom().getUserList());

							// Increse move count and check game status
							gameExt.increaseMoveCount();

							// Switch turn
							gameExt.updateTurn();

							// Check if game is over
							checkBoardState(gameExt);
							log.info(
									"MoveHandler First move checkBoardState after gameExt.getGameMap().get(user.getName()) - "
											+ gameExt.getGameMap().get(gameExt.getGameRoom().getName()));
							if (gameExt.getGameMap().get(gameExt.getGameRoom().getName()) != null) {
								log.info("MoveHandler First updating the turn whoseturn value "
										+ gameExt.getGameRoom().getName() + " and  gameExt.getWhoseTurn() - "+gameExt.getWhoseTurn());
								gameExt.getGameMap().put(gameExt.getGameRoom().getName(), gameExt.getWhoseTurn());
							}
						}
					} else
						gameExt.trace(ExtensionLogLevel.WARN, "Wrong turn error. It was expcted: "
								+ gameExt.getWhoseTurn() + ", received from: " + user);
				} else
					gameExt.trace(ExtensionLogLevel.WARN,
							"Wrong turn error. It was expcted: " + gameExt.getWhoseTurn() + ", received from: " + user);
			} else if (gameExt.getGameRoom().getName().equalsIgnoreCase("game2")) {
				TrisGameBoard board = gameExt.getGameBoardSecond();
				gameExt.trace("********************  MoveHandler Second***********************************");
				int moveX = params.getInt("x");
				int moveY = params.getInt("y");

				log.info(String.format(" Handling Second move from player %s. (%s, %s) = %s and Game- %s ",
						user.getPlayerId(), moveX, moveY, board.getTileAt(moveX, moveY),
						gameExt.getGameRoom().getName()));
				if (gameExt.getWhoseTurnSecond() == null) {
					
					if (gameExt.getGameMap().get(gameExt.getGameRoom().getName()) != null) {
						log.info("MoveHandler Second  setting the turn whoseturnSecond is null ");
						gameExt.setWhoseTurnSecond(gameExt.getGameMap().get(gameExt.getGameRoom().getName()));
					}
				}
				if (gameExt.isGameStartedSecond()) {
					
					if (gameExt.getWhoseTurnSecond() == null) {
						log.info(" @#@#@# getWhoseTurnSecond is null and  gameExt.getGameRoom().getName()- "+gameExt.getGameRoom().getName()+ " and gameExt.getGameMap().get(gameExt.getGameRoom().getName()) - "+gameExt.getGameMap().get(gameExt.getGameRoom().getName()));
						gameExt.setWhoseTurnSecond(gameExt.getGameMap().get(gameExt.getGameRoom().getName()));
					}
					if (gameExt.getWhoseTurnSecond() == user) {
						log.info("MoveHandler Second whoseturn matched user and board.getTileAt(moveX, moveY)- "
								+ board.getTileAt(moveX, moveY));
						if (board.getTileAt(moveX, moveY) == Tile.EMPTY) {
						
							if (gameExt.getGameMap().get(gameExt.getGameRoom().getName()) != null) {
								log.info("MoveHandler Second getting the value for playerId -  "
										+ gameExt.getGameRoom().getName());
								User userGame = gameExt.getGameMap().get(gameExt.getGameRoom().getName());
								int playerID = userGame.getPlayerId();
								Map<Room, Integer> localMap = userGame.getPlayerIds();
								log.info("MoveHandler Second Usergame - " + userGame.getName()
										+ " and  user.getPlayerId() - " + user.getPlayerId());
								final User innerUser = user;
								localMap.forEach((Room, Id) -> {
									log.info("MoveHandler Second  Room name- " + Room.getName()
											+ ", K.getUserByName(user.getName()).getPlayerId()-  "
											+ Room.getUserByName(innerUser.getName()).getPlayerId() + ", Player ID- : " + Id + " Room.getName().equalsIgnoreCase(gameExt.getGameRoom().getName())-"+Room.getName().equalsIgnoreCase(gameExt.getGameRoom().getName()));
									if (Room.getName().equalsIgnoreCase(gameExt.getGameRoom().getName())) {
										if (innerUser.getPlayerId() != Id) {
											log.info(String.format(" Second Updating the playerId-%s to id- %s for user- %s",
													innerUser.getPlayerId(), Id, innerUser.getName()));
											innerUser.setPlayerId(Id, Room);
											log.info(String.format("MoveHandler Second **Updating** user.setPlayerId(Id, Room);",Id,Room));
										}
									}

								});
								user = innerUser;
								log.info("**************** Second user name - "+user.getName()+" and playerId -"+user.getPlayerId());

							}
							List<User> userList = gameExt.getParentRoom().getUserList();
							for(User userLocal:userList)
							{
								log.info("SEcond Tile Updating userLocal.getName() - "+userLocal.getName() + " and  user.getName() - "+user.getName());
								if(userLocal.getName().equalsIgnoreCase(user.getName()))
								{
									log.info("**Internal Tile  Updating whoseturn second for user - "+userLocal + " playerId Local-  "+userLocal.getPlayerId() +" and user.getPlayerId() - "+user.getPlayerId());
									if(user.getPlayerId() != userLocal.getPlayerId())
									{
										int id = userLocal.getPlayerId() == 1 ? 2 : 1;
										userLocal.setPlayerId(id, userLocal.getLastJoinedRoom());
										log.info(" Internal Final Tile  update after userLocal.getPlayerId() - "+userLocal.getPlayerId());
										user = userLocal;
									}
								}
							}
							Tile color = user.getPlayerId() == 1 ? Tile.GREEN : Tile.RED;
							log.info("%%%%%%%%%%%%%%%%%  Color is - "+Tile.values());
							// Set game board tile
							board.setTileAt(moveX, moveY, user.getPlayerId() == 1 ? Tile.GREEN : Tile.RED);

							// Send response
							log.info("()()()()(Before) gameExt.getWhoseTurnSecond() - "+gameExt.getWhoseTurnSecond());
							ISFSObject respObj = new SFSObject();
							respObj.putInt("x", moveX);
							respObj.putInt("y", moveY);
							respObj.putInt("t", user.getPlayerId());
							log.info("MoveHandler Second move command before");
							send(CMD_MOVE, respObj, gameExt.getGameRoom().getUserList());

							// Increse move count and check game status
							gameExt.increaseMoveCountSecond();

							// Switch turn
							gameExt.updateTurnSecond(gameExt.getWhoseTurnSecond());
							log.info("()()()()(After) gameExt.getWhoseTurnSecond() - "+gameExt.getWhoseTurnSecond());
							// Check if game is over
							checkBoardStateSecond(gameExt);
							log.info("MoveHandler Second move checkBoardState after gameExt.getGameMap().get(user.getName())- "
											+ gameExt.getGameMap().get(gameExt.getGameRoom().getName()));
							if (gameExt.getGameMap().get(gameExt.getGameRoom().getName()) != null) {
								log.info("MoveHandler Second updating the turn whoseturn value "
										+ gameExt.getGameRoom().getName() + " and  gameExt.getWhoseTurnSecond() - "+gameExt.getWhoseTurnSecond());
								gameExt.getGameMap().put(gameExt.getGameRoom().getName(), gameExt.getWhoseTurnSecond());
							}
						}
					} else
						gameExt.trace(ExtensionLogLevel.WARN, "Wrong Second turn error. It was expcted: "
								+ gameExt.getWhoseTurnSecond() + ", received from: " + user);
				} else
					gameExt.trace(ExtensionLogLevel.WARN, "Wrong Second turn error. It was expcted: "
							+ gameExt.getWhoseTurnSecond() + ", received from: " + user);
			}
		} catch (Exception e) {
			log.error("Exception inside the MoveHandler.class is - " + e.getMessage());
		}

	}

	private void checkBoardState(TrisExtension gameExt) {
		GameState state = gameExt.getGameBoard().getGameStatus(gameExt.getMoveCount());

		if (state == GameState.END_WITH_WINNER) {
			int winnerId = gameExt.getGameBoard().getWinner();

			gameExt.trace("Winner found: ", winnerId);

			// Stop game
			gameExt.stopGame();

			// Send update
			ISFSObject respObj = new SFSObject();
			respObj.putInt("w", winnerId);
			gameExt.send(CMD_WIN, respObj, gameExt.getGameRoom().getUserList());

			// Set the last game ending for spectators joining after the end and before a
			// new game starts
			gameExt.setLastGameEndResponse(new LastGameEndResponse(CMD_WIN, respObj));

			// Next turn will be given to the winning user.
			gameExt.setTurn(gameExt.getGameRoom().getUserByPlayerId(winnerId));
		}

		else if (state == GameState.END_WITH_TIE) {
			gameExt.trace("TIE!");

			// Stop game
			gameExt.stopGame();

			// Send update
			ISFSObject respObj = new SFSObject();
			gameExt.send(CMD_TIE, respObj, gameExt.getGameRoom().getUserList());

			// Set the last game ending for spectators joining after the end and before a
			// new game starts
			gameExt.setLastGameEndResponse(new LastGameEndResponse(CMD_TIE, respObj));
		}
	}

	private void checkBoardStateSecond(TrisExtension gameExt) {
		try {
			GameState state = gameExt.getGameBoardSecond().getGameStatus(gameExt.getMoveCountSecond());
			log.info("$$$$$$$$   Second state - " + state + " and  GameState.END_WITH_WINNER- "
					+ GameState.END_WITH_WINNER);
			if (state == GameState.END_WITH_WINNER) {
				int winnerId = gameExt.getGameBoardSecond().getWinner();

				gameExt.trace("Winner found: ", winnerId);

				// Stop game
				gameExt.stopGameSecond();

				// Send update
				ISFSObject respObj = new SFSObject();
				respObj.putInt("w", winnerId);
				gameExt.send(CMD_WIN, respObj, gameExt.getGameRoom().getUserList());

				// Set the last game ending for spectators joining after the end and before a
				// new game starts
				gameExt.setLastGameEndResponseSecond(new LastGameEndResponseSecond(CMD_WIN, respObj));

				// Next turn will be given to the winning user.
				gameExt.setWhoseTurnSecond(gameExt.getGameRoom().getUserByPlayerId(winnerId));
			}

			else if (state == GameState.END_WITH_TIE) {
				gameExt.trace("TIE!");

				// Stop game
				gameExt.stopGameSecond();

				// Send update
				ISFSObject respObj = new SFSObject();
				gameExt.send(CMD_TIE, respObj, gameExt.getGameRoom().getUserList());

				// Set the last game ending for spectators joining after the end and before a
				// new game starts
				gameExt.setLastGameEndResponseSecond(new LastGameEndResponseSecond(CMD_TIE, respObj));
			}
		} catch (Exception e) {
			log.error("Exception inside the MoveHandler is - " + e.getMessage());
		}
	}
}
