package org.example.Repository.Impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.Repository.CustomerOrderRepository;
import org.example.config.DBManager;
import org.example.model.Client;
import org.example.model.CustomerOrder;
import org.example.model.OrderItem;

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
            // Serialize Client
            Document clientDoc = null;
            if (order.getClient() != null) {
                clientDoc = new Document()
                        .append("name", order.getClient().getName())
                        .append("email", order.getClient().getEmail())
                        .append("phone", order.getClient().getPhone())
                        .append("address", order.getClient().getAddress());
            }

            // Serialize OrderItems
            List<Document> itemDocs = new ArrayList<>();
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    itemDocs.add(new Document()
                            .append("product_id", item.getProductId())
                            .append("product_name", item.getProductName())
                            .append("quantity", item.getQuantity())
                            .append("unit_price", item.getUnitPrice()));
                }
            }

            Document doc = new Document()
                    .append("client",            clientDoc)
                    .append("order_items",       itemDocs)
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
            // Serialize Client
            Document clientDoc = null;
            if (order.getClient() != null) {
                clientDoc = new Document()
                        .append("name", order.getClient().getName())
                        .append("email", order.getClient().getEmail())
                        .append("phone", order.getClient().getPhone())
                        .append("address", order.getClient().getAddress());
            }

            // Serialize OrderItems
            List<Document> itemDocs = new ArrayList<>();
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    itemDocs.add(new Document()
                            .append("product_id", item.getProductId())
                            .append("product_name", item.getProductName())
                            .append("quantity", item.getQuantity())
                            .append("unit_price", item.getUnitPrice()));
                }
            }

            collection.updateOne(
                    Filters.eq("_id", new ObjectId(order.getMongoId())),
                    Updates.combine(
                            Updates.set("client",            clientDoc),
                            Updates.set("order_items",       itemDocs),
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
        Object id = doc.get("_id");
        o.setMongoId(id != null ? id.toString() : null);
        o.setStatus(doc.getString("status"));
        Object agentIdObj = doc.get("delivery_agent_id");
        o.setDeliveryAgentId(agentIdObj != null ? agentIdObj.toString() : null);
        o.setSupplierName(doc.getString("supplier_name"));
        o.setTotalAmount(doc.get("total_amount") != null ? ((Number) doc.get("total_amount")).doubleValue() : 0.0);

        Date date = doc.getDate("order_date");
        if (date != null) {
            o.setOrderDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }

        // Map Client
        if (doc.containsKey("client") && doc.get("client") instanceof Document) {
            Document clientDoc = (Document) doc.get("client");
            Client client = new Client(
                    clientDoc.getString("name"),
                    clientDoc.getString("email"),
                    clientDoc.getString("phone"),
                    clientDoc.getString("address")
            );
            o.setClient(client);
        } else {
            // Fallback to legacy fields
            String customerName = doc.getString("customer_name");
            Client client = new Client(
                    customerName,
                    customerName != null ? customerName.toLowerCase().replaceAll("\\s+", "") + "@example.com" : "",
                    "",
                    ""
            );
            o.setClient(client);
        }

        // Map Order Items
        List<OrderItem> items = new ArrayList<>();
        if (doc.containsKey("order_items") && doc.get("order_items") instanceof List) {
            List<?> rawList = (List<?>) doc.get("order_items");
            for (Object obj : rawList) {
                if (obj instanceof Document) {
                    Document itemDoc = (Document) obj;
                    items.add(new OrderItem(
                            itemDoc.getString("product_id"),
                            itemDoc.getString("product_name"),
                            itemDoc.getInteger("quantity", 0),
                            itemDoc.get("unit_price") != null ? ((Number) itemDoc.get("unit_price")).doubleValue() : 0.0
                    ));
                }
            }
        } else {
            // Fallback to legacy fields
            String productId = doc.getString("product_id");
            String productName = doc.getString("product_name");
            int qty = doc.getInteger("quantity_ordered", 0);
            double price = doc.get("unit_price") != null ? ((Number) doc.get("unit_price")).doubleValue() : 0.0;
            if (productId != null || productName != null || qty > 0) {
                items.add(new OrderItem(productId, productName, qty, price));
            }
        }
        o.setOrderItems(items);

        return o;
    }
}

