# Content Relationship Management

- Repository: [GitHub](https://github.com/polito-WAII-2024/lab3-g13)

# Environmental variable

- `POSTGRES_URL`: url of the Postgres server
- `POSTGRES_PORT`: port of the Postgres server
- `POSTGRES_DB`: name of the Postgres database
- `POSTGRES_USERNAME`: username of the Postgers user
- `POSTGRES_PASSWORD`: password of the Postgres user
- `SPRING_PROFILES_ACTIVE`: Spring comma-separated profiles. Default: `dev`, `api-docs`, `no-security`. Values:
    - `dev`: db logs and errors are returned on responses
    - `prod`: need to specify postgres connections
    - `no-security`: filers are disables
    - `api-docs`: enable `/v3/api-docs` and `/swagger-ui.html` endpoints
- `JWT_ISSUER_URI`: uri of the JWT issuer, e.g. `http://keycloak:9090/realms/app`
- `KAFKA_CONSUMER_BOOTSTRAP_SERVERS`: consumer bootstrap servers for Apache Kafka
- `MAIL_TOPIC`: name of the mail topic. Default: `mail.json`
