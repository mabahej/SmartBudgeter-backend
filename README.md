#  SmartBudgeter-backend

SmartBudgeter-backend is the backend service of *SmartBudgeter*: an intelligent, user-friendly web and mobile application that helps families manage and optimize their finances through a multilingual conversational assistant (voice/text).

## 📌 Objectives
- Provide an accessible, AI-powered solution for tracking expenses and planning budgets.
- Enable natural, conversational interactions to simplify financial management.
- Support multi-user family accounts with secure authentication.

## 🏗 Architecture Overview

### 🔹 Frontend (web or mobile)
- **Technology:** Angular
- Responsive design
- Multilingual interface (French / English)

### 🔹 Backend (Spring Boot)
- User management (authentication via JWT or Keycloak)
- RESTful services:
  - Expenses
  - Budgets
  - Reminders
  - Chatbot interactions (NLU + logic)

### 🔹 Database
- **PostgreSQL**
- Schema:
  - Users
  - Expenses (amount, category, date)
  - Budgets (category, monthly limit)
  - Reminders
  - Chatbot messages (log, intent, entity)

### 🔹 AI / NLP
- Integration with OpenAI GPT (e.g., GPT-4o) or HuggingFace models
- Intent recognition: add expense, get summary, ask remaining budget, schedule reminder
- Contextual analysis and smart advice generation

## ⚙️ Functional Modules

✅ **Authentication**
- Sign up / login (JWT | Keycloak)
- Family multi-user accounts

✅ **Expense Management**
- Add, update, delete
- Filter by date, category

✅ **Budget Planning**
- Monthly category limits
- Alerts & notifications

✅ **Visualization**
- Dynamic charts (Chart.js or Recharts)
- Weekly / monthly summaries

✅ **Intelligent Assistant**
- Natural language command parsing
- Textual chatbot interface
- Context awareness and smart suggestions

## 🤖 Chatbot AI – Technical Details
- Intent detection examples:
  - “Ajoute 50 TND pour les enfants”
  - “Combien reste-t-il dans loisirs ?”
  - “Montre les dépenses de cette semaine”
- OpenAI GPT integration:
  - API call with user context
  - Text analysis → contextual response
  - [Example integration guide](https://www.baeldung.com/spring-boot-chatgpt-api-openai)

## 🛠 Tech Stack
- **Backend:** Spring Boot
- **Frontend:** Angular
- **Database:** PostgreSQL
- **AI / NLP:** OpenAI GPT / HuggingFace
- **Authentication:** JWT | Keycloak
- **Charts:** Chart.js / Recharts

## 🚀 Getting Started (to be added)
- Installation & setup instructions
- Running locally
- API documentation (Swagger / OpenAPI)
