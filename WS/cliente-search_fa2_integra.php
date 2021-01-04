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
	$sql = 'select * from clientespg where lower(rfc) like lower(:search_query) or lower(nombre) like lower(:search_query) limit 10';
	//$sql = "select * from 'clientespg' limit 10";
	$search_query = "%".$search_query."%";
	$statement = $conexion->prepare($sql);
	$statement->bindParam(':search_query',$search_query, PDO::PARAM_STR);
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