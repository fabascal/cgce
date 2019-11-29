<?php
error_reporting (E_ALL & ~E_STRICT ^ E_NOTICE );
include("include/conecta.php");

require_once 'vendor/autoload.php';
use SWServices\Stamp\StampService as StampService;

if(isset($_POST['id_cliente']) && isset($_POST['id_domicilio']) && isset($_POST['id_formpago'])  && isset($_POST['cveest']) && isset($_POST['ticket']))
{

$fecha_alta     =date("Y-m-d H:i:s");
$id_cliente     =$_POST['id_cliente'];
$id_domicilio   =$_POST['id_domicilio'];
$id_formpago    =$_POST['id_formpago'];
$cveest         =$_POST['cveest'];
$ticket         =$_POST['ticket'];
$fecha_ticket   =$_POST['fecha_ticket'];
$id_producto    =$_POST['id_producto'];
$bomba          =$_POST['bomba'];
$preunitario    =$_POST['preunitario'];
$mtogto         =$_POST['mtogto'];
$importe        =$_POST['importe'];
$comentarios    =$_POST['comentarios'];
$nip            =$_POST['nip'];	
$usocfdi        =$_POST['usocfdi'];	

            $sTicket=mysqli_query($link,"select * from tickets3_3 where ticket='$ticket' and cveest='$cveest'");
			$dTicket=mysqli_fetch_assoc($sTicket);

            if($dTicket){

			$jsondata['mensaje'] ="El ticket [ ".$ticket." ] ya esta facturado";
				
			}else{


            $sProd=mysqli_query($link,"select * from productos3_3 where id_producto='$id_producto'");
			$dProd=mysqli_fetch_assoc($sProd);

            $claveprodserv  =$dProd['claveprodserv'];
			$claveunidad    =$dProd['claveunidad'];
		    $unidad_medida  =$dProd['unidad_medida'];
			$descripcion    =$dProd['descripcion'];
			$impuestofijo   =$dProd['impuestofijo'];
			$tipofactorfijo =$dProd['tipofactorfijo'];
			$tasacuotafijo  =$dProd['tasacuotafijo'];
			$ieps           =$dProd['ieps'];

            $sClie=mysqli_query($link,"select id_cliente,nombre,rfc,correo,telefono from clientes where id_cliente='$id_cliente'");
			$dClie=mysqli_fetch_assoc($sClie);

            $Rrfc=$dClie['rfc'];
			$Rnombre=$dClie['nombre'];
			

$sEst=mysqli_query($link,"SELECT d.id as id_despachador,d.nip,e.id as id_estacion,e.serie,e.cp,e.cuenta_correo FROM despachadores as d left outer join estaciones as e on e.id=d.id_estacion where d.nip='$nip' and e.pemex='$cveest' ");
$dEst=mysqli_fetch_assoc($sEst);				

				
			$id_despachador=$dEst['id_despachador'];	
            $id_estacion=$dEst['id_estacion'];
            $serie=$dEst['serie'];
            $LugarExpedicion=$dEst['cp'];
		    $cuentacorreo=$dEst['cuenta_correo'];

            $sForm=mysqli_query($link,"select * from forma_pago where id='$id_formpago'");
			$dForm=mysqli_fetch_assoc($sForm);

            $formapago=$dForm['clave'];
	
				

$fecha=@date("Y-m-d");
$hora=@date("H:i:s");
$T_fecha_hora=$fecha."T".$hora;

//$T_fecha_hora=$fecha."T".$hora;

$sEmpresa=mysqli_query($link,"select * from empresa where id=1");
$dEmp=mysqli_fetch_assoc($sEmpresa);
	
$llave           = 'llavecombu.key.pem';
$TnoCertificado  = "00001000000403081928";
$id_empresa      = $dEmp['id'];	
$regimenfiscal   = $dEmp['clave_regimen'];
$Erfc            = $dEmp['rfc'];
$Enombre_emisor  = $dEmp['nombre_empresa'];
$metodopago      = 'PUE';
$moneda          = 'MXN';
$tipocomprobante = 'I';
				
$Dcalle=trim($dEmp['calle']);
$DnoExterior=trim($dEmp['numero_exterior']);
$Dcolonia=trim($dEmp['colonia']);
$Dlocalidad=trim($dEmp['localidad']);
$Dmunicipio=trim($dEmp['municipio']);
$Destado=trim($dEmp['estado']);
$Dpais=trim($dEmp['pais']);
$DcodigoPostal=trim($dEmp['cp']);

$Tcertificado="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";
				
$monto=$importe+abs($mtogto);				
$unitario=(($preunitario-$ieps)/1.16)+$ieps;
$cantidad=$monto/$preunitario;
$subtotal=$cantidad*$unitario;
$iva=($subtotal-($ieps*$cantidad))*0.16;
$cuotaieps=$cantidad*$ieps;
$base=$subtotal-$cuotaieps;
$total=$subtotal+$iva;				
					
		
		$sql=mysqli_query($link,"insert into facturas3_3 (id_estacion,folio,estatus,fecha_alta)values('$id_estacion','$folio',0,'$fecha_alta')");

	    $id_factura=mysqli_insert_id($link);
				
	    $numfactura=$serie.$id_factura;

$conceptos=$claveprodserv."|".@number_format($cantidad,5,".","")."|".$claveunidad."|".$unidad_medida."|".$descripcion."|".@number_format($unitario,5,".","")."|".@number_format($subtotal,2,".","")."|".@number_format($base,2,".","")."|".$impuestofijo."|".$tipofactorfijo."|".$tasacuotafijo."|".@number_format($iva,2,".","");
			
$cadenaoriginal='||3.3|'.$serie.'|'.$id_factura.'|'.$T_fecha_hora.'|'.$formapago.'|'.$TnoCertificado.'|'.@number_format($subtotal,2,".","").'|'.$moneda.'|'.@number_format($total,2,".","").'|'.$tipocomprobante.'|'.$metodopago.'|'.$LugarExpedicion.'|'.$Erfc.'|'.$Enombre_emisor.'|'.$regimenfiscal.'|'.$Rrfc.'|'.$Rnombre.'|'.$usocfdi.'|'.$conceptos.'|002|Tasa|0.160000|'.@number_format($iva,2,".","").'|'.@number_format($iva,2,".","").'||';
				
$pkeyid = openssl_get_privatekey(file_get_contents($llave));
openssl_sign($cadenaoriginal, $crypttext, $pkeyid, OPENSSL_ALGO_SHA256);
openssl_free_key($pkeyid);
$sellodigital = base64_encode($crypttext);
							
$salida_xml.='<?xml version="1.0" encoding="UTF-8"?><cfdi:Comprobante xsi:schemaLocation="http://www.sat.gob.mx/cfd/3 http://www.sat.gob.mx/sitio_internet/cfd/3/cfdv33.xsd" Version="3.3" Serie="'.$serie.'" Folio="'.$id_factura.'" Fecha="'.$T_fecha_hora.'" Sello="'.$sellodigital.'" FormaPago="'.$formapago.'" NoCertificado="'.$TnoCertificado.'" Certificado="'.$Tcertificado.'"  SubTotal="'.@number_format($subtotal,2,".","").'" Moneda="'.$moneda.'" Total="'.@number_format($total,2,".","").'" TipoDeComprobante="'.$tipocomprobante.'" MetodoPago="'.$metodopago.'" LugarExpedicion="'.$LugarExpedicion.'" xmlns:cfdi="http://www.sat.gob.mx/cfd/3" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">';
$salida_xml.='<cfdi:Emisor Rfc="'.$Erfc.'" Nombre="'.$Enombre_emisor.'" RegimenFiscal="'.$regimenfiscal.'" />';
$salida_xml.='<cfdi:Receptor Rfc="'.$Rrfc.'" Nombre="'.$Rnombre.'" UsoCFDI="'.$usocfdi.'" />';
$salida_xml.='<cfdi:Conceptos>';
$salida_xml.="<cfdi:Concepto ClaveProdServ=\"".$claveprodserv."\" Cantidad=\"".@number_format($cantidad,5,".","")."\" ClaveUnidad=\"".$claveunidad."\" Unidad=\"".$unidad_medida."\" Descripcion=\"".$descripcion."\" ValorUnitario=\"".@number_format($unitario,5,".","")."\" Importe=\"".@number_format($subtotal,2,".","")."\">";
$salida_xml.="<cfdi:Impuestos>";
$salida_xml.="<cfdi:Traslados>";
$salida_xml.="<cfdi:Traslado Base=\"".@number_format($base,2,".","")."\" Impuesto=\"002\" TipoFactor=\"Tasa\" TasaOCuota=\"0.160000\" Importe=\"".@number_format($iva,2,".","")."\" />";
$salida_xml.="</cfdi:Traslados>";
$salida_xml.="</cfdi:Impuestos>";
$salida_xml.="</cfdi:Concepto>";	
$salida_xml.='</cfdi:Conceptos>';
$salida_xml.='<cfdi:Impuestos TotalImpuestosTrasladados="'.@number_format($iva,2,".","").'">';
$salida_xml.='<cfdi:Traslados>';
$salida_xml.='<cfdi:Traslado Impuesto="002" TipoFactor="Tasa" TasaOCuota="0.160000" Importe="'.@number_format($iva,2,".","").'" />';
$salida_xml.='</cfdi:Traslados>';
$salida_xml.='</cfdi:Impuestos>';
$salida_xml.='</cfdi:Comprobante>';
				
//file_put_contents("wfacturacion/".$numfactura."3.3.xml", $salida_xml);	
				
    $params = array(
	      "url"=>"http://services.test.sw.com.mx",	      "token"=>"T2lYQ0t4L0RHVkR4dHZ5Nkk1VHNEakZ3Y0J4Nk9GODZuRyt4cE1wVm5tbXB3YVZxTHdOdHAwVXY2NTdJb1hkREtXTzE3dk9pMmdMdkFDR2xFWFVPUXpTUm9mTG1ySXdZbFNja3FRa0RlYURqbzdzdlI2UUx1WGJiKzViUWY2dnZGbFloUDJ6RjhFTGF4M1BySnJ4cHF0YjUvbmRyWWpjTkVLN3ppd3RxL0dJPQ.T2lYQ0t4L0RHVkR4dHZ5Nkk1VHNEakZ3Y0J4Nk9GODZuRyt4cE1wVm5tbFlVcU92YUJTZWlHU3pER1kySnlXRTF4alNUS0ZWcUlVS0NhelhqaXdnWTRncklVSWVvZlFZMWNyUjVxYUFxMWFxcStUL1IzdGpHRTJqdS9Zakw2UGRiMTFPRlV3a2kyOWI5WUZHWk85ODJtU0M2UlJEUkFTVXhYTDNKZVdhOXIySE1tUVlFdm1jN3kvRStBQlpLRi9NeWJrd0R3clhpYWJrVUMwV0Mwd3FhUXdpUFF5NW5PN3J5cklMb0FETHlxVFRtRW16UW5ZVjAwUjdCa2g0Yk1iTExCeXJkVDRhMGMxOUZ1YWlIUWRRVC8yalFTNUczZXdvWlF0cSt2UW0waFZKY2gyaW5jeElydXN3clNPUDNvU1J2dm9weHBTSlZYNU9aaGsvalpQMUxrUndzK0dHS2dpTittY1JmR3o2M3NqNkh4MW9KVXMvUHhZYzVLQS9UK2E1SVhEZFJKYWx4ZmlEWDFuSXlqc2ZRYXlUQk1ldlZkU2tEdU10NFVMdHZKUURLblBxakw0SDl5bUxabDFLNmNPbEp6b3Jtd2Q1V2htRHlTdDZ6eTFRdUNnYnVvK2tuVUdhMmwrVWRCZi9rQkU9.7k2gVCGSZKLzJK5Ky3Nr5tKxvGSJhL13Q8W-YhT0uIo"
	  );
     
    try
    {
        header('Content-type: application/json');

		$stamp = StampService::Set($params);
        $result = $stamp::StampV4($salida_xml);
	
	    if($result->status=='error'){
        //echo $result->message;
	    //echo $result->messageDetail;
		$jsondata['facturado'] =0;
		$jsondata['mensaje']   =$result->message;
        }else{
			
		$cadena_original=$result->data->cadenaOriginalSAT;
		$sello_cfd=$result->data->selloCFDI;
		$fecha_timbrado=$result->data->fechaTimbrado;
		$uuid=$result->data->uuid;
		$nocertificadosat=$result->data->noCertificadoSAT;
		$sello_sat=$result->data->selloSAT;	
			
		$sub=@number_format($subtotal,2,".","");
		$iv=@number_format($iva,2,".","");
		$tot=@number_format($total,2,".","");
		$cuota=@number_format($cuotaieps,2,".","");
			
	    $Fol=mysqli_query($link,"select folio from folios where id_estacion='$id_estacion' order by folio DESC limit 1");
		$dFol=mysqli_fetch_assoc($Fol);
			
		if(empty($dFol['folio'])){
		$nfol=1;
		}else{
		$nfol=$dFol['folio']+1;	
		}	
		$Udfol=mysqli_query($link,"INSERT INTO folios values ('','$id_factura','$nfol','$id_estacion','$fecha_alta')");		
			
		$sFactura=mysqli_query($link,"update facturas3_3 set numfactura='$numfactura',folio='$nfol',id_cliente='$id_cliente',rfc='$Rrfc',razon_social='$Rnombre',id_domicilio='$id_domicilio',lugarexpedicion='$LugarExpedicion',serie='$serie',cveest='$cveest',formapago='$formapago',usocfdi='$usocfdi',id_empresa='$id_empresa',subtotal='$sub',iva='$iv',total='$tot',cuotaieps='$cuota',estatus=1,fecha_modificacion='$fecha_alta',cadena_original='$cadena_original',sello_cfd='$sello_cfd',fecha_timbrado='$fecha_timbrado',uuid='$uuid',nocertificadosat='$nocertificadosat',version='1.1',sello_sat='$sello_sat',tipo='7',pac='smartweb',comentarios='$comentarios',id_despachador='$id_despachador' where id='$id_factura'");
		
		$sTi=("insert into tickets3_3 (numfactura,id_factura,ticket,fecha,id_producto,bomba,pre,unitario,cantidad,monto,subtotal,siniva,base,cuotaieps,cveest,facturado,fecha_alta) values ('$numfactura','$id_factura','$ticket','$fecha_ticket','$id_producto','$bomba','$preunitario','$unitario','$cantidad','$total','$subtotal','$iva','$base','$cuotaieps','$cveest','1','$fecha_alta')");
		$insTick=@mysqli_query($link,$sTi);	
			
		$nuevodirectorio = "wfacturacion/$serie/$fecha";
			
		if (!file_exists($nuevodirectorio))
        {
         mkdir ($nuevodirectorio, 0777, true);	
        }

        file_put_contents($nuevodirectorio."/".$numfactura.".xml", $result->data->cfdi);
			
	/*		
        ob_start();
	include(dirname(__FILE__).'/res/genera_factura_cel.php');
        $content = ob_get_clean();
        require_once(dirname(__FILE__).'/html2pdf.class.php');
        try
        {
        
	     $html2pdf = new HTML2PDF('P', 'A4', 'es', true, 'UTF-8', 3);
         $html2pdf->writeHTML($content);
	     $html2pdf->Output($nuevodirectorio."/".$numfactura.".pdf", 'F');

        } 
        catch(HTML2PDF_exception $e) {
        $jsondata['mensaje']   =$e;		

        }
 
	require("phpmailer/class.phpmailer.php");	
    $copia=$_POST['copia'];
    $correo=$dClie['correo'];		
			
    $mail = new PHPMailer();
    $pdf=$nuevodirectorio."/".$numfactura.".pdf";	
	$xml=$nuevodirectorio."/".$numfactura.".xml";
	$mail->From     = $cuentacorreo;
    $mail->FromName = "Facturacion Web combuexpress";		
	$mail->AddAddress($correo);
	if($copia){
	$mail->AddCC($copia);	
	}
    $mail->WordWrap = 50; 
    $mail->IsHTML(true);     
    $mail->Subject  =  "FACTURA ".$numfactura;
    $mail->Body     =  '<html>

<table width="572" border="0" cellpadding="2" cellspacing="2">
  <tr>
    <td colspan="4" align="center"><img src="http://combuexpress.com.mx/images/logo.jpg" ></td>
  </tr>
  <tr>
    <td colspan="4">&nbsp;</td>
  </tr>
  <tr>
    <td colspan="4"><strong>Gracias por su preferencia</strong></td>

  </tr>
  <tr>
    <td colspan="4">&nbsp;</td>
  </tr>
  <tr>
    <td width="128" bgcolor="#CCCCCC"><strong># Factura:</strong></td>
    <td width="418" colspan="3" bgcolor="#EAEAEA"><span class="style2">'.$numfactura.'</span></td>
  </tr>
  <tr>
    <td height="7" colspan="4"></td>
  </tr>
  <tr>
    <td colspan="4" height="7"></td>
  </tr>
</table>
</html>'; 			
*/
    $mail->AddAttachment($pdf);
    $mail->AddAttachment($xml);
    $mail->SMTPDebug = 0;                         
    $mail->IsSMTP();                                 
    $mail->SMTPAuth      = false; 
    $mail->SMTPKeepAlive = true;
    $mail->Host = "smtp-relay.gmail.com"; 
	$mail->Send();
			
	$jsondata['facturado']   = 1;
	$jsondata['erfc']=$Erfc;
    $jsondata['enombre']=$Enombre_emisor;			
	$jsondata['ecalle']=$Dcalle;
	$jsondata['enumexterior']=$DnoExterior;
	$jsondata['ecolonia']=$Dcolonia;
	$jsondata['emunicipio']=$Dmunicipio;
	$jsondata['eestado']=$Destado;
	$jsondata['epais']=$Dpais;
	$jsondata['ecp']=$DcodigoPostal;			
   	$jsondata['factura']=$numfactura;
	$jsondata['subtotal']=$subtotal;
	$jsondata['iva']=$iva;
	$jsondata['total']=$total;
	$jsondata['claveprodserv']=$claveprodserv; 
	$jsondata['claveunidad']=$claveunidad;
	$jsondata['unidad_medida']=$unidad_medida;
	$jsondata['descripcion']=$descripcion;
	$jsondata['selloCFD']=$sello_cfd;
	$jsondata['FechaTimbrado']=$fecha_timbrado;
	$jsondata['UUID']=$uuid;
	$jsondata['noCertificadoSAT']=$nocertificadosat;
	$jsondata['version']='1.1';
	$jsondata['selloSAT']=$sello_sat;	
					
        }
	}
 catch(Exception $e)
    {
		$jsondata['facturado'] =0;
		$jsondata['mensaje']   =$e->getMessage();
    }

				
}

}else{
	
$jsondata['mensaje']="No se pudo procesar el CFDI por falta de informacion";
	
}				
				
echo json_encode($jsondata);
mysqli_close($link);
?>