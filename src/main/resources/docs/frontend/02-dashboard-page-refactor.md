# Dashboard Page Refactor

## Current Flow

DashboardPage

calls

GET /users/{id}

Returns full user domain.

---

# New Flow

DashboardPage

calls

GET /dashboard/complete

---

# Implementation Steps

1 Create dashboardApi.js

2 Replace API call in DashboardPage

3 Update state handling

---

# Tasks

- [ ] create dashboardApi
- [ ] update DashboardPage
- [ ] remove old API usage