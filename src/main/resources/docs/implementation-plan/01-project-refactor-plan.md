# Expense Tracker Refactor Plan

## Purpose

This document defines the complete refactor plan for improving
performance, scalability, and maintainability of the Expense Tracker system.

The refactor focuses on:

- reducing large API payloads
- improving frontend architecture
- implementing infinite scroll for expenses
- introducing feature-based APIs
- optimizing database queries

---

# Current Problems

## 1 Dashboard loads full user domain

Dashboard currently calls:

GET /users/{userId}

This returns:

- months
- categories
- expenses
- savings

This is inefficient and unnecessary.

---

## 2 Expense page loads all expenses

GET /expenses

Returns entire dataset.

Problems:

- slow rendering
- large payload
- poor scalability

---

## 3 No pagination

Lists grow indefinitely.

---

# Target Architecture

Frontend
↓
Feature APIs
↓
Service Layer
↓
Optimized Queries
↓
Database

---

# Implementation Phases

## Phase 1 Backend API Design

Create new APIs:

GET /dashboard/complete
GET /expenses/feed
GET /months/names

---

## Phase 2 Database Optimization

Add indexes to support large datasets.

---

## Phase 3 Frontend API Layer Refactor

Replace current AxiosService usage.

---

## Phase 4 Dashboard Refactor

Replace `/users/{id}` with `/dashboard/complete`.

---

## Phase 5 Expense Page Redesign

Implement:

- infinite scroll
- date grouping
- filters

---

# Task List

- [ ] Design new APIs
- [ ] Implement backend endpoints
- [ ] Add database indexes
- [ ] Create frontend API services
- [ ] Refactor dashboard page
- [ ] Redesign expense page
- [ ] Remove deprecated APIs