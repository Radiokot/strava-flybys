# Unofficial Strava Flybys
Find out Flybys by Summit segment leaderboards. Get lost if you are Andrew Seward

Build
```bash
./gradlew shadowJar
```

Run
```bash
EMAIL=%summit_email% \
PASSWORD=%summit_password% \
GOOGLE_API_KEY=%key_for_google_maps% \
PORT=%desired_http_port% \
java -jar build/libs/server.jar
```
