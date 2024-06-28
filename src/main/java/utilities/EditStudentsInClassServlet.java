//package utilities;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.Date;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.ArrayList;
//import java.util.stream.Collectors;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@WebServlet("/editStudentsInClass")
//public class EditStudentsInClassServlet extends HttpServlet {
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
//        StringBuilder sb = new StringBuilder();
//        BufferedReader reader = request.getReader();
//        String line;
//        while ((line = reader.readLine()) != null) {
//            sb.append(line);
//        }
//        JSONObject jsonRequest = new JSONObject(sb.toString());
//
//        int classId = jsonRequest.getInt("classId");
//        JSONArray studentIdsJsonArray = jsonRequest.getJSONArray("studentIds");
//        List<Integer> studentIds = new ArrayList<>();
//        for (int i = 0; i < studentIdsJsonArray.length(); i++) {
//            studentIds.add(studentIdsJsonArray.getInt(i));
//        }
//
//        try {
//            Class.forName("org.postgresql.Driver");
//            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//
//            // get current students in the class
//            List<Integer> currentStudentIds = fetchCurrentStudents(connection, classId);
//
//            // get students to add and remove
//            List<Integer> studentsToAdd = studentIds.stream()
//                    .filter(id -> !currentStudentIds.contains(id))
//                    .collect(Collectors.toList());
//
//            List<Integer> studentsToRemove = currentStudentIds.stream()
//                    .filter(id -> !studentIds.contains(id))
//                    .collect(Collectors.toList());
//
//            // remove existing students from the class by setting class_id to NULL
//            String removeStudentsSql = "UPDATE somtoday6.Student SET class_id = NULL WHERE class_id = ? AND student_id = ?";
//            PreparedStatement removeStmt = connection.prepareStatement(removeStudentsSql);
//
//            for (Integer studentId : studentsToRemove) {
//                removeStmt.setInt(1, classId);
//                removeStmt.setInt(2, studentId);
//                removeStmt.addBatch();
//            }
//            removeStmt.executeBatch();
//
//            // assign new students to the class
//            String assignStudentsSql = "UPDATE somtoday6.Student SET class_id = ? WHERE student_id = ?";
//            PreparedStatement assignStmt = connection.prepareStatement(assignStudentsSql);
//
//            for (Integer studentId : studentsToAdd) {
//                assignStmt.setInt(1, classId);
//                assignStmt.setInt(2, studentId);
//                assignStmt.addBatch();
//            }
//            assignStmt.executeBatch();
//
//            // get class name
//            String className = fetchClassName(connection, classId);
//
//            // send notifications for added students
//            for (int studentId : studentsToAdd) {
//                int personId = fetchPersonId(connection, studentId);
//                insertNotification(connection, personId, "Admin added you to class: " + className);
//            }
//
//            // send notifications for removed students
//            for (int studentId : studentsToRemove) {
//                int personId = fetchPersonId(connection, studentId);
//                insertNotification(connection, personId, "Admin removed you from class: " + className);
//            }
//
//            connection.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
//            return;
//        }
//
//        PrintWriter out = response.getWriter();
//        out.write("{\"success\": true}");
//        out.close();
//    }
//
//    private List<Integer> fetchCurrentStudents(Connection connection, int classId) throws Exception {
//        String sql = "SELECT student_id FROM somtoday6.Student WHERE class_id = ?";
//        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
//            pstmt.setInt(1, classId);
//            ResultSet rs = pstmt.executeQuery();
//            List<Integer> studentIds = new ArrayList<>();
//            while (rs.next()) {
//                studentIds.add(rs.getInt("student_id"));
//            }
//            return studentIds;
//        }
//    }
//
//    private String fetchClassName(Connection connection, int classId) throws Exception {
//        String sql = "SELECT class_name FROM somtoday6.Class WHERE class_id = ?";
//        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
//            pstmt.setInt(1, classId);
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return rs.getString("class_name");
//            } else {
//                return "";
//            }
//        }
//    }
//
//    private int fetchPersonId(Connection connection, int studentId) throws Exception {
//        String sql = "SELECT person_id FROM somtoday6.Student WHERE student_id = ?";
//        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
//            pstmt.setInt(1, studentId);
//            ResultSet rs = pstmt.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("person_id");
//            } else {
//                throw new Exception("Person ID not found for student ID: " + studentId);
//            }
//        }
//    }
//
//    private void insertNotification(Connection connection, int personId, String info) throws Exception {
//        String sql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) " +
//                "VALUES (?, ?, ?, ?)";
//        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
//            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
//            pstmt.setString(2, "Admin");
//            pstmt.setString(3, info);
//            pstmt.setInt(4, personId);
//            pstmt.executeUpdate();
//        }
//    }
//}
