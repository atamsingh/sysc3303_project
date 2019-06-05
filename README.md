# SYSC 3303 Project Group 11
- This is actually an esclipse project. To use this project easily, choose the GX11_ITXX folder as Eclipse workspace.
- The program actually lives in the project folder and the tests for all of those classes exists in test folder.
- Some sample functions have been created in the program classe. Example for client test is also created. please follow similar programming styling
- Any funciton you feel will be useful for more than 1 class, please create it in the Commons class so we can share this funcitonlity. Some funcitons live there now. Feel free to add more or remove some. e,g, I can see the confirming acknowledge function being something common. 
- Please make a branch for yourself on the repo. and PRs for all of us to look at the work progress. Commit often offcourse. Would hate to loose work that's been done. 
- For the project folder name, I choose ITXX since this can be our working model over different iterations. 

-----------------------------------COPY BELOW FOR README.TXT------------------------------------------
For set up make sure all files below in the eclipse project: 
Client.java -- client side
ClientInputLoader.java -- handles client inputs
ClientConnection.java -- connection thread that handles communication with client
Commons.java -- class contains methods shared by all classes
ErrorSimulator.java -- simulates errors of user's choice
RequestListener.java -- handles shutdown of connection threads when server shuts down
Server.java -- server side, also spawns connection threads

RUNNING INSTRUCTIONS
-CLIENT 
Hit run
type local
type 23 to simulate errors or 69 for normal operation
type 'verbose' for verbose mode and 'quiet' for quiet mode
enter read or write for connection type
enter filepath of directory to store or grab the text file on the server side
enter filename to write to or read from on the server side
enter filepath of directory to store or grab the text file on the client side
enter filename to write to or read from on the client side

-Error Simulation
Hit run
follow instructions given by program

-Server 
Hit run
follow instructions given by program



## Responsibilities for all iterations
### Alden
* Server -read
### Nnamdi 
* Server - write 
* Error Simulator
### Daniyal
* Diagrams
* Error Simulator 
### Joey 
* Error Simulator
### Atam 
* Client
