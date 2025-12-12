package com.ehliyet.dao;

import com.ehliyet.database.DatabaseConnection;
import com.ehliyet.models.Exam;
import com.ehliyet.models.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamDAO {

    /**
     * Yeni sınav oluşturur
     */
    public boolean createExam(Exam exam) {
        String sql = "INSERT INTO exams (exam_name, exam_code, description, duration_minutes, " +
                "total_questions, passing_score, created_by, start_date, end_date, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, exam.getExamName());
            stmt.setString(2, exam.getExamCode());
            stmt.setString(3, exam.getDescription());
            stmt.setInt(4, exam.getDurationMinutes());
            stmt.setInt(5, exam.getTotalQuestions());
            stmt.setInt(6, exam.getPassingScore());
            stmt.setInt(7, exam.getCreatedBy());
            stmt.setTimestamp(8, exam.getStartDate() != null ? Timestamp.valueOf(exam.getStartDate()) : null);
            stmt.setTimestamp(9, exam.getEndDate() != null ? Timestamp.valueOf(exam.getEndDate()) : null);
            stmt.setBoolean(10, exam.isActive());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    exam.setId(rs.getInt(1));
                }
                System.out.println("✅ Sınav oluşturuldu: " + exam.getExamCode());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınav oluşturulurken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sınav günceller
     */
    public boolean updateExam(Exam exam) {
        String sql = "UPDATE exams SET exam_name=?, description=?, duration_minutes=?, " +
                "total_questions=?, passing_score=?, start_date=?, end_date=?, is_active=? " +
                "WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, exam.getExamName());
            stmt.setString(2, exam.getDescription());
            stmt.setInt(3, exam.getDurationMinutes());
            stmt.setInt(4, exam.getTotalQuestions());
            stmt.setInt(5, exam.getPassingScore());
            stmt.setTimestamp(6, exam.getStartDate() != null ? Timestamp.valueOf(exam.getStartDate()) : null);
            stmt.setTimestamp(7, exam.getEndDate() != null ? Timestamp.valueOf(exam.getEndDate()) : null);
            stmt.setBoolean(8, exam.isActive());
            stmt.setInt(9, exam.getId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Sınav güncellendi: " + exam.getExamCode());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınav güncellenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sınav siler (soft delete)
     */
    public boolean deleteExam(int examId) {
        String sql = "UPDATE exams SET is_active=FALSE WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Sınav silindi: ID=" + examId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınav silinirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sınava soru ekler
     */
    public boolean addQuestionToExam(int examId, int questionId, int order) {
        String sql = "INSERT INTO exam_questions (exam_id, question_id, question_order) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            stmt.setInt(2, questionId);
            stmt.setInt(3, order);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Sınava soru eklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sınavdaki soruları getirir
     */
    public List<Question> getExamQuestions(int examId) {
        String sql = "SELECT q.* FROM questions q " +
                "INNER JOIN exam_questions eq ON q.id = eq.question_id " +
                "WHERE eq.exam_id = ? " +
                "ORDER BY eq.question_order";

        List<Question> questions = new ArrayList<>();
        QuestionDAO questionDAO = new QuestionDAO();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Question question = new Question();
                question.setId(rs.getInt("id"));
                question.setQuestionText(rs.getString("question_text"));
                question.setOptionA(rs.getString("option_a"));
                question.setOptionB(rs.getString("option_b"));
                question.setOptionC(rs.getString("option_c"));
                question.setOptionD(rs.getString("option_d"));
                question.setCorrectAnswer(rs.getString("correct_answer"));
                question.setCategory(rs.getString("category"));
                question.setDifficulty(rs.getString("difficulty"));
                question.setImagePath(rs.getString("image_path"));
                question.setPoints(rs.getInt("points"));
                questions.add(question);
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınav soruları getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * ID'ye göre sınav getirir
     */
    public Exam getExamById(int id) {
        String sql = "SELECT * FROM exams WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractExamFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınav getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Exam code'a göre sınav getirir
     */
    public Exam getExamByCode(String examCode) {
        String sql = "SELECT * FROM exams WHERE exam_code=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, examCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractExamFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınav getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tüm aktif sınavları getirir
     */
    public List<Exam> getAllActiveExams() {
        String sql = "SELECT * FROM exams WHERE is_active=TRUE ORDER BY created_at DESC";
        return getExams(sql);
    }

    /**
     * Öğrenci için uygun sınavları getirir
     */
    public List<Exam> getAvailableExamsForStudent() {
        String sql = "SELECT * FROM exams WHERE is_active=TRUE " +
                "AND (start_date IS NULL OR start_date <= NOW()) " +
                "AND (end_date IS NULL OR end_date >= NOW()) " +
                "ORDER BY created_at DESC";
        return getExams(sql);
    }

    /**
     * Helper: SQL query ile sınavları getirir
     */
    private List<Exam> getExams(String sql) {
        List<Exam> exams = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                exams.add(extractExamFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınavlar getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return exams;
    }

    /**
     * Helper: ResultSet'ten Exam objesi oluşturur
     */
    private Exam extractExamFromResultSet(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setId(rs.getInt("id"));
        exam.setExamName(rs.getString("exam_name"));
        exam.setExamCode(rs.getString("exam_code"));
        exam.setDescription(rs.getString("description"));
        exam.setDurationMinutes(rs.getInt("duration_minutes"));
        exam.setTotalQuestions(rs.getInt("total_questions"));
        exam.setPassingScore(rs.getInt("passing_score"));
        exam.setCreatedBy(rs.getInt("created_by"));

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            exam.setCreatedDate(createdTimestamp.toLocalDateTime());
        }

        Timestamp startTimestamp = rs.getTimestamp("start_date");
        if (startTimestamp != null) {
            exam.setStartDate(startTimestamp.toLocalDateTime());
        }

        Timestamp endTimestamp = rs.getTimestamp("end_date");
        if (endTimestamp != null) {
            exam.setEndDate(endTimestamp.toLocalDateTime());
        }

        exam.setActive(rs.getBoolean("is_active"));
        return exam;
    }
}
