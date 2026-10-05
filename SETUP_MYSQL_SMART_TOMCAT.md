# EduTrack — MySQL + Smart Tomcat Setup

## 1. MySQL
1. Open MySQL Workbench and connect to your local MySQL server.
2. Open `database/schema.sql` and execute the whole file.
3. Open `database/seed.sql` and execute the whole file.
4. Confirm:
   ```sql
   USE edutrack;
   SHOW TABLES;
   SELECT id, name FROM roles;
   ```
   The first role must be `ADMIN`.

## 2. Database password
Open:
`src/main/resources/db.properties`

Set:
```properties
db.username=root
db.password=YOUR_MYSQL_PASSWORD
```

Do not put quotes around the password.

## 3. IntelliJ + Smart Tomcat
This project is configured for Java 21 and Tomcat 10.1+.

Smart Tomcat:
- Tomcat server: Tomcat 10.1.x
- Deployment directory: `src/main/webapp`
- Module classpath: `edutrack`
- Context path: `/edutrack`
- Server port: `8080`
- Admin port: `8005`

Run the Smart Tomcat configuration.

## 4. Correct browser address
Use:
`http://localhost:8080/edutrack/`

Do NOT use:
`http://localhost:8080/edutrack/edutrack/`

The project now also normalizes an accidentally duplicated context path.

## 5. Maven
You can use IntelliJ's Maven panel:
`Maven -> EduTrack -> Lifecycle -> clean`
then:
`Maven -> EduTrack -> Lifecycle -> package`

The project is a WAR project and produces:
`target/edutrack.war`

Smart Tomcat can run directly from the webapp source, so you do not need to manually copy the WAR when using Smart Tomcat.

## 6. Demo accounts
All demo accounts use:
`EduTrack123`

Examples:
- admin / EduTrack123
- principal / EduTrack123
- coordinator / EduTrack123
- teacher1 / EduTrack123
- finance / EduTrack123
- student1 / EduTrack123
- parent1 / EduTrack123

## Important
If you changed your MySQL root password, only `db.properties` needs to be updated. If you re-run `schema.sql`, it intentionally recreates the `edutrack` database and removes existing demo/application data.
