USE cecg_app
go

CREATE TABLE bandera (
id integer IDENTITY (1,1) PRIMARY KEY,
nombre varchar(60),
uso smallint,
urltimbre varchar (60),
activo smallint,
)
INSERT INTO bandera (nombre,uso,urltimbre,activo)
	VALUES	('Combu-Express',0,'http://factura.combuexpress.mx/cefactura3.3/timbrarws1.3.php',1)
INSERT INTO bandera (nombre,activo)
	VALUES	('Repsol',1,'http://factura.combuexpress.mx/cerepsol/timbrarws1.3.php',1)

