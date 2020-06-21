package ua.edu.onu.util;

import ua.edu.onu.agent.Offering;

import java.util.Comparator;

public class Sort {
    public static final Comparator<Offering> OFFERING_COMPARATOR = (o1, o2) -> o1.getMileage() == o2.getMileage() ?
            Integer.valueOf(o1.getPrice()).compareTo(o2.getPrice()) :
            Integer.valueOf(o1.getMileage()).compareTo(o2.getMileage());


}
