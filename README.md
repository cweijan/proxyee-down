# Proxyee Down Web Version

Proxyee Down 是一款开源的免费 HTTP 高速下载器，底层使用`netty`开发，支持自定义 HTTP 请求下载且支持扩展功能，可以通过安装扩展实现特殊的下载需求, 该版本Fork自3.03版本

## 未来路线
- 增加错误提示: HttpDownBootstrap类对于错误的处理不合理, 没有友好提示

## 更新日志

### 3.2.0(2020/6/18)

- 修复下载链接有端口号无法下载

### 3.1.0 

- 移除JavaFx,删除了Window模式 
- 默认增加了全局嗅探拓展
- windows增加直接运行已下载文件功能
- 删除任务默认勾选删除文件
- 将各种文件移至用户目录

## 开发

本项目后端主要使用 java+spring boot+netty，前端使用 vue.js+iview。

### 环境
![](https://img.shields.io/badge/JAVA-1.8%2B-brightgreen.svg) ![](https://img.shields.io/badge/maven-3.0%2B-brightgreen.svg) ![](https://img.shields.io/badge/node.js-8.0%2B-brightgreen.svg)

## 编译
- 前端: npm run build 
- 后端: mvn package -P prd

## 运行
  ```
  java -jar proxyee-down-main.jar
  ```

## 任务模块

用于管理下载任务，可以在此页面创建、查看、删除、暂停、恢复下载任务。

- **进阶**
  - [自定义下载请求](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/create/read.md)
  - [刷新任务下载链接](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/refresh/read.md)

## 扩展模块

在开启扩展模块时一定要手动安装一个由 Proxyee Down 随机生成的一个 CA 证书用于`HTTPS MITM`的支持。

- **安装证书**

  进入扩展页面，如果软件检测到没有安装 Proxyee Down CA 证书时，会有对应的安装提示，接受的话点击安装按照系统指引即可安装完毕。
  ![安装证书](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-08-36.png)

- **扩展商店**

  安装完证书后会进入扩展商店页面，目前扩展商店只有一款百度云下载扩展，以后会陆续开发更多的扩展(_例如：各大网站的视频下载扩展、其他网盘的下载扩展等等_)。
  ![扩展商城](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-12-21.png)

- **扩展安装**

  在操作栏找到安装按钮，点击安装即可安装扩展。
  ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-26-44.png)

- **全局代理**

  全局代理默认是不开启的，开启 Proxyee Down 会根据启用的扩展进行对应的系统代理设置，可能会与相同机制的软件发生冲突(_例如：SS、SSR_)。
  如果不使用全局代理，可以点击`复制PAC链接`，配合[SwitchyOmega 插件](https://www.switchyomega.com/)来使用。

- **其他相关**

  - **SwitchyOmega 设置教程**  
    1. 新建情景模式，选择 PAC 情景模式类型。
      ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-25-34.png)
    2. 把复制的 PAC 链接粘贴进来并点击立即更新情景模式然后保存。
      ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-30-30.png)
    3. 切换情景模式进行下载  
      ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-32-00.png)

  - **参与扩展开发**  
    详见[proxyee-down-extension](https://github.com/proxyee-down-org/proxyee-down-extension)

  - **扩展实现原理**  
    扩展功能是由 MITM(中间人攻击)技术实现的，使用[proxyee](https://github.com/monkeyWie/proxyee)框架拦截和修改`HTTP`、`HTTPS`的请求和响应报文，从而实现对应的扩展脚本注入。
