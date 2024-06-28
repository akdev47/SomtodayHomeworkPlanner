package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.model.Student;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchStudents")
public class FetchStudentsResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchStudents(@QueryParam("unassigned") String unassigned) {
        List<Student> studentList = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT s.student_id, p.person_name " +
                    "FROM somtoday6.Student s " +
                    "JOIN somtoday6.Person p ON s.person_id = p.person_id";

            if ("true".equals(unassigned)) {
                sql += " WHERE s.class_id IS NULL";
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Student student = new Student();
                student.setStudentID(resultSet.getInt("student_id"));
                student.setPersonName(resultSet.getString("person_name"));
                studentList.add(student);
            }

            connection.close();
            return Response.ok(toJson(studentList)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while fetching students.").build();
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
