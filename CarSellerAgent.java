package lab8;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Hashtable;

public class CarSellerAgent extends Agent {
    // The catalogue of cars for sale (maps the mileage of a car to its price)
    private Hashtable catalogue;
    // The GUI by means of which the user can add books in the catalogue
    private lab8.CarSellerGui myGui;

    // Put agent initializations here
    protected void setup() {
        // Create the catalogue
        catalogue = new Hashtable();

        // Create and show the GUI
        myGui = new lab8.CarSellerGui(this);
        myGui.show();

        // Register the car-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("car-selling");
        sd.setName("JADE-car-trading");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Add the behaviour serving queries from buyer agents

        addBehaviour(new OfferRequestsServer());

        // Add the behaviour serving purchase orders from buyer agents

        addBehaviour(new PurchaseOrdersServer());
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
        myGui.dispose();
        // Printout a dismissal message
        System.out.println("Seller-agent " + getAID().getName() + " terminating.");
    }

    /**
     * This is invoked by the GUI when the user adds a new car for sale
     */
    public void updateCatalogue(final String mileage, final int price) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                catalogue.put(mileage, new Integer(price));
                System.out.println(mileage + " inserted into catalogue. Price = " + price);
            }
        });
    }

    /**
     * Inner class OfferRequestsServer.
     * This is the behaviour used by Car-seller agents to serve incoming requests
     * for offer from buyer agents.
     * If the requested car is in the local catalogue the seller agent replies
     * with a PROPOSE message specifying the price. Otherwise a REFUSE message is
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

                Integer price = (Integer) catalogue.get(mileage);
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

                Integer price = (Integer) catalogue.remove(mileage);
                if (price != null) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(mileage + " sold to agent " + msg.getSender().getName());
                } else {
                    // The requested car has been sold to another buyer in the meanwhile .
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("not-available");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }  // End of inner class OfferRequestsServer
}

