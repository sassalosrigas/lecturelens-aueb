# 🎓 LectureLens

> **A native Android application for university course and professor evaluations, featuring camera OCR text scanning.**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Language-Java-orange.svg?style=flat&logo=openjdk)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📽️ Video Demo & Screenshots

[![Watch Demo Video](https://img.shields.io/badge/▶️_Watch_Demo-Video-red?style=for-the-badge&logo=youtube)](https://youtu.be/ZojHPQZR8A4?si=1j1rtgXDZ4ZgdA6Q)

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

LectureLens/
├── app/                                            # Native Android Client
│   ├── src/main/     
|       ├── java/gr/aueb/lecturelens/               # Activities, Fragments, Adapters, Models
|       ├── res/                                    # Layout XMLs, Drawables, Fonts, Themes
├── backend/                                        # Spring Boot REST API
│   └── src/main/java/gr/aueb/lecturelens/backend   # REST Controllers, JPA Repositories, Models
└── docs/                                           # Documentation & Media Screenshots
