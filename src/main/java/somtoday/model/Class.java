package somtoday.model;

import java.util.Date;
import java.util.List;

public class Class {

    private int classID;

    private String className;

    private int classCapacity;

    private List<Student> studentList;

    private boolean hasProfilePicture;

    public Class() {
    }
    public Class(int classID, String className, int classCapacity, List<Student> studentList,  boolean hasProfilePicture) {
        this.classID = classID;
        this.className = className;
        this.classCapacity = classCapacity;
        this.studentList = studentList;
        this.hasProfilePicture = hasProfilePicture;
    }

    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getClassCapacity() {
        return classCapacity;
    }

    public void setClassCapacity(int classCapacity) {
        this.classCapacity = classCapacity;
    }

    public List<Student> getStudentList() {
        return studentList;
    }

    public void setStudentList(List<Student> studentList) {
        this.studentList = studentList;
    }

    public void setHasProfilePicture(boolean hasProfilePicture) {
        this.hasProfilePicture = hasProfilePicture;
    }

    public boolean isHasProfilePicture() {
        return hasProfilePicture;
    }
}
