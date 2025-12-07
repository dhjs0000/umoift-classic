# 🎮 UMOIFT模组使用指南

## 📦 安装方法

### 1. 前提条件
- **Minecraft版本**：1.20.1
- **Forge版本**：47.4.10或更高版本
- **Java版本**：17或更高版本

### 2. 安装步骤
1. 下载对应版本的Forge安装器
2. 运行Forge安装器，选择"Install client"
3. 将`umoift-*.jar`文件放入Minecraft的`mods`文件夹
4. 启动Minecraft，选择Forge版本运行

## 🌈 主要功能使用

### 1. 彩色灯具系统

#### 获取灯具
- 在创造模式中，灯具会自动出现在以下标签页：
  - 功能方块
  - 建筑方块  
  - 红石方块
- 也可以通过`/give`命令获取，例如：
  ```
  /give @p umoift:white_lamp_next
  ```

#### 灯具特性
- **16种颜色**：白色、橙色、品红色、淡蓝色、黄色、黄绿色、粉色、灰色、淡灰色、青色、紫色、蓝色、棕色、绿色、红色、黑色
- **智能照明**：开启时亮度为15，关闭时为0
- **红石控制**：可以通过红石信号控制开关

#### 合成配方
每种灯具都需要对应颜色的染料 + 基础材料，具体配方可在游戏中通过JEI/REI查看。

### 2. 领地管理系统

#### 基础命令
```
/claim help - 显示帮助信息
```

#### 领地创建
```
/claim addclaim <领地名称> <起始坐标> <终止坐标>
```
**示例**：
```
/claim addclaim 我的家 ~-10 ~-5 ~-10 ~10 ~10 ~10
```
这会在你当前位置创建一个以你为中心，20x15x20区域的领地。

#### 领地管理
```
/claim listclaim - 列出所有领地
/claim queryclaim <领地名称> - 查询领地详细信息
/claim removeclaim <领地名称> - 删除领地
```

#### 权限管理
```
/claim setpermissionstoclaim <领地名称> <玩家> <权限> <true/false>
```
**权限类型**：
- `canEnter` - 进入领地
- `canBreak` - 破坏方块
- `canPlace` - 放置方块
- `canInteract` - 交互（使用门、箱子等）
- `canAttack` - 攻击实体

**示例**：
```
/claim setpermissionstoclaim 我的家 玩家名称 canEnter true
```

#### 高级管理
```
/claim op <玩家> - 给予玩家全局管理权限
/claim deop <玩家> - 移除玩家的全局管理权限
```

### 3. 对话框系统

#### 显示对话框
```
/dialog show <玩家> <对话框ID或SNBT>
```

**使用预定义对话框**：
```
/dialog show @p example:welcome
```

**使用自定义SNBT**：
```
/dialog show @p {"type":"notice","title":"欢迎","body":[{"type":"plain_message","contents":"欢迎来到服务器！"}]}
```

#### 管理命令
```
/dialog list - 列出可用对话框
/dialog reload - 重载对话框配置
/dialog clear <玩家> - 清除玩家当前的对话框
```

#### SNBT格式说明
```json
{
  "type": "notice",           // 对话框类型: notice, confirmation, input
  "title": "标题文本",         // 对话框标题
  "body": [                   // 内容数组
    {
      "type": "plain_message", // 内容类型
      "contents": "消息内容"   // 具体内容
    }
  ]
}
```

## 🌍 多语言支持

模组支持以下语言，会根据客户端语言设置自动切换：
- 简体中文 (zh_cn)
- 繁体中文 (zh_tw) 
- 香港繁体 (zh_hk)
- 文言文 (lzh)

## 🛠️ 管理员命令

### 全局管理
```
/claim op <玩家> - 给予玩家全局领地管理权限
/claim deop <玩家> - 移除权限
```

### 领地修改
```
/claim changeclaim <旧名称> <新名称> <新起始坐标> <新终止坐标> <原主人> <新主人>
```

## ⚠️ 注意事项

1. **权限系统**：领地主人自动拥有所有权限，其他玩家默认无权限
2. **重叠检测**：新领地不能与其他领地重叠
3. **性能考虑**：建议合理设置领地大小和数量
4. **数据保存**：领地数据会自动保存，服务器重启后仍然有效
5. **对话框权限**：`/dialog`命令需要管理员权限（permission level 2）

## 🔧 故障排除

### 常见问题
1. **命令无效**：确保你有足够的权限
2. **领地创建失败**：检查坐标格式和重叠情况
3. **对话框不显示**：检查SNBT格式是否正确
4. **语言不切换**：检查客户端语言设置

### 日志文件
如有问题，请查看服务器日志文件，搜索`umoift`相关错误信息。

## 📞 支持
如有问题，请联系：Hei_wan_Feng@outlook.com