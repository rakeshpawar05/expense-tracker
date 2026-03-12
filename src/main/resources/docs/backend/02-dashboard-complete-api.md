# Dashboard Complete API

## Endpoint

GET /api/dashboard/complete

---

# Query Parameters

userId
monthName

Example

/api/dashboard/complete?userId=1&monthName=March,2026

---

# Response Structure

{
summary: {},
topExpenses: [],
categoryBreakdown: [],
expenseCount: 0,
savingCount: 0
}

---

# DTO Classes

DashboardResponseDTO

Fields:

summary
topExpenses
categoryBreakdown
expenseCount
savingCount

---

# Implementation Steps

1 Create DTOs

DashboardResponseDTO
ExpenseSummaryDTO
CategorySummaryDTO

---

2 Create DashboardService

Method:

getDashboardData()

---

3 Implement Repository Queries

Total expenses

SELECT SUM(amount)
FROM expenses
WHERE user_id = :userId
AND month_id = :monthId

---

Top expenses

SELECT description, amount
FROM expenses
WHERE month_id = :monthId
ORDER BY amount DESC
LIMIT 5

---

Category totals

SELECT category_name, SUM(amount)
FROM expenses
WHERE month_id = :monthId
GROUP BY category_name

---

# Tasks

- [ ] Create DTO classes
- [ ] Implement repository queries
- [ ] Implement DashboardService
- [ ] Implement DashboardController
- [ ] Add API documentation