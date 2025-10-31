-- ============================================
-- Ehliyet Sınav Sistemi - Komple Veritabanı Şeması
-- ============================================

-- Veritabanı oluşturma (eğer yoksa)
CREATE DATABASE IF NOT EXISTS ehliyet_sistemi 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE ehliyet_sistemi;

-- ============================================
-- 1. USERS TABLE (Kullanıcılar)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    tc_no VARCHAR(11) UNIQUE,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    user_type ENUM('student', 'admin') NOT NULL DEFAULT 'student',
    student_no VARCHAR(20) UNIQUE,
    phone VARCHAR(15),
    email VARCHAR(100),
    birth_date DATE,
    photo_path VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_user_type (user_type),
    INDEX idx_tc_no (tc_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. CATEGORIES TABLE (Soru Kategorileri)
-- ============================================
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. QUESTIONS TABLE (Sınav Soruları)
-- ============================================
CREATE TABLE IF NOT EXISTS questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_answer ENUM('A', 'B', 'C', 'D') NOT NULL,
    difficulty_level ENUM('easy', 'medium', 'hard') DEFAULT 'medium',
    points INT DEFAULT 1,
    image_path VARCHAR(255),
    explanation TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_category (category_id),
    INDEX idx_difficulty (difficulty_level),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. EXAMS TABLE (Sınav Tanımları)
-- ============================================
CREATE TABLE IF NOT EXISTS exams (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exam_name VARCHAR(100) NOT NULL,
    exam_code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL DEFAULT 45,
    total_questions INT NOT NULL DEFAULT 30,
    passing_score DECIMAL(5,2) NOT NULL DEFAULT 70.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_exam_code (exam_code),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. EXAM_QUESTIONS TABLE (Sınavdaki Sorular)
-- ============================================
CREATE TABLE IF NOT EXISTS exam_questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    question_id INT NOT NULL,
    question_order INT NOT NULL,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_exam_question (exam_id, question_id),
    INDEX idx_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 6. EXAM_ATTEMPTS TABLE (Öğrenci Sınav Girişimleri)
-- ============================================
CREATE TABLE IF NOT EXISTS exam_attempts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    exam_id INT NOT NULL,
    student_id INT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    total_questions INT NOT NULL,
    correct_answers INT DEFAULT 0,
    wrong_answers INT DEFAULT 0,
    empty_answers INT DEFAULT 0,
    score DECIMAL(5,2) DEFAULT 0.00,
    is_passed BOOLEAN DEFAULT FALSE,
    status ENUM('in_progress', 'completed', 'cancelled') DEFAULT 'in_progress',
    photo_path VARCHAR(255),
    ip_address VARCHAR(45),
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_student (student_id),
    INDEX idx_exam (exam_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 7. EXAM_ANSWERS TABLE (Öğrenci Cevapları)
-- ============================================
CREATE TABLE IF NOT EXISTS exam_answers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    attempt_id INT NOT NULL,
    question_id INT NOT NULL,
    student_answer ENUM('A', 'B', 'C', 'D', 'EMPTY') DEFAULT 'EMPTY',
    is_correct BOOLEAN DEFAULT FALSE,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    UNIQUE KEY unique_attempt_question (attempt_id, question_id),
    INDEX idx_attempt (attempt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- ÖRNEK VERİLER
-- ============================================

-- Admin kullanıcı ekleme (Şifre: admin123)
INSERT INTO users (username, password_hash, user_type, full_name, email, is_active)
VALUES (
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW7QK5K0cIAq',
    'admin',
    'Admin Kullanıcı',
    'admin@ehliyetsistemi.com',
    TRUE
) ON DUPLICATE KEY UPDATE username=username;

-- Test öğrenci ekleme (Şifre: student123)
INSERT INTO users (tc_no, username, password_hash, user_type, full_name, email, phone, student_no, birth_date, is_active)
VALUES (
    '12345678901',
    'ogrenci1',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5lW7QK5K0cIAq',
    'student',
    'Test Öğrenci',
    'ogrenci1@test.com',
    '05551234567',
    'OGR001',
    '2000-01-01',
    TRUE
) ON DUPLICATE KEY UPDATE username=username;

-- Kategoriler ekleme
INSERT INTO categories (name, description) VALUES
('Trafik ve Çevre Bilgisi', 'Trafik kuralları ve çevre bilgisi ile ilgili sorular'),
('Motor ve Araç Tekniği', 'Araç tekniği ve bakım bilgisi soruları'),
('İlk Yardım', 'İlk yardım ve acil durum soruları'),
('Trafik Adabı', 'Trafik adabı ve güvenlik soruları')
ON DUPLICATE KEY UPDATE name=name;

-- Örnek sorular ekleme
INSERT INTO questions (category_id, question_text, option_a, option_b, option_c, option_d, correct_answer, difficulty_level, points, explanation) VALUES
(1, 'Şehir içi yollarda azami hız sınırı kaç km/saat\'tir?', '30', '50', '70', '90', 'B', 'easy', 1, 'Şehir içinde azami hız 50 km/saat olarak belirlenmiştir.'),
(1, 'Aşağıdakilerden hangisi sürücü belgesine el konulmasını gerektirir?', 'Hız sınırını aşmak', 'Alkollü araç kullanmak', 'Kırmızı ışıkta geçmek', 'Emniyet kemeri takmamak', 'B', 'medium', 1, 'Alkollü araç kullanmak sürücü belgesine el konulmasını gerektiren bir durumdur.'),
(2, 'Motor yağı hangi amaçla kullanılır?', 'Soğutmak için', 'Yağlamak için', 'Temizlemek için', 'Hepsı', 'D', 'easy', 1, 'Motor yağı, motoru yağlar, soğutur ve temizler.'),
(2, 'Araçta ABS sisteminin görevi nedir?', 'Hızı artırır', 'Yakıt tasarrufu sağlar', 'Fren sırasında tekerleklerin kilitlenmesini önler', 'Motoru korur', 'C', 'medium', 1, 'ABS (Anti-lock Braking System), fren yaparken tekerleklerin kilitlenmesini önler.'),
(3, 'Trafik kazasında yaralıya ilk yapılması gereken nedir?', 'Su vermek', 'Olay yerini güvenli hale getirmek', 'Hastaneye götürmek', 'Yaralıyı kaldırmak', 'B', 'hard', 1, 'İlk olarak olay yeri güvenli hale getirilmeli, ardından yaralıya müdahale edilmelidir.'),
(3, 'Bilinçsiz bir yaralıya ne yapılmalıdır?', 'Hemen su içirilmeli', 'Havayolu açıklığı kontrol edilmeli', 'Ayağa kaldırılmalı', 'Hiçbir şey yapılmamalı', 'B', 'medium', 1, 'Bilinçsiz yaralıda önce havayolu açıklığı kontrol edilmelidir.'),
(4, 'Aşağıdakilerden hangisi savunma sürücülüğü prensiplerindendir?', 'Hızlı gitmek', 'Dikkatli ve öngörülü olmak', 'Son anda fren yapmak', 'Sürekli korna çalmak', 'B', 'easy', 1, 'Savunma sürücülüğünde dikkatli ve öngörülü olmak en önemli prensiptir.'),
(4, 'Yolcu taşırken en önemli öncelik nedir?', 'Hız', 'Konfor', 'Güvenlik', 'Zaman', 'C', 'easy', 1, 'Yolcu taşımada en önemli öncelik her zaman güvenliktir.')
ON DUPLICATE KEY UPDATE question_text=question_text;

-- Örnek sınav oluşturma
INSERT INTO exams (exam_name, exam_code, description, duration_minutes, total_questions, passing_score, created_by) VALUES
('B Sınıfı Ehliyet Sınavı', 'B-SINAV-001', 'B sınıfı ehliyet için teorik sınav', 45, 30, 70.00, 1),
('Örnek Deneme Sınavı', 'DENEME-001', 'Pratik yapma amaçlı deneme sınavı', 30, 20, 70.00, 1)
ON DUPLICATE KEY UPDATE exam_code=exam_code;

-- Sınava soru ekleme (B Sınıfı Ehliyet Sınavı için ilk 8 soruyu ekliyoruz)
INSERT INTO exam_questions (exam_id, question_id, question_order)
SELECT 1, id, (@row_number:=@row_number + 1) as question_order
FROM questions, (SELECT @row_number:=0) as t
WHERE questions.id <= 8
ON DUPLICATE KEY UPDATE question_order=question_order;

-- ============================================
-- VERİTABANI ŞEMASI BAŞARIYLA OLUŞTURULDU
-- ============================================

SELECT 'Veritabanı şeması başarıyla oluşturuldu!' as Message;
SELECT COUNT(*) as 'Toplam Kategori' FROM categories;
SELECT COUNT(*) as 'Toplam Soru' FROM questions;
SELECT COUNT(*) as 'Toplam Kullanıcı' FROM users;
SELECT COUNT(*) as 'Toplam Sınav' FROM exams;