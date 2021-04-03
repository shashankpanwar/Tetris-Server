/**
 * 
 */
package sfs2x.extensions.games.tris;

import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSErrorCode;
import com.smartfoxserver.v2.exceptions.SFSErrorData;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;


/**
 * @author Shashank Panwar
 *
 */
public class SignInEventHandler extends BaseServerEventHandler {
	@Override
	public void handleServerEvent(ISFSEvent arg0) throws SFSException {
		trace("!@#!@#!@# SignInEventHandler - " + arg0.getType());
		try {
			String name = (String) arg0.getParameter(SFSEventParam.LOGIN_NAME);
			String password = (String) arg0.getParameter(SFSEventParam.LOGIN_PASSWORD);
			User user = (User) arg0.getParameter(SFSEventParam.USER);
			ISession session = (ISession) arg0.getParameter(SFSEventParam.SESSION);
			trace("!@#!@#!@# SignInEventHandler session - " + session);
			try {
				int userID = (int) session.getProperty(TrisExtension.DATABASE_ID);
				trace("###### SignInEventHandler userID - "+userID);
				if (userID <= 0) {
					trace("######  SignInEventHandler userID is not found");
					SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
					errData.addParameter(name);
					throw new SFSLoginException("Bad user name: " + name, errData);
				}
			} catch (Exception e) {
				trace("Exception inside SignInEventHandler is -" + e.getMessage());
				SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
				throw new SFSLoginException("Exception inside SignInEventHandler: ", errData);
			}

		} catch (Exception e) {
			trace("Exception inside SignInEventHandler is -" + e.getMessage());
			SFSErrorData errData = new SFSErrorData(SFSErrorCode.LOGIN_BAD_USERNAME);
			throw new SFSLoginException("Exception inside SignInEventHandler: ", errData);
		}

	}

}
