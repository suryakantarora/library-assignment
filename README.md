# Library Management

Angular UI + Spring Boot REST API with an in-memory store. All state lives in the running process, so every start is clean.

## Run

```bash
./start.sh
```

Open http://localhost:9999 (set `PORT=8080 ./start.sh` to use another port).

`start.sh` installs and builds the Angular app, copies it into the Spring Boot static resources, builds the jar with the Maven wrapper and starts it. Needs Java 21+ and Node.js/npm.

## Tests

```bash
cd backend && ./mvnw test
cd frontend && npm test -- --watch=false
```

## Usage

Log in with any name (new names are registered on first login). Log in as `admin` to add books. Other users can borrow, return, join a waitlist and see their status.

| Command    | In the UI                                   | Endpoint                  |
|------------|---------------------------------------------|---------------------------|
| `login`    | Login page                                  | `POST /api/auth/login`    |
| `add`      | "Add a book" form (admin only)              | `POST /api/books`         |
| `list`     | Books table                                 | `GET /api/books`          |
| `borrow`   | Borrow button                               | `POST /api/borrow`        |
| `return`   | Return button                               | `POST /api/return`        |
| `waitlist` | Join waitlist button                        | `POST /api/waitlist`      |
| book status | Details link on a book                     | `GET /api/books/detail`   |
| `status`   | "My status" panel                           | `GET /api/status`         |
| `logout`   | Logout button                               | `POST /api/auth/logout`   |

## Layout

```
backend/    Spring Boot (controller -> service -> in-memory repository)
frontend/   Angular app
start.sh
```

## Decisions and assumptions

- Login is by name only, no password. A random token is issued per login and sent as `X-Auth-Token`.
- Usernames and book titles are matched ignoring case and surrounding spaces. The first spelling is kept for display.
- When a book is returned and people are waiting, it is handed to the first in line immediately. They see "You got the book ... from your waitlist" the next time they log in.
- Waitlisting a book that is available, already borrowed by you, or already waitlisted by you is rejected with a message.
- Borrowing a book you already hold is rejected. Adding a title that already exists is rejected.
- After login, members see the spec's summary (borrowed books, wait lists, waitlist notifications). Book details show the waitlist size and your position; only `admin` also sees who holds the book.
- `admin` manages books only and cannot borrow, return or waitlist.
