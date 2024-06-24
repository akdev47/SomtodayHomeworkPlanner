package utilities;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/getClassPicture")
public class GetClassPictureServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String classId = request.getParameter("classId");

        if (classId == null || classId.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing classId parameter");
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Class.forName("org.postgresql.Driver");

            String sql = "SELECT profile_picture FROM somtoday6.Class WHERE class_id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, Integer.parseInt(classId));
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        byte[] imgData = resultSet.getBytes("profile_picture");

                        if (imgData != null) {
                            response.setContentType("image/jpeg");
                            try (OutputStream out = response.getOutputStream()) {
                                out.write(imgData);
                            }
                        } else {
                            response.sendError(HttpServletResponse.SC_NOT_FOUND, "no image found for the given classId");
                        }
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "class not found for the given classId");
                    }
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid classId parameter");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "an internal server error occurred!");
        }
    }
}
