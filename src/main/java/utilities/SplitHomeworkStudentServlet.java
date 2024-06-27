package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/splitHomeworkStudentServlet")
public class SplitHomeworkStudentServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String homeworkIdParam = request.getParameter("homeworkId");
        if (homeworkIdParam == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing homeworkId parameter");
            return;
        }
        int homeworkId = Integer.parseInt(homeworkIdParam);

        // Parse JSON from request body
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
            int splitCount = jsonObject.getInt("split_count");
            String description = jsonObject.getString("description");
            JSONArray splits = jsonObject.getJSONArray("splits");
            int teacherHomeworkId = 0;

            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                java.lang.Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

                // Retrieve homework teacher_id
                String selectHomeworkQuery = "SELECT homework_name, teacher_id, publish_date, due_date, student_id FROM homework WHERE homework_id = ?";
                stmt = connection.prepareStatement(selectHomeworkQuery);
                stmt.setInt(1, homeworkId);
                rs = stmt.executeQuery();

                if (!rs.next()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid homework_id");
                    return;
                }

                String originalHomeworkName = rs.getString("homework_name");
                int originalTeacherId = rs.getInt("teacher_id");
                Date originalPublishDate = rs.getDate("publish_date");
                Date originalDueDate = rs.getDate("due_date");
                int studentId = rs.getInt("student_id");


                // Fetch matching homework instances
                String fetchTeacherHomeworkSql = "SELECT h.homework_id " +
                        "FROM somtoday6.Homework h " +
                        "WHERE h.homework_name = ? AND h.due_date = ? AND h.publish_date = ? AND h.teacher_id = ? AND h.student_id IS NULL";
                PreparedStatement fetchTeacherHomeworkStmt = connection.prepareStatement(fetchTeacherHomeworkSql);
                fetchTeacherHomeworkStmt.setString(1, originalHomeworkName);
                fetchTeacherHomeworkStmt.setDate(2, originalDueDate);
                fetchTeacherHomeworkStmt.setDate(3, originalPublishDate);
                fetchTeacherHomeworkStmt.setInt(4, originalTeacherId);
                ResultSet teacherHomeworkRs = fetchTeacherHomeworkStmt.executeQuery();

                while (teacherHomeworkRs.next()){
                    teacherHomeworkId = teacherHomeworkRs.getInt("homework_id");
                }

                // Prepare statement for split request
                String insertSplitRequestQuery = "INSERT INTO split_request (teacher_homework_id, homework_id, student_id, request_description, accepted) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertReqStmt = connection.prepareStatement(insertSplitRequestQuery);

                insertReqStmt.setInt(1, teacherHomeworkId);
                insertReqStmt.setInt(2, homeworkId);
                insertReqStmt.setInt(3, studentId);
                insertReqStmt.setString(4, description);
                insertReqStmt.setBoolean(5, false);

                insertReqStmt.addBatch();
                insertReqStmt.executeBatch();

                // Prepare statement for inserting split homework
                    String insertSplitHomeworkQuery = "INSERT INTO splitted_homework (homework_id, student_id, time_indication, accepted, student_calendar_date, split_name) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement insertStmt = connection.prepareStatement(insertSplitHomeworkQuery);

                    // Insert split homework for each split entry in the request
                    for (int i = 0; i < splits.length(); i++) {
                        JSONObject split = splits.getJSONObject(i);
                        Time duration = convertMinutesToTime(split.getInt("duration"));
                        Date studentCalendarDate = null;
                        boolean accepted = false;
                        String splitName = split.getString("name");

                        insertStmt.setInt(1, homeworkId);
                        insertStmt.setInt(2, studentId);
                        insertStmt.setTime(3, duration);
                        insertStmt.setBoolean(4, accepted);
                        insertStmt.setDate(5, studentCalendarDate);
                        insertStmt.setString(6, splitName);

                        insertStmt.addBatch();
                    }

                    // Execute batch insert
                    insertStmt.executeBatch();


                // Send success response
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Split homeworks created successfully.\"}");

            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"message\": \"Error processing request: " + e.getMessage() + "\"}");
            } finally {
                // Close resources
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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
