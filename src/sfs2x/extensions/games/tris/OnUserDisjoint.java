/**
 * 
 */
package sfs2x.extensions.games.tris;

import org.apache.log4j.Logger;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;

/**
 * @author Shashank Panwar
 *
 */
public class OnUserDisjoint extends BaseServerEventHandler {
	static Logger log = Logger.getLogger(OnUserGoneHandler.class.getName());

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		trace("@#@#@@# OnUserDisjoint ISFSEvent - " + event.getType());
		User user = (User) event.getParameter(SFSEventParam.USER);
		trace("@#@#@@# OnUserDisjoint ISFSEvent - " + user.getName());
		try {
			TrisExtension gameExt = (TrisExtension) getParentExtension();

			if (TrisExtension.getUserAssignedID().get(gameExt.getGameRoom().getName() + user.getName()) != null) {
				TrisExtension.getUserAssignedID().remove(gameExt.getGameRoom().getName() + user.getName());
			}
		} catch (Exception e) {
			log.error("Exception inside the OnUserDisjoint is - " + e.getMessage());
		}
	}

}
