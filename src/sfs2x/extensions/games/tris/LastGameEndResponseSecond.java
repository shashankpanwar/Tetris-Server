package sfs2x.extensions.games.tris;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class LastGameEndResponseSecond
{
	private final ISFSObject params;
	private final String cmd;
	
	public LastGameEndResponseSecond(String cmd, ISFSObject params)
    {
	    this.params = params;
	    this.cmd = cmd;
    }
	
	public String getCmd()
    {
	    return cmd;
    }
	
	public ISFSObject getParams()
    {
	    return params;
    }
}
