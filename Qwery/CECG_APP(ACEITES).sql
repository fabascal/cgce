USE cecg_app

ALTER TABLE datos_factura
ADD config_id integer


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
impreso int not null default 0
)
