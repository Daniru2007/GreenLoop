package org.example.Repository;

import org.example.model.CustomerOrder;
import org.example.model.Product;

import java.util.List;

public interface ReportRepository {

    /**
     * Get all delivered orders for a specific month and year.
     * @param month 1-12
     * @param year  e.g. 2026
     */
    List<CustomerOrder> getMonthlyDeliveredOrders(int month, int year);

    /**
     * Calculate total revenue from delivered orders for a given month and year.
     * @param month 1-12
     * @param year  e.g. 2026
     */
    double getMonthlyRevenue(int month, int year);

    /**
     * Get products whose stockQuantity is at or below their reorderLevel.
     */
    List<Product> getLowStockProducts();

    /**
     * Get top selling products for a given month and year.
     * @param month 1-12
     * @param year  e.g. 2026
     * @param topN  how many top products to return
     */
    List<CustomerOrder> getTopSellingProducts(int month, int year, int topN);
}