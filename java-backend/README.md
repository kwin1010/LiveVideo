# LivePhoto 后端（Java/Spring Boot）

该模块提供一个可被微信 iOS 小程序或其他客户端调用的 Live Photo 转换 REST API。核心逻辑：
1. 通过 `/api/livephoto/convert` 接收 Live Photo 的静态图 (`photo`) 与动态视频 (`video`)；
2. 将素材暂存到本地 `storage/<requestId>` 目录；
3. 调用 `ffmpeg` 将 Live Photo 视频流转码为指定格式（默认 mp4），若上传了静态图则在前 1 秒叠加静态画面；
4. 返回输出文件的绝对路径，供后续下载或上传到对象存储。

## 运行
```bash
cd java-backend
./mvnw spring-boot:run
```
> 如果没有 `./mvnw`，可用系统 Maven：`mvn spring-boot:run`

### 必备依赖
- JDK 17+
- Maven 3.9+
- 本机可执行的 `ffmpeg`

## 配置
`src/main/resources/application.yml` 提供默认配置：
- `server.port`: 启动端口（默认 8080）
- `livephoto.storage-root`: 服务器临时存储目录（默认 `storage`）
- `spring.servlet.multipart.*`: 上传大小限制

## API 说明
`POST /api/livephoto/convert` （`multipart/form-data`）

| 字段 | 是否必填 | 说明 |
| --- | --- | --- |
| `photo` | 否 | Live Photo 的静态图像文件（jpeg/heic/png），若提供将在视频前 1 秒覆盖 |
| `video` | 是 | Live Photo 的动态视频（常见为 `.mov`） |
| `outputFormat` | 否 | 目标格式，默认 `mp4` |
| `requestId` | 否 | 客户端追踪 ID，未提供则自动生成 |

返回：
```json
{
  "requestId": "<traceId>",
  "outputFormat": "mp4",
  "outputPath": "/abs/path/to/storage/<traceId>/livephoto-converted.mp4"
}
```

## 微信小程序/客户端调用示例
使用微信小程序的 `wx.uploadFile` 上传 `photo` 和 `video` 字段，后端返回的 `outputPath` 可用于后续由服务端上传至 OSS/云存储后再提供给小程序播放。

```javascript
wx.uploadFile({
  url: 'https://your-domain.com/api/livephoto/convert',
  filePath: videoPath, // 小程序视频临时路径
  name: 'video',
  formData: {
    requestId: 'wx-demo-001',
    outputFormat: 'mp4'
  },
  success(res) {
    console.log('convert result', JSON.parse(res.data));
  }
});
```

## API 测试文件
仓库提供 `api-test.http` 方便在 VS Code/Idea 的 HTTP Client 中验证：
```http
### Live Photo 转换
POST http://localhost:8080/api/livephoto/convert
Content-Type: multipart/form-data; boundary=WebAppBoundary

--WebAppBoundary
Content-Disposition: form-data; name="photo"; filename="photo.jpg"
Content-Type: image/jpeg

< ./sample/photo.jpg
--WebAppBoundary
Content-Disposition: form-data; name="video"; filename="live.mov"
Content-Type: video/quicktime

< ./sample/live.mov
--WebAppBoundary
Content-Disposition: form-data; name="outputFormat"

mp4
--WebAppBoundary--
```

> 可将 `./sample` 替换为本地素材路径。若未提供 `photo`，接口仍可正常转码视频。
