package org.example.Repository.Impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.Repository.ClientRepository;
import org.example.config.DBManager;
import org.example.model.Client;

import java.util.ArrayList;
import java.util.List;

public class ClientRepositoryImpl implements ClientRepository {

    private final MongoCollection<Document> collection;

    public ClientRepositoryImpl() {
        this.collection = DBManager.getDatabase().getCollection("clients");
        importClientsFromOrders();
    }

    @Override
    public Client addClient(Client client) {
        try {
            Document doc = new Document()
                    .append("name", client.getName())
                    .append("email", client.getEmail())
                    .append("phone", client.getPhone())
                    .append("address", client.getAddress());
            collection.insertOne(doc);
            client.setMongoId(doc.getObjectId("_id").toString());
            return client;
        } catch (Exception e) {
            System.err.println("[Client] addClient error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Client updateClient(Client client) {
        try {
            collection.updateOne(
                    Filters.eq("_id", new ObjectId(client.getMongoId())),
                    Updates.combine(
                            Updates.set("name", client.getName()),
                            Updates.set("email", client.getEmail()),
                            Updates.set("phone", client.getPhone()),
                            Updates.set("address", client.getAddress())
                    )
            );
            return client;
        } catch (Exception e) {
            System.err.println("[Client] updateClient error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteClient(String clientId) {
        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(clientId)));
        } catch (Exception e) {
            System.err.println("[Client] deleteClient error: " + e.getMessage());
        }
    }

    @Override
    public Client getClientById(String clientId) {
        try {
            Document doc = collection.find(Filters.eq("_id", new ObjectId(clientId))).first();
            return doc != null ? mapDocToClient(doc) : null;
        } catch (Exception e) {
            System.err.println("[Client] getClientById error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Client> getAllClients() {
        List<Client> clients = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                clients.add(mapDocToClient(doc));
            }
        } catch (Exception e) {
            System.err.println("[Client] getAllClients error: " + e.getMessage());
        }
        return clients;
    }

    private Client mapDocToClient(Document doc) {
        Client c = new Client();
        Object id = doc.get("_id");
        c.setMongoId(id != null ? id.toString() : null);
        c.setName(doc.getString("name"));
        c.setEmail(doc.getString("email"));
        c.setPhone(doc.getString("phone"));
        c.setAddress(doc.getString("address"));
        return c;
    }

    private void importClientsFromOrders() {
        try {
            com.mongodb.client.MongoCollection<Document> ordersCol = DBManager.getDatabase().getCollection("orders");
            for (Document orderDoc : ordersCol.find()) {
                Document clientDoc = (Document) orderDoc.get("client");
                String name = null;
                String email = null;
                String phone = "";
                String address = "";

                if (clientDoc != null) {
                    name = clientDoc.getString("name");
                    email = clientDoc.getString("email");
                    phone = clientDoc.getString("phone");
                    address = clientDoc.getString("address");
                } else {
                    name = orderDoc.getString("customer_name");
                    if (name != null) {
                        email = name.toLowerCase().replaceAll("\\s+", "") + "@example.com";
                    }
                }

                if (name == null || name.trim().isEmpty()) {
                    continue;
                }
                if (email == null || email.trim().isEmpty()) {
                    email = name.toLowerCase().replaceAll("\\s+", "") + "@example.com";
                }

                long count = collection.countDocuments(Filters.eq("email", email));
                if (count == 0) {
                    Document newClient = new Document()
                            .append("name", name)
                            .append("email", email)
                            .append("phone", phone != null ? phone : "")
                            .append("address", address != null ? address : "");
                    collection.insertOne(newClient);
                    System.out.println("[Client Migration] Imported client: " + name + " (" + email + ") from orders.");
                }
            }
        } catch (Exception e) {
            System.err.println("[Client Migration] Error importing clients from orders: " + e.getMessage());
        }
    }
}
