package jaxRS;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

@Path("/addToClass")
public class AddClassResource {

    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addClass(
            @FormDataParam("className") String className,
            @FormDataParam("classCapacity") int classCapacity,
            @FormDataParam("students") List<String> students,
            @FormDataParam("classPicture") InputStream fileInputStream,
            @FormDataParam("classPicture") FormDataContentDisposition fileMetaData) {

        System.out.println("Received className: " + className);
        System.out.println("Received classCapacity: " + classCapacity);

        if (fileInputStream == null) {
            System.out.println("fileInputStream is null");
        } else {
            System.out.println("fileInputStream is not null");
        }

        if (fileMetaData == null) {
            System.out.println("fileMetaData is null");
        } else {
            System.out.println("fileMetaData is not null");
            System.out.println("fileMetaData.getSize(): " + fileMetaData.getSize());
            System.out.println("fileMetaData.getFileName(): " + fileMetaData.getFileName());
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            connection.setAutoCommit(false);

            String sql = "INSERT INTO somtoday6.Class (class_name, class_capacity, profile_picture) VALUES (?, ?, ?) RETURNING class_id";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, className);
                preparedStatement.setInt(2, classCapacity);

                if (fileInputStream != null) {
                    byte[] fileBytes = readAllBytes(fileInputStream);
                    System.out.println("File bytes length: " + fileBytes.length);
                    if (fileBytes.length > 0) {
                        preparedStatement.setBytes(3, fileBytes);
                    } else {
                        preparedStatement.setNull(3, java.sql.Types.BINARY);
                    }
                } else {
                    System.out.println("File is empty");
                    preparedStatement.setNull(3, java.sql.Types.BINARY);
                }

                ResultSet rs = preparedStatement.executeQuery();
                int classId = 0;
                if (rs.next()) {
                    classId = rs.getInt(1);
                }

                if (students != null) {
                    for (String studentIdStr : students) {
                        int studentId = Integer.parseInt(studentIdStr);

                        String studentSql = "UPDATE somtoday6.Student SET class_id = ? WHERE student_id = ?";
                        try (PreparedStatement studentStmt = connection.prepareStatement(studentSql)) {
                            studentStmt.setInt(1, classId);
                            studentStmt.setInt(2, studentId);
                            studentStmt.executeUpdate();

                            int personId = fetchPersonId(connection, studentId);
                            insertNotification(connection, personId, "Admin added you to a class: " + className);
                        }
                    }
                }

                connection.commit();
                return Response.seeOther(new java.net.URI("/SomtodayHomeworkPlanner_war/classes.html")).build();

            } catch (Exception e) {
                connection.rollback();
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred.").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred.").build();
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws Exception {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        }
    }

    private int fetchPersonId(Connection connection, int studentId) throws Exception {
        String sql = "SELECT person_id FROM somtoday6.Student WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("person_id");
            } else {
                throw new Exception("Person ID not found for student ID: " + studentId);
            }
        }
    }

    private void insertNotification(Connection connection, int personId, String info) throws Exception {
        String sql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, "Admin");
            pstmt.setString(3, info);
            pstmt.setInt(4, personId);
            pstmt.executeUpdate();
        }
    }
}
