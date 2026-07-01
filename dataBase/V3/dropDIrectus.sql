
CREATE SCHEMA IF NOT EXISTS directus;

DROP TABLE IF EXISTS directus.directus_dashboards CASCADE;
DROP TABLE IF EXISTS directus.directus_panels CASCADE;
DROP TABLE IF EXISTS directus.directus_presets CASCADE;
DROP TABLE IF EXISTS directus.directus_shares CASCADE;
DROP TABLE IF EXISTS directus.directus_flows CASCADE;
DROP TABLE IF EXISTS directus.directus_operations CASCADE;
DROP TABLE IF EXISTS directus.directus_webhooks CASCADE;
DROP TABLE IF EXISTS directus.directus_versions CASCADE;
DROP TABLE IF EXISTS directus.directus_activity CASCADE;
DROP TABLE IF EXISTS directus.directus_notifications CASCADE;
DROP TABLE IF EXISTS directus.directus_revisions CASCADE;
DROP TABLE IF EXISTS directus.directus_relations CASCADE;
DROP TABLE IF EXISTS directus.directus_permissions CASCADE;
DROP TABLE IF EXISTS directus.directus_fields CASCADE;
DROP TABLE IF EXISTS directus.directus_collections CASCADE;
DROP TABLE IF EXISTS directus.directus_users CASCADE;
DROP TABLE IF EXISTS directus.directus_roles CASCADE;
DROP TABLE IF EXISTS directus.directus_sessions CASCADE;
DROP TABLE IF EXISTS directus.directus_files CASCADE;
DROP TABLE IF EXISTS directus.directus_folders CASCADE;
DROP TABLE IF EXISTS directus.directus_settings CASCADE;
DROP TABLE IF EXISTS directus.directus_translations CASCADE;
DROP TABLE IF EXISTS directus.directus_extensions CASCADE;
DROP TABLE IF EXISTS directus.directus_migrations CASCADE;

-- Tablas de despliegues y políticas (con CASCADE barren con los nudos ciegos)
DROP TABLE IF EXISTS directus.directus_deployment_runs CASCADE;
DROP TABLE IF EXISTS directus.directus_deployments CASCADE;
DROP TABLE IF EXISTS directus.directus_comments CASCADE;
DROP TABLE IF EXISTS directus.directus_deployment_projects CASCADE;
DROP TABLE IF EXISTS directus.directus_policies CASCADE;
DROP TABLE IF EXISTS directus.directus_access CASCADE;

