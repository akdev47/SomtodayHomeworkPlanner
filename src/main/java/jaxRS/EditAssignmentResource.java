package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import somtoday.db.Db;

import java.sql.*;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Path("/editAssignment")
public class EditAssignmentResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssignmentDetails(@QueryParam("homeworkId") int homeworkId) {
        if (homeworkId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Invalid homeworkId\"}")
                    .build();
        }

        try (Connection connection = Db.getConnection()) {

            String fetchHomeworkSql = "SELECT * FROM homework WHERE homework_id = ?";
            JSONObject homeworkData = new JSONObject();

            try (PreparedStatement fetchHomeworkStmt = connection.prepareStatement(fetchHomeworkSql)) {
                fetchHomeworkStmt.setInt(1, homeworkId);

                try (ResultSet homeworkRs = fetchHomeworkStmt.executeQuery()) {
                    if (homeworkRs.next()) {
                        homeworkData.put("homeworkName", homeworkRs.getString("homework_name"));
                        homeworkData.put("dueDate", homeworkRs.getDate("due_date").toString());
                        homeworkData.put("publishDate", homeworkRs.getDate("publish_date").toString());
                        homeworkData.put("timeIndication", homeworkRs.getTime("time_indication").toString());
                        homeworkData.put("description", homeworkRs.getString("description"));
                        homeworkData.put("lessonId", homeworkRs.getInt("lesson_id"));
                        homeworkData.put("classId", homeworkRs.getInt("class_id"));
                        homeworkData.put("homeworkSplittable", homeworkRs.getBoolean("homework_splitable"));
                    } else {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity("{\"error\":\"Homework not found\"}")
                                .build();
                    }
                }
            }

            String fetchGoalsSql = "SELECT goal_name FROM goal WHERE homework_id = ?";
            JSONArray goals = new JSONArray();

            try (PreparedStatement fetchGoalsStmt = connection.prepareStatement(fetchGoalsSql)) {
                fetchGoalsStmt.setInt(1, homeworkId);

                try (ResultSet goalsRs = fetchGoalsStmt.executeQuery()) {
                    while (goalsRs.next()) {
                        JSONObject goal = new JSONObject();
                        goal.put("name", goalsRs.getString("goal_name"));
                        goals.put(goal);
                    }
                }
            }

            homeworkData.put("goals", goals);
            return Response.ok(homeworkData.toString()).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + escapeForJson(e.getMessage()) + "\"}")
                    .build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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

            try (Connection connection = Db.getConnection()) {
                connection.setAutoCommit(false);

                // Fetch original homework to locate ALL matching instances
                String fetchOriginalHomeworkSql =
                        "SELECT homework_name, due_date, publish_date, teacher_id " +
                                "FROM homework WHERE homework_id = ?";

                String originalHomeworkName;
                Date originalDueDate;
                Date originalPublishDate;
                int originalTeacherId;

                try (PreparedStatement stmt = connection.prepareStatement(fetchOriginalHomeworkSql)) {
                    stmt.setInt(1, homeworkId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (!rs.next()) {
                            connection.rollback();
                            return Response.status(Response.Status.NOT_FOUND)
                                    .entity("{\"error\":\"Original homework not found\"}")
                                    .build();
                        }
                        originalHomeworkName = rs.getString("homework_name");
                        originalDueDate = rs.getDate("due_date");
                        originalPublishDate = rs.getDate("publish_date");
                        originalTeacherId = rs.getInt("teacher_id");
                    }
                }

                String fetchHomeworkInstancesSql =
                        "SELECT homework_id " +
                                "FROM homework " +
                                "WHERE homework_name = ? AND due_date = ? AND publish_date = ? AND teacher_id = ?";

                String updateHomeworkSql =
                        "UPDATE homework " +
                                "SET homework_name = ?, due_date = ?, publish_date = ?, time_indication = ?, " +
                                "    description = ?, lesson_id = ?, class_id = ?, homework_splitable = ? " +
                                "WHERE homework_id = ?";

                String deleteGoalsSql = "DELETE FROM goal WHERE homework_id = ?";
                String insertGoalSql = "INSERT INTO goal (homework_id, goal_name) VALUES (?, ?)";

                try (PreparedStatement fetchInstancesStmt = connection.prepareStatement(fetchHomeworkInstancesSql);
                     PreparedStatement updateHomeworkStmt = connection.prepareStatement(updateHomeworkSql);
                     PreparedStatement deleteGoalsStmt = connection.prepareStatement(deleteGoalsSql);
                     PreparedStatement insertGoalStmt = connection.prepareStatement(insertGoalSql)) {

                    fetchInstancesStmt.setString(1, originalHomeworkName);
                    fetchInstancesStmt.setDate(2, originalDueDate);
                    fetchInstancesStmt.setDate(3, originalPublishDate);
                    fetchInstancesStmt.setInt(4, originalTeacherId);

                    try (ResultSet homeworkInstancesRs = fetchInstancesStmt.executeQuery()) {
                        while (homeworkInstancesRs.next()) {
                            int currentHomeworkId = homeworkInstancesRs.getInt("homework_id");

                            // update homework row
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

                            // replace goals
                            deleteGoalsStmt.setInt(1, currentHomeworkId);
                            deleteGoalsStmt.executeUpdate();

                            for (int i = 0; i < goals.length(); i++) {
                                JSONObject goal = goals.getJSONObject(i);
                                String goalName = goal.getString("name");

                                insertGoalStmt.setInt(1, currentHomeworkId);
                                insertGoalStmt.setString(2, goalName);
                                insertGoalStmt.addBatch();
                            }
                            insertGoalStmt.executeBatch();
                        }
                    }
                }

                connection.commit();
                return Response.ok("{\"message\":\"Assignment updated successfully\"}").build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + escapeForJson(e.getMessage()) + "\"}")
                    .build();
        }
    }

    private Time convertMinutesToTime(int minutes) {
        long millis = TimeUnit.MINUTES.toMillis(minutes);
        return new Time(millis - TimeZone.getDefault().getRawOffset());
    }

    private String escapeForJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
