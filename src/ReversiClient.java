import java.io.*;
import java.net.*;

public class ReversiClient {
	Socket serverConnection;
	ObjectOutputStream output;
	ObjectInputStream input;
	
	public ReversiClient(int port) {
		try {
			serverConnection = new Socket("localhost", port);
			output = new ObjectOutputStream(serverConnection.getOutputStream());
			input = new ObjectInputStream(serverConnection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendBoard(ReversiBoard board) throws IOException {
		output.writeObject(board);
	}
	
	public ObjectInputStream getInput() {
		return input;
	}
}
