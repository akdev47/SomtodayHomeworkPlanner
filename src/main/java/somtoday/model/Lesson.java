package somtoday.model;

import java.util.List;

public class Lesson {
    private int lessonID;

    private int courseID;

    private int classID;

    private int schoolYear;


    private String lessonName;

    private String lessonDescription;

    public Lesson() { }

    public Lesson(int lessonID, int courseID, int classID, int schoolYear) {
        this.lessonID = lessonID;
        this.courseID = courseID;
        this.classID = classID;
        this.schoolYear = schoolYear;
    }

    public int getLessonID() {
        return lessonID;
    }

    public void setLessonID(int lessonID) {
        this.lessonID = lessonID;
    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseID) {
        this.courseID = courseID;
    }

    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public int getSchoolYear() {
        return schoolYear;
    }

    public void setSchoolYear(int schoolYear) {
        this.schoolYear = schoolYear;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public String getLessonDescription() {
        return lessonDescription;
    }

    public void setLessonDescription(String lessonDescription) {
        this.lessonDescription = lessonDescription;
    }
}
