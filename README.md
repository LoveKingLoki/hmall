1. 启动docker：mysql数据库、nacos管理服务和配置文件、seata分布式事务、mq消息队列
2. 本地启动服务保护sentinel：java -Dserver.port=8090 -Dcsp.sentinel.dashboard.server=localhost:8090 -Dproject.name=sentinel-dashboard -jar sentinel-dashboard.jar
3. 启动类：CartApplication、GatewayApplication、ItemApplication、PayApplication、TradeApplication、UserApplication
