-- Refresh tokens révoqués (déconnexion) : auth-service refuse ensuite de les rafraîchir.
-- On n'y stocke que l'empreinte SHA-256 du jeton (64 caractères hexadécimaux), jamais le jeton.
-- Table requise par POST /api/v1/auth/refresh et /logout (RevokedTokenEntity) : sans elle,
-- le rafraîchissement échoue en erreur 500.
CREATE TABLE IF NOT EXISTS revoked_tokens (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    token_hash VARCHAR(64) NOT NULL UNIQUE,

    expires_at TIMESTAMPTZ NOT NULL

);
