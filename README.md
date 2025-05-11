## Cài đặt Firebase Admin SDK

Để ứng dụng hoạt động đúng, bạn cần tạo file credentials Firebase Admin SDK:

1. Đăng nhập vào [Firebase Console](https://console.firebase.google.com/)
2. Chọn dự án "nihongo-ae96a"
3. Vào **Cài đặt dự án** > **Tài khoản dịch vụ**
4. Chọn **Firebase Admin SDK** > **Tạo khóa mới**
5. Tải xuống file JSON
6. Đổi tên file thành `nihongo-ae96a-firebase-adminsdk-fbsvc-df1b5fe014.json`
7. Lưu file vào thư mục `app/src/main/assets/`

Ví dụ về cách sử dụng Firebase Admin SDK (Node.js):
```javascript
var admin = require("firebase-admin");

var serviceAccount = require("path/to/serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://nihongo-ae96a-default-rtdb.firebaseio.com"
});
```

**Lưu ý quan trọng:** File credentials chứa thông tin nhạy cảm. KHÔNG commit file này lên Git repository!
