package ua.edu.onu.agent;

import jade.core.AID;
import ua.edu.onu.OfferingStatus;

import java.util.UUID;

public class Offering {
    private int mileage;
    private int price;
    private String id = UUID.randomUUID().toString();
    private OfferingStatus status = OfferingStatus.NEW;
    private AID sender;

    public Offering(String mileage, int price) {
        this.mileage = Integer.parseInt(mileage);
        this.price = price;
    }

    public Offering(String id, String mileage, int price, AID sender) {
        this(mileage, price);
        this.id = id;
        this.sender = sender;
    }

    public OfferingStatus getStatus() {
        return status;
    }

    public int getMileage() {
        return mileage;
    }

    public int getPrice() {
        return price;
    }

    public String getId() {
        return id;
    }

    public void setStatus(OfferingStatus status) {
        this.status = status;
    }

    public AID getSender() {
        return sender;
    }
}
