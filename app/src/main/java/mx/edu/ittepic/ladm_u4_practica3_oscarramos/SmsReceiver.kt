package mx.edu.ittepic.ladm_u4_practica3_oscarramos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SmsMessage
import android.widget.Toast


class SmsReceiver : BroadcastReceiver(){

    override fun onReceive(context: Context, intent: Intent) {


        val extras = intent.extras
        if(extras != null){
            var sms = extras.get("pdus") as Array<Any>
            for(indice in sms.indices){
                var formato = extras.getString("format")

                var smsMensaje = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    SmsMessage.createFromPdu(sms[indice] as ByteArray,formato)
                }else{
                    SmsMessage.createFromPdu(sms[indice] as ByteArray)
                }

                var origen = smsMensaje.originatingAddress
                var contenidoSMS = smsMensaje.messageBody.toString()
                var cadena = contenidoSMS.split(" ")
                var envio = ""
                Toast.makeText(context,"Recibiste mensaje de: "+origen,Toast.LENGTH_LONG)
                    .show()
                if(cadena.size !=3){

                }else{
                    if(!(cadena[0].equals("CALIFICACION"))){
                        SmsManager.getDefault().sendTextMessage(
                            origen,null,
                            "SINTAXIS INCORRECTA, SIGA EL SIGUIENTE FORMATO [CALIFICACION 16400937 U1]",null,null)
                    }else{
                        if(cadena[1].toString().length != 8){
                            SmsManager.getDefault().sendTextMessage(
                                origen,null,
                                "SINTAXIS INCORRECTA EL NUMERO DE CONTROL DEBE DE TENER SOLO 8 DIGITOS",null,null)

                        }else{
                            if(cadena[2].toString().length != 2){
                                SmsManager.getDefault().sendTextMessage(
                                    origen,null,
                                    "SINTAXIS INCORRECTA EL FORMATO DE LA UNIDAD DEBE SER EL SIGUIENTE [U1]",null,null)

                            }else{
                                //
                                try {

                                    val cursor = BaseDatos(context,"CALIFICACION",null,1)
                                        .readableDatabase
                                        .rawQuery("SELECT * FROM CALIFICACION WHERE NUMEROCONTROL = '${cadena[1]}' AND UNIDAD = '${cadena[2]}'",null)
                                    if(cursor.moveToNext()){

                                        envio = "Buen dia "+cursor.getString(0)+" tu calificacion en la "+cursor.getString(2)+
                                                " es: "+cursor.getString(3)

                                        SmsManager.getDefault().sendTextMessage(
                                            origen,null,
                                            ""+envio,null,null)

                                    }else{
                                        SmsManager.getDefault().sendTextMessage(
                                            origen,null,
                                            "Lo siento, aun no se registra tu calificacion",null,null)

                                    }
                                }catch (e: SQLiteException){
                                    SmsManager.getDefault().sendTextMessage(
                                        origen,null,
                                        e.message,null,null)

                                }

                                //


                            }
                        }
                    }
                }
            }
        }
    }

}