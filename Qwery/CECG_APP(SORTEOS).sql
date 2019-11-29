USE cecg_app
go

CREATE TABLE sorteo (
id integer IDENTITY (1,1) PRIMARY KEY,
nombre varchar(60),
activo smallint,
fecha_inicio date,
fecha_fin date,
)
INSERT INTO sorteo (nombre,activo,fecha_inicio,fecha_fin)
	VALUES	('Gana con combu',1,'2019-11-24','2019-12-31')
