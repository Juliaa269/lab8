package ua.edu.onu.agent;

import jade.core.Agent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ua.edu.onu.util.ConsoleColors;
import ua.edu.onu.util.Gateway;
import ua.edu.onu.util.Status;

public class CarMarketAgent extends Agent {
    private int mileage;
    private int price;
    private Status status = Status.NEW;
    protected String color = ConsoleColors.RESET;
    private String dealAgentName;
    private int dealAgentPrice;
    private boolean dealFinished;

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "CarMarketAgent{" +
                "name=" + getName() +
                ",milleage=" + mileage +
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

    @Override
    public void doDelete() {
        Gateway.getInstance().deleteAgent(this);
        super.doDelete();
    }

    public void closeDeal(String name, int bestPrice){
        this.dealFinished = true;
        this.dealAgentName = name;
        this.dealAgentPrice = bestPrice;
    }
    public String getDealDetails() {
        return String.format("from %s with %d", dealAgentName, dealAgentPrice);
    }

    public boolean isDealFinished() {
        return dealFinished;
    }
}
