package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {
    EditText etRegUser, etRegPwd, etRegPwd2, etCode;
    Button btnRegister, btnPickAvatar, btnRefreshCode,btFinishReg;
    ImageView ivAvatarPreview;
    ImageView ivAvatar1, ivAvatar2, ivAvatar3;
    TextView tvCode;
    // 预设头像存资源ID字符串（如 "2131230823"）；自定义头像存 "file:/绝对路径"
    private String selectedAvatarId = String.valueOf(R.drawable.p1);
    private String currentCode = "";   // 当前4位验证码
    SharedPreferences userSp;
    static final String USER_SP = "UserInfo";

    // 相册选择头像的回调
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String path = saveAvatarToInternal(uri);
                    if (path != null) {
                        selectedAvatarId = "file:" + path;
                        Bitmap bmp = BitmapFactory.decodeFile(path);
                        if (bmp != null) ivAvatarPreview.setImageBitmap(bmp);
                        // 选了自定义头像，取消三个备选的选中高亮
                        clearPresetHighlight();
                        Toast.makeText(this, "已选择自定义头像", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "读取头像失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etRegUser = findViewById(R.id.et_reg_username);
        etRegPwd = findViewById(R.id.et_reg_pwd);
        etRegPwd2 = findViewById(R.id.et_reg_pwd2);
        etCode = findViewById(R.id.et_code);
        btnRegister = findViewById(R.id.btn_register);
        btnPickAvatar = findViewById(R.id.btn_pick_avatar);
        btnRefreshCode = findViewById(R.id.btn_refresh_code);
        tvCode = findViewById(R.id.tv_code);
        ivAvatarPreview = findViewById(R.id.iv_avatar_preview);
        ivAvatar1 = findViewById(R.id.iv_avatar1);
        ivAvatar2 = findViewById(R.id.iv_avatar2);
        ivAvatar3 = findViewById(R.id.iv_avatar3);
        userSp = getSharedPreferences(USER_SP, Context.MODE_PRIVATE);
        btFinishReg = findViewById(R.id.btn_register1);

        // 默认预览并选中第一个备选头像
        ivAvatarPreview.setImageResource(R.drawable.p1);
        selectPreset(ivAvatar1, R.drawable.p1);

        // 三个备选头像：点击选中（互斥）
        ivAvatar1.setOnClickListener(v -> selectPreset(ivAvatar1, R.drawable.p1));
        ivAvatar2.setOnClickListener(v -> selectPreset(ivAvatar2, R.drawable.p2));
        ivAvatar3.setOnClickListener(v -> selectPreset(ivAvatar3, R.drawable.p3));

        // 生成验证码
        generateCode();
        // 换一张：重新生成验证码
        btnRefreshCode.setOnClickListener(v -> generateCode());

        // 从相册选择自定义头像
        btnPickAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnRegister.setOnClickListener(v -> {
            String username = etRegUser.getText().toString().trim();
            String pwd1 = etRegPwd.getText().toString().trim();
            String pwd2 = etRegPwd2.getText().toString().trim();
            String inputCode = etCode.getText().toString().trim();

            // ===== 校验 =====
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(pwd1) || TextUtils.isEmpty(pwd2)) {
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            // 两次密码必须相同才能提交
            if (!pwd1.equals(pwd2)) {
                Toast.makeText(this, "两次输入密码不一致！", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pwd1.length() < 6) {
                Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean hasLetter = pwd1.matches(".*[a-zA-Z].*");
            boolean hasDigit = pwd1.matches(".*\\d.*");
            if (!hasLetter || !hasDigit) {
                Toast.makeText(this, "密码必须包含字母+数字", Toast.LENGTH_SHORT).show();
                return;
            }
            // 验证码必须正确才能注册
            if (TextUtils.isEmpty(inputCode)) {
                Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!inputCode.equals(currentCode)) {
                Toast.makeText(this, "验证码错误", Toast.LENGTH_SHORT).show();
                generateCode();          // 答错自动刷新验证码
                etCode.setText("");
                return;
            }
            if (TextUtils.isEmpty(selectedAvatarId)) {
                Toast.makeText(this, "请选择头像", Toast.LENGTH_SHORT).show();
                return;
            }
            // 同一用户名无法再次注册
            String existPwd = userSp.getString(username + "_pwd", "");
            if (!TextUtils.isEmpty(existPwd)) {
                Toast.makeText(this, "该用户名已被注册！", Toast.LENGTH_SHORT).show();
                return;
            }

            // ===== 保存新用户（支持多用户）=====
            SharedPreferences.Editor editor = userSp.edit();
            editor.putString(username + "_pwd", pwd1);
            editor.putString(username + "_avatar", selectedAvatarId);
            // 记录注册时间，用于登录页“免密登录”展示最近注册的账号
            editor.putString(username + "_regtime", String.valueOf(System.currentTimeMillis()));
            editor.apply();
            Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
            finish(); // 注册成功，返回登录页
        });
        btFinishReg.setOnClickListener(v ->{
            Intent intent = new Intent(RegisterActivity.this, MainActivity2.class);
            startActivity(intent);
            finish();
        });
    }

    // 选中某个备选头像（互斥）：清掉其它高亮，给当前加高亮环
    private void selectPreset(ImageView selected, int resId) {
        clearPresetHighlight();
        selected.setForeground(ContextCompat.getDrawable(this, R.drawable.bg_avatar_ring));
        selectedAvatarId = String.valueOf(resId);
        ivAvatarPreview.setImageResource(resId);
    }

    private void clearPresetHighlight() {
        ivAvatar1.setForeground(null);
        ivAvatar2.setForeground(null);
        ivAvatar3.setForeground(null);
    }

    // 生成 4 位随机数字验证码
    private void generateCode() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(r.nextInt(10));
        }
        currentCode = sb.toString();
        tvCode.setText(currentCode);
    }

    // 将相册选中的图片拷贝到应用私有目录，避免跨进程 URI 失效
    private String saveAvatarToInternal(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is == null) return null;
            File dir = new File(getFilesDir(), "avatars");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(out);
            byte[] buf = new byte[4096];
            int len;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fos.close();
            is.close();
            return out.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
