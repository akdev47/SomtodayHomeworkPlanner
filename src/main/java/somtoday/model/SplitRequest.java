package somtoday.model;

public class SplitRequest {

    private int splittedRequestId;

    private int teacherHomeworkId;

    private int homeworkId;

    private int studentId;

    private boolean accepted;

    private String requestDescription;

    public SplitRequest() {

    }

    public SplitRequest(int splittedRequestId, int teacherHomeworkId, int homeworkId, int studentId, boolean accepted, String requestDescription) {
        this.splittedRequestId = splittedRequestId;
        this.teacherHomeworkId = teacherHomeworkId;
        this.homeworkId = homeworkId;
        this.studentId = studentId;
        this.accepted = accepted;
        this.requestDescription = requestDescription;
    }

    public int getSplittedRequestId() {
        return splittedRequestId;
    }

    public void setSplittedRequestId(int splittedRequestId) {
        this.splittedRequestId = splittedRequestId;
    }

    public int getTeacherHomeworkId() {
        return teacherHomeworkId;
    }

    public void setTeacherHomeworkId(int teacherHomeworkId) {
        this.teacherHomeworkId = teacherHomeworkId;
    }

    public int getHomeworkId() {
        return homeworkId;
    }

    public void setHomeworkId(int homeworkId) {
        this.homeworkId = homeworkId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getRequestDescription() {
        return requestDescription;
    }

    public void setRequestDescription(String requestDescription) {
        this.requestDescription = requestDescription;
    }

}
