package somtoday.model;

import java.util.Date;

public class SplitHomework {

    private int splitHomeworkID;

    private int studentID;

    private int homeworkID;

    private Byte[] submittedFile;

    private Date submissionDate;

    public SplitHomework() { }

    public SplitHomework(int splitHomeworkID, int studentID, int homeworkID, Byte[] submittedFile, Date submissionDate) {
        this.splitHomeworkID = splitHomeworkID;
        this.studentID = studentID;
        this.homeworkID = homeworkID;
        this.submittedFile = submittedFile;
        this.submissionDate = submissionDate;
    }

    public int getSplitHomeworkID() {
        return splitHomeworkID;
    }

    public void setSplitHomeworkID(int splitHomeworkID) {
        this.splitHomeworkID = splitHomeworkID;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public int getHomeworkID() {
        return homeworkID;
    }

    public void setHomeworkID(int homeworkID) {
        this.homeworkID = homeworkID;
    }

    public Byte[] getSubmittedFile() {
        return submittedFile;
    }

    public void setSubmittedFile(Byte[] submittedFile) {
        this.submittedFile = submittedFile;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }
}
