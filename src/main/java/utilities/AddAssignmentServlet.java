package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/addAssignment")
public class AddAssignmentServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuilder jb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            throw new IOException("Error reading request body: " + e.getMessage());
        }

        try {
            JSONObject jsonObject = new JSONObject(jb.toString());

            int personId = jsonObject.getInt("personId");
            String homeworkName = jsonObject.getString("homeworkName");
            Date dueDate = Date.valueOf(jsonObject.getString("dueDate"));
            Date publishDate = Date.valueOf(jsonObject.getString("publishDate"));
            Time timeIndication = null;
            int splitCount = 0; // UPDATE
            String description = jsonObject.getString("description");
            int lessonId = jsonObject.getInt("lessonId");
            int teacherId = 0; // Will be fetched from DB
            int classId = jsonObject.getInt("classId");

            JSONArray goals = jsonObject.getJSONArray("goals");

            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);

            // Use PreparedStatement to avoid SQL injection
            String fetchTeacherSql = "SELECT teacher_id FROM somtoday6.teacher t WHERE t.person_id = ?";
            PreparedStatement fetchTeacherStmt = connection.prepareStatement(fetchTeacherSql);
            fetchTeacherStmt.setInt(1, personId);
            ResultSet resultSet = fetchTeacherStmt.executeQuery();

            if (resultSet.next()) {
                teacherId = resultSet.getInt("teacher_id");
            } else {
                throw new SQLException("Teacher not found for personId: " + personId);
            }

            String fetchStudentsSql = "SELECT student_id FROM somtoday6.student WHERE class_id = ?";
            PreparedStatement fetchStudentsStmt = connection.prepareStatement(fetchStudentsSql);
            fetchStudentsStmt.setInt(1, classId);
            ResultSet rs = fetchStudentsStmt.executeQuery();

            String insertHomeworkSql = "INSERT INTO somtoday6.Homework (homework_name, due_date, publish_date, time_indication, split_count, description, lesson_id, student_id, class_id, homeworksubmittable, homeworksplittable, teacher_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertHomeworkStmt = connection.prepareStatement(insertHomeworkSql, Statement.RETURN_GENERATED_KEYS);

            while (rs.next()) {
                int studentId = rs.getInt("student_id");

                insertHomeworkStmt.setString(1, homeworkName);
                insertHomeworkStmt.setDate(2, dueDate);
                insertHomeworkStmt.setDate(3, publishDate);
                insertHomeworkStmt.setTime(4, timeIndication);
                insertHomeworkStmt.setInt(5, splitCount);
                insertHomeworkStmt.setString(6, description);
                insertHomeworkStmt.setInt(7, lessonId);
                insertHomeworkStmt.setInt(8, studentId);
                insertHomeworkStmt.setInt(9, classId);
                insertHomeworkStmt.setBoolean(10, true);
                insertHomeworkStmt.setBoolean(11, true);
                insertHomeworkStmt.setInt(12, teacherId);
                insertHomeworkStmt.executeUpdate();

                ResultSet generatedKeys = insertHomeworkStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int homeworkId = generatedKeys.getInt(1);

                    for (int i = 0; i < goals.length(); i++) {
                        JSONObject goal = goals.getJSONObject(i);
                        String goalName = goal.getString("name");
                        Time goalTime = convertMinutesToTime(Integer.parseInt(goal.getString("time")));


                        String insertGoalSql = "INSERT INTO somtoday6.goal (homework_id, goal_name, time_indication) VALUES (?, ?, ?)";
                        PreparedStatement insertGoalStmt = connection.prepareStatement(insertGoalSql);
                        insertGoalStmt.setInt(1, homeworkId);
                        insertGoalStmt.setString(2, goalName);
                        insertGoalStmt.setTime(3, goalTime);
                        insertGoalStmt.executeUpdate();
                    }
                }
            }

            connection.commit(); // Commit the transaction
            connection.close();
            response.sendRedirect("assignments-teacher.html?timestamp=" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred: " + e.getMessage());
        }
    }

    private Time convertMinutesToTime(int minutes) {
        long millis = TimeUnit.MINUTES.toMillis(minutes);
        return new Time(millis - TimeZone.getDefault().getRawOffset());
    }
}
