## 云打印

北京交通大学威海校区联创云打印安卓 App，通过抓包分析网页版云打印对接了登录和上传任务
API，实现登录持久化和**一键上传**
打印任务，并以 webView 集成了其余页面。

### 使用说明

- 安装后打开本软件，授予**存储和网络权限**，连接**校园网**后使用自己的云打印账号密码进行**登录**。(
  登录成功后每次启动软件会自动登录进入首页，如需切换账号，依次点击，首页右上角-设置-退出，会再次进入到登录页面)
- 登录后从手机任意 app 里对要打印的文件选择**分享**或选择**其他应用打开/打开方式**，在对话框选择本软件。手机会跳转到本
  app 的任务上传页面并**自动拉取文件**
  ，自定义参数后，点击确定按钮即可完成任务上传。
- 也可使用选择文件按钮调动文件管理器，在文件管理器选择文件后上传（对于 **Android 11** 及以上的手机，如需访问
  android\data\ 内的文件，请使用其他**
  有访问权限**的第三方文件管理器，如 RE、ES、SE 等）
- 以 webView 集成了其余页面，在 app 内可使用**网页版所有功能**。

![通过其他应用打开上传](res/VIEW.gif "通过其他应用打开上传") ![通过分享上传](res/SEND.gif "通过分享上传")

### 功能实现

- 整体使用 MVVM 架构
- 登录页面使用了Android Studio 模板的登录框架代码，在模板内进行了修改
- 使用 Compose Material Design 的控件实现上传页面，适配手机和平板
- 使用 intent 机制，接收手机的对可打印文件的查看（VIEW）和发送（SEND）两种隐式 intent
- 对 intent 的进行解析，对于 scheme 为 file 的 intent 直接使用 intent.file 文件，对于 scheme 为
  content 的
  intent 将 intent 转为文件存至本软件的文件夹中，然后调用打印上传功能
- 使用 retrofit 进行网络请求，使用 okhttp 对 retrofit 进行 cookies 管理，使用 GSON 解析请求响应
- 使用 sharedPreferences 存储登录信息，登录成功后在每次上传任务时自动登录
- 以 webView 集成了其余页面，通过对 webView 个别 URL 的拦截，可再次返回到本 app 的自有页面

### 其他说明

- 开发该 App 旨在学习 Android 开发和为自己为同学们提供便利，如有问题和 bug 请在 issue 反馈
- 本人第一次做 Android 开发，kotlin 也是现学的，如果有更好的方案或对代码有意见请在 issue 反馈或提交
  pull request
- 理论上该代码仅需更改服务器 IP 地址就可适配其他使用联创云打印且使用相同 API 的学校
- 上架应用商店需要申请软著，周期较长，故只在
  [releases](https://github.com/Darley-Wey/UniFound-Printer/releases)
  [发行版](https://gitee.com/Darley-Wey/Unifound-Printer/releases)
  中提供 APK，如需在自己电脑上 build，请在 build.gradle 更改为自己的证书文件及密码。
-

如果觉得软件好用，可以分享给你的同学，也可以在[Gitee仓库](https://gitee.com/Darley-Wey/Unifound-Printer)
桌面版网页下方捐赠按钮适量捐赠以表支持，谢谢！

### TODO

- [x] 优化登录检查，上传检查
- [x] 实现任务上传页面，可在上传前对上传任务进行自定义参数配置
- [x] 任务上传页面添加文件选择器，实现类似网页版的手动上传
- [x] 以 webView 接入网页版的任务查看和管理等页面
- [x] 为上传进度添加百分比显示
- [ ] 英语支持（当前禁用了 webView 内原网页版自带的语言切换功能，等待完成原生页面语言适配后统一开启）