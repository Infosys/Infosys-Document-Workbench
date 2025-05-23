SET search_path TO docwbmain;

select * from app_user; --Should return zero rows

UPDATE app_user SET account_enabled=true WHERE app_user_id=1000;

select * from app_user_role_rel; --Should return zero rows

INSERT INTO app_user_role_rel(app_user_id, role_type_cde, tenant_id) VALUES
(1000, 101, (SELECT tenant_id FROM tenant WHERE tenant_num = 1000));

