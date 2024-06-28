//package utilities;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.*;
//import java.time.LocalDate;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.json.JSONObject;
//
//@WebServlet("/addLesson")
//public class AddLessonServlet extends HttpServlet {
//    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
//    private static final String DB_USER = "dab_di23242b_168";
//    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        try {
//            Class.forName("org.postgresql.Driver");
//            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//
//
//            StringBuilder sb = new StringBuilder();
//            BufferedReader reader = request.getReader();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//            }
//            JSONObject jsonRequest = new JSONObject(sb.toString());
//
//            String lessonName = jsonRequest.getString("lesson_name");
//            String lessonDescription = jsonRequest.getString("lesson_description");
//            int teacherId = jsonRequest.getInt("teacher_id");
//            int classId = jsonRequest.getInt("class_id");
//            int schoolYear = LocalDate.now().getYear();
//
//            String insertLessonSql = "INSERT INTO somtoday6.Lesson (lesson_name, lesson_description, teacher_id, class_id, school_year) " +
//                    "VALUES (?, ?, ?, ?, ?)";
//
//            try (PreparedStatement pstmt = connection.prepareStatement(insertLessonSql)) {
//                pstmt.setString(1, lessonName);
//                pstmt.setString(2, lessonDescription);
//                pstmt.setInt(3, teacherId);
//                pstmt.setInt(4, classId);
//                pstmt.setInt(5, schoolYear);
//                pstmt.executeUpdate();
//            }
//
//            //get person_id of teacher
//            int personId = 0;
//            String fetchPersonIdSql = "SELECT person_id FROM somtoday6.Teacher WHERE teacher_id = ?";
//            try (PreparedStatement pstmt = connection.prepareStatement(fetchPersonIdSql)) {
//                pstmt.setInt(1, teacherId);
//                ResultSet rs = pstmt.executeQuery();
//                if (rs.next()) {
//                    personId = rs.getInt("person_id");
//                }
//            }
//
//            // send notification to teacher
//            String insertNotificationSql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) " +
//                    "VALUES (?, ?, ?, ?)";
//            try (PreparedStatement pstmt = connection.prepareStatement(insertNotificationSql)) {
//                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
//                pstmt.setString(2, "Admin");
//                pstmt.setString(3, "Admin added you to a lesson: " + lessonName);
//                pstmt.setInt(4, personId);
//                pstmt.executeUpdate();
//            }
//
//            // get class name
//            String className = "";
//            String fetchClassNameSql = "SELECT class_name FROM somtoday6.Class WHERE class_id = ?";
//            try (PreparedStatement pstmt = connection.prepareStatement(fetchClassNameSql)) {
//                pstmt.setInt(1, classId);
//                ResultSet rs = pstmt.executeQuery();
//                if (rs.next()) {
//                    className = rs.getString("class_name");
//                }
//            }
//
//            // send notification to teacher
//            try (PreparedStatement pstmt = connection.prepareStatement(insertNotificationSql)) {
//                pstmt.setDate(1, Date.valueOf(LocalDate.now()));
//                pstmt.setString(2, "Admin");
//                pstmt.setString(3, "Admin added you to class: " + className);
//                pstmt.setInt(4, personId);
//                pstmt.executeUpdate();
//            }
//
//            // get all students in the class
//            String fetchStudentsSql = "SELECT person_id FROM somtoday6.Student WHERE class_id = ?";
//            try (PreparedStatement pstmt = connection.prepareStatement(fetchStudentsSql)) {
//                pstmt.setInt(1, classId);
//                ResultSet rs = pstmt.executeQuery();
//                while (rs.next()) {
//                    int studentPersonId = rs.getInt("person_id");
//
//                    // add notification for each student
//                    try (PreparedStatement studentPstmt = connection.prepareStatement(insertNotificationSql)) {
//                        studentPstmt.setDate(1, Date.valueOf(LocalDate.now()));
//                        studentPstmt.setString(2, "Admin");
//                        studentPstmt.setString(3, "Admin added a new lesson (" + lessonName + ") to your class: " + className);
//                        studentPstmt.setInt(4, studentPersonId);
//                        studentPstmt.executeUpdate();
//                    }
//                }
//            }
//
//            connection.close();
//
//            PrintWriter out = response.getWriter();
//            out.write("{\"success\": true}");
//            out.close();
//
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid number format");
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
//        }
//    }
//}
