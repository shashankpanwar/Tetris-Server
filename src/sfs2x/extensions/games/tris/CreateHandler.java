/**
 * 
 */
package sfs2x.extensions.games.tris;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.smartfoxserver.v2.api.CreateRoomSettings;
import com.smartfoxserver.v2.api.CreateRoomSettings.RoomExtensionSettings;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.exceptions.SFSCreateRoomException;
import com.smartfoxserver.v2.exceptions.SFSJoinRoomException;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;

/**
 * @author Shashank Panwar
 *
 */
public class CreateHandler extends BaseClientRequestHandler {
	static Logger log = Logger.getLogger(CreateHandler.class.getName());
	@Override
	public void handleClientRequest(User arg0, ISFSObject isfsObj) {
		log.info("*************  CreateHandler ******************");
		CreateRoomSettings crs = new CreateRoomSettings();
		TrisExtension gameExt = (TrisExtension) getParentExtension();
		try
		{
			log.info("@#@#@@# CreateHandler ::: GameName - "+isfsObj.getUtfString("GameName"));
			log.info("@#@#@@# CreateHandler ::: GroupId - "+isfsObj.getUtfString("GroupId") + " and  ExtensionId - "+isfsObj.getUtfString("ExtensionId")+" and ExtensionClass - "+isfsObj.getUtfString("ExtensionClass"));
			log.info("@#@#@@# CreateHandler  Spectators"+isfsObj.getInt("Spectators") );
			RoomExtensionSettings res = new CreateRoomSettings.RoomExtensionSettings(isfsObj.getUtfString("ExtensionId"), isfsObj.getUtfString("ExtensionClass"));
			crs.setName(isfsObj.getUtfString("GameName"));
			crs.setGame(true);
			crs.setMaxUsers(2);
			crs.setMaxSpectators(isfsObj.getInt("Spectators"));
			crs.setGroupId(isfsObj.getUtfString("GroupId"));
			crs.setExtension(res);
			crs.setDynamic(true);
			Room gameRoom = getApi().createRoom(gameExt.getParentZone(), crs, null);
			TrisExtension.getRoomList().add(gameRoom);
			gameExt.getApi().joinRoom(arg0, gameRoom);
		}
		catch (SFSCreateRoomException | SFSJoinRoomException ex )
		{
			log.error("Exception inside the CreateHandler.class is  - "+ex.getMessage());
		}
		log.info("Size - "+TrisExtension.getRoomList().size());
	}

}
