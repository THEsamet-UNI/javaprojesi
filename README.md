# 🎓 Ehliyet Sınav Sistemi

JavaFX tabanlı ehliyet sınav yönetim sistemi. Bu uygulama, sürücü kurslarında ehliyet sınavlarını yönetmek için geliştirilmiştir.

## 📋 Özellikler

### 👨‍💼 Yönetici (Admin) Paneli
- **Özet Panel**: Sistem istatistiklerini görüntüleme (öğrenci sayısı, soru sayısı, sınav sayısı)
- **Soru Yönetimi**: Soru ekleme, düzenleme, silme ve kategorilere göre filtreleme
- **Sınav Yönetimi**: Sınav oluşturma, düzenleme ve sınav sorularını yönetme
- **Öğrenci Yönetimi**: Öğrenci kayıtları, düzenleme ve silme
- **Raporlar**: Sınav sonuçları ve öğrenci performans raporları

### 👨‍🎓 Öğrenci Paneli
- **Mevcut Sınavlar**: Aktif sınavları görüntüleme ve sınava girme
- **Sonuçlarım**: Geçmiş sınav sonuçlarını görüntüleme
- **Çalışma Modu**: Kategorilere göre pratik yapma

## 🛠️ Teknolojiler

- **Java 17**
- **JavaFX 17** - Kullanıcı arayüzü
- **MySQL** - Veritabanı
- **Maven** - Bağımlılık yönetimi
- **BCrypt** - Şifre güvenliği
- **iText PDF** - PDF rapor oluşturma (planlanıyor)

## 📦 Kurulum

### Gereksinimler
- Java 17 veya üzeri
- MySQL 8.0 veya üzeri
- Maven 3.6 veya üzeri

### Veritabanı Kurulumu
1. MySQL'de yeni bir veritabanı oluşturun
2. `database_schema.sql` dosyasını çalıştırarak tabloları ve örnek verileri oluşturun:
   ```bash
   mysql -u root -p < database_schema.sql
   ```

### Uygulama Kurulumu
1. Depoyu klonlayın
2. `ehliyet sistemi` klasörüne gidin
3. Bağımlılıkları yükleyin ve uygulamayı derleyin:
   ```bash
   mvn clean compile
   ```
4. Uygulamayı çalıştırın:
   ```bash
   mvn javafx:run
   ```

## 🔐 Varsayılan Kullanıcılar

| Kullanıcı Tipi | Kullanıcı Adı | Şifre |
|----------------|---------------|-------|
| Admin | admin | admin123 |
| Öğrenci | ogrenci1 | student123 |

## 📁 Proje Yapısı

```
ehliyet sistemi/
├── src/main/java/com/ehliyet/
│   ├── Main.java                 # Ana uygulama başlatıcı
│   ├── controllers/              # FXML Controller sınıfları
│   │   ├── LoginController.java
│   │   ├── AdminDashboardController.java
│   │   ├── StudentDashboardController.java
│   │   ├── QuestionManagementController.java
│   │   ├── ExamManagementController.java
│   │   └── ...
│   ├── dao/                      # Data Access Objects
│   │   ├── UserDAO.java
│   │   ├── QuestionDAO.java
│   │   ├── ExamDAO.java
│   │   └── ExamResultDAO.java
│   ├── models/                   # Model sınıfları
│   │   ├── User.java
│   │   ├── Question.java
│   │   ├── Exam.java
│   │   └── ExamResult.java
│   └── database/
│       └── DatabaseConnection.java
├── src/main/resources/fxml/      # FXML arayüz dosyaları
│   ├── login.fxml
│   ├── admin_dashboard.fxml
│   ├── student_dashboard.fxml
│   ├── question_management.fxml
│   ├── exam_management.fxml
│   └── ...
└── pom.xml
```

## 🔧 Veritabanı Yapılandırması

Veritabanı bağlantı ayarlarını değiştirmek için `DatabaseConnection.java` dosyasını düzenleyin:

```java
private static final String URL = "jdbc:mysql://localhost:3306/ehliyet_sistemi";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

## 📊 Veritabanı Şeması

- **users**: Kullanıcı bilgileri (admin ve öğrenciler)
- **categories**: Soru kategorileri
- **questions**: Sınav soruları
- **exams**: Sınav tanımları
- **exam_questions**: Sınavdaki sorular
- **exam_attempts**: Öğrenci sınav girişimleri
- **exam_answers**: Öğrenci cevapları

## 🚀 Geliştirme Durumu

- [x] Giriş sistemi
- [x] Admin paneli yapısı
- [x] Soru yönetimi
- [x] Sınav yönetimi
- [x] Öğrenci yönetimi
- [x] Öğrenci paneli yapısı
- [ ] Sınav alma ekranı
- [ ] Çalışma modu
- [ ] PDF rapor oluşturma
- [ ] Fotoğraflı sınav girişi

## 📝 Lisans

Bu proje eğitim amaçlı geliştirilmiştir.

## 👥 Katkıda Bulunanlar

- Proje sahibi: THEsamet-UNI