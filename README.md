# 仿微信即时通讯 Android 客户端
> 课程项目，基于 Java 开发的仿微信 UI 即时通讯客户端

## 技术栈
- 语言：Java（计划后续引入 Kotlin 与 Jetpack）
- 开发工具：Android Studio
- UI：XML 布局、RecyclerView 实现会话列表与新闻列表（ViewHolder 复用 ItemView）
- 图片：Glide 异步加载、随列表滚动懒加载
- 核心：多线程异步（OkHttp 回调 / Handler 计时线程）、网络请求、JSON 解析（Gson）、本地存储（SharedPreferences）
- 多媒体：Service + MediaPlayer 后台播放、BroadcastReceiver、WebView
- 版本管理：Git

## 功能说明
1. 多 Tab 页面（4 个）：会话、音乐、新闻、我的
2. 会话列表：RecyclerView + ViewHolder 复用，关注列表滚动性能
3. 音乐播放：后台 Service 播放、进度条拖动跳转、播放完成广播复位
4. 新闻模块：OkHttp 请求天行接口 + Gson 解析，RecyclerView 展示，点击跳转 WebView 详情
5. 登录注册：多用户注册、免密登录、头像选择（相册裁剪到私有目录）
6. 本地缓存用户信息；基础分辨率适配，修复界面交互缺陷

## 安全与规范
- 接口 Key（天行新闻）已移至 `local.properties` 的 `TIAN_API_KEY`，通过 BuildConfig 注入，**不入库**（该文件已被 .gitignore 忽略）。克隆后需自行配置 Key 才能拉取新闻。
- 密码不做明文持久化与 Intent 传递，仅记住用户名，规避敏感信息落盘风险。
- `FragmentTwo` 在 `onDestroy` 中主动停止计时线程、注销广播、释放 MediaPlayer，规避内存泄漏。

## 优化方向
后续计划学习 Jetpack（ViewModel / LiveData / Compose）、Kotlin 协程与 MVVM 架构，并将新闻列表接入 Room 做离线缓存。
