package project;

public class ErrorSimulator {
	
	Commons common = new Commons("ERROR SIMULATOR");
	int port_address = 23;
	
	private void startListening() {
		// listen on port in a threaded way.
		// on received request kick new thread and handle passing data to server and returning response to server. 
	}
	
	public void main() {
		// get ready to accept 
		ErrorSimulator er = new ErrorSimulator();
		er.startListening();
	}
}
