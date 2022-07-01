package com.matrix_maeny.onlinetodolist;

public class WorkModel {

    private String workName;

    public WorkModel() {
    }

    public WorkModel(String workName) {
        this.workName = workName;
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }
}
