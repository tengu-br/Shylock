package dominio;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author tengu
 */
public class Comprador extends Agent {

    private String targetRegiao;
    private AID[] mercadores;
    private static final ArrayList<String> wishlist = new ArrayList<>();

    protected void setup() {
        Object[] args = getArguments();
        System.out.println("Can I pay in Bitcoin? - " + getAID().getName() + " .");
        if (args != null && args.length > 0) {
            targetRegiao = (String) args[0];
            addBehaviour(new TickerBehaviour(this, 6000) {
                protected void onTick() {
                    System.out.println("Tentando comprar os itens desejados em " + targetRegiao);
                    // Update the list of seller agents
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("book-selling");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
//                        System.out.println("Foram encontrados os seguintes vendedores:");
                        mercadores = new AID[result.length];
                        for (int i = 0; i < result.length; ++i) {
                            mercadores[i] = result[i].getName();
                            System.out.println(mercadores[i].getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }

                    // Perform the request
                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
            
            addBehaviour(new TickerBehaviour(this, 6000) {
                protected void onTick() {
                    switch (ThreadLocalRandom.current().nextInt(0, 5)) {
                    case 0:
                        wishlist.add("comida");
                        break;
                    case 1:
                        wishlist.add("roupa");
                        break;
                    case 2:
                        wishlist.add("entreterimento");
                        break;
                    case 3:
                        wishlist.add("eletronicos");
                        break;
                    case 4:
                        wishlist.add("decoracao");
                        break;
                    default:
                        break;
                }
                }
            });
        } else {
            System.out.println("I have no money!");
            doDelete();
        }

    }

    protected void takeDown() {
        System.out.println("I lost my wallet password! - " + getAID().getName() + " .");
    }

    private class RequestPerformer extends Behaviour {
        
        private AID bestSeller; // The agent who provides the best offer 
        private int bestPrice;  // The best offered price
        private int repliesCnt = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < mercadores.length; ++i) {
                        cfp.addReceiver(mercadores[i]);
                    }
                    cfp.setContent(targetRegiao + "comida");
                    cfp.setConversationId("busca-regiao");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("busca-regiao"),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    // Receive all proposals/refusals from seller agents
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Reply received
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            // This is an offer 
                            int price = Integer.parseInt(reply.getContent());
                            if (bestSeller == null || price < bestPrice) {
                                // This is the best offer at present
                                bestPrice = price;
                                bestSeller = reply.getSender();
                            }
                        }
                        repliesCnt++;
                        if (repliesCnt >= mercadores.length) {
                            // We received all replies
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    // Send the purchase order to the seller that provided the best offer
                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestSeller);
                    order.setContent(targetRegiao + "comida");
                    order.setConversationId("busca-regiao");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("busca-regiao"),
                            MessageTemplate.MatchInReplyTo(order.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    // Receive the purchase order reply
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        // Purchase order reply received
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            // Purchase successful. We can terminate
                            System.out.println("Encontrei o vendedor" + reply.getSender().getName() + " na regiao" + targetRegiao + " .");
                            System.out.println("Price = " + bestPrice);
//                            myAgent.doDelete();
                        } else {
                            System.out.println("Ah n.");
                        }

                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            if (step == 2 && bestSeller == null) {
                System.out.println("Compra falhou: sem mercadores na regiao " + targetRegiao + " .");
            }
            return ((step == 2 && bestSeller == null) || step == 4);
        }
    }
}
