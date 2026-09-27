CREATE TABLE accounts (

    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    first_name VARCHAR(100) NOT NULL,

    last_name VARCHAR(100) NOT NULL,

    email VARCHAR(255) NOT NULL UNIQUE,

    phone VARCHAR(30) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    birth_date DATE,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    verified BOOLEAN NOT NULL DEFAULT FALSE,

    locked BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP
);