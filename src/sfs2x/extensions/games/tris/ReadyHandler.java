package sfs2x.extensions.games.tris;

import org.apache.log4j.Logger;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class ReadyHandler extends BaseClientRequestHandler {
	static Logger log = Logger.getLogger(ReadyHandler.class.getName());

	@Override
	public void handleClientRequest(User user, ISFSObject params) {
		log.info("@#@#@   ReadyHandler @#@#@#@#");
		TrisExtension gameExt = (TrisExtension) getParentExtension();
		try {
			if (user.isPlayer()) {
				log.info("@#@#@#   ReadyHandler  gameExt.getGameRoom().getSize().getUserCount() = " + gameExt.getGameRoom().getSize().getUserCount()+"  gameExt.getGameMap() - "+gameExt.getGameMap());
				if(gameExt.getGameMap().get(gameExt.getGameRoom().getName()) == null)
				{
					log.info("@#@#@#   ReadyHandler *initializing* the map game-  "+gameExt.getGameRoom().getName());
					gameExt.getGameMap().put(gameExt.getGameRoom().getName(), user);
				}
				// Checks if two players are available and start game
				if (gameExt.getGameRoom().getSize().getUserCount() == 2) {
					
					if (gameExt.getGameRoom().getName().contains("game1")) {
						gameExt.startGame();
					} else {
						gameExt.startGameSecond();
					}
				}
			} else {
				log.info("@#@#@   ReadyHandler isNotPlayer  @#@#@#@#"+gameExt.getGameRoom().getName());
				if (gameExt.getGameRoom().getName().contains("game1")) {
					gameExt.updateSpectator(user);

					LastGameEndResponse endResponse = gameExt.getLastGameEndResponse();

					// If game has ended send the outcome
					if (endResponse != null)
						send(endResponse.getCmd(), endResponse.getParams(), user);
				} else {
					gameExt.updateSpectatorSecond(user);

					LastGameEndResponseSecond endResponse = gameExt.getLastGameEndResponseSecond();

					// If game has ended send the outcome
					if (endResponse != null)
						send(endResponse.getCmd(), endResponse.getParams(), user);
				}
			}
		} catch (Exception e) {
			log.error("Exception inside the ReadyHandler.class is - " + e.getMessage());
			// TODO: handle exception
		}
	}
}
