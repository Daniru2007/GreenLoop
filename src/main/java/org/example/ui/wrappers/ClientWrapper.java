package org.example.ui.wrappers;

import org.example.model.Client;

public class ClientWrapper {
    private final Client client;
    public ClientWrapper(Client client) { this.client = client; }
    public Client getClient() { return client; }
    @Override
    public String toString() {
        return String.format("%s - %s", client.getName(), client.getEmail());
    }
}
