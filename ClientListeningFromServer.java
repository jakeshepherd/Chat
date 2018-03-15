import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * Listens for messages from server and prints it to the client
 */
public class ClientListeningFromServer implements Runnable {

	private boolean running = true;

	private BufferedReader inFromServer = null;

	private ChatClient chatClient;

	/**
	 * @param chatClient
	 * 		gives thread an instance of ChatClient
	 * @param clientSocket
	 * 		gives the thread the Socket info
	 */
	ClientListeningFromServer(ChatClient chatClient, Socket clientSocket) {
		this.chatClient = chatClient;
		try {
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setRunning(boolean running){
		this.running = running;
	}

	/*
	 * block searches for input from server,
	 * if the message is exit (from server), then clients will shutdown
	 * if only client has been shutdown, message received will be null,
	 * and so the client is shutdown
	 */
	@Override
	public void run() {
		try {
			System.out.print(inFromServer.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (running) {
			//String message = "";
			try {
				String message = inFromServer.readLine();

				if(message == null){
					System.exit(0);
				}
				if (message.equals("EXIT")) {
					setRunning(false);
					System.out.println("\nServer is now closing. Goodbye.");
					chatClient.closeConnections();
				}
				else{
					System.out.println(message);
				}
			} catch (SocketException e){
				System.out.println("Connection to the server has been lost.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}