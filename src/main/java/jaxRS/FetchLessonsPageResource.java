package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

@Path("/fetchLessonsPage")
public class FetchLessonsPageResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchLessonsPage(@QueryParam("role") String role, @QueryParam("personId") String personId) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                        "FROM somtoday6.Lesson l " +
                        "JOIN somtoday6.Class c ON l.class_id = c.class_id " +
                        "JOIN somtoday6.Teacher t ON l.teacher_id = t.teacher_id " +
                        "JOIN somtoday6.Person p ON t.person_id = p.person_id " +
                        "WHERE t.person_id = " + personId;
            } else if ("student".equals(role)) {
                sql = "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                        "FROM somtoday6.Lesson l " +
                        "JOIN somtoday6.Class c ON l.class_id = c.class_id " +
                        "JOIN somtoday6.Student s ON c.class_id = s.class_id " +
                        "JOIN somtoday6.Teacher t ON l.teacher_id = t.teacher_id " +
                        "JOIN somtoday6.Person p ON t.person_id = p.person_id " +
                        "WHERE s.person_id = " + personId;
            } else if ("admin".equals(role)) {
                sql = "SELECT l.lesson_id, l.lesson_name, l.lesson_description, c.class_name, p.person_name AS teacher_name " +
                        "FROM somtoday6.Lesson l " +
                        "JOIN somtoday6.Class c ON l.class_id = c.class_id " +
                        "JOIN somtoday6.Teacher t ON l.teacher_id = t.teacher_id " +
                        "JOIN somtoday6.Person p ON t.person_id = p.person_id";
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid role\"}").build();
            }

            ResultSet resultSet = statement.executeQuery(sql);
            JSONArray jsonArray = new JSONArray();

            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("lesson_id", resultSet.getInt("lesson_id"));
                jsonObject.put("lesson_name", resultSet.getString("lesson_name"));
                jsonObject.put("lesson_description", resultSet.getString("lesson_description"));
                jsonObject.put("class_name", resultSet.getString("class_name"));
                jsonObject.put("teacher_name", resultSet.getString("teacher_name"));
                jsonArray.put(jsonObject);
            }

            connection.close();
            return Response.ok(jsonArray.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred\"}").build();
        }
    }
}
