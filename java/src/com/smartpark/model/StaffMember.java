package com.smartpark.model;

/**
 * Staff and operator model for park operations.
 */
public class StaffMember {
    private String id;
    private String name;
    private String assignedWork;
    private String shift;
    private String certification;
    private String status; // ON DUTY, ON BREAK, OFF DUTY

    public StaffMember(String id, String name, String assignedWork, String shift, String certification, String status) {
        this.id = id;
        this.name = name;
        this.assignedWork = assignedWork;
        this.shift = shift;
        this.certification = certification;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAssignedWork() { return assignedWork; }
    public void setAssignedWork(String assignedWork) { this.assignedWork = assignedWork; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public String getCertification() { return certification; }
    public void setCertification(String certification) { this.certification = certification; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
