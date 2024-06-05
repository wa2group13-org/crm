# Content Relationship Management

- Repository: [GitHub](https://github.com/polito-WAII-2024/lab3-g13)

# Environmental variable

- `POSTGRES_URL`: url of the Postgres server
- `POSTGRES_PORT`: port of the Postgres server
- `POSTGRES_DB`: name of the Postgres database
- `POSTGRES_USERNAME`: username of the Postgers user
- `POSTGRES_PASSWORD`: password of the Postgres user
- `SPRING_PROFILES_ACTIVE`: Spring comma-separated profiles, by default `dev` and `no-security` are selected. List of
  profiles:
    - `dev`: db logs and errors are returned on responses
    - `prod`: need to specify postgres connections
    - `no-security`: filers are disables
- `JWT_ISSUER_URI`: uri of the JWT issuer, e.g. `http://keycloak:9090/realms/app`
