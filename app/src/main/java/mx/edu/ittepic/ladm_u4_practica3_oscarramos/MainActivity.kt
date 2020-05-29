package mx.edu.ittepic.ladm_u4_practica3_oscarramos

import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var dataLista = ArrayList<String>()
    var listaNoControl = ArrayList<String>()
    val siPermiso = 1
    val siPermisoReceiver = 2
    val siPermisoLectura = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle("Servicio de calificaciones SMS")

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_SMS),siPermisoLectura)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.RECEIVE_SMS),siPermisoReceiver)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS) !=
            PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.SEND_SMS), siPermiso)
        }


        leerCal()

        Agregar.setOnClickListener {
            var nombre = ""
            var noControl = ""
            var calificacion = ""
            var unidad = ""


            if(Nombre.text.toString().isEmpty()||
                NumeroControl.text.toString().isEmpty()||
                Calificacion.text.toString().isEmpty()||
                Unidad.text.toString().isEmpty()){
                mensaje("CAMPOS VACIOS")
                return@setOnClickListener
            }

            if(Unidad.text.toString().length != 2){
                mensaje("SINTAXIS INCORRECTA"+"\nEJEMPLO: U1")
                return@setOnClickListener
            }

            if(Calificacion.text.toString().toInt()<0 || Calificacion.text.toString().toInt()>100){
                mensaje("SINTAXIS INCORRECTA CALIFICACION DE 0 A 100")
                return@setOnClickListener
            }


            if(NumeroControl.text.toString().length != 8){
                mensaje("SINTAXIS INCORRECTA SOLO 8 DIGITOS")
                return@setOnClickListener
            }

            nombre = Nombre.text.toString()
            noControl = NumeroControl.text.toString()
            calificacion = Calificacion.text.toString()
            unidad = Unidad.text.toString()

            try {
                var baseDatos = BaseDatos(this,"CALIFICACION",null,1)
                var insertar = baseDatos.writableDatabase
                var SQL = "INSERT INTO CALIFICACION VALUES('${nombre}','${noControl}','${unidad}','${calificacion}')"
                insertar.execSQL(SQL)
                baseDatos.close()
            }catch (e: SQLiteException){
                mensaje(e.message!!)
            }

            leerCal()
        }

        lista.setOnItemClickListener { parent, view, position, id ->
            if(listaNoControl.size==0){
                return@setOnItemClickListener
            }
            AlertaEliminar(position)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == siPermiso){

        }
        if(requestCode == siPermisoReceiver){

        }
        if(requestCode == siPermisoLectura){

        }
    }

    private fun AlertaEliminar(position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Esta seguro de eliminar?")
            .setPositiveButton("Eliminar"){d,i->
                EliminarCal(position)
            }
            .setNeutralButton("Cancelar"){
                    d,i->

            }
            .show()
    }

    private fun EliminarCal(position: Int) {
        try {
            var base = BaseDatos(this,"CALIFICACION",null,1)
            var eliminar = base.writableDatabase
            var noControlEliminar = arrayOf(listaNoControl[position])
            var respuesta =  eliminar.delete("CALIFICACION","NUMEROCONTROL=?",noControlEliminar)
            if(respuesta.toInt() == 0){
                mensaje("NO SE ELIMINO REGISTRO")
            }
        }catch (e:SQLiteException){
            mensaje(e.message!!)
        }
        leerCal()
    }

    private fun leerCal() {
        dataLista.clear()
        listaNoControl.clear()
        try{
            val cursor = BaseDatos(this,"CALIFICACION",null,1)
                .readableDatabase
                .rawQuery("SELECT * FROM CALIFICACION",null)
            var temporal = ""

            if(cursor.moveToFirst()){
                do{
                    temporal ="Nombre: "+cursor.getString(0)+"\n"+
                            "No.Control: "+cursor.getString(1)+"\n"+
                            "Unidad: "+cursor.getString(2)+"\n"+
                            "Calificacion: "+cursor.getString(3)

                    cursor.getString(1)
                    dataLista.add(temporal)
                    listaNoControl.add(cursor.getString(1))
                }while(cursor.moveToNext())
            }else{
                dataLista.add("NO HAY DATOS")
            }
            var adaptador = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataLista)
            lista.adapter = adaptador
        }catch (err: SQLiteException){
            Toast.makeText(this,err.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun mensaje(s: String) {
        Toast.makeText(this,s,Toast.LENGTH_LONG)
            .show()
    }




}

