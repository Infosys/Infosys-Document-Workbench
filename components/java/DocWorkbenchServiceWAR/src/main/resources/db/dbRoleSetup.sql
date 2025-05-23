--DB User #1 - Administrator
CREATE ROLE admin WITH SUPERUSER CREATEDB CREATEROLE LOGIN PASSWORD 'admin';
--DB User #2 - Web Service
CREATE ROLE docwbweb WITH LOGIN PASSWORD 'docwbweb';
--DB User #3 - Engine
CREATE ROLE docwbengine WITH SUPERUSER CREATEDB CREATEROLE LOGIN PASSWORD 'docwbengine';
--DB User #4 - Reporter
CREATE USER reporter WITH PASSWORD 'reporter';
--DB User #5 - Housekeeping user
CREATE USER hkuser PASSWORD 'hkuser';


CREATE DATABASE :postgresDBName OWNER admin;
GRANT ALL ON DATABASE :postgresDBName TO admin;