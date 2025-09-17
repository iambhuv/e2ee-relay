CREATE KEYSPACE IF NOT EXISTS p2pe2e WITH REPLICATION = {
  'class': 'SimpleStrategy',
  'replication_factor': 3
};

CREATE TABLE IF NOT EXISTS p2pe2e.known_users (
  public_key blob,
  created_at timestamp,
  PRIMARY KEY (public_key)
);