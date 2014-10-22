

package com.mxh.ftp.server.command;

public class CmdMap {
	protected Class<? extends FtpCmd> cmdClass;
	String name;
	int type=0;
	public static final int 	REQUIRE_READ_TYPE=1;
	public static final int     REQUIRE_WRITE_TYPE=2;
	public static final int     REQUIRE_MODIFY_TYPE=3;
	public static final int     REQUIRE_DELETE_TYPE=4;
	
	
	public CmdMap(String name, Class<? extends FtpCmd> cmdClass,int type) {
		super();
		this.name = name;
		this.cmdClass = cmdClass;
		this.type=type;
	}

	public Class<? extends FtpCmd> getCommand() {
		return cmdClass;
	}

	public void setCommand(Class<? extends FtpCmd> cmdClass) {
		this.cmdClass = cmdClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(int type){
		this.type=type;
	}
	
	public int getType(){
		return type;
	}
}
