## 云打印

北京交通大学威海校区联创云打印安卓 App，通过抓包分析网页版云打印对接了登录和上传任务 API，实现登录持久化和一键上传打印任务。

### 使用说明

- 打开app，授予存储和网络权限，连接校园网后使用自己的云打印账号密码进行登录。(仅需一次登录，如需切换账号请在手机设置里清空该应用数据)
- 登录后从手机任意app里对要打印的文件选择分享或选择用其他应用打开，在对话框选择本软件。
- 手机会跳转到本 app 的任务上传页面并自动拉取文件，自定义参数后，点击上传按钮即可完成任务上传。
- 也可使用选择文件按钮调动文件管理器，在文件管理器选择文件后上传（对于 Android 11 及以上的手机，如需访问
  android\data\ 内的文件，请使用其他有访问权限第三方文件管理器，如 RE、ES、SE 等）

<img alt="手机上传页面" src="res/Phone.jpg" width="360"/><img alt="平板上传页面" src="res/Pad.jpg" width="360"/>

![通过使用其他应用打开上传](res/VIEW.gif) ![通过分享上传](res/SEND.gif)

### 功能实现

- 整体使用 MVVM 架构
- 登录页面使用了Android Studio模板的登录框架代码，在模板内进行了修改
- 使用 compose material design 的控件实现上传页面，适配手机和平板
- 使用 intent 机制，接收手机的对可打印文件的查看（VIEW）和发送（SEND）两种隐式 intent
- 对 intent 的进行解析，对于 scheme 为 file 的 intent 直接使用 intent.file 文件，对于
  scheme 为 content 的 intent 将 intent 转为文件存至本软件的文件夹中，然后调用打印上传功能
- 使用 retrofit 进行网络请求，使用 okhttp 对 retrofit 进行 cookie 管理，使用 GSON 解析请求响应
- 使用 sharedPreferences 存储登录信息，登录成功后在每次上传任务时自动登录

### 其他说明

- 开发该 App 旨在学习 Android 开发和为自己为同学们提供便利，如有问题和 bug 请在 issue 反馈
- 本人第一次做 Android 开发，kotlin 也是现学的，如果有更好的方案或对代码有意见请在 issue 反馈或提交 pull
  request
- 如有同学有意开发 IOS 版本，希望能为你提供参考，也可直接与我交流。
- 理论上该代码仅需更改服务器 IP 地址就可适配其他使用联创云打印且使用相同 API 的学校
- 在 release 中下载APK，如需要在自己电脑上 build，请在 build.gradle 更改自己的证书文件及密码。

### TODO

- [x] 优化登陆检查，上传检查
- [x] 实现任务上传页面，可在上传前对上传任务进行自定义参数配置
- [x] 任务上传页面添加文件选择器，实现类似网页版的手动上传
- [ ] 以 webView 接入网页版的任务查看和管理等页面