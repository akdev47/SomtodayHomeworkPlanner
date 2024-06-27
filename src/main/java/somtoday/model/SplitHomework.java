package somtoday.model;

import java.sql.Time;
import java.util.Date;

public class SplitHomework {

    int splittedHomeworkId;
    int studentId;
    int homeworkId;
    Time timeIndication;
    boolean accept;
    Date studentCalendarDate;
    String splitName;

    public SplitHomework() {
    }

    public SplitHomework(int splittedHomeworkId, int studentId, int homeworkId, Time timeIndication, boolean accept, Date studentCalendarDate, String splitName) {
        this.splittedHomeworkId = splittedHomeworkId;
        this.studentId = studentId;
        this.homeworkId = homeworkId;
        this.timeIndication = timeIndication;
        this.accept = accept;
        this.studentCalendarDate = studentCalendarDate;
        this.splitName = splitName;
    }

    public int getSplittedHomeworkId() {
        return splittedHomeworkId;
    }

    public void setSplittedHomeworkId(int splittedHomeworkId) {
        this.splittedHomeworkId = splittedHomeworkId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getHomeworkId() {
        return homeworkId;
    }

    public void setHomeworkId(int homeworkId) {
        this.homeworkId = homeworkId;
    }

    public Time getTimeIndication() {
        return timeIndication;
    }

    public void setTimeIndication(Time timeIndication) {
        this.timeIndication = timeIndication;
    }

    public boolean isAccept() {
        return accept;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public Date getStudentCalendarDate() {
        return studentCalendarDate;
    }

    public void setStudentCalendarDate(Date studentCalendarDate) {
        this.studentCalendarDate = studentCalendarDate;
    }

    public String getSplitName() {
        return splitName;
    }

    public void setSplitName(String splitName) {
        this.splitName = splitName;
    }

}
