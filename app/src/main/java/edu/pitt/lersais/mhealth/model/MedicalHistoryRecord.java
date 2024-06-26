package edu.pitt.lersais.mhealth.model;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The MedicalHistoryRecord entity that represents a medical history record.
 *
 * @author Haobing Huang and Runhua Xu.
 */

public class MedicalHistoryRecord implements Serializable{

    public String name;
    public String dob;
    public String sex;

    public String marital_status;
    public String occupation;
    public String contact;
    public String allergies;
    public String diseases;
    public HashMap<String,String> family_diseases;
    public HashMap<String,String> habits;
    public String comments;

    public MedicalHistoryRecord() {}

    public MedicalHistoryRecord(String name,String dob,String sex,
                String marital_status,String occupation,String contact, String allergies,
                String diseases, HashMap<String,String> family_diseases,
                HashMap<String,String> habits,String comments){
        this.name = name;
        this.dob = dob;
        this.sex = sex;
        this.marital_status = marital_status;
        this.occupation = occupation;
        this.contact = contact;
        this.allergies = allergies;
        this.diseases = diseases;
        this.family_diseases = family_diseases;
        this.habits = habits;
        this.comments = comments;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getMarital_status() {
        return marital_status;
    }

    public void setMarital_status(String marital_status) {
        this.marital_status = marital_status;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getDiseases() {
        return diseases;
    }

    public void setDiseases(String diseases) {
        this.diseases = diseases;
    }

    public HashMap<String, String> getFamily_diseases() {
        return family_diseases;
    }

    public void setFamily_diseases(HashMap<String, String> family_diseases) {
        this.family_diseases = family_diseases;
    }

    public HashMap<String, String> getHabits() {
        return habits;
    }

    public void setHabits(HashMap<String, String> habits) {
        this.habits = habits;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
