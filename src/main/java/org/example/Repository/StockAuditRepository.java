package org.example.Repository;

import org.example.model.StockAudit;
import java.util.List;

/**
 * Repository interface for StockAudit operations
 * Tracks all inventory movements for compliance and audit purposes
 */
public interface StockAuditRepository {
    StockAudit addAuditLog(StockAudit audit);
    List<StockAudit> getAuditsByProductId(String productId);
    List<StockAudit> getAuditsByType(String transactionType);
    List<StockAudit> getAllAudits();
    List<StockAudit> getAuditsByDateRange(String startDate, String endDate);
}
