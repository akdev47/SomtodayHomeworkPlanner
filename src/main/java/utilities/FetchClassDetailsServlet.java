package utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import somtoday.model.Person;
import somtoday.model.Student;
import somtoday.model.Lesson;

@WebServlet("/fetchClassDetails")
public class FetchClassDetailsServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("Fetching class details...");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String classId = request.getParameter("classId");

        if (classId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Class ID is required");
            return;
        }

        List<Student> students = new ArrayList<>();
        List<Lesson> lessons = new ArrayList<>();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Fetch students
            String studentSql = "SELECT s.student_id, p.person_id, p.person_name FROM somtoday6.Student s " +
                    "JOIN somtoday6.Person p ON s.person_id = p.person_id " +
                    "WHERE s.class_id = " + classId;
            Statement studentStatement = connection.createStatement();
            ResultSet studentResultSet = studentStatement.executeQuery(studentSql);
            while (studentResultSet.next()) {
                Student student = new Student();
                student.setStudentID(studentResultSet.getInt("student_id"));
                student.setPersonID(studentResultSet.getInt("person_id"));
                student.setPersonName(studentResultSet.getString("person_name"));
                students.add(student);
            }

            // Fetch lessons
            String lessonSql = "SELECT l.lesson_id, l.lesson_name, l.lesson_description FROM somtoday6.Lesson l " +
                    "WHERE l.class_id = " + classId;
            Statement lessonStatement = connection.createStatement();
            ResultSet lessonResultSet = lessonStatement.executeQuery(lessonSql);
            while (lessonResultSet.next()) {
                Lesson lesson = new Lesson();
                lesson.setLessonID(lessonResultSet.getInt("lesson_id"));
                lesson.setLessonName(lessonResultSet.getString("lesson_name"));
                lesson.setLessonDescription(lessonResultSet.getString("lesson_description"));
                lessons.add(lesson);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(students, lessons));
        out.close();
    }

    private String toJson(List<Student> students, List<Lesson> lessons) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"students\": [");
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            json.append("{");
            json.append("\"student_id\":").append(student.getStudentID()).append(",");
            json.append("\"person_id\":").append(student.getPersonID()).append(",");
            json.append("\"person_name\":\"").append(student.getPersonName()).append("\"");
            json.append("}");
            if (i < students.size() - 1) {
                json.append(",");
            }
        }
        json.append("],");

        json.append("\"lessons\": [");
        for (int i = 0; i < lessons.size(); i++) {
            Lesson lesson = lessons.get(i);
            json.append("{");
            json.append("\"lesson_id\":").append(lesson.getLessonID()).append(",");
            json.append("\"lesson_name\":\"").append(lesson.getLessonName()).append("\",");
            json.append("\"lesson_description\":\"").append(lesson.getLessonDescription()).append("\"");
            json.append("}");
            if (i < lessons.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        json.append("}");
        return json.toString();
    }
}
