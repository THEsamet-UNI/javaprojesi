package com.ehliyet.dao;

import com.ehliyet.database.DatabaseConnection;
import com.ehliyet.models.Question;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    /**
     * Yeni soru ekler
     */
    public boolean addQuestion(Question question) {
        String sql = "INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, " +
                "correct_answer, category, difficulty, image_path, points, created_by, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, question.getQuestionText());
            stmt.setString(2, question.getOptionA());
            stmt.setString(3, question.getOptionB());
            stmt.setString(4, question.getOptionC());
            stmt.setString(5, question.getOptionD());
            stmt.setString(6, question.getCorrectAnswer());
            stmt.setString(7, question.getCategory());
            stmt.setString(8, question.getDifficulty());
            stmt.setString(9, question.getImagePath());
            stmt.setInt(10, question.getPoints());
            stmt.setInt(11, question.getCreatedBy());
            stmt.setBoolean(12, question.isActive());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    question.setId(rs.getInt(1));
                }
                System.out.println("✅ Soru eklendi: ID=" + question.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Soru eklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Soru günceller
     */
    public boolean updateQuestion(Question question) {
        String sql = "UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, " +
                "option_d=?, correct_answer=?, category=?, difficulty=?, image_path=?, " +
                "points=?, is_active=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, question.getQuestionText());
            stmt.setString(2, question.getOptionA());
            stmt.setString(3, question.getOptionB());
            stmt.setString(4, question.getOptionC());
            stmt.setString(5, question.getOptionD());
            stmt.setString(6, question.getCorrectAnswer());
            stmt.setString(7, question.getCategory());
            stmt.setString(8, question.getDifficulty());
            stmt.setString(9, question.getImagePath());
            stmt.setInt(10, question.getPoints());
            stmt.setBoolean(11, question.isActive());
            stmt.setInt(12, question.getId());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                System.out.println("✅ Soru güncellendi: ID=" + question.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Soru güncellenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Soru siler (soft delete)
     */
    public boolean deleteQuestion(int questionId) {
        String sql = "UPDATE questions SET is_active=FALSE WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, questionId);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Soru silindi: ID=" + questionId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Soru silinirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ID'ye göre soru getirir
     */
    public Question getQuestionById(int id) {
        String sql = "SELECT * FROM questions WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractQuestionFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Soru getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Tüm aktif soruları getirir
     */
    public List<Question> getAllQuestions() {
        return getQuestions("SELECT * FROM questions WHERE is_active=TRUE ORDER BY created_at DESC");
    }

    /**
     * Kategoriye göre soruları getirir
     */
    public List<Question> getQuestionsByCategory(String category) {
        String sql = "SELECT * FROM questions WHERE category=? AND is_active=TRUE ORDER BY id";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(extractQuestionFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Sorular getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Zorluk seviyesine göre soruları getirir
     */
    public List<Question> getQuestionsByDifficulty(String difficulty) {
        String sql = "SELECT * FROM questions WHERE difficulty=? AND is_active=TRUE ORDER BY id";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, difficulty);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(extractQuestionFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Sorular getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Rastgele soru seçer (sınav için)
     */
    public List<Question> getRandomQuestions(int limit) {
        String sql = "SELECT * FROM questions WHERE is_active=TRUE ORDER BY RAND() LIMIT ?";
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                questions.add(extractQuestionFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Rastgele sorular getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Toplam soru sayısını getirir
     */
    public int getTotalQuestionCount() {
        String sql = "SELECT COUNT(*) FROM questions WHERE is_active=TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Soru sayısı alınırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Kategoriye göre soru sayısı
     */
    public int getQuestionCountByCategory(String category) {
        String sql = "SELECT COUNT(*) FROM questions WHERE category=? AND is_active=TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Kategori soru sayısı alınırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Helper: SQL query ile soruları getirir
     */
    private List<Question> getQuestions(String sql) {
        List<Question> questions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                questions.add(extractQuestionFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Sorular getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    /**
     * Helper: ResultSet'ten Question objesi oluşturur
     */
    private Question extractQuestionFromResultSet(ResultSet rs) throws SQLException {
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
        question.setCreatedBy(rs.getInt("created_by"));

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            question.setCreatedDate(createdTimestamp.toLocalDateTime());
        }

        question.setActive(rs.getBoolean("is_active"));
        return question;
    }
}
