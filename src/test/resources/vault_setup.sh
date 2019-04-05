#!/bin/bash
vault secrets enable -path=secrets/test database
vault write secrets/test/config/testdb \
  allowed_roles="user" \
  plugin_name=postgresql-database-plugin \
  connection_url="postgresql://{{username}}:{{password}}@$DB_NAME:5432/$DB_NAME?sslmode=disable" \
  username="$DB_USERNAME" \
  password="$DB_PASSWORD"
vault write secrets/test/roles/user \
    db_name=testdb \
    creation_statements="CREATE ROLE \"{{name}}\" WITH SUPERUSER LOGIN PASSWORD '{{password}}' VALID UNTIL '{{expiration}}'" \
    default_ttl="1m" \
    max_ttl="1m"
vault policy write db policy_db.hcl
vault token create -policy=db -ttl=768h -period=15s -field token