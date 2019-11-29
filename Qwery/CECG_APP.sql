USE master;
CREATE DATABASE cecg_app;
	USE cecg_app
	GO
	CREATE TABLE dispositivos (
		id integer IDENTITY(1,1) PRIMARY KEY,
		mac_adr varchar(17) not null,
		activo smallint)
	CREATE TABLE despachadores ( 
		id integer IDENTITY(1,1) PRIMARY KEY,
		nombre varchar(60) not null,
		pass varchar (60) not null)
	CREATE TABLE dispensario (
		id integer IDENTITY(1,1) PRIMARY KEY,
		numero_logico integer not null)
	CREATE TABLE posicion (
		id integer IDENTITY(1,1) PRIMARY KEY,
		numero_logico integer not null,
		id_dispensario integer foreign key references dispensario(id) not null)
	CREATE TABLE zona (
		id integer IDENTITY(1,1) PRIMARY KEY,
		nombre varchar(30) not null)
	CREATE TABLE estacion (
		id integer IDENTITY(1,1) PRIMARY KEY,
		nombre varchar (30) not null,
		zona integer foreign key references zona(id) not null)
	CREATE TABLE corte (
		id integer IDENTITY(1,1) PRIMARY KEY,
		id_despachador integer foreign key references despachadores(id) not null,
		id_dispensario integer foreign key references dispensario(id) not null,
		id_dispositivo integer foreign key references dispositivos(id) not null,
		hora_entrada datetime not null,
		hora_salida datetime null,
		status smallint not null default 0)	
	CREATE TABLE despachos (
		id integer IDENTITY(1,1) PRIMARY KEY,
		nrotrn integer not null,
		nota integer not null,
		corte integer foreign key references corte(id) not null,
		impreso integer not null,
		tipo_venta integer not null,
		flotillero integer not null)
	CREATE TABLE datos_factura (
		id integer IDENTITY(1,1) PRIMARY KEY,
		id_estacion integer foreign key references estacion(id) not null,
		razon_social varchar (60) not null,
		rfc varchar (60) not null,
		calle varchar (60) not null,
		num_exterior varchar (60) not null,
		num_interior varchar (60) not null,
		colonia varchar (60) not null,
		codigo_postal varchar (60) not null,
		localidad varchar (60) not null,
		municipio varchar (60) not null,
		estado varchar (60) not null,
		pais varchar (60) not null,
		telefono varchar (60) not null,
		regimen_fiscal varchar (60) not null,
		cveest varchar (20) not null,
		config_id integer not null)
	CREATE TABLE aceites (
		nrotrn integer IDENTITY (1,1) PRIMARY KEY,
		nota integer,
		codigo varchar(60),
		producto varchar (100),
		precio float,
		cantidad integer,
		corte integer foreign key references corte(id) not null,
		rfc varchar(15),
		tipo_venta integer,
		web int not null default 0,
		cancelado int not null default 0,
		impreso int not null default 0,
		hora_venta datetime not null)
	INSERT INTO zona (nombre)
	VALUES	('OCCIDENTE')
	INSERT INTO zona (nombre)
	VALUES	('OCCIDENTE SUR')
	INSERT INTO zona (nombre)
	VALUES	('BAJIO')
	INSERT INTO estacion (nombre,zona)
	VALUES	('CENTRAL',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('MIRAMAR',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('IXTLAN',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('AHUACATLAN',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CHAPALILLA',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('RANCHO CONTENTO',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('PLANETARIO',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CALZADA',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CRESPO II',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CRESPO I',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('M CELAYA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('24 HORAS',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('TAMAYO',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('PROVIDENCIA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('S APASEO',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('E APASEO',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CONSTITUYENTES',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('SALVATIERRA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('MOLINA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('TECNOLOGICO',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('GASO INDUSTRIAL',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('SAN RAFAEL',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('PRADERAS',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CLOUTHIER',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('PEÑITA',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('VILLANUEVA',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('MEGA SERVICIO',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('GARZAS',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('LA LAJA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CAPRICHO',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('PIPILA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CONTRALORIA BAJIO',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('MAGDALENA',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('RYSVAL',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('DON BOSCO',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('SAN JAVIER',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('IXTLAN II',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('INDEPENDENCIA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('AUTLAN',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('SALVATIERRA II',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('TAMAYO II',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('BONANZA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('PONCITLAN',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('COLON',2)	
	INSERT INTO estacion (nombre,zona)
	VALUES	('AGUAMILPA',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('VILLA DE PURIFICACION',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('MARMOL',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('TONILA',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('Y GRIEGA',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('TALA',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('LA HUERTA',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('LO ARADO',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('CASIMIRO CASTILLO',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('TEQUILA',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('COLOSIO',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('ZAPOTILTIC',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('ULTIMO VAGON',3)
	INSERT INTO estacion (nombre,zona)
	VALUES	('H. CASAS',1)
	INSERT INTO estacion (nombre,zona)
	VALUES	('SAYULA I',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('SAYULA II',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('EL GRULLO',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('MELAQUE',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('EL MENTIDERO',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('UNIVERSIDAD',2)
	INSERT INTO estacion (nombre,zona)
	VALUES	('PALMAS',2)
