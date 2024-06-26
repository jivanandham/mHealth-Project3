package edu.pitt.lersais.mhealth.model;


public class GridItem {
    private int iId;
    private String iName;

    public GridItem() {
    }

    public GridItem(int iId, String iName) {
        this.iId = iId;
        this.iName = iName;
    }

    public int getiId() {
        return iId;
    }

    public String getiName() {
        return iName;
    }

    public void setiId(int iId) {
        this.iId = iId;
    }

    public void setiName(String iName) {
        this.iName = iName;
    }
}
