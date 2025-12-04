package com.ehliyet.dao;

import com.ehliyet.database.DatabaseConnection;
import com.ehliyet.models.ExamResult;
import com.ehliyet.models.Exam;
import com.ehliyet.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamResultDAO {

    /**
     * Yeni sınav sonucu başlatır
     */
    public boolean startExam(ExamResult result) {
        String sql = "INSERT INTO exam_attempts (exam_id, student_id, start_time, status) " +
                "VALUES (?, ?, ?, 'in_progress')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, result.getExamId());
            stmt.setInt(2, result.getStudentId());
            stmt.setTimestamp(3, Timestamp.valueOf(result.getStartTime()));

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    result.setId(rs.getInt(1));
                }
                System.out.println("✅ Sınav başlatıldı: ResultID=" + result.getId());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınav başlatılırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sınavı tamamlar ve sonucu kaydeder
     */
    public boolean completeExam(ExamResult result) {
        String sql = "UPDATE exam_attempts SET end_time=?, score=?, correct_answers=?, " +
                "wrong_answers=?, empty_answers=?, status='completed' WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(result.getEndTime()));
            stmt.setInt(2, result.getScore());
            stmt.setInt(3, result.getCorrectAnswers());
            stmt.setInt(4, result.getWrongAnswers());
            stmt.setInt(5, result.getEmptyAnswers());
            stmt.setInt(6, result.getId());

            int affected = stmt.executeUpdate();

            if (affected > 0) {
                System.out.println("✅ Sınav tamamlandı: Score=" + result.getScore());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Sınav tamamlanırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Öğrenci cevabını kaydeder
     */
    public boolean saveStudentAnswer(int attemptId, int questionId, String answer, boolean isCorrect) {
        String sql = "INSERT INTO exam_answers (attempt_id, question_id, student_answer, is_correct) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE student_answer=?, is_correct=?, answered_at=NOW()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            stmt.setInt(2, questionId);
            stmt.setString(3, answer);
            stmt.setBoolean(4, isCorrect);
            stmt.setString(5, answer);
            stmt.setBoolean(6, isCorrect);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Cevap kaydedilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Öğrencinin cevaplarını getirir
     */
    public List<StudentAnswer> getStudentAnswers(int attemptId) {
        String sql = "SELECT sa.*, q.correct_answer, q.question_text " +
                "FROM exam_answers sa " +
                "INNER JOIN questions q ON sa.question_id = q.id " +
                "WHERE sa.attempt_id = ? " +
                "ORDER BY sa.id";

        List<StudentAnswer> answers = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, attemptId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StudentAnswer answer = new StudentAnswer();
                answer.setId(rs.getInt("id"));
                answer.setResultId(rs.getInt("attempt_id"));
                answer.setQuestionId(rs.getInt("question_id"));
                answer.setStudentAnswer(rs.getString("student_answer"));
                answer.setCorrect(rs.getBoolean("is_correct"));
                answer.setCorrectAnswer(rs.getString("correct_answer"));
                answer.setQuestionText(rs.getString("question_text"));

                Timestamp timestamp = rs.getTimestamp("answered_at");
                if (timestamp != null) {
                    answer.setAnsweredAt(timestamp.toLocalDateTime());
                }

                answers.add(answer);
            }

        } catch (SQLException e) {
            System.err.println("❌ Cevaplar getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return answers;
    }

    /**
     * Sınav sonucunu ID'ye göre getirir
     */
    public ExamResult getResultById(int id) {
        String sql = "SELECT er.*, e.exam_name, e.exam_code, e.passing_score, " +
                "u.username, u.full_name, u.student_no " +
                "FROM exam_attempts er " +
                "INNER JOIN exams e ON er.exam_id = e.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "WHERE er.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractResultFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Sonuç getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Öğrencinin tüm sınav sonuçlarını getirir
     */
    public List<ExamResult> getResultsByStudentId(int studentId) {
        String sql = "SELECT er.*, e.exam_name, e.exam_code, e.passing_score, " +
                "u.username, u.full_name, u.student_no " +
                "FROM exam_attempts er " +
                "INNER JOIN exams e ON er.exam_id = e.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "WHERE er.student_id = ? " +
                "ORDER BY er.created_at DESC";

        return getResults(sql, studentId);
    }

    /**
     * Öğrencinin belirli bir sınavdaki sonuçlarını getirir
     */
    public List<ExamResult> getResultsByStudentAndExam(int studentId, int examId) {
        String sql = "SELECT er.*, e.exam_name, e.exam_code, e.passing_score, " +
                "u.username, u.full_name, u.student_no " +
                "FROM exam_attempts er " +
                "INNER JOIN exams e ON er.exam_id = e.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "WHERE er.student_id = ? AND er.exam_id = ? " +
                "ORDER BY er.created_at DESC";

        List<ExamResult> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            stmt.setInt(2, examId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(extractResultFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Sonuçlar getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Sınava göre tüm sonuçları getirir
     */
    public List<ExamResult> getResultsByExamId(int examId) {
        String sql = "SELECT er.*, e.exam_name, e.exam_code, e.passing_score, " +
                "u.username, u.full_name, u.student_no " +
                "FROM exam_attempts er " +
                "INNER JOIN exams e ON er.exam_id = e.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "WHERE er.exam_id = ? " +
                "ORDER BY er.score DESC, er.created_at DESC";

        return getResults(sql, examId);
    }

    /**
     * Öğrencinin devam eden sınavını getirir
     */
    public ExamResult getInProgressExam(int studentId) {
        String sql = "SELECT er.*, e.exam_name, e.exam_code, e.passing_score, " +
                "u.username, u.full_name, u.student_no " +
                "FROM exam_attempts er " +
                "INNER JOIN exams e ON er.exam_id = e.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "WHERE er.student_id = ? AND er.status = 'in_progress' " +
                "ORDER BY er.start_time DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractResultFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Devam eden sınav getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Fotoğraf yolu günceller
     */
    public boolean updatePhotoPath(int resultId, String photoPath) {
        String sql = "UPDATE exam_attempts SET photo_path=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, photoPath);
            stmt.setInt(2, resultId);

            int affected = stmt.executeUpdate();
            return affected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Fotoğraf yolu güncellenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Sınav istatistikleri - Ortalama puan
     */
    public double getAverageScoreByExam(int examId) {
        String sql = "SELECT AVG(score) as avg_score FROM exam_attempts " +
                "WHERE exam_id=? AND status='completed'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_score");
            }

        } catch (SQLException e) {
            System.err.println("❌ Ortalama hesaplanırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Sınav istatistikleri - Başarı oranı
     */
    public double getPassRateByExam(int examId) {
        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN score >= (SELECT passing_score FROM exams WHERE id=?) THEN 1 ELSE 0 END) as passed " +
                "FROM exam_attempts WHERE exam_id=? AND status='completed'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examId);
            stmt.setInt(2, examId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                int passed = rs.getInt("passed");
                if (total > 0) {
                    return (passed * 100.0) / total;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Başarı oranı hesaplanırken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Helper: SQL query ile sonuçları getirir
     */
    private List<ExamResult> getResults(String sql, int parameter) {
        List<ExamResult> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parameter);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                results.add(extractResultFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Sonuçlar getirilirken hata: " + e.getMessage());
            e.printStackTrace();
        }
        return results;
    }

    /**
     * Helper: ResultSet'ten ExamResult objesi oluşturur
     */
    private ExamResult extractResultFromResultSet(ResultSet rs) throws SQLException {
        ExamResult result = new ExamResult();
        result.setId(rs.getInt("id"));
        result.setExamId(rs.getInt("exam_id"));
        result.setStudentId(rs.getInt("student_id"));

        Timestamp startTimestamp = rs.getTimestamp("start_time");
        if (startTimestamp != null) {
            result.setStartTime(startTimestamp.toLocalDateTime());
        }

        Timestamp endTimestamp = rs.getTimestamp("end_time");
        if (endTimestamp != null) {
            result.setEndTime(endTimestamp.toLocalDateTime());
        }

        result.setScore(rs.getInt("score"));
        result.setCorrectAnswers(rs.getInt("correct_answers"));
        result.setWrongAnswers(rs.getInt("wrong_answers"));
        result.setEmptyAnswers(rs.getInt("empty_answers"));
        result.setStatus(rs.getString("status"));
        result.setPhotoPath(rs.getString("photo_path"));

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            result.setCreatedDate(createdTimestamp.toLocalDateTime());
        }

        // İlişkili Exam bilgisi
        Exam exam = new Exam();
        exam.setId(rs.getInt("exam_id"));
        exam.setExamName(rs.getString("exam_name"));
        exam.setExamCode(rs.getString("exam_code"));
        exam.setPassingScore(rs.getInt("passing_score"));
        result.setExam(exam);

        // İlişkili User bilgisi
        User student = new User();
        student.setId(rs.getInt("student_id"));
        student.setUsername(rs.getString("username"));
        student.setFullName(rs.getString("full_name"));
        student.setStudentNo(rs.getString("student_no"));
        result.setStudent(student);

        return result;
    }

    /**
     * Inner class: Öğrenci cevabı için model
     */
    public static class StudentAnswer {
        private int id;
        private int resultId;
        private int questionId;
        private String studentAnswer;
        private boolean isCorrect;
        private String correctAnswer;
        private String questionText;
        private java.time.LocalDateTime answeredAt;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getResultId() { return resultId; }
        public void setResultId(int resultId) { this.resultId = resultId; }

        public int getQuestionId() { return questionId; }
        public void setQuestionId(int questionId) { this.questionId = questionId; }

        public String getStudentAnswer() { return studentAnswer; }
        public void setStudentAnswer(String studentAnswer) { this.studentAnswer = studentAnswer; }

        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }

        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public java.time.LocalDateTime getAnsweredAt() { return answeredAt; }
        public void setAnsweredAt(java.time.LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
    }
}
