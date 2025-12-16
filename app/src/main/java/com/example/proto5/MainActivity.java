package com.example.proto5;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.proto5.Login.login.Login;
import com.example.proto5.qr_scanner.QrMain;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button login=findViewById(R.id.login);
        login.setOnClickListener(v->{
            Intent intent=new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        });
        Button scan=findViewById(R.id.scan);
        scan.setOnClickListener(v->{
            Intent intent=new Intent(MainActivity.this, QrMain.class);
            startActivity(intent);
        });
    }
}