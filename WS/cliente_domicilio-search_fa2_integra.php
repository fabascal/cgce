<?php
if(isset($_POST['searchQuery'])){
	$search_query=$_POST['searchQuery'];
	if (isset ($_POST['searchBandera'])){
		$search_bandera=$_POST['searchBandera'];
		if ($search_bandera=='Repsol'){
			require_once('config.inc_fa_33_integra.php');
		}else{
			require_once('config.inc_fa_33.php');
		}
	}else{
		require_once('config.inc_fa_33.php');
	}
$sql = 'SELECT d.*,e.nombre as estado FROM domicilio as d left outer join estado as e on d.id_estado=e.id WHERE d.id_cliente = :search_query';  
$statement = $connection->prepare($sql);
$search_query = $search_query;
$statement->bindParam(':search_query',$search_query, PDO::PARAM_INT);
$statement->execute();
if($statement->rowCount()){
	$row_all = $statement->fetchall(PDO::FETCH_ASSOC);
	header('Content-type: application/json');	  		
	echo json_encode($row_all);
}elseif(!$statement->rowCount()){
	echo "no rows";
	}
}
?>