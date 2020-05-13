package com.example.organizze.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.organizze.R;
import com.example.organizze.config.ConfiguracaoFirebase;
import com.example.organizze.helper.Base64Custom;
import com.example.organizze.helper.DateCustom;
import com.example.organizze.model.Movimentacao;
import com.example.organizze.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class DespesasActivity extends AppCompatActivity {

    private TextInputEditText campoData, campoCategoria, campoDescricao;
    private EditText campoValor;
    private Movimentacao movimentacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private Double despesaTotal;
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerDespesa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);

        campoCategoria = findViewById(R.id.editCategoria);
        campoData      = findViewById(R.id.editData);
        campoDescricao = findViewById(R.id.editDescricao);
        campoValor     = findViewById(R.id.editValor);

        //Preencher o campo data com a date atual
        campoData.setText(DateCustom.dataAtual());


    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarDespesa();
    }

    public void salvarDespesa(View view){
        if(validarCamposDespesa()) {
            movimentacao = new Movimentacao();
            String data = campoData.getText().toString();
            Double valorRecuperado = Double.parseDouble(campoValor.getText().toString());
            movimentacao.setValor(valorRecuperado);
            movimentacao.setCategoria(campoCategoria.getText().toString());
            movimentacao.setDescricao(campoDescricao.getText().toString());
            movimentacao.setData(data);
            movimentacao.setTipo("d");

            Double despesaAtualizada = despesaTotal + valorRecuperado;
            atualizarDespesa(despesaAtualizada);

            movimentacao.salvar(data);
            finish();
        }
    }

    public boolean validarCamposDespesa() {

        String textoValor = campoValor.getText().toString();
        String textoData = campoData.getText().toString();
        String textoCategoria = campoCategoria.getText().toString();
        String textoDescricao = campoDescricao.getText().toString();

        if (!textoValor.isEmpty()) {
            if (!textoData.isEmpty()) {
                if (!textoCategoria.isEmpty()) {
                    if (!textoDescricao.isEmpty()) {
                        return true;
                    } else {
                        Toast.makeText(DespesasActivity.this,
                                "Preencha a Descrição!",
                                Toast.LENGTH_LONG).show();
                        return false;
                    }
                } else {
                    Toast.makeText(DespesasActivity.this,
                            "Preencha a Categoria!",
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            } else {
                Toast.makeText(DespesasActivity.this,
                        "Preencha a Data!",
                        Toast.LENGTH_LONG).show();
                return false;
            }
        } else {
            Toast.makeText(DespesasActivity.this,
                    "Preencha o Valor!",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void recuperarDespesa(){

        String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());
        usuarioRef = firebaseRef.child("usuarios")
                .child(idUsuario);

        valueEventListenerDespesa = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                despesaTotal = usuario.getDespesaTotal();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void atualizarDespesa(Double despesa){
        String idUsuario = Base64Custom.codificarBase64(autenticacao.getCurrentUser().getEmail());
        DatabaseReference usuarioRef = firebaseRef.child("usuarios")
                .child(idUsuario);

        usuarioRef.child("despesaTotal").setValue(despesa);
    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioRef.removeEventListener(valueEventListenerDespesa);
    }
}
