package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/addClass")
@MultipartConfig
public class AddClassServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String className = request.getParameter("className");
        String[] students = request.getParameterValues("students");
        Part filePart = request.getPart("classPicture");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            connection.setAutoCommit(false);

            String sql = "INSERT INTO somtoday6.Class (class_name, class_capacity, profile_picture) VALUES (?, ?, ?) RETURNING class_id";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, className);
            preparedStatement.setInt(2, Integer.parseInt(request.getParameter("classCapacity")));
            if (filePart != null && filePart.getSize() > 0) {
                InputStream fileContent = filePart.getInputStream();
                preparedStatement.setBinaryStream(3, fileContent, (int) filePart.getSize());
            } else {
                preparedStatement.setNull(3, java.sql.Types.BINARY);
            }
            ResultSet rs = preparedStatement.executeQuery();
            int classId = 0;
            if (rs.next()) {
                classId = rs.getInt(1);
            }

            // Handle associations with students
            if (students != null) {
                for (String studentId : students) {
                    String studentSql = "UPDATE somtoday6.Student SET class_id = ? WHERE student_id = ?";
                    PreparedStatement studentStmt = connection.prepareStatement(studentSql);
                    studentStmt.setInt(1, classId);
                    studentStmt.setInt(2, Integer.parseInt(studentId));
                    studentStmt.executeUpdate();
                }
            }

            connection.commit(); // Commit the transaction
            connection.close();
            response.sendRedirect("classes.html?timestamp=" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }
}
