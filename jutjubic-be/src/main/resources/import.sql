-- Insert roles
INSERT INTO role (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO role (id, name) VALUES (2, 'ROLE_ADMIN');

-- Insert users (password is 'password123' encoded with BCrypt)
INSERT INTO users (id, username, password, first_name, last_name, email, enabled, last_password_reset_date, activation_token, token_expiry_date, created_at, updated_at, street, city, country, postal_code) VALUES (1, 'admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Admin', 'User', 'admin@jutjubic.com', true, CURRENT_TIMESTAMP, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Bulevar Oslobodjenja 46', 'Novi Sad', 'Serbia', '21000');

INSERT INTO users (id, username, password, first_name, last_name, email, enabled, last_password_reset_date, activation_token, token_expiry_date, created_at, updated_at, street, city, country, postal_code) VALUES (2, 'darjan', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Darjan', 'Ristic', 'darjan@jutjubic.com', true, CURRENT_TIMESTAMP, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Cara Dusana 15', 'Novi Sad', 'Serbia', '21000');

INSERT INTO users (id, username, password, first_name, last_name, email, enabled, last_password_reset_date, activation_token, token_expiry_date, created_at, updated_at, street, city, country, postal_code) VALUES (3, 'marko', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Marko', 'Markovic', 'marko@jutjubic.com', true, CURRENT_TIMESTAMP, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Kralja Petra 10', 'Belgrade', 'Serbia', '11000');

INSERT INTO users (id, username, password, first_name, last_name, email, enabled, last_password_reset_date, activation_token, token_expiry_date, created_at, updated_at, street, city, country, postal_code) VALUES (4, 'ana', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Ana', 'Jovanovic', 'ana@jutjubic.com', true, CURRENT_TIMESTAMP, null, null, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Kneza Milosa 20', 'Belgrade', 'Serbia', '11000');

INSERT INTO users (id, username, password, first_name, last_name, email, enabled, last_password_reset_date, activation_token, token_expiry_date, created_at, updated_at, street, city, country, postal_code) VALUES (5, 'nikola', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'Nikola', 'Nikolic', 'nikola@jutjubic.com', false, CURRENT_TIMESTAMP, 'activation-token-123', CURRENT_TIMESTAMP + INTERVAL '24 hours', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Zmaj Jovina 5', 'Novi Sad', 'Serbia', '21000');

-- Assign roles to users
INSERT INTO user_role (user_id, role_id) VALUES (1, 2);
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);
INSERT INTO user_role (user_id, role_id) VALUES (2, 1);
INSERT INTO user_role (user_id, role_id) VALUES (3, 1);
INSERT INTO user_role (user_id, role_id) VALUES (4, 1);
INSERT INTO user_role (user_id, role_id) VALUES (5, 1);

-- Reset sequence for auto-increment
ALTER SEQUENCE role_id_seq RESTART WITH 3;
ALTER SEQUENCE users_id_seq RESTART WITH 6;