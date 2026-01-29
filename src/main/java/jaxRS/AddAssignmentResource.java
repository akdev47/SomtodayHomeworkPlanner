package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import somtoday.db.Db;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.*;

@Path("/addAssignment")
public class AddAssignmentResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addAssignment(String requestBody) {
        try {
            JSONObject jsonObject = new JSONObject(requestBody);

            int personId = jsonObject.getInt("personId");
            String homeworkName = jsonObject.getString("homeworkName");
            Date dueDate = Date.valueOf(jsonObject.getString("dueDate"));
            Date publishDate = Date.valueOf(jsonObject.getString("publishDate"));
            Time timeIndication = convertMinutesToTime(Integer.parseInt(jsonObject.getString("timeIndication")));
            int splitCount = 0;
            String description = jsonObject.getString("description");
            int lessonId = jsonObject.getInt("lessonId");
            int teacherId;
            int classId = jsonObject.getInt("classId");
            boolean homeworkSplittable = jsonObject.getBoolean("homeworkSplittable");
            Date studentCalendarDate = null;

            JSONArray goals = jsonObject.getJSONArray("goals");

            // ✅ connection from env vars (Supabase)
            try (Connection connection = Db.getConnection()) {
                connection.setAutoCommit(false);

                // ✅ Removed "somtoday6." schema prefixes
                String fetchTeacherSql =
                        "SELECT teacher_id, person_name " +
                                "FROM teacher t " +
                                "JOIN person p ON t.person_id = p.person_id " +
                                "WHERE t.person_id = ?";

                String teacherName;
                try (PreparedStatement fetchTeacherStmt = connection.prepareStatement(fetchTeacherSql)) {
                    fetchTeacherStmt.setInt(1, personId);

                    try (ResultSet resultSet = fetchTeacherStmt.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Teacher not found for personId: " + personId);
                        }
                        teacherId = resultSet.getInt("teacher_id");
                        teacherName = resultSet.getString("person_name");
                    }
                }

                String fetchClassNameSql = "SELECT class_name FROM class WHERE class_id = ?";
                String className;
                try (PreparedStatement fetchClassNameStmt = connection.prepareStatement(fetchClassNameSql)) {
                    fetchClassNameStmt.setInt(1, classId);

                    try (ResultSet classResultSet = fetchClassNameStmt.executeQuery()) {
                        if (!classResultSet.next()) {
                            throw new SQLException("Class not found for classId: " + classId);
                        }
                        className = classResultSet.getString("class_name");
                    }
                }

                String fetchStudentsSql = "SELECT student_id, person_id FROM student WHERE class_id = ?";

                String insertHomeworkSql =
                        "INSERT INTO homework " +
                                "(homework_name, due_date, publish_date, time_indication, split_count, description, " +
                                " lesson_id, student_id, class_id, homework_splitable, teacher_id, student_calendar_date) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement fetchStudentsStmt = connection.prepareStatement(fetchStudentsSql);
                     PreparedStatement insertHomeworkStmt = connection.prepareStatement(insertHomeworkSql, Statement.RETURN_GENERATED_KEYS)) {

                    fetchStudentsStmt.setInt(1, classId);

                    try (ResultSet rs = fetchStudentsStmt.executeQuery()) {

                        // create homework for each student
                        while (rs.next()) {
                            int studentId = rs.getInt("student_id");
                            int studentPersonId = rs.getInt("person_id");

                            insertHomeworkStmt.setString(1, homeworkName);
                            insertHomeworkStmt.setDate(2, dueDate);
                            insertHomeworkStmt.setDate(3, publishDate);
                            insertHomeworkStmt.setTime(4, timeIndication);
                            insertHomeworkStmt.setInt(5, splitCount);
                            insertHomeworkStmt.setString(6, description);
                            insertHomeworkStmt.setInt(7, lessonId);
                            insertHomeworkStmt.setInt(8, studentId);
                            insertHomeworkStmt.setInt(9, classId);
                            insertHomeworkStmt.setBoolean(10, homeworkSplittable);
                            insertHomeworkStmt.setInt(11, teacherId);
                            insertHomeworkStmt.setDate(12, studentCalendarDate);
                            insertHomeworkStmt.executeUpdate();

                            int homeworkId;
                            try (ResultSet generatedKeys = insertHomeworkStmt.getGeneratedKeys()) {
                                if (!generatedKeys.next()) {
                                    throw new SQLException("Creating homework failed, no ID obtained.");
                                }
                                homeworkId = generatedKeys.getInt(1);
                            }

                            insertGoals(connection, homeworkId, goals);

                            // send notification
                            insertNotification(connection, studentPersonId,
                                               teacherName + " created an assignment for class: " + className);
                        }
                    }

                    // create a homework instance for the teacher (student_id null)
                    insertHomeworkStmt.setString(1, homeworkName);
                    insertHomeworkStmt.setDate(2, dueDate);
                    insertHomeworkStmt.setDate(3, publishDate);
                    insertHomeworkStmt.setTime(4, timeIndication);
                    insertHomeworkStmt.setInt(5, splitCount);
                    insertHomeworkStmt.setString(6, description);
                    insertHomeworkStmt.setInt(7, lessonId);
                    insertHomeworkStmt.setNull(8, Types.INTEGER);
                    insertHomeworkStmt.setInt(9, classId);
                    insertHomeworkStmt.setBoolean(10, homeworkSplittable);
                    insertHomeworkStmt.setInt(11, teacherId);
                    insertHomeworkStmt.setDate(12, studentCalendarDate);
                    insertHomeworkStmt.executeUpdate();

                    int teacherHomeworkId;
                    try (ResultSet teacherGeneratedKeys = insertHomeworkStmt.getGeneratedKeys()) {
                        if (!teacherGeneratedKeys.next()) {
                            throw new SQLException("Creating teacher homework failed, no ID obtained.");
                        }
                        teacherHomeworkId = teacherGeneratedKeys.getInt(1);
                    }

                    insertGoals(connection, teacherHomeworkId, goals);
                }

                connection.commit();
                return Response.ok("{\"message\":\"Assignment created successfully.\"}").build();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"message\":\"An error occurred: " + escapeForJson(e.getMessage()) + "\"}")
                    .build();
        }
    }

    private void insertGoals(Connection connection, int homeworkId, JSONArray goals) throws SQLException {
        String insertGoalSql = "INSERT INTO goal (homework_id, goal_name) VALUES (?, ?)";
        try (PreparedStatement insertGoalStmt = connection.prepareStatement(insertGoalSql)) {
            for (int i = 0; i < goals.length(); i++) {
                JSONObject goal = goals.getJSONObject(i);
                String goalName = goal.getString("name");

                insertGoalStmt.setInt(1, homeworkId);
                insertGoalStmt.setString(2, goalName);
                insertGoalStmt.addBatch();
            }
            insertGoalStmt.executeBatch();
        }
    }

    private void insertNotification(Connection connection, int personId, String info) throws SQLException {
        String sql = "INSERT INTO notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, "Teacher");
            pstmt.setString(3, info);
            pstmt.setInt(4, personId);
            pstmt.executeUpdate();
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
