# Backend API Refactor

## Purpose

Replace entity-based APIs with feature-based APIs.

---

# Current APIs

GET /users/{id}
GET /expenses
GET /expenses/top5

Problems:

- heavy payload
- unnecessary data
- inefficient queries

---

# New API Design

## Dashboard APIs

GET /api/dashboard/complete

---

## Expense APIs

GET /api/expenses/feed

---

## Month APIs

GET /api/months/names

---

# Implementation Tasks

## Task 1 Create Dashboard Controller

Create:

DashboardController

Path:

controller/dashboard

---

## Task 2 Create Dashboard Service

Service:

DashboardService

Method:

getDashboardData(userId, monthName)

---

## Task 3 Create Expense Feed Endpoint

Controller:

ExpenseController

Method:

getExpenseFeed()

---

## Task Checklist

- [x] Create DashboardController
- [x] Create DashboardService
- [x] Implement expense feed endpoint
- [x] Add request parameters
- [x] Add DTO responses
