package somtoday.model;

import java.util.Date;

public class Student extends Person {
    private int studentID;
    private int classID;
    private String personName;

    public Student() {
        super();
    }

    public Student(String personName, int personID, Date birthDate, char personGender, String emailAddress, String userName, String userPassword, int studentID, int classID) {
        super(personName, personID, birthDate, personGender, emailAddress, userName, userPassword);
        this.studentID = studentID;
        this.classID = classID;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }


}
