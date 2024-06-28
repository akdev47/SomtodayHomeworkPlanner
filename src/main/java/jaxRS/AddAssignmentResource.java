package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.json.*;

@Path("/addAssignment")
public class AddAssignmentResource {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

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
            int splitCount = 0; // Set to 0 as per new requirement
            String description = jsonObject.getString("description");
            int lessonId = jsonObject.getInt("lessonId");
            int teacherId = 0; // Will be fetched from DB
            int classId = jsonObject.getInt("classId");
            boolean homeworkSplittable = jsonObject.getBoolean("homeworkSplittable");
            Date studentCalendarDate = null; // Set to null as per new requirement

            JSONArray goals = jsonObject.getJSONArray("goals");

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);

            // Fetch teacher information
            String fetchTeacherSql = "SELECT teacher_id, person_name FROM somtoday6.teacher t JOIN somtoday6.person p ON t.person_id = p.person_id WHERE t.person_id = ?";
            PreparedStatement fetchTeacherStmt = connection.prepareStatement(fetchTeacherSql);
            fetchTeacherStmt.setInt(1, personId);
            ResultSet resultSet = fetchTeacherStmt.executeQuery();

            String teacherName = "";
            if (resultSet.next()) {
                teacherId = resultSet.getInt("teacher_id");
                teacherName = resultSet.getString("person_name");
            } else {
                throw new SQLException("Teacher not found for personId: " + personId);
            }

            // Fetch class name
            String fetchClassNameSql = "SELECT class_name FROM somtoday6.class WHERE class_id = ?";
            PreparedStatement fetchClassNameStmt = connection.prepareStatement(fetchClassNameSql);
            fetchClassNameStmt.setInt(1, classId);
            ResultSet classResultSet = fetchClassNameStmt.executeQuery();

            String className = "";
            if (classResultSet.next()) {
                className = classResultSet.getString("class_name");
            } else {
                throw new SQLException("Class not found for classId: " + classId);
            }

            // Fetch students
            String fetchStudentsSql = "SELECT student_id, person_id FROM somtoday6.student WHERE class_id = ?";
            PreparedStatement fetchStudentsStmt = connection.prepareStatement(fetchStudentsSql);
            fetchStudentsStmt.setInt(1, classId);
            ResultSet rs = fetchStudentsStmt.executeQuery();

            String insertHomeworkSql = "INSERT INTO somtoday6.Homework (homework_name, due_date, publish_date, time_indication, split_count, description, lesson_id, student_id, class_id, homework_splitable, teacher_id, student_calendar_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertHomeworkStmt = connection.prepareStatement(insertHomeworkSql, Statement.RETURN_GENERATED_KEYS);

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

                ResultSet generatedKeys = insertHomeworkStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int homeworkId = generatedKeys.getInt(1);

                    for (int i = 0; i < goals.length(); i++) {
                        JSONObject goal = goals.getJSONObject(i);
                        String goalName = goal.getString("name");

                        String insertGoalSql = "INSERT INTO somtoday6.goal (homework_id, goal_name) VALUES (?, ?)";
                        PreparedStatement insertGoalStmt = connection.prepareStatement(insertGoalSql);
                        insertGoalStmt.setInt(1, homeworkId);
                        insertGoalStmt.setString(2, goalName);
                        insertGoalStmt.executeUpdate();
                    }
                }

                // send notification for the student
                insertNotification(connection, studentPersonId, teacherName + " created an assignment for class: " + className);
            }

            // Create a homework instance for the teacher
            insertHomeworkStmt.setString(1, homeworkName);
            insertHomeworkStmt.setDate(2, dueDate);
            insertHomeworkStmt.setDate(3, publishDate);
            insertHomeworkStmt.setTime(4, timeIndication);
            insertHomeworkStmt.setInt(5, splitCount);
            insertHomeworkStmt.setString(6, description);
            insertHomeworkStmt.setInt(7, lessonId);
            insertHomeworkStmt.setNull(8, Types.INTEGER); // Set student_id to null for teacher
            insertHomeworkStmt.setInt(9, classId);
            insertHomeworkStmt.setBoolean(10, homeworkSplittable);
            insertHomeworkStmt.setInt(11, teacherId);
            insertHomeworkStmt.setDate(12, studentCalendarDate);
            insertHomeworkStmt.executeUpdate();

            ResultSet teacherGeneratedKeys = insertHomeworkStmt.getGeneratedKeys();
            if (teacherGeneratedKeys.next()) {
                int teacherHomeworkId = teacherGeneratedKeys.getInt(1);

                for (int i = 0; i < goals.length(); i++) {
                    JSONObject goal = goals.getJSONObject(i);
                    String goalName = goal.getString("name");

                    String insertGoalSql = "INSERT INTO somtoday6.goal (homework_id, goal_name) VALUES (?, ?)";
                    PreparedStatement insertGoalStmt = connection.prepareStatement(insertGoalSql);
                    insertGoalStmt.setInt(1, teacherHomeworkId);
                    insertGoalStmt.setString(2, goalName);
                    insertGoalStmt.executeUpdate();
                }
            }

            connection.commit();
            connection.close();

            return Response.ok().entity("{\"message\": \"Assignment created successfully.\"}").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("{\"message\": \"An error occurred: " + e.getMessage() + "\"}").build();
        }
    }

    private void insertNotification(Connection connection, int personId, String info) throws Exception {
        String sql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";
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
}
