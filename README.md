文档地址：https://www.kancloud.cn/shf675/walk

**walk简介**
* walk基于spring等开源框架及组件进行二次封装的轻量级web开发框架，使用简便，快速开发，同时兼具高安全性、高稳定性、高扩展性。
* walk并非重复造轮子，与spring理念一致，都是致力于整合现有的技术，并完善细节，使之更加完善，开发简单快速，上手容易，解放开发人员，使主要精力都放到业务开发上。
* walk从2013年开始到迭代到至今，是一个相对成熟且稳定的框架，并已成功应用于很多内部项目中。

**后端**
* IoC容器 spring
* web框架spring mvc
* orm框架 mybatis
* 安全框架 shiro
* 任务调度框架 spring-task
* 缓存 ehcache/redis
* 消息(轻量) redis
* 数据源 dbcp/druid/hikaricp
* 模板视图 jsp
* 工作流 activiti

**前端**
* jquery
* jquery EasyUI
* sea.js
* 其他各种插件，具体看seajs.config.js文件，有相关说明
* 图表组件 fusioncharts

**核心功能**
* 基于redis的分布式缓存、分布式会话、分布式消息队列、分布式任务
* 基于shiro的安全管理(认证、授权)
* 基于mybatis的持久层封装
* 基于swagger的接口开发
* 基于CAS的单点登录
* 基于activiti的工作流封装
* 支持spring boot方式启动，支持以jar方式启动
* 多数据源跨事物解决方案
* 其他：静态参数加载、静态参数翻译器、数据导入导出、表单校验等

**核心模块**
* walk-data：一些基础类
* walk-cache：新定义一套缓存接口，底层基于ehcache与redis分别实现，应用层面可自由切换
* walk-batis：基于mybatis封装的数据库交互工具，简单的增、删、改、查无需写sql，同时支持sql热部署、值绑定后的sql语句输出到控制台等功能
* walk-mq：目前是基于redis实现了轻量的分布式队列、分布式发布/订阅，后续根据需要再去实现kafka、rabbitmq等
* walk-shiro：基于shiro二次封装，提供用户认证、访问授权(支持动态授权)、分布式会话等功能
* walk-base：一系列的封装汇总，包括前端框架、分布式任务、静态参数加载器、静态参数翻译器、数据导入导出、表单校验等
* walk-restful：基于swagger实现的接口开发框架，规范接口开发，同时提供代码生成工具及API生成工具
* walk-activiti：基于开源工作流activiti的封装
* walk-console：提供缓存、静态参数、会话、消息队列的管理界面
* walk-boot：支持spring boot方式启动，同时也支持直接以单独jar包方式运行

* * * * *
**示例工程**
详细请看示例工程：https://github.com/shf675/walk-demo
