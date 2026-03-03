-- Script de création de la table transco_rule
-- À exécuter sur PostgreSQL avant de démarrer l'application

CREATE TABLE IF NOT EXISTS transco_rule (
    id           SERIAL PRIMARY KEY,
    context      VARCHAR(100)        NOT NULL,
    inputs       JSONB               NOT NULL,
    output_value TEXT                NOT NULL,
    priority     INT                 NOT NULL DEFAULT 0,
    created_at   TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP           NOT NULL DEFAULT NOW()
);

-- Index GIN pour les recherches JSONB performantes (opérateur @>)
CREATE INDEX IF NOT EXISTS idx_transco_inputs
    ON transco_rule USING GIN (inputs);

-- Contrainte d'unicité : pas deux règles identiques dans le même contexte
CREATE UNIQUE INDEX IF NOT EXISTS idx_transco_unique
    ON transco_rule (context, inputs);

-- Trigger de mise à jour automatique du champ updated_at
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_transco_rule_updated_at ON transco_rule;
CREATE TRIGGER trg_transco_rule_updated_at
    BEFORE UPDATE ON transco_rule
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Données de démonstration
INSERT INTO transco_rule (context, inputs, output_value, priority) VALUES
-- Règles simples : 1 entrée
('pays_vers_devise', '{"pays": "FR"}', 'EUR', 10),
('pays_vers_devise', '{"pays": "GB"}', 'GBP', 10),
('pays_vers_devise', '{"pays": "US"}', 'USD', 10),

-- Règles multi-entrées : 2 entrées
('pays_type_vers_tva', '{"pays": "FR", "type_produit": "alimentaire"}', '5.5',  20),
('pays_type_vers_tva', '{"pays": "FR", "type_produit": "standard"}',    '20.0', 20),
('pays_type_vers_tva', '{"pays": "DE", "type_produit": "standard"}',    '19.0', 20),

-- Règles avec fallback : du plus spécifique au plus générique
('tarif', '{"pays": "FR", "canal": "web", "client": "premium"}', 'TARIF_A', 30),
('tarif', '{"pays": "FR", "canal": "web"}',                      'TARIF_B', 20),
('tarif', '{"pays": "FR"}',                                       'TARIF_C', 10),
('tarif', '{}',                                                   'TARIF_DEFAULT', 0)

ON CONFLICT DO NOTHING;

ALTER TABLE transco_rule ALTER COLUMN id TYPE BIGINT;

CREATE TABLE IF NOT EXISTS api_key (
    id          BIGSERIAL    PRIMARY KEY,
    client_name VARCHAR(100) NOT NULL,
    key_hash    CHAR(64)     NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_api_key_hash ON api_key (key_hash);