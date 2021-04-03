package sfs2x.extensions.games.tris;

import org.apache.log4j.Logger;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

public class RestartHandler extends BaseClientRequestHandler
{static Logger log = Logger.getLogger(MoveHandler.class.getName()); 
	@Override
	public void handleClientRequest(User user, ISFSObject params)
	{
		TrisExtension gameExt = (TrisExtension) getParentExtension();
		
		// Checks if two players are available and start game
		if (gameExt.getGameRoom().getSize().getUserCount() == 2)
			gameExt.startGame();
	}
}
