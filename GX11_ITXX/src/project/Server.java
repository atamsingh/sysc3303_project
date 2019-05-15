package project;

public class Server {
	
	Commons common = new Commons("SERVER");
	int port_address =  69;
	
	private void handleReadRequest() {

	}
	
	private void handleWriteRequest() {

	}
	
	private void handleRequest() {
		// if read
		this.handleReadRequest();
		// else if write
		this.handleWriteRequest();
		// else
		// handle error
	}
	
	public void startListening() {
		// listen to the port needed for requests 
		// create threads to handle these req. received
		this.handleRequest(); // send data along
	}

	public void main() {
		// get ready to accept 
		Server s = new Server();
		s.startListening();
	}
}
