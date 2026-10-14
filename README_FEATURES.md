# Online Reservation System - Updated Project

## Included in this updated ZIP
- Existing Spring Boot + Thymeleaf + MySQL reservation application retained.
- Train search page: `/trains/search` (optional source/destination filters).
- Admin train management: `/admin/trains` (list/add/update via form/delete; protected by `ROLE_ADMIN`).
- My Bookings now filters reservations to the currently authenticated username rather than showing every user's bookings.
- Default seeded admin account role corrected to `ADMIN` for fresh databases.
- Responsive standalone templates for train search and admin train management.

## Run
1. Install JDK 17+, Maven (or use Eclipse/STS Maven support), and MySQL.
2. Create database `reservation_db` or allow the configured URL to create it.
3. Update `src/main/resources/application.properties` with your own MySQL username/password. Never commit real credentials.
4. Import the root folder as an Existing Maven Project in Eclipse/STS.
5. Run `OnlineReservationApplication.java` and open `http://localhost:8081`.
6. Fresh database seeded admin credentials: username `admin`, password `Admin@123`. Change this password immediately. If the `admin` row already exists, the initializer does not change its role/password; update its role to `ADMIN` in the database or create an admin user securely.

## New pages
- `/trains/search`
- `/admin/trains` (ADMIN role only)
- `/my-bookings` (current user's reservations)

## Important implementation notes
This is a learning/demo project, not a production railway booking system. Demo payment gateway, real email delivery, downloadable PDF ticket, waiting-list promotion, full fare rules, comprehensive admin booking reports, and Docker-based end-to-end verification are not configured in this package. Do not treat a simulated payment as a real payment. Seat allocation and cancellation should receive concurrency and ownership checks before any real deployment. Review all controllers/services and run tests locally against your own MySQL setup before relying on the application.
