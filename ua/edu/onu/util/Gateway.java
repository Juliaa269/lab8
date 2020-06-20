package ua.edu.onu.util;

import ua.edu.onu.agent.CarMarketAgent;

import java.util.LinkedHashSet;
import java.util.Set;

public class Gateway {

    private static Gateway gateway;

    private Set<CarMarketAgent> agents;
    private int requestNumber;
    private Gui gui = new Gui();

    private Gateway(int requestNumber) {
        this.requestNumber = requestNumber;
        agents = new LinkedHashSet<>();
    }

    public static Gateway getInstance() {
        if (gateway == null) {
            gateway = new Gateway(0);
        }

        return gateway;
    }

    public String getRequestNumber() {
        requestNumber = ++requestNumber;
        return requestNumber + ":" + this.toString();
    }

    public void append(CarMarketAgent agent) {
        log("add agent: " + agent);
        agents.add(agent);
        gui.render(getAgents());
    }

    public String[][] getAgents() {
        String[][] data = new String[agents.size()][];
        int i = 0;

        log("for: " + agents);
        for (CarMarketAgent agent : agents) {
            String localName = agent.getDealedWith() == null ? "NONE" : agent.getDealedWith().getId();
            data[i++] = new String[]{agent.getId(), String.valueOf(agent.getMilleage()), String.valueOf(agent.getPrice()), agent.getStatus().toString(), localName};
            log("data[" + (i - 1) + "]: " + data[i - 1][0] + "," + data[i - 1][1] + "," + data[i - 1][2] + "," + data[i - 1][3] + "," + data[i - 1][4]);
        }

        return data;
    }

    private void log(String text) {
        System.out.println(ConsoleColors.CYAN + "[" + Gateway.getInstance().getRequestNumber() + "]" + text + ConsoleColors.RESET);
    }
}
