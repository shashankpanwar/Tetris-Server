package sfs2x.extensions.games.tris;

import java.util.Map;

import org.apache.log4j.Logger;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

public class OnUserGoneHandler extends BaseServerEventHandler
{
	static Logger log = Logger.getLogger(OnUserGoneHandler.class.getName());
	@SuppressWarnings("unchecked")
    @Override
	public void handleServerEvent(ISFSEvent event) throws SFSException
	{
		trace("@#@#@@# ISFSEvent - "+event.getType());
		User user = (User) event.getParameter(SFSEventParam.USER);
		trace("@#@#@@# ISFSEvent - "+user.getName());
		try
		{
		TrisExtension gameExt = (TrisExtension) getParentExtension();
		Room gameRoom = gameExt.getGameRoom();
		log.info("Before Removing the gameMap  UserGoneHandler for gameName - "+gameRoom.getName());
		// Get event params
		
		Integer oldPlayerId;
		
		if(gameExt.getGameMap().get(gameRoom.getName()) != null)
		{
			log.info("&&&& Removing the gameMap  UserGoneHandler for user - "+gameRoom.getName());
			gameExt.getGameMap().remove(gameRoom.getName());
		}
		
		// User disconnected
		if (event.getType() == SFSEventType.USER_DISCONNECT)
		{
			Map<Room, Integer> playerIdsByRoom = (Map<Room, Integer>) event.getParameter(SFSEventParam.PLAYER_IDS_BY_ROOM);
			oldPlayerId = playerIdsByRoom.get(gameRoom);
		}
		else
		{
			oldPlayerId = (Integer) event.getParameter(SFSEventParam.PLAYER_ID);
		}
		
		// Old user was in this Room
		if (oldPlayerId != null)
		{
			// And it was a player
			if (oldPlayerId > 0)
			{
				gameExt.stopGame(true);
				
				// If 1 player is inside let's notify him that the game is now stopped
				if (gameRoom.getSize().getUserCount() > 0)
				{
					ISFSObject resObj = new SFSObject();
					resObj.putUtfString("n", user.getName());
					
					gameExt.send("stop", resObj, gameRoom.getUserList());
				}
			}
		}
		}
		catch (Exception e) {
			log.error("Exception inside the OnUserGoneHandler.class is -"+e.getMessage());
		}
	}
}
