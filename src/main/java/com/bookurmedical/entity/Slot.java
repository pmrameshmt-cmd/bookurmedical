package com.bookurmedical.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "slots")
public class Slot {
    @Id
    private String id;
    private String doctorId;
    private String patientId;
    private String date; // e.g. "2026-03-26"
    private String time; // e.g. "10:00 AM"
    private String status; // "available", "selected", "booked"
    private String caseId;

    public Slot() {}

    public Slot(String doctorId, String date, String time) {
        this.doctorId = doctorId;
        this.date = date;
        this.time = time;
        this.status = "available";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }
}
