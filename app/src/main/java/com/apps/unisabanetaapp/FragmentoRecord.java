package com.apps.unisabanetaapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
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

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento en el que se visualizará el registro completo de las materias cursadas por el estudiante
 */
public class FragmentoRecord extends Fragment {

    View view;
    private List<Record> items;
    Spinner spinnerProgramas;
    private Programa[] listaProgramas;
    int programaId;
    String correo;
    boolean bandera = false;

    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       view =  inflater.inflate(R.layout.fragmento_record, container, false);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        items = new ArrayList<>();

        ActividadPrincipal actividadPrincipal = (ActividadPrincipal)getActivity();
        this.correo = actividadPrincipal.correo;

        spinnerProgramas = (Spinner) view.findViewById(R.id.spinnerPrograma3);

        final ConsultaProgramas programas = new ConsultaProgramas();
        programas.execute();

        spinnerProgramas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (verificaConexion(getActivity())){
                    items.clear();
                    programaId = listaProgramas[i].id;
                    bandera = true;
                    ConsultaRecord record = new ConsultaRecord();
                    record.execute();
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
     * Comprueba si el usuario tiene una conexión a internet
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
     * Clase que se encarga de consumir el servicio Web publicado, mediante el protocolo SOAP.
     */
    private class ConsultaRecord extends AsyncTask<String,Integer,Boolean> {

        private Record[] listaRecord;
        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
          /*  progressDialog = ProgressDialog.show(getActivity(),
                    "ProgressDialog",
                    "Cargando");*/
        }

        protected Boolean doInBackground(String... params) {
            publishProgress(0);
            boolean resul = true;

            String NAMESPACE = getResources().getString(R.string.NAMESPACE);
            String URL=getResources().getString(R.string.URL);
            String METHOD_NAME = "ConsultarRecord";
            String SOAP_ACTION = "http://tempuri.org/ConsultarRecord";
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
                listaRecord = new Record[resSoap.getPropertyCount()];

                for (int i = 0; i < listaRecord.length; i++)
                {
                    SoapObject ic = (SoapObject)resSoap.getProperty(i);

                    Record record = new Record();

                    record.semestre = ic.getProperty(0).toString();
                    record.materia.add(ic.getProperty(1).toString());
                    record.nota.add(Double.parseDouble(ic.getProperty(2).toString()));
                    record.estado.add(ic.getProperty(3).toString());
                    record.creditos.add(Integer.parseInt(ic.getProperty(4).toString()));
                    record.periodo.add(ic.getProperty(5).toString());

                    listaRecord[i] = record;

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

                ArrayList<String> semestre = new ArrayList<String>();
                items.clear();

                for (int i=0; i<listaRecord.length; i++){

                    if(i==0){
                        items.add(listaRecord[i]);
                        semestre.add(listaRecord[i].semestre);
                    }
                    else {
                        if (!semestre.contains(listaRecord[i].semestre)) {
                            items.add(listaRecord[i]);
                            semestre.add(listaRecord[i].semestre);
                        }
                        else{
                            int posicion = semestre.indexOf(listaRecord[i].semestre);
                            items.get(posicion).materia.add(listaRecord[i].getMateria());
                            items.get(posicion).nota.add(listaRecord[i].getNota());
                            items.get(posicion).estado.add(listaRecord[i].getEstado());
                            items.get(posicion).creditos.add(listaRecord[i].getCreditos());
                            items.get(posicion).periodo.add(listaRecord[i].getPeriodo());
                        }
                    }
                }

                // Obtener el Recycler
                recycler = (RecyclerView) view.findViewById(R.id.recicladorRecord);
                recycler.setHasFixedSize(true);

                // Usar un administrador para LinearLayout
                lManager = new LinearLayoutManager(getActivity());
                recycler.setLayoutManager(lManager);

                // Crear un nuevo adaptador
                adapter = new AdaptadorRecord(items);
                recycler.setAdapter(adapter);

                if(listaRecord.length==0)
                    Toast.makeText(getContext(),"No se encontraron Registros", Toast.LENGTH_LONG).show();
            }
            else
            {
                items.clear();
                Toast.makeText(getContext(),"No se encontraron registros ", Toast.LENGTH_LONG).show();
                //Log.e("FRAGMENTO NOTAS","No se encontraron los registros");
            }
        }
    }

    public class AdaptadorRecord extends RecyclerView.Adapter<RecordViewHolder> {
        private List<Record> items;
        private boolean cargado;

        public AdaptadorRecord(List<Record> items) {
            this.items = items;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public RecordViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.tarjeta_record, viewGroup, false);
            return new RecordViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RecordViewHolder viewHolder, int i) {

            double notaFinal=0;
            double creditosTotales =0;
            cargado = false;
            DecimalFormat df = new DecimalFormat("#.00");

            Log.i("Adapter", "Filas: " + viewHolder.tablaRecord.getChildCount() );


            viewHolder.semestre.setText("Nivel " + items.get(i).getSemestre());

            if(viewHolder.tablaRecord.getChildCount()==1) {
                int filas = items.get(i).materia.size();
                for (int j = 0; j < filas; j++) {

                    TableRow fila = new TableRow(getContext());
                    if (j % 2 == 0)
                        fila.setBackgroundColor(getResources().getColor(R.color.gray_light));

                    TextView materia = new TextView(getContext());
                    TextView nota = new TextView(getContext());
                    TextView estado = new TextView(getContext());
                    TextView creditos = new TextView(getContext());
                    TextView periodo = new TextView(getContext());

                    materia.setPadding(3, 3, 3, 3);
                    nota.setPadding(3, 3, 3, 3);
                    estado.setPadding(3, 3, 3, 3);
                    creditos.setPadding(3, 3, 3, 3);
                    periodo.setPadding(3, 3, 3, 3);

                    LinearLayout.LayoutParams lay;
                    lay = (LinearLayout.LayoutParams) viewHolder.textoMateria.getLayoutParams();
                    materia.setLayoutParams(lay);
                    lay = (LinearLayout.LayoutParams) viewHolder.textNota.getLayoutParams();
                    nota.setLayoutParams(lay);
                    estado.setLayoutParams(lay);
                    creditos.setLayoutParams(lay);
                    creditos.setGravity(Gravity.CENTER_HORIZONTAL);
                    periodo.setLayoutParams(lay);

                    materia.setText(minusculas(items.get(i).materia.get(j)));
                    nota.setText(df.format(items.get(i).nota.get(j)));
                    estado.setText(minusculas(items.get(i).estado.get(j)));
                    creditos.setText(Integer.toString(items.get(i).creditos.get(j)));
                    periodo.setText(items.get(i).periodo.get(j));

                    fila.addView(materia);
                    fila.addView(nota);
                    fila.addView(estado);
                    fila.addView(creditos);
                    fila.addView(periodo);

                    viewHolder.tablaRecord.addView(fila, j + 1);

                    Boolean repetido = false;


                    //Algoritmo para tomar los creditos totales excluyendo las materias perdidas
                    if(j==0){
                        creditosTotales = creditosTotales + items.get(i).creditos.get(j);
                    }
                    else {

                        double creditosTemp = items.get(i).creditos.get(j);

                        for (int k = j; k < filas; k++) {

                            if (j != k) {

                                if(items.get(i).materia.get(j).equals(items.get(i).materia.get(k))) {
                                    repetido = true;
                                }

                            }

                        }
                        if(!repetido)
                            creditosTotales = creditosTotales + creditosTemp;
                    }


                }

                Log.i("Creditos", Double.toString(creditosTotales));

                //Algoritmo para sacar promedio sin materias repetidas
                for (int j = 0; j < filas; j++) {

                    boolean repetido2 = false;

                    if(j==0){
                        notaFinal = notaFinal + (items.get(i).creditos.get(j)/creditosTotales)*items.get(i).nota.get(j);
                    }
                    else {

                        double notaTemp = items.get(i).nota.get(j);
                        double notaTemp1 = 0;

                        for (int k = 0; k < j; k++) {

                            if (j != k) {

                                if(items.get(i).materia.get(j).equals(items.get(i).materia.get(k))) {
                                    repetido2 = true;
                                    if (notaTemp < items.get(i).nota.get(k)) {
                                        notaTemp1 = notaTemp;
                                        notaTemp = items.get(i).nota.get(k);
                                        notaFinal = notaFinal - (items.get(i).creditos.get(j) / creditosTotales) * notaTemp1;
                                    }
                                }

                            }

                        }
                        if(!repetido2)
                            notaFinal = notaFinal + (items.get(i).creditos.get(j)/creditosTotales)*notaTemp;
                    }
                   // notaFinal = notaFinal + (items.get(i).creditos.get(j)/creditosTotales)*items.get(i).nota.get(j);
                }
                //viewHolder.textoPromedio.setText("Promedio nivel: " + df.format(notaFinal));
            }
        }
    }

    public  class RecordViewHolder extends RecyclerView.ViewHolder {
        // Campos respectivos de un item

        public TextView semestre;
        public TableLayout tablaRecord;
        public TextView textoPromedio;
        public TextView textoMateria;
        public TextView textNota;


        ExpandableRelativeLayout el1;


        public RecordViewHolder(View v) {

            super(v);
            semestre = (TextView) v.findViewById(R.id.nivel);
            tablaRecord = (TableLayout) v.findViewById(R.id.tablaRecord);
            textoPromedio = (TextView) v.findViewById(R.id.textoPromedioNivel);
            textoMateria = (TextView) v.findViewById(R.id.textoMateria);
            textNota = (TextView) v.findViewById(R.id.textoNota);
        }
    }

    private class ConsultaProgramas extends AsyncTask<String,Integer,Boolean> {

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
            //progressDialog.dismiss();
            if (result)
            {
                String[] datosSpinner = new String[listaProgramas.length];
                for (int i = 0; i<listaProgramas.length; i++){

                    datosSpinner[i] = minusculas(listaProgramas[i].nombre);
                }

                //programaId = listaProgramas[0].id;
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, datosSpinner);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProgramas.setAdapter(spinnerArrayAdapter);
            }
            else
            {
                Log.e("PROGRAMAS","Error cargando Programas FR3 ");
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

    /**
     * Clase objeto para mostrar las materias por semestre
     */
    public class Record{

        String semestre;
        ArrayList<String> materia;
        ArrayList<Double> nota;
        ArrayList<String> estado;
        ArrayList<Integer> creditos;
        ArrayList<String> periodo;

        public Record(){

            this.semestre = "";
            this.materia = new ArrayList<String>();
            this.nota = new ArrayList<Double>();
            this.estado = new ArrayList<String>();
            this.creditos = new ArrayList<Integer>();
            this.periodo = new ArrayList<String>();

        }
        public Record( String semestre){

            this.semestre = semestre;
            this.materia = new ArrayList<String>();
            this.nota = new ArrayList<Double>();
            this.estado = new ArrayList<String>();
            this.creditos = new ArrayList<Integer>();
            this.periodo = new ArrayList<String>();
        }

        public String getSemestre() {
            return semestre;
        }

        public String getMateria() { return materia.get(0); }

        public double getNota() { return nota.get(0); }

        public String getEstado() { return estado.get(0); }

        public int getCreditos() { return creditos.get(0); }

        public String getPeriodo() { return periodo.get(0); }
        }

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
