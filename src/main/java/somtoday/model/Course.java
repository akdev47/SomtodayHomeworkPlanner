package somtoday.model;

import java.util.List;

public class Course {
    private int courseID;

    private String courseName;

    private int teacherID;

    private List<Teacher> teacherList;

    public Course() {}

    public Course(int courseID, String courseName, int teacherID, List<Teacher> teacherList) {
        this.courseID = courseID;
        this.courseName = courseName;
        this.teacherID = teacherID;
        this.teacherList = teacherList;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(int teacherID) {
        this.teacherID = teacherID;
    }

    public List<Teacher> getTeacherList() {
        return teacherList;
    }

    public void setTeacherList(List<Teacher> teacherList) {
        this.teacherList = teacherList;
    }
}
