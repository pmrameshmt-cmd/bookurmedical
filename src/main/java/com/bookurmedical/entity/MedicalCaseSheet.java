
package com.bookurmedical.entity;

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
    private String userId; // Link to User

    // Patient Information
    private String name;
    private String address;
    private String dob;
    private String contactInformation;

    // Physical Stats
    private String height;
    private String weight;
    private String sex;

    // Vitals
    private String heartRate;
    private String spo2;
    private String bloodPressure;
    private String respiratoryRate;

    // Diagnosis
    private String primaryDiagnosis;
    private String secondaryDiagnosis;

    // History & Physical
    private String chiefComplaint;
    private String historyOfPresentingIllness;
    private String physicalExamination;

    // Diagnostic Files (Paths/URLs)
    // Imaging
    private String xrayFile;
    private String ctScanFile;
    private String usgFile;
    private String mriFile;
    // Pathological Lab
    private String bloodReportFile;
    private String urineReportFile;
    private String tissueBiopsyFile;
    // Biochemistry Lab
    private String liverFunctionTestFile;
    private String kidneyFunctionTestFile;
    private String lipidProfileFile;
    // Micro Lab
    private String bloodCultureFile;
    private String urineCultureFile;
    private String sputumCultureFile;

    // Med History
    private String presentOngoingTreatment;
    private String previousMedicalHistory;
    private String dischargeSummaryFile;

    // Surgical & Medical History
    private String surgeries;
    private String conditions;

    // Allergies
    private String allergies;
    private String reaction;

    // Progress Notes (Simplified as a text block or we could make it a list)
    private String progressNotes;
}
