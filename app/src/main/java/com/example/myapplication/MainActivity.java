package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.myapplication.UI.FragmentOne;
import com.example.myapplication.UI.FragmentTwo;
import com.example.myapplication.UI.FragmentThree;
import com.example.myapplication.UI.FragmentFour;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    //保存登录传过来的数据，传给FragmentFour
    private String username;
    private String avatarStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //接收登录页面传过来Intent数据
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        avatarStr = intent.getStringExtra("avatarIdStr");




        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setItemIconTintList(null);

        switchFragment(new FragmentOne());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.nav_one){
                switchFragment(new FragmentOne());
            }else if(id == R.id.nav_two){
                switchFragment(new FragmentTwo());
            }else if(id == R.id.nav_three){
                switchFragment(new FragmentThree());
            }else if(id == R.id.nav_four){
                //把登录数据打包Bundle传给FragmentFour
                FragmentFour fragmentFour = new FragmentFour();
                Bundle bundle = new Bundle();
                bundle.putString("username", username);
                bundle.putString("avatarIdStr", avatarStr);
                fragmentFour.setArguments(bundle);
                switchFragment(fragmentFour);
            }
            return true;
        });
    }

    private void switchFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
