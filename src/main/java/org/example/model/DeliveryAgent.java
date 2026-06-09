package org.example.model;

public class DeliveryAgent {

    private String mongoId;
    private String name;
    private String phone;
    private String email;
    private String licenseNumber;

    private String vehicleType;
    private String vehiclePlate;
    private String vehicleModel;


    private boolean isAvailable;

    public DeliveryAgent() {}

    public DeliveryAgent(int agentId, String name, String phone, String email,
                         String licenseNumber, String vehicleType,
                         String vehiclePlate, String vehicleModel) {
        this.name          = name;
        this.phone         = phone;
        this.email         = email;
        this.licenseNumber = licenseNumber;
        this.vehicleType   = vehicleType;
        this.vehiclePlate  = vehiclePlate;
        this.vehicleModel  = vehicleModel;
        this.isAvailable   = true;
    }


    public String getMongoId()             { return mongoId; }
    public void setMongoId(String mongoId) { this.mongoId = mongoId; }

    public String getName()                        { return name; }
    public void setName(String name)               { this.name = name; }

    public String getPhone()                       { return phone; }
    public void setPhone(String phone)             { this.phone = phone; }

    public String getEmail()                       { return email; }
    public void setEmail(String email)             { this.email = email; }

    public String getLicenseNumber()               { return licenseNumber; }
    public void setLicenseNumber(String n)         { this.licenseNumber = n; }

    public String getVehicleType()                 { return vehicleType; }
    public void setVehicleType(String t)           { this.vehicleType = t; }

    public String getVehiclePlate()                { return vehiclePlate; }
    public void setVehiclePlate(String p)          { this.vehiclePlate = p; }

    public String getVehicleModel()                { return vehicleModel; }
    public void setVehicleModel(String m)          { this.vehicleModel = m; }

    public boolean isAvailable()                   { return isAvailable; }
    public void setAvailable(boolean available)    { this.isAvailable = available; }

    @Override
    public String toString() {
        return String.format(
                "DeliveryAgent{id=%s, name='%s', phone='%s', license='%s', " +
                        "vehicle='%s %s (%s)', available=%b}",
                mongoId, name, phone, licenseNumber,
                vehicleType, vehicleModel, vehiclePlate, isAvailable
        );
    }
}

