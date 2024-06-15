package somtoday.model;

import java.util.Date;

public class Teacher extends Person {
    private int teacherID;

    public Teacher() {
        super();
    }
    public Teacher(String personName, int personID, Date birthDate, char personGender, String emailAddress, String userName, String userPassword, int teacherID) {
        super(personName, personID, birthDate, personGender, emailAddress, userName, userPassword);
        this.teacherID = teacherID;
    }


    public int getTeacherID() {
        return teacherID;
    }

    public void setTeacherID(int teacherID) {
        this.teacherID = teacherID;
    }
}

