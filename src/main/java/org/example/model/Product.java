package org.example.model;

public class Product {

    private String mongoId;
    private String name;
    private String category;    // e.g. Bags, Boxes, Wraps
    private String material;    // e.g. Biodegradable, Recycled, Compostable
    private double price;
    private int stockQuantity;
    private String unit;        // e.g. kg, pack, roll
    private String supplierName;
    private int reorderLevel;   // low-stock alert triggers below this

    public Product() {}

    public Product(String name, String category, String material,
                   double price, int stockQuantity, String unit,
                   String supplierName, int reorderLevel) {
        this.name         = name;
        this.category     = category;
        this.material     = material;
        this.price        = price;
        this.stockQuantity = stockQuantity;
        this.unit         = unit;
        this.supplierName = supplierName;
        this.reorderLevel = reorderLevel;
    }

    public String getMongoId()                       { return mongoId; }
    public void setMongoId(String mongoId)           { this.mongoId = mongoId; }

    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }

    public String getCategory()                      { return category; }
    public void setCategory(String category)         { this.category = category; }

    public String getMaterial()                      { return material; }
    public void setMaterial(String material)         { this.material = material; }

    public double getPrice()                         { return price; }
    public void setPrice(double price)               { this.price = price; }

    public int getStockQuantity()                    { return stockQuantity; }
    public void setStockQuantity(int stockQuantity)  { this.stockQuantity = stockQuantity; }

    public String getUnit()                          { return unit; }
    public void setUnit(String unit)                 { this.unit = unit; }

    public String getSupplierName()                  { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public int getReorderLevel()                     { return reorderLevel; }
    public void setReorderLevel(int reorderLevel)    { this.reorderLevel = reorderLevel; }

    @Override
    public String toString() {
        return String.format(
                "Product{name='%s', category='%s', material='%s', price=%.2f, " +
                        "stock=%d %s, supplier='%s', reorderLevel=%d}",
                name, category, material, price,
                stockQuantity, unit, supplierName, reorderLevel
        );
    }
}