-- copied from databasesetuppostgres3.sh
--ALTER SCHEMA docwbmain OWNER TO admin;

--1. USAGE settings 
--schema=docwbmain
GRANT USAGE ON SCHEMA docwbmain TO docwbweb;
GRANT USAGE ON SCHEMA docwbmain TO reporter;
GRANT USAGE ON SCHEMA docwbmain TO hkuser;
--docwbmain=workflow
GRANT USAGE ON SCHEMA workflow TO docwbengine;
GRANT USAGE ON SCHEMA workflow TO reporter;
GRANT USAGE ON SCHEMA workflow TO hkuser;


--2a. READ/WRITE
--User #2 
--schema=docwbmain
--For existing tables and sequences
GRANT ALL ON ALL TABLES IN SCHEMA docwbmain TO docwbweb;
GRANT ALL ON ALL SEQUENCES IN SCHEMA docwbmain TO docwbweb;
--For future tables and sequences not yet created
ALTER DEFAULT PRIVILEGES IN SCHEMA docwbmain GRANT ALL ON TABLES TO docwbweb;
ALTER DEFAULT PRIVILEGES IN SCHEMA docwbmain GRANT ALL ON SEQUENCES TO docwbweb;
--User #3
--schema=workflow
--For existing tables and sequences
GRANT ALL ON ALL TABLES IN SCHEMA workflow TO docwbengine;
GRANT ALL ON ALL SEQUENCES IN SCHEMA workflow TO docwbengine;
--For future tables and sequences not yet created
ALTER DEFAULT PRIVILEGES IN SCHEMA workflow GRANT ALL ON TABLES TO docwbengine;
ALTER DEFAULT PRIVILEGES IN SCHEMA workflow GRANT ALL ON SEQUENCES TO docwbengine;


--2b. READ-ONLY
--User #4
--schema=docwbmain
--For existing tables and sequences
GRANT SELECT ON ALL TABLES IN SCHEMA docwbmain TO reporter;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA docwbmain TO reporter;
--For future tables and sequences not yet created
ALTER DEFAULT PRIVILEGES IN SCHEMA docwbmain GRANT SELECT ON TABLES TO reporter;
ALTER DEFAULT PRIVILEGES IN SCHEMA docwbmain GRANT SELECT ON SEQUENCES TO reporter;
--schema=workflow
--For existing tables and sequences
GRANT SELECT ON ALL TABLES IN SCHEMA workflow TO reporter;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA workflow TO reporter;
--For future tables and sequences not yet created
ALTER DEFAULT PRIVILEGES IN SCHEMA workflow GRANT SELECT ON TABLES TO reporter;
ALTER DEFAULT PRIVILEGES IN SCHEMA workflow GRANT SELECT ON SEQUENCES TO reporter;
--User #5
--schema=docwbmain
--For existing tables and sequences
GRANT SELECT ON ALL TABLES IN SCHEMA docwbmain TO hkuser;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA docwbmain TO hkuser;
--For future tables and sequences not yet created
ALTER DEFAULT PRIVILEGES IN SCHEMA docwbmain GRANT SELECT ON TABLES TO hkuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA docwbmain GRANT SELECT ON SEQUENCES TO hkuser;
--schema=workflow
--For existing tables and sequences
GRANT SELECT ON ALL TABLES IN SCHEMA workflow TO hkuser;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA workflow TO hkuser;
--For future tables and sequences not yet created
ALTER DEFAULT PRIVILEGES IN SCHEMA workflow GRANT SELECT ON TABLES TO hkuser;
ALTER DEFAULT PRIVILEGES IN SCHEMA workflow GRANT SELECT ON SEQUENCES TO hkuser;