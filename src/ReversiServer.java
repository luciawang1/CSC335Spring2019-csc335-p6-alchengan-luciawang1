import java.io.*;
import java.net.*;

public class ReversiServer {
	ServerSocket server;
	Socket clientConnection;
	ObjectOutputStream output;
	ObjectInputStream input;
	
	public ReversiServer(int port) {
		try {
			server = new ServerSocket(port);
			clientConnection = server.accept();
			output = new ObjectOutputStream(clientConnection.getOutputStream());
			input = new ObjectInputStream(clientConnection.getInputStream());
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		System.out.println("connected");
	}
	
	public void sendBoard(ReversiBoard board) throws IOException {
		output.writeObject(board);
	}
	
	public ObjectInputStream getInput() {
		return input;
	}
}
