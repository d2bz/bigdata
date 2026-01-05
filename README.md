# 销售管理系统

基于 HBase 和 Redis 的分布式销售管理系统，支持高并发访问、海量数据存储和实时数据查询。

## 系统架构

### 技术栈
- **后端框架**: Spring Boot 2.7.14
- **数据库**: HBase (海量数据存储) + Redis (实时缓存)
- **前端**: HTML5 + CSS3 + JavaScript (Element Plus UI)
- **构建工具**: Maven
- **Java版本**: JDK 11

### 系统特性
- 🚀 **高性能**: Redis缓存 + HBase分布式存储
- 📊 **实时分析**: 实时销售看板、排行榜、趋势分析
- 🛒 **完整功能**: 商品管理、订单处理、购物车、用户管理
- 🔄 **数据同步**: HBase与Redis数据一致性保障
- 📱 **响应式设计**: 支持多端访问

## 功能模块

### 1. 商品管理模块
- ✅ 商品信息存储（HBase）
- ✅ 商品库存实时更新（Redis）
- ✅ 商品搜索和分类管理
- ✅ 库存预警和补货提醒

### 2. 订单处理模块
- ✅ 订单创建与存储（HBase）
- ✅ 订单状态实时更新（Redis）
- ✅ 订单查询与统计分析
- ✅ 物流信息管理

### 3. 用户管理模块
- ✅ 用户信息存储（HBase）
- ✅ 用户会话管理（Redis）
- ✅ 会员等级和积分系统
- ✅ 用户行为分析

### 4. 销售分析模块
- ✅ 实时销售看板（Redis）
- ✅ 历史销售数据分析（HBase）
- ✅ 热门商品排行榜
- ✅ 销售趋势和区域分析

## 数据存储设计

### HBase 表结构

#### 商品信息表 (product_info)
```
RowKey: 商品ID
列族:
- cf_base: 基本信息 (名称、分类、价格、状态等)
- cf_detail: 详细信息 (描述、规格、图片、标签)
- cf_stock: 库存信息 (总库存、仓库库存、安全库存)
- cf_stat: 统计信息 (浏览数、销量、收藏数)
```

#### 订单表 (order_history)
```
RowKey: 订单ID
列族:
- cf_base: 订单基本信息 (用户ID、金额、状态、时间等)
- cf_address: 收货信息 (收货人、电话、地址)
- cf_items: 商品明细 (动态列存储)
- cf_logistics: 物流信息 (快递公司、快递单号、轨迹)
```

#### 用户表 (user_profile)
```
RowKey: 用户ID
列族:
- cf_base: 基本信息 (用户名、昵称、手机、邮箱等)
- cf_account: 账户信息 (等级、积分、余额、成长值)
- cf_address: 地址信息 (多版本存储)
- cf_behavior: 行为信息 (登录记录、消费统计)
```

#### 销售数据表 (sales_data)
```
RowKey: 日期_商品ID 或 日期_品类ID
列族:
- cf_daily: 每日销售数据
- cf_hourly: 小时级数据
- cf_region: 区域销售数据
```

### Redis 数据结构

#### 库存缓存
```
Key格式: stock:商品ID
Value: 库存数量
TTL: 1小时
```

#### 购物车数据
```
Key格式: cart:用户ID
Type: Hash
Field: 商品ID
Value: 商品信息JSON
TTL: 7天
```

#### 销售排行榜
```
Key格式: rank:daily:sale
Type: Sorted Set
Score: 销售金额
Member: 商品ID
```

#### 用户会话
```
Key格式: session:会话ID
Value: 用户信息JSON
TTL: 30分钟
```

## 快速开始

### 环境要求
- JDK 11+
- Maven 3.6+
- HBase 2.4+
- Redis 6.0+

### 配置说明

1. **修改配置文件** `src/main/resources/application.properties`:
```properties
# HBase配置
hbase.zookeeper.quorum=your-hbase-zookeeper-host
hbase.zookeeper.property.clientPort=2181

# Redis配置
spring.redis.host=your-redis-host
spring.redis.port=6379
spring.redis.password=your-redis-password
```

2. **构建项目**:
```bash
mvn clean package
```

3. **启动应用**:
```bash
java -jar target/bigdata-sales-system-1.0.0.jar
```

4. **访问系统**:
```
http://localhost:8080/sales
```

## API 文档

### 商品管理 API
- `GET /api/products` - 获取商品列表
- `GET /api/products/{productId}` - 获取商品详情
- `POST /api/products` - 创建商品
- `PUT /api/products/{productId}` - 更新商品
- `DELETE /api/products/{productId}` - 删除商品
- `PUT /api/products/{productId}/stock` - 更新库存
- `POST /api/products/{productId}/stock/deduct` - 扣减库存

### 订单管理 API
- `GET /api/orders` - 获取订单列表
- `GET /api/orders/{orderId}` - 获取订单详情
- `POST /api/orders` - 创建订单
- `POST /api/orders/{orderId}/pay` - 支付订单
- `POST /api/orders/{orderId}/deliver` - 发货
- `POST /api/orders/{orderId}/complete` - 确认收货
- `POST /api/orders/{orderId}/cancel` - 取消订单

### 用户管理 API
- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/{userId}` - 获取用户信息
- `PUT /api/users/{userId}` - 更新用户信息
- `PUT /api/users/{userId}/points` - 更新积分
- `PUT /api/users/{userId}/balance` - 更新余额

### 销售分析 API
- `GET /api/analysis/dashboard` - 获取看板数据
- `GET /api/analysis/daily/{date}` - 获取日销售数据
- `GET /api/analysis/trend` - 获取销售趋势
- `GET /api/analysis/top-products` - 获取热销商品

## 数据同步策略

### 库存同步
1. **实时同步**: Redis作为库存主数据，HBase异步更新
2. **定时同步**: 每分钟检查Redis与HBase库存一致性
3. **一致性保障**: 原子操作 + 分布式锁 + 事务日志

### 缓存策略
1. **商品缓存**: 5分钟过期，读时更新
2. **会话缓存**: 30分钟过期，访问时续期
3. **排行榜缓存**: 实时更新，定期重置

## 性能优化

### 缓存优化
- 多级缓存：本地缓存 + Redis缓存
- 缓存预热：系统启动时预加载热点数据
- 缓存穿透：布隆过滤器防护
- 缓存雪崩：随机过期时间

### 数据库优化
- HBase预分区：避免热点问题
- 批量操作：减少网络开销
- 连接池：复用数据库连接
- 异步处理：提升响应速度

## 监控指标

### 业务指标
- 日销售额、订单数、用户数
- 商品库存预警
- 订单状态分布
- 用户活跃度

### 技术指标
- API响应时间
- 缓存命中率
- 数据库连接数
- 系统资源使用率

## 部署说明

### Docker 部署
```bash
# 构建镜像
docker build -t sales-system .

# 运行容器
docker run -d -p 8080:8080 sales-system
```

### 生产环境配置
- JVM参数调优
- 连接池配置
- 日志级别设置
- 监控告警配置

## 故障排查

### 常见问题
1. **HBase连接失败**: 检查Zookeeper配置
2. **Redis连接超时**: 检查网络和密码配置
3. **库存不一致**: 执行数据一致性检查
4. **性能下降**: 检查缓存命中率和数据库负载

### 日志分析
- 应用日志：`logs/application.log`
- 错误日志：`logs/error.log`
- 访问日志：`logs/access.log`

## 开发指南

### 代码规范
- 遵循阿里巴巴Java开发手册
- 统一异常处理
- 完善的日志记录
- 充分的单元测试

### 扩展开发
- 新增功能模块
- 自定义数据源
- 集成第三方服务
- 性能优化改进

## 联系方式

如有问题或建议，请联系开发团队。

---

**注意**: 本系统为演示项目，生产环境使用请进行充分的测试和安全评估。
