package com.example.proto5.Login.register;



import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proto5.Login.login.Login;
import com.example.proto5.Login.server_request.ApiClient;
import com.example.proto5.Login.server_request.ApiService;
import com.example.proto5.R;

import com.example.proto5.Login.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register); // Links with respective xml file
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton back=findViewById(R.id.backregister);
        back.setOnClickListener(v->{
            Intent intent=new Intent(CreateAccount.this, Login.class);
            startActivity(intent);
        });
        Button createBtn = findViewById(R.id.loginButton);
        EditText user = findViewById(R.id.userNameBox);
        EditText pass = findViewById(R.id.passwordBox);

        createBtn.setOnClickListener(v -> {
            String u = user.getText().toString();
            String p = pass.getText().toString();



            ApiService api = ApiClient.getClient().create(ApiService.class);
            User newUser = new User(u, p);
            api.registerUser(newUser).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    Toast.makeText(CreateAccount.this, response.body(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(CreateAccount.this, "Server error", Toast.LENGTH_SHORT).show();
                }
            });
        });

    } // onCreate() ends here
}
