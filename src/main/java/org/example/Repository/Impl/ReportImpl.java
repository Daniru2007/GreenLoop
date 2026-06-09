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
import org.example.model.CustomerOrder;
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
                o.setUnitPrice(doc.getDouble("unit_price"));
                o.setTotalAmount(doc.getDouble("total_revenue"));
                results.add(o);
            });
        } catch (Exception e) {
            System.err.println("[Report] getTopSellingProducts error: " + e.getMessage());
        }
        return results;
    }

    private CustomerOrder mapDocToOrder(Document doc) {
        CustomerOrder o = new CustomerOrder();
        o.setMongoId(doc.getObjectId("_id").toString());
        o.setProductId(doc.getString("product_id"));
        o.setProductName(doc.getString("product_name"));
        o.setQuantityOrdered(doc.getInteger("quantity_ordered", 0));
        o.setUnitPrice(((Number) doc.get("unit_price")).doubleValue());
        o.setTotalAmount(((Number) doc.get("total_amount")).doubleValue());
        o.setCustomerName(doc.getString("customer_name"));
        o.setStatus(doc.getString("status"));
        o.setDeliveryAgentId(doc.getString("delivery_agent_id"));

        Date date = doc.getDate("order_date");
        if (date != null) {
            o.setOrderDate(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        }
        return o;
    }

    private Product mapDocToProduct(Document doc) {
        Product p = new Product();
        p.setMongoId(doc.getObjectId("_id").toString());
        p.setName(doc.getString("name"));
        p.setCategory(doc.getString("category"));
        p.setMaterial(doc.getString("material"));
        p.setPrice(((Number) doc.get("price")).doubleValue());
        p.setStockQuantity(doc.getInteger("stock_quantity", 0));
        p.setUnit(doc.getString("unit"));
        p.setSupplierName(doc.getString("supplier_name"));
        p.setReorderLevel(doc.getInteger("reorder_level", 0));
        return p;
    }

}