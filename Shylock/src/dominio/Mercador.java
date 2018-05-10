package dominio;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.GroupLayout;
import javax.swing.JOptionPane;

/**
 *
 * @author tengu
 */
public class Mercador extends Agent {

    private String regiao;
    private Hashtable catalogo;
    private Integer money = 0;

    protected void setup() {
        catalogo = new Hashtable();

        addBehaviour(new TickerBehaviour(this, 6000) {
            protected void onTick() {
                switch (ThreadLocalRandom.current().nextInt(0, 5)) {
                    case 0:
                        updateCatalogue("comida", ThreadLocalRandom.current().nextInt(1, 3));
                        break;
                    case 1:
                        updateCatalogue("roupa", ThreadLocalRandom.current().nextInt(2, 5));
                        break;
                    case 2:
                        updateCatalogue("entreterimento", ThreadLocalRandom.current().nextInt(1, 6));
                        break;
                    case 3:
                        updateCatalogue("eletronicos", ThreadLocalRandom.current().nextInt(5, 7));
                        break;
                    case 4:
                        updateCatalogue("decoracao", ThreadLocalRandom.current().nextInt(1, 4));
                        break;
                    default:
                        break;
                }

            }
        });
        
        addBehaviour(new TickerBehaviour(this, 30000) {
            protected void onTick() {
                switch (ThreadLocalRandom.current().nextInt(0, 5)) {
                    case 0:
                        regiao = "SS";
                        System.out.println("Vendedor "+myAgent.getLocalName()+ " se mudou para a regiao Sul (ss)");
                        break;
                    case 1:
                        regiao = "SD";
                        System.out.println("Vendedor "+myAgent.getLocalName()+ " se mudou para a regiao Sudeste (sd)");
                        break;
                    case 2:
                        regiao = "CO";
                        System.out.println("Vendedor "+myAgent.getLocalName()+ " se mudou para a regiao Centro-Oeste (co)");
                        break;
                    case 3:
                        regiao = "ND";
                        System.out.println("Vendedor "+myAgent.getLocalName()+ " se mudou para a regiao Nordeste (nd)");
                        break;
                    case 4:
                        regiao = "NN";
                        System.out.println("Vendedor "+myAgent.getLocalName()+ " se mudou para a regiao Norte (nn)");
                        break;
                    default:
                        break;
                }

            }
        });

        Object[] args = getArguments();
        System.out.println("You speak an infinite deal of nothing. - " + getAID().getName() + " .");
        if (args != null && args.length > 0) {
            regiao = (String) args[0];
            System.out.println("I will go to  " + regiao + " .");

        } else {
            System.out.println("I won't go anywhere!");
        }

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("book-selling");
        sd.setName("JADE-book-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new OfferRequestsServer());
        addBehaviour(new PurchaseOrdersServer());

    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("All that glisters is not gold. - " + getAID().getName() + " .");
    }

    public void updateCatalogue(final String item, final int preco) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                catalogo.put(item, new Integer(preco));
            }
        });
    }

//    private void addProduto() {
//        String produto = JOptionPane.showInputDialog("Nome do produto: ");
//        Integer preco = Integer.parseInt(JOptionPane.showInputDialog("Preço do produto: "));
//        this.updateCatalogue(produto, preco);
//        if (JOptionPane.showConfirmDialog(null, "Adicionar outro produto?") == JOptionPane.YES_OPTION) {
//            addProduto();
//        }
//    }

    private class OfferRequestsServer extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                String title = msg.getContent().substring(2);
                String regiaoB = msg.getContent().substring(0, 2);
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogo.get(title);
                if ((price != null) && (regiaoB.equals(regiao))) {
                    // The requested book is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                } else {
                    // The requested book is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer

    private class PurchaseOrdersServer extends CyclicBehaviour {

        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String title = msg.getContent().substring(2);
                String regiaoB = msg.getContent().substring(0, 2);
                ACLMessage reply = msg.createReply();

                Integer price = (Integer) catalogo.remove(title);
                if ((price != null) && (regiaoB.equals(regiao))) {
                    money += price;
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(title + " vendido(s) para " + msg.getSender().getLocalName());
                } else {
                    // The requested book has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }    
}
