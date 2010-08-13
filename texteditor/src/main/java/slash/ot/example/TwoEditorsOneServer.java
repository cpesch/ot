package slash.ot.example;

import slash.ot.client.JsonStub;
import slash.ot.server.JsonSkeleton;
import slash.ot.server.Server;
import slash.ot.server.ServerImpl;

import javax.swing.*;

public class TwoEditorsOneServer {

    public static void main(String[] args) {
        Server server = new ServerImpl();
        final JsonStub jsonStub = new JsonStub(new JsonSkeleton(server));

        for (int i = 0; i < 2; i++) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    TextEditor textEditor = new TextEditor();
                    textEditor.setServer(jsonStub);
                    textEditor.show();
                }
            });
        }
    }
}
