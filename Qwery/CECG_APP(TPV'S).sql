USE cecg_app
go

CREATE TABLE tpv (
id integer IDENTITY (1,1) PRIMARY KEY,
nombre varchar(60),
bancaria smallint,
activo smallint,
)
INSERT INTO tpv (nombre,bancaria,activo)
	VALUES	('BANAMEX',1,1)
INSERT INTO tpv (nombre,bancaria,activo)
	VALUES	('SODEXO',0,1)
INSERT INTO tpv (nombre,bancaria,activo)
	VALUES	('EFECTICARD',0,1)
INSERT INTO tpv (nombre,bancaria,activo)
	VALUES	('ULTRAGAS',0,1)
INSERT INTO tpv (nombre,bancaria,activo)
	VALUES	('INBURSA',0,1)
INSERT INTO tpv (nombre,bancaria,activo)
	VALUES	('TICKETCAR',0,1)


ALTER TABLE despachos
ADD tpv_id integer foreign key references tpv(id)

