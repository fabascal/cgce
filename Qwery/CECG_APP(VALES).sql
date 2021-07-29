use cecg_app
go
-- check to see if table exists in sys.tables - ignore DROP TABLE if it does not
IF EXISTS(SELECT * FROM sys.tables WHERE SCHEMA_NAME(schema_id) LIKE 'dbo' AND name like 'vale')  
   DROP TABLE [dbo].[vale];  
GO
CREATE TABLE vale (
id integer IDENTITY (1,1) PRIMARY KEY,
codcli integer,
dencli varchar(150),
urlfile varchar(150),
)
INSERT INTO vale (codcli)
	VALUES	(0)


