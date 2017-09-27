package com.apps.unisabanetaapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.plus.model.people.Person;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.biubiubiu.justifytext.library.JustifyTextView;

/**
 * Fragmento que controla una Interfaz Visual para el modulo de Noticias por tarjetas
 */
public class FragmentoNoticias extends Fragment {

    private RecyclerView recycler;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager lManager;
    private Noticia[] listaNoticias;
    View view;
    public List<Noticia> items;
    byte[] byteArray;
    private ProgressDialog mProgressDialog;
    ImageButton botonActualizar;
    TextView textoActualizar;
    LinearLayout layoutActualizar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragmento_noticias, container, false);
        return view;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        items = new ArrayList<>();

        layoutActualizar = (LinearLayout) view.findViewById(R.id.layoutActualizar);
        botonActualizar = (ImageButton) view.findViewById(R.id.boton_actualizar);
        textoActualizar = (TextView) view.findViewById(R.id.texto_actualizar);

        botonActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTask();
            }
        });

        runTask();

        byteArray = new byte[0];
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        Log.i("Imagen ", byteArray.toString());
    }

    public void runTask () {
        if(verificaConexion(getActivity()))
        {
            ConsultaNoticias consultaNoticias = new ConsultaNoticias();
            consultaNoticias.execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error de Conexión");
            builder.setMessage("Verifica tu conexión a internet.\nRed no disponible.");
            builder.setCancelable(false);
            builder.setPositiveButton("Reintentar", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    runTask();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

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
    private class ConsultaNoticias extends AsyncTask<String,Integer,Boolean> {

        //ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        protected Boolean doInBackground(String... params) {
            publishProgress(0);
            boolean resul = true;

            String NAMESPACE = getResources().getString(R.string.NAMESPACE);
            String URL=getResources().getString(R.string.URL_noticias);
            String METHOD_NAME = "ConsultaNoticiasUni";
            String SOAP_ACTION = "http://tempuri.org/ConsultaNoticiasUni";
            String USER = getResources().getString(R.string.User_SOAP);
            String PASS = getResources().getString(R.string.Pass_SOAP);

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

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
                listaNoticias = new Noticia[resSoap.getPropertyCount()];

                for (int i = 0; i < listaNoticias.length; i++)
                {
                    SoapObject ic = (SoapObject)resSoap.getProperty(i);

                    Noticia noticia = new Noticia();
                    noticia.titulo = ic.getProperty(0).toString();
                    noticia.autor = ic.getProperty(1).toString();
                    noticia.descripcion = ic.getProperty(3).toString();
                    noticia.fechaInicio = ic.getProperty(5).toString();
                    noticia.fechaFin = ic.getProperty(2).toString();


                    try {
                        byte[] byteArray = Base64.decode(ic.getProperty(4).toString(), Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        noticia.imagen = bmp;
                    }catch (Exception e){

                    }

                    //noticia.likes =Integer.parseInt(ic.getProperty(6).toString());
                    noticia.URL = ic.getProperty(6).toString();

                    listaNoticias[i] = noticia;
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
                //Toast.makeText(getContext(),"Todo Bien2", Toast.LENGTH_LONG).show();
                items.clear();

                for (int i=0; i<listaNoticias.length; i++){
                    items.add(listaNoticias[i]);
                }

                if(listaNoticias.length == 0)
                {
                    layoutActualizar.setVisibility(View.VISIBLE);
                    botonActualizar.setVisibility(View.VISIBLE);
                    textoActualizar.setVisibility(View.VISIBLE);
                }
                else{

                    layoutActualizar.setVisibility(View.GONE);
                    botonActualizar.setVisibility(View.GONE);
                    textoActualizar.setVisibility(View.GONE);

                }

                // Obtener el Recycler
                recycler = (RecyclerView) view.findViewById(R.id.reciclador);
                recycler.setHasFixedSize(true);

                // Usar un administrador para LinearLayout
                lManager = new LinearLayoutManager(getActivity());
                recycler.setLayoutManager(lManager);

                // Crear un nuevo adaptador
                adapter = new AdaptadorNoticias(items);
                recycler.setAdapter(adapter);

                Log.i("PROGRAMAS","Cargado Exitosamente ");
            }
            else
            {
                layoutActualizar.setVisibility(View.VISIBLE);
                botonActualizar.setVisibility(View.VISIBLE);
                textoActualizar.setVisibility(View.VISIBLE);

                Log.e("PROGRAMAS","Error Noticias Fragmento NOTICIAS ");
            }
        }
    }

    /**
     * Vista de cada Noticia, se incluyen los manejadores de eventos desde aquí.
     */

    public static class NoticiaViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{
        // Campos respectivos de un item
        public ImageView imagen;
        public TextView nombre;
        public TextView descripcion;
        public Button verMas;
        public ImageButton compartir;
        public ImageButton like;
        public Boolean likeNoticia;
        public TextView conteoLikes;


        private List<Noticia> items;
        private FirebaseAnalytics mFirebaseAnalytics;
        private FirebaseAuth mAuth;
        private FirebaseAuth.AuthStateListener mAuthListener;
        FirebaseDatabase database;
        DatabaseReference myRef;
        private Bundle bundle;
        private DatabaseReference DBLikes;
        final String iid;



        public NoticiaViewHolder(View v) {
            super(v);

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(v.getContext());
            mAuth = FirebaseAuth.getInstance();
            iid = InstanceID.getInstance(v.getContext()).getId();

           // mAuthListener = FirebaseAuth.AuthStateListener
            bundle = new Bundle();
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("message");
            DBLikes = FirebaseDatabase.getInstance().getReference().child("likes");
            DBLikes.keepSynced(true);
            likeNoticia = false;

            imagen = (ImageView) v.findViewById(R.id.imagen);
            nombre = (TextView) v.findViewById(R.id.nombre);
            descripcion = (TextView) v.findViewById(R.id.descripcion);
            conteoLikes = (TextView)v.findViewById(R.id.conteoLikes);
            verMas = (Button) v.findViewById(R.id.verMas);
            verMas.setOnClickListener(this);
            compartir = (ImageButton) v.findViewById(R.id.compartir);
            compartir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String URL = items.get(getAdapterPosition()).getURL();
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, items.get(getAdapterPosition()).getTitulo());
                    i.putExtra(Intent.EXTRA_TEXT, URL);
                    v.getContext().startActivity(Intent.createChooser(i, "Compartir Noticia"));

                    try {
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Compartir_Noticia");
                        mFirebaseAnalytics.logEvent("Compartir_Noticia", bundle);
                    }
                    catch (Exception e){
                        Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
            like = (ImageButton) v.findViewById(R.id.me_gusta);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    likeNoticia = true;
                    final String iid = InstanceID.getInstance(v.getContext()).getId();
                    final String titulo = items.get(getAdapterPosition()).getTitulo();


                        DBLikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(likeNoticia) {

                                    if (dataSnapshot.child(titulo).hasChild(iid)) {

                                        DBLikes.child(titulo).child(iid).removeValue();
                                        likeNoticia = false;

                                    } else {
                                        DBLikes.child(titulo).child(iid).setValue("Like");
                                        likeNoticia = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                }
            });
            v.setOnClickListener(this);

        }

        public void setLikeBtn(final String titulo){

            DBLikes.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    conteoLikes.setText(Long.toString(dataSnapshot.child(titulo).getChildrenCount()));

                    if(dataSnapshot.child(titulo).hasChild(iid)){

                        like.setImageResource(R.drawable.ic_thumb_up_blue);


                    }
                    else {
                        like.setImageResource(R.drawable.ic_thumb_up);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setItemsLayout(List<Noticia> items){
            this.items = items;
        }

        @Override
        public void onClick(View view) {

            Context context = view.getContext();
            ImageHolder.imageHolder.setBitmap(items.get(getAdapterPosition()).getImagen());
            Intent intent = new Intent(context, ActividadDetalleNoticia.class);
            intent.putExtra("Titulo",items.get(getAdapterPosition()).getTitulo());
            intent.putExtra("Descripcion", items.get(getAdapterPosition()).getDescripcion());
            intent.putExtra("Autor", items.get(getAdapterPosition()).getAutor());
            intent.putExtra("Fecha", items.get(getAdapterPosition()).getFechaInicio());
            intent.putExtra("Url", items.get(getAdapterPosition()).getURL());
            context.startActivity(intent);
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public static class AdaptadorNoticias extends RecyclerView.Adapter<NoticiaViewHolder> {
        private List<Noticia> items;

        public AdaptadorNoticias(List<Noticia> items) {
            this.items = items;
        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public NoticiaViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.tarjeta_noticia, viewGroup, false);

            final String titulo =  items.get(i).getTitulo();



            return new NoticiaViewHolder(v);
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

        @Override
        public void onBindViewHolder(NoticiaViewHolder viewHolder, int i) {
            String text;
            viewHolder.imagen.setImageBitmap( items.get(i).getImagen());
            viewHolder.nombre.setText(minusculas(items.get(i).getTitulo()));
            viewHolder.descripcion.setText(String.valueOf(items.get(i).getDescripcion()));
            viewHolder.setItemsLayout(items);
            viewHolder.setLikeBtn(items.get(i).getTitulo());
        }


    }
    /**
     * Estructura de cada Noticia
     */

    public class Noticia {

        private Bitmap imagen;
        private String titulo;
        private String autor;
        private String descripcion;
        private String fechaInicio;
        private String fechaFin;
        private int likes;
        private String URL;

        public Noticia(){

            imagen = null;
            titulo = "";
            autor = "";
            descripcion = "";
            fechaInicio = "";
            fechaFin = "";
            likes = 0;
            URL = "";
        }
        public Noticia(Bitmap imagen, String titulo, String descripcion, String autor, String fechaInicio, String fechaFin, int likes, String URL) {
            this.imagen = imagen;
            this.titulo = titulo;
            this.autor = autor;
            this.descripcion = descripcion;
            this.fechaInicio = fechaInicio;
            this.fechaFin = fechaFin;
            this.likes = likes;
            this.URL = URL;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getAutor() {
            return autor;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public Bitmap getImagen() {
            return imagen;
        }

        public int getLikes(){ return likes;}

        public String getFechaInicio() {return fechaInicio;}

        public String getFechaFin() {return fechaFin;}

        public String getURL(){return URL;}
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public static class ImageHolder {

        private Bitmap bitmap;
        public void setBitmap(Bitmap bitmap){this.bitmap = bitmap;}
        public Bitmap getBitmap(){return  bitmap;}

        private static final ImageHolder imageHolder = new ImageHolder();
        public static ImageHolder getInstance() {return imageHolder;}
    }

}
