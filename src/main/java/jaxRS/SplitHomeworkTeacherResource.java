package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.json.*;

import somtoday.db.Db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Types;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Path("/splitHomeworkTeacher")
public class SplitHomeworkTeacherResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response splitHomeworkTeacher(@QueryParam("homeworkId") int homeworkId, String requestBody) {
        if (homeworkId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Missing/invalid homeworkId\"}")
                    .build();
        }

        try {
            JSONObject jsonObject = new JSONObject(requestBody);
            int splitCount = jsonObject.getInt("split_count");
            JSONArray splits = jsonObject.getJSONArray("splits");

            if (splits.length() == 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\":\"splits array cannot be empty\"}")
                        .build();
            }

            try (Connection connection = Db.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // 1) Load original homework (used to find all matching instances)
                    String selectHomeworkSql =
                            "SELECT homework_name, teacher_id, publish_date, due_date " +
                                    "FROM homework WHERE homework_id = ?";

                    String originalHomeworkName;
                    int originalTeacherId;
                    Date originalPublishDate;
                    Date originalDueDate;

                    try (PreparedStatement stmt = connection.prepareStatement(selectHomeworkSql)) {
                        stmt.setInt(1, homeworkId);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (!rs.next()) {
                                connection.rollback();
                                return Response.status(Response.Status.BAD_REQUEST)
                                        .entity("{\"message\":\"Invalid homework_id\"}")
                                        .build();
                            }

                            originalHomeworkName = rs.getString("homework_name");
                            originalTeacherId = rs.getInt("teacher_id");
                            originalPublishDate = rs.getDate("publish_date");
                            originalDueDate = rs.getDate("due_date");
                        }
                    }

                    // 2) Fetch all homework instances (teacher + all students) that match this "assignment"
                    String fetchHomeworkInstancesSql =
                            "SELECT homework_id, student_id " +
                                    "FROM homework " +
                                    "WHERE homework_name = ? AND due_date = ? AND publish_date = ? AND teacher_id = ?";

                    String insertSplitHomeworkSql =
                            "INSERT INTO splitted_homework (homework_id, student_id, time_indication, accepted, student_calendar_date, split_name) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";

                    String updateSplitCountSql =
                            "UPDATE homework SET split_count = ? WHERE homework_id = ?";

                    try (PreparedStatement fetchStmt = connection.prepareStatement(fetchHomeworkInstancesSql);
                         PreparedStatement insertSplitStmt = connection.prepareStatement(insertSplitHomeworkSql);
                         PreparedStatement updateSplitCountStmt = connection.prepareStatement(updateSplitCountSql)) {

                        fetchStmt.setString(1, originalHomeworkName);
                        fetchStmt.setDate(2, originalDueDate);
                        fetchStmt.setDate(3, originalPublishDate);
                        fetchStmt.setInt(4, originalTeacherId);

                        try (ResultSet rs = fetchStmt.executeQuery()) {
                            while (rs.next()) {
                                int currentHomeworkId = rs.getInt("homework_id");
                                Integer studentId = (Integer) rs.getObject("student_id"); // null if teacher

                                // Insert splits for this homework instance
                                for (int i = 0; i < splits.length(); i++) {
                                    JSONObject split = splits.getJSONObject(i);

                                    int durationMinutes = split.getInt("duration");
                                    String splitName = split.getString("name");

                                    Time duration = convertMinutesToTime(durationMinutes);

                                    insertSplitStmt.setInt(1, currentHomeworkId);

                                    if (studentId != null) {
                                        insertSplitStmt.setInt(2, studentId);
                                    } else {
                                        insertSplitStmt.setNull(2, Types.INTEGER);
                                    }

                                    insertSplitStmt.setTime(3, duration);
                                    insertSplitStmt.setBoolean(4, true); // teacher-created splits are auto-accepted
                                    insertSplitStmt.setDate(5, null);    // kept null like your original
                                    insertSplitStmt.setString(6, splitName);

                                    insertSplitStmt.addBatch();
                                }

                                // Update split_count on homework instance
                                updateSplitCountStmt.setInt(1, splitCount);
                                updateSplitCountStmt.setInt(2, currentHomeworkId);
                                updateSplitCountStmt.addBatch();
                            }
                        }

                        insertSplitStmt.executeBatch();
                        updateSplitCountStmt.executeBatch();
                    }

                    connection.commit();
                    return Response.ok("{\"message\":\"Split homeworks created successfully.\"}").build();

                } catch (Exception inner) {
                    connection.rollback();
                    inner.printStackTrace();
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"message\":\"An error occurred: " + inner.getMessage() + "\"}")
                            .build();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\":\"An error occurred: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    private Time convertMinutesToTime(int minutes) {
        long millis = TimeUnit.MINUTES.toMillis(minutes);
        return new Time(millis - TimeZone.getDefault().getRawOffset());
    }
}
