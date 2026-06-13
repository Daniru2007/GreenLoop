package org.example.Repository;

import org.example.model.Client;
import java.util.List;

public interface ClientRepository {
    Client addClient(Client client);
    Client updateClient(Client client);
    void deleteClient(String clientId);
    Client getClientById(String clientId);
    List<Client> getAllClients();
}
