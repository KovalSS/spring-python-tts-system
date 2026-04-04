# Frontend React (Lab 7 + Lab 8)

This UI is a thin client for the TTS backend:

- creates jobs via text file upload
- shows per-user jobs and statuses
- starts jobs, downloads results, deletes jobs
- receives live status updates over WebSocket (STOMP + SockJS)
- falls back to REST polling when live connection is offline/reconnecting

## Run

1. Start backend/infrastructure first (`backend-spring`, RabbitMQ, MinIO, PostgreSQL).
2. Start frontend:

```bash
npm install
npm start
```

The app opens on `http://localhost:3000` and proxies REST/WS traffic to `http://localhost:8080`.

## Optional env overrides

- `REACT_APP_API_BASE_URL` (default: empty, uses CRA proxy)
- `REACT_APP_WS_URL` (default: `/ws`)

## Test

```bash
npm test -- --watchAll=false
```
