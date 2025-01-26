CREATE KEYSPACE IF NOT EXISTS spring_cassandra
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};