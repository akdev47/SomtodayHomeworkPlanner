package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.model.Teacher;

@Path("/fetchAllTeachers")
public class FetchAllTeachersResource {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAllTeachers() {
        List<Teacher> teacherList = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT t.teacher_id, p.person_name " +
                    "FROM somtoday6.Teacher t " +
                    "JOIN somtoday6.Person p ON t.person_id = p.person_id";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Teacher teacher = new Teacher();
                teacher.setTeacherID(resultSet.getInt("teacher_id"));
                teacher.setPersonName(resultSet.getString("person_name"));
                teacherList.add(teacher);
            }

            connection.close();
            return Response.ok(toJson(teacherList)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred\"}").build();
        }
    }

    private String toJson(List<Teacher> teacherList) {
        JSONArray jsonArray = new JSONArray();
        for (Teacher teacher : teacherList) {
            JSONObject json = new JSONObject();
            json.put("teacher_id", teacher.getTeacherID());
            json.put("person_name", teacher.getPersonName());
            jsonArray.put(json);
        }
        return jsonArray.toString();
    }
}
