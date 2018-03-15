import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Accepts a client connection,
 * shuts connections down if it is greater than 10
 */

public class ChatServer implements Runnable {

	private final int maxConnections = 5;
	private int port;
	private int currentConnections = 0;
	private boolean running = true;

	private ServerSocket serverSocket = null;
	private Socket clientSocket = null;

	private final ServerToClientThread[] allThreads = new ServerToClientThread[maxConnections];
	private Thread serverListen;


	private ChatServer() {
		port = 14001;
		serverListen = new Thread(new ServerListen(this));
		serverListen.start();
	}

	private ChatServer(int port) {
		this.port = port;
		serverListen = new Thread(new ServerListen(this));
		serverListen.start();
	}

	private void setRunning(boolean runningState) {
		running = runningState;
	}

	private void checkMaxUsers(int currentConnections){
		this.currentConnections = currentConnections;

		// limits total users
		try {
			if (currentConnections == maxConnections || currentConnections>maxConnections) {
				PrintStream outServer = new PrintStream(clientSocket.getOutputStream());
				System.out.println("Server did not accept client on: " + serverSocket.getLocalPort() + " and: " + clientSocket.getPort());
				outServer.println("Server is too busy for new users.");
				outServer.close();
				clientSocket.close();
			}
			else{
				System.out.println("Server accepted client on: " + serverSocket.getLocalPort() + " and: " + clientSocket.getPort());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeAllConnections() {
		try {
			setRunning(false);
			for (int i = 0; i < maxConnections; i++) {
				if (allThreads[i] != null) {
					allThreads[i].closeConnections();
				}
			}
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMessageFromServer(String message){
		for (int i = 0; i < maxConnections; i++) {
			if (allThreads[i] != null) {
				allThreads[i].sendMessageFromServer(message);
			}
		}
	}

	@Override
	public void run() {
		System.out.println("Server listening...");
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("The server may already be in use. Shutting down." );
			System.exit(1);
		}
		while (running) {
			try {
				clientSocket = serverSocket.accept();

				// starts a thread on an inactive connection
				for (currentConnections = 0; currentConnections < maxConnections; currentConnections++) {
					if (allThreads[currentConnections] == null) {
						(allThreads[currentConnections] = new ServerToClientThread(clientSocket, allThreads)).start();
						System.out.println("Current connections: " + (currentConnections+1));
						break;
					}
				}
				checkMaxUsers(currentConnections);
			}catch (IOException e) {
				System.exit(0);
			}
		}
	}

	public static void main(String[] args) {

		// checks for command line argument specifying port number
		if (args.length > 0) {
			if (args[0].startsWith("-csp")) {
				String commandPort = args[1];
				int commandIntPort = Integer.parseInt(commandPort);
				new ChatServer(commandIntPort).run();
			}
			else{
				System.out.println("Invalid command line input, running with defaults.");
				new ChatServer().run();
			}
		} else {
			new ChatServer().run();
		}
	}
}
