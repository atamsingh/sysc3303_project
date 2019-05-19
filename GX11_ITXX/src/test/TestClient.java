package test;

import java.net.UnknownHostException;
import java.util.function.Function;

public class TestClient {
	DummyInputLoader inputLoader = null;
	project.Client client = null;
	
	Function<Void, Boolean> test_creation = (Void v) -> {return client != null;};

	public void handleTest(String func_name, Function<Void, Boolean> func) throws Exception {
		System.out.println("running test `TestClient`.`" + func_name + "`...");
		Boolean test_return = func.apply(null);
		if(!test_return) {
			throw new Exception();
		}
	}

	public void run() throws Exception {
		inputLoader = new DummyInputLoader();
		client = new project.Client(inputLoader);
		
		// call tests
		handleTest("test_creation", test_creation);
	}
}

class DummyInputLoader extends project.ClientInputLoader {

	public DummyInputLoader() throws UnknownHostException {
		super();
	}
	
}