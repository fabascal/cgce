USE cecg_app
go

CREATE TABLE tpv (
id integer IDENTITY (1,1) PRIMARY KEY,
nombre varchar(60),
se_factura smallint,
activo smallint,
)
INSERT INTO tpv (nombre,se_factura,activo)
	VALUES	('BANAMEX',1,1)
INSERT INTO tpv (nombre,se_factura,activo)
	VALUES	('SANTANDER',1,1)
INSERT INTO tpv (nombre,se_factura,activo)
	VALUES	('ACCORD',0,1)
INSERT INTO tpv (nombre,se_factura,activo)
	VALUES	('EFECTIVALE',0,1)
INSERT INTO tpv (nombre,se_factura,activo)
	VALUES	('ULTRA-GAS',0,1)


ALTER TABLE despachos
ADD tpv_id integer foreign key references tpv(id)
