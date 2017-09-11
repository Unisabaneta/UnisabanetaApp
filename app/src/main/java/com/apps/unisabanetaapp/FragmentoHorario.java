package com.apps.unisabanetaapp;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * Fragmento que permite visualizar el horario del estudiante logueado previamente en la aplicación
 */
public class FragmentoHorario extends Fragment {

    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    View view;
    private List<Horario> items;
    Spinner spinnerProgramas;
    private Programa[] listaProgramas;
    private String[] listaPeriodos;
    int programaId;
    String correo;
    String periodo;
    String usuario;
    ActividadPrincipal actividadPrincipal;

    private ProgressDialog mProgressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragmento_horario, container, false);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        items = new ArrayList<>();

        actividadPrincipal = (ActividadPrincipal)getActivity();
        this.correo = actividadPrincipal.correo;
        this.usuario= actividadPrincipal.usuario.getText().toString();


        spinnerProgramas = (Spinner) view.findViewById(R.id.spinnerPrograma);
        ConsultaProgramas programas = new ConsultaProgramas();
        programas.execute();

        spinnerProgramas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (verificaConexion(getActivity())){
                    items.clear();
                    programaId = listaProgramas[i].id;

                    ConsultaPeriodos periodos = new ConsultaPeriodos();
                    periodos.execute();
                    //periodo = listaPeriodos[listaPeriodos.length-1];


                }
                else
                    Snackbar.make(view, "Verifica tu conexión a internet.", Snackbar.LENGTH_LONG).show();
            }
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
    }

    /**
     * Permite verificar si el usuario tiene conexión o no a internet
     * @param ctx
     * @return
     */

    public static boolean verificaConexion(Context ctx) {
        boolean bConectado = false;
        ConnectivityManager connec = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // No sólo wifi, también GPRS
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        // este bucle debería no ser tan ñapa
        for (int i = 0; i < 2; i++) {
            // ¿Tenemos conexión? ponemos a true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                bConectado = true;
            }
        }
        return bConectado;
    }

    /**
     * Clase que permite gestionar las tarjetas en un RecyclerView
     */

    public  class HorarioViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item

        public TextView asignatura;
        public TextView ciclo;
        public TextView hora;
        public TextView docente;
        public TextView fechaInicio;
        public TextView fechaFin;
        public TextView aula;
        public TextView creditos;
        public TextView grupo;
        public TextView semestre;
        public TextView viewMore;
        public TextView dias;
        public boolean expandido;

        ExpandableRelativeLayout el1;


        public HorarioViewHolder(View v) {

            super(v);
            expandido = true;
            asignatura = (TextView) v.findViewById(R.id.asignatura);
            creditos = (TextView) v.findViewById(R.id.creditos);
            fechaInicio = (TextView) v.findViewById(R.id.fechaInicio);
            fechaFin = (TextView) v.findViewById(R.id.fechaFin);
            viewMore = (TextView) v.findViewById(R.id.viewMore);
            ciclo = (TextView) v.findViewById(R.id.textoCiclo);
            hora = (TextView) v.findViewById(R.id.textoHora);
            docente = (TextView) v.findViewById(R.id.docente);
            aula = (TextView) v.findViewById(R.id.aula);
            semestre = (TextView) v.findViewById(R.id.semestre);
            dias = (TextView) v.findViewById(R.id.textoDias);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    el1 = (ExpandableRelativeLayout) v.findViewById(R.id.expandableLayout1);
                    el1.toggle(); // toggle expand and collapse

                    if(expandido) {
                        viewMore.setText("[ - ]");
                        expandido = false;
                    }
                    else {
                        viewMore.setText("[ + ]");
                        expandido = true;
                    }
                }
            });
        }
    }

    public class AdaptadorHorario extends RecyclerView.Adapter<HorarioViewHolder> {
        private List<Horario> items;


        public AdaptadorHorario(List<Horario> items) {
            this.items = items;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public HorarioViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.tarjeta_asignatura, viewGroup, false);
            return new HorarioViewHolder(v);
        }

        @Override
        public void onBindViewHolder(HorarioViewHolder viewHolder, int i) {
            viewHolder.creditos.setText(items.get(i).getCreditos());
            viewHolder.asignatura.setText(minusculas(items.get(i).getAsignatura()));
            viewHolder.fechaInicio.setText(items.get(i).getFechaInicio());
            viewHolder.fechaFin.setText(items.get(i).getFechaFin());

            if(minusculas(items.get(i).getDocente()).equals("Anytype{}"))
                viewHolder.docente.setText("Por definir");
            else
                viewHolder.docente.setText(minusculas(items.get(i).getDocente()));

            viewHolder.ciclo.setText("Ciclo "+items.get(i).getCiclo());
            viewHolder.aula.setText(minusculas(items.get(i).getAula()));
            viewHolder.semestre.setText(items.get(i).getSemestre());
            viewHolder.hora.setText(items.get(i).getHora());
            viewHolder.dias.setText(minusculas(items.get(i).getDias()));
        }

    }

    private class ConsultaPeriodos extends AsyncTask<String,Integer,Boolean> {

        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(getActivity(),
                    "",
                    "Cargando...");
        }

        protected Boolean doInBackground(String... params) {
            publishProgress(0);
            boolean resul = true;

            String NAMESPACE = getResources().getString(R.string.NAMESPACE);
            String URL=getResources().getString(R.string.URL);
            String METHOD_NAME = "ConsultarPeriodo";
            String SOAP_ACTION = "http://tempuri.org/ConsultarPeriodo";
            String USER = getResources().getString(R.string.User_SOAP);
            String PASS = getResources().getString(R.string.Pass_SOAP);

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            PropertyInfo PI = new PropertyInfo();
            PI.setName("correo");
            PI.setValue(correo);
            PI.setType(String.class);
            request.addProperty(PI);

            PI = new PropertyInfo();
            PI.setName("programaId");
            PI.setValue(programaId);
            PI.setType(Integer.class);
            request.addProperty(PI);


            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            envelope.headerOut = new Element[1];
            envelope.headerOut[0] = SoapAutenticationBuild.buildAuthHeader(NAMESPACE,USER,PASS);

            HttpTransportSE transporte = new HttpTransportSE(URL);

            try
            {   //transporte.call(SOAP_ACTION, envelope, headerPropertyList);
                transporte.call(SOAP_ACTION, envelope);
                SoapObject resSoap =(SoapObject)envelope.getResponse();
                listaPeriodos = new String[resSoap.getPropertyCount()];

                for (int i = 0; i < listaPeriodos.length; i++)
                {
                    SoapObject ic = (SoapObject) resSoap.getProperty(i);

                    listaPeriodos[i] = ic.getProperty(1).toString();

                }
            }
            catch (Exception e)
            {
                resul = false;
            }

            return resul;
        }

        protected void onPostExecute(Boolean result) {
            mProgressDialog.dismiss();
            if (result)
            {

                //Log.e("PeriodoFr1",listaPeriodos[listaPeriodos.length-1]);
                periodo = listaPeriodos[0];
                ConsultaHorario tarea = new ConsultaHorario();
                tarea.execute();


                /*String[] datosSpinner = new String[listaPeriodos.length];
                for (int i = 0; i<listaPeriodos.length; i++){

                    datosSpinner[i] = listaPeriodos[i].substring(0,4) + " - " + listaPeriodos[i].substring(4,6) ;
                }

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, datosSpinner);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPeriodo.setAdapter(spinnerArrayAdapter);*/

            }
            else
            {
                Log.e("PERIODOS","Error cargando Periodos FR1 ");
            }
        }
    }

    /**
     * Objeto Horario, todos sus componentes y constructor
     * Created by diezc on 1/05/2017.
     */

    public class Horario {

        private String asignatura;
        private String ciclo;
        private String hora;
        private String docente;
        private String fechaInicio;
        private String fechaFin;
        private String aula;
        private String creditos;
        private String grupo;
        private String semestre;
        private String dias;

        public Horario(){

            this.asignatura = "";
            this.ciclo = "";
            this.hora = "";
            this.docente = "";
            this.fechaInicio = "";
            this.fechaFin = "";
            this.aula = "";
            this.creditos = "";
            this.grupo = "";
            this.semestre = "";
            this.dias = "";
        }

        public Horario(String asignatura, String ciclo, String hora, String docente, String fechaInicio, String fechaFin,
                       String aula, String creditos, String grupo, String semestre, String dias) {

            this.asignatura = asignatura;
            this.ciclo = ciclo;
            this.hora = hora;
            this.docente = docente;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
            this.aula = aula;
            this.creditos = creditos;
            this.grupo = grupo;
            this.semestre = semestre;
            this.dias = dias;
        }

        public String getAsignatura() {
            return asignatura;
        }

        public String getCiclo() { return ciclo; }

        public String getHora() { return hora; }

        public String getDocente() { return docente; }

        public String getFechaInicio() { return fechaInicio; }

        public String getFechaFin() { return fechaFin; }

        public String getAula() { return aula; }

        public String getCreditos() { return creditos; }

        public String getGrupo() { return grupo; }

        public String getSemestre() { return semestre; }

        public String getDias(){ return dias;}
    }


    /**
     * Clase que se encarga de consumir el servicio Web publicado, mediante el protocolo SOAP.
     */
    private class ConsultaHorario extends AsyncTask<String,Integer,Boolean> {

        private Horario[] listaHorario;

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }


        protected Boolean doInBackground(String... params) {
            publishProgress(0);
            boolean resul = true;

            String NAMESPACE = getResources().getString(R.string.NAMESPACE);
            String URL=getResources().getString(R.string.URL);
            String METHOD_NAME = "ConsultaHorario";
            String SOAP_ACTION = "http://tempuri.org/ConsultaHorario";
            String USER = getResources().getString(R.string.User_SOAP);
            String PASS = getResources().getString(R.string.Pass_SOAP);

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

            PropertyInfo PI = new PropertyInfo();
            PI.setName("correo");
            PI.setValue(correo);
            PI.setType(String.class);
            request.addProperty(PI);

            PI = new PropertyInfo();
            PI.setName("programaId");
            PI.setValue(programaId);
            PI.setType(Integer.class);
            request.addProperty(PI);

            PI = new PropertyInfo();
            PI.setName("periodo");
            PI.setValue(periodo);
            PI.setType(String.class);
            request.addProperty(PI);

            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            envelope.headerOut = new Element[1];
            envelope.headerOut[0] = SoapAutenticationBuild.buildAuthHeader(NAMESPACE,USER,PASS);

            HttpTransportSE transporte = new HttpTransportSE(URL);
            int conteo=0;

            try {

                transporte.call(SOAP_ACTION, envelope);
                SoapObject resSoap = (SoapObject) envelope.getResponse();
                listaHorario = new Horario[resSoap.getPropertyCount()];
                conteo = resSoap.getPropertyCount();

                if (conteo > 0) {

                    for (int i = 0; i < listaHorario.length; i++) {
                        SoapObject ic = (SoapObject) resSoap.getProperty(i);

                        Horario horario = new Horario();
                        horario.asignatura = ic.getProperty(0).toString();
                        horario.ciclo = ic.getProperty(1).toString();
                        horario.hora = ic.getProperty(2).toString();
                        horario.docente = ic.getProperty(3).toString();
                        horario.fechaInicio = ic.getProperty(9).toString().substring(0, 10);
                        horario.fechaFin = ic.getProperty(10).toString().substring(0, 10);
                        horario.aula = ic.getProperty(4).toString();
                        horario.creditos = ic.getProperty(5).toString();
                        horario.semestre = ic.getProperty(8).toString();
                        horario.dias = ic.getProperty(6).toString();

                        listaHorario[i] = horario;
                    }
                  }
                }
            catch(Exception e)
                {
                    resul = false;
                    Log.e("Consulta_horario", e.getMessage() + " Resultado" + conteo);
                }


                return resul;

        }

        /**
         * Despúes de consumir el servicio se cargará el horario en la interfaz
         * @param result
         */

        protected void onPostExecute(Boolean result) {
            hideProgressDialog();
            if (result)
            {
                items.clear();

                for (int i=0; i<listaHorario.length; i++){
                        items.add(listaHorario[i]);

                    }

                // Obtener el Recycler
                recycler = (RecyclerView) view.findViewById(R.id.recicladorHorario);
                recycler.setHasFixedSize(true);

                // Usar un administrador para LinearLayout
                lManager = new LinearLayoutManager(getActivity());
                recycler.setLayoutManager(lManager);

                // Crear un nuevo adaptador
                adapter = new AdaptadorHorario(items);
                recycler.setAdapter(adapter);

                if(listaHorario.length==0)
                    alertaNotificacion();
                    //Toast.makeText(getContext(),"No se encontraron Registros", Toast.LENGTH_LONG).show();

            }
            else
            {
                items.clear();
                /*if(listaHorario.length==0)
                    alertaNotificacion();*/
                //Toast.makeText(getContext(),"No se encontraron Registros", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ConsultaProgramas extends AsyncTask<String,Integer,Boolean> {

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        protected Boolean doInBackground(String... params) {
            publishProgress(0);
            boolean resul = true;

            String NAMESPACE = getResources().getString(R.string.NAMESPACE);
            String URL=getResources().getString(R.string.URL);
            String METHOD_NAME = "ConsultarProgramas";
            String SOAP_ACTION = "http://tempuri.org/ConsultarProgramas";
            String USER = getResources().getString(R.string.User_SOAP);
            String PASS = getResources().getString(R.string.Pass_SOAP);

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);


            PropertyInfo PI = new PropertyInfo();
            PI.setName("correo");
            PI.setValue(correo);
            PI.setType(String.class);
            request.addProperty(PI);

            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);

            envelope.headerOut = new Element[1];
            envelope.headerOut[0] = SoapAutenticationBuild.buildAuthHeader(NAMESPACE,USER,PASS);

            HttpTransportSE transporte = new HttpTransportSE(URL);

            try
            {   transporte.call(SOAP_ACTION, envelope);
                SoapObject resSoap =(SoapObject)envelope.getResponse();
                listaProgramas = new Programa[resSoap.getPropertyCount()];

                for (int i = 0; i < listaProgramas.length; i++)
                {
                    SoapObject ic = (SoapObject)resSoap.getProperty(i);

                    Programa programa = new Programa();
                    programa.id = Integer.parseInt(ic.getProperty(0).toString()) ;
                    programa.nombre = ic.getProperty(1).toString();

                    listaProgramas[i] = programa;
                }
            }
            catch (Exception e)
            {
                resul = false;
            }

            return resul;
        }

        protected void onPostExecute(Boolean result) {
            hideProgressDialog();
            if (result)
            {
                if(listaProgramas.length==0)
                    alertaNotificacion();

                //Toast.makeText(getContext(),"Todo Bien", Toast.LENGTH_LONG).show();
                String[] datosSpinner = new String[listaProgramas.length];
                for (int i = 0; i<listaProgramas.length; i++){

                    datosSpinner[i] =minusculas(listaProgramas[i].nombre);
                }
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, datosSpinner);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProgramas.setAdapter(spinnerArrayAdapter);


            }
            else
            {
                Toast.makeText(getContext(),"Error cargando Programas ", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Objeto Programa
     */

    public class Programa{

        int id;
        String nombre;

        public Programa (){
            id=0;
            nombre = "";
        }

        public Programa(int id, String nombre){
            this.id  = id;
            this.nombre = nombre;
        }

    }

    /**
     * Permite mostrar fragmento de dialogo para indicar al usuario que la app se encuentra procesando una tarea
     */

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    /**
     * Permite cerrar fragmento de dialogo que indica al usuario que la app se encuentra procesando una tarea
     */
    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            //mProgressDialog.hide();
            mProgressDialog.dismiss();
        }
    }


    /**
     * Permite transcribir texto en Mayusculas a Minsuculas
     * @param string
     * @return
     */
    public String minusculas (String string){

        char[] chars = string.toLowerCase().toCharArray();
        boolean found = false;
        for (int i = 0; i < chars.length; i++) {
            if (!found && Character.isLetter(chars[i])) {
                chars[i] = Character.toUpperCase(chars[i]);
                found = true;
            } else if (Character.isWhitespace(chars[i]) || chars[i]=='.' || chars[i]=='\'') { // You can add other chars here
                found = false;
            }
        }
        return String.valueOf(chars);

    }

    public void alertaNotificacion()
    {

        final TextView tx1 = new TextView(getContext());

        tx1.setAutoLinkMask(RESULT_OK);
        tx1.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                            }
                        })
                .setView(tx1);

        final AlertDialog dialog = builder.create();

        String sourceString = "\n\nNo se encontraron Registros, contacta al CDT para acutalizar tu cuenta de correo universitaria en el SAG:\n\n  cdt@unisabaneta.edu.co";
        String keyWord = "cdt@unisabaneta.edu.co";
        SpannableString spannableString = new SpannableString(sourceString);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "cdt@unisabaneta.edu.co" });
                intent.putExtra(Intent.EXTRA_SUBJECT, "Solicitud actualización correo en SAG ");
                intent.putExtra(Intent.EXTRA_TEXT, "\nCordialmente solicito que se actualice el correo que tengo actualmente registrado en el " +
                        "SAG por el correo universitario:\n\n"+correo+"\n\n"+"Mi numero de cédula es: x.xxx.xxx.xxx"+"\n\n\nAtentamente: \n\n"+usuario);
                startActivity(Intent.createChooser(intent, "Enviar email con..."));
                dialog.dismiss();
            }
        };
        spannableString.setSpan(clickableSpan, sourceString.indexOf(keyWord), sourceString.indexOf(keyWord) + keyWord.length(), 0);

        tx1.setText(spannableString);
        tx1.setPadding(10,0,0,0);
        dialog.setIcon(R.drawable.logo_u);//to show the icon next to the title "About"
        dialog.setTitle("UNISABANETA MÓVIL");//set the title of the about box to say "About"

        dialog.show();



    }

}
