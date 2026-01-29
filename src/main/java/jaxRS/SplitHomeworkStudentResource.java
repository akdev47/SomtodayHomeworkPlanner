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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Path("/splitHomeworkStudent")
public class SplitHomeworkStudentResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response splitHomeworkStudent(@QueryParam("homeworkId") int homeworkId, String requestBody) {
        if (homeworkId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Missing/invalid homeworkId\"}")
                    .build();
        }

        try {
            JSONObject jsonObject = new JSONObject(requestBody);

            int splitCount = jsonObject.getInt("split_count"); // currently not used, but kept for compatibility
            String description = jsonObject.getString("description");
            JSONArray splits = jsonObject.getJSONArray("splits");

            if (splits.length() == 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"message\":\"splits array cannot be empty\"}")
                        .build();
            }

            try (Connection connection = Db.getConnection()) {
                connection.setAutoCommit(false);

                try {
                    // 1) Load the original homework
                    String selectHomeworkSql =
                            "SELECT homework_name, teacher_id, publish_date, due_date, student_id " +
                                    "FROM homework " +
                                    "WHERE homework_id = ?";

                    String originalHomeworkName;
                    int originalTeacherId;
                    Date originalPublishDate;
                    Date originalDueDate;
                    int studentId;

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
                            studentId = rs.getInt("student_id");
                        }
                    }

                    // 2) Find the teacher's "master" homework (same name/dates/teacher, but student_id IS NULL)
                    int teacherHomeworkId = 0;
                    String fetchTeacherHomeworkSql =
                            "SELECT homework_id " +
                                    "FROM homework " +
                                    "WHERE homework_name = ? AND due_date = ? AND publish_date = ? AND teacher_id = ? AND student_id IS NULL";

                    try (PreparedStatement stmt = connection.prepareStatement(fetchTeacherHomeworkSql)) {
                        stmt.setString(1, originalHomeworkName);
                        stmt.setDate(2, originalDueDate);
                        stmt.setDate(3, originalPublishDate);
                        stmt.setInt(4, originalTeacherId);

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                teacherHomeworkId = rs.getInt("homework_id");
                            }
                        }
                    }

                    if (teacherHomeworkId == 0) {
                        connection.rollback();
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity("{\"message\":\"Teacher homework instance not found for this homework.\"}")
                                .build();
                    }

                    // 3) Create split_request row
                    String insertSplitRequestSql =
                            "INSERT INTO split_request (teacher_homework_id, homework_id, student_id, request_description, accepted) " +
                                    "VALUES (?, ?, ?, ?, ?)";

                    try (PreparedStatement insertReqStmt = connection.prepareStatement(insertSplitRequestSql)) {
                        insertReqStmt.setInt(1, teacherHomeworkId);
                        insertReqStmt.setInt(2, homeworkId);
                        insertReqStmt.setInt(3, studentId);
                        insertReqStmt.setString(4, description);
                        insertReqStmt.setBoolean(5, false);
                        insertReqStmt.executeUpdate();
                    }

                    // 4) Create splitted_homework rows (batch insert)
                    String insertSplitHomeworkSql =
                            "INSERT INTO splitted_homework (homework_id, student_id, time_indication, accepted, student_calendar_date, split_name) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSplitHomeworkSql)) {
                        for (int i = 0; i < splits.length(); i++) {
                            JSONObject split = splits.getJSONObject(i);

                            int durationMinutes = split.getInt("duration");
                            String splitName = split.getString("name");

                            Time duration = convertMinutesToTime(durationMinutes);
                            Date studentCalendarDate = null; // kept as null like your current behavior
                            boolean accepted = false;

                            insertStmt.setInt(1, homeworkId);
                            insertStmt.setInt(2, studentId);
                            insertStmt.setTime(3, duration);
                            insertStmt.setBoolean(4, accepted);
                            insertStmt.setDate(5, studentCalendarDate);
                            insertStmt.setString(6, splitName);

                            insertStmt.addBatch();
                        }

                        insertStmt.executeBatch();
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
