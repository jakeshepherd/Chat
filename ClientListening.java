import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Class acts as a thread to wait for a keyboard input
 * and send it to the server
 */
public class ClientListening implements Runnable {

	private boolean running = true;

	private Scanner keyboardIn = null;
	private PrintStream outToServer;

	private ChatClient chatClient;

	/**
	 * @param chatClient
	 * 		give this thread an instance of the ChatClient class
	 * @param clientSocket
	 * 		gives the thread the Socket information
	 * @param running
	 * 		useful when trying to close the threads
	 */
	public ClientListening(ChatClient chatClient, Socket clientSocket, boolean running) {
		try {
			this.running = running;
			this.chatClient = chatClient;
			keyboardIn = new Scanner(System.in);
			outToServer = new PrintStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setRunning(boolean running) {
		this.running = running;
	}

	/*
	 * sends messages to server from client,
	 * if //shutdown is entered, client will begin shutdown.
	 */
	@Override
	public void run() {
		String username = (keyboardIn.nextLine());
		outToServer.println(username);
		System.out.println("To exit the chat room at any time, enter: //shutdown");

		while (running) {
			String message = keyboardIn.nextLine();

			// check if there should be a shutdown message
			if (message.equals("//shutdown")) {
				System.out.println("Shutting down client: " + username);
				outToServer.println("shutdown");
				setRunning(false);
				outToServer.println(username + ": is leaving the chat.");
				chatClient.closeConnections();
			}
			outToServer.println(username + ": " + message);
		}
		System.out.println("Server has closed.");
	}
}