package jaxRS;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.model.Class;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchClasses")
public class FetchClassesResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchClasses(@QueryParam("role") String role, @QueryParam("personId") String personId) {
        List<Class> classList = new ArrayList<>();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT DISTINCT c.class_id, c.class_name, c.class_capacity, c.profile_picture IS NOT NULL AS has_profile_picture " +
                        "FROM somtoday6.Class c " +
                        "JOIN somtoday6.Lesson l ON c.class_id = l.class_id " +
                        "JOIN somtoday6.Teacher t ON l.teacher_id = t.teacher_id " +
                        "WHERE t.person_id = " + personId;
            } else if ("student".equals(role)) {
                sql = "SELECT DISTINCT c.class_id, c.class_name, c.class_capacity, c.profile_picture IS NOT NULL AS has_profile_picture " +
                        "FROM somtoday6.Class c " +
                        "JOIN somtoday6.Student s ON c.class_id = s.class_id " +
                        "WHERE s.person_id = " + personId;
            } else if ("admin".equals(role)) {
                sql = "SELECT c.class_id, c.class_name, c.class_capacity, c.profile_picture IS NOT NULL AS has_profile_picture FROM somtoday6.Class c";
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid role\"}").build();
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Class cls = new Class();
                cls.setClassID(resultSet.getInt("class_id"));
                cls.setClassName(resultSet.getString("class_name"));
                cls.setClassCapacity(resultSet.getInt("class_capacity"));
                cls.setHasProfilePicture(resultSet.getBoolean("has_profile_picture"));
                classList.add(cls);
            }

            connection.close();
            return Response.ok(toJson(classList)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred\"}").build();
        }
    }

    private String toJson(List<Class> classList) {
        JSONArray jsonArray = new JSONArray();
        for (Class cls : classList) {
            JSONObject json = new JSONObject();
            json.put("class_id", cls.getClassID());
            json.put("class_name", cls.getClassName());
            json.put("class_capacity", cls.getClassCapacity());
            json.put("has_profile_picture", cls.isHasProfilePicture());
            jsonArray.put(json);
        }
        return jsonArray.toString();
    }
}
