package ua.edu.onu.agent;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import ua.edu.onu.util.ConsoleColors;
import ua.edu.onu.util.Gateway;

import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ua.edu.onu.util.Sort.OFFERING_COMPARATOR;

public class CarBuyerAgent extends CarMarketAgent {
    private AID[] sellerAgents;

    private void setup(Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            // Make the agent terminate
            log("No target car mileage specified");
            doDelete();
        } else {
            String[] args = ((String) arguments[0]).split(" ");
            setMileage(Integer.parseInt(args[0]));
            setPrice(Integer.parseInt(args[1]));
            log("Target car mileage is " + getMileage());
        }
    }

    protected void setup() {
        this.color = ConsoleColors.YELLOW;
        setup(getArguments());

        // Add a TickerBehaviour that schedules a request to seller agents every minute
        addBehaviour(getTicker());

        Gateway.getInstance().append(this);

    }

    private TickerBehaviour getTicker() {
        return new TickerBehaviour(this, 5000) {
            protected void onTick() {
                log("Trying to buy " + getMileage());
                // Update the list of seller agents
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("car-selling");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    log("Found the following seller agents:");
                    sellerAgents = new AID[result.length];
                    for (int i = 0; i < result.length; ++i) {
                        sellerAgents[i] = result[i].getName();
                        log(sellerAgents[i].getName());
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

                // Perform the request
                myAgent.addBehaviour(new RequestPerformer());
            }
        };
    }

    protected void takeDown() {
        // Printout a dismissal message
        log("Buyer-agent " + getAID().getName() + " terminating.");
    }

    /**
     * Inner class RequestPerformer.
     * This is the behaviour used by Book-buyer agents to request seller
     * agents the target book.
     */
    private class RequestPerformer extends Behaviour {

        private int repliesCnt = 0; // The counter of replies from seller agents
        private MessageTemplate mt; // The template to receive replies
        private int step = 0;
        TreeSet<Offering> offerings = new TreeSet<>(OFFERING_COMPARATOR);
        Offering bestOffering;

        public void action() {
            switch (step) {
                case 0:
                    // Send the cfp to all sellers
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < sellerAgents.length; ++i) {
                        cfp.addReceiver(sellerAgents[i]);
                    }
                    cfp.setContent(getMileage() + " " + getPrice());
                    cfp.setConversationId("car-trade");
                    cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
                    myAgent.send(cfp);
                    // Prepare the template to get proposals
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("car-trade"),
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
                            String[] content = reply.getContent().split(" ");
                            log("content: " + Stream.of(content).collect(Collectors.joining(", ")));
                            Offering offering = new Offering(content[0], content[1], Integer.parseInt(content[2]), reply.getSender());
                            offerings.add(offering);
                        }
                        repliesCnt++;
                        if (repliesCnt >= sellerAgents.length) {
                            // We received all replies
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    // Send the purchase order to the seller that provided the best offer
                    if (offerings.isEmpty()) {
                        log("There is no applicable offerings");
                        return;
                    }

                    bestOffering = offerings.first();
                    log("Offering sent " + bestOffering);

                    ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    order.addReceiver(bestOffering.getSender());
                    order.setContent(bestOffering.getId());
                    order.setConversationId("car-trade");
                    order.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(order);
                    // Prepare the template to get the purchase order reply
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("car-trade"),
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
                            log(getMileage() + " successfully purchased from agent " + reply.getSender().getName());
                            log("Price = " + bestOffering);
                            ((CarMarketAgent) myAgent).closeDeal(bestOffering.getId());
                            myAgent.doDelete();
                        } else {
                            log("Attempt failed: requested car already sold.");
                        }

                        step = 4;
                    } else {
                        block();
                    }
                    break;
            }
        }

        public boolean done() {
            if (step == 3 && bestOffering == null) {
                log("Attempt failed: " + getMileage() + " not available for sale");
            }
            return ((step == 3 && bestOffering == null) || step == 4);
        }
    }  // End of inner class RequestPerformer

    @Override
    protected String getAgentType() {
        return "B";
    }

}

