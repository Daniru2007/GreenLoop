package org.example.Repository.Impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.Repository.CustomerOrderRepository;
import org.example.config.DBManager;
import org.example.model.CustomerOrder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerOrderImpl implements CustomerOrderRepository {

    private final MongoCollection<Document> collection;

    public CustomerOrderImpl() {
        MongoDatabase db = DBManager.getDatabase();
        this.collection = db.getCollection("orders");
    }

    private Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public CustomerOrder addOrder(CustomerOrder order) {
        try {
            Document doc = new Document()
                    .append("product_id",        order.getProductId())
                    .append("product_name",      order.getProductName())
                    .append("quantity_ordered",  order.getQuantityOrdered())
                    .append("unit_price",        order.getUnitPrice())
                    .append("total_amount",      order.getTotalAmount())
                    .append("customer_name",     order.getCustomerName())
                    .append("status",            order.getStatus())
                    .append("delivery_agent_id", order.getDeliveryAgentId())
                    .append("supplier_name",     order.getSupplierName());

            if (order.getOrderDate() != null) {
                doc.append("order_date", toDate(order.getOrderDate()));
            }

            collection.insertOne(doc);
            order.setMongoId(doc.getObjectId("_id").toString());
            return order;
        } catch (Exception e) {
            System.err.println("[CustomerOrder] addOrder error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public CustomerOrder updateOrder(CustomerOrder order) {
        try {
            collection.updateOne(
                    Filters.eq("_id", new ObjectId(order.getMongoId())),
                    Updates.combine(
                            Updates.set("product_id",        order.getProductId()),
                            Updates.set("product_name",      order.getProductName()),
                            Updates.set("quantity_ordered",  order.getQuantityOrdered()),
                            Updates.set("unit_price",        order.getUnitPrice()),
                            Updates.set("total_amount",      order.getTotalAmount()),
                            Updates.set("customer_name",     order.getCustomerName()),
                            Updates.set("status",            order.getStatus()),
                            Updates.set("delivery_agent_id", order.getDeliveryAgentId()),
                            Updates.set("order_date",        toDate(order.getOrderDate())),
                            Updates.set("supplier_name",     order.getSupplierName())
                    )
            );
            return order;
        } catch (Exception e) {
            System.err.println("[CustomerOrder] updateOrder error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void deleteOrder(String orderId) {
        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(orderId)));
        } catch (Exception e) {
            System.err.println("[CustomerOrder] deleteOrder error: " + e.getMessage());
        }
    }

    @Override
    public CustomerOrder getOrderById(String orderId) {
        try {
            Document doc = collection.find(Filters.eq("_id", new ObjectId(orderId))).first();
            return doc != null ? mapDocToOrder(doc) : null;
        } catch (Exception e) {
            System.err.println("[CustomerOrder] getOrderById error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<CustomerOrder> getAllOrders() {
        try {
            List<CustomerOrder> orders = new ArrayList<>();
            for (Document doc : collection.find()) {
                orders.add(mapDocToOrder(doc));
            }
            return orders;
        } catch (Exception e) {
            System.err.println("[CustomerOrder] getAllOrders error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private CustomerOrder mapDocToOrder(Document doc) {
        CustomerOrder o = new CustomerOrder();
        o.setMongoId(doc.getObjectId("_id").toString());
        o.setProductId(doc.getString("product_id"));
        o.setProductName(doc.getString("product_name"));
        o.setQuantityOrdered(doc.getInteger("quantity_ordered", 0));
        o.setUnitPrice(doc.get("unit_price") != null ? ((Number) doc.get("unit_price")).doubleValue() : 0.0);
        o.setTotalAmount(doc.get("total_amount") != null ? ((Number) doc.get("total_amount")).doubleValue() : 0.0);
        o.setCustomerName(doc.getString("customer_name"));
        o.setStatus(doc.getString("status"));
        o.setDeliveryAgentId(doc.getString("delivery_agent_id"));
        o.setSupplierName(doc.getString("supplier_name"));

        Date date = doc.getDate("order_date");
        if (date != null) {
            o.setOrderDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        return o;
    }
}
