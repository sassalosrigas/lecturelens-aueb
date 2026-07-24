# 🎓 LectureLens

> **A native Android application for university course and professor evaluations, featuring camera OCR text scanning.**

**Platform:** [Android](https://www.android.com/) | **Language:** [Java](https://www.java.com/) | **Backend:** [Spring Boot](https://spring.io/projects/spring-boot) | **License:** [MIT](LICENSE)

---

## 📽️ Video Demo & Screenshots

* **Demo Video:** [LectureLens Demo Video](https://youtu.be/ZojHPQZR8A4?si=1j1rtgXDZ4ZgdA6Q)

| Home & Search | Course & Professor Details | Camera OCR Scanner |
| :---: | :---: | :---: |
| <img src="docs/screenshots/home-search.png" width="230"/> | <img src="docs/screenshots/course-details.png" width="230"/> | <img src="docs/screenshots/ocr-scan.png" width="230"/> |

| Write Review | Admin Moderation | User Profile |
| :---: | :---: | :---: |
| <img src="docs/screenshots/submit-review.png" width="230"/> | <img src="docs/screenshots/admin.png" width="230"/> | <img src="docs/screenshots/profile-myreviews.png" width="230"/> |

---

## 🌟 Key Features

* 🔍 **Course & Professor Discovery**: Search and filter academic courses and faculty with real-time ratings and workload statistics.
* 📷 **OCR Syllabus Scanner**: Scan printed syllabus documents or course materials using the device camera for fast course lookups.
* ✍️ **Student Reviews & Feedback**: Submit detailed ratings covering difficulty, weekly hours, and quality of instruction.
* 🛡️ **Content Moderation Dashboard**: Built-in admin panel to process flagged reviews and manage user submissions.
* 🔐 **Session Management**: Secure client-side authentication, session persistence, and password management.

---

## 🛠️ Tech Stack

### **Android Client (Mobile)**
* **Language**: Java
* **UI/UX**: Material Design components, custom drawables, `RecyclerView` Adapters, custom typography (Poppins & Roboto)
* **Architecture**: Activity/Fragment lifecycle management, Model-Adapter separation
* **Device Hardware**: Camera OCR scanner integration

### **Backend Service**
* **Framework**: Spring Boot (Java)
* **Architecture**: RESTful API, MVC architecture, Spring Data JPA Repositories
* **Persistence**: Relational database persistence layer

---

## 📁 Repository Structure

```
LectureLens/
├── app/                                            # Native Android Client
│   ├── src/main/    
│   │   ├── java/gr/aueb/lecturelens/               # Activities, Fragments, Adapters, Models
│   │   └── res/                                    # Layout XMLs, Drawables, Fonts, Themes
├── backend/                                        # Spring Boot REST API
│   └── src/main/java/gr/aueb/lecturelens/backend   # REST Controllers, JPA Repositories, Models
└── docs/                                           # Documentation & Media Screenshots
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio**: Ladybug / Jellyfish (or newer) with JDK 17+
* **Android SDK**: API Level 24+ (Android 7.0+)
* **Gradle**: 8.x+

### Installation & Run

1. **Clone the repository**:
   ```bash
   git clone [https://github.com/sassalosrigas/lecturelens-aueb.git](https://github.com/sassalosrigas/lecturelens-aueb.git)
   cd lecturelens-aueb

2. **Start the Spring Boot Backend**:

       cd backend
        ./gradlew bootRun

3. **Run the Android App**:

    * Open the project root in ***Android Studio***.

    * Sync Gradle files (`File > Sync Project with Gradle Files`)

    * Select a target emulator or physical device and click Run (`Shift + F10`).

---

## 👨‍💻 Authors

**Rigas** - [Github Profile](https://github.com/sassalosrigas), [LinkedIn](https://www.linkedin.com/in/rigas-sassalos-27a256262/) <br>
**Eleni** - [Github Profile](https://github.com/tsaranto), [LinkedIn](https://www.linkedin.com/in/eleni-antoniadi-tsarampoulidi-4b2279314/) <br>
**Thodoris** - [Github Profile](https://github.com/ThodTsi), [LinkedIn](https://www.linkedin.com/in/thodoristsirikolias/)

