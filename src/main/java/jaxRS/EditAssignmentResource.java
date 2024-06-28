package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Path("/editAssignment")
public class EditAssignmentResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssignmentDetails(@QueryParam("homeworkId") int homeworkId) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String fetchHomeworkSql = "SELECT * FROM somtoday6.Homework WHERE homework_id = ?";
            PreparedStatement fetchHomeworkStmt = connection.prepareStatement(fetchHomeworkSql);
            fetchHomeworkStmt.setInt(1, homeworkId);
            ResultSet homeworkRs = fetchHomeworkStmt.executeQuery();

            JSONObject homeworkData = new JSONObject();
            if (homeworkRs.next()) {
                homeworkData.put("homeworkName", homeworkRs.getString("homework_name"));
                homeworkData.put("dueDate", homeworkRs.getDate("due_date").toString());
                homeworkData.put("publishDate", homeworkRs.getDate("publish_date").toString());
                homeworkData.put("timeIndication", homeworkRs.getTime("time_indication").toString());
                homeworkData.put("description", homeworkRs.getString("description"));
                homeworkData.put("lessonId", homeworkRs.getInt("lesson_id"));
                homeworkData.put("classId", homeworkRs.getInt("class_id"));
                homeworkData.put("homeworkSplittable", homeworkRs.getBoolean("homework_splitable"));
            }

            String fetchGoalsSql = "SELECT goal_name FROM somtoday6.goal WHERE homework_id = ?";
            PreparedStatement fetchGoalsStmt = connection.prepareStatement(fetchGoalsSql);
            fetchGoalsStmt.setInt(1, homeworkId);
            ResultSet goalsRs = fetchGoalsStmt.executeQuery();

            JSONArray goals = new JSONArray();
            while (goalsRs.next()) {
                JSONObject goal = new JSONObject();
                goal.put("name", goalsRs.getString("goal_name"));
                goals.put(goal);
            }
            homeworkData.put("goals", goals);

            connection.close();

            return Response.ok(homeworkData.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred: " + e.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAssignment(String requestBody) {
        try {
            JSONObject jsonObject = new JSONObject(requestBody);

            int homeworkId = jsonObject.getInt("homeworkId");
            String homeworkName = jsonObject.getString("homeworkName");
            Date dueDate = Date.valueOf(jsonObject.getString("dueDate"));
            Date publishDate = Date.valueOf(jsonObject.getString("publishDate"));
            Time timeIndication = convertMinutesToTime(jsonObject.getInt("timeIndication"));
            String description = jsonObject.getString("description");
            int lessonId = jsonObject.getInt("lessonId");
            int classId = jsonObject.getInt("classId");
            boolean homeworkSplittable = jsonObject.getBoolean("homeworkSplittable");

            JSONArray goals = jsonObject.getJSONArray("goals");

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);

            // Fetch the original homework data to identify the relevant homework instances
            String fetchOriginalHomeworkSql = "SELECT homework_name, due_date, publish_date, teacher_id FROM somtoday6.Homework WHERE homework_id = ?";
            PreparedStatement fetchOriginalHomeworkStmt = connection.prepareStatement(fetchOriginalHomeworkSql);
            fetchOriginalHomeworkStmt.setInt(1, homeworkId);
            ResultSet originalHomeworkRs = fetchOriginalHomeworkStmt.executeQuery();

            if (originalHomeworkRs.next()) {
                String originalHomeworkName = originalHomeworkRs.getString("homework_name");
                Date originalDueDate = originalHomeworkRs.getDate("due_date");
                Date originalPublishDate = originalHomeworkRs.getDate("publish_date");
                int originalTeacherId = originalHomeworkRs.getInt("teacher_id");

                // Fetch the homework instances to be updated
                String fetchHomeworkInstancesSql = "SELECT homework_id FROM somtoday6.Homework WHERE homework_name = ? AND due_date = ? AND publish_date = ? AND teacher_id = ?";
                PreparedStatement fetchHomeworkInstancesStmt = connection.prepareStatement(fetchHomeworkInstancesSql);
                fetchHomeworkInstancesStmt.setString(1, originalHomeworkName);
                fetchHomeworkInstancesStmt.setDate(2, originalDueDate);
                fetchHomeworkInstancesStmt.setDate(3, originalPublishDate);
                fetchHomeworkInstancesStmt.setInt(4, originalTeacherId);
                ResultSet homeworkInstancesRs = fetchHomeworkInstancesStmt.executeQuery();

                while (homeworkInstancesRs.next()) {
                    int currentHomeworkId = homeworkInstancesRs.getInt("homework_id");

                    // Update the homework instance
                    String updateHomeworkSql = "UPDATE somtoday6.Homework SET homework_name = ?, due_date = ?, publish_date = ?, time_indication = ?, description = ?, lesson_id = ?, class_id = ?, homework_splitable = ? WHERE homework_id = ?";
                    PreparedStatement updateHomeworkStmt = connection.prepareStatement(updateHomeworkSql);
                    updateHomeworkStmt.setString(1, homeworkName);
                    updateHomeworkStmt.setDate(2, dueDate);
                    updateHomeworkStmt.setDate(3, publishDate);
                    updateHomeworkStmt.setTime(4, timeIndication);
                    updateHomeworkStmt.setString(5, description);
                    updateHomeworkStmt.setInt(6, lessonId);
                    updateHomeworkStmt.setInt(7, classId);
                    updateHomeworkStmt.setBoolean(8, homeworkSplittable);
                    updateHomeworkStmt.setInt(9, currentHomeworkId);
                    updateHomeworkStmt.executeUpdate();

                    // Delete old goals
                    String deleteGoalsSql = "DELETE FROM somtoday6.goal WHERE homework_id = ?";
                    PreparedStatement deleteGoalsStmt = connection.prepareStatement(deleteGoalsSql);
                    deleteGoalsStmt.setInt(1, currentHomeworkId);
                    deleteGoalsStmt.executeUpdate();

                    // Insert new goals
                    for (int i = 0; i < goals.length(); i++) {
                        JSONObject goal = goals.getJSONObject(i);
                        String goalName = goal.getString("name");

                        String insertGoalSql = "INSERT INTO somtoday6.goal (homework_id, goal_name) VALUES (?, ?)";
                        PreparedStatement insertGoalStmt = connection.prepareStatement(insertGoalSql);
                        insertGoalStmt.setInt(1, currentHomeworkId);
                        insertGoalStmt.setString(2, goalName);
                        insertGoalStmt.executeUpdate();
                    }
                }

                connection.commit();
                connection.close();
                return Response.ok().entity("Assignment updated successfully").build();
            } else {
                throw new SQLException("Original homework not found for homeworkId: " + homeworkId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred: " + e.getMessage()).build();
        }
    }

    private Time convertMinutesToTime(int minutes) {
        long millis = TimeUnit.MINUTES.toMillis(minutes);
        return new Time(millis - TimeZone.getDefault().getRawOffset());
    }
}
