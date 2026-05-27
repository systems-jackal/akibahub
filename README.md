content = """# Akiba Hub: Secure Student Chama Management System

Akiba Hub is a secure web-based student chama (savings group) management system designed to digitize, automate, and secure informal savings groups in Kenyan educational institutions.

## 1. Introduction

### 1.1 Background
In many institutions across Kenya, students participate in informal savings groups (chamas) to support personal savings and group financial activities. Currently, these systems are managed manually via mobile money confirmations, messaging platforms, and spreadsheets, leading to poor transparency, weak accountability, and high security risks.

### 1.2 Problem Statement
Students face challenges in securely managing both personal and group savings due to:
* Lack of transparent, automated, and secure digital systems.
* Manual tracking methods resulting in errors and fraud risks.
* Unauthorized group access and disputes over contributions.

## 2. Proposed Solution
Akiba Hub allows users to manage both personal and group savings digitally. It provides a choice between individual saving or joining a group system. Access is controlled through unique invite codes, and the system integrates **PayHero** for secure payment processing, ensuring real-time balance updates and transparent transaction recording.

## 3. Objectives
* Develop a secure digital platform for student personal and group savings.
* Implement secure group access using unique invite codes.
* Integrate PayHero for seamless digital payments.
* Provide transparent transaction tracking for accountability.
* Improve financial discipline among students.

## 4. Target Users
* University and college students.
* Group administrators (Chama leaders/treasurers).
* Members of student savings groups.

## 5. Functional Requirements
* **User Authentication:** Registration and login system.
* **Savings Modes:** Selection between personal and group savings.
* **Group Management:** Creation and management of groups by administrators.
* **Access Control:** Generation and validation of unique invite codes.
* **Payment Integration:** Contributions via PayHero API.
* **Transparency:** Automatic recording and history viewing for all transactions.

## 6. Non-Functional Requirements
* **Security:** Password hashing (bcrypt), JWT authentication, secure APIs, and HTTPS encryption.
* **Performance:** Fast processing of transactions and system requests.
* **Reliability:** Consistent financial data storage without loss.
* **Usability:** Simple, mobile-friendly interface for students.
* **Maintainability:** Modular backend design using Spring Boot.

## 7. Proposed Technologies
* **Frontend:** HTML, CSS, JavaScript (Vanilla JS).
* **Backend:** Java with Spring Boot.
* **Database:** MySQL.
* **Payment Integration:** PayHero API.
* **Security:** JWT, bcrypt, HTTPS.
* **Version Control:** Git & GitHub.

## 8. System Overview (Architecture)
* **Frontend:** User Interface built with web standards.
* **Backend:** RESTful APIs and Business Logic.
* **Database:** Persistent storage for users, groups, and transactions.
* **Payment Gateway:** External PayHero API for payment confirmations.

## 9. Project Structure

```text
akiba-hub/
├── backend/                  # Spring Boot Application
│   ├── src/main/java/com/akibahub/
│   │   ├── config/           # Security + CORS + JWT config
│   │   ├── controller/       # REST APIs
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Database access (JPA)
│   │   ├── model/            # Entities (User, Group, Transaction)
│   │   └── util/             # Helper classes (invite codes, etc.)
│   └── pom.xml
├── frontend/                 # Plain HTML/CSS/JS
│   ├── index.html            # Landing page
│   ├── dashboard.html        # Main interface
│   ├── group.html            # Group savings view
│   ├── assets/
│   │   ├── css/              # Stylesheets
│   │   └── js/               # API, Auth, and Payment logic
├── database/                 # SQL Scripts
│   ├── schema.sql
│   └── seed.sql
└── README.md
