import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * class used to send messages to all clients,
 * when they have been received
 * also can shut down clients
 */
class ServerToClientThread extends Thread {

	private int maxConnections;
	private boolean running = true;

	private BufferedReader inputFromServer;
	private PrintStream outToServer;

	private Socket clientSocket;
	private final ServerToClientThread[] allThreads;

	/**
	 * constructor - assigns variables
	 *
	 * @param clientSocket gives the thread the info of the Socket
	 * @param allThreads   gives the thread the instance of itself
	 */
	public ServerToClientThread(Socket clientSocket, ServerToClientThread[] allThreads) {
		this.clientSocket = clientSocket;
		this.allThreads = allThreads;
		maxConnections = allThreads.length;
	}

	private void setRunning(boolean runningState) {
		running = runningState;
	}

	/*
	 * Method used to close all running connections to server, when "EXIT" is
	 * entered into the server
	 */
	public void closeConnections() {
		sendMessage("EXIT");
		setRunning(false);
		System.out.println("Successfully closed the connections and threads.");
		System.exit(0);
	}

	private void handleInput(String username) {
		try {
			String messageIn = inputFromServer.readLine();
			if (messageIn.equals("shutdown")) {
				System.out.println(username + " has left the chat.");
				for (int i = 0; i < maxConnections; i++) {
					if (allThreads[i] != null) {
						allThreads[i].outToServer.println(username + " has left the chat.");
					}
					break;
				}
				for (int i = 0; i < maxConnections; i++) {
					if (allThreads[i] == this && allThreads[i] != null) {
						allThreads[i].setRunning(false);
					}
				}
			} else {
				System.out.println(messageIn);
				sendMessage(messageIn);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resetThread(){
		for (int i = 0; i < maxConnections; i++) {
			if (allThreads[i] == this) {
				allThreads[i] = null;
			}
		}
	}

	/**
	 * Method sends input from clients, to all available clients
	 *
	 * @param messageIn Gives the method the string message to print out to the clients.
	 */
	private void sendMessage(String messageIn) {

		// all active clients are sent the message from the server
		for (int i = 0; i < maxConnections; i++) {
			if (allThreads[i] != null) {
				allThreads[i].outToServer.println(messageIn);
			}
		}
	}

	/**
	 * block used to make sure the server will only send one message,
	 * if the user inputs into the server.
	 *
	 * @param message Gives the method the string message from server to print out to the clients.
	 */
	public void sendMessageFromServer(String message) {
		outToServer.println(message);
	}

	/**
	 * Used to read for messages from clients and
	 * begin shutdown of clients
	 */
	public void run() {
		int maxConnections = this.maxConnections;
		ServerToClientThread[] allThreads = this.allThreads;

		try {
			inputFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			outToServer = new PrintStream(clientSocket.getOutputStream(), true);

			// user defines username
			outToServer.println("Enter a username: ");
			String username = (inputFromServer.readLine());
			System.out.println(username + " has now entered the chat");

			// lets all active clients know a new user connected
			for (int i = 0; i < maxConnections; i++) {
				if (allThreads[i] != null) {
					allThreads[i].outToServer.println(username + " has now entered the chat");
				}
			}
			outToServer.println("Type a message...");

			while (running) {
				handleInput(username);
			}

			resetThread();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputFromServer.close();
				outToServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}
}

