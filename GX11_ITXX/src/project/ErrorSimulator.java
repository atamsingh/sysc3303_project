package project;

public class ErrorSimulator implements Runnable {
	
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
		Thread t1 = new Thread ( new ErrorSimulator);
		t1.start();
	}

	
	Public void delayPacket(){
		// first thread handles next packet
		//second thread handles delayedpacket


	}

	public void duplicatePacket(){

	}


	public void losePacket(){
		//first thread handles next packet after selected packet is lost
		//second packet handles lost packet

	}

}
