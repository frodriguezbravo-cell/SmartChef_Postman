SET search_path TO smartchef;

INSERT INTO usuario (email, username, password_hash, nombre, objetivo_calorias_dia)
VALUES ('demo@smartchef.es', 'demo', '$2a$10$hMUZV3fbBH8mEhEB0cKC0O.mOQ6Vgm.r42XSY7BOvKEu1qDr97I1W', 'Usuario de prueba', 2000);

INSERT INTO despensa (id_usuario, nombre)
VALUES ((SELECT id FROM usuario WHERE username = 'demo'), 'Casa');

INSERT INTO lista_compra (id_usuario, nombre)
VALUES ((SELECT id FROM usuario WHERE username = 'demo'), 'Compra de la semana');

INSERT INTO usuario (email, username, password_hash, nombre, id_rol)
VALUES ('chef@smartchef.es', 'chef', '$2a$10$hMUZV3fbBH8mEhEB0cKC0O.mOQ6Vgm.r42XSY7BOvKEu1qDr97I1W', 'Chef de prueba',
        (SELECT id FROM cat_rol_usuario WHERE codigo = 'CHEF'));

INSERT INTO usuario (email, username, password_hash, nombre, id_rol)
VALUES ('admin@smartchef.es', 'admin', '$2a$10$hMUZV3fbBH8mEhEB0cKC0O.mOQ6Vgm.r42XSY7BOvKEu1qDr97I1W', 'Admin de prueba',
        (SELECT id FROM cat_rol_usuario WHERE codigo = 'ADMIN'));

INSERT INTO coleccion (id_usuario, nombre, es_favoritas)
SELECT id, 'Favoritas', TRUE FROM usuario WHERE username IN ('demo', 'chef', 'admin');
