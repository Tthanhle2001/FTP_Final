
package features.handlers;

import app.ProcessHandle.ClientThread;


public abstract class HandleBase {

//    public abstract void handleRequest(ObjectRequest message);
//    public abstract void handleRequest(String message);
    public abstract void handleRequest(String[] data);

}
