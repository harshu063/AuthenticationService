ALTER TABLE `user`
    MODIFY email VARCHAR(320) NOT NULL,
    MODIFY password VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE `role`
    MODIFY role VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uc_role_name UNIQUE (role);

ALTER TABLE client
    MODIFY client_id VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uc_client_client_id UNIQUE (client_id);

CREATE INDEX ix_authorization_registered_client_id ON authorization (registered_client_id);
CREATE INDEX ix_authorization_principal_name ON authorization (principal_name);
