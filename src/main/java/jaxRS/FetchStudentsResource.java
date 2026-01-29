package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;
import somtoday.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchStudents")
public class FetchStudentsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchStudents(@QueryParam("unassigned") String unassigned) {
        List<Student> studentList = new ArrayList<>();

        boolean onlyUnassigned = "true".equalsIgnoreCase(unassigned);

        String sql =
                "SELECT s.student_id, p.person_name " +
                        "FROM student s " +
                        "JOIN person p ON s.person_id = p.person_id " +
                        (onlyUnassigned ? "WHERE s.class_id IS NULL " : "") +
                        "ORDER BY p.person_name ASC";

        try (Connection connection = Db.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Student student = new Student();
                student.setStudentID(rs.getInt("student_id"));
                student.setPersonName(rs.getString("person_name"));
                studentList.add(student);
            }

            return Response.ok(toJson(studentList)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"An error occurred while fetching students.\"}")
                    .build();
        }
    }

    private String toJson(List<Student> studentList) {
        JSONArray jsonArray = new JSONArray();
        for (Student student : studentList) {
            JSONObject json = new JSONObject();
            json.put("student_id", student.getStudentID());
            json.put("person_name", student.getPersonName());
            jsonArray.put(json);
        }
        return jsonArray.toString();
    }
}
