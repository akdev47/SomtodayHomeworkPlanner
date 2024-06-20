package somtoday.model;

import java.sql.Time;
import java.util.Date;
import java.util.List;

public class Homework {

    private int homeworkID;
    private String homeworkName;

    private Date dueDate;

    private Date publishDate;

    private Time timeIndication;

    private int splitCount;

    private String description;

    private int lessonID;

    private int classID;
    private int studentID;

    private int teacherID;

    private Boolean homeworkSplittable;

    private Boolean homeworkSubmittable;

    private List<Goal> goalList;

    public Homework() { }

    public Homework(int homeworkID, String homeworkName, Date dueDate, Date publishDate, Time timeIndication,
                    int splitCount, String description, int lessonID, int classID, int studentID, int teacherID,
                    Boolean homeworkSplittable, Boolean homeworkSubmittable, List<Goal> goalList) {
        this.homeworkID = homeworkID;
        this.homeworkName = homeworkName;
        this.dueDate = dueDate;
        this.publishDate = publishDate;
        this.timeIndication = timeIndication;
        this.splitCount = splitCount;
        this.description = description;
        this.lessonID = lessonID;
        this.classID = classID;
        this.studentID = studentID;
        this.teacherID = teacherID;
        this.homeworkSplittable = homeworkSplittable;
        this.homeworkSubmittable = homeworkSubmittable;
        this.goalList = goalList;
    }

    public int getHomeworkID() {
        return homeworkID;
    }

    public void setHomeworkID(int homeworkID) {
        this.homeworkID = homeworkID;
    }

    public String getHomeworkName() {
        return homeworkName;
    }

    public void setHomeworkName(String homeworkName) {
        this.homeworkName = homeworkName;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Time getTimeIndication() {
        return timeIndication;
    }

    public void setTimeIndication(Time timeIndication) {
        this.timeIndication = timeIndication;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getLessonID() {
        return lessonID;
    }

    public void setLessonID(int lessonID) {
        this.lessonID = lessonID;
    }

    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public int getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(int teacherID) {
        this.teacherID = teacherID;
    }

    public Boolean getHomeworkSplittable() {
        return homeworkSplittable;
    }

    public void setHomeworkSplittable(Boolean homeworkSplittable) {
        this.homeworkSplittable = homeworkSplittable;
    }

    public Boolean getHomeworkSubmittable() {
        return homeworkSubmittable;
    }

    public void setHomeworkSubmittable(Boolean homeworkSubmittable) {
        this.homeworkSubmittable = homeworkSubmittable;
    }

    public List<Goal> getGoalList() {
        return goalList;
    }

    public void setGoalList(List<Goal> goalList) {
        this.goalList = goalList;
    }

}