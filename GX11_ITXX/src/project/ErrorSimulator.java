package project;

public class ErrorSimulator implements Runnable {
	
	Commons common = new Commons("ERROR SIMULATOR");
	int port_address = 23;
	long delayvalue;

	private void startListening() {

		// listen on port in a threaded way.
		// on received request kick new thread and handle passing data to server and returning response to server. 
	}
	//socket recivie packet
	//socket get packet

	//delay by 1000ms 
	public void main() {
		// get ready to accept 
		ErrorSimulator er = new ErrorSimulator();
		er.startListening();
		//create new thread
		Thread t1 = new Thread ( new ErrorSimulator());
		t1.start();
		Thread t2 = new THread (new ErrorSimulator());
		t2.start();

		}

	//Delay 
	Public void delayPacket(long delay){
		// first thread handles next packet
		//second thread handles delayedpacket
		t1.sleep(100);



	}

	public void duplicatePacket(){
		t1.sleep(1500);//delays by 1500ms causing server to resend packet


	}


	public void losePacket(){
		//first thread handles next packet after selected packet is lost
		//second packet handles lost packet
		//t1.sleep():


	}

}
