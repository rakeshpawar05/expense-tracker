# Frontend Refactor Plan

## Current Architecture

Pages
↓
Components
↓
Context
↓
API Service

---

# Refactor Goals

- reduce API calls
- simplify data flow
- implement infinite scroll
- improve performance

---

# New API Flow

DashboardPage
↓
dashboardApi.getDashboardData()

ExpensePage
↓
expenseApi.getExpenseFeed()

---

# Tasks

- [ ] Create new API service files
- [ ] Replace dashboard API
- [ ] redesign expense page