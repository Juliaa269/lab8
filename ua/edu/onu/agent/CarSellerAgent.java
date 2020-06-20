package ua.edu.onu.agent;

import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ua.edu.onu.util.ConsoleColors;

import java.util.LinkedHashMap;
import java.util.Map;

public class CarSellerAgent extends CarMarketAgent {
    private Map<String, Integer> catalogue = new LinkedHashMap<>();
    private CarSellerGui gui;

    protected void setup() {
        this.color = ConsoleColors.GREEN;
        DFAgentDescription dfd = createDescription();

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the behaviour serving queries from buyer agents
        addBehaviour(new OfferRequestsServer());
        // Add the behaviour serving purchase orders from buyer agents
        addBehaviour(new PurchaseOrdersServer());

        gui = new CarSellerGui(this);
        gui.setVisible(true);
    }

    private DFAgentDescription createDescription() {
        // Register the car-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("car-selling");
        sd.setName("JADE-car-trading");
        dfd.addServices(sd);
        return dfd;
    }

    // Put agent clean-up operations here
    protected void takeDown() {
        // Deregister from the yellow pages

        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Close the GUI
        // Printout a dismissal message
        log("Seller-agent " + getAID().getName() + " terminating.");
    }

    public void updateCatalogue(String mileage, int price) {
        catalogue.put(mileage, price);
        gui.render(mapCatalog());
    }

    private String[][] mapCatalog() {
        String[][] data = new String[catalogue.size()][];
        int i = 0;
        for (Map.Entry entry : catalogue.entrySet()) {
            data[i++] = new String[]{(String) entry.getKey(), String.valueOf(entry.getValue())};
        }
        return data;
    }

    /**
     * Inner class OfferRequestsServer.
     * This is the behaviour used by Car-seller agents to serve incoming requests
     * for offer from buyer agents.
     * If the requested car is in the local catalogue the seller agent replies
     * with a PROPOSE message specifying the
     * . Otherwise a REFUSE message is
     * sent back.
     */
    private class OfferRequestsServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // CFP Message received. Process it
                String mileage = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = catalogue.get(mileage);
                if (price != null) {
                    // The requested car is available for sale. Reply with the price
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(price.intValue()));
                } else {
                    // The requested car is NOT available for sale.
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer

    /**
     * Inner class PurchaseOrdersServer.
     * This is the behaviour used by Car-seller agents to serve incoming
     * offer acceptances (i.e. purchase orders) from buyer agents.
     * The seller agent removes the purchased car from its catalogue
     * and replies with an INFORM message to notify the buyer that the
     * purchase has been sucesfully completed.
     */
    private class PurchaseOrdersServer extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // ACCEPT_PROPOSAL Message received. Process it
                String mileage = msg.getContent();
                ACLMessage reply = msg.createReply();

                Integer price = catalogue.remove(mileage);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    log(mileage + " sold to agent " + msg.getSender().getName());
                } else {
                    // The requested car has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
                gui.render(mapCatalog());
            } else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer

    @Override
    protected String getAgentType() {
        return "S";
    }

}

