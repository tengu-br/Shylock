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
    private Hashtable cidades;
    private Hashtable afinidade;
    private Integer money = 0;
    private String personalidade;

    protected void setup() {
        catalogo = new Hashtable();
        cidades = new Hashtable();
        afinidade = new Hashtable();
        cidades.put("NN", 0);
        cidades.put("ND", 0);
        cidades.put("CO", 0);
        cidades.put("SD", 0);
        cidades.put("SS", 0);
        afinidade.put("NN", ThreadLocalRandom.current().nextInt(-1, 2));
        afinidade.put("ND", ThreadLocalRandom.current().nextInt(-1, 2));
        afinidade.put("CO", ThreadLocalRandom.current().nextInt(-1, 2));
        afinidade.put("SD", ThreadLocalRandom.current().nextInt(-1, 2));
        afinidade.put("SS", ThreadLocalRandom.current().nextInt(-1, 2));

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
                int result = 5;

                cidades.put("SS", (Integer) cidades.get("SS") + 1);
                cidades.put("SD", (Integer) cidades.get("SD") + 1);
                cidades.put("CO", (Integer) cidades.get("CO") + 1);
                cidades.put("ND", (Integer) cidades.get("ND") + 1);
                cidades.put("NN", (Integer) cidades.get("NN") + 1);
                if (personalidade.equals("cinico")) {

                    int biggest = (Integer) cidades.get("SS");
                    result = 0;
                    if ((Integer) cidades.get("SD") > biggest) {
                        biggest = (Integer) cidades.get("SD");
                        result = 1;
                    }
                    if ((Integer) cidades.get("CO") > biggest) {
                        biggest = (Integer) cidades.get("CO");
                        result = 2;
                    }
                    if ((Integer) cidades.get("ND") > biggest) {
                        biggest = (Integer) cidades.get("ND");
                        result = 3;
                    }
                    if ((Integer) cidades.get("NN") > biggest) {
                        biggest = (Integer) cidades.get("NN");
                        result = 4;
                    }
                } else {
                    int biggest = (((Integer) cidades.get("SS")) + (Integer) afinidade.get("SS"));
                    result = 0;
                    if (((Integer) cidades.get("SD") + (Integer) afinidade.get("SD")) > biggest) {
                        biggest = (Integer) cidades.get("SD");
                        result = 1;
                    }
                    if (((Integer) cidades.get("CO") + (Integer) afinidade.get("CO")) > biggest) {
                        biggest = (Integer) cidades.get("CO");
                        result = 2;
                    }
                    if (((Integer) cidades.get("ND") + (Integer) afinidade.get("ND")) > biggest) {
                        biggest = (Integer) cidades.get("ND");
                        result = 3;
                    }
                    if (((Integer) cidades.get("NN") + (Integer) afinidade.get("NN")) > biggest) {
                        biggest = (Integer) cidades.get("NN");
                        result = 4;
                    }
                }
                System.out.println(result);
                switch (result) {
                    case 0:
                        regiao = "SS";
                        cidades.put("SS", 0);
                        System.out.println("Vendedor " + myAgent.getLocalName() + " se mudou para a regiao Sul (ss)");
                        break;
                    case 1:
                        regiao = "SD";
                        cidades.put("SD", 0);
                        System.out.println("Vendedor " + myAgent.getLocalName() + " se mudou para a regiao Sudeste (sd)");
                        break;
                    case 2:
                        regiao = "CO";
                        cidades.put("CO", 0);
                        System.out.println("Vendedor " + myAgent.getLocalName() + " se mudou para a regiao Centro-Oeste (co)");
                        break;
                    case 3:
                        regiao = "ND";
                        cidades.put("ND", 0);
                        System.out.println("Vendedor " + myAgent.getLocalName() + " se mudou para a regiao Nordeste (nd)");
                        break;
                    case 4:
                        regiao = "NN";
                        cidades.put("NN", 0);
                        System.out.println("Vendedor " + myAgent.getLocalName() + " se mudou para a regiao Norte (nn)");
                        break;
                    default:
                        break;
                }

            }
        });

        Object[] args = getArguments();
        System.out.println("You speak an infinite deal of nothing. - " + getAID().getName() + " .");
        if (args != null && args.length > 0) {
            personalidade = (String) args[0];
        } else {
            System.out.println("Nao sei minha personalidade!");
            takeDown();
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
