package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    Button btLoginCheck;
    EditText etUsername, etPassword;
    String LoginName, LoginPass;

    static final String PREFERENCE_NAME = "SaveLogin";
    static final String USER_SP = "UserInfo"; //存放所有注册用户的SP
    static final int MODE = Context.MODE_PRIVATE;
    SharedPreferences sharedPreferences;
    SharedPreferences userSp;

    CheckBox cbIsSave;
    TextView tvRegister;
    ImageView ivAvatar;

    // 免密登录相关控件
    LinearLayout tabAccount, tabFree, groupAccount, groupFree;
    View indAccount, indFree;
    TextView tvFreeName, tvFreeHint, tvSwitchAccount;
    ImageView ivFreeAvatar;
    Button btnFreeLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_pwd);
        btLoginCheck = findViewById(R.id.btn_login);
        cbIsSave = findViewById(R.id.cbsave);
        tvRegister = findViewById(R.id.tv_register);
        ivAvatar = findViewById(R.id.iv_avatar);

        sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE);
        userSp = getSharedPreferences(USER_SP, MODE);
        loadUserInfo();

        // 用户名输入框变化时，实时显示该账号对应的头像
        etUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String name = s.toString().trim();
                String av = userSp.getString(name + "_avatar", "");
                showAvatar(ivAvatar, av);
            }
        });
        // 自动填充（记住密码）的账号也同步显示头像
        showAvatar(ivAvatar, userSp.getString(etUsername.getText().toString().trim() + "_avatar", ""));
        //读取记住密码标记
        boolean isSave = sharedPreferences.getBoolean("isSave", false);
        cbIsSave.setChecked(isSave);

        //====注册文字点击跳转注册页面====
        tvRegister.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity2.this, RegisterActivity.class);
            startActivity(intent);
        });

        //====免密登录：绑定控件与标签切换====
        tabAccount = findViewById(R.id.tab_account);
        tabFree = findViewById(R.id.tab_free);
        groupAccount = findViewById(R.id.group_account);
        groupFree = findViewById(R.id.group_free);
        indAccount = findViewById(R.id.ind_account);
        indFree = findViewById(R.id.ind_free);
        tvFreeName = findViewById(R.id.tv_free_name);
        tvFreeHint = findViewById(R.id.tv_free_hint);
        tvSwitchAccount = findViewById(R.id.tv_switch_account);
        ivFreeAvatar = findViewById(R.id.iv_free_avatar);
        btnFreeLogin = findViewById(R.id.btn_free_login);

        tabAccount.setOnClickListener(v -> showAccountLogin());
        tabFree.setOnClickListener(v -> showFreeLogin());
        tvSwitchAccount.setOnClickListener(v -> showAccountLogin());

        // 一键免密登录：直接以最近注册的账号进入主页
        btnFreeLogin.setOnClickListener(v -> {
            String latest = getLatestUser();
            if (TextUtils.isEmpty(latest)) {
                Toast.makeText(this, "暂无可用账号，请先注册", Toast.LENGTH_SHORT).show();
                return;
            }
            String avatarRes = userSp.getString(latest + "_avatar", "");
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            intent.putExtra("username", latest);
            intent.putExtra("password", userSp.getString(latest + "_pwd", ""));
            intent.putExtra("avatarIdStr", avatarRes);
            startActivity(intent);
            finish();
        });

        // 默认进入免密登录界面
        showFreeLogin();


        btLoginCheck.setOnClickListener(v -> {
            LoginName = etUsername.getText().toString().trim();
            LoginPass = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(LoginName)) {
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(LoginPass)) {
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (LoginPass.length() < 6) {
                Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean hasLetter = LoginPass.matches(".*[a-zA-Z].*");
            boolean hasDigit = LoginPass.matches(".*\\d.*");
            if (!hasLetter || !hasDigit) {
                Toast.makeText(this, "密码必须同时包含字母和数字", Toast.LENGTH_SHORT).show();
                return;
            }

            // =========多用户校验：从SP读取注册用户信息，验证账号密码========
            String savedPwd = userSp.getString(LoginName + "_pwd", "");
            String avatarRes = userSp.getString(LoginName + "_avatar", "");
            if(TextUtils.isEmpty(savedPwd) || !savedPwd.equals(LoginPass)){
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.i("LoginCheck", "验证通过，开始跳转");
            if(cbIsSave.isChecked()){
                saveUserInfo(LoginName, LoginPass);
            }else{
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("Login_Name");
                editor.remove("Login_Pass");
                editor.putBoolean("isSave",false);
                editor.apply();
            }

            // 把用户名、密码、头像资源ID一起传给MainActivity主页
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            intent.putExtra("username", LoginName);
            intent.putExtra("password", LoginPass);
            intent.putExtra("avatarIdStr", avatarRes);
            startActivity(intent);
            finish();
        });
    }

    public void saveUserInfo(String username, String password)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Login_Name",username);
        editor.putString("Login_Pass",password);
        editor.putBoolean("isSave",true);
        editor.apply();
    }

    public void loadUserInfo()
    {
        String name = sharedPreferences.getString("Login_Name","");
        String pwd = sharedPreferences.getString("Login_Pass","");
        etUsername.setText(name);
        etPassword.setText(pwd);
    }

    // 根据存储的头像字符串（资源ID 或 "file:/路径"）显示对应账号的头像
    private void showAvatar(ImageView iv, String avatarStr) {
        if (!TextUtils.isEmpty(avatarStr)) {
            if (avatarStr.startsWith("file:")) {
                String path = avatarStr.substring(5);
                Bitmap bmp = BitmapFactory.decodeFile(path);
                if (bmp != null) {
                    iv.setImageBitmap(bmp);
                    return;
                }
            } else {
                try {
                    int id = Integer.parseInt(avatarStr);
                    iv.setImageResource(id);
                    return;
                } catch (NumberFormatException e) {
                }
            }
        }
        iv.setImageResource(R.drawable.p); // 未匹配到账号时用默认头像
    }

    // 取出“最近注册”的账号（按 _regtime 最大的用户名）
    private String getLatestUser() {
        java.util.Map<String, ?> all = userSp.getAll();
        long maxTime = -1;
        String latest = null;
        for (java.util.Map.Entry<String, ?> entry : all.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("_regtime")) {
                try {
                    long t = Long.parseLong(String.valueOf(entry.getValue()));
                    if (t > maxTime) {
                        maxTime = t;
                        latest = key.substring(0, key.length() - "_regtime".length());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return latest;
    }

    // 切换到“免密登录”界面（默认显示，展示最近注册账号）
    private void showFreeLogin() {
        groupAccount.setVisibility(View.GONE);
        groupFree.setVisibility(View.VISIBLE);
        // 底部操作区：只显示免密登录按钮 + 切换账号入口
        btLoginCheck.setVisibility(View.GONE);
        btnFreeLogin.setVisibility(View.VISIBLE);
        tvRegister.setVisibility(View.GONE);
        tvSwitchAccount.setVisibility(View.VISIBLE);
        // 标签样式：免密登录 高亮（右侧白色台阶）
        ((TextView) findViewById(R.id.tv_tab_free)).setTextColor(getResources().getColor(R.color.wx_text_dark));
        ((TextView) findViewById(R.id.tv_tab_account)).setTextColor(getResources().getColor(R.color.wx_text_gray));
        ((TextView) findViewById(R.id.tv_tab_free)).setTypeface(null, android.graphics.Typeface.BOLD);
        ((TextView) findViewById(R.id.tv_tab_account)).setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFree.setBackgroundResource(R.drawable.bg_tab_right);
        tabAccount.setBackgroundResource(0);
        indFree.setBackgroundResource(R.drawable.bg_tab_indicator);
        indAccount.setBackgroundResource(0);

        String latest = getLatestUser();
        if (!TextUtils.isEmpty(latest)) {
            showAvatar(ivFreeAvatar, userSp.getString(latest + "_avatar", ""));
            tvFreeName.setText(latest);
            tvFreeHint.setText("最近注册的账号，点击一键登录");
            btnFreeLogin.setVisibility(View.VISIBLE);
        } else {
            ivFreeAvatar.setImageResource(R.drawable.p);
            tvFreeName.setText("");
            tvFreeHint.setText("暂无可用账号，请先注册或使用账号登录");
            btnFreeLogin.setVisibility(View.GONE);
        }
    }

    // 切换到“账号登录”界面（用户名 + 密码表单）
    private void showAccountLogin() {
        groupFree.setVisibility(View.GONE);
        groupAccount.setVisibility(View.VISIBLE);
        // 底部操作区：只显示账号登录按钮 + 注册入口
        btnFreeLogin.setVisibility(View.GONE);
        btLoginCheck.setVisibility(View.VISIBLE);
        tvSwitchAccount.setVisibility(View.GONE);
        tvRegister.setVisibility(View.VISIBLE);
        // 标签样式：账号登录 高亮（左侧白色台阶）
        ((TextView) findViewById(R.id.tv_tab_account)).setTextColor(getResources().getColor(R.color.wx_text_dark));
        ((TextView) findViewById(R.id.tv_tab_free)).setTextColor(getResources().getColor(R.color.wx_text_gray));
        ((TextView) findViewById(R.id.tv_tab_account)).setTypeface(null, android.graphics.Typeface.BOLD);
        ((TextView) findViewById(R.id.tv_tab_free)).setTypeface(null, android.graphics.Typeface.NORMAL);
        tabAccount.setBackgroundResource(R.drawable.bg_tab_left);
        tabFree.setBackgroundResource(0);
        indAccount.setBackgroundResource(R.drawable.bg_tab_indicator);
        indFree.setBackgroundResource(0);
        // 同步显示当前用户名对应的头像
        showAvatar(ivAvatar, userSp.getString(etUsername.getText().toString().trim() + "_avatar", ""));
    }
}
