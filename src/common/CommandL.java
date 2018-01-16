package common;

public final class CommandL extends DataBlock {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5115104402453532818L;

	public final Command command;

	public final Object object;

	public static enum Command {
		OK, RequestResend, TotalFailureReset, SendNewFileHeader, FileData, KeepAlive, LoginData, RequestFile, FullList, SendFile, DeleteFile
	}

	public CommandL(Command c, byte[] content, Object object) {
		super(content);
		command = c;
		this.object = object;
		switch (command) {
		case OK: {
			break;
		}
		case RequestResend: {
			break;
		}
		case TotalFailureReset: {// content: reason
			break;
		}
		case SendNewFileHeader: {// object[long]: size
			break;
		}
		case FileData: {// content: file content
			break;
		}
		case KeepAlive: {
			break;
		}
		case LoginData: {// content: username:1 password:2
			break;
		}
		case RequestFile: {// content: file path
			break;
		}
		case FullList: { //content: null, object: null :1 ; content: null, object[int]: count :2 ; content: file path, object[long]: file size :3
			break;
		}
		default:
			break;
		}
	}
}