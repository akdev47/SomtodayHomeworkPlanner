package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.model.Lesson;
import somtoday.model.Student;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchClassDetails")
public class FetchClassDetailsResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchClassDetails(@QueryParam("classId") String classId) {
        if (classId == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Class ID is required\"}").build();
        }

        List<Student> students = new ArrayList<>();
        List<Lesson> lessons = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
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
            return Response.ok(toJson(students, lessons)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred\"}").build();
        }
    }

    private String toJson(List<Student> students, List<Lesson> lessons) {
        JSONObject jsonObject = new JSONObject();
        JSONArray studentsArray = new JSONArray();
        for (Student student : students) {
            JSONObject studentJson = new JSONObject();
            studentJson.put("student_id", student.getStudentID());
            studentJson.put("person_id", student.getPersonID());
            studentJson.put("person_name", student.getPersonName());
            studentsArray.put(studentJson);
        }
        jsonObject.put("students", studentsArray);

        JSONArray lessonsArray = new JSONArray();
        for (Lesson lesson : lessons) {
            JSONObject lessonJson = new JSONObject();
            lessonJson.put("lesson_id", lesson.getLessonID());
            lessonJson.put("lesson_name", lesson.getLessonName());
            lessonJson.put("lesson_description", lesson.getLessonDescription());
            lessonsArray.put(lessonJson);
        }
        jsonObject.put("lessons", lessonsArray);

        return jsonObject.toString();
    }
}
