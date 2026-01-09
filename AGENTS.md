# Plataforma Darcy - AI Agent Context

## 🚀 Project Overview
**Plataforma Darcy** is a monolithic web application for student preparation (PAS/UnB) built with Java Spring Boot (Backend) and Python (Intelligence Engine).

## 🏗️ Architecture
- **Backend**: Spring Boot 3.2.x (`backend-core/`)
- **Frontend**: Thymeleaf (Server-side rendering) + Vanilla JS + Bootstrap/CSS
- **Database**: MySQL (Production/Dev), H2 (Test)
- **AI/ML**: Python microservices (`intelligence-engine/`) for OCR, Podcast, and PDF Search.
- **Integration**: Google Gemini API, Efi Pay (Pix), Podcastfy.

## 📂 Directory Structure
- `backend-core/`: Main Java application.
  - `src/main/java/.../controller`: Web controllers.
  - `src/main/java/.../service`: Business logic.
  - `src/main/resources/templates`: HTML views.
- `intelligence-engine/`: Python scripts for AI tasks.
  - `src/`: Python source code.
  - `requirements.txt`: Python dependencies.

## 🛠️ Build & Run Commands
### Backend (Java)
```bash
cd backend-core
./mvnw clean install
./mvnw spring-boot:run
```

### Intelligence Engine (Python)
```bash
cd intelligence-engine
pip install -r requirements.txt
python src/processor.py
```

## 📝 Coding Conventions
- **Language**: Java 17+ and Python 3.10+
- **Style**: Standard Java checks, PEP 8 for Python.
- **Commits**: Conventional Commits (e.g., `feat(auth): ...`, `fix(core): ...`).
- **CSRF**: Currently disabled (needs fix for production).
- **Auth**: Spring Security + Google OAuth2.

## 🚧 Current MVP Status
- **Missing**: Real Payment integration (Efi SDK), Production Config, Email Service.
- **Mocked**: `EfiService.java` uses in-memory mocks.

## 💡 Key Files for Agents
- `SecurityConfig.java`: Auth rules.
- `SubscriptionService.java`: Logic for PRO plans.
- `SimuladoController.java`: Core logic for exams.
