package com.apps.unisabanetaapp;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Fragmento simple que permite mostrar las notas del estudiante.
 */
public class FragmentoNotas extends Fragment {


    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    Spinner spinnerProgramas;
    Spinner spinnerPeriodo;
    private Programa[] listaProgramas;
    private String[] listaPeriodos;
    int programaId;
    String correo;
    String periodo = "";
    boolean bandera = false;
    boolean bandera2 = false;
    List<Notas> items;
    ActividadPrincipal actividadPrincipal;

    View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragmento_notas, container, false);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

            items = new ArrayList<>();

            actividadPrincipal = (ActividadPrincipal)getActivity();
            this.correo = actividadPrincipal.correo;
            //Log.i("Correo ", correo);

            spinnerProgramas = (Spinner) view.findViewById(R.id.spinnerPrograma2);
            spinnerPeriodo = (Spinner) view.findViewById(R.id.spinnerPeriodo);

            final ConsultaProgramas programas = new ConsultaProgramas();
            programas.execute();


            spinnerProgramas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (verificaConexion(getActivity())){
                        items.clear();
                        //if (bandera) {
                            programaId = listaProgramas[i].id;
                        //}
                        //else
                            bandera = true;
                        ConsultaPeriodos periodos = new ConsultaPeriodos();
                        periodos.execute();
                    }
                    else
                        Snackbar.make(view, "Verifica tu conexión a internet.", Snackbar.LENGTH_LONG).show();


                }
                public void onNothingSelected(AdapterView<?> adapterView) {
                    return;
                }
            });

            spinnerPeriodo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (verificaConexion(getActivity())){
                        items.clear();

                            periodo = listaPeriodos[i];
                            bandera2 = true;

                        ConsultaNotas tarea = new ConsultaNotas();
                        tarea.execute();

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
     * Objeto para las notas del estudiante
     */

    public class Notas{

        ArrayList<Double> nota;
        ArrayList<Double> porcentaje;
        ArrayList<String> tipoEvaluacion;
        String asignatura;
        int creditos;

        public Notas(){

            this.nota = new ArrayList<Double>();
            this.porcentaje = new ArrayList<Double>();
            this.tipoEvaluacion = new ArrayList<String>();
            this.asignatura = "";
            this.creditos = 0;
        }
        public Notas( double nota, double porcentaje, String tipoEvaluacion, String asignatura, int creditos){

            this.nota = new ArrayList<Double>();
            this.porcentaje = new ArrayList<Double>();
            this.tipoEvaluacion = new ArrayList<String>();
            this.nota.add(nota);
            this.porcentaje.add(porcentaje);
            this.tipoEvaluacion.add(tipoEvaluacion);
            this.asignatura = asignatura;
            this.creditos = creditos;
        }

        public String getAsignatura() {
            return asignatura;
        }

        public int getCreditos() { return creditos; }

        public double getNota() { return nota.get(0); }

        public double getPorcentaje() { return porcentaje.get(0); }

        public String getTipoEvaluacion() { return tipoEvaluacion.get(0); }
    }

    /**
     * Clase que se encarga de consumir el servicio Web publicado, mediante el protocolo SOAP.
     */
    private class ConsultaNotas extends AsyncTask<String,Integer,Boolean> {

        private Notas[] listaNotas;
        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
           /* progressDialog = ProgressDialog.show(getActivity(),
                    "ProgressDialog",
                    "Cargando");*/
        }

        protected Boolean doInBackground(String... params) {
            publishProgress(0);
            boolean resul = true;

            String NAMESPACE = getResources().getString(R.string.NAMESPACE);
            String URL=getResources().getString(R.string.URL);
            String METHOD_NAME = "ConsultarNotas";
            String SOAP_ACTION = "http://tempuri.org/ConsultarNotas";

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

            HttpTransportSE transporte = new HttpTransportSE(URL);

            try
            {

                transporte.call(SOAP_ACTION, envelope);
                SoapObject resSoap =(SoapObject)envelope.getResponse();
                listaNotas = new Notas[resSoap.getPropertyCount()];

                for (int i = 0; i < listaNotas.length; i++)
                {
                    SoapObject ic = (SoapObject)resSoap.getProperty(i);

                    Notas notas = new Notas();
                    notas.nota.add(Double.parseDouble(ic.getProperty(0).toString()));
                    notas.porcentaje.add(Double.parseDouble(ic.getProperty(1).toString()));
                    notas.tipoEvaluacion.add(ic.getProperty(2).toString());
                    notas.asignatura = ic.getProperty(3).toString();
                    notas.creditos = Integer.parseInt(ic.getProperty(4).toString());

            listaNotas[i] = notas;
        }
    }
            catch (Exception e)
    {
        resul = false;
        Log.e("CONEXION", e.getMessage());
    }

            return resul;
        }

        protected void onPostExecute(Boolean result) {
            //progressDialog.dismiss();
            if (result) {

                ArrayList<String> asignaturas = new ArrayList<String>();
                items.clear();

                for (int i=0; i<listaNotas.length; i++){

                    if(i==0){
                        items.add(listaNotas[i]);
                        asignaturas.add(listaNotas[i].asignatura);
                    }
                    else {
                            if (!asignaturas.contains(listaNotas[i].asignatura)) {
                                items.add(listaNotas[i]);
                                asignaturas.add(listaNotas[i].asignatura);

                            }
                            else{
                                int posicion = asignaturas.indexOf(listaNotas[i].asignatura);
                                items.get(posicion).nota.add(listaNotas[i].getNota());
                                items.get(posicion).porcentaje.add(listaNotas[i].getPorcentaje());
                                items.get(posicion).tipoEvaluacion.add(listaNotas[i].getTipoEvaluacion());
                            }
                    }

                }

                // Obtener el Recycler
                recycler = (RecyclerView) view.findViewById(R.id.recicladorNotas);
                recycler.setHasFixedSize(true);

                // Usar un administrador para LinearLayout
                lManager = new LinearLayoutManager(getActivity());
                recycler.setLayoutManager(lManager);

                // Crear un nuevo adaptador
                adapter = new AdaptadorNotas(items);
                recycler.setAdapter(adapter);

                if(listaNotas.length==0) {

                    FragmentoSAG fragmentoSAG = (FragmentoSAG) getParentFragment();
                    TabLayout tabLayout = (TabLayout) fragmentoSAG.tabs;
                    if(tabLayout.getSelectedTabPosition()==1)
                        Snackbar.make(view, "Aún no se han cargado notas.\n Cargando Periodo anterior...", Snackbar.LENGTH_LONG).show();

                    try {

                        spinnerPeriodo.setSelection(1);

                    } catch (Exception e) {

                    }
                    //Toast.makeText(getContext(),"No se encontraron Registros", Toast.LENGTH_LONG).show();
                }

            }
            else
            {
                items.clear();
                Toast.makeText(getContext(),"No se encontraron registros ", Toast.LENGTH_LONG).show();
                //Log.e("FRAGMENTO NOTAS","No se encontraron registros");
            }
        }
    }

    public class AdaptadorNotas extends RecyclerView.Adapter<NotasViewHolder> {
        private List<Notas> items;


        public AdaptadorNotas(List<Notas> items) {
            this.items = items;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public NotasViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.tarjeta_notas, viewGroup, false);
            return new NotasViewHolder(v);
        }

        @Override
        public void onBindViewHolder(NotasViewHolder viewHolder, int i) {

            double sumaPorcentaje=0, notaFinal=0;
            DecimalFormat df = new DecimalFormat("#.00");
            DecimalFormat df2 = new DecimalFormat("#.0");

            if(viewHolder.tablaNotas.getChildCount()==2) {

                viewHolder.creditos.setText("Créditos: " + Integer.toString(items.get(i).getCreditos()));
                viewHolder.asignatura.setText(minusculas(items.get(i).getAsignatura()));

                int filas = items.get(i).nota.size();
                for (int j = 0; j < filas; j++) {

                    TableRow fila = new TableRow(getContext());
                    if (j % 2 == 0)
                        fila.setBackgroundColor(getResources().getColor(R.color.gray_light));

                    TextView tipoEvaluacion = new TextView(getContext());
                    TextView porcentaje = new TextView(getContext());
                    TextView nota = new TextView(getContext());

                    tipoEvaluacion.setPadding(3, 3, 3, 3);
                    //tipoEvaluacion.setMaxWidth(140);
                    tipoEvaluacion.setMaxLines(1);
                    tipoEvaluacion.setEllipsize(TextUtils.TruncateAt.END);
                    nota.setPadding(3, 3, 3, 3);
                    porcentaje.setPadding(3, 3, 3, 3);

                    LinearLayout.LayoutParams lay;
                    lay = (LinearLayout.LayoutParams) viewHolder.textViewGuia.getLayoutParams();
                    tipoEvaluacion.setLayoutParams(lay);
                    lay = (LinearLayout.LayoutParams) viewHolder.textPorGuia.getLayoutParams();
                    porcentaje.setLayoutParams(lay);
                    lay = (LinearLayout.LayoutParams) viewHolder.textGuiaNota.getLayoutParams();
                    nota.setLayoutParams(lay);

                    String seg = minusculas(items.get(i).tipoEvaluacion.get(j));

                    if(seg.equals("Anytype{}"))
                        seg = "Seguimiento " + (j+1);

                    tipoEvaluacion.setText(seg);
                    porcentaje.setText(df2.format(items.get(i).porcentaje.get(j)));
                    nota.setText(df.format(items.get(i).nota.get(j)));

                    fila.addView(tipoEvaluacion);
                    fila.addView(porcentaje);
                    fila.addView(nota);

                    viewHolder.tablaNotas.addView(fila, j + 1);

                    sumaPorcentaje = sumaPorcentaje + items.get(i).porcentaje.get(j);
                    notaFinal = notaFinal + ((items.get(i).nota.get(j) / 5) * (items.get(i).porcentaje.get(j)) / 20);
                }

                viewHolder.sumaPorcentajes.setText(df2.format(sumaPorcentaje));
                viewHolder.notaFinal.setText(df.format(notaFinal));

                if (sumaPorcentaje == 100 || sumaPorcentaje == 1) {
                    viewHolder.textoNota.setText("Nota: " + df.format(notaFinal));
                    if (notaFinal >= 3)
                        viewHolder.textoEstado.setText("Estado: Ganada");
                    else
                        viewHolder.textoEstado.setText("Estado: Perdida");
                } else {
                    viewHolder.textoNota.setText("Nota: " + df.format(notaFinal));
                    viewHolder.textoEstado.setText("Estado: En Curso");
                }
            }
        }
    }

    public  class NotasViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item

        public TextView asignatura;
        public TextView creditos;
        public TableLayout tablaNotas;
        public TextView textViewGuia;
        public TextView textGuiaNota;
        public TextView textPorGuia;
        public TextView sumaPorcentajes;
        public TextView notaFinal;
        public TextView textoNota;
        public TextView textoEstado;


        ExpandableRelativeLayout el1;


        public NotasViewHolder(View v) {

            super(v);
            asignatura = (TextView) v.findViewById(R.id.asignatura2);
            creditos = (TextView) v.findViewById(R.id.textoCreditos2);
            tablaNotas = (TableLayout) v.findViewById(R.id.tablaNotas);
            textViewGuia = (TextView) v.findViewById(R.id.textViewGuia);
            textPorGuia = (TextView) v.findViewById(R.id.textPorGuia);
            textGuiaNota = (TextView) v.findViewById(R.id.textoNotaGuia);
            sumaPorcentajes = (TextView) v.findViewById(R.id.sumaPorcentajes);
            notaFinal = (TextView) v.findViewById(R.id.notaFinal);
            textoNota = (TextView) v.findViewById(R.id.textoNota);
            textoEstado = (TextView) v.findViewById(R.id.textoEstado);
        }
    }


    private class ConsultaProgramas extends AsyncTask<String,Integer,Boolean> {

        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            /*progressDialog = ProgressDialog.show(getActivity(),
                    "ProgressDialog",
                    "Cargando");*/
        }

        protected Boolean doInBackground(String... params) {
            publishProgress(0);
            boolean resul = true;

            String NAMESPACE = getResources().getString(R.string.NAMESPACE);
            String URL=getResources().getString(R.string.URL);
            String METHOD_NAME = "ConsultarProgramas";
            String SOAP_ACTION = "http://tempuri.org/ConsultarProgramas";

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
                Log.e("CargangoProgramas", e.getMessage());
            }

            return resul;
        }

        protected void onPostExecute(Boolean result) {
            //progressDialog.dismiss();
            if (result)
            {
                String[] datosSpinner = new String[listaProgramas.length];
                for (int i = 0; i<listaProgramas.length; i++){

                    datosSpinner[i] = minusculas(listaProgramas[i].nombre);
                }

                if(listaProgramas.length>0){

                //programaId = listaProgramas[0].id;
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, datosSpinner);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProgramas.setAdapter(spinnerArrayAdapter);}
            }
            else
            {
                Log.e("PROGRAMAS","Error cargando Programas FR2 ");
            }
        }
    }

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

    private class ConsultaPeriodos extends AsyncTask<String,Integer,Boolean> {

        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            /*progressDialog = ProgressDialog.show(getActivity(),
                    "ProgressDialog",
                    "Cargando");*/
        }

        protected Boolean doInBackground(String... params) {
            publishProgress(0);
            boolean resul = true;

            String NAMESPACE = getResources().getString(R.string.NAMESPACE);
            String URL=getResources().getString(R.string.URL);
            String METHOD_NAME = "ConsultarPeriodo";
            String SOAP_ACTION = "http://tempuri.org/ConsultarPeriodo";

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

            HttpTransportSE transporte = new HttpTransportSE(URL);

            try
            {   transporte.call(SOAP_ACTION, envelope);
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
            //progressDialog.dismiss();
            if (result)
            {
                String[] datosSpinner = new String[listaPeriodos.length];
                for (int i = 0; i<listaPeriodos.length; i++){

                    datosSpinner[i] = listaPeriodos[i].substring(0,4) + " - " + listaPeriodos[i].substring(4,6) ;
                }

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, datosSpinner);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPeriodo.setAdapter(spinnerArrayAdapter);

            }
            else
            {
                Log.e("PERIODOS","Error cargando Periodos ");
            }
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
}
