package test;
public class TestClient {
	
	project.Client client = new project.Client();
	
	private boolean test_creation() {
		return true;
	}
	
	public void run() throws Exception {
		// call tests
		if(!this.test_creation()) {
			throw new Exception();
		}
	}
}
