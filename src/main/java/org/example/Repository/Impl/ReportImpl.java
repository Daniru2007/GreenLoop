package org.example.Repository.Impl;

import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.example.Repository.ReportRepository;
import org.example.config.DBManager;
import org.example.model.Client;
import org.example.model.CustomerOrder;
import org.example.model.OrderItem;
import org.example.model.Product;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportImpl implements ReportRepository {

    private final MongoCollection<Document> orderCollection;
    private final MongoCollection<Document> productCollection;

    public ReportImpl() {
        MongoDatabase db = DBManager.getDatabase();
        this.orderCollection   = db.getCollection("orders");
        this.productCollection = db.getCollection("products");
    }

    private Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private org.bson.conversions.Bson monthFilter(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end   = start.withDayOfMonth(start.lengthOfMonth());

        return Filters.and(
                Filters.eq("status", CustomerOrder.STATUS_DELIVERED),
                Filters.gte("order_date", toDate(start)),
                Filters.lte("order_date", toDate(end))
        );
    }

    @Override
    public List<CustomerOrder> getMonthlyDeliveredOrders(int month, int year) {
        List<CustomerOrder> customerOrders = new ArrayList<>();
        try {
            for (Document doc : orderCollection.find(monthFilter(month, year))
                    .sort(Sorts.ascending("order_date"))) {
                customerOrders.add(mapDocToOrder(doc));
            }
        } catch (Exception e) {
            System.err.println("[Report] getMonthlyDeliveredOrders error: " + e.getMessage());
        }
        return customerOrders;
    }

    @Override
    public double getMonthlyRevenue(int month, int year) {
        try {
            List<CustomerOrder> customerOrders = getMonthlyDeliveredOrders(month, year);
            return customerOrders.stream().mapToDouble(CustomerOrder::getTotalAmount).sum();
        } catch (Exception e) {
            System.err.println("[Report] getMonthlyRevenue error: " + e.getMessage());
        }
        return 0.0;
    }

    @Override
    public List<Product> getLowStockProducts() {
        List<Product> products = new ArrayList<>();
        try {
            // MongoDB can't directly compare two fields, so we fetch all
            // and filter in Java using each product's own reorderLevel
            for (Document doc : productCollection.find()
                    .sort(Sorts.ascending("stock_quantity"))) {
                int stock        = doc.getInteger("stock_quantity", 0);
                int reorderLevel = doc.getInteger("reorder_level", 0);
                if (stock <= reorderLevel) {
                    products.add(mapDocToProduct(doc));
                }
            }
        } catch (Exception e) {
            System.err.println("[Report] getLowStockProducts error: " + e.getMessage());
        }
        return products;
    }

    @Override
    public List<CustomerOrder> getTopSellingProducts(int month, int year, int topN) {
        List<CustomerOrder> results = new ArrayList<>();
        try {
            orderCollection.aggregate(List.of(
                    Aggregates.match(monthFilter(month, year)),
                    Aggregates.group("$product_name",
                            Accumulators.sum("total_qty",      "$quantity_ordered"),
                            Accumulators.sum("total_revenue",  "$total_amount"),
                            Accumulators.first("product_id",   "$product_id"),
                            Accumulators.first("unit_price",   "$unit_price")
                    ),
                    Aggregates.sort(Sorts.descending("total_qty")),
                    Aggregates.limit(topN)
            )).forEach((Block<? super Document>) doc -> {
                CustomerOrder o = new CustomerOrder();
                o.setProductName(doc.getString("_id"));
                o.setProductId(doc.getString("product_id"));
                o.setQuantityOrdered(doc.getInteger("total_qty", 0));
                o.setUnitPrice(doc.get("unit_price") != null ? ((Number) doc.get("unit_price")).doubleValue() : 0.0);
                o.setTotalAmount(doc.get("total_revenue") != null ? ((Number) doc.get("total_revenue")).doubleValue() : 0.0);
                results.add(o);
            });
        } catch (Exception e) {
            System.err.println("[Report] getTopSellingProducts error: " + e.getMessage());
        }
        return results;
    }

    private CustomerOrder mapDocToOrder(Document doc) {
        CustomerOrder o = new CustomerOrder();
        Object orderIdObj = doc.get("_id");
        o.setMongoId(orderIdObj != null ? orderIdObj.toString() : null);
        o.setStatus(doc.getString("status"));
        o.setDeliveryAgentId(doc.getString("delivery_agent_id"));
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

    private Product mapDocToProduct(Document doc) {
        Product p = new Product();
        Object id = doc.get("_id");
        p.setMongoId(id != null ? id.toString() : null);
        p.setName(doc.getString("name"));
        p.setCategory(doc.getString("category"));
        p.setMaterial(doc.getString("material"));
        p.setPrice(doc.get("price") != null ? ((Number) doc.get("price")).doubleValue() : 0.0);
        p.setStockQuantity(doc.get("stock_quantity") != null ? ((Number) doc.get("stock_quantity")).intValue() : 0);
        p.setUnit(doc.getString("unit"));
        p.setSupplierName(doc.getString("supplier_name"));
        p.setReorderLevel(doc.get("reorder_level") != null ? ((Number) doc.get("reorder_level")).intValue() : 0);
        return p;
    }

}