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
public class onPrintEvent extends BaseServerEventHandler {

	static Logger log = Logger.getLogger(onPrintEvent.class.getName());
	@Override
	public void handleServerEvent(ISFSEvent arg0) throws SFSException {
		log.info("@#@#@@# ISFSEvent - "+arg0.getType());
		User user = (User) arg0.getParameter(SFSEventParam.USER);
		trace("@#@#@@# ISFSEvent - "+user.getName());
		
	}

}
