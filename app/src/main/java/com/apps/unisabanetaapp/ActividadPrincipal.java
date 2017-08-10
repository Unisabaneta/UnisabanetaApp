package com.apps.unisabanetaapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.Transaction;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *Clase principal de la aplicación que se encarga de gestionar las interacciones del usuario manejar
 *otras actividades y fragmentos
 *
 *Created by diezc on 1/05/2017.
 *
 **/
public class ActividadPrincipal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener {

    FragmentoNoticias fragmentoNoticias;
    FragmentoSAG sagFragment;
    FragmentTransaction transition;

    int fragmentoVisible;
    NavigationView navigationView;
    TextView correoUsuario;
    TextView usuario;
    View hView;
    boolean sesionIniciada;
    boolean sesion;
    String correo;



    private static final String TAG = "GoogleSignIn";
    private static final int RC_SIGN_IN = 9001;

    private final Context mContext = this;
    private GoogleApiClient mGoogleApiClient;
    private MenuItem itemSesion;
    private ImageView mProfileImageView;
    private ProgressDialog mProgressDialog;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.actividad_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        bundle = new Bundle();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        hView =  navigationView.getHeaderView(0);
        correoUsuario = (TextView)hView.findViewById(R.id.textoCorreo);
        usuario = (TextView)hView.findViewById(R.id.usuario);
        mProfileImageView = (ImageView) hView.findViewById(R.id.imageView);

        fragmentoNoticias = new FragmentoNoticias();
        getSupportFragmentManager().beginTransaction().add(R.id.contenedor, fragmentoNoticias).commit();
        fragmentoVisible = 0;
        navigationView.setCheckedItem(R.id.menu_noticias);

        correo ="";
        sesionIniciada = false;
        sesion = false;

        Menu menu = navigationView.getMenu();
        itemSesion = menu.findItem(R.id.menu_inicioSesion);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN)) // "https://www.googleapis.com/auth/plus.login"
                .setHostedDomain("unisabaneta.edu.co")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this , this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

    }

    /**
     * Cuando el usuario presiona el boton atrás.
     */

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            switch (fragmentoVisible){
                case 0:
                    super.onBackPressed();
                    break;
                case 1:
                    FragmentTransaction transition =  getSupportFragmentManager().beginTransaction();
                    transition.setCustomAnimations(R.anim.in_from_left, R.anim.out_to_right);
                    transition.replace(R.id.contenedor, fragmentoNoticias);
                    transition.commit();
                    fragmentoVisible = 0;
                    navigationView.setCheckedItem(R.id.menu_noticias);
                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    toolbar.setTitle("Unisabaneta Móvil");
                    break;
            }

        }

    }

    /**
     * Se instancia automaticamente al crearse la actividad
     * @param menu
     * @return
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Maneja los difentes clics de la barra de acción
     * @param item este es el item seleccionado
     * @return
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            aboutWindow();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menu_noticias) {
            FragmentTransaction transition =  getSupportFragmentManager().beginTransaction();
            transition.setCustomAnimations(R.anim.in_from_left, R.anim.out_to_right);
            transition.replace(R.id.contenedor, fragmentoNoticias);
            transition.commit();
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitle("Unisabaneta Móvil");
            fragmentoVisible = 0;

        } else if (id == R.id.menu_notas) {

                if (verificaConexion(this)){

                    if(sesionIniciada && correo.contains("@unisabaneta.edu.co")) {

                        sagFragment = new FragmentoSAG();

                        transition = getSupportFragmentManager().beginTransaction();
                        transition.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left);
                        transition.replace(R.id.contenedor, sagFragment);
                        transition.commit();
                        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                        toolbar.setTitle("SAG - " + usuario.getText());
                        fragmentoVisible = 1;
                    }
                    else{
                      Snackbar.make(getCurrentFocus(), "Debes Iniciar Sesión con tu cuenta de Unisabaneta", Snackbar.LENGTH_LONG).show();
                    }


                }
                else
                    Snackbar.make(getCurrentFocus(), "Verifica tu conexión a internet.", Snackbar.LENGTH_LONG).show();

            fragmentoVisible = 1;
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Menu_Notas");
            mFirebaseAnalytics.logEvent("Menu_Notas", bundle);

        }

         else if (id == R.id.menu_inicioSesion) {

            if(!sesionIniciada) {
                signIn();

            }
            else{
                signOut();
                itemSesion.setTitle("Iniciar Sesión");
                correoUsuario.setText("");
                usuario.setText("Invitado");
                mProfileImageView.setImageResource(R.drawable.user_icon);
                Snackbar.make(getCurrentFocus(), "Sesión Finalizada", Snackbar.LENGTH_LONG ).show();
                sesionIniciada = false;
                sesion = false;

                FragmentTransaction transition =  getSupportFragmentManager().beginTransaction();
                transition.setCustomAnimations(R.anim.in_from_left, R.anim.out_to_right);
                transition.replace(R.id.contenedor, fragmentoNoticias);
                transition.commit();
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                toolbar.setTitle("Unisabaneta Móvil");

                fragmentoVisible = 0;
                navigationView.setCheckedItem(R.id.menu_noticias);
            }

            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Inicio_Sesion");
            mFirebaseAnalytics.logEvent("Inicio_Sesion", bundle);


        }else if (id == R.id.menu_facebook) {

            Uri uri = Uri.parse("https://www.facebook.com/Unisabaneta.colombia/"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Visita_Facebook");
            mFirebaseAnalytics.logEvent("Visita_Facebook", bundle);


        } else if (id == R.id.menu_twitter) {

            Uri uri = Uri.parse("https://twitter.com/UNISABANETA"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Visita_Twitter");
            mFirebaseAnalytics.logEvent("Visita_Twitter", bundle);

        } else if (id == R.id.menu_instagram) {

            Uri uri = Uri.parse("http://instagram.com/unisabaneta"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Visita_Instagram");
            mFirebaseAnalytics.logEvent("Visita_Instagram", bundle);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

            // G+
            if (mGoogleApiClient.hasConnectedApi(Plus.API)) {
                Person person  = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                if (person != null) {
                    Log.i(TAG, "--------------------------------");
                    Log.i(TAG, "Display Name: " + person.getDisplayName());
                    Log.i(TAG, "Gender: " + person.getGender());
                    Log.i(TAG, "About Me: " + person.getAboutMe());
                    Log.i(TAG, "Birthday: " + person.getBirthday());
                    Log.i(TAG, "Current Location: " + person.getCurrentLocation());
                    Log.i(TAG, "Language: " + person.getLanguage());

                } else {
                    Log.e(TAG, "Error!");
                }
            } else {
                Log.e(TAG, "Google+ not connected");
            }
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            correo = acct.getEmail();
            correoUsuario.setText(acct.getEmail());
            usuario.setText(minusculas( acct.getDisplayName()));
            //mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            //itemSesion.setTitle(getString(R.string.signed_in_fmt, acct.getDisplayName()));
            // Views inside NavigationView's header
            /*mUserTextView.setText(acct.getDisplayName());
            mEmailTextView.setText(acct.getEmail());*/
            Uri uri = acct.getPhotoUrl();
            Picasso.with(mContext)
                    .load(uri)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .error(android.R.drawable.sym_def_app_icon)
                    .into(mProfileImageView);
            updateUI(true);
            sesionIniciada = true;
            itemSesion.setTitle("Cerrar Sesión");

            try {
                if(!sesion) {
                    Snackbar.make(getCurrentFocus(), "Sesión Inciada por " + minusculas(acct.getDisplayName()), Snackbar.LENGTH_LONG).show();
                    sesion = true;
                }
            }catch (Exception e){
                Log.e(TAG, "Error manejado");
            }

        } else {
            // Signed out, show unauthenticated UI.
            correo = "";
            updateUI(false);
        }
    }

    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        correo = "";
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(final ConnectionResult result) {
        Log.e("GoogleSignIN", "Connecting to Google Play Services failed. Result=" + result);
        /*Transaction.Handler handler = new Transaction.Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(INTENT_EXTRA_CONNECTION_RESULT, connectionResult);
                context.startActivity(intent);
            }
        });*/
    }

    /**
     *Metodo para mostrar dialogo de progreso al cargar el inicio de sesion y el contenido de las noticias
     */
    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            //mProgressDialog.hide();
            mProgressDialog.dismiss();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {


        } else {

        }
    }

    /**
     *Metodo para verificar si cuenta con conexion a internet por WiFi u operador de Datos
     */
    public static boolean verificaConexion(Context ctx) {
        boolean bConectado = false;
        ConnectivityManager connec = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] redes = connec.getAllNetworkInfo();
        for (int i = 0; i < 2; i++) {
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                bConectado = true;
            }
        }
        return bConectado;
    }

    /**
     *Metodo para transcribir a minusculas si el texto enviado está en mayusculas
     *
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

    /**
     * Método que muestra fragmento con el "Acerca de" de la aplicación
     */

    public void aboutWindow()
    {

        AlertDialog.Builder aboutWindow = new AlertDialog.Builder(this);//creates a new instance of a dialog box
        final String website = "      www.unisabaneta.edu.co";
        final String AboutDialogMessage = "\n\n    Aplicación desarrollada por:   \n\n" +
                "    Unisabaneta para el fortalecimiento institucional.\n\n";
        final TextView tx = new TextView(this);//we create a textview to store the dialog text/contents
        tx.setText(AboutDialogMessage + website);//we set the text/contents
        tx.setAutoLinkMask(RESULT_OK);//to linkify any website or email links
        tx.setTextColor(Color.BLACK);//setting the text color
        tx.setTextSize(15);//setting the text size
        //again to enable any website urls or email addresses to be clickable links
        tx.setMovementMethod(LinkMovementMethod.getInstance());
        Linkify.addLinks(tx, Linkify.WEB_URLS);

        aboutWindow.setIcon(R.drawable.logo_u);//to show the icon next to the title "About"
        aboutWindow.setTitle("UNISABANETA MÓVIL");//set the title of the about box to say "About"
        aboutWindow.setView(tx);//set the textview on the dialog box

        aboutWindow.setPositiveButton("OK", new DialogInterface.OnClickListener()//creates the OK button of the dialog box
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();//when the OK button is clicked, dismiss() is called to close it
            }
        });
        aboutWindow.show();//this method call will bring up the dialog box when the user calls the AboutDialog() method
    }

}
