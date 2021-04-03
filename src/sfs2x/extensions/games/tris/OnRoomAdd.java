/**
 * 
 */
package sfs2x.extensions.games.tris;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

/**
 * @author Shashank Panwar
 *
 */
public class OnRoomAdd  extends  BaseServerEventHandler{

	@Override
	public void handleServerEvent(ISFSEvent arg0) throws SFSException {
		trace("@#@#@@# ISFSEvent - "+arg0.getType());
		
	}

}
