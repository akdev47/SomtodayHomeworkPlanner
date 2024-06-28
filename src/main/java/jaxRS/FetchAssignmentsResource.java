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
import somtoday.model.Homework;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Path("/fetchAssignments")
public class FetchAssignmentsResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAssignments(@QueryParam("role") String role, @QueryParam("personId") String personId) {
        List<Class> classList = new ArrayList<>();
        List<Homework> assignmentList = new ArrayList<>();
        List<JSONObject> splitList = new ArrayList<>(); // Placeholder for splits data

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date " +
                        "FROM homework h " +
                        "JOIN class c ON h.class_id = c.class_id " +
                        "JOIN teacher t ON h.teacher_id = t.teacher_id " +
                        "WHERE h.student_id IS NULL " +
                        "AND t.person_id = " + personId +
                        " ORDER BY h.publish_date DESC;";
            } else if ("student".equals(role)) {
                sql = "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date " +
                        "FROM homework h, student s, person p, class c " +
                        "WHERE h.student_id = s.student_id " +
                        "AND h.class_id = c.class_id " +
                        "AND s.person_id = " + personId +
                        " ORDER BY h.publish_date DESC;";
            } else if ("admin".equals(role)) {
                sql = "SELECT DISTINCT c.class_name, h.homework_id, h.homework_name, h.publish_date, h.due_date " +
                        "FROM homework h " +
                        "JOIN class c ON h.class_id = c.class_id " +
                        "WHERE h.student_id IS NULL " +
                        "ORDER BY h.publish_date DESC;";
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Invalid role\"}").build();
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Class c = new Class();
                c.setClassName(resultSet.getString("class_name"));
                Homework hw = new Homework();
                hw.setHomeworkID(resultSet.getInt("homework_id"));
                hw.setHomeworkName(resultSet.getString("homework_name"));
                hw.setPublishDate(resultSet.getDate("publish_date"));
                hw.setDueDate(resultSet.getDate("due_date"));
                classList.add(c);
                assignmentList.add(hw);
            }

            connection.close();

            JSONArray homeworksArray = new JSONArray();
            for (int i = 0; i < assignmentList.size(); i++) {
                JSONObject homeworkJson = new JSONObject();
                homeworkJson.put("class_name", classList.get(i).getClassName());
                homeworkJson.put("homework_id", assignmentList.get(i).getHomeworkID());
                homeworkJson.put("homework_name", assignmentList.get(i).getHomeworkName());
                homeworkJson.put("publish_date", assignmentList.get(i).getPublishDate());
                homeworkJson.put("due_date", assignmentList.get(i).getDueDate());
                homeworksArray.put(homeworkJson);
            }

            JSONObject responseJson = new JSONObject();
            responseJson.put("homeworks", homeworksArray);
            responseJson.put("splits", new JSONArray(splitList)); // Placeholder for splits data

            return Response.ok(responseJson.toString()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"error\":\"An error occurred\"}").build();
        }
    }
}
