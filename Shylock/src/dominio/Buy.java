package dominio;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author tengu
 */
public class Buy extends Behaviour {

    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            // Message received. Process it
            String mensagem = msg.getContent();
            ACLMessage reply = msg.createReply();
            myAgent.send(reply);
        } else {
            block();
        }

    }

    public boolean done() {
        return true;
    }
}
