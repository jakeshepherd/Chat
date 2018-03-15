import java.util.Scanner;

/**
 *  Thread waits for a user to input something into the server
 *  and prints it to the client
 */
public class ServerListen implements Runnable {

	private boolean running = true;

	private Scanner keyboardIn;
	private ChatServer chatServer;

	public ServerListen(ChatServer chatServer) {
		this.chatServer = chatServer;
		keyboardIn = new Scanner(System.in);
	}

	private void setRunning(boolean running){
		this.running = running;
	}

	@Override
	public void run() {
		String message;
		while (running) {
			message = keyboardIn.nextLine();
			if (message.equals("EXIT")) {
				setRunning(false);
				System.out.println("Server exiting...");
				chatServer.closeAllConnections();
			} else {
				chatServer.sendMessageFromServer("SERVER: " + message);
			}
		}
	}
}
