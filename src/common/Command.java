package common;


public class Command {
/*	private final NetworkFunctionality s;

	public static enum Co {
		OK, RequestResend, TotalFailureReset, SendNewFileHeader, FileData, KeepAlive, LoginData, RequestFile, FullList
	}

	public static byte etoi(Co a) {
		switch (a) {
		case OK:
			return 0;
		case RequestResend:
			return 1;
		case TotalFailureReset:
			return 2;
		case SendNewFileHeader:
			return 3;
		case FileData:
			return 4;
		case KeepAlive:
			return 5;
		case LoginData:
			return 6;
		case RequestFile:
			return 7;
		case FullList:
			return 8;
		default:
			return -1;
		}
	}

	public Command(NetworkFunctionality s) {
		this.s = s;
	}

	public static Command TotalFailureReset(byte[] reason) {
		Command c = new Command();
		c.fullList.add(new byte[] { etoi(Co.TotalFailureReset) });
		c.fullList.add(reason);
		return c;
	}

	public static Command SendNewFileHeader(long size) {
		Command c = new Command();
		c.fullList.add(new byte[] { etoi(Co.SendNewFileHeader) });
		c.fullList.add(size);
		return c;
	}

	public void sendOk() throws Exception {
		byte[] encd = s.enc.doFinal(new byte[] { etoi(Co.OK) });
	}

	public static Command RequestResend() {
		return RequestResend;
	}

	public static Command KeepAlive() {
		return KeepAlive;
	}

	public static Command FullList() {
		return FullList;
	}
}
*/
}