package somtoday.model;

import java.util.Date;

public class Person {

    String personName;
    int personID;
    Date birthDate;
    char personGender;
    String emailAddress;
    String userName;
    String userPassword;


    public Person() {
    }

    public Person(String personName, int personID, Date birthDate, char personGender, String emailAddress, String userName, String userPassword) {
        this.personName = personName;
        this.personID = personID;
        this.birthDate = birthDate;
        this.personGender = personGender;
        this.emailAddress = emailAddress;
        this.userName = userName;
        this.userPassword = userPassword;

    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public int getPersonID() {
        return personID;
    }

    public void setPersonID(int personID) {
        this.personID = personID;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public char getPersonGender() {
        return personGender;
    }

    public void setPersonGender(char personGender) {
        this.personGender = personGender;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }


}
