
package com.bookurmedical.entity;

import com.bookurmedical.annotation.Encrypted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "medical_case_sheets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCaseSheet {
    @Id
    private String id;

    private String userId; // Link to User — kept plain for querying

    // ── Patient Information (PHI) ─────────────────────────────────────────────
    @Encrypted
    private String name;

    @Encrypted
    private String address;

    @Encrypted
    private String dob;

    @Encrypted
    private String contactInformation;

    // ── Physical Stats (PHI) ─────────────────────────────────────────────────
    @Encrypted
    private String height;

    @Encrypted
    private String weight;

    @Encrypted
    private String sex;

    // ── Vitals (PHI) ─────────────────────────────────────────────────────────
    @Encrypted
    private String heartRate;

    @Encrypted
    private String spo2;

    @Encrypted
    private String bloodPressure;

    @Encrypted
    private String respiratoryRate;

    // ── Diagnosis (PHI) ──────────────────────────────────────────────────────
    @Encrypted
    private String primaryDiagnosis;

    @Encrypted
    private String secondaryDiagnosis;

    // ── History & Physical (PHI) ─────────────────────────────────────────────
    @Encrypted
    private String chiefComplaint;

    @Encrypted
    private String historyOfPresentingIllness;

    @Encrypted
    private String physicalExamination;

    // ── Diagnostic File Paths / URLs ──────────────────────────────────────────
    // Imaging
    @Encrypted
    private String xrayFile;
    @Encrypted
    private String ctScanFile;
    @Encrypted
    private String usgFile;
    @Encrypted
    private String mriFile;

    // Pathological Lab
    @Encrypted
    private String bloodReportFile;
    @Encrypted
    private String urineReportFile;
    @Encrypted
    private String tissueBiopsyFile;

    // Biochemistry Lab
    @Encrypted
    private String liverFunctionTestFile;
    @Encrypted
    private String kidneyFunctionTestFile;
    @Encrypted
    private String lipidProfileFile;

    // Micro Lab
    @Encrypted
    private String bloodCultureFile;
    @Encrypted
    private String urineCultureFile;
    @Encrypted
    private String sputumCultureFile;

    // ── Medical History (PHI) ────────────────────────────────────────────────
    @Encrypted
    private String presentOngoingTreatment;

    @Encrypted
    private String previousMedicalHistory;

    @Encrypted
    private String dischargeSummaryFile;

    // ── Surgical & Medical History (PHI) ─────────────────────────────────────
    @Encrypted
    private String surgeries;

    @Encrypted
    private String conditions;

    // ── Allergies (PHI) ──────────────────────────────────────────────────────
    @Encrypted
    private String allergies;

    @Encrypted
    private String reaction;

    // ── Status ──────────────────────────────────────────────────────────────
    private String status; // DRAFT or COMPLETED

    @Encrypted
    private String progressNotes;
}
