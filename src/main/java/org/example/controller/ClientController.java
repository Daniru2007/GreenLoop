package org.example.controller;

import org.example.Repository.ClientRepository;
import org.example.Repository.Impl.ClientRepositoryImpl;
import org.example.model.Client;

import java.util.List;

public class ClientController {

    private final ClientRepository clientRepo = new ClientRepositoryImpl();

    public Client addClient(String name, String email, String phone, String address) {
        Client c = new Client(name, email, phone, address);
        return clientRepo.addClient(c);
    }

    public Client updateClient(String clientId, String name, String email, String phone, String address) {
        Client c = new Client(name, email, phone, address);
        c.setMongoId(clientId);
        return clientRepo.updateClient(c);
    }

    public void deleteClient(String clientId) {
        clientRepo.deleteClient(clientId);
    }

    public Client getClientById(String clientId) {
        return clientRepo.getClientById(clientId);
    }

    public List<Client> getAllClients() {
        return clientRepo.getAllClients();
    }
}
