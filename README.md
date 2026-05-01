## NaviMusic 项目

### 功能
- Navidrome（Subsonic API）连接
- 内网/外网地址自动切换（Wi-Fi 时优先尝试内网地址 TCP 可达性）
- 歌词显示（LRC 解析 + 同步高亮滚动）
- 后台播放（Media3 ExoPlayer + MediaSession + 通知栏控制）
- 音乐库浏览（歌手/专辑/歌单）
- 搜索（歌曲/专辑/歌手混合结果）
- 播放控制（播放/暂停/上下曲/进度拖拽/迷你播放条）
- 设置页（内外网地址 + 账号配置 + 一键测试连接）

### 项目结构
```
NaviMusic/
├── build.gradle              # 根 build
├── settings.gradle
└── app/
    ├── build.gradle          # 模块依赖（ExoPlayer/Retrofit/Glide等）
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/navimusic/
        │   ├── api/          # SubsonicApi(Retrofit) + NetworkManager(内外网切换)
        │   ├── model/        # 数据模型
        │   ├── repository/   # SubsonicRepository
        │   ├── service/      # MusicPlayerService(Media3)
        │   ├── ui/           # Activity/Fragment/Adapter
        │   ├── util/         # PrefsManager(DataStore) + LrcParser
        │   └── viewmodel/    # PlayerViewModel
        └── res/
            ├── layout/       # XML布局
            ├── navigation/   # nav_graph.xml
            ├── menu/         # bottom_nav_menu + toolbar_menu
            ├── values/       # strings/colors/themes
            ├── color/        # 选择器
            └── drawable/     # 占位图
```

### 构建方式
用 Android Studio 打开 NaviMusic 目录，等待 Gradle 同步后运行即可。
最低 API：24（Android 7.0）

### 内外网切换逻辑
1. 当前为 Wi-Fi → 尝试 TCP 连通内网地址（500ms 超时）
2. 内网可达 → 使用内网地址
3. 内网不可达或非 Wi-Fi → 使用外网地址
