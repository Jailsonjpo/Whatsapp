package com.jailsonspeedway.whatsapp.activity;

import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.jailsonspeedway.whatsapp.R;
import com.jailsonspeedway.whatsapp.adapter.MensagensAdapter;
import com.jailsonspeedway.whatsapp.config.ConfiguracaoFirebase;
import com.jailsonspeedway.whatsapp.helper.Base64Custom;
import com.jailsonspeedway.whatsapp.helper.UsuarioFirebase;
import com.jailsonspeedway.whatsapp.model.Mensagem;
import com.jailsonspeedway.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewfoto;
    private EditText editMensagem;
    private Usuario usuarioDestinatario;
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagem;

    private RecyclerView recyclerMensagens;

    //Identificador usuarios remetente e destinatario
    private String idUsuarioRemetente, idUsuarioDestinatario;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configuracoes iniciais
        textViewNome        = findViewById(R.id.textViewNomeChat);
        circleImageViewfoto = findViewById(R.id.circleImageFotoChat);
        editMensagem        = findViewById(R.id.editMensagem);
        recyclerMensagens   = findViewById(R.id.recyclerMensagens);

        //Recuperar dados do usuário remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();

        //Recuperar dados do usuário destinatário

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){

            usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
            textViewNome.setText(usuarioDestinatario.getNome());

            String foto = usuarioDestinatario.getFoto();
            if (foto != null){
                Uri url = Uri.parse(usuarioDestinatario.getFoto());
                Glide.with(ChatActivity.this).load(url).into(circleImageViewfoto);

            }else{
                circleImageViewfoto.setImageResource(R.drawable.padrao);
            }
        }

        //Recuperar dados do usuário destinatário
        idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());

    //Configuracao adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext() );

    //Configuracao recyclerview
   RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
    recyclerMensagens.setLayoutManager(layoutManager);
    recyclerMensagens.setHasFixedSize(true);
    recyclerMensagens.setAdapter(adapter);

    database = ConfiguracaoFirebase.getFirebaseDatabase();
    mensagensRef = database.child("mensagens").child(idUsuarioRemetente).child(idUsuarioDestinatario);
    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if(!textoMensagem.isEmpty()){

            Mensagem mensagem = new Mensagem();
            mensagem.setIdUsuario(idUsuarioRemetente);
            mensagem.setMensagem(textoMensagem);

            //Salvar mensagem para o remetente
            salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

            //Salvar mensagem para o Destinatário
            salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

        }else{
            Toast.makeText(ChatActivity.this, "Digite uma mensagem para enviar!", Toast.LENGTH_LONG).show();
        }
    }

    private void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){

        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemref = database.child("mensagens");

        mensagemref.child(idRemetente).child(idDestinatario).push().setValue(msg);

        //Limpar texto
        editMensagem.setText("");
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagem);
    }

    private void recuperarMensagens(){

        childEventListenerMensagem = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Mensagem mensagem = dataSnapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



}
