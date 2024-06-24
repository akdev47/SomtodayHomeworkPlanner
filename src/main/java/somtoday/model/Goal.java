package somtoday.model;

import java.sql.Time;

public class Goal {

    private int goalID;

    private int homeworkID;

    private String goalName;

    private Time timeIndication;

    public Goal() { }

    public Goal(int goalID, int homeworkID, String goalName, Time timeIndication) {
        this.goalID = goalID;
        this.homeworkID = homeworkID;
        this.goalName = goalName;
        this.timeIndication = timeIndication;
    }

    public int getGoalID() {
        return goalID;
    }

    public void setGoalID(int goalID) {
        this.goalID = goalID;
    }

    public int getHomeworkID() {
        return homeworkID;
    }

    public void setHomeworkID(int homeworkID) {
        this.homeworkID = homeworkID;
    }

    public String getGoalName() {
        return goalName;
    }

    public void setGoalName(String goalName) {
        this.goalName = goalName;
    }

    public Time getTimeIndication() {
        return timeIndication;
    }

    public void setTimeIndication(Time timeIndication) {
        this.timeIndication = timeIndication;
    }
}
