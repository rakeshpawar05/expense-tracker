# Unified Expense Search Endpoint

## Goal
Create a single backend endpoint for expense listing, filtering, and pagination so the front-end can use one clean API instead of multiple endpoints.

## Endpoint
`GET /api/expenses`

---

## Query parameters
- `userId` (required)
- `monthName` (optional) — e.g. `March,2026`
- `categoryName` (optional)
- `expenseName` (optional) — search by description/title
- `fromDate` (optional) — `yyyy-MM-dd`
- `toDate` (optional) — `yyyy-MM-dd`
- `limit` (optional, default `20`)
- `cursor` (optional) — for cursor-based pagination
- `sortOrder` (optional, default `desc`)

---

## Example request

`GET /api/expenses?userId=1&monthName=March,2026&expenseName=coffee&fromDate=2026-03-01&toDate=2026-03-31&limit=20&cursor=2026-03-18T12:00:00&sortOrder=desc`

---

## Response structure

```json
{
  "items": [
    {
      "id": 7,
      "description": "hello new",
      "amount": 678,
      "date": "2026-03-16",
      "monthName": "March,2026",
      "categoryName": null,
      "eventName": null,
      "userId": 1
    }
  ],
  "nextCursor": "2026-03-12T08:30:00",
  "hasMore": true,
  "totalItems": 94
}
```

### Fields
- `items`: page of results
- `nextCursor`: cursor value for the next request
- `hasMore`: whether more results exist
- `totalItems`: optional total matching count

---

## Backend DTO suggestions

### ExpenseSearchResponseDto
- `List<ExpenseDto> items`
- `String nextCursor`
- `Boolean hasMore`
- `Long totalItems`

### Controller signature

```java
@GetMapping
public ExpenseSearchResponseDto searchExpenses(
        @RequestParam("userId") Long userId,
        @RequestParam(name = "monthName", required = false) String monthName,
        @RequestParam(name = "categoryName", required = false) String categoryName,
        @RequestParam(name = "expenseName", required = false) String expenseName,
        @RequestParam(name = "fromDate", required = false) String fromDate,
        @RequestParam(name = "toDate", required = false) String toDate,
        @RequestParam(name = "limit", required = false, defaultValue = "20") Integer limit,
        @RequestParam(name = "cursor", required = false) String cursor,
        @RequestParam(name = "sortOrder", required = false, defaultValue = "desc") String sortOrder) {
    return expenseService.searchExpenses(userId, monthName, categoryName, expenseName, fromDate, toDate, limit, cursor, sortOrder);
}
```

---

## Implementation behavior

- If `fromDate`/`toDate` are present, filter by date range.
- If `monthName` is present and date range is not, filter by month.
- If both `monthName` and date range are present, use the intersection of both filters.
- If `expenseName` is present, apply description search using partial matching.
- If `categoryName` is present, apply category filter.
- Use cursor-based pagination. `cursor` should represent the last returned item’s sort key.

---

## Front-end contract

The front-end should use:
- `getExpenses(params)` where `params` can include all supported query fields
- response uses `items`, `nextCursor`, `hasMore`

This keeps the Expense page clean and avoids multiple specialized endpoints.
