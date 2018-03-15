import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Class sets up threads, sockets and I/O
 * also gives address and IP that the client connects on
 * @see ClientListening
 * @see ClientListeningFromServer
 */
public class ChatClient implements Runnable {

	private int port;
	private String address;
	private boolean running = true;

	private PrintStream outToServer = null;
	private BufferedReader inputFromServer = null;

	private Socket clientSocket = null;

	/**
	 * sets default values
	 */
	private ChatClient() {
		port = 14001;
		address = "localhost";
	}

	private ChatClient(int port) {
		this.port = port;
		address = "localhost";
	}

	private ChatClient(String address) {
		port = 14001;
		this.address = address;
	}
	private ChatClient(String address, int port){
		this.port = port;
		this.address = address;
	}

	private boolean getRunning() {
		return running;
	}

	private void setRunning(boolean running) {
		this.running = running;
	}

	public void closeConnections() {
		try {
			clientSocket.close();
			setRunning(false);
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Socket set up, to be accepted to server I/O initialised Thread started to
	 * constantly search for messages from the server sends message from client
	 * to server
	 */
	@Override
	public void run() {
		try {
			clientSocket = new Socket(address, port);

			System.out.println("Client created on: " + clientSocket.getLocalPort() + " and: " + clientSocket.getPort());

			outToServer = new PrintStream(clientSocket.getOutputStream());
			inputFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			System.out.println("I/O initialised.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (clientSocket != null && outToServer != null && inputFromServer != null) {

			// starts thread to listen for messages from the server & from keyboard
			Thread clientListening = new Thread(new ClientListening(this, clientSocket, getRunning()));
			clientListening.start();
			Thread clientListeningFromServer = new Thread(new ClientListeningFromServer(this, clientSocket));
			clientListeningFromServer.start();
		}
	}

	/**
	 * main method used to check command line input so user can change port and
	 * address
	 * @param args
	 * 		args here is used to change the port and address
	 */
	public static void main(String[] args) {
		String IP;
		int commandIntPort;

		if(args.length==4){
			if(args[0] != null && args[2] != null){
				if (args[0].startsWith("-cca") && args[2].startsWith("-ccp")) {
					IP = args[1];
					String commandPort = args[3];
					commandIntPort = Integer.parseInt(commandPort);
					System.out.println("Using IP: " + IP + " and port: " + commandIntPort);
					Thread t1 = new Thread(new ChatClient(IP, commandIntPort));
					t1.start();
				}
				else{
					System.out.println("Invalid command line input, running with defaults.");
					Thread t1 = new Thread(new ChatClient());
					t1.start();
				}
			}
		}
		else if(args.length==2){
			if(args[0] != null){
				if (args[0].startsWith("-ccp")) {
					String commandPort = args[1];
					commandIntPort = Integer.parseInt(commandPort);
					System.out.println("Using port: " + commandPort);
					Thread t1 = new Thread(new ChatClient(commandIntPort));
					t1.start();
				} else if (args[0].startsWith("-cca")) {
					IP = args[1];
					System.out.println("Using IP: " + IP);
					Thread t1 = new Thread(new ChatClient(IP));
					t1.start();
				}
				else{
					System.out.println("Invalid command line input, running with defaults");
					Thread t1 = new Thread(new ChatClient());
					t1.start();
				}
			}
		}
		else{
			Thread t1 = new Thread(new ChatClient());
			t1.start();
		}
	}
}