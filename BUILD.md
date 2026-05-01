## 打包方式

### 方法一：GitHub Actions（推荐，无需本地环境）

1. 在 GitHub 创建仓库，把 NaviMusic 整个目录推上去
2. 打开仓库 → Actions → `Build NaviMusic APK` → Run workflow
3. 构建完成后，在 Artifacts 下载 `NaviMusic-debug.apk`（约 10 分钟）

```bash
cd NaviMusic
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/你的用户名/NaviMusic.git
git push -u origin main
```

---

### 方法二：Android Studio（本地构建）

**前提：** 安装 Android Studio + JDK 17

1. File → Open → 选择 `NaviMusic/` 目录
2. 等待 Gradle Sync 完成
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. APK 路径：`app/build/outputs/apk/debug/app-debug.apk`

---

### 方法三：命令行（已有Android SDK）

```bash
cd NaviMusic
# 下载 gradle-wrapper.jar（必须）
curl -L "https://github.com/gradle/gradle/raw/v8.3.0/gradle/wrapper/gradle-wrapper.jar" \
     -o gradle/wrapper/gradle-wrapper.jar
chmod +x gradlew

# 设置 SDK 路径
echo "sdk.dir=$ANDROID_HOME" > local.properties

# 构建
./gradlew assembleDebug
```

APK 输出：`app/build/outputs/apk/debug/app-debug.apk`

---

### 安装到手机

```bash
# 通过 ADB 安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

或直接把 APK 传到手机，在文件管理器中点击安装（需开启"允许未知来源"）。
