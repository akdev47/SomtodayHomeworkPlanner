package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;
import somtoday.model.Lesson;
import somtoday.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchClassDetails")
public class FetchClassDetailsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchClassDetails(@QueryParam("classId") String classId) {
        if (classId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Class ID is required\"}")
                    .build();
        }

        int classIdInt;
        try {
            classIdInt = Integer.parseInt(classId);
        } catch (NumberFormatException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"classId must be a number\"}")
                    .build();
        }

        List<Student> students = new ArrayList<>();
        List<Lesson> lessons = new ArrayList<>();

        try (Connection connection = Db.getConnection()) {

            // Fetch students
            String studentSql =
                    "SELECT s.student_id, p.person_id, p.person_name " +
                            "FROM student s " +
                            "JOIN person p ON s.person_id = p.person_id " +
                            "WHERE s.class_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(studentSql)) {
                stmt.setInt(1, classIdInt);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Student student = new Student();
                        student.setStudentID(rs.getInt("student_id"));
                        student.setPersonID(rs.getInt("person_id"));
                        student.setPersonName(rs.getString("person_name"));
                        students.add(student);
                    }
                }
            }

            // Fetch lessons
            String lessonSql =
                    "SELECT lesson_id, lesson_name, lesson_description " +
                            "FROM lesson " +
                            "WHERE class_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(lessonSql)) {
                stmt.setInt(1, classIdInt);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Lesson lesson = new Lesson();
                        lesson.setLessonID(rs.getInt("lesson_id"));
                        lesson.setLessonName(rs.getString("lesson_name"));
                        lesson.setLessonDescription(rs.getString("lesson_description"));
                        lessons.add(lesson);
                    }
                }
            }

            return Response.ok(toJson(students, lessons)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred\"}")
                    .build();
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
