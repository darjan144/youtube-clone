-- Jutjubić Test Data
INSERT INTO role (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO role (id, name) VALUES (2, 'ROLE_ADMIN');
-- Users with embedded addresses (password: password123 for all)
INSERT INTO users (id, username, password, first_name, last_name, email, enabled, street, city, country, postal_code, created_at, updated_at) VALUES (1, 'admin', '$2a$12$cDNwmmqrdpys6AGjlZNyWuofIKicFZje7YB7Nq7dhYofoyxF1VpCC', 'Admin', 'User', 'admin@jutjubic.com', true, 'Admin Street 1', 'Belgrade', 'Serbia', '11000', NOW(), NOW());
INSERT INTO users (id, username, password, first_name, last_name, email, enabled, street, city, country, postal_code, created_at, updated_at) VALUES (2, 'darjan', '$2a$12$cDNwmmqrdpys6AGjlZNyWuofIKicFZje7YB7Nq7dhYofoyxF1VpCC', 'Darjan', 'Ristic', 'darjan@jutjubic.com', true, 'Cara Dusana 15', 'Novi Sad', 'Serbia', '21000', NOW(), NOW());
INSERT INTO users (id, username, password, first_name, last_name, email, enabled, street, city, country, postal_code, created_at, updated_at) VALUES (3, 'marko', '$2a$10$xQEaXsHCF5dYJcE2dKHCGeXC0JLdw3N5vQq6mB5KfIJCJvJxJCJCa', 'Marko', 'Markovic', 'marko@jutjubic.com', true, 'Knez Mihailova 10', 'Belgrade', 'Serbia', '11000', NOW(), NOW());
INSERT INTO users (id, username, password, first_name, last_name, email, enabled, activation_token, token_expiry_date, street, city, country, postal_code, created_at, updated_at) VALUES (4, 'testuser', '$2a$12$cDNwmmqrdpys6AGjlZNyWuofIKicFZje7YB7Nq7dhYofoyxF1VpCC', 'Test', 'User', 'test@jutjubic.com', false, 'test-activation-token-12345', NOW() + INTERVAL '24 hours', 'Test Street 1', 'Belgrade', 'Serbia', '11000', NOW(), NOW());
-- User Roles
INSERT INTO user_role (user_id, role_id) VALUES (1, 2);
INSERT INTO user_role (user_id, role_id) VALUES (2, 1);
INSERT INTO user_role (user_id, role_id) VALUES (3, 1);
INSERT INTO user_role (user_id, role_id) VALUES (4, 1);
-- Video Tags
INSERT INTO video_tags (id, name) VALUES (1, 'Java');
INSERT INTO video_tags (id, name) VALUES (2, 'Spring Boot');
INSERT INTO video_tags (id, name) VALUES (3, 'Tutorial');
INSERT INTO video_tags (id, name) VALUES (4, 'React');
INSERT INTO video_tags (id, name) VALUES (5, 'JavaScript');
INSERT INTO video_tags (id, name) VALUES (6, 'PostgreSQL');
INSERT INTO video_tags (id, name) VALUES (7, 'Database');
INSERT INTO video_tags (id, name) VALUES (8, 'Backend');
INSERT INTO video_tags (id, name) VALUES (9, 'Frontend');
INSERT INTO video_tags (id, name) VALUES (10, 'Full Stack');
-- Videos
INSERT INTO videos (id, title, description, thumbnail_path, video_path, video_size_mb, view_count, uploader_id, created_at) VALUES (1, 'Java Spring Boot Tutorial for Beginners', 'Complete guide to building REST APIs with Spring Boot 3', '/uploads/thumbnails/spring-boot-thumb.jpg', '/uploads/videos/spring-boot-tutorial.mp4', 150.5, 1250, 2, NOW() - INTERVAL '5 days');
INSERT INTO videos (id, title, description, thumbnail_path, video_path, video_size_mb, view_count, uploader_id, created_at) VALUES (2, 'React Crash Course 2025', 'Learn React from scratch in this comprehensive tutorial', '/uploads/thumbnails/react-thumb.jpg', '/uploads/videos/react-crash-course.mp4', 180.2, 850, 2, NOW() - INTERVAL '3 days');
INSERT INTO videos (id, title, description, thumbnail_path, video_path, video_size_mb, view_count, uploader_id, created_at) VALUES (3, 'PostgreSQL Database Design Best Practices', 'Learn how to design efficient database schemas', '/uploads/thumbnails/postgres-thumb.jpg', '/uploads/videos/postgres-design.mp4', 120.0, 450, 3, NOW() - INTERVAL '2 days');
INSERT INTO videos (id, title, description, thumbnail_path, video_path, video_size_mb, view_count, uploader_id, created_at) VALUES (4, 'Full Stack Development Roadmap 2025', 'Everything you need to know to become a full stack developer', '/uploads/thumbnails/fullstack-thumb.jpg', '/uploads/videos/fullstack-roadmap.mp4', 195.8, 2100, 1, NOW() - INTERVAL '7 days');
INSERT INTO videos (id, title, description, thumbnail_path, video_path, video_size_mb, view_count, uploader_id, created_at) VALUES (5, 'Git and GitHub for Beginners', 'Master version control with Git and GitHub', '/uploads/thumbnails/git-thumb.jpg', '/uploads/videos/git-tutorial.mp4', 90.5, 320, 3, NOW() - INTERVAL '1 day');
-- Video Tags Assignments
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (1, 1);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (1, 2);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (1, 3);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (1, 8);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (2, 4);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (2, 5);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (2, 3);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (2, 9);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (3, 6);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (3, 7);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (3, 8);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (4, 10);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (4, 3);
INSERT INTO video_tag_mapping (video_id, tag_id) VALUES (5, 3);
-- Comments
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (1, 'Great tutorial! Very clear explanations.', 3, 1, NOW() - INTERVAL '4 days');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (2, 'This helped me finally understand Spring Boot. Thanks!', 1, 1, NOW() - INTERVAL '3 days');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (3, 'Could you make a video about Spring Security next?', 2, 1, NOW() - INTERVAL '2 days');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (4, 'Perfect timing! I was just learning React.', 3, 2, NOW() - INTERVAL '2 days');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (5, 'The hooks section was especially helpful.', 1, 2, NOW() - INTERVAL '1 day');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (6, 'Very useful tips on indexing!', 2, 3, NOW() - INTERVAL '1 day');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (7, 'Can you do a video on query optimization?', 1, 3, NOW() - INTERVAL '12 hours');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (8, 'This roadmap is exactly what I needed!', 2, 4, NOW() - INTERVAL '6 days');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (9, 'Bookmarking this for reference.', 3, 4, NOW() - INTERVAL '5 days');
INSERT INTO comments (id, text, author_id, video_id, created_at) VALUES (10, 'Would love to see more content like this.', 2, 4, NOW() - INTERVAL '4 days');

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('videos_id_seq', (SELECT MAX(id) FROM videos));
SELECT setval('comments_id_seq', (SELECT MAX(id) FROM comments));
SELECT setval('video_tags_id_seq', (SELECT MAX(id) FROM video_tags));