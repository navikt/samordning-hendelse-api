path "sys/renew/*" {
  capabilities = ["update"]
}

path "secrets/test/creds/user" {
  capabilities = ["user"]
}