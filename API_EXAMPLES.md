# Postman / curl examples

## Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"employee@company.com","password":"password123"}'
```

The response contains `userId`; use it in booking requests.

## Create route A -> B -> C -> D
```bash
curl -u employee@company.com:password123 \
  -X POST http://localhost:8080/api/routes \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Office Shuttle",
    "stops": [
      {"name": "A", "arrivalTime": "10:00:00"},
      {"name": "B", "arrivalTime": "10:15:00"},
      {"name": "C", "arrivalTime": "10:30:00"},
      {"name": "D", "arrivalTime": "10:45:00"}
    ]
  }'
```

## Create trip
```bash
curl -u employee@company.com:password123 \
  -X POST http://localhost:8080/api/trips \
  -H 'Content-Type: application/json' \
  -d '{"routeId":1,"tripDate":"2026-09-22","seatCount":2}'
```

## Check availability
```bash
curl -u employee@company.com:password123 \
  'http://localhost:8080/api/trips/1/availability?fromOrder=0&toOrder=1'
```

## Book a segment
```bash
curl -u employee@company.com:password123 \
  -X POST http://localhost:8080/api/trips/1/bookings \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"fromOrder":0,"toOrder":1}'
```

## Join waitlist
```bash
curl -u employee@company.com:password123 \
  -X POST http://localhost:8080/api/trips/1/waitlist \
  -H 'Content-Type: application/json' \
  -d '{"userId":2,"fromOrder":1,"toOrder":3}'
```

## Cancel
```bash
curl -u employee@company.com:password123 -X DELETE http://localhost:8080/api/bookings/1
```

## Mark no-show
```bash
curl -u employee@company.com:password123 -X POST http://localhost:8080/api/bookings/1/no-show
```

## Segment reuse demo
```bash
# Seat 1 can be reused because A->B and B->D only touch at stop B.
curl -u employee@company.com:password123 \
  -X POST http://localhost:8080/api/trips/1/bookings \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"fromOrder":0,"toOrder":1}'

curl -u employee@company.com:password123 \
  -X POST http://localhost:8080/api/trips/1/bookings \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"fromOrder":1,"toOrder":3}'
```
