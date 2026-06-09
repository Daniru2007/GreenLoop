package org.example.Repository.Impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.Repository.DeliveryAgentRepository;
import org.example.config.DBManager;
import org.example.model.DeliveryAgent;

public class DeliveryAgentImpl implements DeliveryAgentRepository {

    private final MongoCollection<Document> collection;

    public DeliveryAgentImpl() {
        MongoDatabase db = DBManager.getDatabase();
        this.collection = db.getCollection("delivery_agents");
    }

    @Override
    public DeliveryAgent addDeliveryAgent(DeliveryAgent da) {
        try {
            Document doc = new Document()
                    .append("name",           da.getName())
                    .append("phone",          da.getPhone())
                    .append("email",          da.getEmail())
                    .append("license_number", da.getLicenseNumber())
                    .append("vehicle_type",   da.getVehicleType())
                    .append("vehicle_plate",  da.getVehiclePlate())
                    .append("vehicle_model",  da.getVehicleModel())
                    .append("is_available",   da.isAvailable());

            collection.insertOne(doc);
            da.setMongoId(doc.getObjectId("_id").toString());
            return da;
        } catch (Exception e) {
            System.err.println("[DeliveryAgent] addDeliveryAgent error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public DeliveryAgent updateDeliveryAgent(DeliveryAgent da) {
        try {
            collection.updateOne(
                    Filters.eq("_id", new ObjectId(da.getMongoId())),
                    Updates.combine(
                            Updates.set("name",           da.getName()),
                            Updates.set("phone",          da.getPhone()),
                            Updates.set("email",          da.getEmail()),
                            Updates.set("license_number", da.getLicenseNumber()),
                            Updates.set("vehicle_type",   da.getVehicleType()),
                            Updates.set("vehicle_plate",  da.getVehiclePlate()),
                            Updates.set("vehicle_model",  da.getVehicleModel()),
                            Updates.set("is_available",   da.isAvailable())
                    )
            );
            return da;
        } catch (Exception e) {
            System.err.println("[DeliveryAgent] updateDeliveryAgent error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteDeliveryAgent(String agentId) {

        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(agentId)));
        } catch (Exception e) {
            System.err.println("[DeliveryAgent] deleteDeliveryAgent error: " + e.getMessage());
        }
    }

    @Override
    public DeliveryAgent getDeliveryAgent(String agentId) {
        try {
            Document doc = collection.find(Filters.eq("_id", new ObjectId(agentId))).first();
            return doc != null ? mapDocToAgent(doc) : null;
        } catch (Exception e) {
            System.err.println("[DeliveryAgent] getDeliveryAgent error: " + e.getMessage());
            return null;
        }
    }

    private DeliveryAgent mapDocToAgent(Document doc) {
        DeliveryAgent a = new DeliveryAgent();
        a.setMongoId(doc.getObjectId("_id").toString());
        a.setName(doc.getString("name"));
        a.setPhone(doc.getString("phone"));
        a.setEmail(doc.getString("email"));
        a.setLicenseNumber(doc.getString("license_number"));
        a.setVehicleType(doc.getString("vehicle_type"));
        a.setVehiclePlate(doc.getString("vehicle_plate"));
        a.setVehicleModel(doc.getString("vehicle_model"));
        a.setAvailable(Boolean.TRUE.equals(doc.getBoolean("is_available")));
        return a;
    }

}
