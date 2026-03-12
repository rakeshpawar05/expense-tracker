# Expense Feed API

## Purpose

Provide paginated expense feed with infinite scrolling.

---

# Endpoint

GET /api/expenses/feed

---

# Query Parameters

userId
monthName
cursor
limit
fromDate
toDate
categoryId
eventId

---

# Example Request

/api/expenses/feed?userId=1&monthName=March,2026&limit=20

---

# Response

{
items: [],
nextCursor: "2026-03-19T20:10:00"
}

---

# Query Logic

SELECT *
FROM expenses
WHERE user_id = :userId
AND month_id = :monthId
AND (:cursor IS NULL OR date < :cursor)
ORDER BY date DESC
LIMIT :limit

---

# Implementation Steps

1 Create ExpenseFeedDTO

2 Create ExpenseFeedResponseDTO

3 Implement repository method

4 Implement service method

5 Implement controller endpoint

---

# Tasks

- [ ] Create DTO classes
- [ ] Add repository query
- [ ] Add service method
- [ ] Add controller endpoint
- [ ] Add request validation