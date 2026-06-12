package org.example.ui.wrappers;

import org.example.model.DeliveryAgent;

public class AgentWrapper {
    private final DeliveryAgent agent;
    public AgentWrapper(DeliveryAgent agent) { this.agent = agent; }
    public DeliveryAgent getAgent() { return agent; }
    @Override
    public String toString() {
        return String.format("%s - %s (%s)", agent.getName(), agent.getVehicleType(), agent.getVehiclePlate());
    }
}
