# EduTrack – Microsoft SQL Server setup

1. Install Microsoft SQL Server and SQL Server Management Studio (SSMS).
2. Make sure TCP/IP is enabled for your SQL Server instance and note its port (the project defaults to 1433).
3. Open `database/schema.sql` in SSMS and execute it.
4. Open `database/seed.sql` and execute it against the `edutrack` database.
5. Edit `src/main/resources/db.properties` and set your SQL Server username/password.
6. In IntelliJ, reload Maven, use JDK 21, and deploy to Tomcat 10.1+.

The project uses Microsoft JDBC Driver 13.4.0 (`mssql-jdbc`), which supports JDK 21.
