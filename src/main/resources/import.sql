-- 
-- El contenido de este fichero se cargará al arrancar la aplicación, suponiendo que uses
-- 		application-default ó application-externaldb en modo 'create'
--

-- Usuario de ejemplo con username = b y contraseña = aa  
INSERT INTO user(id,enabled,username,password,roles,first_name,last_name, address, about_me) VALUES (
	1, 1, 'a', 
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u',
	'USER,ADMIN',
	'Abundio', 'Ejémplez', 'Calle Rotatoria', 'Adoro el modelaje WOW'
);

-- Otro usuario de ejemplo con username = b y contraseña = aa  
INSERT INTO user(id,enabled,username,password,roles,first_name,last_name, address, about_me) VALUES (
	2, 1, 'b', 
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u',
	'USER',
	'Berta', 'Muéstrez', 'Calle Perico', 'Vivan las impresoras 3D'
);

-- Unos pocos auto-mensajes de prueba
INSERT INTO MESSAGE VALUES(1,NULL,'2020-03-23 10:48:11.074000','probando 1',1,1);
INSERT INTO MESSAGE VALUES(2,NULL,'2020-03-23 10:48:15.149000','probando 2',1,1);
INSERT INTO MESSAGE VALUES(3,NULL,'2020-03-23 10:48:18.005000','probando 3',1,1);
INSERT INTO MESSAGE VALUES(4,NULL,'2020-03-23 10:48:20.971000','probando 4',1,1);
INSERT INTO MESSAGE VALUES(5,NULL,'2020-03-23 10:48:22.926000','probando 5',1,1);

--Diseños de prueba
INSERT INTO DESIGN VALUES(1, 'Deporte', 'Hola soy un algo', 123, 'Prueba1', 0, 12, 1, 2);
INSERT INTO DESIGN VALUES(2, 'Accesorios', 'Hola soy un algo1', 123, 'Prueba1', 12, 12, 4, 1);
-- INSERT INTO DESIGN VALUES(3, 'Juguetes', 'Hola soy un algo2', 123, 'Prueba2', 1, 12, 3, 2);

--Impresores de prueba
INSERT INTO PRINTER VALUES(1,90,'Impresor1', 10, 'AVAILABLE', 1);
INSERT INTO PRINTER VALUES(2,100,'Impresor2', 10, 'NO_INK', 2);

-- Compras de prueba
--INSERT INTO design VALUES();
--INSERT INTO SALES VALUES (1, 'Calle ola mayor', 40, 1);

--Sales line de prueba
--INSERT INTO SALES_LINE VALUES (1, sysdate, 1, 40, 2, 1, 1);