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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/fetchHomework")
public class FetchHomeworkResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchHomework(@QueryParam("personId") int personId, @QueryParam("role") String role) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "";
            if ("student".equals(role)) {
                sql = "SELECT homework_id, homework_name, due_date, description, time_indication FROM somtoday6.Homework WHERE student_id = (SELECT student_id FROM somtoday6.Student WHERE person_id = ?)";
            } else if ("teacher".equals(role)) {
                sql = "SELECT homework_id, homework_name, due_date, description, time_indication FROM somtoday6.Homework WHERE teacher_id = (SELECT teacher_id FROM somtoday6.Teacher WHERE person_id = ?)";
            } else {
                connection.close();
                return Response.ok(new JSONArray().toString()).build(); // nothing for admins
            }

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, personId);
            ResultSet rs = preparedStatement.executeQuery();

            JSONArray homeworkArray = new JSONArray();

            while (rs.next()) {
                JSONObject homework = new JSONObject();
                homework.put("homework_id", rs.getInt("homework_id"));
                homework.put("homework_name", rs.getString("homework_name"));
                homework.put("due_date", rs.getDate("due_date"));
                homework.put("description", rs.getString("description"));
                homework.put("time_indication", rs.getString("time_indication"));
                homeworkArray.put(homework);
            }

            connection.close();
            return Response.ok(homeworkArray.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred\"}").build();
        }
    }
}

