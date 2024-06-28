package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.json.*;

@Path("/splitHomeworkTeacher")
public class SplitHomeworkTeacherResource {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response splitHomeworkTeacher(@QueryParam("homeworkId") int homeworkId, String requestBody) {
        try {
            JSONObject jsonObject = new JSONObject(requestBody);
            int splitCount = jsonObject.getInt("split_count");
            JSONArray splits = jsonObject.getJSONArray("splits");

            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String selectHomeworkQuery = "SELECT homework_name, teacher_id, publish_date, due_date FROM homework WHERE homework_id = ?";
            PreparedStatement stmt = connection.prepareStatement(selectHomeworkQuery);
            stmt.setInt(1, homeworkId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"message\": \"Invalid homework_id\"}").build();
            }

            String originalHomeworkName = rs.getString("homework_name");
            int originalTeacherId = rs.getInt("teacher_id");
            Date originalPublishDate = rs.getDate("publish_date");
            Date originalDueDate = rs.getDate("due_date");

            String fetchHomeworkInstancesSql = "SELECT h.homework_id, h.student_id, h.split_count FROM somtoday6.Homework h WHERE h.homework_name = ? AND h.due_date = ? AND h.publish_date = ? AND h.teacher_id = ?";
            PreparedStatement fetchHomeworkInstancesStmt = connection.prepareStatement(fetchHomeworkInstancesSql);
            fetchHomeworkInstancesStmt.setString(1, originalHomeworkName);
            fetchHomeworkInstancesStmt.setDate(2, originalDueDate);
            fetchHomeworkInstancesStmt.setDate(3, originalPublishDate);
            fetchHomeworkInstancesStmt.setInt(4, originalTeacherId);
            ResultSet homeworkInstancesRs = fetchHomeworkInstancesStmt.executeQuery();

            while (homeworkInstancesRs.next()) {
                String insertSplitHomeworkQuery = "INSERT INTO splitted_homework (homework_id, student_id, time_indication, accepted, student_calendar_date, split_name) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertSplitHomeworkQuery);

                for (int i = 0; i < splits.length(); i++) {
                    JSONObject split = splits.getJSONObject(i);
                    Time duration = convertMinutesToTime(split.getInt("duration"));
                    int studentId = homeworkInstancesRs.getInt("student_id");
                    Date studentCalendarDate = null;
                    boolean accepted = true;
                    String splitName = split.getString("name");

                    insertStmt.setInt(1, homeworkInstancesRs.getInt("homework_id"));

                    if (studentId != 0) {
                        insertStmt.setInt(2, studentId);
                    } else {
                        insertStmt.setNull(2, Types.INTEGER);
                    }

                    insertStmt.setTime(3, duration);
                    insertStmt.setBoolean(4, accepted);
                    insertStmt.setDate(5, studentCalendarDate);
                    insertStmt.setString(6, splitName);

                    insertStmt.addBatch();
                }

                String updateSplitCountQuery = "UPDATE homework SET split_count = ? WHERE homework_id = ?";
                PreparedStatement updateStmt = connection.prepareStatement(updateSplitCountQuery);
                updateStmt.setInt(1, splitCount);
                updateStmt.setInt(2, homeworkInstancesRs.getInt("homework_id"));
                updateStmt.executeUpdate();

                insertStmt.executeBatch();
            }

            connection.close();

            return Response.ok("{\"message\": \"Split homeworks created successfully.\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\": \"An error occurred: " + e.getMessage() + "\"}").build();
        }
    }

    private Time convertMinutesToTime(int minutes) {
        long millis = TimeUnit.MINUTES.toMillis(minutes);
        return new Time(millis - TimeZone.getDefault().getRawOffset());
    }
}
