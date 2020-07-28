# JianMu

电信数据上传服务端代码，基于[Play framework框架](https://www.playframework.com/)。

## 项目结构

|  目录/文件  |        说明        |
| :---------: | :----------------: |
|    .g8/     |      模板文件      |
|    app/     |        代码        |
|    conf/    |   配置文件和路由   |
|  project/   |    项目配置文件    |
|   public/   |      静态文件      |
|  scripts/   |   部署相关的脚本   |
|    test/    |      测试代码      |
| .travis.yml | Travis CI 部署脚本 |
|  build.sbt  |    项目配置文件    |

## 部署

1. 分支结构
   - master 分支：开发
   - deploy1 分支：部署
2. 部署方式
   - 新功能开发测试完成后，将 master 分支的代码 merge 到 deploy1 分支，会自动调用`.travis.yml`脚本，触发 [Travis CI](https://travis-ci.org/github/tongjimobiml/JianMu) 持续集成，将最新的代码部署到阿里云服务器上。
