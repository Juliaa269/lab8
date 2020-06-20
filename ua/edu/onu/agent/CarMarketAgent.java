package ua.edu.onu.agent;

import jade.core.Agent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ua.edu.onu.util.ConsoleColors;
import ua.edu.onu.util.Gateway;
import ua.edu.onu.util.Status;

public class CarMarketAgent extends Agent {
    private int milleage;
    private int price;
    private Status status = Status.NEW;
    private CarMarketAgent dealedWith;
    protected String agentType;
    protected String color = ConsoleColors.RESET;

    protected void setup(Object[] arguments) {
        if(arguments == null || arguments.length == 0) {
            // Make the agent terminate
            log("No target car mileage specified");
            doDelete();
        } else {
            String[] args = ((String) arguments[0]).split(" ");
            setMilleage(Integer.parseInt(args[0]));
            setPrice(Integer.parseInt(args[1]));
            log("Target car mileage is " + getMilleage());
            log("Target car price is " + getPrice());
        }
    }

    public CarMarketAgent() {
    }


    public int getMilleage() {
        return milleage;
    }

    public void setMilleage(int milleage) {
        this.milleage = milleage;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public CarMarketAgent getDealedWith() {
        return dealedWith;
    }

    public void setDealedWith(CarMarketAgent dealedWith) {
        this.dealedWith = dealedWith;
    }

    @Override
    public String toString() {
        return "CarMarketAgent{" +
                "name=" + getName() +
                ",milleage=" + milleage +
                ", price=" + price +
                '}';
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getId() {
        return this.getAgentType() + this.getLocalName();
    }

    protected String getAgentType() {
        throw new NotImplementedException();
    }

    protected void log(String text) {
        System.out.println(this.color + "[" + Gateway.getInstance().getRequestNumber() + "]" + text + ConsoleColors.RESET);
    }
}
